# Reference-Inspired Home UI Maintenance Record

## Scope and visual intent

This phase adapts the user-provided dark mobile reference into the functional home experience of Telegram Drive Uploader. It is an original Material 3 implementation inspired by the reference’s visual language: a near-black canvas, restrained violet and magenta ambient glow, a bright lime action/selection accent, large rounded feature surface, compact visual hierarchy, and a floating rounded navigation treatment.

The reference is a music application, so its greeting, discovery card, playlists, and media controls are translated into real uploader concepts: Telegram account readiness, the actual document picker entry point, local upload overview, and active upload status. No music-specific terminology, simulated actions, fake telemetry, or nonfunctional filters may be introduced.

## Protected behavior

| Area | Protected behavior |
|---|---|
| File picking | The existing `OpenMultipleDocuments` launcher and `onVideosSelected` callback remain the only upload-selection path. |
| Telegram account | The existing connection state, user fields, and `onConnectClick` callback remain authoritative. |
| Upload data | Existing counts, total size, active uploads, status projections, and `VideoItem`/`UploadStatusIndicator` stay bound to current ViewModel state. |
| Navigation | Existing routes, localized labels, test tags, navigation callbacks, compact navigation bar, and expanded navigation rail remain intact. |
| TDLib and work | Authentication, native TDLib, destinations, Room, WorkManager, upload progress, retries, persistence, dependencies, signing, CI, and release configuration are out of scope. |
| Inclusion | Arabic/RTL support, light/dark themes, semantics, touch targets, and reduced-motion handling are preserved. |

## Change record

| Field | Record |
|---|---|
| Finding | The current home screen is already Material 3 and data-bound, but its top app bar, connection card, feature card, metric grid, and full-width navigation bar do not yet express the hierarchy and compact floating-surface composition requested by the supplied reference. |
| Hypothesis | Restyling existing data-bound surfaces and callbacks into a greeting header, account-status chip, vivid upload feature card, compact overview, and floating compact navigation preserves functional semantics while making the visual hierarchy closely resemble the reference. |
| Scope | `HomeScreen.kt`, `AppNavigation.kt`, and this maintenance record. Existing Material 3 color tokens are reused rather than changing upload/business layers. |
| Risk | Medium visual/interaction-layout risk; no data or upload logic risk is intended. Overly dense layouts may affect text scaling or RTL, so existing resource strings, semantic labels, and minimum touch targets must be kept. |
| Validation | Inspect focused diff; compile debug Kotlin; run existing navigation and UI unit tests; inspect preserved test tags and callback paths statically. Device visual, RTL, TalkBack, and real-upload verification require later runtime evidence. |
| Reversal | Revert the focused commit or restore `HomeScreen.kt` and `AppNavigation.kt` to their previous versions; no migration or persisted state is involved. |

## Implementation checklist

- [x] Inspect the user-provided reference and map it to real uploader tasks.
- [x] Inspect home UI state, callback boundaries, theme tokens, navigation, and available test coverage.
- [x] Record protected TDLib, upload, localization, and navigation behavior.
- [x] Implement the home hierarchy and floating navigation visual treatment.
- [x] Confirm all existing interaction test tags and callbacks remain in the screen.
- [x] Compile the debug Kotlin target and execute focused regression tests.
- [x] Run static diff, protected-path, and secret checks.
- [x] Update outcomes and evidence limitations.
- [ ] Commit and push only if explicitly requested by the user.

## Outcomes and limitations

The redesign keeps the original view-model state collection, document picker, `onVideosSelected`, `onConnectClick`, top-level routes, localized labels, and test tags. The implementation changes only the rendering hierarchy: it introduces the reference-inspired greeting/header, compact account state, vivid upload feature card, overview cards, ambient background glow, and floating compact navigation surface.

`./gradlew :app:compileDebugKotlin --no-configuration-cache` and `./gradlew :app:testDebugUnitTest --no-configuration-cache --max-workers=1` both completed successfully with JDK 17 and Android SDK API 36 supplied outside the repository. The build displayed existing deprecation and Android SDK XML-version warnings; no warning suppression, dependency, or build configuration change was made. `git diff --check` passed, the static review found no protected-path changes, and all pre-existing interaction test tags remain in the redesigned files.

A connected Android runtime has not been used to inspect the final composition, Arabic RTL wrapping, large-font behavior, or TalkBack traversal. The design is build-verified, not device-verified. It also does not establish Telegram login or real upload delivery, which remain intentionally out of scope.

## Verification status

| Layer | Status | Notes |
|---|---|---|
| Repository/design audit | PASS | Source, theme, navigation, state ownership, callback boundaries, and available unit tests were inspected. |
| Kotlin compilation | PASS | `:app:compileDebugKotlin --no-configuration-cache` completed successfully. |
| Unit tests | PASS | `:app:testDebugUnitTest --no-configuration-cache --max-workers=1` completed successfully. |
| Static diff/protected paths | PASS | `git diff --check` passed; no TDLib, upload, WorkManager, native, Gradle, CI, signing, or release path changed. |
| Device visual/RTL/TalkBack | NOT VERIFIED | A real runtime inspection is not yet available. |
| Real Telegram authentication/upload | NOT VERIFIED / OUT OF SCOPE | This is a UI-only maintenance phase. |
