# Release Artifacts

| Artifact | Built | Signed | Verified | Location |
|---|---:|---:|---:|---|
| Debug APK arm64-v8a | PASS | Debug signing only | PASS: produced by Gradle | `app/build/outputs/apk/debug/app-arm64-v8a-debug.apk` |
| Debug APK armeabi-v7a | NOT VERIFIED | Debug signing only | NOT VERIFIED | Not built in the final local run |
| Debug APK x86_64 | NOT VERIFIED | Debug signing only | NOT VERIFIED | Not built in the final local run |
| Release APK arm64-v8a | PASS | Unsigned | PASS: produced by Gradle | `app/build/outputs/apk/release/app-arm64-v8a-release-unsigned.apk` |
| Release APK armeabi-v7a | PASS | Unsigned | PASS: produced by Gradle | `app/build/outputs/apk/release/app-armeabi-v7a-release-unsigned.apk` |
| Release APK x86_64 | PASS | Unsigned | PASS: produced by Gradle | `app/build/outputs/apk/release/app-x86_64-release-unsigned.apk` |
| Release AAB | PASS | Unsigned/not release-distributable | PASS: produced by Gradle | `app/build/outputs/bundle/release/app-release.aab` |

Hashes are intentionally not recorded as release hashes because the generated artifacts are unsigned and must not be distributed. No APK, AAB, keystore, or private artifact was published.
