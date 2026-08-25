# Full Theme and Motion Unification Maintenance Record

## Target, task, and scope

The target is a Compose Android uploader used on compact and expanded devices. Its primary task remains selecting videos, selecting a genuine Telegram destination, and following real queue state. The scope covers splash, onboarding, home, upload preparation, Telegram authorization, destination selection, queue, history, and settings visual presentation. It excludes TDLib, upload state ownership, Room/DataStore schema, WorkManager, authentication semantics, destination identity, notification authority, release configuration, and native artifacts.

## Reference translation

The Pinterest source contributes a dark cockpit hierarchy: one coherent background field, low-energy translucent surfaces, compact factual metrics, a single blue-violet contour, and an illuminated genuine primary action. It does not contribute casino, wallet, social-stream, score, balance, or reward concepts. The complete visual mapping is recorded in `docs/design/PINTEREST_LUCKY_SWAPS_MOTION_REFERENCE.md`.

## System design

| Layer | Implementation | Purpose |
|---|---|---|
| Semantic dark color scheme | Deep neutral background with translucent `surface`, `surfaceContainer`, and `surfaceContainerHigh`; existing lime primary and semantic error roles remain. | Lets all existing M3 cards and scaffolds read as one glass hierarchy without changing light theme. |
| Shared page field | A `MissionControlPage` wrapper supplies a low-opacity, clipped Aurora contour beneath every NavHost destination. | Provides common spatial identity rather than duplicating gradients in every screen. |
| Page entrance | 220–280 ms fade plus restrained vertical spatial entry, derived from `AppMotion` and disabled by Android animator settings. | Orients route changes without delaying interaction or fabricating progress. |
| Existing local motion | Onboarding pages, auth states, connection status, Aurora hero, notification state, and pointer hover retain their local semantic motion. | Keeps cause-and-effect aligned with real state. |
| Navigation | Existing bottom bar/rail routes remain unchanged and inherit translucent M3 surfaces. | Preserves adaptive navigation and labels. |

## Accessibility and behavior boundaries

Color is not a status signal; cards retain text/icons and semantic M3 roles. Pages keep Arabic RTL and localized strings. Motion is finite and snaps when system animation is disabled. The implementation adds no callbacks, no nonfunctional buttons, no fake progress, no fake completions, and no animation on upload telemetry or large queue lists.

## Applied implementation

- `Theme.kt` makes dark semantic surface roles and secondary container roles translucent while preserving the existing opaque, contrast-oriented light scheme and real primary CTA.
- `MissionControlPage.kt` applies the shared clipped Aurora field and a one-time per-route 280 ms fade/vertical entry. `AppNavigation.kt` passes the active route key while retaining the original destinations, padding, rail/bar, back-stack, and navigation callbacks.
- `VideoItem`, `EmptyState`, and `ErrorState` now use a shared outlined glass surface. This carries through real prepared-file, queue, history, empty, and failure content without altering any model, test tag, or click action.
- `QueueScreen`, `HistoryScreen`, and `SettingsSection` receive a local factual emphasis card and consistent contour treatment. Upload preparation, Telegram authorization, destination selection, onboarding, splash, and home receive the shared page field plus dark semantic surface treatment through their existing M3 components.

## Static verification

The constrained local check completed successfully with JDK 17, Android SDK at `/home/ubuntu/android-sdk`, one Gradle worker, and a 1 GiB heap:

```text
./gradlew :app:compileDebugKotlin :app:testDebugUnitTest --no-daemon --no-configuration-cache --max-workers=1
```

`git diff --check` also completed with no whitespace errors. Existing non-blocking deprecation, SDK XML, and Kotlin annotation-target warnings were observed and deliberately left unchanged.

## Validation and reversal

Compile debug Kotlin, run unit tests, inspect static diff and protected paths, then use the interactive preview only as an illustrative motion-direction check. Device checks remain required for RTL, large text, TalkBack, motion-scale, pointer/keyboard, light/dark contrast, and real upload behavior. Reversal is limited to the shared wrapper, theme alpha values, navigation wrapper calls, shared presentational components, and this record; no data or runtime engine changes are included.
