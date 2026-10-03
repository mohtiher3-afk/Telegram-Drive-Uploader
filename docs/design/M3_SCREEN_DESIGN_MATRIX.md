# Material 3 Screen Design Matrix

| Screen | App Bar | Navigation | Primary Action | Surface | Components | Motion | Adaptive | Status |
|---|---|---|---|---|---|---|---|---|
| Home | Existing screen app bar | App shell | Select/upload videos | Tonal sections | Button, status content, supporting statistics | Existing short transitions | Shared max width | Modernized shell |
| Destination | Small top app bar | Back | Confirm destination | Surface/list hierarchy | Search field, list row, selected state | Existing state transition | Shared max width | Preserved behavior |
| File Selection | Existing preparation structure | Back | Add to queue | List/grouped surfaces | File rows, metadata, remove actions | Existing extraction feedback | Shared max width | Preserved behavior |
| Upload Queue | Existing app bar | Queue destination | Retry/pause/resume/cancel per item | List/card hierarchy | Progress, status, actions | Existing progress behavior | Shared max width | Progress semantics preserved |
| History | Existing app bar | History destination | Search/filter/sort | List surface | Search, filters, history rows | Minimal | Shared max width | Data rules preserved |
| Scheduler | Existing upload preparation controls | Back | Schedule/add to queue | Form surface | Date/time controls already present | Minimal | Compact-first | Scheduling preserved |
| Settings | Existing top app bar | Settings destination | Change supported settings | Grouped sections | Radio, buttons, diagnostics | Minimal | Shared max width | Settings behavior preserved |
| Authentication | Existing top app bar | Back | Connect/continue | Form surface | Fields, buttons, snackbar, progress | Existing short transitions | Compact-first | Error mapping improved separately |
| Onboarding | Full-screen first-run | None | Continue/skip/permission action | Expressive page surface | Progress, cards, buttons | Existing onboarding motion | Compact-first | Permission flow preserved |

The first implementation slice updates the app shell, navigation labels, shared content width, and design-token contract. Screen-level business behavior remains unchanged.
