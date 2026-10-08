# UI previews

Generated evidence for the launcher icon and the real, running UI. Nothing here is a mockup:
every screen image was captured from the app running on an emulator (`tdu_x86_64`, API 34).

## Launcher icon

| File | What it is |
| --- | --- |
| `icon-preview.png` | Icon sheet: adaptive icon at 192 px as a rounded square and as a circle, a size ladder (96 → 24 px), and a home-screen strip. |

Sources of truth live in the app module:

- `app/src/main/res/drawable/ic_launcher_background.xml` — Obsidian (`#0F172A`) adaptive backdrop.
- `app/src/main/res/drawable/ic_launcher_foreground.xml` — the mark: an upload arrow rising out of a
  drive plate, with a connected status dot.
- `app/src/main/res/drawable/ic_launcher_monochrome.xml` — themed-icon variant (single colour).
- `app/src/main/res/mipmap-*/ic_launcher.webp` and `ic_launcher_round.webp` — legacy raster icons for
  launchers that predate adaptive icons, rendered from the same geometry.

Palette: Obsidian `#0F172A`, plate `#1E293B`, rail `#475569`, cyan signal `#22D3EE`,
connected green `#22C55E`.

## Screens

| File | Screen |
| --- | --- |
| `screens/home-dark.png` | Home, dark theme — Mission Control header, overview tiles, primary actions. |
| `screens/home-light.png` | Home, light theme. |
| `screens/queue-dark.png` | Upload queue, dark theme (empty state + filters). |
| `screens/queue-light.png` | Upload queue, light theme. |
| `screens/history-dark.png` | Upload history, dark theme (empty state + time filters). |
| `screens/settings-dark.png` | Settings — appearance, glow colour presets. |
| `screens/settings-more.png` | Settings, scrolled to the later sections. |
| `screens-overview.png` | Contact sheet of all of the above. |

## Capturing new screenshots

`MainActivity` sets `FLAG_SECURE`, so `adb screencap` returns a black frame on a stock build. To
recapture, build with the flag disabled for debug only, install, then screenshot:

```kotlin
// app/src/main/java/com/telegramdrive/uploader/MainActivity.kt — temporary, debug only
if (!BuildConfig.DEBUG) {
    window.setFlags(FLAG_SECURE, FLAG_SECURE)
}
```

```bash
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n <applicationId>/com.telegramdrive.uploader.MainActivity
adb exec-out screencap -p > home.png
```

Revert the `FLAG_SECURE` guard before committing — the shipped build must keep the flag on.
