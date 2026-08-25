# Personal Glow Color Settings Maintenance Record

## Finding

The application owns static Material 3 schemes in `Theme.kt`, and `MainActivity` already collects DataStore’s theme preference before composing `TelegramDriveTheme`. `SettingsDataStore` persists string settings and `SettingsViewModel` combines them into `SettingsUiState`. This is an appropriate UI-only path for a persisted **Glow primary** preference.

## Safe preference model

The setting offers four curated, contrast-reviewed primary glow presets—Cobalt, Lime, Cyan, and Violet—not arbitrary RGB input. Each preset overrides only `primary`, `onPrimary`, `primaryContainer`, and `onPrimaryContainer` in the existing light/dark schemes. Teal completion, amber/retry, error, text, neutral surfaces, notification semantics, and upload progress authority remain unchanged.

| Preset | Purpose | Primary semantic use |
|---|---|---|
| Cobalt | Existing Mission Control default. | Existing primary action and selected control. |
| Lime | High-visibility signal preference. | Existing primary action and selected control. |
| Cyan | Cooler technical signal preference. | Existing primary action and selected control. |
| Violet | Softer Aurora signal preference. | Existing primary action and selected control. |

## Scope and protection

Allowed files are the centralized theme/preset model, `SettingsDataStore`, `MainActivity`, settings UI/ViewModel, English/Arabic resources, focused unit tests, and illustrative preview. The change does not modify TDLib, authentication, destinations, Room/WorkManager, upload state/progress, `UploadWorker`, notification policy/channel, semantic success/error roles, native libraries, signing, or CI.

## Accessibility and validation

The chosen preset is exposed through a radio-style selectable row with a visible color swatch and localized text; it does not use color as the only selection cue. Light and dark override pairs are explicit. Validate DataStore/preset parsing with a focused unit test, Kotlin compile, all debug unit tests, `git diff --check`, resource parity, and preview build. Device checks for theme switch persistence, RTL, large type, TalkBack, contrast, animation scale, and real upload remain **NOT VERIFIED** until observed.

## Reversal

Remove the dedicated preference key, preset flow/mapping, settings section, and optional preview controls. No upload or persisted task data migration is required.

## Implemented setting

`GlowColorPreset` defines Cobalt, Lime, Cyan, and Violet using explicit dark/light primary color pairs. It changes only primary roles through `ColorScheme.copy`; completion remains the existing teal tertiary role and error remains the existing semantic error role. `SettingsDataStore` persists the selected preset under `glow_color_preference`, `MainActivity` applies it before `TelegramDriveTheme`, and `SettingsViewModel` exposes it in `SettingsUiState`.

The Appearance section now displays localized, radio-selectable preset rows with a visible swatch and stable test tags. The control has no arbitrary color input, no color-only selection semantics, and no effect on TDLib, upload status, notifications, or destinations. The interactive preview provides the same four choices as a local-only illustration.

## Verification evidence

The constrained Android command completed successfully with JDK 17, Android SDK `/home/ubuntu/android-sdk`, one worker, and the 1 GiB heap:

```text
./gradlew :app:compileDebugKotlin :app:testDebugUnitTest --no-daemon --no-configuration-cache --max-workers=1
```

`GlowColorPresetTest` verifies fallback to Cobalt and preservation of tertiary/error roles. `git diff --check` completed without whitespace errors. English and Arabic resource files each contain six `glow_*` entries. The preview passed `pnpm check` and `pnpm build`; its final CSS-only focal adjustment is static and does not alter its interactive logic.

Actual-device persistence across app restart, light/dark visual contrast, Arabic RTL, large type, TalkBack, and real upload/notification behavior remain **NOT VERIFIED**. No commit, push, signing, or release operation was performed.

## Custom color extension

The preset model gains a `CUSTOM` mode plus one validated `RRGGBB` value in DataStore. A color wheel selects hue while an accessible Material `Slider` controls brightness. The editor keeps its color locally until the user activates **Save custom glow**, so its live preview affects only the editor before saving. The preview contains an ordinary button and selected-control treatment using the pending primary color; it is not an upload control and has no runtime effects.

Reset writes the default Cobalt selection and default Cobalt custom value in a single settings action. For a saved custom color, the theme calculates only the existing primary roles; success/tertiary, warning, error, neutral, and telemetry sources remain semantic and unmodified. Invalid stored hex values fall back safely to Cobalt.

The color wheel is pointer-friendly but not the only input: the labeled hue/brightness sliders provide an accessible equivalent. The visible selected radio/label remains the authoritative state cue. The custom editor is only exposed after the user chooses the Custom row, preventing unnecessary visual motion or form complexity.

## Custom editor implementation and verification

`GlowColorCodec` validates and normalizes a stored `RRGGBB` value, derives a central primary-role mapping, and falls back to Cobalt for invalid input. `GlowColorPreset.CUSTOM` consumes that value only for primary roles. `GlowColorEditor` has a pointer color wheel with tap and drag input, TalkBack content/state descriptions, plus labeled hue, saturation, and brightness `Slider` controls. The sliders are the complete non-pointer alternative; each reports its percentage state. The editor keeps a local pending preview until explicit save. Save and reset are each single DataStore transactions: save writes the custom hex and `Custom` selection together, and reset writes Cobalt plus the default hex together.

The reusable `tdlib-android-visual-motion-maintenance` skill now includes the personal-color trigger and `references/personal-glow-colors.md`; the skill validator completed successfully. Android compile and debug unit tests completed successfully after adding custom hex fallback and primary-role preservation coverage. `git diff --check` is clean. Resource parity includes nine custom-color entries in both English and Arabic. The web preview passed `pnpm check` and `pnpm build`; it illustrates a native color picker, pre-save local preview, and reset as non-data-connected behavior.

## Persistence, TalkBack, and contrast verification follow-up

`SettingsDataStorePersistenceTest` is an Android instrumentation test that writes `12ABEF`, creates a fresh settings reader, and verifies that both the `Custom` selection and normalized hex are read back. It restores Cobalt in `finally` and does not initialize TDLib, authenticate, select a destination, or enqueue uploads. The test compiles successfully with `:app:compileDebugAndroidTestKotlin`; it could not be executed here because neither `adb` nor an Android Virtual Device is available.

`GlowColorPresetTest` now evaluates black, white, magenta, cyan, and yellow custom sources in both themes. It asserts a minimum 4.5:1 contrast ratio for `primary/onPrimary` and `primaryContainer/onPrimaryContainer`. The custom dark container is opaque to prevent uncertain background blending, while the status roles remain unchanged. `:app:testDebugUnitTest` and the focused color test passed after this change; `git diff --check` and English/Arabic custom-resource parity also passed.

These are **repository/build verified** checks. Actual TalkBack traversal and spoken output, wheel touch/drag on a phone, true process termination and relaunch, display-level contrast under user font/display settings, Arabic RTL, and reduced-motion behavior are **NOT VERIFIED** because no device or emulator is attached.

The reusable `tdlib-android-visual-motion-maintenance` skill was updated with the same atomic-save, full-slider, and contrast-test guardrails, and `quick_validate.py tdlib-android-visual-motion-maintenance` completed successfully.
