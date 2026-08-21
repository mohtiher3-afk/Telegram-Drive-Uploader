# Telegram Drive Uploader

Telegram Drive Uploader is an Android application for preparing and uploading video files to Telegram destinations such as Saved Messages, groups, supergroups, and channels. The project uses the official [TDLib](https://github.com/tdlib/td) Java bindings and an ARM64 native library, a local queue for upload preparation, WorkManager-oriented background processing, Arabic localization with RTL support, and a local Smart File Assistant for filename and keyword suggestions.

> **Current status:** The repository contains the official TDLib **v1.8.66** Java bindings and an optimized `arm64-v8a` native integration. The current release target is ARM64 Android devices.

## Features

| Feature | Description |
|---|---|
| Telegram authentication | Typed authentication flow for phone number, verification code, two-step password, and QR login states. |
| Real TDLib integration | Uses official `Client.java`, `TdApi.java`, and `Log.java` bindings with no mock Telegram implementation. |
| Fail-closed native loading | Native initialization reports a controlled Telegram runtime error when the library cannot be loaded instead of fabricating a successful connection. |
| Upload preparation | Local video selection, queue management, scheduling support, progress states, retry/cancel controls, and upload history. |
| Destination search | Search and filter Telegram chats, groups, supergroups, and channels that can receive messages. |
| Smart File Assistant | Lightweight, fully local filename and keyword suggestions with Arabic-aware and English-aware inference. Uploads do not depend on an online AI service. |
| Material 3 Expressive UI | Modern Compose interface with expressive shapes, adaptive layout, onboarding, and visual feedback. |
| Arabic and RTL | Arabic resources and right-to-left rendering through Android locale support and Compose layout direction. |
| ARM64 optimization | Release packaging is restricted to `arm64-v8a` to avoid shipping unnecessary native ABIs. |

## Requirements

The project requires Android Studio with an Android SDK that includes API 36, JDK 11-compatible Android tooling, Android Gradle Plugin support for the project’s Gradle wrapper, and an ARM64 Android device for the packaged release APK. The minimum Android API level is 24 and the target API level is 36.

The project is intentionally ARM64-only because the included TDLib artifact is `arm64-v8a`. It is not expected to install or run on 32-bit ARM, x86, or x86_64-only devices unless additional official TDLib artifacts are built and integrated.

## Telegram API configuration

Telegram API credentials are application configuration values, not user login credentials. Obtain an `api_id` and `api_hash` from [my.telegram.org](https://my.telegram.org), and provide them through the project’s Gradle configuration mechanism or a local build configuration that is excluded from version control.

Never commit a real `api_hash`, personal phone number, verification code, password, session database, keystore, or generated local configuration file. The repository must remain free of personal Telegram session data. If a credential is accidentally exposed, revoke or rotate it immediately through the appropriate Telegram account controls.

## TDLib artifacts

The project stores official source bindings under:

```text
app/src/main/java/org/drinkless/tdlib/
```

The ARM64 native library is stored at:

```text
app/src/main/jniLibs/arm64-v8a/libtdjni.so
```

Run the repository integrity gate from the project root:

```bash
./scripts/check-tdlib-artifacts.sh
```

A successful check ends with:

```text
STATUS: TDLIB_ARTIFACTS_PRESENT=true
```

The checker validates the required manifest, the ARM64 ELF native library, the AArch64 machine type, and the generated Java bindings. It does not replace testing on a physical Android device.

For a complete source-build procedure, see [`docs/TDLIB_ANDROID_BUILD.md`](docs/TDLIB_ANDROID_BUILD.md), [`docs/TDLIB_ARTIFACT_MANIFEST.md`](docs/TDLIB_ARTIFACT_MANIFEST.md), and [`docs/tdlib_v1.8.0_android_workflow.md`](docs/tdlib_v1.8.0_android_workflow.md) when present.

## Build the application

From the repository root, use the project’s Gradle wrapper where available:

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:lintVitalRelease
./gradlew :app:assembleRelease
```

The ARM64 release APK is produced at:

```text
app/build/outputs/apk/release/app-arm64-v8a-release.apk
```

Before building locally, create a machine-specific `local.properties` file containing the path to the Android SDK. This file is intentionally excluded from Git. Release signing must use a keystore configured for the local environment; signing material must never be committed.

For a constrained build machine, use one worker and a bounded Gradle JVM, for example:

```bash
./gradlew --no-daemon --max-workers=1 \
  -Dorg.gradle.jvmargs="-Xmx1200m -XX:MaxMetaspaceSize=512m" \
  :app:assembleRelease
```

The project includes strict R8 keep rules for `org.drinkless.tdlib.**`. These rules are required because official TDLib JNI registration depends on the generated binding class and native method names remaining stable in a minified release build.

## Testing

The JVM test suite includes coverage for the local Smart File Assistant, including Arabic inference, English filename inference, metadata handling, and safe fallback behavior:

```bash
./gradlew :app:testDebugUnitTest
```

The TDLib artifact gate should be run before packaging:

```bash
./scripts/check-tdlib-artifacts.sh
```

A sandbox or JVM build cannot prove native behavior on every handset. On a physical ARM64 device, test first-run onboarding, Arabic RTL layout, media selection, Telegram phone authentication, QR authentication, destination loading, uploading to a permitted destination, scheduling after process restart, logout/login recovery, low-storage behavior, and network loss during an upload.

## Installation and first run

Install only the ARM64 release APK on a compatible device. If Android reports that the package conflicts with an existing installation, uninstall the older development build first unless both APKs were signed with the same key. On first launch, follow the onboarding flow and grant only the storage or media permissions requested by the current Android version.

Open **Connect Telegram**, choose phone or QR authentication, and complete the Telegram login flow. The app should display a controlled error state if TDLib cannot initialize. It must not report a successful Telegram connection unless TDLib has actually entered an authorized state.

## Troubleshooting

| Symptom | Recommended action |
|---|---|
| The app closes when pressing Connect Telegram | Confirm that the latest Release ARM64 APK is installed. Rebuild after verifying the TDLib R8 keep rules in `app/proguard-rules.pro`. If the problem remains, collect the Android `FATAL EXCEPTION` or native crash entry from Logcat. |
| TDLib runtime unavailable | Confirm that the device is ARM64, that `lib/arm64-v8a/libtdjni.so` is present in the APK, and that `./scripts/check-tdlib-artifacts.sh` passes. |
| Telegram credentials are rejected | Confirm that the configured API ID is numeric and the API hash is complete. Do not confuse Telegram API credentials with the phone login code or account password. |
| No chats appear in destination search | Complete authentication, wait for TDLib authorization and chat updates, then retry the destination screen. Confirm that the account has permission to send messages to the selected destination. |
| Arabic layout looks incorrect | Set Arabic as the Android system language, restart the app, and verify that RTL support is enabled. Report the specific screen and Android version if a layout remains misaligned. |
| Release build fails at signing | Configure a local release keystore or use a temporary development signing key for testing. Do not add `debug.keystore`, `local.properties`, or signing credentials to Git. |
| R8 warns about Kotlin metadata | Treat the warning separately from JNI preservation. Keep the official TDLib rules intact and align Kotlin, Android Gradle Plugin, Moshi, and KSP versions during a dedicated dependency-maintenance pass. |

## Project structure

```text
app/src/main/java/com/telegramdrive/uploader/
  core/                 Navigation, diagnostics, datastore, AI assistant, and shared utilities
  data/                 TDLib client, repositories, upload data, and platform integrations
  domain/               Models, repository contracts, and upload state definitions
  feature/              Compose screens and ViewModels for home, uploads, settings, history, and Telegram auth
app/src/main/java/org/drinkless/tdlib/
  Client.java           Official TDLib Java client binding
  TdApi.java            Official generated TDLib API binding
  Log.java              Official TDLib logging binding
app/src/main/jniLibs/arm64-v8a/
  libtdjni.so           Official TDLib v1.8.66 ARM64 native library
docs/                   Build, artifact, and audit documentation
scripts/                Artifact validation and project helper scripts
```

## Security and privacy

The Smart File Assistant is local and optional; it is not a remote generative AI service and does not require an online AI key. Telegram session data and the TDLib database are created in the application’s private storage on the device. Do not copy those directories into an issue, archive, or public repository.

Diagnostics are intended to be privacy-conscious and bounded. Before sharing logs, inspect them for account identifiers, file names, phone numbers, tokens, and other personal information. Redact anything that could identify the Telegram account or uploaded files.

## Documentation and references

The project audit and remaining device-level validation items are documented in [`docs/PROJECT_AUDIT_2026-08-21.md`](docs/PROJECT_AUDIT_2026-08-21.md). The official TDLib source and Android documentation are available through the links below.

[1]: https://github.com/tdlib/td "Official TDLib repository"
[2]: https://core.telegram.org/api/obtaining_api_id "Telegram API ID and API hash documentation"
[3]: https://developer.android.com/guide/topics/manifest/uses-sdk-element "Android SDK version documentation"
[4]: https://developer.android.com/studio/build/shrink-code "Android R8 and app optimization documentation"
[5]: https://m3.material.io/ "Material Design 3 documentation"
[6]: https://developer.android.com/develop/ui/compose/layouts/look-at-the-layout "Jetpack Compose layout documentation"

*Maintained for the Telegram Drive Uploader project.*
