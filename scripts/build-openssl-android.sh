#!/usr/bin/env bash
set -euo pipefail

# Build only the OpenSSL shared libraries required by the checked-in TDLib JNI
# binaries. The source is pinned to the official OpenSSL 3.0.16 release.
OPENSSL_VERSION="${OPENSSL_VERSION:-3.0.16}"
OPENSSL_SHA256="57e03c50feab5d31b152af2b764f10379aecd8ee92f16c985983ce4a99f7ef86"
ANDROID_API="${ANDROID_API:-24}"
TARGET_ABIS="${TARGET_ABIS:-arm64-v8a armeabi-v7a x86_64}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
CACHE_ROOT="${TDLIB_NATIVE_BUILD_DIR:-$PROJECT_ROOT/.native-build}"
SOURCE_ARCHIVE="$CACHE_ROOT/openssl-${OPENSSL_VERSION}.tar.gz"
SOURCE_ROOT="$CACHE_ROOT/openssl-${OPENSSL_VERSION}"
NDK_ROOT="${ANDROID_NDK_ROOT:-${ANDROID_NDK_HOME:-}}"

if [[ -z "$NDK_ROOT" || ! -x "$NDK_ROOT/toolchains/llvm/prebuilt/linux-x86_64/bin/clang" ]]; then
  echo "ANDROID_NDK_ROOT or ANDROID_NDK_HOME must point to an installed Android NDK." >&2
  exit 2
fi

TOOLCHAIN="$NDK_ROOT/toolchains/llvm/prebuilt/linux-x86_64"
SYSROOT="$TOOLCHAIN/sysroot"
mkdir -p "$CACHE_ROOT"

if [[ ! -f "$SOURCE_ARCHIVE" ]]; then
  curl --fail --location --retry 3 --output "$SOURCE_ARCHIVE" \
    "https://github.com/openssl/openssl/releases/download/openssl-${OPENSSL_VERSION}/openssl-${OPENSSL_VERSION}.tar.gz"
fi
printf '%s  %s\n' "$OPENSSL_SHA256" "$SOURCE_ARCHIVE" | sha256sum --check --status

if [[ ! -d "$SOURCE_ROOT" ]]; then
  tar -xzf "$SOURCE_ARCHIVE" -C "$CACHE_ROOT"
fi

build_abi() {
  local abi="$1"
  local configure_target
  local host_prefix
  local target_triple
  local cflags="-fPIC -O2 -D__ANDROID_API__=${ANDROID_API}"

  case "$abi" in
    arm64-v8a)
      configure_target="android-arm64"
      host_prefix="aarch64-linux-android"
      target_triple="aarch64-linux-android"
      ;;
    armeabi-v7a)
      configure_target="android-arm"
      host_prefix="armv7a-linux-androideabi"
      target_triple="armv7a-linux-androideabi"
      cflags="$cflags -march=armv7-a -mfloat-abi=softfp"
      ;;
    x86_64)
      configure_target="android-x86_64"
      host_prefix="x86_64-linux-android"
      target_triple="x86_64-linux-android"
      ;;
    *)
      echo "Unsupported ABI: $abi" >&2
      exit 2
      ;;
  esac

  local tdjni="$PROJECT_ROOT/app/src/main/jniLibs/$abi/libtdjni.so"
  if ! readelf -d "$tdjni" | grep -q 'Shared library: \[libssl.so\]'; then
    echo "${abi}: libtdjni.so does not require libssl.so; no OpenSSL build needed."
    return 0
  fi

  local build_dir="$CACHE_ROOT/build-$abi"
  local install_dir="$CACHE_ROOT/install-$abi"
  local wrapper_dir="$CACHE_ROOT/toolchain-$abi/bin"
  local compiler_name="${host_prefix}${ANDROID_API}-clang"
  local wrapper="$wrapper_dir/$compiler_name"
  rm -rf "$build_dir" "$install_dir" "$wrapper_dir"
  mkdir -p "$build_dir" "$install_dir" "$wrapper_dir"

  # Some NDK distributions do not ship API-specific clang launcher scripts for
  # every API level. Use a deterministic wrapper around the NDK clang binary.
  cat > "$wrapper" <<EOF
#!/usr/bin/env bash
exec "$TOOLCHAIN/bin/clang" --target=${target_triple}${ANDROID_API} --sysroot="$SYSROOT" "\$@"
EOF
  chmod +x "$wrapper"

  pushd "$build_dir" >/dev/null
  PATH="$wrapper_dir:$TOOLCHAIN/bin:$PATH" \
    CC="$wrapper" \
    AR="$TOOLCHAIN/bin/llvm-ar" \
    RANLIB="$TOOLCHAIN/bin/llvm-ranlib" \
    "$SOURCE_ROOT/Configure" "$configure_target" shared no-tests no-engine no-legacy \
      --prefix="$install_dir" --openssldir="$install_dir/ssl" \
      -D__ANDROID_API__="$ANDROID_API" \
      -fPIC \
      -static-libgcc \
      -Wl,-z,max-page-size=16384
  PATH="$wrapper_dir:$TOOLCHAIN/bin:$PATH" \
    make -j"${OPENSSL_JOBS:-2}" build_libs \
      CC="$wrapper" AR="$TOOLCHAIN/bin/llvm-ar" RANLIB="$TOOLCHAIN/bin/llvm-ranlib" \
      CFLAGS="$cflags"
  popd >/dev/null

  local destination="$PROJECT_ROOT/app/src/main/jniLibs/$abi"
  mkdir -p "$destination"
  local crypto_so ssl_so
  crypto_so="$(find "$build_dir" -maxdepth 4 \( -type f -o -type l \) -name 'libcrypto.so*' | sort | head -n 1)"
  ssl_so="$(find "$build_dir" -maxdepth 4 \( -type f -o -type l \) -name 'libssl.so*' | sort | head -n 1)"
  if [[ -z "$crypto_so" || -z "$ssl_so" ]]; then
    echo "OpenSSL build did not produce both shared libraries for $abi." >&2
    find "$build_dir" "$install_dir" -maxdepth 5 \( -type f -o -type l \) -name 'lib*.so*' -print >&2 || true
    exit 1
  fi

  # TDLib's existing ELF files request the unversioned names. Keep the
  # versioned files too because libssl.so.* may request libcrypto.so.*.
  cp -Lf "$crypto_so" "$destination/$(basename "$crypto_so")"
  cp -Lf "$ssl_so" "$destination/$(basename "$ssl_so")"
  cp -Lf "$crypto_so" "$destination/libcrypto.so"
  cp -Lf "$ssl_so" "$destination/libssl.so"

  echo "${abi}: OpenSSL shared libraries installed in $destination"
}

for abi in $TARGET_ABIS; do
  build_abi "$abi"
done
