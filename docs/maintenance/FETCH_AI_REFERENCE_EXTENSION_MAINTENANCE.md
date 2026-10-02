# Fetch AI Reference Extension Maintenance Record

## Finding and safe translation

The Pinterest reference visibly combines a floating dark mobile UI with cobalt light ribbons and compact modular cards. The uploader already has a real Home upload focal card, a semantic cobalt primary role, and a liquid-glass surface helper. The smallest useful addition is a static, clipped cobalt ribbon behind the existing upload-card content. It provides the reference’s depth and directional energy without changing information hierarchy or inventing agent/task features.

## Scope and protected behavior

The implementation scope is `HomeScreen.kt` decoration only and a matching visual treatment in the illustrative preview. It retains `onSelectVideos`, all upload/connection state, progress semantics, callbacks, test tags, localized resources, navigation, TDLib, stable destinations, Room/DataStore, WorkManager, notifications, JNI, signing, and release configuration.

## Visual mapping

| Reference element | App element | Constraint |
|---|---|---|
| Blue ribbon behind the device | Low-opacity clipped curved ribbon behind the real upload hero. | Decorative only; never indicates progress or success. |
| Compact at-a-glance modules | Existing Home connection/stat cards. | Show only real account and upload counts. |
| Saturated media tiles | Existing genuine video thumbnails. | No fabricated thumbnails or task cards. |

## Risk, validation, and reversal

The ribbon must not obscure text or be mistaken for upload telemetry. It is rendered behind existing content at low opacity and has no animation. Validate with Kotlin compile/unit tests, diff hygiene, preview build, and visual screenshot; device checks for dark/light, Arabic RTL, large font, TalkBack, and real upload remain required. Reversal is a localized removal of the ribbon draw block and its preview CSS.

## Verification evidence

The constrained Android build completed successfully with JDK 17 and Android SDK `/home/ubuntu/android-sdk`:

```text
./gradlew :app:compileDebugKotlin :app:testDebugUnitTest --no-daemon --no-configuration-cache --max-workers=1
```

`git diff --check` completed without whitespace errors. The illustrative preview also passed `pnpm check` and `pnpm build`, followed by a full-page visual inspection. The reviewer’s repeated recommendation to make lime the primary signal was not adopted: it conflicts with the observed cobalt blue ribbons of this reference and the existing user-approved cobalt palette pass. The cobalt ribbon is intentionally localized to the Home upload card so the informational console remains quiet.

Real-device/emulator verification for transparent-surface contrast in dark/light mode, Arabic RTL, enlarged type, TalkBack, pointer hover, reduced-motion settings, and genuine upload flow remains **NOT VERIFIED**.
