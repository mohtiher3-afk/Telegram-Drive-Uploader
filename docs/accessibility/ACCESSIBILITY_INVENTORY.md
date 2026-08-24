# Accessibility Inventory

**Scope:** Existing Compose UI only. No product redesign or formal compliance claim.

| Component / control | Screen | Interactive | Description / state | Semantics evidence | Risk |
|---|---|---:|---|---|---|
| Skip / Continue buttons | Onboarding | Yes | Visible localized labels | Material Button semantics | Dynamic page and permission result need TalkBack testing. |
| Onboarding hero image | Onboarding | No | Meaningful app identity image has localized description | `contentDescription` present | Decorative page icons intentionally have null descriptions. |
| Bottom navigation items | Main shell | Yes | Localized navigation labels | Material navigation semantics | Order and spoken Arabic/LTR flow need runtime test. |
| Back icon | Upload/destination/settings routes | Yes | Localized Back description where implemented | IconButton description present in reviewed screen | Coverage across every route needs device test. |
| Search field / clear action | Destination | Yes | Search and clear labels are localized | TextField/material semantics and clear description | IME, focus, and Arabic keyboard need runtime test. |
| Destination list rows | Destination | Yes | Chat title, type, permission/pinned indicators | Row click plus child semantics | Spoken state and long names need TalkBack test. |
| Pin/remove selection icon | Destination | Yes | Localized action description | IconButton description present | State announcement needs runtime verification. |
| Queue filter chips | Queue | Yes | All, Active, Paused, Failed | `FilterChip(selected=...)` exposes selected state | Dynamic spoken state needs TalkBack test. |
| Retry/pause/cancel/resume buttons | Queue/status | Yes | Localized visible action text; icons decorative | Material TextButton semantics | Status and action order need runtime test. |
| Upload progress indicator | Queue/status | No direct action | Visual percentage, speed, ETA, status text | LinearProgressIndicator has progress value; surrounding status is visible text | No explicit `stateDescription` or custom progress semantics found. |
| File row/remove action | Upload/queue/history | Yes | Filename and remove action | VideoItem implementation requires runtime review | Long names and small action target need test. |
| Settings controls | Settings | Yes | Theme/language/account controls as implemented | Material controls | Large font and keyboard/focus order need runtime test. |
| Dialogs/sheets | Existing screens | Yes | Contextual confirmation/errors if present | Material dialog semantics where used | Focus entry and dismissal need runtime test. |

## Static Summary

The UI largely uses Material 3 controls, localized visible labels, lifecycle-aware collection, and stable list keys. Icons that repeat adjacent visible action text are generally given `contentDescription = null`, avoiding duplicate announcements. A formal TalkBack, contrast, touch-target, or adaptive-layout pass was not executed on a device.
