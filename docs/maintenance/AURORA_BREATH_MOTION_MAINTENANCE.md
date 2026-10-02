# Aurora Breathing Motion Maintenance Record

## Motion intent

Apply a quiet, decorative breathing effect to the existing Aurora glow inside the home upload feature card. The movement should feel like a slow change in light depth, not an alert, loading signal, success celebration, or upload-progress indicator.

| Parameter | Decision |
|---|---|
| Target | Existing clipped radial Aurora layers in `UploadFeatureCard` only. |
| Cycle | 2,800 ms, reverse repeat, standard easing. |
| Range | Radius and opacity share a restrained multiplier from 0.90 to 1.08. |
| Spatial movement | None. The glow does not drift, rotate, or move toward controls. |
| Reduced motion | Render a static multiplier of 1.0 when Android animator-duration accessibility is disabled. |
| Upload semantics | No connection state, upload percentage, list row, error, or terminal status is animated. |

## Protected behavior

| Area | Protected behavior |
|---|---|
| File selection | The existing document-picker callback and `select_videos_button` test tag remain unchanged. |
| Motion preference | `rememberSystemMotionEnabled()` remains the authority; disabled system animation produces no infinite transition. |
| Upload/TDLib | Home state, upload progress, TDLib, authorization, Room/DataStore, WorkManager, native libraries, and delivery remain untouched. |
| Accessibility/localization | Existing text, semantics, RTL, color roles, and static readability remain unchanged. |
| Build/release | No dependencies, Gradle, signing, CI, ABI, or release configuration changes are in scope. |

## Change record

| Field | Record |
|---|---|
| Finding | The Aurora layers are static and visually ready for a minimal breathing motion, but no semantic motion token exists for a slow ambient decorative cycle. |
| Smallest safe change | Add one semantic duration token and a local composable pulse value; use it only to scale Aurora glow radius/opacity. |
| Risk | Low visual/performance risk. Continuous animation must be disabled under the system reduced-motion setting and must not imply upload activity. |
| Validation | Compile Kotlin, run unit tests, inspect the diff and protected paths, and verify static fallback by code review. Device accessibility and visual timing remain runtime-unverified. |
| Reversal | Revert `MotionTokens.kt`, `HomeScreen.kt`, and this record. No persisted state changes occur. |

## Checklist

- [x] Define a limited decorative motion target and interaction boundary.
- [x] Preserve reduced-motion behavior and protected runtime paths.
- [x] Add the semantic token and static fallback.
- [x] Apply the pulse only to Aurora drawing parameters.
- [x] Compile and run unit tests.
- [x] Run static and protected-path checks.
- [x] Update outcomes and limitations.
- [ ] Commit and push only if explicitly requested by the user.

## Outcomes and limitations

`AppMotion.auroraBreath()` now centralizes a 2,800 ms reverse-repeat standard-easing cycle. `UploadFeatureCard` uses this value only to vary the existing Aurora radial-layer opacity and radius from 0.90 to 1.08. The effect has no spatial drift, does not touch upload/connection/progress data, and does not add a loading or completion implication.

When `rememberSystemMotionEnabled()` reports that Android animations are disabled, no infinite transition is created and the Aurora uses the static multiplier `1f`. The card’s file-picker callback, localized copy, test tag, primary action color, and semantics remain unchanged.

`./gradlew :app:compileDebugKotlin --no-configuration-cache --max-workers=1` and `./gradlew :app:testDebugUnitTest --no-configuration-cache --max-workers=1` completed successfully with JDK 17 and Android SDK API 36 outside the repository. `git diff --check` passed, and the protected-path review found no changes to TDLib, upload, WorkManager, native code, Gradle, CI, signing, or release configuration.

No device/emulator runtime has been used to observe the tempo, animator-duration setting, TalkBack, RTL, or large-font behavior. The motion is build- and unit-test-verified, not device-verified. Real Telegram authentication and delivery remain out of scope.

## Verification status

| Layer | Status | Notes |
|---|---|---|
| Repository/motion audit | PASS | Existing motion tokens, animator setting handling, callback boundaries, and protected paths were inspected. |
| Kotlin compilation | PASS | `:app:compileDebugKotlin --no-configuration-cache --max-workers=1` completed successfully. |
| Unit tests | PASS | `:app:testDebugUnitTest --no-configuration-cache --max-workers=1` completed successfully. |
| Static diff/protected paths | PASS | `git diff --check` passed; no protected runtime/build/release path changed. |
| Device visual/reduced-motion/RTL/TalkBack | NOT VERIFIED | No connected device/emulator check has been performed. |
| Real Telegram authentication/upload | NOT VERIFIED / OUT OF SCOPE | This decorative motion phase does not exercise real delivery. |
