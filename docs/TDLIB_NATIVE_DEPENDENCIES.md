# TDLib Android Native Runtime Dependencies

The checked-in TDLib v1.8.66 JNI binaries must be loaded together with every non-system ELF dependency they request. The Android emulator smoke test exposed that the `armeabi-v7a` and `x86_64` `libtdjni.so` files request `libssl.so` and `libcrypto.so`; Android does not provide those names as application-loadable libraries in the test namespace.

The project therefore builds the required OpenSSL shared libraries from the official OpenSSL 3.0.16 source and packages them beside the matching TDLib JNI library. The source archive is downloaded from the official OpenSSL release and verified against its published SHA-256 checksum by `scripts/build-openssl-android.sh`. No mock or substitute implementation is used.

## Local preparation

Install Android NDK `26.3.11579264`, set `ANDROID_NDK_ROOT` or `ANDROID_NDK_HOME`, and run:

```bash
ANDROID_NDK_ROOT="$ANDROID_HOME/ndk/26.3.11579264" \
  TARGET_ABIS="arm64-v8a armeabi-v7a x86_64" \
  ./scripts/build-openssl-android.sh
```

The script only builds OpenSSL for an ABI whose checked-in `libtdjni.so` actually requests `libssl.so`. It writes the resulting `libssl.so`, `libcrypto.so`, and their versioned counterparts into the matching `app/src/main/jniLibs/<abi>/` directory.

## Verification

Run:

```bash
./scripts/check-tdlib-artifacts.sh
```

The checker validates the ELF headers, architecture, Java bindings, and every `libssl.so` or `libcrypto.so` dependency requested by each TDLib binary. The Android emulator smoke test then proves the packaged libraries can load together and that a real `org.drinkless.tdlib.Client` can be created.

`libz.so`, `liblog.so`, `libdl.so`, `libm.so`, and `libc.so` remain Android platform dependencies. They are not copied into the application because the Android platform supplies them.
