#!/usr/bin/env bash
set -euo pipefail

TDLIB_VERSION="${TDLIB_VERSION:-1.8.66}"

echo "=== TDLib v${TDLIB_VERSION} Android Artifact Integrity & Completeness Check ==="

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
JNI_DIR="$PROJECT_ROOT/data/src/main/jniLibs"
JAVA_BINDING_DIR="$PROJECT_ROOT/data/src/main/java/org/drinkless/tdlib"
MANIFEST_FILE="$PROJECT_ROOT/docs/TDLIB_ARTIFACT_MANIFEST.md"
CHECKSUM_FILE="$PROJECT_ROOT/docs/TDLIB_SHA256SUMS.txt"
ELF_ALIGNMENT_HELPER="scripts/check-elf-alignment.py"

MISSING_COUNT=0
CHECK_ABI="${TDLIB_CHECK_ABI:-armeabi-v7a}"

# Pick a Python interpreter once. `python` is tried before `python3` because on
# Windows `python3` is often the Microsoft Store stub, which prints an install
# banner and exits non-zero.
python_cmd() {
    if command -v python >/dev/null 2>&1; then
        echo python
    elif command -v python3 >/dev/null 2>&1; then
        echo python3
    else
        return 1
    fi
}

# Print the ELF e_machine as a readelf-compatible name. The helper is invoked with
# a REPOSITORY-RELATIVE path on purpose: PROJECT_ROOT is an MSYS path such as
# /c/Users/..., and a native Windows python cannot open that (it becomes
# C:\c\Users\...). A relative path works in both environments. The old code passed
# the absolute MSYS path, so the helper silently failed and the 16 KB gate reported
# "no LOAD segments readable" on every ABI.
python_elf_machine() {
    local file_path="$1"
    local relative="${file_path#"$PROJECT_ROOT"/}"
    ( cd "$PROJECT_ROOT" && "$(python_cmd)" "$ELF_ALIGNMENT_HELPER" --machine "$relative" 2>/dev/null )
}
case "$CHECK_ABI" in
    all|arm64-v8a|armeabi-v7a|x86_64) ;;
    *)
        echo "Unsupported TDLIB_CHECK_ABI: $CHECK_ABI" >&2
        exit 2
        ;;
esac

should_check_abi() {
    local abi="$1"
    [[ "$CHECK_ABI" == "all" || "$CHECK_ABI" == "$abi" ]]
}

check_file() {
    local file_path="$1"
    local min_size_bytes="$2"
    local desc="$3"

    if [ ! -f "$file_path" ]; then
        echo "❌ [MISSING] $desc: $file_path"
        MISSING_COUNT=$((MISSING_COUNT + 1))
    else
        local actual_size
        actual_size=$(wc -c < "$file_path")
        if [ "$actual_size" -lt "$min_size_bytes" ]; then
            echo "⚠️ [INVALID SIZE] $desc is too small ($actual_size bytes < $min_size_bytes bytes): $file_path"
            MISSING_COUNT=$((MISSING_COUNT + 1))
        else
            # Check if native library has valid ELF magic bytes (7f 45 4c 46)
            if [[ "$file_path" == *.so ]]; then
                if command -v readelf >/dev/null 2>&1; then
                    if ! readelf -h "$file_path" >/dev/null 2>&1; then
                        echo "⚠️ [CORRUPT ELF] $desc failed readelf verification: $file_path"
                        MISSING_COUNT=$((MISSING_COUNT + 1))
                        return
                    fi
                elif command -v file >/dev/null 2>&1; then
                    if ! file "$file_path" | grep -iq "ELF"; then
                        echo "⚠️ [INVALID ELF] $desc is not an ELF binary: $file_path"
                        MISSING_COUNT=$((MISSING_COUNT + 1))
                        return
                    fi
                fi
            fi
            echo "✅ [FOUND & VALID] $desc ($actual_size bytes)"
        fi
    fi
}

check_elf_arch() {
    local abi="$1"
    local expected="$2"
    local file_path="$JNI_DIR/$abi/libtdjni.so"

    # An ELF reader is mandatory: without one we cannot tell an AArch64 library from
    # an x86-64 one, and a wrong-architecture binary is a hard runtime failure. The
    # previous `file`-based fallback accepted ANY shared object (its output always
    # contains "ELF"), which is exactly how three cross-placed libraries went
    # undetected. Fail loudly instead of guessing.
    local machine=""
    if command -v readelf >/dev/null 2>&1; then
        machine=$(readelf -h "$file_path" 2>/dev/null | awk -F: '/Machine:/ {gsub(/^ +/, "", $2); print $2; exit}')
    elif command -v llvm-readelf >/dev/null 2>&1; then
        machine=$(llvm-readelf -h "$file_path" 2>/dev/null | awk -F: '/Machine:/ {gsub(/^ +/, "", $2); print $2; exit}')
    elif [ -f "$PROJECT_ROOT/$ELF_ALIGNMENT_HELPER" ] && python_cmd >/dev/null 2>&1; then
        machine=$(python_elf_machine "$file_path")
    fi

    if [ -z "$machine" ]; then
        echo "[ERROR] $abi: no ELF reader available (need readelf or llvm-readelf); cannot verify architecture, refusing to pass"
        MISSING_COUNT=$((MISSING_COUNT + 1))
        return 1
    fi

    if [ "$machine" != "$expected" ]; then
        echo "[WRONG ARCHITECTURE] $abi expected '$expected', found '$machine'"
        MISSING_COUNT=$((MISSING_COUNT + 1))
        return 1
    fi
    echo "[ARCHITECTURE] $abi is $machine"
    return 0
}

