# Telegram Drive Uploader v1.0.16 Historical Release Note

**Published:** 2026-08-24

## Historical release result

The signed multi-ABI release workflow completed successfully and published a non-draft, non-prerelease GitHub Release for `v1.0.16`. Post-publication verification found that the GitHub tag was `v1.0.16` while the APK internal metadata remained `versionName 1.0.15` and `versionCode 15`. The release is retained to avoid destructive deletion; the corrected release is `v1.0.17`.

- Workflow run: https://github.com/mohtiher3-afk/Telegram-Drive-Uploader/actions/runs/32704066824
- Release page: https://github.com/mohtiher3-afk/Telegram-Drive-Uploader/releases/tag/v1.0.16
- Source revision: `2cbdb3c`
- APK internal version: `1.0.15` (`versionCode 15`)
- Release tag: `v1.0.16` (metadata mismatch; superseded by `v1.0.17`)

## Historical APK assets (superseded)

| ABI | APK | Size |
|---|---|---:|
| arm64-v8a | [app-arm64-v8a-release.apk](https://github.com/mohtiher3-afk/Telegram-Drive-Uploader/releases/download/v1.0.16/app-arm64-v8a-release.apk) | 20,471,074 bytes |
| armeabi-v7a | [app-armeabi-v7a-release.apk](https://github.com/mohtiher3-afk/Telegram-Drive-Uploader/releases/download/v1.0.16/app-armeabi-v7a-release.apk) | 15,935,501 bytes |
| x86_64 | [app-x86_64-release.apk](https://github.com/mohtiher3-afk/Telegram-Drive-Uploader/releases/download/v1.0.16/app-x86_64-release.apk) | 17,330,280 bytes |

The release also contains one SHA-256 checksum file for each ABI. The workflow verified the expected `libtdjni.so` library for the selected ABI, rejected additional ABI libraries in each split APK, and passed `apksigner verify` for every signed APK before publication.

## Workflow coverage

The corrected release target is `v1.0.17`, built from the metadata-only version bump to `versionName 1.0.17` and `versionCode 17`. Its final links will be recorded in a separate corrected release report after GitHub verification.

Each ABI job completed repository security checks, official TDLib artifact checks, JVM tests, release lint, signed release assembly, APK content validation, signature verification, checksum generation, and artifact upload. The publish job downloaded all signed artifacts and created the release successfully.

This publication does not constitute proof of Telegram account authentication, channel permissions, or real upload delivery. Those remain separate runtime validations requiring a logged-in test device or emulator and a non-sensitive test file.
