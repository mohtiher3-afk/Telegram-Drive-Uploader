# Upload and Connection State Motion Maintenance Record

## Motion intent

Add short, state-driven transitions to the upload indicator and Telegram connection card. These transitions clarify a real data update; they are not decorative loops and must not suggest network activity, upload completion, or authorization that TDLib has not reported.

| Surface | Transition | Trigger | Reduced-motion behavior |
|---|---|---|---|
| Upload status label and bar tint | Short crossfade/color transition, while the existing progress fraction continues its short interpolation. | `UploadTask.status` or actual `progress` update. | Immediate snap; existing progress semantics remain unchanged. |
| Connection card | Short color and content-size transition; avatar/icon crossfade only as authorization data changes. | `TelegramConnectionState` and observed user data. | Immediate snap; no infinite or celebratory effect. |

## Protected behavior

| Area | Protected behavior |
|---|---|
| Upload truth | `UploadTask.progress`, `UploadTask.status`, `uploadProgressFraction`, accessibility range, and terminal completion policy remain authoritative. |
| Telegram truth | TDLib authorization state and current-user flow remain authoritative. No client, credential, destination, or session behavior changes. |
| Actions | Pause, resume, retry, cancel, file selection, and connect callbacks remain unchanged. |
| Accessibility | Semantic labels, progress information, non-color state text, localized resources, touch targets, RTL, and reduced-motion setting remain preserved. |
| Protected runtime/build | Room, DataStore, WorkManager, upload engine, TDLib/native artifacts, dependencies, Gradle, CI, signing, and release configuration remain out of scope. |

## Change record

| Field | Record |
|---|---|
| Finding | Progress fraction already interpolates safely, but status text/tint and connection-surface color/content change abruptly when state changes. |
| Smallest safe change | Add short `animateColorAsState`, `Crossfade`, and content-size transitions at the rendering boundary; retain data values, callbacks, and semantic descriptions. |
| Risk | Low visual and composition risk. Repeated progress updates must not create extra pulse/loop effects, and connection motion must not imply authorization before actual state changes. |
| Validation | Compile Kotlin; run unit tests; inspect actual callback/data boundaries, reduced-motion snap path, static diff, and protected paths. Device, TalkBack, RTL, and real auth/upload evidence remain separately unverified. |
| Reversal | Revert `UploadStatusIndicator.kt`, `HomeScreen.kt`, and this record. No persisted or network behavior changes. |

## Checklist

- [x] Audit state ownership and existing progress interpolation.
- [x] Define short state-transition rules and reduced-motion behavior.
- [x] Record protected upload, Telegram, accessibility, and release boundaries.
- [x] Implement status and connection transitions at the rendering boundary.
- [x] Verify action callbacks and semantics remain intact.
- [x] Compile and run unit tests.
- [x] Run static/protected-path review.
- [x] Update outcomes and limitations.
- [ ] Commit and push only if explicitly requested by the user.

## Outcomes and limitations

The upload indicator now crossfades its localized status label and animates only the rendering colors of the status text, progress bar, and supporting surface. Its progress fraction continues to be derived from the original `UploadTask.progress`; its accessibility range and description remain based on the unanimated real value. The component has no new loop, simulated progress, or terminal-state behavior.

The connection card now transitions its container/status/content colors, crossfades only the observed avatar-or-icon and connection label, and animates size changes when the real authorization state changes. The existing `onConnectClick` callback remains unchanged and appears only when the actual state is not authorized.

`./gradlew :app:compileDebugKotlin --no-configuration-cache --max-workers=1` and `./gradlew :app:testDebugUnitTest --no-configuration-cache --max-workers=1` completed successfully with JDK 17 and Android SDK API 36 outside the repository. `git diff --check` passed. The static review confirmed original progress semantics and connection callback boundaries, with no changes in TDLib, upload, WorkManager, native, Gradle, CI, signing, or release paths.

No device/emulator runtime has been used to observe the exact transition tempo, reduced-motion behavior, TalkBack, Arabic RTL, or large-font layout. The change is build- and unit-test-verified, not device-verified. Real Telegram authorization and file delivery remain out of scope.

## Verification status

| Layer | Status | Notes |
|---|---|---|
| Repository/state audit | PASS | Upload status and connection boundaries, semantics, callback, and protected paths were inspected. |
| Kotlin compilation | PASS | `:app:compileDebugKotlin --no-configuration-cache --max-workers=1` completed successfully. |
| Unit tests | PASS | `:app:testDebugUnitTest --no-configuration-cache --max-workers=1` completed successfully. |
| Static diff/protected paths | PASS | `git diff --check` passed; no protected runtime/build/release path changed. |
| Device visual/reduced-motion/RTL/TalkBack | NOT VERIFIED | No connected device/emulator check in this phase. |
| Real Telegram authentication/upload | NOT VERIFIED / OUT OF SCOPE | State transitions do not exercise real delivery. |
