# Glassmorphism Home Interface Maintenance Record

## Intent

Apply the Pinterest reference’s dark glass hierarchy to the existing Home upload interface. The user’s primary task remains selecting videos and following real upload state; the visual change must not introduce dashboard-only controls or decorative state signals.

## Repository evidence

| Element | Evidence | Glass translation |
|---|---|---|
| Upload hero | `HomeScreen.kt` uses `UploadFeatureCard` with the genuine document-picker callback and a single Aurora focal point. | Keep one dominant card; use a translucent surface, clipped Aurora, and a fine outline. |
| Telegram connection | `ConnectionCard` is driven by real connection state and its existing connect action. | Use a restrained glass surface only; retain text label, avatar/state icon, and semantic connection color. |
| Statistics and empty state | Existing stat and queue cards are data-bound. | Apply secondary glass layering with lower visual energy than the upload hero. |
| Theme | `Theme.kt` has user-selectable light/dark color schemes and semantic M3 roles. | Apply translucency only when the active surface is dark; keep light-mode cards opaque and semantic. |

## Protected behavior

The picker callback, Hilt ViewModel state, upload telemetry, queue list, TDLib, WorkManager, notification integration, navigation, Arabic/English strings, RTL, test tags, and reduced-motion Aurora behavior remain unchanged.

## Smallest safe visual change

Use existing Material color roles with alpha only on dark surfaces, paired `onSurface` text, clipped gradients, and `outlineVariant` borders. Change only Home composable styling. Error, progress, notification, and connection state remain semantic and continue to include text/icons.

## Validation and limits

Run focused Kotlin compilation and unit tests, inspect the diff and protected paths, and provide the existing interactive Mission Control Aurora preview as a visual direction check. Device composition, RTL with large text, TalkBack, dynamic-color contrast, and real upload behavior are runtime checks and remain not verified until observed.

## Verification result

| Layer | Result | Evidence |
|---|---|---|
| Kotlin compile and unit tests | PASS | `:app:compileDebugKotlin :app:testDebugUnitTest` completed successfully with JDK 17, Android SDK 36, one worker, and a command-line 1 GiB Gradle heap cap. |
| Static diff check | PASS | `git diff --check` completed without whitespace errors. |
| Glass scope | PASS | This change edits only `HomeScreen.kt`, this record, and the work list. The existing worker notification diff belongs to the earlier notification-integration task and was not edited in this Glassmorphism change. |
| Visual direction preview | PASS — illustrative | The Mission Control Aurora interactive preview shows the intended dark, translucent card hierarchy and restrained Aurora emphasis. It is a visual reference, not a runtime capture of the Android APK. |
| Device runtime check | NOT VERIFIED | Android device/emulator, RTL at large font sizes, TalkBack, light/dark dynamic-color contrast, and actual picker/upload behavior require runtime observation. |
