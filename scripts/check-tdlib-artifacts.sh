#!/usr/bin/env bash
set -e

echo "=== TDLib Android Artifact Integrity & Completeness Check ==="

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
JNI_DIR="$PROJECT_ROOT/app/src/main/jniLibs"
JAVA_BINDING_DIR="$PROJECT_ROOT/app/src/main/java/org/drinkless/tdlib"
MANIFEST_FILE="$PROJECT_ROOT/docs/TDLIB_ARTIFACT_MANIFEST.md"

JAVA_BINDINGS_PRESENT=false
NATIVE_TDLIB_PRESENT=false

echo "1. Checking Artifact Manifest..."
if [ -f "$MANIFEST_FILE" ]; then
    echo "✅ [FOUND] Manifest: $MANIFEST_FILE"
else
    echo "❌ [MISSING] Manifest: $MANIFEST_FILE"
fi

echo ""
echo "2. Checking TDLib Java/JNI Source Bindings..."
JAVA_OK=true
for java_file in Client.java TdApi.java Log.java; do
    target="$JAVA_BINDING_DIR/$java_file"
    if [ -s "$target" ]; then
        echo "✅ [FOUND] Java Binding: $target ($(wc -c < "$target") bytes)"
    else
        echo "❌ [MISSING/EMPTY] Java Binding: $target"
        JAVA_OK=false
    fi
done

if [ "$JAVA_OK" = true ]; then
    JAVA_BINDINGS_PRESENT=true
fi

echo ""
echo "3. Checking Native JNI Libraries (.so)..."
NATIVE_OK=true
for abi in arm64-v8a armeabi-v7a x86_64; do
    so_file="$JNI_DIR/$abi/libtdjni.so"
    if [ ! -f "$so_file" ]; then
        echo "❌ [MISSING] Native Library ($abi): $so_file"
        NATIVE_OK=false
    elif [ ! -s "$so_file" ]; then
        echo "⚠️ [EMPTY] Native Library ($abi): $so_file (0 bytes)"
        NATIVE_OK=false
    else
        # Structural ELF verification
        if command -v readelf >/dev/null 2>&1; then
            if readelf -h "$so_file" >/dev/null 2>&1; then
                echo "✅ [FOUND & VALID ELF] Native Library ($abi): $so_file ($(wc -c < "$so_file") bytes)"
            else
                echo "⚠️ [CORRUPT ELF] Native Library ($abi) failed readelf verification: $so_file"
                NATIVE_OK=false
            fi
        elif command -v file >/dev/null 2>&1; then
            if file "$so_file" | grep -iq "ELF"; then
                echo "✅ [FOUND & VALID ELF] Native Library ($abi): $so_file ($(wc -c < "$so_file") bytes)"
            else
                echo "⚠️ [INVALID ELF] Native Library ($abi) is not an ELF binary: $so_file"
                NATIVE_OK=false
            fi
        else
            echo "✅ [FOUND] Native Library ($abi): $so_file ($(wc -c < "$so_file") bytes)"
        fi
    fi
done

if [ "$NATIVE_OK" = true ]; then
    NATIVE_TDLIB_PRESENT=true
fi

echo ""
echo "=== SUMMARY ==="
echo "JAVA_BINDINGS_PRESENT=$JAVA_BINDINGS_PRESENT"
echo "NATIVE_TDLIB_PRESENT=$NATIVE_TDLIB_PRESENT"

if [ "$JAVA_BINDINGS_PRESENT" = true ] && [ "$NATIVE_TDLIB_PRESENT" = true ]; then
    echo "STATUS: TDLIB_ARTIFACTS_PRESENT=true"
    echo "🎉 All required TDLib Android artifacts verified successfully!"
    exit 0
else
    echo "STATUS: TDLIB_ARTIFACTS_PRESENT=false"
    if [ "$JAVA_BINDINGS_PRESENT" = true ] && [ "$NATIVE_TDLIB_PRESENT" = false ]; then
        echo "⚠️ Gap Analysis: Java bindings present, but native TDLib binaries (libtdjni.so) are MISSING."
    fi
    exit 1
fi