# Verify 16 KB ELF page-size alignment of LOAD segments (Google Play
# requirement for apps targeting API 35+). Accepts p_align >= 0x4000 (16384).
# Uses `readelf -lW` (single-line LOAD rows, last field = Align in hex),
# `llvm-objdump -p` (`align 2**N`), or the Python ELF helper as a portable
# fallback, parsed with portable bash arithmetic.
check_16kb_alignment() {
    local abi="$1"
    local file_path="$JNI_DIR/$abi/libtdjni.so"
    local relative="${file_path#"$PROJECT_ROOT"/}"

    local aligns=""
    if command -v readelf >/dev/null 2>&1; then
        aligns=$(readelf -lW "$file_path" 2>/dev/null | awk '/[[:space:]]LOAD[[:space:]]/{print $NF}')
    fi
    if [ -z "$aligns" ] && command -v llvm-readelf >/dev/null 2>&1; then
        aligns=$(llvm-readelf -lW "$file_path" 2>/dev/null | awk '/[[:space:]]LOAD[[:space:]]/{print $NF}')
    fi
    # The helper must be called with a repository-relative path from inside the
    # project: an absolute MSYS path is not openable by a native Windows python.
    if [ -z "$aligns" ] && [ -f "$PROJECT_ROOT/$ELF_ALIGNMENT_HELPER" ] && python_cmd >/dev/null 2>&1; then
        aligns=$( cd "$PROJECT_ROOT" && "$(python_cmd)" "$ELF_ALIGNMENT_HELPER" "$relative" 2>/dev/null || true )
    fi
    aligns=${aligns//$'\r'/}

    if [ -z "$aligns" ]; then
        echo "[16KB CHECK] $abi: FAILED - no ELF reader available (need readelf or llvm-readelf); cannot verify 16 KB alignment"
        MISSING_COUNT=$((MISSING_COUNT + 1))
        return 1
    fi

    local failed=""
    local v h had_hex=0
    for v in $aligns; do
        case "$v" in
            0x*) h="${v#0x}"; had_hex=1; if (( 16#$h < 16384 )); then failed="$failed $v"; fi ;;
            2**) n="${v#2\*\*}"; had_hex=1; if (( (1 << n) < 16384 )); then failed="$failed $v"; fi ;;
            *) echo "[16KB CHECK] $abi: unparseable align token '$v'; manual verification required"
               MISSING_COUNT=$((MISSING_COUNT + 1))
               return ;;
        esac
    done
    if [ "$had_hex" -eq 0 ]; then
        echo "[16KB CHECK] $abi: no align values found"
        MISSING_COUNT=$((MISSING_COUNT + 1))
        return
    fi

    if [ -n "$failed" ]; then
        echo "[16KB CHECK] $abi: NOT 16 KB aligned (segments below 0x4000:$failed)"
        MISSING_COUNT=$((MISSING_COUNT + 1))
    else
        echo "[16KB CHECK] $abi: LOAD segments aligned (aligns: $(echo $aligns | tr '\n' ' '))"
    fi
}

check_runtime_dependencies() {
    local abi="$1"
    local file_path="$JNI_DIR/$abi/libtdjni.so"
    local dependency
    while IFS= read -r dependency; do
        case "$dependency" in
            libssl.so|libcrypto.so)
                check_file "$JNI_DIR/$abi/$dependency" 100000 "TDLib $abi runtime dependency $dependency"
                ;;
        esac
    done < <(readelf -d "$file_path" | sed -n 's/.*Shared library: \[\([^]]*\)\].*/\1/p')
}

echo "1. Checking Artifact Manifest..."
if [ -f "$MANIFEST_FILE" ]; then
    echo "✅ [FOUND] Manifest: $MANIFEST_FILE"
else
    echo "❌ [MISSING] Manifest: $MANIFEST_FILE"
    MISSING_COUNT=$((MISSING_COUNT + 1))
fi

echo ""
echo "1b. Verifying documented SHA-256 checksums..."
if [ ! -f "$CHECKSUM_FILE" ]; then
    echo "❌ [MISSING] Checksum file: $CHECKSUM_FILE"
    MISSING_COUNT=$((MISSING_COUNT + 1))
elif ! command -v sha256sum >/dev/null 2>&1; then
    echo "❌ [MISSING] sha256sum is required to verify documented artifacts"
    MISSING_COUNT=$((MISSING_COUNT + 1))
