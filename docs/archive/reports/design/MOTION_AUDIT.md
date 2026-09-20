# Material 3 Expressive Motion & Interaction Audit — Phase 52

**Repository:** [Telegram-Drive-Uploader](https://github.com/mohtiher3-afk/Telegram-Drive-Uploader)

**Scope:** Controlled maintenance of existing Jetpack Compose motion and interaction behavior.

**Date:** 2026-08-24

**Decision:** Audit in progress; no product, TDLib, upload, database, WorkManager, or navigation-route changes are permitted.

## 1. Motion principles used

The current Material 3 motion system introduces standard and expressive motion schemes built around springs. The official guidance states that the physics system is replacing the older duration/easing-based system, while standard motion is functional and expressive motion is intended for key interactions and hero moments [1]. Spatial springs are for movement, size, rotation, and shape; effects springs are for color and opacity, where overshoot is undesirable [1].

The app is a utility for authenticated Telegram uploads, so the default should remain restrained and functional. Expressive motion is appropriate only for a small number of comprehension-enhancing moments, such as onboarding page progression or a clear state transition. Upload progress itself must remain a direct projection of persisted TDLib progress; it must never be animated independently or delayed for visual effect.

## 2. Existing motion inventory

The inventory searched production Kotlin and tests for `AnimatedVisibility`, `AnimatedContent`, `animate*`, `Transition`, `Crossfade`, `graphicsLayer`, `InfiniteTransition`, repeatable animations, slide/fade/scale/offset APIs, animation-related effects, and list item animation APIs. Only two production animation sites were found, plus centralized motion tokens.

| Location | Animation | Trigger | Purpose | Duration/spec | Category | Risk | Status |
|---|---|---|---|---|---|---|---|
| `feature/onboarding/OnboardingScreen.kt:174-183` | `AnimatedContent` with horizontal slide and fade | User advances between onboarding pages | Preserves directional continuity between explanatory pages | Central `AppMotion.shortTween()` / `offsetShortTween()`, 220ms | NAVIGATION / CONTENT | Low; finite and user-triggered | Retain; add reduced-motion handling if supported by the project’s available Compose APIs |
| `feature/telegram/TelegramAuthScreen.kt:95-102` | `AnimatedContent` with fade in/out | Telegram connection state changes | Makes state replacement less abrupt while keeping the same centered content region | Central `AppMotion.shortTween()`, 220ms | STATE_CHANGE | Low; finite, no business delay | Retain; use a less expressive effect than onboarding because auth is utilitarian |
| `core/ui/theme/MotionTokens.kt:8-26` | Central tween/easing helpers | Used by the two transitions above | Prevents arbitrary per-screen durations | 160ms, 220ms, 280ms constants; only short helper currently used | SYSTEM | Low | Candidate for semantic naming and future spring migration, but no API/toolchain change is justified in this phase |
| `MainActivity.kt:26-45` | No custom splash or startup animation | Activity creation | Immediate theme and navigation composition | None | STARTUP | Low | Retain; no artificial startup delay exists |
| Upload queue/status UI | No animation API found in the inventory | Real TDLib status/progress updates | Keeps status and progress immediate and lightweight | None | PROGRESS | Low | Correct; do not add animation to rapid progress updates |
| Lazy lists | No item movement animation found | Queue/history changes | Avoids animating large or frequently updating lists | None | LIST | Low | Correct; do not add list-wide animation without reorder comprehension need |
| Infinite/background animation | No `InfiniteTransition`, `infiniteRepeatable`, or repeatable animation found | N/A | N/A | N/A | DECORATIVE | None | No unnecessary battery work found |

## 3. Motion classification and component review

| Area | Existing interaction | Material 3 / Compose assessment | Decision |
|---|---|---|---|
| Onboarding page change | Directional slide plus fade | Meaningful content replacement; the direction reinforces progression and the duration is centralized and short [2] | Retain as the primary expressive moment; do not add bounce or shape morphing to the whole screen |
| Authentication state change | Fade only | Appropriate for a utilitarian state replacement; avoids implying hierarchy or celebration during login | Retain; no spring overshoot |
| Upload progress | Direct indicator update | Progress is business data, not decorative motion. Artificial interpolation could misrepresent TDLib state | No change; preserve immediate persisted progress |
| Upload completion/failure | Status text/color/button changes without custom animation | Clear state communication is more important than celebration; success/failure must remain immediate | No celebratory animation added |
| Destination selection | Selectable row and pin icon button | Selection semantics are already explicit; broad animated surfaces could distract from destination identity and independent pin action | No custom animation added |
| Settings theme selection | Selectable rows and radio controls | Radio selected state communicates choice; no transition is required to understand a static preference change | No custom animation added |
| Filter chips | Material 3 selected state | Component handles interaction feedback and selected semantics | No custom animation added |
| Navigation | Adaptive navigation bar/rail without custom route transition | Stable navigation is appropriate for this utility and route structure is protected | No custom transition added |
| Error feedback | Direct error state/snackbar | Error should be fast, stable, and actionable; screen shake is not justified | No shake or delay added |
| Buttons and icon buttons | Material 3 components with default interaction indication | Built-in indication is preferable to hand-built press animation; it remains consistent with the component system | Retain defaults |
| Startup/splash | No custom animation | Startup is not delayed and no fake loading is shown | Retain |

## 4. Duration and performance findings

All custom finite motion currently uses the centralized 220ms token. The unused 160ms and 280ms constants are still part of the design-token surface but are not scattered through screens. There are no arbitrary 173ms/247ms/413ms values, no infinite decorative loops, and no list-wide item animations.

The upload queue is especially sensitive because TDLib progress can update frequently and multiple files may be visible. The current absence of `animate*AsState`, `animateContentSize`, or list movement animation in the queue avoids recomposition and frame work caused by continuously animating every update. The audit therefore rejects adding animated progress smoothing, animated list placement, or full-screen transitions to the upload path.

## 5. Accessibility and lifecycle findings

Material describes accessibility as a default design value, and motion must not be the only channel through which state is communicated [3]. In this app, onboarding and authentication retain visible text and controls independent of motion. The upload indicator retains localized status text, percentage semantics, speed, and ETA independent of any animation.

No `InfiniteTransition` or animation coroutine was found that could continue while a screen is backgrounded or destroyed. The two finite transitions are scoped to the composition and are not tied to upload workers or TDLib callbacks. Runtime verification of TalkBack, large font scale, reduced-motion system settings, background/foreground interruption, and low-end frame pacing still requires a real device or emulator.

## 6. Planned safe correction boundary

The audit does not justify adding expressive physics, custom press animations, animated queue movement, fake progress interpolation, or navigation transitions. If the available Compose Material 3 version exposes a stable motion scheme API without dependency or toolchain changes, it may be evaluated separately for the onboarding hero transition only. Otherwise, the existing centralized short tween remains the safer compatible choice.

Any implementation change in this phase must be limited to semantic motion tokens, reduced-motion behavior supported by the project’s current dependencies, testable transition contracts, or documentation. It must not change TDLib state timing, WorkManager execution, upload completion, destination IDs, authentication callbacks, or route strings.

## References

[1]: https://m3.material.io/styles/motion/overview "Material 3 Motion physics system"
[2]: https://developer.android.com/develop/ui/compose/animation/introduction "Android Developers — Animations in Compose"
[3]: https://m3.material.io/foundations/accessible-design/overview "Material 3 Accessible design"
[4]: https://m3.material.io/blog/building-with-m3-expressive "Material 3 — Start building with Material 3 Expressive"

## 7. Official evidence collected

| Source | Finding applied to this audit |
|---|---|
| [Material 3 Motion physics system](https://m3.material.io/styles/motion/overview) | M3 Expressive introduces expressive and standard motion schemes backed by springs. Spatial springs suit movement/size/shape; effects springs suit color/opacity. Most app motion should share a scheme, and expressive motion is most appropriate for hero moments and key interactions. |
| [Start building with Material 3 Expressive](https://m3.material.io/blog/building-with-m3-expressive) | Expressive motion is one tactic among hierarchy, shape, color, typography, containment, and component flexibility. Hero moments should be brief and limited rather than applied everywhere. |
| [Android Developers — Animations in Compose](https://developer.android.com/develop/ui/compose/animation/introduction) | `AnimatedContent` is intended for content replacement; `AnimatedVisibility` for showing/hiding content; `Transition` coordinates multiple properties; animation APIs should be selected by the state/value being communicated. |
| [Android Developers — Customize animations](https://developer.android.com/develop/ui/compose/animation/customize) | Springs handle interruptions smoothly; duration-based tweens remain valid when a deliberate fixed timing is needed. The upload path must not use animation to invent or delay real progress. |
| [Android Developers — Accessibility in Jetpack Compose](https://developer.android.com/develop/ui/compose/accessibility) | Compose provides semantics and testing tools; accessible state communication must remain available independently of motion. |
| [Android accessibility principles](https://developer.android.com/guide/topics/ui/accessibility/principles) | Interactive and meaningful elements need useful labels, custom components should use semantics, and users should receive cues other than color. |

## 8. Implemented correction

The onboarding transition now uses the centralized short spatial spring with `Spring.DampingRatioNoBouncy` and `Spring.StiffnessMedium`. This applies the official spring model to a meaningful content transition without adding bounce or altering page progression. Authentication continues to use the centralized short effects tween because its state changes are utilitarian and should not imply a hero moment.

`rememberSystemMotionEnabled()` reads Android’s `ValueAnimator.areAnimatorsEnabled()` setting once per composition. When system animations are disabled, both finite motion helpers return `snap()` specs, so onboarding and authentication state changes remain immediate while all visible content and action semantics remain intact. This is a motion-accessibility safeguard, not a product setting and not a change to business timing.

No motion was added to TDLib progress, upload completion, WorkManager state, queue item placement, history updates, destination IDs, authentication callbacks, or navigation routes. No infinite animation or background animation exists.

The audit is now **implemented and statically verified**. The repository motion safety scan, protected-boundary scan, and whitespace check passed. The Android Gradle test task was attempted but stopped before compilation because the sandbox has no valid Android SDK path (`ANDROID_HOME`, `ANDROID_SDK_ROOT`, and `local.properties` are absent). Runtime reduced-motion behavior, frame pacing, TalkBack announcements during state changes, and background/foreground interruption still require device or emulator evidence.
