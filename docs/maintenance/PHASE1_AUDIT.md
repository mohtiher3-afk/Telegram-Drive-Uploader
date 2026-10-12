# Phase 1 Audit — Telegram Drive Uploader

Read-only audit. Branch: `chore/phase1-audit`. No application code, build files,
dependency versions, or native libraries were changed.

Working tree at start: **not clean** — `docs/maintenance/PHASE1_AUDIT.md` was
already staged (left over from an earlier incomplete run) and `tools/` was
untracked. `tools/` was left untouched (out of scope).

All findings below were produced in this session. Anything not directly measured
is labelled as a gap rather than stated as a fact.

## 1. Current versions

| Component | Version | Source |
|---|---|---|
| AGP | 9.4.1 | `gradle/libs.versions.toml` → `agp` |
| Gradle wrapper | 9.8.0 | `gradle/wrapper/gradle-wrapper.properties` |
| Kotlin | 2.4.20 | `gradle/libs.versions.toml` → `kotlin` |
| KSP | 2.3.12 | `gradle/libs.versions.toml` → `googleDevtoolsKsp` |
| Compose BOM | 2026.09.00 | `gradle/libs.versions.toml` → `composeBom` |
| compileSdk | 37 | `app/build.gradle.kts`; `:core`, `:feature`, `:data`, `:domain` also 37 |
| targetSdk | 36 | `app/build.gradle.kts` |
| minSdk | 30 (app) / 24 (`:core`, `:feature`) | `app/build.gradle.kts`, `feature/build.gradle.kts`, `core/build.gradle.kts` |
| NDK | no `ndkVersion` set anywhere; ABIs selected only via `ndk { abiFilters }` and `splits.abi` in `app/build.gradle.kts` | read of build files |
| JDK | Temurin 21.0.12.1+1-LTS, `JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot` | `java -version` |
| Toolchain target | Java/JVM 17 (`sourceCompatibility`, `jvmTarget = JVM_17`) | `app/build.gradle.kts`, `feature/build.gradle.kts` |

App version: `versionCode = 28`, `versionName = 1.1.3` in root `build.gradle.kts`
(`app/build.gradle.kts` still hardcodes `versionCode = 27`).

## 2. Verification result — `./scripts/verify-project.sh QUICK`

Run on 2026-10-10. Full log at `build/reports/verification/verification-summary.txt`.

```
repository   PASS
tdlib        FAIL
gradle       PASS
compile      PASS
Tests        FAIL
Lint         NOT RUN   (QUICK mode skips lint)
Build        NOT RUN   (QUICK mode skips assemble)
security     PASS
ReleaseBuild NOT RUN
VERIFICATION FAILED
```

**Cause A — test compilation fails in `:feature` (this is why `Tests=FAIL`).**

`:feature:compileDebugUnitTestKotlin` fails with 15 errors, all in one file:
`feature/src/test/java/com/telegramdrive/uploader/feature/ui/MissionHomeScreenTest.kt`.

First failing line:

```
e: .../feature/src/test/java/com/telegramdrive/uploader/feature/ui/MissionHomeScreenTest.kt:15:54 Unresolved reference 'HomeScreenState'.
```

The errors are:

```
15:54  Unresolved reference 'HomeScreenState'
16:54  Unresolved reference 'QueueStats'
30:13  Unresolved reference 'MissionHomeScreen'
31:25  Unresolved reference 'HomeScreenState'
44:13  Unresolved reference 'MissionHomeScreen'
45:25  Unresolved reference 'HomeScreenState'
58:13  Unresolved reference 'MissionHomeScreen'
59:25  Unresolved reference 'HomeScreenState'
59:51  Cannot infer type for type parameter 'T'. Specify it explicitly.
72:13  No value passed for parameter 'sourceUri'.
82:13  Unresolved reference 'MissionHomeScreen'
83:25  Unresolved reference 'HomeScreenState'
95:21  Unresolved reference 'HomeScreenState'
96:21  Unresolved reference 'QueueStats'
106:13 Unresolved reference 'MissionHomeScreen'
```

This is **not** a missing-dependency problem. Verified by search:

- `MissionHomeScreen` and `HomeScreenState` do **not exist anywhere in production
  code** — the only occurrences of `HomeScreenState` in the whole repo are the
  imports and call sites inside that same test file.
