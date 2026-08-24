# Telegram Destination and File-Routing Flow

## End-to-End Flow

```text
Authenticated Telegram client
        ↓ AuthorizationStateReady
Initial GetChats(ChatListMain, 100)
        ↓ chat IDs
GetChat + GetSupergroup metadata
        ↓ synchronized in-memory maps
rebuildDestinations()
        ↓ eligible TelegramDestination list
Destination screen search/filter
        ↓ selected object keyed by Long id
Upload preparation
        ↓ UploadViewModel global selected destination
Prepared UploadTask copies destination.id
        ↓ Room uploads.destinationId
WorkManager input contains upload_id only
        ↓ worker reloads UploadTask from Room
UploadEngine.uploadFile(uploadTask)
        ↓ task.destinationId
TDLib SendMessage(task.destinationId, ...)
        ↓ UpdateMessageSendSucceeded / UpdateMessageSendFailed
Confirmed completion or failure
```

## Entry and Navigation

The home screen can navigate to upload preparation or Telegram authentication. In upload preparation, the user opens the destination route. The destination screen shows a reconnect path when Telegram is not `AUTHORIZED`; otherwise it shows the searchable eligible-destination list. Confirming a selected destination returns it through the navigation callback to the shared `UploadViewModel`. The route graph is unchanged by this review.

## Chat Loading

After TDLib reaches `AuthorizationStateReady`, `TelegramClientImpl` requests the main chat list once with `GetChats(ChatListMain(), 100)`. Each returned chat ID is resolved with `GetChat`; supergroups additionally trigger `GetSupergroup`. New and changed chat/supergroup updates update the synchronized maps and rebuild the destination list. No pagination, explicit refresh, expiration timer, or full-database load was identified.

## Search and Filtering

`TelegramDestinationViewModel` debounces the query by 300 ms and uses `flatMapLatest` to replace the repository search flow when the query changes. The client searches an exact public username with `SearchPublicChat` and partial/server results with `SearchChatsOnServer` restricted to channel type and a limit of 50. The resulting local flow filters titles and usernames case-insensitively and removes a leading `@`. Blank queries return the current destination cache without issuing a search request.

Search result ordering is then adjusted by the pinned-ID set. List identity is the actual Telegram chat ID. The UI uses `LazyColumn` keys based on `id`, compares selection by `id`, and does not identify a destination by title or list position.

## Selection and Upload Creation

`TelegramDestinationViewModel` owns the transient selection while the destination screen is open. `UploadViewModel` owns the selected destination for the prepared upload batch. When the user confirms a destination, `UploadViewModel.selectDestination()` stores the object and exposes it in the preparation state. `addToQueue()` copies the selected destination’s `id` into every prepared task, then persists each task and enqueues work.

The current semantics are **one global destination per prepared batch**. Each file receives the same selected destination ID. Per-file destination selection is not implemented and was not added.

## Background Propagation

WorkManager serializes only the upload ID. `UploadWorker` reloads the complete task from Room and passes it to the upload engine. The destination ID therefore does not rely on a mutable screen or ViewModel. Scheduled uploads use the same persisted task; the scheduling delay is derived from `scheduledAt`. No database migration or alternate destination serialization was introduced.

## Final TDLib Routing

The upload engine reaches `TelegramClientImpl.uploadLocalDocument()`. The client chooses `FileTypeVideo` for MIME values beginning with `video/`, otherwise `FileTypeDocument`, stages the local file according to the existing upload path, and sends `TdApi.SendMessage(task.destinationId, ..., content)`. Completion is not inferred from the local `SendMessage` response: the code waits for `UpdateMessageSendSucceeded` or reports `UpdateMessageSendFailed`. The stable task ID is the only destination identity used for the final send.

## Destination Changes and Staleness

The selected destination is transient until task creation. Once tasks are inserted, each task retains the numeric destination ID. A later change in the UI selection affects future batches, not already-persisted tasks. There is no explicit pre-upload `GetChat` validation for a task destination immediately before sending; TDLib send failure and the existing upload error path are the available protection. A stale or inaccessible destination is therefore a documented runtime validation case, not claimed as fully handled.

## Logout Boundary

The Telegram client clears its in-memory chat cache and destination list on logout. Pinned IDs remain in DataStore, but they do not themselves create a destination or route an upload. Existing queued tasks retain their persisted destination IDs; the reviewed code does not automatically rewrite or delete them during logout. Cross-session queued-task behavior requires explicit real-device testing and is not certified here.
