# Telegram Drive Uploader v1.0.17 Release

**Published:** 2026-08-24

## Release result

The signed multi-ABI release workflow completed successfully and published the corrected release for `v1.0.17`.

- Workflow run: https://github.com/mohtiher3-afk/Telegram-Drive-Uploader/actions/runs/32705499171
- Release page: https://github.com/mohtiher3-afk/Telegram-Drive-Uploader/releases/tag/v1.0.17
- Source revision: `744363d`
- APK internal version: `1.0.17` (`versionCode 17`)

This release supersedes the historical `v1.0.16` tag whose APKs were built with internal metadata `1.0.15` / `versionCode 15`. The older release was retained rather than deleted to avoid breaking existing links.

## Published APK assets

| ABI | APK | Size |
|---|---|---:|
| arm64-v8a | [app-arm64-v8a-release.apk](https://github.com/mohtiher3-afk/Telegram-Drive-Uploader/releases/download/v1.0.17/app-arm64-v8a-release.apk) | 20,471,074 bytes |
| armeabi-v7a | [app-armeabi-v7a-release.apk](https://github.com/mohtiher3-afk/Telegram-Drive-Uploader/releases/download/v1.0.17/app-armeabi-v7a-release.apk) | 15,935,501 bytes |
| x86_64 | [app-x86_64-release.apk](https://github.com/mohtiher3-afk/Telegram-Drive-Uploader/releases/download/v1.0.17/app-x86_64-release.apk) | 17,330,280 bytes |

The release also contains one SHA-256 checksum file for each ABI:

- `apk-arm64-v8a-sha256.txt`: `0c155a09a3d3832b694c7abc565a6a6d1970a21f01377ec38d4f04fa9d9b0291`
- `apk-armeabi-v7a-sha256.txt`: `312d79ffa66b9ad8683f2321a3fb17c1a330a0d35d50787a663364b4da8241b6`
- `apk-x86_64-sha256.txt`: `1ea4b113e32f6ba96c06bc0def880e610535d06c1f316978075e9d07fb686907`

## Verification

The downloaded APKs were independently checked with Android Build-Tools 36.0.0. Each reports package `com.telegramdrive.uploader`, `versionCode 17`, `versionName 1.0.17`, one signer, and a valid APK Signature Scheme v2 signature. Each APK contains only its selected `libtdjni.so` ABI library, and every published checksum matches the downloaded APK byte-for-byte.

The GitHub workflow also passed JVM tests, release lint, signed release assembly, TDLib artifact checks, APK content validation, signature verification, checksum generation, and publication for all three ABIs.

This release does not prove Telegram account authentication, channel permissions, or real upload delivery. Those remain separate runtime validations requiring a logged-in test device or emulator and a non-sensitive test file.
