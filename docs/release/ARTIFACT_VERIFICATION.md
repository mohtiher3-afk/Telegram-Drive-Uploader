# Release Artifact Verification

**Status: CURRENT — phase-28 release-candidate verification.**

## Local artifacts

A clean local build produced three debug APKs, three unsigned release APKs, and one unsigned release AAB.

| Artifact | Result | Evidence |
|---|---|---|
| Debug APKs | PASS | `arm64-v8a`, `armeabi-v7a`, and `x86_64` APKs produced by `assembleDebug`. |
| Release APKs | PASS for build/package inspection; signing NOT VERIFIED locally | Three `*-release-unsigned.apk` files produced by `assembleRelease`. |
| Release AAB | PASS for build/package inspection; signing NOT VERIFIED locally | `app-release.aab` produced by `bundleRelease`. |
| Application identity | PASS | `com.telegramdrive.uploader`, versionCode `15`, versionName `1.0.15`. |
| SDK identity | PASS | minSdk `24`, compileSdk `36`, targetSdk `36`. |
| Application label | PASS | English `Telegram Drive Uploader`; Arabic resource `محمل تيليجرام درايف`. |
| Native packaging | PASS | Each ABI APK contains only its matching `lib/<abi>/libtdjni.so`; the AAB contains all three configured ABI native libraries. |
| Permissions/manifest | PASS for static inspection | INTERNET, network state, media/storage permissions, WAKE_LOCK, boot, foreground service, and non-exported startup provider match the inspected manifest. |
| Local signing | NOT VERIFIED | Local release outputs are explicitly unsigned; `apksigner verify` correctly reports `NOT VERIFIED`. |

## Published signed artifacts

The repository’s release records document signed ABI-specific v1.0.15 APKs published by GitHub Actions run `32630539974`. Their checksums and sizes are recorded in [`RELEASE_ARTIFACTS.md`](RELEASE_ARTIFACTS.md). No signing key or secret value is reproduced here.

## Boundary

Static artifact inspection cannot prove installation, startup, JNI loading on a device, Telegram authorization, or real upload delivery. Those areas remain separately classified in the GO/NO-GO matrix.
