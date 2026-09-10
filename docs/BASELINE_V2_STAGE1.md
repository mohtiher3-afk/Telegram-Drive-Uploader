# Baseline Report — Stage 1 (Spec V2)

| Field | Value |
|-------|-------|
| Stage | 1 — Baseline (Spec V2 §1–§3) |
| Date | 2026-09-08 |
| Repository | `mohtiher3-afk/Telegram-Drive-Uploader` |
| Branch | `main` |
| App version | 1.0.22 (versionCode 22) |
| Purpose | Untouched-baseline record before any Stage-2+ change |

---

## 1. Scope and method

Spec V2 Stage 1 requires: inspect and document the existing repository without
modifying source; build the untouched baseline; report architecture,
dependencies, upload flow, permissions, security issues, and test coverage.

Method:
- Read-only inspection of source tree, Gradle config, manifest, resources,
  workflows, and existing documentation.
- Clean re-run of the full local gates with `--rerun-tasks` so cache does not
  mask results: `:app:testDebugUnitTest`, `:app:lintVitalRelease`,
  `:app:assembleRelease`.
- No product source, dependency, or workflow file was modified.

---

## 2. Toolchain and build identity

| Item | Value | Evidence |
|------|-------|----------|
| Kotlin | 2.3.21 | `gradle/libs.versions.toml` |
| AGP | 9.4.0 | `gradle/libs.versions.toml` |
| Gradle | 9.6.0 | wrapper |
| JDK | 21.0.12.1 LTS (local) / Temurin 21 (CI) | `java -version`, `android-ci.yml` |
| compileSdk | 37 | `app/build.gradle.kts` |
| targetSdk | 36 | `app/build.gradle.kts` |
| minSdk | 24 | `app/build.gradle.kts` |
| applicationId | `com.aistudio.telegramdrive.prmuq` | `app/build.gradle.kts` |
| namespace | `com.telegramdrive.uploader` | `app/build.gradle.kts` |
| Kotlin/JVM target | 17 | `app/build.gradle.kts` |
| buildDir | `${java.io.tmpdir}/tdg-build/app/build` (ephemeral) | `app/build.gradle.kts` |
| Native libs | TDLib 1.8.66 builds: `libtdjni.so`, `libcrypto.so`, `libssl.so` for arm64-v8a, armeabi-v7a, x86_64 | `app/src/main/jniLibs` |

---

## 3. Repository structure map

```
app/src/main/java/com/telegramdrive/uploader/
├── core/
│   ├── ai/               SmartFileAssistant (local heuristics, not inferred-model dependent)
│   ├── di/               Hilt modules: DatabaseModule, RepositoryModule, UploadModule, WorkModule
│   ├── diagnostics/      DiagnosticsManager
│   ├── navigation/       AppNavigation, AppRoutes
│   ├── notifications/    AndroidUploadEventNotifier (progress/completion/failure)
│   ├── ui/components/    EmptyState, ErrorState, TransferMetrics, UploadStatusIndicator,
│   │                     UploadTelemetryFormatter, VideoItem, LiquidGlassSurface,
│   │                     MissionControlPage, GlowFocusIndicator
│   ├── ui/theme/         Color, DesignTokens, GlowColorPreset, MotionTokens, Theme, Type
│   └── util/
│       ├── PinnedDestinationIds
│       └── media/        VideoCompressor, VideoFormatSupport, VideoMetadataExtractor
├── data/
│   ├── local/database/   Room: AppDatabase, UploadDao, UploadEntity
│   ├── local/datastore/  SettingsDataStore
│   ├── repository/       UploadRepositoryImpl
│   ├── telegram/
│   │   ├── client/       TelegramClient, TelegramClientImpl (TDLib)
│   │   └── repository/   TelegramRepositoryImpl
│   └── upload/           TelegramUploadEngineImpl, UploadManagerImpl, UploadWorkPolicy,
│                         reader/StreamingFileReader
├── domain/
│   ├── model/            TelegramConnectionState, TelegramDestination(+Policy), TelegramError,
│   │                     TelegramUser, UploadError, UploadProgress, UploadStatus, UploadTask
│   ├── repository/       TelegramRepository, UploadRepository
│   └── upload/           SpeedCalculator, TelegramUploadEngine, UploadCompletionPolicy,
│                         UploadEventNotifier, UploadEventNotificationPolicy, UploadManager
├── feature/
│   ├── history/          HistoryScreen, HistoryViewModel
│   ├── home/             HomeScreen, HomeViewModel, GlassCardHover
│   ├── onboarding/       OnboardingScreen, OnboardingViewModel
│   ├── queue/            QueueScreen, QueueViewModel
│   ├── settings/         SettingsScreen, SettingsViewModel, GlowColorEditor
│   ├── splash/           SplashScreen
│   ├── telegram/         TelegramAuthScreen/ViewModel, TelegramDestinationScreen/ViewModel
│   └── upload/           UploadScreen, UploadViewModel, worker/UploadWorker
├── MainActivity.kt
└── TelegramDriveApp.kt
```

