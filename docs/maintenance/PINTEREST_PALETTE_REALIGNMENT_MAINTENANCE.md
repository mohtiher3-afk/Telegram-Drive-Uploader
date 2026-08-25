# Pinterest Palette Realignment Maintenance Record

## Finding and source boundary

The supplied Pinterest pin was opened in the browser. Its visible artwork shows a near-black charcoal dashboard, dark blue-slate panels, restrained cobalt/periwinkle selection and graph accents, and small muted green status points. Its description identifies it as a dashboard UI by UI UX Bunker / @uixninja. The palette is recorded as a visual approximation in `docs/design/PINTEREST_LUCKY_SWAPS_PALETTE_REFERENCE.md`; no source token file or exact color values were available.

## Scope and protected behavior

The scope is limited to `core/ui/theme/Theme.kt`, the retained decorative `AuroraCobalt` primitive, and the Home Aurora composable where a disconnected connection currently derives a visual tint from `tertiary`. It does not alter any UI callback, ViewModel, route, test tag, resource string, TDLib state, destination ID, Room/DataStore record, WorkManager policy, upload state, notification policy, native artifact, signing rule, or release configuration.

## Semantic mapping

| Role | Previous visual temperature | New semantic direction |
|---|---|---|
| `primary` | Lime action and navigation selection | Light cobalt/periwinkle in dark theme; accessible royal blue in light theme. |
| `secondary` | Lavender preparation/accent | Cool steel-blue supporting accent. |
| `tertiary` | Pink accent | Muted teal reserved for confirmed completion/success surfaces. |
| `surface*` and outlines | Plum/eggplant glass | Deep blue-black and blue-slate glass hierarchy. |
| `AuroraCobalt` | Saturated indigo | Reference-aligned periwinkle cobalt decorative glow. |
| Disconnected Telegram visual | Previously inherited tertiary | Neutral on-surface variant; it must not resemble confirmed completion. |

## Risk, validation, and reversal

The color change can affect text contrast and perceived status meaning. Paired `on-*` roles remain explicit, status keeps icon/text cues, and light mode remains user-selectable. Compile debug Kotlin, run debug unit tests, inspect the focused diff and whitespace, then test dark/light, Arabic RTL, large text, TalkBack, and animation-scale settings on a device when available. Reversal is limited to reverting the two theme files and the small disconnected-color visual branch; runtime paths are not touched.

## Static verification

The proposed pairs were measured with the WCAG relative-luminance formula before implementation. The minimum measured proposed pair was dark `surfaceVariant` / `onSurfaceVariant` at **5.53:1**; primary, container, tertiary, and light-mode text pairs were all above **6.04:1**. These measurements support static text contrast decisions, but they do not replace visual checking of composited transparent surfaces on a physical device.

The following constrained local command completed successfully with JDK 17 and Android SDK `/home/ubuntu/android-sdk`:

```text
./gradlew :app:compileDebugKotlin :app:testDebugUnitTest --no-daemon --no-configuration-cache --max-workers=1
```

`git diff --check` also completed with no whitespace errors. Existing deprecation, SDK XML, and Kotlin annotation-target warnings were observed but not modified. Android device/emulator checks for dark/light contrast, Arabic RTL, large text, TalkBack, and real upload states are **NOT VERIFIED**.
