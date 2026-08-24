# Telegram Destination Architecture

## Ownership

The TDLib client owns live chat and supergroup metadata. `TelegramDestinationViewModel` owns the destination screen’s query, result flow, transient selection, and pinned ordering. `UploadViewModel` owns the selected destination for the prepared batch and copies its stable ID into each `UploadTask`. Room owns the persisted upload record, including `destinationId`. `UploadWorker` reloads that record and does not read mutable UI state. `TelegramClientImpl` is the final TDLib routing boundary.

This creates one application-level source of truth for each concern: live chat facts come from TDLib, screen selection comes from the destination ViewModel, upload selection is materialized into the persisted task, and final routing uses the task’s `Long` ID.

## Stable Identity

`TelegramDestination.id` and `UploadTask.destinationId` are Kotlin `Long` values. The repository mapper copies the value directly between the Room entity and domain model. The final TDLib call uses `TdApi.SendMessage(task.destinationId, ...)`. No string parsing, title lookup, list-index lookup, or integer narrowing was found in the reviewed path.

The destination UI uses the same `Long` ID for lazy-list keys, selected-state comparison, and pinned-ID ordering. This is the correct stable identity boundary for the current single-account architecture.

## Data Minimization

The destination model carries only the fields currently used by the application: ID, title, optional username, type, optional photo slot, and sendability. The current builder supplies no photo. Room stores only the destination ID with upload metadata, not title, username, or message content. The destination screen displays title and username because those fields are needed for user confirmation.

## Chat Loading and Cache

The in-memory cache is owned by `TelegramClientImpl` and consists of `LinkedHashMap<Long, TdApi.Chat>` and `LinkedHashMap<Long, TdApi.Supergroup>`, guarded by `chatLock`. Updates replace entries by ID and rebuild the derived destination list. There is no explicit cache expiration or pagination mechanism. Logout clears the cache and derived list.

## Permissions

`rebuildDestinations()` includes private chats, groups, supergroups, and channels only when the current TDLib chat/supergroup status or permissions indicate that sending is allowed. Creator and administrator rights are checked for supergroups; restricted permissions and basic chat permissions are considered. The application does not invent a separate Telegram permission model. Real permission-denied scenarios remain runtime validation requirements.

## Session and Account Safety

The application supports one Telegram account. Logout clears the client’s in-memory destinations and the cached account user, but existing Room upload tasks are not automatically rewritten. Because queued tasks retain destination IDs, cross-session task execution must be tested explicitly before production certification. Multi-account isolation was not added.

## Wrong-Destination Safety

The final send operation receives the exact persisted `destinationId` from the upload task. The implementation does not use a title, current list position, last selected row, or index position. This is repository-supported evidence of correct identity propagation. It does not prove that a stale ID remains accessible after a logout, chat deletion, or account change; those cases remain unverified.

## Routing Decision

The repository supports stable-ID routing through selection, Room, WorkManager, the worker, and TDLib. No confirmed wrong-destination defect was identified during inspection, so no behavior change was justified under the controlled-maintenance rules.