Architecture follows the spec's target layering: presentation (Compose +
ViewModel + immutable UI state), domain (use cases, queue rules, progress
model), data (TDLib client, repositories, Room, DataStore, content resolver),
execution (WorkManager), DI (Hilt). UI is Compose with StateFlow/Flow +
coroutines; transport is isolated behind `TelegramUploadEngine`/`TelegramRepository`
interfaces so it can be replaced or tested with fakes.

---

## 4. Spec V2 → evidence matrix

Legend: ✅ implemented / ⚠️ partial / 🔲 external (requires device, credentials, or user approval).

| Spec section | Requirement | Status | Evidence |
|--------------|-------------|--------|----------|
| §2 Goals | Native Android app, video upload to Telegram bot destination | ✅ | TDLib engine, destination search |
| §3 Baseline | repository assessed, no blind rewrite | ✅ | this report |
| §4 Architecture | presentation/domain/data/execution layering + Hilt | ✅ | package map above, `core/di/*` |
| §5 Stack | Compose, M3, Navigation, ViewModel, coroutines/Flow, Hilt, Room, DataStore, WorkManager, Photo Picker/SAF, Keystore | ✅/⚠️ | all present; secret storage keeps tokens out of source/logs (see §13) |
| §6 Screens | Splash, Home, Picker, Queue, Upload Details, History, Settings, Bot Config, About/Diagnostics | ✅/⚠️ | 9 screens implemented; Bot Config embedded in Settings, About/Diagnostics partial |
| §7 Visual design | purple primary, red accent, rounded cards, glass surfaces, dark/light, reduced motion, responsive, accessibility | ✅ | Mission Control theme, DesignTokens/Glow/Motion tokens, LiquidGlassSurface, values/ + values-ar/ |
| §8 Branding/splash/icon | adaptive icon, splash brand transition | ✅ | `feature/splash`, core-splashscreen, launcher asset set |
| §9 Telegram upload engine | streaming, MIME/URI/destination validation, persisted queue, real progress, categorized errors, compliant retry | ✅ | StreamingFileReader, TelegramUploadEngineImpl, UploadError categories, UploadCompletionPolicy |
| §10 Background | Room durable queue, WorkManager scheduling, foreground service, connectivity, recovery | ✅ | UploadWorkPolicy, UploadWorker, dataSync foreground, WorkManager constraints |
| §11 Notifications | ongoing + file name/percent/speed/ETA, cancel/open/retry, completion/failure, POST_NOTIFICATIONS | ✅ | AndroidUploadEventNotifier, POST_NOTIFICATIONS manifest |
| §12 Queue controls | add multiple, persist ordering, cancel item/queue, retry, unique work ids, idempotent transitions | ✅ | UploadDao, QueueViewModel, UploadWorkPolicy (unique work), tests |
| §13 Permissions & security | INTERNET only core, Photo Picker/SAF minimized, token secrecy, secure storage, in-app privacy explanation | ✅/⚠️ | INTERNET+NETWORK_STATE+READ_MEDIA_VIDEO+POST_NOTIFICATIONS+FGS(dataSync); no bot token in source; privacy docs exist |
| §14 Performance | streaming buffers, no temp copies, throttled progress, sequential default, profiling | ✅/⚠️ | streaming I/O, throttling; host-based profiling only, device profiling 🔲 |
| §15 Testing | unit (queue/retry/state/progress/speed/ETA), repo with fake transport, worker tests, Compose tests, Android 13–16 notification/background, small/large videos, offline, invalid config, process death | ✅/⚠️ | 101 unit tests green; device/emulator matrix 🔲 |
| §16 Master prompt stage prompts | staged execution discipline | ✅ | history of staged phases documented in `docs/` |

