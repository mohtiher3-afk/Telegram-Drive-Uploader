# Telegram Drive Uploader

[![Latest release](https://img.shields.io/github/v/release/mohtiher3-afk/Telegram-Drive-Uploader?label=version)](https://github.com/mohtiher3-afk/Telegram-Drive-Uploader/releases/latest)

> **Telegram Drive Uploader** is an Android application for preparing and uploading video files to Telegram destinations such as Saved Messages, groups, supergroups, and channels.

| Project | Value |
|---|---|
| Application ID | `com.telegramdrive.uploader` |
| Current source version | `v1.0.18` |
| Telegram engine | Official TDLib `v1.8.66` |
| UI toolkit | Kotlin, Jetpack Compose, Material 3 Expressive |
| Minimum Android version | API 24 |
| Target Android version | API 36 |
| Supported native ABIs | `arm64-v8a`, `armeabi-v7a`, `x86_64` |
| Localization | English and Arabic with RTL support |

The repository contains the real TDLib Java bindings and native libraries, a local upload-preparation queue, WorkManager background processing, Telegram authentication flows, destination search, upload progress states, retry and cancellation controls, and a local Smart File Assistant for filename and keyword suggestions.

> **Evidence boundary:** A successful build proves compilation and packaging only. Telegram authentication, destination permissions, native runtime behavior, and real file delivery require testing on a compatible physical device or emulator. The current repository status should not be interpreted as unrestricted production certification without that runtime evidence.

## Contents

- [Application preview](#application-preview)
- [Mission Control visual identity](#mission-control-visual-identity)
- [Features](#features)
- [Requirements](#requirements)
- [Quick start](#quick-start)
- [Telegram API configuration](#telegram-api-configuration)
- [Build and verification](#build-and-verification)
- [First-run and device validation](#first-run-and-device-validation)
- [TDLib artifacts](#tdlib-artifacts)
- [Release and CI](#release-and-ci)
- [Troubleshooting](#troubleshooting)
- [Project structure](#project-structure)
- [Security and privacy](#security-and-privacy)
- [Documentation and references](#documentation-and-references)

## Application preview

The following images show the current Material 3 Expressive direction, the Mission Control visual system, and the destination-selection and video-preparation flow. They are design previews with sample content; they do not replace authenticated Telegram delivery testing.

<p align="center">
  <img src="design/multi_device_ui_preview.png" alt="Telegram Drive Uploader Mission Control preview across phone and tablet layouts" width="900" />
</p>

<p align="center">
  <img src="design/app_icon_concept.png" alt="Telegram Drive Uploader Mission Control orbital upload logo" width="260" />
</p>

## Mission Control visual identity

Telegram Drive Uploader uses a **Mission Control** visual language: a dark control-room surface, luminous orbital accents, and a high-visibility upload action. The identity reinforces upload state and destination context while preserving Material 3 semantic roles and adaptive Compose layouts.

### Color system

The values below are the source of truth for the custom theme in [`app/src/main/java/com/telegramdrive/uploader/core/ui/theme/Theme.kt`](app/src/main/java/com/telegramdrive/uploader/core/ui/theme/Theme.kt). Each color has a functional role and should not be used as decoration without a corresponding hierarchy or state purpose.

| Token | Implemented value | Role |
|---|---|---|
| **Plum** | `#17131D` | Primary dark background and base surface. |
| **Violet** | `#D7B9FF` / `#512A73` | Secondary navigation, orbit glow, selected controls, and destination emphasis. |
| **Lime** | `#D8F56A` | Primary upload action and high-priority positive interaction. |
| **Mint** | `#9FFFD2` | Success, completion, telemetry, and supporting upload-state feedback. |
| **Soft text** | `#F2EAF5` | Primary text on dark surfaces. |
| **Muted text** | `#D6C6DC` | Supporting text, metadata, and secondary descriptions. |

The light theme adapts the same semantic roles for contrast. On Android 12 and later, system dynamic color may be used when enabled; the semantic roles remain the design authority even when the platform derives the final tones.

### Logo usage

The current mark is the **Mission Control orbital upload logo**: a Lime upward arrow and tray enclosed by a Violet orbital form with Mint highlights. The canonical asset is [`app/src/main/res/drawable-nodpi/mission_control_logo.png`](app/src/main/res/drawable-nodpi/mission_control_logo.png). It is used by the launcher foreground, first-run onboarding hero, opening animation, and repository preview at [`design/app_icon_concept.png`](design/app_icon_concept.png).

Use the mark without added text, stretching, recoloring, or extra badges. Preserve its square aspect ratio and leave clear space around the symbol. A Plum or similarly dark surface provides the intended contrast for the luminous Violet, Lime, and Mint details. The multi-device design reference is [`design/multi_device_ui_preview.png`](design/multi_device_ui_preview.png).

### Motion and accessibility

Mission Control motion communicates readiness and upload progress rather than serving as continuous decoration. The opening sequence uses a short logo reveal and restrained pulse; reduced-motion settings disable non-essential pulse motion and shorten the transition. Screen-reader labels are localized, and color or animation must never be the only indication of upload state.

English and Arabic are first-class locales. Compose layout direction follows the active locale, so controls and supporting content remain usable in RTL without mirroring the logo. Future screens and visual assets should preserve readable contrast, adaptive sizing, semantic labels, and the [Material 3 accessibility guidance][5].

## Features

| Area | Current capability |
|---|---|
| Telegram authentication | Phone number, verification code, two-step password, and QR login states through TDLib. |
| Real Telegram engine | Official `Client.java`, `TdApi.java`, `Log.java`, and native TDLib integrations; no mock Telegram implementation. |
| Upload preparation | Local video selection, queue preparation, scheduling support, progress states, retry, cancellation, and upload history. |
| Destination search | Search and filter Telegram chats, groups, supergroups, and channels that can receive messages. |
| Background work | WorkManager-backed upload processing with persisted local state. |
| Smart File Assistant | Local Arabic-aware and English-aware filename and keyword suggestions; uploads do not depend on an online AI service. |
| UI and localization | Material 3 Expressive Compose UI, adaptive layouts, onboarding, English resources, Arabic resources, and RTL rendering. |
| Packaging | ABI-specific builds for `arm64-v8a`, `armeabi-v7a`, and `x86_64`. |

## Requirements

A local build requires Android Studio or an equivalent Android SDK installation containing API 36, JDK 17 for CI-compatible builds, the project’s Gradle wrapper, and a device or emulator matching one of the packaged ABIs. Before building locally, create a machine-specific `local.properties` file containing the Android SDK path; this file is intentionally excluded from Git.

Install only the APK matching the target device architecture. Android can reject an incompatible native ABI when no compatible `libtdjni.so` is packaged.

## Quick start

Clone the repository and enter the project directory:

```bash
git clone https://github.com/mohtiher3-afk/Telegram-Drive-Uploader.git
cd Telegram-Drive-Uploader
```

Create `local.properties` with the local Android SDK path, configure the Telegram API values as described below, and run the repository’s quick verification mode:

```bash
./scripts/verify-project.sh QUICK
```

For a direct debug build and unit-test run:

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug
```

Do not commit `local.properties`, Telegram credentials, session data, signing material, or generated local configuration files.

## Telegram API configuration

Telegram API credentials are application configuration values, not user login credentials. Obtain an `api_id` and `api_hash` from [my.telegram.org](https://my.telegram.org), then provide them through the project’s Gradle or local build configuration mechanism.

Never commit a real `api_hash`, personal phone number, verification code, password, Telegram session database, keystore, or generated local configuration. If a credential is exposed, revoke or rotate it immediately through the appropriate Telegram account controls.

## Build and verification

The repository provides layered verification modes:

```bash
./scripts/verify-project.sh QUICK
./scripts/verify-project.sh FULL
./scripts/verify-project.sh RELEASE
```

The modes run the repository, TDLib, Gradle, compilation, unit-test, lint, packaging, security, resource, and WorkManager gates appropriate to each mode. See [`docs/DEVELOPER_ONBOARDING.md`](docs/DEVELOPER_ONBOARDING.md) and [`docs/maintenance/README.md`](docs/maintenance/README.md) for the maintenance workflow.

For constrained build machines, use one worker and a bounded Gradle JVM:

```bash
./gradlew --no-daemon --max-workers=1 \\
  -Dorg.gradle.jvmargs="-Xmx1200m -XX:MaxMetaspaceSize=512m" \\
  :app:assembleRelease
```

The project includes strict R8 keep rules for `org.drinkless.tdlib.**`. These rules are required because official TDLib JNI registration depends on generated binding classes and native method names remaining stable in a minified release build.

## First-run and device validation

On first launch, follow the onboarding flow and grant only the media permissions requested by the Android version. The Mission Control opening animation is designed for the first-run experience and respects reduced-motion settings.

On a compatible physical device or emulator, validate the following sequence: native TDLib loading, Telegram phone or QR authentication, authorized-state transition, destination loading, permission to send to the selected destination, media selection, upload progress, real delivery, cancellation, retry, process restart, logout/login recovery, and network loss during an upload.

A sandbox or JVM build cannot prove native behavior on every handset. Record runtime evidence separately from build evidence, and do not report a real upload as successful unless the Telegram success signal is observed.

## TDLib artifacts

Official source bindings are stored under:

```text
app/src/main/java/org/drinkless/tdlib/
```

Native libraries are stored under:

```text
app/src/main/jniLibs/arm64-v8a/libtdjni.so
app/src/main/jniLibs/armeabi-v7a/libtdjni.so
app/src/main/jniLibs/x86_64/libtdjni.so
```

Run the artifact gate from the project root:

```bash
./scripts/check-tdlib-artifacts.sh
```

A successful check ends with:

```text
STATUS: TDLIB_ARTIFACTS_PRESENT=true
```

The checker validates the required manifest, each configured ELF native library and machine type, and the generated Java bindings. It does not replace testing on a physical device.

For native dependency preparation and runtime loading, see [`docs/TDLIB_NATIVE_DEPENDENCIES.md`](docs/TDLIB_NATIVE_DEPENDENCIES.md), [`docs/TDLIB_ARTIFACT_MANIFEST.md`](docs/TDLIB_ARTIFACT_MANIFEST.md), and [`docs/TDLIB_DEVICE_SMOKE_TEST.md`](docs/TDLIB_DEVICE_SMOKE_TEST.md).

## Release and CI

Release signing must use a keystore configured for the local environment or the repository’s protected CI secrets. Signing material must never be committed. ABI-specific release APKs are produced by the project’s CI workflow; select the artifact matching the target device architecture.

Before creating a release, verify the version metadata, run the release verification mode, confirm all supported ABI jobs pass, and inspect the generated checksums and signatures. A signed APK proves packaging and signing, not Telegram authentication or real upload delivery.

## Troubleshooting

| Symptom | Recommended action |
|---|---|
| The app closes when connecting Telegram | Confirm that the ABI-matching APK is installed. Rebuild after verifying the TDLib R8 keep rules, then collect the Android `FATAL EXCEPTION` or native crash entry from Logcat. |
| TDLib is unavailable at runtime | Confirm the device ABI, verify that the corresponding `lib/<abi>/libtdjni.so` exists, and run `./scripts/check-tdlib-artifacts.sh`. |
| Telegram credentials are rejected | Confirm that the API ID is numeric and the API hash is complete. Do not confuse Telegram API credentials with the phone login code or account password. |
| No chats appear in destination search | Complete authentication, wait for TDLib authorization and chat updates, retry the destination screen, and confirm that the account can send to the selected destination. |
| Arabic layout is misaligned | Set Arabic as the Android system language, restart the app, and verify RTL support. Report the exact screen and Android version if the issue remains. |
| Release signing fails | Configure a local release keystore or use a temporary development key for testing. Do not add signing credentials to Git. |
| R8 reports Kotlin metadata warnings | Treat the warning separately from JNI preservation. Keep the official TDLib rules intact and align Kotlin, AGP, Moshi, and KSP versions during a dedicated dependency-maintenance pass. |

## Project structure

```text
app/src/main/java/com/telegramdrive/uploader/
  core/                 Navigation, diagnostics, datastore, UI theme, and shared utilities
  data/                 TDLib client, repositories, upload data, and platform integrations
  domain/               Models, repository contracts, and upload state definitions
  feature/              Compose screens and ViewModels for home, onboarding, uploads, settings, history, and Telegram auth
app/src/main/java/org/drinkless/tdlib/
  Client.java           Official TDLib Java client binding
  TdApi.java            Official generated TDLib API binding
  Log.java              Official TDLib logging binding
app/src/main/jniLibs/{arm64-v8a,armeabi-v7a,x86_64}/
  libtdjni.so           Official TDLib v1.8.66 native libraries
docs/                   Build, artifact, maintenance, and audit documentation
scripts/                Artifact validation and project helper scripts
```

## Security and privacy

The Smart File Assistant is local and optional; it is not a remote generative AI service and does not require an online AI key. Telegram session data and the TDLib database are created in the application’s private storage. Do not copy those directories into an issue, archive, or public repository.

Diagnostics are privacy-conscious and bounded. Before sharing logs, inspect them for account identifiers, file names, phone numbers, tokens, and other personal information. Redact anything that could identify the Telegram account or uploaded files.

## Documentation and references

The project audit and remaining device-level validation items are documented in [`docs/PROJECT_AUDIT_2026-08-21.md`](docs/PROJECT_AUDIT_2026-08-21.md). Current maintenance and repository-cleanup records are indexed in [`docs/maintenance/README.md`](docs/maintenance/README.md). Resource and branding reviews are available under [`docs/resources/`](docs/resources/) and [`docs/operations/`](docs/operations/).

[1]: https://github.com/tdlib/td "Official TDLib repository"
[2]: https://core.telegram.org/api/obtaining_api_id "Telegram API ID and Telegram API hash documentation"
[3]: https://developer.android.com/guide/topics/manifest/uses-sdk-element "Android SDK version documentation"
[4]: https://developer.android.com/studio/build/shrink-code "Android R8 and app optimization documentation"
[5]: https://m3.material.io/foundations/accessible-design/overview "Material Design 3 accessible design guidance"
[6]: https://m3.material.io/ "Material Design 3 documentation"

*Maintained for the Telegram Drive Uploader project.*
