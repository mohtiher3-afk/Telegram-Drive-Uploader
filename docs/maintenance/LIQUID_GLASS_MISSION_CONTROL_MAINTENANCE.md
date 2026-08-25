# Liquid Glass Mission Control Maintenance Record

## Finding and product translation

The user requested a liquid-glass design for the existing Telegram Drive Uploader. The design is translated as a **presentation-only surface system**: deep blue-black background, translucent Material 3 containers, a clipped cool reflection from the upper edge, a low-energy cobalt/teal bloom, and a fine glass contour. The genuine focal moment remains the existing Home upload card and its real `onSelectVideos` callback.

## Scope and protected behavior

The scope includes a reusable Compose draw modifier and glass treatment for the Home connection/upload/stat cards plus shared presentational cards used by settings, video items, empty states, and error states. It excludes routes, callbacks, test tags, ViewModels, upload state, progress semantics, TDLib, Room/DataStore, WorkManager, notification policy, authentication, destination IDs, permissions, JNI, dependencies, signing, and release configuration.

## Design system

| Layer | Dark mode behavior | Light mode behavior | Accessibility boundary |
|---|---|---|---|
| Base surface | Existing blue-black transparent M3 surface containers. | Existing opaque M3 surface containers. | Text keeps paired `onSurface` and `onSurfaceVariant` roles. |
| Liquid reflection | A clipped, static diagonal cool-white sheen and small cobalt/teal bloom drawn above the container. | Disabled to avoid low-contrast frosted white effects. | Decoration is non-semantic and never the only state cue. |
| Contour | Existing M3 outline, optionally brightened on pointer hover by the current helper. | Existing M3 outline. | No new focus/click semantics are added. |
| Motion | Existing hover lift and Aurora breathing remain gated by Android animator settings. | No extra motion. | The liquid layer itself is static and safe with reduced motion. |

## Risks, validation, and reversal

The main risks are reducing text contrast through a reflection overlay or adding expensive blur. The implementation avoids platform blur/RenderEffect and uses clipped static drawing only on bounded card surfaces. Compile debug Kotlin, run debug unit tests, inspect focused diffs and `git diff --check`, then inspect dark/light, Arabic RTL, enlarged text, TalkBack, motion-scale-off, pointer hover, and live upload status on a device. Reversal is limited to removing the reusable modifier from visual card modifiers and deleting the helper; no runtime behavior is touched.

## Implemented surface slice

`LiquidGlassSurface.kt` adds a dark-theme-only clipped reflection and small low-opacity cobalt bloom. The helper is used by the Home connection, upload, and statistic cards; shared video, empty, error, and settings cards also inherit it. The existing liquid behavior uses only static drawing, while the prior pointer hover and Aurora breathing continue to observe Android’s animation-scale setting. Light theme retains the opaque M3 surface fallback.

## Verification evidence

The Android command below completed successfully with JDK 17, Android SDK `/home/ubuntu/android-sdk`, one Gradle worker, and the constrained JVM heap:

```text
./gradlew :app:compileDebugKotlin :app:testDebugUnitTest --no-daemon --no-configuration-cache --max-workers=1
```

`git diff --check` completed with no whitespace errors. The interactive preview also passed `pnpm check` and `pnpm build`, then received a full-page visual inspection. Its reviewer recommended restoring a lime primary; that recommendation was deliberately not applied because it conflicts with the current user-requested Pinterest cobalt palette realignment. The preview now demonstrates liquid-glass reflections while retaining cobalt as the primary action role and teal for completion.

Device/emulator checks for dark/light compositing, Arabic RTL, enlarged text, TalkBack, motion-scale-off, hover, and real transfer screens are **NOT VERIFIED**.
