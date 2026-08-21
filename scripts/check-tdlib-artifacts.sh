#!/usr/bin/env bash
set -euo pipefail

echo "=== TDLib v1.8.66 Android Artifact Integrity & Completeness Check ==="

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
JNI_DIR="$PROJECT_ROOT/app/src/main/jniLibs"
JAVA_BINDING_DIR="$PROJECT_ROOT/app/src/main/java/org/drinkless/tdlib"
MANIFEST_FILE="$PROJECT_ROOT/docs/TDLIB_ARTIFACT_MANIFEST.md"

MISSING_COUNT=0

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
    if ! command -v readelf >/dev/null 2>&1; then
        echo "[ERROR] readelf is required for exact ELF architecture validation"
        MISSING_COUNT=$((MISSING_COUNT + 1))
        return
    fi
    local machine
    machine=$(readelf -h "$file_path" | awk -F: '/Machine:/ {gsub(/^ +/, "", $2); print $2; exit}')
    if [[ "$machine" != "$expected" ]]; then
        echo "[WRONG ARCHITECTURE] $abi expected '$expected', found '$machine'"
        MISSING_COUNT=$((MISSING_COUNT + 1))
    else
        echo "[ARCHITECTURE] $abi is $machine"
    fi
}

echo "1. Checking Artifact Manifest..."
if [ -f "$MANIFEST_FILE" ]; then
    echo "✅ [FOUND] Manifest: $MANIFEST_FILE"
else
    echo "❌ [MISSING] Manifest: $MANIFEST_FILE"
    MISSING_COUNT=$((MISSING_COUNT + 1))
fi

echo ""
echo "2. Checking Native JNI Libraries (.so)..."
check_file "$JNI_DIR/arm64-v8a/libtdjni.so" 5000000 "TDLib v1.8.66 arm64-v8a Native Library" && check_elf_arch "arm64-v8a" "AArch64"
check_file "$JNI_DIR/armeabi-v7a/libtdjni.so" 5000000 "TDLib v1.8.66 armeabi-v7a Native Library" && check_elf_arch "armeabi-v7a" "ARM"
check_file "$JNI_DIR/x86_64/libtdjni.so" 5000000 "TDLib v1.8.66 x86_64 Native Library" && check_elf_arch "x86_64" "Advanced Micro Devices X86-64"

echo ""
echo "3. Checking TDLib Java/JNI Source Bindings..."
check_file "$JAVA_BINDING_DIR/Client.java" 1000 "TDLib Java Client Binding"
check_file "$JAVA_BINDING_DIR/TdApi.java" 1500000 "TDLib v1.8.66 TdApi Bindings"
check_file "$JAVA_BINDING_DIR/Log.java" 1000 "TDLib Java Log Binding"

echo ""
if [ "$MISSING_COUNT" -gt 0 ]; then
    echo "STATUS: TDLIB_ARTIFACTS_PRESENT=false"
    echo "❌ TDLib Artifact Check FAILED: $MISSING_COUNT required artifact(s) missing or incomplete."
    echo "Please compile TDLib externally as described in docs/TDLIB_ANDROID_BUILD.md and place the resulting artifacts in the project."
    exit 1
else
    echo "STATUS: TDLIB_ARTIFACTS_PRESENT=true"
    echo "All required official TDLib v1.8.66 native and Java artifacts verified successfully."
    exit 0
fi
