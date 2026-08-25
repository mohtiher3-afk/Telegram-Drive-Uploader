# Real Upload Progress Motion Maintenance Record

## Repository finding

`UploadStatusIndicator` derives `progressFraction` and the accessibility `ProgressBarRangeInfo` directly from `UploadTask.progress`. It already applies a finite 220 ms state transition to the rendered value. No display-local progress state or artificial completion value exists.

## Motion design

| Layer | Source of truth | Visual behavior | Reduced-motion behavior |
|---|---|---|---|
| Filled bar length | `UploadTask.progress` only | Continues to interpolate to actual incoming values with the existing short token. | Snaps to the true value. |
| Signal bead | Only visible while actual status is `UPLOADING` and progress is below 100%. | A small low-opacity cobalt pulse sits at the current rendered progress fraction. It does not move the fill or imply a different percentage. | Not composed. |
| Accessibility range and percentage | Raw `UploadTask.progress` only. | Remains aligned with text percentage and `ProgressBarRangeInfo`. | Unchanged. |

## Scope and boundary

The implementation is limited to the reusable status indicator, central motion token, its unit test, and a clearly illustrative preview effect. It does not modify upload states, progress persistence, Worker behavior, TDLib, Room/DataStore, WorkManager, notifications, callbacks, test tags, or resource text.

## Risk, validation, and reversal

Risk is misleading users if a decorative pulse looks like a changing progress value. The bead is consequently conditional on a genuine active upload and is positioned from the already animated true fraction; it disappears at terminal/paused states. Validate compile/unit tests, `git diff --check`, preview build, screen-reader value, animation-scale-off, and real upload progression on a device. Reversal is limited to removing the bead overlay and the central pulse token; no persisted or runtime upload data changes.

## Implemented and verified

`AppMotion.uploadSignalPulse()` centralizes a 1,200 ms reverse opacity/radius cycle. `UploadStatusIndicator` composes the visual bead only for a real `UPLOADING` task below 100%, positions it from the rendered fraction, and preserves the raw fraction in `ProgressBarRangeInfo` and the text percentage. With Android motion disabled, the bead is not composed and the existing finite progress transition snaps.

The constrained Android check completed successfully with JDK 17 and Android SDK `/home/ubuntu/android-sdk`:

```text
./gradlew :app:compileDebugKotlin :app:testDebugUnitTest --no-daemon --no-configuration-cache --max-workers=1
```

`git diff --check` completed without whitespace errors. The preview passed `pnpm check` and `pnpm build`, and was visually inspected. Its generic lime-color suggestion was deliberately not adopted because the user-selected Pinterest palette and Glow UI reference point to a cobalt ambient signal; the animation is therefore cobalt and remains decorative. Device/emulator validation of TalkBack, system animation scale, Arabic RTL, enlarged text, and real upload events remains **NOT VERIFIED**.
