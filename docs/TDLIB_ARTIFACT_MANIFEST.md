# TDLib Android Artifact Manifest

## 1. Upstream Source Information

| Field | Value |
|---|---|
| Repository | https://github.com/tdlib/td |
| Version | `v1.8.66` |
| Pinned source commit | `022d60202e446ad1287b9fb68e687c8a0760788b` |
| Target interface | Official Java / Android JNI (`org.drinkless.tdlib`) |
| Android ABI | `arm64-v8a` / AArch64 |
| Android platform | API 24 |
| Android NDK | `26.3.11579264` |
| CMake | `3.22.1` |
| OpenSSL | `3.0.16` |
| Zlib | `1.3.1` |

The source was generated and built from the official TDLib repository. No mock, stub, or fabricated native implementation is included. The Java bindings and `libtdjni.so` were generated from the same source revision.

## 2. Target Architecture

The Android project is intentionally packaged for **ARM64 only**. This prevents mixing the new v1.8.66 native library with stale v1.8.0 libraries for other ABIs and reduces the APK footprint relative to a universal package.

## 3. Artifact Verification Table

| Artifact | Relative path | Size | Status |
|---|---|---:|---|
| ARM64 JNI | `app/src/main/jniLibs/arm64-v8a/libtdjni.so` | 58,944,152 bytes | PASS; stripped ELF AArch64 |
| Client binding | `app/src/main/java/org/drinkless/tdlib/Client.java` | 11,015 bytes | PASS |
| Log binding | `app/src/main/java/org/drinkless/tdlib/Log.java` | 3,401 bytes | PASS |
| TdApi binding | `app/src/main/java/org/drinkless/tdlib/TdApi.java` | 1,757,224 bytes | PASS |

The mandatory checker reports `TDLIB_ARTIFACTS_PRESENT=true`, verifies the ELF header, and rejects a non-AArch64 ARM64 artifact. Missing native libraries remain a hard failure at build/runtime integration boundaries.

## 4. SHA-256 Checksums

```text
e3b7b195000787efce458cdf9b1bfa6271c9b18ea23041b03e805b9ae2515654  app/src/main/jniLibs/arm64-v8a/libtdjni.so
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
| Debug APK path | `app/build/outputs/apk/debug/app-arm64-v8a-debug.apk` |
| Debug APK size | 44,441,943 bytes |
| Debug APK SHA-256 | `07ae90143853b78da072b58c8bbfaba866ad119e6b4235e94dfcb57c288d165e` |
| Release APK path | `app/build/outputs/apk/release/app-arm64-v8a-release.apk` |
| Release APK size | 21,550,668 bytes |
| Release APK SHA-256 | `aea78956e436e632821be1737ee0dec4a126eecaa6dd8f8384bc04008850810a` |
| APK native entry | `lib/arm64-v8a/libtdjni.so` |

The native library was stripped with the Android NDK `llvm-strip --strip-debug --strip-unneeded` operation. Its ELF type, AArch64 machine, Android dependencies, and `JNI_OnLoad` entry point were revalidated after stripping. This removes debug/unneeded symbols without changing the official TDLib implementation. Physical-device authentication still requires testing on an ARM64 Android device with valid Telegram API credentials and network access.
