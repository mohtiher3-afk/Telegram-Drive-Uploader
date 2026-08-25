# Glass Card Hover Motion Maintenance Record

## Finding

`HomeScreen.kt` already contains dark glass surfaces for the upload hero, Telegram connection card, and statistic cards. These cards are display surfaces; the video picker and connection action remain child controls with their own genuine callbacks.

## Design and scope

For Android devices with a mouse or trackpad, the three glass-card families will use `hoverable` with a local `MutableInteractionSource`. A pointer hover will produce only a short upward translation, a near-imperceptible scale change, and a brighter glass border. The cards will not gain click callbacks, focus stops, fake status, or action semantics. On touch-only devices, no pointer-hover event occurs. When the Android animation scale disables motion, existing centralized motion tokens resolve the transition immediately.

## Protected behavior

TDLib, upload progress, WorkManager, picker callbacks, Telegram connection, notifications, card content, localization, RTL, accessibility labels, navigation, and test tags remain unchanged. No large list or progress animation is added.

## Risk and validation

The risk is limited to visual composable state and pointer-input compatibility. Validate with focused Kotlin compilation, unit tests, static diff inspection, and a visual direction preview. Pointer hover, physical keyboard focus, TalkBack, and touch interaction require Android hardware/emulator evidence and remain not verified unless observed.

## Reversal

Remove the local hover interaction helper and its three card modifiers/border-color bindings; no persisted data or runtime upload behavior is changed.

## Verification result

| Layer | Result | Evidence |
|---|---|---|
| Kotlin compile and unit tests | PASS | `:app:compileDebugKotlin :app:testDebugUnitTest` completed successfully with JDK 17, Android SDK 36, one worker, and an explicit 1 GiB Gradle heap cap. |
| Static diff check | PASS | `git diff --check` completed without whitespace errors. |
| Change scope | PASS | The implementation adds `GlassCardHover.kt`, updates `HomeScreen.kt`, and records this phase. It does not edit TDLib, worker, progress, notification, authentication, or persistence code. |
| Pointer hover / keyboard / touch runtime | NOT VERIFIED | Requires Android hardware or emulator with a mouse/trackpad and accessibility settings. The display cards intentionally do not receive a new keyboard focus stop or click action. |
