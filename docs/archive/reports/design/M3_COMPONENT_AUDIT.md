# Official Material 3 Component Audit — Phase 51

**Repository:** [Telegram-Drive-Uploader](https://github.com/mohtiher3-afk/Telegram-Drive-Uploader)

**Scope:** Controlled maintenance of the existing Jetpack Compose UI.

**Date:** 2026-08-24

**Decision status:** **Conditionally verified; device validation remains NO-GO.**

## 1. Audit frame

The application is a private Telegram file-upload client whose critical UI tasks are connecting an account, selecting an authorized destination, preparing videos, adding work to the local upload queue, monitoring status, and reviewing history. The target platform is Android with Jetpack Compose and Material 3, across compact phone layouts and medium/expanded tablet or foldable widths. English and Arabic are supported, including RTL rendering. The audit deliberately excludes TDLib, JNI, ABI, upload, database, WorkManager, scheduler, and navigation-route changes.

The review follows the current official Material 3 component guidance. Material 3 treats components as semantic interaction patterns rather than decorative controls: buttons should match action importance, cards should group related content, lists should support scanning and action, search should expose query/results behavior, and progress should represent the real process state [1] [2] [3] [4] [5]. Accessibility remains a release criterion rather than a visual afterthought [6].

## 2. Actual component inventory

The inventory was created from production Kotlin sources under `app/src/main/java`; unused components were not added to the audit as if they existed.

| Component family | Actual APIs found | Screens or shared components | Audit disposition |
|---|---|---|---|
| Scaffold | `Scaffold` | App shell and all major screens | Retained. Outer shell intentionally uses zero content insets because child screens own their top app bars; bottom navigation padding is passed once to the `NavHost`. |
| Top app bars | `TopAppBar` | Home, queue, history, settings, upload preparation, Telegram auth, destination picker | Retained as small app bars. Page titles are concise; navigation and action icons have labels. Queue’s supporting summary is documented as a compact title-slot extension, not a large-app-bar substitution. |
| Adaptive navigation | `NavigationBar`, `NavigationRail`, `NavigationBarItem`, `NavigationRailItem` | App shell | Retained. Four stable destinations meet the 3–5 navigation-bar guidance and the rail is shown at 600dp and above. Routes and state restoration are unchanged. |
| Buttons | `Button`, `FilledTonalButton`, `OutlinedButton`, `TextButton` | All feature screens, onboarding, shared empty/error/status components | Retained and classified by intent. Primary continuation/confirmation/queue actions use filled buttons; secondary actions use tonal/outlined/text buttons; destructive actions use error roles. |
| Icon buttons | `IconButton` | App bars, password visibility, search clear, pin, selection clear, remove, history clear | Retained. Action icons have localized content descriptions; decorative icons use `null`. Default Material touch targets are preserved. |
| Cards | `Card`, `ElevatedCard` | Home, upload, history, settings, auth, destination picker, video item | Retained where grouping or emphasis is meaningful. History’s unnecessary outer card around an already-contained `VideoItem` was removed. |
| Filter chips | `FilterChip` | Queue and history filters | Correct for mutually selectable content filters. No decorative chip badges were introduced. |
| Text fields | `OutlinedTextField` | Telegram authentication, destination search, history search | Correct for long forms and embedded filtering contexts. SearchBar was not introduced because the existing flows do not expose a suggestion/result view or a global search entry point. |
| Dialogs | `AlertDialog` | Telegram logout confirmation | Correct for a high-risk, single-task confirmation. Dialog copy is now localized. |
| Snackbars | `Snackbar` | Telegram authentication errors | Single inline persistent error snackbar is retained because the current state model exposes one error and an explicit dismiss action. A SnackbarHost queue was not introduced because that would change error timing and behavior without a product requirement. |
| Progress indicators | `LinearProgressIndicator`, `CircularProgressIndicator` | Upload status, metadata extraction, authentication | Determinate linear progress now exposes an accessibility range and localized percentage. Circular indicators remain indeterminate for unknown-duration work. |
| Choice controls | `RadioButton`, `Switch` | Settings theme selection, diagnostics, disabled upload placeholders | Theme rows now expose single-choice radio semantics. The diagnostics switch is an actual user-controlled switch. Disabled upload switches remain visibly disabled placeholders and are not presented as new functionality. |
| Other requested families | FABs, extended FABs, button groups, split buttons, `ListItem`, assist/input/suggestion chips, `SearchBar`, sheets, checkbox, slider, pickers, tooltips, pagers | None found as production Compose APIs | Not applicable. No replacement or new component was added merely because the API exists. Platform date/time dialogs remain part of the existing schedule flow. |

## 3. Component matrix

Priority uses the requested scale: **P0** broken behavior, **P1** major UX/design issue, **P2** consistency or semantics issue, and **P3** cosmetic issue.

| Component | Screen | Current API | Official pattern | Correct? | Issue | Priority |
|---|---|---|---|---|---|---|
| Scaffold | App shell | Outer `Scaffold` with `contentWindowInsets = WindowInsets(0, 0, 0, 0)` and conditional bottom bar | Insets should be consumed once and content padding passed to content | Yes, by design | Child screens own their own top bars; the outer shell supplies only navigation padding. | — |
| Top app bar | Feature screens | `TopAppBar` with localized title and back/action icons | Use the smallest title bar that matches hierarchy; provide navigation and actions | Yes | No large title or scroll behavior is required by the current screens. | — |
| Navigation bar | Compact shell | `NavigationBar` with four stable destinations | Compact/medium navigation bars support 3–5 stable destinations | Yes | Expressive flexible-bar options exist, but adoption is not required to preserve current architecture. | P3 |
| Navigation rail | Medium/expanded shell | `NavigationRail` with four stable destinations | Rails support medium and expanded widths and 3–7 destinations | Yes | Width breakpoint is implemented at 600dp; runtime tablet/foldable validation is still pending. | P2 |
| Buttons | Home/upload/auth | Filled and filled-tonal buttons | Use one primary action and lower-emphasis alternatives for secondary actions | Yes | The selection, continuation, destination confirmation, and queue actions have clear roles. | — |
| Buttons | Queue/settings | Peer filled buttons for retry/pause and error-role actions | Do not make every action filled; destructive actions need clear treatment | Mostly | Bulk retry and pause are simultaneous controls over different subsets. They remain explicit rather than being combined into an invented group. | P2 |
| Icon buttons | All screens | `IconButton` | Provide semantic labels, enabled/selected states, and adequate touch targets | Yes | Localized labels and default targets are present. | — |
| Cards | History | Outer `Card` around `VideoItem` and status surface | Use cards for meaningful grouping and avoid unnecessary nested containment | Corrected | The outer history card duplicated containment around a child card and was removed. | P2 |
| Cards | Destination picker | Card rows with a selected surface and independent pin action | Cards may contain related content and actions; lists are another valid pattern | Yes, justified | A custom row is retained because it combines selection, destination identity, and a separate pin action. | — |
| Lists | Destination picker | Custom card row with `selectable` semantics | Lists should be scannable, consistent, and support selection where applicable | Yes, after correction | Each destination’s main region now exposes `Role.RadioButton`; the pin control is outside that region. | P2 |
| Chips | Queue/history | `FilterChip` | Filter chips filter content and communicate selected state | Yes | No action or decorative semantics are being conflated with filtering. | — |
| Text fields | Auth | `OutlinedTextField` for phone, code, and password | Outlined fields are appropriate for longer forms and should expose labels, input type, and state | Yes | Labels, keyboard configuration, single-line state, and password visibility semantics are present. | — |
| Search | Destination/history | Outlined text field with leading search and trailing clear action where needed | Search may use a bar when it has query/results behavior and a search entry point | Yes, justified | These are embedded list filters, not global suggestion/search views; behavior and logic are preserved. | — |
| Dialog | Settings | `AlertDialog` for logout | Dialogs should handle important prompts and one task | Yes, corrected | Title, message, confirm, and cancel are localized. | — |
| Snackbar | Auth | Direct `Snackbar` in bottom-aligned content | Snackbars provide brief, non-interruptive updates and may remain until action | Acceptable with limitation | It is a single state-bound error surface, not a queued notification system. Device accessibility testing remains pending. | P2 |
| Progress | Upload status | Determinate `LinearProgressIndicator` | Progress must represent actual progress and expose usable contrast/semantics | Corrected | Localized status text and `ProgressBarRangeInfo` now expose the persisted percentage to assistive technology. | P1 |
| Progress | Auth/loading | Indeterminate `CircularProgressIndicator` | Use indeterminate progress when duration or fraction is unknown | Yes | The UI does not invent percentages for auth or metadata extraction. | — |
| Choice control | Settings themes | `RadioButton` plus row `selectable` | A single-choice set should expose selected state and a radio role | Corrected | Row-level selection semantics were added; the nested radio control no longer duplicates click handling. | P1 |
| Switch | Settings diagnostics | Enabled `Switch` | Switches represent persistent on/off settings | Yes | It controls visibility of sanitized diagnostics. | — |
| Switch | Settings upload placeholders | Disabled switches | Disabled controls must be visibly disabled and must not imply available behavior | Yes, documented | These controls remain placeholders and do not alter upload behavior. | P2 |

## 4. Corrections applied

### 4.1 Localized settings and error surfaces

The settings screen previously contained visible English literals for section titles, theme labels, cache actions, Telegram status, diagnostics, toasts, and log states. They are now resource-backed in both English and Arabic. Theme storage keys remain the original `System`, `Light`, and `Dark` values, so the settings behavior and persistence contract are unchanged. The About screen reads `BuildConfig.VERSION_NAME`, preventing the displayed version from becoming stale.

The reusable error state now uses localized title and icon semantics. This correction is purely presentational and does not modify error mapping or retry behavior.

### 4.2 Upload progress semantics

`UploadStatusIndicator` now maps every `UploadStatus` value to a localized resource and no longer exposes raw enum names as user-facing copy. The determinate linear indicator reports the clamped persisted progress through `ProgressBarRangeInfo` and provides a localized accessibility description such as “Upload progress: 42 percent.” The existing visual speed and ETA text remain unchanged.

### 4.3 Single-choice destination semantics

The destination row keeps its custom layout because it includes a pin action independent from destination selection. The row’s main region now uses `Modifier.selectable(..., role = Role.RadioButton)`, while pin remains a separate `IconButton`. The previous broad clickable card was removed to avoid nested or ambiguous action semantics; stable destination IDs and selection callbacks are unchanged.

### 4.4 Removed redundant history containment

History items no longer wrap the already-contained `VideoItem` in an additional outer card. Upload duration and status remain present in the same list item, but the hierarchy is less fragmented and better matches the M3 guidance to use containment only when it clarifies relationships.

### 4.5 Removed duplicate destination affordance during preparation

The upload preparation destination surface remains a grouped card with an explicit text action. The redundant `Card(onClick = ...)` behavior was removed, leaving one clear action affordance and preventing duplicate activation semantics. The callback and destination route are unchanged.

## 5. State and accessibility review

The audited screens now provide distinct visual and textual states for disconnected, connecting, phone entry, verification code, QR login, two-step password, authenticated, and error authentication states. Upload preparation distinguishes empty, metadata-loading, error, and ready states. Queue and history distinguish empty, filtered-empty, active, paused, failed, and completed content through labels, filters, status copy, and actions rather than color alone.

Interactive icons use localized content descriptions where an action is present, and decorative icons use null descriptions so they do not create redundant announcements. Theme selection has row-level radio semantics. Destination selection has single-choice semantics with the pin action isolated. Determinate upload progress exposes a range and localized description. Default Compose Material touch targets remain in effect for icon buttons and controls.

Runtime verification for TalkBack, large font scale, high contrast, dark mode, Arabic RTL, landscape, tablet/foldable widths, and keyboard focus remains pending because the sandbox has no connected Android device or emulator.

## 6. Verification and protected boundaries

| Verification | Result |
|---|---|
| Actual Material 3 API inventory | PASS; inventory based on production Kotlin sources |
| Official M3 source review | PASS; current official pages consulted and referenced below |
| English/Arabic resource parity | PASS after additions; both files contain the same key set |
| Navigation route and label contract | PASS; no route or destination identity changes |
| TDLib, JNI, ABI, upload, database, WorkManager, scheduler diff review | PASS; no changes in protected areas |
| `git diff --check` | PASS |
| JVM unit tests | Blocked before task execution because the sandbox has no Android SDK path |
| Compose compile, lint, and release build | Blocked by the same missing Android SDK; no valid SDK location exists in the sandbox |
| Device/emulator visual and accessibility validation | Pending; required before GO certification |

## 7. Remaining risks and decision

No P0 issue was found. The applied corrections address the highest-confidence P1/P2 findings without introducing product features or changing real Telegram/upload behavior. The direct authentication snackbar, disabled upload setting placeholders, and peer bulk-action buttons remain documented limitations rather than being changed speculatively.

The release decision remains **NO-GO for full operational certification** until a real device or emulator demonstrates English and Arabic flows in light/dark themes, compact and expanded layouts, larger font scale, TalkBack semantics, authentication recovery, destination selection and pinning, queue state transitions, history operations, scheduling, and a real TDLib upload.

## References

[1]: https://m3.material.io/components/buttons/overview "Material 3 Buttons — Overview"
[2]: https://m3.material.io/components/cards/overview "Material 3 Cards — Overview"
[3]: https://m3.material.io/components/lists/overview "Material 3 Lists — Overview"
[4]: https://m3.material.io/components/progress-indicators/overview "Material 3 Progress indicators — Overview"
[5]: https://m3.material.io/components/search/overview "Material 3 Search — Overview"
[6]: https://m3.material.io/foundations/accessible-design/overview "Material 3 Accessible design — Overview"
[7]: https://m3.material.io/components/navigation-bar/overview "Material 3 Navigation bar — Overview"
[8]: https://m3.material.io/components/navigation-rail/overview "Material 3 Navigation rail — Overview"
[9]: https://m3.material.io/components/text-fields/overview "Material 3 Text fields — Overview"
[10]: https://m3.material.io/components/dialogs/overview "Material 3 Dialogs — Overview"
[11]: https://m3.material.io/components/chips/overview "Material 3 Chips — Overview"
[12]: https://m3.material.io/components/snackbar/overview "Material 3 Snackbar — Overview"
[13]: https://m3.material.io/components/tooltips/overview "Material 3 Tooltips — Overview"
