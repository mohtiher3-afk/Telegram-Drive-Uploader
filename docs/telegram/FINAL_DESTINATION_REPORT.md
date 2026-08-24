# Final Telegram Destination and File-Routing Report

**Repository:** [mohtiher3-afk/Telegram-Drive-Uploader](https://github.com/mohtiher3-afk/Telegram-Drive-Uploader)  
**Mode:** Production / Controlled Maintenance Mode  
**Scope:** Telegram chat loading, destination selection, search, permissions, ID propagation, upload routing, scheduler/worker behavior, privacy, RTL, dark mode, and accessibility.  
**Change policy:** Documentation-only. No TDLib, authentication, upload-engine, database schema, or UI behavior was changed.

## Destination Architecture

TDLib-backed `TelegramClientImpl` owns live chat and supergroup metadata. `TelegramDestinationViewModel` owns search state, derived results, pinned ordering, and transient screen selection. `UploadViewModel` materializes the selected destination into each prepared `UploadTask`. Room persists `destinationId`. `UploadWorker` reloads the task from Room, and the final TDLib send uses `task.destinationId`.

## Chat Types

The current builder emits private users, basic groups, supergroups, and channels. Channels are identified from TDLib supergroup type metadata. Destinations are filtered by current sendability-related chat or supergroup status/permissions. The model includes `OTHER`, but the current builder does not emit it.

## Chat Loading

After `AuthorizationStateReady`, the client requests up to 100 chats from `ChatListMain`, resolves each chat, and loads supergroup metadata when applicable. Updates for new chats, titles, permissions, and supergroups rebuild the derived list. No pagination, cache expiration, or explicit refresh mechanism was found.

## Pagination

No pagination implementation was identified. The initial TDLib request is limited to 100 main-list chats, while server search uses a limit of 50 channel results. No pagination was added under the protocol’s constraints.

## Search

The destination ViewModel applies a 300 ms debounce and `flatMapLatest`. The client issues `SearchPublicChat` for exact public username resolution and `SearchChatsOnServer` with a channel filter for partial/server matches. Local filtering matches title and username case-insensitively and strips a leading `@`. Blank queries use the current in-memory destination list.

## Selection State

Selection is transient. The destination ViewModel owns the screen selection; the upload ViewModel owns the selected destination for the prepared batch. The UI compares IDs and the lazy list keys rows by `id`. The current semantics assign one selected destination to every file in the prepared batch.

## Persistence

The selected destination itself is not persisted. Pinned destination IDs are persisted in DataStore as a user preference. Upload tasks persist only the destination ID as a `Long` in Room. WorkManager stores only `upload_id`; the worker reloads the task and obtains its destination ID from the database. Titles, usernames, and message content are not stored with upload tasks.

## Permissions

The destination builder uses actual TDLib chat permissions and supergroup status to exclude destinations that are known to be non-sendable. It considers creator/admin rights, restricted permissions, and basic chat permissions. No unsupported or invented Telegram permission model was introduced. Runtime permission-denied behavior remains unverified.

## Stale Destination Handling

A persisted task retains its numeric destination ID. The reviewed upload path does not perform a dedicated `GetChat` validation immediately before send and does not silently choose a fallback destination. If the destination is gone or inaccessible, TDLib/send failure is expected to flow through the existing failure path; explicit device evidence is still required to certify the behavior.

## Account Session Safety

The app supports one Telegram account. Logout clears the in-memory chat cache and derived destination list, but does not rewrite existing upload tasks. This avoids changing task history or schema but leaves cross-session queued-task behavior as a runtime validation requirement. Multi-account support was not added.

## Upload Routing

`UploadViewModel.addToQueue()` copies the exact selected destination ID into every task. `UploadRepositoryImpl` maps the `Long` without parsing, truncation, or alternate lookup. `UploadWorker` reloads the task from Room. `TelegramClientImpl` calls `TdApi.SendMessage(task.destinationId, ...)`. The implementation does not use list index, title, last-selected item, or list position as the routing identity.

## Worker Propagation

WorkManager input contains only the upload ID. The worker obtains the complete persisted task before invoking the upload engine. This keeps routing independent of mutable UI state and supports worker recreation through repository/database reload. Runtime scheduling and restart behavior were not exercised on a device in this phase.

## Scheduler

Scheduled uploads retain the same Room task and destination ID. The scheduler computes a delay from `scheduledAt` and enqueues unique work by upload task ID. No scheduler schema or destination behavior was changed.

## History

The current history ViewModel displays completed uploads and aggregate statistics but does not display destination title, username, or chat metadata. Historical destination data is therefore not reformatted or reconstructed by the UI.

## Privacy

The normal reviewed destination/upload diagnostics use upload IDs, event categories, and error codes rather than unnecessary chat titles, usernames, message content, or private identifiers. The UI intentionally displays destination title and optional username so the user can confirm the target. No new destination data is persisted.

## Accessibility

The destination screen has a labeled search field, content descriptions for back/clear/remove actions, test tags, and selected-state rendering. The code review did not establish a complete TalkBack, touch-target, or semantic-announcement result. Accessibility certification remains pending device/UI testing.

## RTL

The application includes Arabic resources and uses standard Compose layout primitives. The destination UI was not device-tested in Arabic and English during this phase, so title, username, search, selection, empty-state, error, and icon-direction behavior remain runtime validation items. Non-directional icons were not intentionally mirrored.

## Dark Mode

The destination screen uses `MaterialTheme` colors for surfaces, selected state, text, and empty/error content. No dark-mode device screenshot or visual QA was performed during this documentation phase.

## Tests

The destination test matrix records repository-supported identity propagation, search structure, loading behavior, worker persistence, and privacy boundaries. It separately marks stale destinations, chat removal, permission failures, cross-session behavior, RTL, dark mode, accessibility, and real Telegram routing as unverified. No fake Telegram behavior was added to production code.

## Runtime Verification

**REAL TELEGRAM DESTINATION TEST NOT VERIFIED.** No connected Android device or emulator with a real authenticated Telegram account was available to verify chat loading, destination selection, permission denial, stale destination behavior, logout/re-login isolation, scheduled worker recreation, or actual upload routing.

## Known Limitations

The implementation has no explicit pagination, cache expiration, pre-send destination existence check, chat-removal handler, or cross-session queued-task invalidation. These are documented behavior boundaries, not silently replaced with speculative fixes. The server-search request is channel-filtered, while the local list includes other eligible chat types already loaded from TDLib.

## Remaining Risks

The main risks are stale or inaccessible destination IDs after account/session changes, destination removal between selection and send, unverified network/device behavior, and incomplete visual/accessibility validation in Arabic, English, dark mode, and compact layouts. The stable-ID path significantly reduces wrong-destination risk in the normal flow, but runtime certification is still required.

## Final Safety Check

| Check | Decision |
|---|---|
| Wrong destination possible | UNKNOWN under stale/session-interruption cases; no title/index routing found |
| Stale destination possible | YES — no dedicated pre-send existence validation was found |
| Duplicate chat entries | UNKNOWN at runtime; maps and LazyColumn use stable ID, but no dedicated duplicate test was run |
| Destination ID preserved through upload | YES — repository code verified from selection to TDLib `SendMessage` |
| Permission failures handled | CONDITIONALLY — filtering and TDLib error path exist; real-device behavior unverified |
| Logout invalidates stale destination | CONDITIONALLY — in-memory cache clears; existing queued tasks remain unchanged |
| Scheduled destination preserved | YES in repository path — worker reloads Room task; runtime restart unverified |
| Chat information unnecessarily logged | NO evidence found in reviewed destination/upload diagnostics |
| TDLib changed | NO |
| Upload behavior changed | NO |
| Security regression | NO evidence introduced by documentation-only changes |

## Final Decision

# DESTINATION ROUTING CONDITIONALLY VERIFIED

Stable destination-ID propagation is verified by repository inspection from selection through Room, WorkManager, the worker, and TDLib `SendMessage`. Real Telegram destination loading, permission behavior, stale destination safety, session isolation, and end-to-end upload routing remain unverified.

## References

[1]: https://github.com/mohtiher3-afk/Telegram-Drive-Uploader "Telegram Drive Uploader repository"
[2]: https://core.telegram.org/tdlib/docs/ "TDLib documentation"
[3]: https://developer.android.com/topic/libraries/architecture/workmanager "Android WorkManager documentation"
[4]: https://developer.android.com/develop/ui/compose/accessibility "Jetpack Compose accessibility documentation"

PHASE AI COMPLETE — TELEGRAM DESTINATION AND FILE ROUTING REVIEW COMPLETE — WAITING FOR APPROVAL
