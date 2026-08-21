# TDLib Android Artifact Manifest

## 1. Upstream Source Information

| Field | Value |
|---|---|
| Repository | https://github.com/tdlib/td |
| Version | `v1.8.66` |
| Pinned source commit | `022d60202e446ad1287b9fb68e687c8a0760788b` |
| Target interface | Official Java / Android JNI (`org.drinkless.tdlib`) |
| Android ABIs | `arm64-v8a` / AArch64, `armeabi-v7a` / ARM, `x86_64` / X86-64 |
| Android platform | API 24 |
| Android NDK | `26.3.11579264` |
| CMake | `3.22.1` |
| OpenSSL | `3.0.16` |
| Zlib | `1.3.1` |

The source was generated and built from the official TDLib repository. No mock, stub, or fabricated native implementation is included. The Java bindings and `libtdjni.so` were generated from the same source revision.

## 2. Target Architecture

The Android project is packaged as separate ABI APKs for **arm64-v8a**, **armeabi-v7a**, and **x86_64**. Each APK contains exactly one matching v1.8.66 `libtdjni.so`; universal APK generation remains disabled to avoid unnecessary size.

## 3. Artifact Verification Table

| Artifact | Relative path | Size | Status |
|---|---|---:|---|
| ARM64 JNI | `app/src/main/jniLibs/arm64-v8a/libtdjni.so` | 58,944,152 bytes | PASS; stripped ELF AArch64 |
| ARMv7 JNI | `app/src/main/jniLibs/armeabi-v7a/libtdjni.so` | 23,441,904 bytes | PASS; stripped ELF ARM EABI5 |
| x86_64 JNI | `app/src/main/jniLibs/x86_64/libtdjni.so` | 36,146,336 bytes | PASS; stripped ELF X86-64 |
| Client binding | `app/src/main/java/org/drinkless/tdlib/Client.java` | 11,015 bytes | PASS |
| Log binding | `app/src/main/java/org/drinkless/tdlib/Log.java` | 3,401 bytes | PASS |
| TdApi binding | `app/src/main/java/org/drinkless/tdlib/TdApi.java` | 1,757,224 bytes | PASS |

The mandatory checker reports `TDLIB_ARTIFACTS_PRESENT=true`, verifies the ELF header, and rejects a non-AArch64 ARM64 artifact. Missing native libraries remain a hard failure at build/runtime integration boundaries.

## 4. SHA-256 Checksums

```text
e3b7b195000787efce458cdf9b1bfa6271c9b18ea23041b03e805b9ae2515654  app/src/main/jniLibs/arm64-v8a/libtdjni.so
77350d864515071279a51549b145b925c820f42a8e87d039bdab622cdc47e9a8  app/src/main/jniLibs/armeabi-v7a/libtdjni.so
2a66e9c5927a5bebe8aadbfb97e6776babb804f8326a3fb75856b073267edab8  app/src/main/jniLibs/x86_64/libtdjni.so
ea37f5c3f2cb894ad14381a22e1c6ca22affbaa25346669ff117e0b489e6eabe  app/src/main/java/org/drinkless/tdlib/Client.java
e162d82cd9b88f89668ba83451d600f578de205ceaf90625f062aad757173a36  app/src/main/java/org/drinkless/tdlib/Log.java
8f40a88e7bd379c5362afe8af0fe079c36b7d638f0adf19d024cfbce2ee74e7d  app/src/main/java/org/drinkless/tdlib/TdApi.java
```

## 5. Android Build Verification

| Check | Result |
|---|---|
| Artifact checker | PASS; `TDLIB_ARTIFACTS_PRESENT=true` |
| Kotlin compilation | PASS |
| Java compilation | PASS |
| Unit tests | PASS |
| ARM64 debug APK | PASS |
| ABI split configuration | PASS; arm64-v8a, armeabi-v7a, x86_64 |
| Debug APK path | `app/build/outputs/apk/debug/app-arm64-v8a-debug.apk` |
| Debug APK size | 44,441,943 bytes |
| Debug APK SHA-256 | `07ae90143853b78da072b58c8bbfaba866ad119e6b4235e94dfcb57c288d165e` |
| Release APK path | `app/build/outputs/apk/release/app-arm64-v8a-release.apk` |
| Release APK size | 21,550,668 bytes |
| Release APK SHA-256 | `aea78956e436e632821be1737ee0dec4a126eecaa6dd8f8384bc04008850810a` |
| APK native entries | `lib/<selected-abi>/libtdjni.so` |

Each native library was stripped with the Android NDK `llvm-strip --strip-debug --strip-unneeded` operation and validated using ELF headers and SHA-256 checksums. This removes debug/unneeded symbols without changing the official TDLib implementation. Physical-device authentication still requires testing on a compatible Android device with valid Telegram API credentials and network access.
