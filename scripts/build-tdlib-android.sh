#!/usr/bin/env bash
set -euo pipefail

# Build TDLib native JNI libraries for Android from the official upstream
# source. Produces libtdjni.so per ABI and regenerates Java bindings.
#
# Requirements: NDK 26.3+, CMake 3.22+, Ninja, PHP CLI, gperf, Java 17+.
# OpenSSL shared libraries must already be built via build-openssl-android.sh
# with FORCE_ALL_ABIS=1 so every ABI has headers under install-<abi>/.
#
# Usage:
#   TDLIB_REF=master ./build-tdlib-android.sh          # version derived from source
#   TDLIB_REF=<full-sha> ./build-tdlib-android.sh       # pin an exact commit
#
# TDLib does not publish per-patch git tags: upstream only has v1.8.0 and the
# current 1.8.xx line lives on master, with the version string defined in the
# root CMakeLists.txt. So the checkout ref is a branch/tag/sha, and the exact
# version is derived from the cloned source.

TDLIB_REF="${TDLIB_REF:-master}"
TDLIB_VERSION="${TDLIB_VERSION:-}"        # optional; validated against the source version
TARGET_ABIS="${TARGET_ABIS:-arm64-v8a armeabi-v7a x86_64}"
ANDROID_API="${ANDROID_API:-24}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
CACHE_ROOT="${TDLIB_NATIVE_BUILD_DIR:-$PROJECT_ROOT/.native-build}"

NDK_ROOT="${ANDROID_NDK_ROOT:-${ANDROID_NDK_HOME:-}}"
if [[ -z "$NDK_ROOT" || ! -d "$NDK_ROOT" ]]; then
  echo "ANDROID_NDK_ROOT or ANDROID_NDK_HOME must point to an installed NDK." >&2
  exit 2
fi
command -v cmake >/dev/null || { echo "cmake not found on PATH." >&2; exit 2; }
command -v ninja >/dev/null || { echo "ninja not found on PATH." >&2; exit 2; }
command -v php  >/dev/null || { echo "php not found on PATH (needed by AddIntDef.php)." >&2; exit 2; }

mkdir -p "$CACHE_ROOT"

# ── Stage 0: Fetch TDLib source at the requested ref ───────────────────────
TD_DIR="$CACHE_ROOT/td-${TDLIB_REF//\//_}"
if [[ ! -d "$TD_DIR/.git" ]]; then
  echo "=== Fetching TDLib ref: $TDLIB_REF ==="
  git init -q "$TD_DIR"
  git -C "$TD_DIR" remote add origin https://github.com/tdlib/td.git
fi
git -C "$TD_DIR" fetch --depth 1 origin "$TDLIB_REF"
git -C "$TD_DIR" checkout -q --detach FETCH_HEAD
echo "TDLib source: $TD_DIR @ $(git -C "$TD_DIR" rev-parse --short HEAD)"

# Derive the exact version from the upstream source (root CMakeLists.txt)
# because TDLib does not tag per-patch releases.
SRC_VERSION="$(awk '/^project\(TDLib VERSION/ { for (i = 1; i <= NF; i++) if ($i == "VERSION") { print $(i + 1); exit } }' "$TD_DIR/CMakeLists.txt")"
if [[ -z "$SRC_VERSION" ]]; then
  echo "Could not read TDLib version from $TD_DIR/CMakeLists.txt" >&2
  exit 1
fi
if [[ -n "$TDLIB_VERSION" && "$TDLIB_VERSION" != "$SRC_VERSION" ]]; then
  echo "Requested TDLIB_VERSION=$TDLIB_VERSION but ref '$TDLIB_REF' is v$SRC_VERSION. Aborting to avoid mislabeling." >&2
  exit 1
fi
TDLIB_VERSION="$SRC_VERSION"
echo "Resolved TDLib version: v$TDLIB_VERSION"
# Export the resolved version so later workflow steps (branch/PR/check) use it.
if [[ -n "${GITHUB_ENV:-}" ]]; then
  echo "TDLIB_VERSION=$TDLIB_VERSION" >> "$GITHUB_ENV"
fi

# ── Stage 1: Host build of source generators + generated C++ sources ───────
# The project root for both host and cross builds is example/android (that is
# where the tdjni target lives).
echo "=== Stage 1: Building TDLib generators (host) ==="
BUILD_NATIVE="$CACHE_ROOT/build-native-TDLib-${TDLIB_VERSION}"
cmake -S "$TD_DIR/example/android" -B "$BUILD_NATIVE" -G Ninja \
  -DCMAKE_BUILD_TYPE=Release \
  -DTD_GENERATE_SOURCE_FILES=ON
cmake --build "$BUILD_NATIVE"

# ── Stage 2: Generate Java bindings ────────────────────────────────────────
echo "=== Stage 2: Generating Java bindings ==="
cmake --build "$BUILD_NATIVE" --target tl_generate_java

TDAPI_SRC="$TD_DIR/example/android/org/drinkless/tdlib/TdApi.java"
if [[ ! -f "$TDAPI_SRC" ]]; then
  # Fall back to a build-tree search in case the generator output path differs.
  TDAPI_SRC="$(find "$BUILD_NATIVE" "$TD_DIR/example/android" -name TdApi.java -path '*drinkless*' 2>/dev/null | head -n 1 || true)"
fi
if [[ -z "$TDAPI_SRC" || ! -f "$TDAPI_SRC" ]]; then
  echo "TdApi.java generation failed — not found." >&2
  exit 1
fi

# AddIntDef.php adds Android @IntDef annotations (official example flow).
( cd "$TD_DIR/example/android" && php AddIntDef.php "$TDAPI_SRC" ) || \
  echo "⚠️  AddIntDef.php failed — continuing without @IntDef annotations." >&2

