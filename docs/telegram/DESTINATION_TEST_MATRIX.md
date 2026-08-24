# Telegram Destination Test Matrix

**Rule:** Runtime results are not fabricated. Repository-supported behavior and environment-limited cases are explicitly distinguished.

| Scenario | Expected | Actual | Status |
|---|---|---|---|
| Chat load after authorization | Main chat list is requested and chat metadata is resolved. | `GetChats(ChatListMain(), 100)` is issued once; chat and supergroup records are resolved and cached. | PASS — repository verified |
| Empty list | UI shows a localized empty state without crashing. | Destination screen renders separate empty messages for blank and non-blank queries. | PASS — repository verified |
| Search | Query is debounced, exact public usernames and server channel matches are requested, and local title/username filtering is applied. | 300 ms debounce, `flatMapLatest`, `SearchPublicChat`, `SearchChatsOnServer`, and local filtering are present. | PASS — repository verified; runtime unverified |
| Search cancellation | Older query results do not replace newer flow results. | `flatMapLatest` replaces the repository flow; TDLib requests themselves are asynchronous and not request-ID tagged. | CONDITIONAL — runtime race behavior unverified |
| Selection | Selected state is identified by stable chat ID. | ViewModel stores a destination object; UI compares `selectedDestination.id`; list keys use `id`. | PASS — repository verified |
| Selection restore | Selection survives route recreation if designed to do so. | Selection is transient in the destination ViewModel and not persisted. | NOT APPLICABLE / not persisted |
| Chat updated | Rename/permission updates refresh the derived destination list. | `UpdateChatTitle`, `UpdateChatPermissions`, and `UpdateSupergroup` update cached data and rebuild the list. | PASS — repository verified; runtime unverified |
| Chat removed | Removed destination cannot be selected or used incorrectly. | No explicit chat-removal handler or pre-send destination revalidation was found. | NOT VERIFIED |
| Permission denied | Ineligible destination is excluded or upload fails clearly without silent rerouting. | Current builder filters known permission/status cases; TDLib send failures use the existing upload error path. | CONDITIONAL — runtime not verified |
| Stale destination | A selected/deferred destination that disappears fails safely and never falls back to another chat. | Stable ID is preserved; no explicit pre-send existence check or fallback was found. | NOT VERIFIED |
| Logout/re-login | Old session destinations cannot silently mix with the new session. | Logout clears the client cache/list; existing queued tasks retain their persisted IDs. | CONDITIONAL — cross-session runtime unverified |
| Upload routing | Exact selected destination ID reaches `SendMessage`. | `UploadViewModel` copies `destination.id` to each task; client sends `task.destinationId`. | PASS — repository verified |
| Multiple files | Each file retains intended destination semantics. | Current architecture assigns the same selected destination ID to every prepared task in a batch. | PASS — global-batch semantics documented |
| Scheduled upload | Destination survives scheduling, restart, and worker recreation. | WorkManager stores `upload_id`; worker reloads the Room task containing `destinationId`. | PASS — repository verified; restart runtime unverified |
| Wrong destination by title/index | Title/list order must never determine routing. | No title/index routing path found; final TDLib send uses `Long` ID. | PASS — repository verified |
| Destination privacy | Diagnostics avoid unnecessary chat title, username, content, and identifiers. | Normal reviewed destination/upload diagnostics use upload IDs and error codes; UI intentionally displays title/username. | PASS — repository review |
| Arabic/RTL | Titles, username, search, selection, empty states, and errors remain usable in Arabic. | Arabic resources and standard Compose layout are present; no device RTL screenshot/test was run in this phase. | NOT VERIFIED |
| Dark mode | Selection, list, empty state, and errors remain legible in dark mode. | Theme-based colors are used; no device visual test was run in this phase. | NOT VERIFIED |
| Accessibility | Search is labeled, selection is announced, and actions have semantics. | Search has placeholder text; buttons/icons have labels in reviewed areas; no accessibility audit run. | NOT VERIFIED |
| Real Telegram destination test | Authenticated chat loading, selection, and upload routing work on device. | No real Telegram device/emulator environment was available. | NOT VERIFIED |
