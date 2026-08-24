# Current UI Audit

| Screen/Component | Current Implementation | Material 3 Status | Problem or Risk | Recommended Direction |
|---|---|---|---|---|
| App shell | `Scaffold` with `NavigationBar` on compact screens and `NavigationRail` at 600dp+ | Uses M3 adaptive components | Width decision is a single threshold and content has no shared max-width token | Keep routes and adaptive behavior, centralize width and spacing decisions |
| Theme | Light/dark `ColorScheme`, dynamic color on Android 12+, expressive `Shapes`, shared typography | Strong M3 foundation | Theme values and spacing are not documented as a shared design contract | Document and centralize tokens before screen-specific changes |
| Home | Compose screen with upload action, connection state, and local statistics | Uses M3 surfaces and buttons | Primary task hierarchy should be checked against secondary statistics | Emphasize upload as the dominant action and keep statistics supportive |
| Upload preparation | File list, metadata, destination, scheduling, add-to-queue action | Uses M3 cards, buttons, and fields | Multiple controls can compete for attention on compact widths | Preserve content but use a clearer section hierarchy and max-width |
| Destination selector | Search field, destination list, selected destination, pin actions | Uses M3 search/field/list patterns | Search and selection need large-screen width discipline and RTL runtime testing | Keep stable IDs and refine list hierarchy only |
| Queue | Upload items with status, progress, speed, ETA, and actions | Uses M3 list/card/progress patterns | Progress and state density need scanability; action hierarchy must remain clear | Use semantic status presentation and preserve honest progress |
| History | Completed upload records, search/filter/sort, deletion | Uses M3 list/card patterns | Success-only projection is a product rule, not a visual gap | Improve hierarchy without changing filtering or persistence |
| Settings | Appearance, Telegram connection, diagnostics and app information | Uses M3 cards, radios, buttons, and text | Some diagnostic content is dense and uses fixed-height log surface | Preserve diagnostic controls; consider width and typography tokens |
| Onboarding | First-run pages, animation, permission launcher, skip/continue | M3 Expressive-oriented | Requires runtime checks for TalkBack, text scaling, RTL, and reduced motion | Preserve actual permission flow and refine only if evidence supports it |
| Authentication | State-driven Telegram login with localized errors and recovery | Uses M3 fields, buttons, snackbar, and progress | Runtime state and error accessibility need device validation | Do not change TDLib/auth behavior during UI work |
| App bars/navigation | TopAppBar plus bottom navigation or rail | M3 components | Navigation labels are currently model strings and need locale/runtime review | Keep routes, use semantic labels, and avoid duplicate navigation systems |

## Audit Boundary

The current repository already has a Material 3 foundation. This is not an M2 migration. The safe modernization target is consistency, hierarchy, adaptive width, and accessibility—not wholesale replacement of working components.
