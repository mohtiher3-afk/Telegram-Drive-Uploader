# Telegram Drive Uploader

**Android app for managing and uploading files and videos to Telegram destinations.**

| | |
|---|---|
| **CI** | [Android Multi-ABI CI](https://github.com/mohtiher3-afk/Telegram-Drive-Uploader/actions/workflows/android-ci.yml) — security, artifact gates, JVM tests, lint, debug build × 3 ABIs |
| **Version** | `1.1.2` / versionCode 27 (badge: [![Version](https://img.shields.io/badge/version-1.1.2-blue)](https://github.com/mohtiher3-afk/Telegram-Drive-Uploader/releases)) |
| **License** | [MIT](LICENSE) |
| **Modules** | :app, :domain, :core, :data, :feature, :benchmark |
| **TDLib** | v1.8.67 (native, checked in per-ABI) |
| **Report a vuln** | [GitHub Security Advisory](https://github.com/mohtiher3-afk/Telegram-Drive-Uploader/security/advisories/new) (private) |

---

## Table of Contents

1. [User Guide](#user-guide)
   - [Onboarding & First Run](#onboarding--first-run)
   - [Authentication](#authentication)
   - [Appearance](#appearance)
   - [Uploading Files](#uploading-files)
2. [Developer Guide](#developer-guide)
   - [Prerequisites](#prerequisites)
   - [Getting Started](#getting-started)
   - [Project Structure](#project-structure)
   - [Build & Verification](#build--verification)
   - [TDLib & Native Artifacts](#tdlib--native-artifacts)
   - [Performance & Baseline Profile](#performance--baseline-profile)
   - [Release & CI](#release--ci)
3. [Security & Privacy](#security--privacy)
4. [Troubleshooting](#troubleshooting)
5. [Documentation Index](#documentation-index)

---

## User Guide

### Onboarding & First Run

On first launch, follow the Mission Control onboarding flow. Grant only the media
permissions requested by your Android version. The opening animation is designed
for the first-run experience and respects reduced-motion settings.

### Authentication

Log in securely using your Telegram phone number or a QR code. The app uses
real TDLib authentication; your credentials and session data are stored only on
your device.

### Appearance

Open **Settings → Appearance** to choose how the app looks:

- **Theme**: `System` (follows the device, the default), `Light`, or `Dark`.
- **Glow Color**: the accent colour used for primary actions and progress. Pick
  one of six presets — Seafoam, Orchid, Cobalt, Lime, Cyan, Violet — or choose
  `Custom` and enter your own hex value.
- **Dynamic Color**: `Full` (system wallpaper), `Brand Accented` (keeps your
  glow colour), or `Static Brand` (app colours only).

All choices are stored on the device and applied immediately.

### Uploading Files

- **Select Destination**: search for a chat, group, or channel.
- **Queue Management**: add multiple files; the app manages the queue in the
  background.
- **Reliability**: uploads resume automatically after network loss or device
  restart (WorkManager integration).
- **Smart Assistance**: on-device Gemini Nano AI analyses image content and
  suggests filenames and keywords.
- **Home Screen Widget**: view upload progress and queue status from your home
  screen.
- **App Shortcuts**: long-press the app icon for quick access to New Upload or
  Queue.

---

## Developer Guide

### Prerequisites

- **JDK 17+** (JDK 21 recommended for current build matrices)
- **Android SDK** (API 37)
- **NDK** (matching the version in `app/build.gradle.kts`)
- **Telegram API credentials**: [API ID and API hash][2] from
  [my.telegram.org](https://my.telegram.org)

### Getting Started

1. **Credentials** — never commit them; provide via Gradle configuration or
   environment variables:

   ```bash
   cp .env.example .env
   # fill in TELEGRAM_API_ID and TELEGRAM_API_HASH
   ```

2. **Local Setup** — run the verify-project gates:

   ```bash
   # Linux / macOS
   ./scripts/verify-project.sh QUICK

   # Windows (PowerShell; Git Bash not required)
   .\scripts\verify-project.ps1 -Mode QUICK
   ```

   Both run the same four gates: compile every module, unit tests, lint with
   `warningsAsErrors`, and a debug APK assembly.

   > On Windows, the `app` module writes its build output to
   > `%TEMP%\tdg-build\app\build` (a OneDrive reparse-point workaround, see
   > `app/build.gradle.kts`) → debug APK lands at
   > `%TEMP%\tdg-build\app\build\outputs\apk\debug\app-debug.apk`.

3. **Device Validation** — a JVM build cannot prove native behaviour. Validate
   TDLib loading and authentication on a physical device or compatible
   emulator.

### Project Structure

```text
build.gradle.kts            Root build configuration (version catalogs, resolution)
settings.gradle.kts         Module graph and plugin management
app/                        :app — Compose shell, navigation, DI wiring, app icon
feature/                    :feature — Compose screens, ViewModels, shared UI
core/                       :core — diagnostics, Gemini Nano AI, shared utilities
data/                       :data — TDLib client, Room, upload engine, WorkManager
domain/                     :domain — models, repository contracts, pure upload logic
benchmark/                  :benchmark — startup / macrobenchmark suite
design/                     Visual direction: Mission Control tokens, previews
docs/                       Documentation index + maintainable content
scripts/                    verify-project*, check-* gates, artifact helpers
```

**TDLib & Native Artifacts**

The application bundles official TDLib binaries for every shipped ABI:

```text
data/src/main/jniLibs/{arm64-v8a,armeabi-v7a,x86_64}/libtdjni.so
data/src/main/jniLibs/{arm64-v8a,armeabi-v7a,x86_64}/libcrypto.so
data/src/main/jniLibs/{arm64-v8a,armeabi-v7a,x86_64}/libssl.so
```

> **ABI Compatibility**: the signed release workflow builds one APK per ABI, so
> the device installs the binary that matches.

Run the artifact gate:

```bash
./scripts/check-tdlib-artifacts.sh
```

### Build & Verification

```bash
# Standard verification (compile + tests + lint + debug APK)
./scripts/verify-project.sh QUICK | FULL

# Windows
.\scripts\verify-project.ps1 -Mode QUICK | FULL

# Release-ready validation (assembles + R8-minifies)
./scripts/verify-project.sh RELEASE
```

The underlying Gradle gates, if you prefer to run them directly:

```bash
./gradlew compileDebugKotlin compileDebugUnitTestKotlin   # every module must compile
./gradlew testDebugUnitTest                                # all modules: app, core, data, domain, feature
./gradlew lintDebug                                        # zero lint warnings (warnings are errors)
```

> `:feature` is part of the test gate. A change that only compiles in `:app`
> will fail CI.

### Performance & Baseline Profile

A committed baseline profile at `app/src/main/baseline-prof.txt` is consumed by
R8 on every release build. Because the app loads `libtdjni.so` through JNI and
initialises a TDLib client before Compose draws, cold start is the expensive
path.

Regenerate after changing startup code — a connected device or emulator is
required:

```bash
./gradlew :benchmark:collectNonMinifiedReleaseBaselineProfile
```

Measure cold and warm startup:

```bash
./scripts/measure-startup.sh
```

> A physical device is required for startup measurements.
> `androidx MacrobenchmarkRule` skips emulators; emulator timings measure the
> host, so the rule raises `AssumptionViolatedException` rather than publishing
> a meaningless number. Profile *generation* runs on an emulator; only the
> timings need real hardware.

### Release & CI

- **Android Multi-ABI CI** runs on PRs and pushes to `main`. It executes
  repository security and artifact gates, JVM unit tests, release lint, and a
  Debug APK build for `arm64-v8a`, `armeabi-v7a`, and `x86_64`, storing each as
  a temporary artifact.
- **Android Signed Multi-ABI Release** triggers on `v*` tags. It builds signed
  Release APKs for all ABIs plus a signed AAB, verifies signatures and
  SHA-256 checksums, and publishes a GitHub Release with 8 assets.

See [`docs/ci/CI_GUIDE.md`](docs/ci/CI_GUIDE.md) and
[`docs/maintenance/GITHUB_SIGNED_RELEASE_AUTOMATION.md`](docs/maintenance/GITHUB_SIGNED_RELEASE_AUTOMATION.md).

---

## Security & Privacy

- **Privacy First**: Smart File Assistant uses on-device Gemini Nano; no remote
  AI keys are required, and image content never leaves the device.
- **Local Storage**: Telegram session data and the TDLib database are stored in
  private application storage. Never share these files.
- **No Session Backup**: `allowBackup` is disabled, so TDLib session files are
  excluded from cloud backup and device-to-device transfer. Re-authenticate
  after a fresh install.
- **Network Security**: All traffic is encrypted (TLS); certificate pinning
  prevents MITM; cleartext traffic is blocked.
- **Screen Protection**: `FLAG_SECURE` prevents screenshots and screen recording
  of sensitive upload data.
- **Diagnostic Safety**: Diagnostics are privacy-conscious; inspect logs before
  sharing — sensitive identifiers are redacted by default.

---

## Troubleshooting

| Symptom | Recommended Action |
|---|---|
| App closes on connect | Check ABI-matching APK. Verify R8 keep rules. Check Logcat for native crashes. |
| TDLib unavailable | Confirm device ABI and verify `libtdjni.so` existence using `scripts/check-tdlib-artifacts.sh`. |
| Credentials rejected | Verify API ID (numeric) and API hash (complete). Don't confuse with login code. |
| No chats found | Complete auth, wait for chat updates, and confirm account permissions. |
| Arabic layout issues | Set system language to Arabic and restart. Verify RTL support. |
| Release signing fails | Check local keystore configuration. Never commit signing keys. |

> **Next release** · [v1.1.3 / versionCode 28](CHANGELOG.md#unreleased) — incremental dependency pass (setup-java v5→v6, roborazzi 1.75→1.76, KSP 2.3.11→2.3.12, Sentry 8.58→8.59). CI green on all 4 bumps; binary payload unchanged.

---

## 📖 Documentation Index

See [`docs/README.md`](docs/README.md) for the full, maintained index of
living documentation (architecture, design system, upload pipeline, Telegram
integration, background execution, file handling, lifecycle, errors, testing,
security, privacy, observability, performance, compatibility, accessibility,
i18n/localization, resources, dependencies, CI, release, operations, and
maintenance).

Binaries and release artifacts are distributed exclusively through GitHub
Releases; checksums and documentation live here.
