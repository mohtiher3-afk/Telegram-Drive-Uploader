#!/usr/bin/env bash
set -e

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

echo "1. Checking Artifact Manifest..."
if [ -f "$MANIFEST_FILE" ]; then
    echo "✅ [FOUND] Manifest: $MANIFEST_FILE"
else
    echo "❌ [MISSING] Manifest: $MANIFEST_FILE"
    MISSING_COUNT=$((MISSING_COUNT + 1))
fi

echo ""
echo "2. Checking Native JNI Libraries (.so)..."
check_file "$JNI_DIR/arm64-v8a/libtdjni.so" 5000000 "TDLib v1.8.66 arm64-v8a Native Library"
if command -v readelf >/dev/null 2>&1 && [ -f "$JNI_DIR/arm64-v8a/libtdjni.so" ]; then
    machine=$(readelf -h "$JNI_DIR/arm64-v8a/libtdjni.so" | awk -F: '/Machine:/ {gsub(/^ +/, "", $2); print $2}')
    if [[ "$machine" != *"AArch64"* ]]; then
        echo "⚠️ [WRONG ARCHITECTURE] Expected AArch64, found: $machine"
        MISSING_COUNT=$((MISSING_COUNT + 1))
    else
        echo "✅ [ARCHITECTURE] arm64-v8a is AArch64"
    fi
fi

echo ""
echo "3. Checking TDLib Java/JNI Source Bindings..."
check_file "$JAVA_BINDING_DIR/Client.java" 1000 "TDLib Java Client Binding"
check_file "$JAVA_BINDING_DIR/TdApi.java" 1500000 "TDLib v1.8.66 TdApi Bindings"

echo ""
if [ "$MISSING_COUNT" -gt 0 ]; then
    echo "STATUS: TDLIB_ARTIFACTS_PRESENT=false"
    echo "❌ TDLib Artifact Check FAILED: $MISSING_COUNT required artifact(s) missing or incomplete."
    echo "Please compile TDLib externally as described in docs/TDLIB_ANDROID_BUILD.md and place the resulting artifacts in the project."
    exit 1
else
    echo "STATUS: TDLIB_ARTIFACTS_PRESENT=true"
    echo "🎉 All required official TDLib v1.8.66 ARM64 artifacts verified successfully!"
    exit 0
fi