- The production screen is `feature/.../feature/home/HomeScreen.kt`, whose
  signature is `HomeScreen(onSettingsClick, onConnectClick, onVideosSelected,
  viewModel)` — it takes a ViewModel, not a state object. It cannot be called the
  way the test calls it.
- `QueueStats` does exist, at
  `core/ui/src/main/java/com/telegramdrive/uploader/core/ui/components/QueueStats.kt`,
  but `:feature` does not depend on it, and `core/ui/` is **not a Gradle module at
  all** — `settings.gradle.kts` includes only `:app :domain :core :data :feature
  :benchmark`. `core/ui/` and `core/navigation/` have source trees but no
  `build.gradle.kts` and no `include(...)` entry.
- The test also asserts literal English strings ("Mission Control", "Your secure
  file transfer hub") that appear nowhere in production UI.

Conclusion: the test targets an API that was never built. The test has to be
rewritten against the real `HomeScreen` (or deleted), and `core/ui` has to be
wired into Gradle before anything in it is testable from `:feature`. Note
`:feature` cannot simply add `implementation(project(":core:ui"))` today — that
module is not registered in `settings.gradle.kts`.

**Cause B — TDLib artifact check fails.**

`scripts/check-tdlib-artifacts.sh` reports `TDLIB_ARTIFACTS_PRESENT=false`,
7 artifacts missing. First failing line:

```
❌ [MISSING CHECKSUM TARGET] app/src/main/jniLibs/armeabi-v7a/libcrypto.so
```

The script's expected paths point at `app/src/main/jniLibs/**`, but the
libraries actually live at `data/src/main/jniLibs/**` (all 9 files, all present).
Only `app/src/main/jniLibs/arm64-v8a/` is populated (3 files), which is what makes
the arm64 checksums resolve and the other two ABIs fail. The script additionally
reports, for every ABI:

```
[16KB CHECK] <abi>: no LOAD segments readable (readelf/llvm-objdump/Python failure)
```

so this repo's own gate currently cannot read ELF alignment at all — see section 3.

**Cause C — nothing else fails.** `repository`, `gradle`, `compile`, and
`security` (secrets, production-code, resource-integrity, WorkManager manifest)
all PASS.

`FULL` mode was **not** run: it would add `lint` and `:app:assembleDebug`, but
`:app` depends on `:feature`, and `:feature:compileDebugUnitTestKotlin` is already
broken. Per the rules in `AGENTS.md`, the honest status is: QUICK failed on tests
and TDLib, FULL not run.

## 3. 16 KB page-size alignment of native libraries

Measured with `python scripts/check-elf-alignment.py <file>` (a pure-Python ELF
program-header reader already in the repo), since `readelf`, `llvm-readelf`, and
`objdump` are not installed on this host and no NDK is present at
`%LOCALAPPDATA%\Android\Sdk\ndk`.

PT_LOAD `p_align` values:

| File | p_align values | 16 KB aligned? |
|---|---|---|
| `data/src/main/jniLibs/arm64-v8a/libtdjni.so` | 0x4000, 0x4000, 0x4000 | **YES** |
| `data/src/main/jniLibs/armeabi-v7a/libtdjni.so` | 0x4000, 0x4000, 0x4000 | **YES** |
| `data/src/main/jniLibs/x86_64/libtdjni.so` | 0x1000, 0x1000, 0x1000 | **NO — 4 KB aligned** |
| `data/src/main/jniLibs/arm64-v8a/libcrypto.so` | 0x4000 x4 | YES |
| `data/src/main/jniLibs/arm64-v8a/libssl.so` | 0x4000 x4 | YES |
| `data/src/main/jniLibs/armeabi-v7a/libcrypto.so` | 0x4000 x4 | YES |
| `data/src/main/jniLibs/armeabi-v7a/libssl.so` | 0x4000 x4 | YES |
| `data/src/main/jniLibs/x86_64/libcrypto.so` | 0x4000 x4 | YES |
| `data/src/main/jniLibs/x86_64/libssl.so` | 0x4000 x4 | YES |

**Finding: the x86_64 build of `libtdjni.so` is not 16 KB aligned** (0x1000 on
every LOAD segment) while the arm64-v8a and armeabi-v7a builds are (0x4000). Its
OpenSSL companions in the same directory are 16 KB aligned, so this is specific to
the x86_64 TDLib artifact, not to the toolchain used for that directory. x86_64 is
used for emulator/CI testing rather than shipping devices, but the file is packaged
for all three ABIs and is currently the only misaligned library in the tree.

The repo's own `scripts/check-tdlib-artifacts.sh` does not detect this — its
alignment probe reports "no LOAD segments readable" for every ABI because it needs
`readelf`/`llvm-objdump` and neither is available. `scripts/check-elf-alignment.py`
does work here and produced the table above.

**Packaging.** `app/build.gradle.kts` sets:

```kotlin
packaging {
  jniLibs {
    useLegacyPackaging = true
  }
}
```

`extractNativeLibs` is not set anywhere. `useLegacyPackaging = true` means the
`.so` files are unpacked to disk at install time (equivalent to
`extractNativeLibs="true"`), which is the behaviour required by the current
`System.load(<absolute path>)` loading scheme in section 6. ABI splits are enabled
for the app bundle (`bundle { abi { enableSplit = true } }`) and
`splits { abi { isUniversalApk = true } }`.

## 4. Back navigation

- `onBackPressed` — no occurrences in Kotlin/Java source under `app`, `:feature`,
  `:core`, `:data`, `:domain`.
- `KeyEvent.KEYCODE_BACK` — no occurrences.
- `BackHandler` — no occurrences. Navigation is entirely
  `androidx.navigation.compose`, which supplies predictive back through the
  system's `OnBackInvokedCallback` path.
- `android:enableOnBackInvokedCallback="true"` — **set**, at
  `app/src/main/AndroidManifest.xml:21`.

So there is nothing to migrate here; the app already opts into the new back
dispatcher and uses no deprecated APIs. Device confirmation is still worth doing
as a UX check (not a code change).

## 5. Edge-to-edge and RTL

**Edge-to-edge.** `enableEdgeToEdge()` is called exactly once,
`app/src/main/java/com/telegramdrive/uploader/MainActivity.kt:38`. However,
`app/src/main/java/com/telegramdrive/uploader/core/navigation/AppNavigation.kt`
**zeroes the insets on both navigation containers**:

- line 170 — `Scaffold(contentWindowInsets = WindowInsets(0, 0, 0, 0))`
- line 194 — `NavigationBar(windowInsets = WindowInsets(0, 0, 0, 0))`

Net effect: edge-to-edge is enabled at the Activity level, then every system inset
is discarded by the nav host. Screens do not receive insets from the Scaffold, so
each screen must handle its own, or content will sit under the status/navigation
bars and the cutout.

Screens with their own inset handling:

| Screen | Insets used |
|---|---|
| `feature/.../feature/onboarding/OnboardingScreen.kt` | 3 inset references |
| `feature/.../feature/telegram/TelegramAuthScreen.kt` | 1 |
| `feature/.../feature/telegram/TelegramDestinationScreen.kt` | 1 |
| `feature/.../feature/home/HomeScreen.kt` | 0 inset calls, but compensates with a hand-tuned `contentPadding` (`bottom = AppSpacing.extraLarge * 2`, commented "Double the bottom inset so the last row clears the navigation bar") |

Screens with **no** `WindowInsets` handling and **no** Scaffold:

- `feature/.../feature/splash/SplashScreen.kt`
- `feature/.../feature/upload/UploadScreen.kt`
- `feature/.../feature/queue/QueueScreen.kt`
- `feature/.../feature/settings/SettingsScreen.kt`
- `feature/.../feature/history/HistoryScreen.kt`

The last three do use a `Scaffold`, but since the app-level Scaffold passes
`WindowInsets(0,0,0,0)` and the theme applies `windowLightStatusBar=false`, they
rely on the Scaffold's default insets only where they do not override them — worth
a device pass over each. This is the single most likely source of layout-under-bars
bugs and it is not verifiable from source alone.

**RTL.** `android:supportsRtl="true"` is set
(`app/src/main/AndroidManifest.xml:28`), Arabic string resources exist
(`app/src/main/res/values-ar/strings.xml`), and `android:localeConfig` is declared.

- `paddingLeft` / `paddingRight` / `marginLeft` / `marginRight` — **zero**
  occurrences in production Kotlin.
- `Alignment.Left` / `Alignment.Right` / `Absolute.*` — zero occurrences.
- `layoutDirection` overrides — zero occurrences.
- Hardcoded user-facing strings in production Kotlin — **zero**. Every
  `Text("...")` literal with real words found by grep is inside
  `feature/src/test/**` (test assertions), which is correct usage.

So RTL hygiene is genuinely clean on the axes checked: start/end everywhere,
strings externalised, Arabic locale present. The gap is inset handling, not RTL.

## 6. Android 17 (API 37) risks

**Native library loading — safe, but fragile.**

`data/src/main/java/com/telegramdrive/uploader/data/telegram/client/TelegramClientImpl.kt`,
`ensureNativeRuntime()` (lines 956–996) loads from
`context.applicationInfo.nativeLibraryDir`:

```kotlin
val libDir = context.applicationInfo.nativeLibraryDir
val cryptoPath = "$libDir/libcrypto.so"
val sslPath = "$libDir/libssl.so"
val tdnjiPath = "$libDir/libtdjni.so"
...
System.load(tdnjiPath)
```

`nativeLibraryDir` is the APK's own extracted native library directory, not a
writable path, so this satisfies the API 37 requirement. Two caveats worth
recording:

1. It is annotated `@SuppressLint("UnsafeDynamicallyLoadedCode")` — correct given
   the source is verified, but it means lint will not help if the path ever
   changes.
2. `System.load(<absolute path>)` only works because
   `useLegacyPackaging = true` extracts the libraries to disk. Flipping that flag
   to `false` (needed to shrink APK size / enable `android:extractNativeLibs=false`)
   would break this loader — the fallback `System.loadLibrary("tdjni")` only runs
   when the file does *not* exist, so it would not rescue the change. The two
   settings must be changed together.

Other copies of `TdApi.java`, `Client.java`, `Log.kt` use plain
`System.loadLibrary("tdjni")`, which is fine and is the preferred form.

**Network security config.** `app/src/main/res/xml/network_security_config.xml`
exists and sets `cleartextTrafficPermitted="false"` app-wide with system trust
anchors only. Certificate pinning is deliberately absent, with a comment
explaining that TDLib does its own TLS through the bundled OpenSSL and a
`<pin-set>` would not apply to it. No change needed for API 37.

**Orientation / resizability.**

- `android:resizeableActivity="true"` — set (`AndroidManifest.xml:64`).
- `android:screenOrientation` — **not set** anywhere in any manifest, so there is
  no orientation lock to remove.
- `android:configChanges` — not set on `MainActivity`; the activity recreates on
  rotation. Combined with the zeroed insets in section 5, rotation is the most
  likely place to see layout breakage.
- `theme` sets `android:windowLightStatusBar=false` (`values/themes.xml:12`),
  consistent with the dark-only theme.

## 7. WorkManager / foreground service

**Service type.** `app/src/main/AndroidManifest.xml:53-56` declares:

```xml
<service
    android:name="androidx.work.impl.foreground.SystemForegroundService"
    android:foregroundServiceType="dataSync"
    tools:node="merge" />
```

`dataSync` — consistent with `FOREGROUND_SERVICE_DATA_SYNC`. A second service
exists, `com.telegramdrive.uploader.service.ForegroundUploadControlService`
(lines 57-59), which declares **no** `android:foregroundServiceType`. On API 34+
a foreground service without a declared type is rejected at runtime; if that
service is ever started via `startForeground(...)` this will crash. It is only
listed as a control service here — worth confirming whether it actually calls
`startForeground` (it is `android:exported="false"` and appears to be a control
channel). Flagging as a risk, not a confirmed defect.

WorkManager's default initializer is explicitly removed
(`tools:node="remove"` on `androidx.work.WorkManagerInitializer`), so WorkManager
is configured on demand — that is the standard Hilt-Work setup.

**Permissions requested** (`AndroidManifest.xml:5-15`):

| Permission | Note |
|---|---|
| `INTERNET` | |
| `ACCESS_NETWORK_STATE` | also re-declared in `feature/src/main/AndroidManifest.xml` |
| `READ_MEDIA_VIDEO` | |
| `READ_MEDIA_VISUAL_USER_SELECTED` | |
| `READ_EXTERNAL_STORAGE` | `android:maxSdkVersion="32"` |
| `WAKE_LOCK` | |
| `POST_NOTIFICATIONS` | |
| `FOREGROUND_SERVICE` | |
| `FOREGROUND_SERVICE_DATA_SYNC` | matches the `dataSync` type |

Nothing here looks like it breaks on API 37. No `MANAGE_EXTERNAL_STORAGE`, no
`QUERY_ALL_PACKAGES`.

## 8. Prioritized next five tasks

Each is one commit-sized change.

1. **Unblock `:feature` tests.** Rewrite
   `feature/src/test/java/com/telegramdrive/uploader/feature/ui/MissionHomeScreenTest.kt`
   against the real `HomeScreen(onSettingsClick, onConnectClick, onVideosSelected,
   viewModel)` API, or delete it if it cannot be made meaningful — it currently
   tests a screen that does not exist. Without this, `verify-project.sh` cannot go
   green in any mode.
2. **Register `:core:ui` as a Gradle module.** `core/ui/` has sources
   (`QueueStats.kt` and the Mission components) but no `build.gradle.kts` and no
   `include(":core:ui")` in `settings.gradle.kts`, so nothing outside `:core` can
   compile against it. Same question for `core/navigation/`, which
   `AppNavigation.kt` sits in. Decide module boundary first, then add the
   `include` and the dependency.
3. **Fix the TDLib artifact gate so it checks the real paths and really reads ELF
   alignment.** `scripts/check-tdlib-artifacts.sh` points at
   `app/src/main/jniLibs/**` while the libraries live in `data/src/main/jniLibs/**`,
   and its 16 KB probe always reports "no LOAD segments readable" because no
   readelf is installed. Point it at the real paths and delegate the alignment read
   to `scripts/check-elf-alignment.py`, which works. That makes the gate actually
   report the x86_64 finding from section 3.
4. **Fix or replace the misaligned x86_64 `libtdjni.so`.** It is 0x1000-aligned
   while its arm64-v8a and armeabi-v7a siblings are 0x4000. Either rebuild it with
   16 KB alignment or drop x86_64 from the shipped ABI list. **This is a native
   artifact change and is explicitly out of bounds for this phase** — needs your
   go-ahead, per the "do not touch libtdjni.so" rule.
5. **Pass real insets to the navigation host.** In `AppNavigation.kt`, replace the
   two `WindowInsets(0, 0, 0, 0)` overrides (lines 170 and 194) with the system
   insets, then remove the hand-tuned `bottom = AppSpacing.extraLarge * 2`
   workaround in `HomeScreen.kt`. Verify on a device with a cutout and a gesture
   nav bar, in both orientations, before calling it done.

## Audit provenance

- Audit date: 2026-10-10, branch `chore/phase1-audit`.
- Commands actually run: `./scripts/verify-project.sh QUICK`;
  `./gradlew --no-daemon :feature:compileDebugUnitTestKotlin`;
  `bash scripts/check-tdlib-artifacts.sh`;
  `python scripts/check-elf-alignment.py <each .so>`; `java -version`;
  `git status`; plus content searches over the source tree.
- Files read in full: `AGENTS.md`, `build.gradle.kts`, `app/build.gradle.kts`,
  `feature/build.gradle.kts`, `core/build.gradle.kts`,
  `gradle/libs.versions.toml`, `gradle/wrapper/gradle-wrapper.properties`,
  `settings.gradle.kts`, `app/src/main/AndroidManifest.xml`,
  `feature/src/main/AndroidManifest.xml`,
  `app/src/nonMinifiedRelease/AndroidManifest.xml`,
  `app/src/main/res/xml/network_security_config.xml`,
  `scripts/verify-project.sh`, `scripts/check-elf-alignment.py`,
  `MissionHomeScreenTest.kt`, `QueueStats.kt`.
- Not verified in this phase (stated as gaps, not facts): on-device back/predictive
  back behaviour, on-device inset layout under bars and cutouts, `lint`
  (`lintVitalRelease`) and `assembleDebug` results — FULL mode was not run.
- Not available on this host: `readelf`, `llvm-readelf`, `objdump`, an NDK, and
  the `android` CLI.
