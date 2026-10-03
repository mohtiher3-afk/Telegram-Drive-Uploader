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
| ARM64 JNI | `data/src/main/jniLibs/arm64-v8a/libtdjni.so` | 20,398,552 bytes | PASS; documented by SHA-256 |
| ARMv7 JNI | `data/src/main/jniLibs/armeabi-v7a/libtdjni.so` | 14,407,696 bytes | PASS; documented by SHA-256 |
| x86_64 JNI | `data/src/main/jniLibs/x86_64/libtdjni.so` | 22,573,928 bytes | PASS; documented by SHA-256 |
| Client binding | `data/src/main/java/org/drinkless/tdlib/Client.java` | 11,015 bytes | PASS; documented by SHA-256 |
| Log binding | `data/src/main/java/org/drinkless/tdlib/Log.java` | 3,401 bytes | PASS; documented by SHA-256 |
| TdApi binding | `data/src/main/java/org/drinkless/tdlib/TdApi.java` | 5,096,314 bytes | PASS; documented by SHA-256 |

The mandatory checker reports `TDLIB_ARTIFACTS_PRESENT=true`, verifies the ELF header, and rejects a non-matching ARM64 artifact when the selected ABI is `arm64-v8a`. Missing native libraries remain a hard failure at build/runtime integration boundaries.

## 4. SHA-256 Checksums

The authoritative machine-readable checksums are in [`TDLIB_SHA256SUMS.txt`](TDLIB_SHA256SUMS.txt). That file covers the three prebuilt `libtdjni.so` files and the three generated Java bindings using repository-relative paths and the exact Git blob bytes used for checkout on Linux CI.

The packaged OpenSSL `libssl.so`/`libcrypto.so` files are rebuilt per ABI by `scripts/build-openssl-android.sh` from the pinned official OpenSSL 3.0.16 source archive, whose SHA-256 is verified before extraction. They are intentionally not pinned by a repository checksum because the CI rebuild is the authoritative input to the APK.

`scripts/check-tdlib-artifacts.sh` verifies every checksum entry against the checked-out files, so stale paths, changed prebuilt binaries, and stale binding documentation fail the CI artifact gate.

## 5. Android Build Verification

| Check | Result |
|---|---|
| Artifact checker | PASS; `TDLIB_ARTIFACTS_PRESENT=true` |
| Kotlin compilation | PASS |
| Java compilation | PASS |
| Unit tests | PASS |
| ARM64 debug APK build | PASS |
| ABI split configuration | PASS; arm64-v8a, armeabi-v7a, x86_64 |
| Debug/release APK size and digest | Not documented here; generated APKs are ephemeral and must be verified from their per-build `.sha256` files |
| APK native entries | `lib/<selected-abi>/libtdjni.so` |

Each native library was stripped with the Android NDK `llvm-strip --strip-debug --strip-unneeded` operation and validated using ELF headers and SHA-256 checksums. This removes debug/unneeded symbols without changing the official TDLib implementation. Physical-device authentication still requires testing on a compatible Android device with valid Telegram API credentials and network access.
