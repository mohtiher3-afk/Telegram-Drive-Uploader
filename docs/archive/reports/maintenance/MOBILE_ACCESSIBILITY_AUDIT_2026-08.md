# Mobile, Accessibility, RTL, and Motion Audit — August 2026

## Scope

This audit reviews the current Mission Control phone presentation against the real Compose state model. It does not certify visual appearance on a physical screen, TalkBack output, high-contrast rendering, Arabic layout, or animation-scale behavior without an observed device/emulator run.

## Repository-verified UI boundaries

| Area | Verified source behavior |
|---|---|
| Compact tokens | `AppSpacing` defines 16dp phone edges, 20dp section rhythm, 4dp navigation inset, and retains an independent 48dp touch target. |
| Navigation | The compact navigation bar retains the four existing routes and `nav_tab_*` test tags. Medium/expanded layouts retain the navigation rail. |
| Home action | The existing multiple-document picker and `onVideosSelected` callback remain the only upload-selection action; cards consume real `HomeViewModel` state. |
| Progress semantics | The progress fraction, visible percentage, and `ProgressBarRangeInfo` derive from the same persisted percentage; endpoint animation is limited to genuine `UPLOADING` below completion and respects `rememberSystemMotionEnabled()`. |
| Authentication UI | `TelegramAuthScreen` remains top-aligned, scrollable, IME-safe, state-driven, and preserves its connect/retry/QR callbacks and test tags. It does not hide an unconfigured-credential error. |
| Empty and error states | Shared surfaces are compact and use real title/supporting/action inputs rather than synthetic status. |
| RTL-ready controls | Navigation uses auto-mirrored back/send icons where direction matters; screen labels and state text come from resources. |

## Confirmed compact-layout refinements

The Queue bulk-control surface previously kept explanatory copy plus one or two action buttons in a single horizontal row. On narrow phone widths, the content could compete for the same horizontal measure. The copy now precedes a full-width action row, while the existing retry/pause callbacks and test tags are unchanged.

The History summary previously placed a variable-length summary and two sort buttons in one horizontal row. It now stacks the summary above right-aligned sort controls, retaining the same `setSort` actions and real aggregate values. These are presentation-only changes; they do not affect Room, WorkManager, TDLib, or history filtering.

## Device evidence still required

| Scenario | Status |
|---|---|
| Compact phone composition for Home, Queue, History, and Auth | Not verified. |
| Arabic RTL and English LTR at normal and enlarged fonts | Not verified. |
| TalkBack names, focus order, and control discoverability | Not verified. |
| Android animation scale disabled / reduced motion | Not verified. |
| Dark/light contrast as rendered by a device | Not verified. |
| Touch-target usability and keyboard/pointer fallback | Not verified. |

## Decision

The source audit supports the current state-bound UI behavior and identifies only the two objective narrow-row pressure points repaired here. Further visual redesign is deferred until the new Debug build is inspected on a phone or emulator; no dummy data, decorative upload animation, or duplicate navigation is added to compensate for missing device evidence.

## References

[1]: https://m3.material.io/foundations/accessible-design/overview "Material Design 3 accessibility"
[2]: https://developer.android.com/develop/ui/compose/animation/introduction "Jetpack Compose animation"
