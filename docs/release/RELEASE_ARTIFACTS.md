# Release Artifacts

| Artifact | Built | Signed | Verified | Location |
|---|---:|---:|---:|---|
| Debug APK arm64-v8a | PASS | Debug signing only | PASS: produced by Gradle | `app/build/outputs/apk/debug/app-arm64-v8a-debug.apk` |
| Debug APK armeabi-v7a | NOT VERIFIED | Debug signing only | NOT VERIFIED | Not built in the final local run |
| Debug APK x86_64 | NOT VERIFIED | Debug signing only | NOT VERIFIED | Not built in the final local run |
| Release APK arm64-v8a | PASS | PASS | PASS: CI `apksigner` | GitHub Release `v1.0.15` (20,438,498 bytes) — SHA-256 `78d24e3d2dd5dbf22d5ce3ee5440a5efbee38ae75631fc334bb89c9553f5cafc` |
| Release APK armeabi-v7a | PASS | PASS | PASS: CI `apksigner` | GitHub Release `v1.0.15` (15,902,869 bytes) — SHA-256 `416bbf8644b3536c0cbc1900212e57055ad56d6b1f13d87e5f70f522100b0f6b` |
| Release APK x86_64 | PASS | PASS | PASS: CI `apksigner` | GitHub Release `v1.0.15` (17,297,668 bytes) — SHA-256 `21209274334b1527ea799b1f0a9b50e19ed49f50228f3df91094cb6499776deb` |
| Release AAB | NOT BUILT | NOT APPLICABLE | NOT VERIFIED | Release workflow publishes signed ABI APKs only |

The v1.0.15 APKs were signed and published by the confirmed GitHub Actions workflow run `32630539974`. No signing key or secret value was published.
