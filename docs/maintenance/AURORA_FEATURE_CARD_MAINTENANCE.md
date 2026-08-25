# Mission Control Aurora Feature Card Maintenance Record

## Scope and source evidence

This implementation applies the approved visual direction gathered from the user-provided Pinterest references: sparse near-black feature surfaces; a single focal aurora; clipped low-opacity violet, cobalt, magenta, and restrained white highlights; and Lime reserved for the authentic primary action. The visual reference notes are recorded in `docs/design/PINTEREST_CARD_REFERENCE_NOTES.md`.

The scope is intentionally limited to the home upload feature card. No animation is introduced in this pass; motion design is a separate, user-deferred phase.

## Protected behavior

| Area | Protected behavior |
|---|---|
| File selection | `OpenMultipleDocuments`, the `onSelectVideos` callback, and the `select_videos_button` test tag remain unchanged. |
| Data and state | `HomeViewModel`, upload counts, active uploads, connection state, Room, DataStore, WorkManager, and upload progress remain untouched. |
| TDLib | Authorization, destinations, client lifecycle, native artifacts, and real upload behavior remain untouched. |
| Localization/accessibility | Existing resource strings, content descriptions, touch target, light/dark theme path, RTL layout direction, and reduced-motion behavior remain authoritative. |
| Navigation/release | Routes, adaptive navigation, dependencies, build configuration, signing, CI, and release settings remain untouched. |

## Change record

| Field | Record |
|---|---|
| Finding | `UploadFeatureCard` currently uses an even, full-card high-saturation linear gradient, which competes with the card copy and does not reflect the calmer single-focal aurora direction in the collected references. |
| Smallest safe change | Replace the linear fill with a semantic near-black base and a clipped lower/right set of static, low-opacity radial glows. Keep the existing content hierarchy and real primary button unchanged. |
| Risk | Visual contrast and composition risk only. Runtime upload, persistence, authentication, and native risk is out of scope. |
| Validation | Run `git diff --check`, inspect preserved callback/tag and protected paths, compile `:app:compileDebugKotlin`, and run `:app:testDebugUnitTest`. Device, RTL, TalkBack, and real-upload evidence remain separately unverified. |
| Reversal | Revert `HomeScreen.kt` and this record; no schema, persisted value, or dependency changes are involved. |

## Checklist

- [x] Record visual evidence and functional translation.
- [x] Inspect current upload feature-card structure and preserved callback boundary.
- [x] Record protected TDLib/upload/localization/release behavior.
- [x] Implement the clipped static Aurora treatment.
- [x] Verify the primary action and test tag remain intact.
- [x] Compile and run unit tests.
- [x] Run static diff and protected-path review.
- [x] Update outcomes and limitations.
- [ ] Commit and push only if explicitly requested by the user.

## Outcomes and limitations

The full-card linear gradient has been replaced with a semantic surface base and four static, clipped radial layers: violet, cobalt, magenta, and a restrained highlight. The text hierarchy and the Lime `select_videos_button` continue to use the original string resources, callback, and test tag. No animated loop or progress treatment was added; motion design remains a separate phase as requested.

`./gradlew :app:compileDebugKotlin --no-configuration-cache --max-workers=1` and `./gradlew :app:testDebugUnitTest --no-configuration-cache --max-workers=1` completed successfully with JDK 17 and Android SDK API 36 supplied outside the repository. `git diff --check` passed. The focused review found no changes in TDLib, upload, WorkManager, native, Gradle, CI, signing, or release paths.

The final composition has not been inspected on a connected device/emulator, nor has TalkBack, Arabic RTL wrapping, large-font behavior, real Telegram login, or actual file delivery been exercised. These remain unverified and must not be inferred from the successful build.

## Verification status

| Layer | Status | Notes |
|---|---|---|
| Repository and visual audit | PASS | Reference notes, current card code, callback/tag boundary, and protected paths were inspected. |
| Kotlin compilation | PASS | `:app:compileDebugKotlin --no-configuration-cache --max-workers=1` completed successfully. |
| Unit tests | PASS | `:app:testDebugUnitTest --no-configuration-cache --max-workers=1` completed successfully. |
| Static diff/protected paths | PASS | `git diff --check` passed; no protected runtime/build/release path changed. |
| Device visual/RTL/TalkBack | NOT VERIFIED | No connected device or emulator inspection in this phase. |
| Real Telegram authentication/upload | NOT VERIFIED / OUT OF SCOPE | This visual-only change does not exercise credentials or delivery. |
