# Release Artifacts

| Artifact | Built | Signed | Verified | Location |
|---|---:|---:|---:|---|
| Debug APK arm64-v8a | PASS | Debug signing only | PASS: produced by Gradle | `app/build/outputs/apk/debug/app-arm64-v8a-debug.apk` |
| Debug APK armeabi-v7a | NOT VERIFIED | Debug signing only | NOT VERIFIED | Not built in the final local run |
| Debug APK x86_64 | NOT VERIFIED | Debug signing only | NOT VERIFIED | Not built in the final local run |
| Release APK arm64-v8a | PASS | PASS | PASS: CI `apksigner` | GitHub Release `v1.0.14` — SHA-256 `95e0e92997e4c9a18e69a2824924a2d0c236daa7f193410354d8883b5e38bad8` |
| Release APK armeabi-v7a | PASS | PASS | PASS: CI `apksigner` | GitHub Release `v1.0.14` — SHA-256 `3cc126e1725685e2e5100a8d2c3a934d008e439e2050e5169b58351b9a71f9c4` |
| Release APK x86_64 | PASS | PASS | PASS: CI `apksigner` | GitHub Release `v1.0.14` — SHA-256 `a906de5ab674cf293b87ae44180f9484d446dc1b4f8796eeccdb2ee253381562` |
| Release AAB | NOT BUILT | NOT APPLICABLE | NOT VERIFIED | Release workflow publishes signed ABI APKs only |

The v1.0.14 APKs were signed and published by the confirmed GitHub Actions workflow. No signing key or secret value was published.