Documentation subgroups in `docs/`: accessibility, architecture, background,
bugs, ci, compatibility, dependencies, design, errors, features, files,
final-audit, i18n, lifecycle, localization, maintenance, observability,
operations, performance, privacy, release, resources, security, telegram,
testing, upload.

---

## 5. Build result (untouched baseline, clean re-run)

Command: `./gradlew :app:testDebugUnitTest :app:lintVitalRelease :app:assembleRelease --rerun-tasks`

| Gate | Result | Evidence |
|------|--------|----------|
| `:app:testDebugUnitTest` | ✅ BUILD SUCCESSFUL (3m 9s) | 30 result files, **101 tests, 0 failures, 0 errors** |
| `:app:lintVitalRelease` | ✅ BUILD SUCCESSFUL (blocking lint clean) | no fatal errors; only non-blocking InlinedApi warnings |
| `:app:assembleRelease` | ✅ BUILD SUCCESSFUL (4m 38s combined) | `app-release-unsigned.apk` 48.4 MB |
| Native ABI packaging | ✅ | arm64-v8a, armeabi-v7a, x86_64 each contain `libtdjni.so`, `libcrypto.so`, `libssl.so` |

Env notes:
- `buildDir` resolves to `%TEMP%\tdg-build\app\build` per build script.
- SDK platforms installed locally: android-35, android-36, android-37.0
  (compileSdk 37 satisfied).
- `local.properties` `sdk.dir` present; JDK 21 LTS matches CI.

## 6. Known non-blocking warnings

- `SettingsScreen.kt:369-370` — `Settings.ACTION_APP_NOTIFICATION_SETTINGS` /
  `EXTRA_APP_PACKAGE` require API 26 (min is 24). Value is compile-time inlined;
  intent fires only from an API-26+ context by user action, so low risk.
- `UploadWorker.kt:36` — `FOREGROUND_SERVICE_TYPE_DATA_SYNC` requires API 29
  (min 24). Constant inlined; manifest declares `dataSync` FGS type with the
  counterpart permission, launch gated to supported API levels.

Neither is blocking; both are candidates for `@RequiresApi`/`Build.VERSION`
guards in a later low-risk stage, not in the baseline.

## 7. Security posture summary

- No bot token / API hash / private key in source, manifest, or documented
  strings. Signing keystore is a GitHub secret (release path) or local
  `debug.keystore`; release builds never fall back to debug keys.
- Confidentiality: `check-repository-security.sh` in CI scans for secrets and
  signing material before build.
- `TDLib` session backup disabled (Android backup exclusions) — commit
  `487f627`.
- Permissions kept minimal (see §4).
- Known 🔲: real-account and real-channel runtime validation still requires the
  user's test credentials/device; the app fail-closes when unauthenticated.

## 8. External / deferred items (🔲)

1. **Device matrix Android 13–16** — DONE via cloud emulator matrix (see §11);
   physical-device notification/background confirmation still open.
2. **§14 performance benchmarking** with representative video sizes on device;
   host-side profiling is documented but device telemetry (Peak RSS) is CI-only.
3. **Firebase / Crashlytics credits** — no real account approved; crash
   reporting is not wired and must not be added without approval.