JAVA_OUT="$CACHE_ROOT/tdlib-java-${TDLIB_VERSION}"
mkdir -p "$JAVA_OUT/org/drinkless/tdlib"
cp -f "$TDAPI_SRC" "$JAVA_OUT/org/drinkless/tdlib/TdApi.java"
cp -f "$TD_DIR/example/java/org/drinkless/tdlib/Client.java" \
      "$JAVA_OUT/org/drinkless/tdlib/Client.java"
echo "Java bindings staged in $JAVA_OUT"

# ── Stage 3: Per-ABI cross-compilation ─────────────────────────────────────
echo "=== Stage 3: Building libtdjni.so per ABI ==="
for ABI in $TARGET_ABIS; do
  echo "--- Building ABI: $ABI ---"
  BUILD_ABI="$CACHE_ROOT/build-${ABI}-TDLib-${TDLIB_VERSION}"
  OPENSSL_DIR="$CACHE_ROOT/install-${ABI}"
  if [[ ! -d "$OPENSSL_DIR/include/openssl" ]]; then
    echo "OpenSSL headers missing at $OPENSSL_DIR — run build-openssl-android.sh with FORCE_ALL_ABIS=1 first." >&2
    exit 1
  fi

  cmake -S "$TD_DIR/example/android" -B "$BUILD_ABI" -G Ninja \
    -DCMAKE_TOOLCHAIN_FILE="$NDK_ROOT/build/cmake/android.toolchain.cmake" \
    -DCMAKE_BUILD_TYPE=RelWithDebInfo \
    -DANDROID_ABI="$ABI" \
    -DANDROID_STL=c++_static \
    -DANDROID_PLATFORM="android-${ANDROID_API}" \
    -DOPENSSL_ROOT_DIR="$OPENSSL_DIR" \
    -DOPENSSL_USE_STATIC_LIBS=OFF
  cmake --build "$BUILD_ABI" --target tdjni

  SO_FILE="$(find "$BUILD_ABI" -maxdepth 3 -name 'libtdjni.so' | head -n 1)"
  if [[ -z "$SO_FILE" ]]; then
    echo "libtdjni.so not produced for $ABI." >&2
    exit 1
  fi
  DEST="$PROJECT_ROOT/app/src/main/jniLibs/$ABI"
  mkdir -p "$DEST"
  cp -f "$SO_FILE" "$DEST/libtdjni.so"
  echo "$ABI: installed $(wc -c < "$DEST/libtdjni.so") bytes → $DEST/libtdjni.so"
done

# ── Stage 4: Install Java bindings into the project ────────────────────────
echo "=== Stage 4: Installing Java bindings ==="
JAVA_DEST="$PROJECT_ROOT/app/src/main/java/org/drinkless/tdlib"
mkdir -p "$JAVA_DEST"
cp -f "$JAVA_OUT/org/drinkless/tdlib/TdApi.java" "$JAVA_DEST/TdApi.java"
cp -f "$JAVA_OUT/org/drinkless/tdlib/Client.java" "$JAVA_DEST/Client.java"
# Log.java is hand-written and stays untouched.
echo "TdApi.java:   $(wc -l < "$JAVA_DEST/TdApi.java") lines"
echo "Client.java:  $(wc -l < "$JAVA_DEST/Client.java") lines"

# ── Stage 5: Refresh OpenSSL shared libraries shipped beside libtdjni.so ───
echo "=== Stage 5: Refreshing packaged OpenSSL libraries ==="
for ABI in $TARGET_ABIS; do
  SRC="$CACHE_ROOT/install-${ABI}/lib"
  DEST="$PROJECT_ROOT/app/src/main/jniLibs/$ABI"
  for lib in libssl.so libcrypto.so; do
    if [[ -f "$SRC/$lib" ]]; then
      cp -f "$SRC/$lib" "$DEST/$lib"
      echo "$ABI: packaged $DEST/$lib"
    fi
  done
done

# ── Stage 6: Regenerate SHA-256 checksums ──────────────────────────────────
echo "=== Stage 6: Regenerating SHA-256 checksums ==="
CHECKSUM_FILE="$PROJECT_ROOT/docs/TDLIB_SHA256SUMS.txt"
{
  echo "# TDLib v${TDLIB_VERSION} Android native artifacts — generated $(date -u +%Y-%m-%dT%H:%M:%SZ)"
  for ABI in $TARGET_ABIS; do
    find "$PROJECT_ROOT/app/src/main/jniLibs/$ABI" -maxdepth 1 -name '*.so' -print0 \
      | sort -z \
      | xargs -0 -r sha256sum
  done
} > "$CHECKSUM_FILE"
echo "Checksums written: $CHECKSUM_FILE"

# ── Stage 7: Update manifest version references ────────────────────────────
echo "=== Stage 7: Updating manifest ==="
MANIFEST="$PROJECT_ROOT/docs/TDLIB_ARTIFACT_MANIFEST.md"
if [[ -f "$MANIFEST" ]]; then
  sed -i "s/TDLib v[0-9][0-9.]*/TDLib v${TDLIB_VERSION}/g" "$MANIFEST"
  echo "Manifest updated: $MANIFEST (review before committing)"
fi

echo ""
echo "✅ TDLib v${TDLIB_VERSION} build complete."
echo "   JNI:     app/src/main/jniLibs/<abi>/libtdjni.so"
echo "   Java:    $JAVA_DEST/TdApi.java, Client.java"
echo "   SHA256:  $CHECKSUM_FILE"
echo "   Next:    TDLIB_VERSION=${TDLIB_VERSION} ./scripts/check-tdlib-artifacts.sh"