else
    checksum_entries=0
    while read -r expected relative_path; do
        expected=${expected%$'\r'}
        relative_path=${relative_path%$'\r'}
        [[ -z "$expected" || "$expected" == \# ]] && continue
        checksum_entries=$((checksum_entries + 1))
        # The checksum file was authored against app/src/main/jniLibs/**, but the
        # libraries that ship live in data/src/main/jniLibs/** (app/ only ever held a
        # single arm64-v8a set, which is why 6 of 9 entries reported MISSING TARGET).
        # Rewrite the prefix so the documented hashes are compared against the files
        # that are actually packaged.
        checksum_path=$relative_path
        case "$checksum_path" in
            app/src/main/jniLibs/*) checksum_path="data/src/main/jniLibs/${checksum_path#app/src/main/jniLibs/}" ;;
        esac
        artifact="$PROJECT_ROOT/$checksum_path"
        if [ ! -f "$artifact" ]; then
            echo "❌ [MISSING CHECKSUM TARGET] $relative_path (looked in $checksum_path)"
            MISSING_COUNT=$((MISSING_COUNT + 1))
            continue
        fi
        actual=$(sha256sum "$artifact" | awk '{print $1}')
        if [ "$actual" != "$expected" ]; then
            echo "❌ [CHECKSUM MISMATCH] $checksum_path"
            MISSING_COUNT=$((MISSING_COUNT + 1))
        else
            echo "✅ [CHECKSUM] $checksum_path"
        fi
    done < "$CHECKSUM_FILE"
    if [ "$checksum_entries" -eq 0 ]; then
        echo "❌ [EMPTY CHECKSUM FILE] $CHECKSUM_FILE"
        MISSING_COUNT=$((MISSING_COUNT + 1))
    fi
fi

echo ""
echo "2. Checking Native JNI Libraries (.so)..."
if should_check_abi "arm64-v8a"; then
    check_file "$JNI_DIR/arm64-v8a/libtdjni.so" 5000000 "TDLib v${TDLIB_VERSION} arm64-v8a Native Library" && check_elf_arch "arm64-v8a" "AArch64"
    check_16kb_alignment "arm64-v8a"
fi
if should_check_abi "armeabi-v7a"; then
    check_file "$JNI_DIR/armeabi-v7a/libtdjni.so" 5000000 "TDLib v${TDLIB_VERSION} armeabi-v7a Native Library" && check_elf_arch "armeabi-v7a" "ARM"
    check_16kb_alignment "armeabi-v7a"
fi
if should_check_abi "x86_64"; then
    check_file "$JNI_DIR/x86_64/libtdjni.so" 5000000 "TDLib v${TDLIB_VERSION} x86_64 Native Library" && check_elf_arch "x86_64" "Advanced Micro Devices X86-64"
    check_16kb_alignment "x86_64"
fi

if command -v readelf >/dev/null 2>&1; then
    echo ""
    echo "2b. Checking Native Runtime Dependencies..."
    if should_check_abi "arm64-v8a"; then check_runtime_dependencies "arm64-v8a"; fi
    if should_check_abi "armeabi-v7a"; then check_runtime_dependencies "armeabi-v7a"; fi
    if should_check_abi "x86_64"; then check_runtime_dependencies "x86_64"; fi
fi

echo ""
echo "3. Checking TDLib Java/JNI Source Bindings..."
check_file "$JAVA_BINDING_DIR/Client.java" 1000 "TDLib Java Client Binding"
check_file "$JAVA_BINDING_DIR/TdApi.java" 1500000 "TDLib v${TDLIB_VERSION} TdApi Bindings"
if [ -f "$JAVA_BINDING_DIR/Log.kt" ]; then
    check_file "$JAVA_BINDING_DIR/Log.kt" 1000 "TDLib Kotlin Log Binding"
elif [ -f "$JAVA_BINDING_DIR/Log.java" ]; then
    check_file "$JAVA_BINDING_DIR/Log.java" 1000 "TDLib Java Log Binding"
else
    echo "❌ [MISSING] TDLib Log binding (Log.java or Log.kt)"
    MISSING_COUNT=$((MISSING_COUNT + 1))
fi

echo ""
if [ "$MISSING_COUNT" -gt 0 ]; then
    echo "STATUS: TDLIB_ARTIFACTS_PRESENT=false"
    echo "❌ TDLib Artifact Check FAILED: $MISSING_COUNT required artifact(s) missing or incomplete."
    echo "Please run scripts/build-openssl-android.sh with Android NDK 26.3.11579264, or place the official dependency artifacts in the matching ABI directories, then rerun this check."
    exit 1
else
    echo "STATUS: TDLIB_ARTIFACTS_PRESENT=true"
    echo "All required official TDLib v${TDLIB_VERSION} native and Java artifacts verified successfully for ABI scope '$CHECK_ABI'."
    exit 0
fi