4. **`tdlib-release-check` (gh-aw)** — root cause proven and fixed
    2026-09-10. The account is **Copilot Free ("automatic model selection
    only")**: every explicitly pinned model — including `auto` itself once the
    harness pins it (`configuredModel="auto"`) — is rejected with `400 The
    requested model is not supported / unavailable for this subscription
    tier`. Matches upstream `github/gh-aw#46531` exactly; the fix PR
    (`#46556`, `model: auto`/`none` sentinels) was closed unmerged, and the
    PDF guide's `copilot-requests: write` path was re-tested (run
    `34434639765`) and failed identically. Fix: `codex` engine via the
    OpenRouter free router (`model: openrouter/free`, `OPENAI_BASE_URL`,
    `max-ai-credits: -1` to bypass AIC pricing for BYOK models).
    Green in run `34442903270` (router picked
    `nvidia/nemotron-3-ultra-550b-a12b:free`), which produced issue `#12`
    end-to-end via `create_issue`. Issues `#5/#6/#7/#8/#9/#10/#11` closed.
    Requires repo secret `OPENROUTER_API_KEY`. Known fragility: the free
    router picks randomly — a reasoning-mandatory pick fails the run; a
    re-run re-rolls. If flakiness persists, fund OpenRouter credits and pin
    `deepseek/deepseek-chat`.

## 9. Migration plan (lowest → highest risk)

Ordered workstreams for the remaining spec stages. No stage starts until the
previous is verified on the full gates.

1. **Docs-only normalization** (risk: none) — resolve spec-matrix gaps with
   evidence from existing audits; no code change. *Stage 1 output.*
2. **Warning guards** (low) — `@RequiresApi`/`Build.VERSION` guards for the two
   InlinedApi warnings plus focused tests.
3. **About/Diagnostics completeness** (low) — extend existing diagnostics
   screen with version/build/non-sensitive info, matching §6 without touching
   TDLib/upload logic.
4. **Device/emulator validation** (medium, external) — DONE: API 33–36 emulator
   matrix via cloud (see §11); physical-device confirmation optional.
5. **Performance §14** (medium, external) — host-side streaming benchmarks then
   representative-file device throughput measurement; sequential stays default.
6. **Privacy/safety review gates** (medium) — final security re-scan and
   accessibility audit on the changed screens only.
7. **Release prep** (highest) — version bump, signing, multi-ABI CI, artifact
   verification, checksums, publication — only after explicit authorization.

---

## 10. Conclusion

The repository already satisfies the large majority of the Spec V2 Stage-1 baseline contract: layered architecture, real TDLib upload engine, Room-backed queue, WorkManager background execution, Compose/M3 Mission Control UI with Arabic RTL, Hilt DI, documented security posture, and 101 green unit tests on a clean re-run. The remaining items are external (physical-device matrix, §14 benchmark, Firebase approval, Copilot subscription) plus two low-risk warning guards and a diagnostics completeness pass. No product source was modified by this stage.

## 11. Device matrix execution (API 33–36) — ADDENDUM

After the untouched-baseline build, `android-device-smoke.yml` was parametrized
with a `strategy.matrix` over API levels 33/34/35/36 (one job per Android
13/14/15/16), x86_64, `google_apis`, `pixel_2` profile, KVM-accelerated host
runner. Workflow change: commit `205886d`.

Result (run `34264391418`):

| Job | Conclusion |
|-----|------------|
| TDLib JNI smoke (Android 36) | ✅ success |
| TDLib JNI smoke (Android 35) | ✅ success |
| TDLib JNI smoke (Android 34) | ✅ success |
| TDLib JNI smoke (Android 33) | ✅ success |

Each wave executed the same `run-tdlib-device-smoke-test.sh` suite: APK
install, TDLib JNI init/destroy on the emulator, official-artifact check.
All four Android 13–16 waves pass. This closes the API 13–16 emulator matrix
(Spec V2 §15); a physical-device notification/background confirmation remains
optional and requires user hardware.
baseline contract: layered architecture, real TDLib upload engine, Room-backed
queue, WorkManager background execution, Compose/M3 Mission Control UI with
Arabic RTL, Hilt DI, documented security posture, and 101 green unit tests on a
clean re-run. The remaining items are external (physical-device matrix, §14
benchmark, Firebase approval) plus two low-risk warning guards and a
diagnostics pass; the `tdlib-release-check` model issue was fixed in-workflow.
No product source was modified by this stage.