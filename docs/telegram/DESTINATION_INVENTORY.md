# Telegram Destination Inventory

**Scope:** Chat loading, destination search, selection, permission filtering, persistence, and upload routing.  
**Mode:** Production / Controlled Maintenance Mode.  
**Change policy:** Documentation-only review; no TDLib, authentication, upload-engine, database-schema, or route behavior was changed.

## Components

| Component | Location | Responsibility | Data | Source | Risk / Review Note |
|---|---|---|---|---|---|
| Destination model | `domain/model/TelegramDestination.kt` | Carries the destination identity and display/sendability fields. | `id: Long`, title, optional username, type, optional photo, sendable flag | Application model constructed from TDLib chat/supergroup data | Stable numeric `Long` ID is the routing identity. |
| Destination type | `domain/model/TelegramDestination.kt` | Classifies supported destinations. | USER, GROUP, SUPERGROUP, CHANNEL, OTHER | Application mapping from TDLib chat types | `OTHER` exists in the model but is not emitted by the current builder. |
| TDLib chat cache | `TelegramClientImpl` | Holds `TdApi.Chat` and `TdApi.Supergroup` objects in synchronized maps. | Chat ID, title, type, permissions, supergroup status/usernames | TDLib updates and explicit lookup responses | In-memory cache has no explicit expiration; updates replace entries by stable ID. |
| Initial chat loading | `TelegramClientImpl.requestChats()` | Requests up to 100 chats from `ChatListMain` once after authorization. | Chat IDs, then `GetChat` and supergroup metadata | TDLib `GetChats` | No pagination or explicit refresh path was found. |
| Public search | `TelegramClientImpl.requestDestinationSearch()` | Resolves an exact public username. | Query without leading `@` | TDLib `SearchPublicChat` | Search errors are ignored as normal lookup behavior. |
| Server search | `TelegramClientImpl.requestDestinationSearch()` | Searches names/partial matches with a channel filter and limit 50. | Query, up to 50 chat IDs | TDLib `SearchChatsOnServer` with `SearchChatTypeFilterChannel` | Search intentionally asks TDLib for channels; local filtering also searches current titles/usernames. |
| Destination flow | `TelegramDestinationViewModel` | Debounces query, flat-maps repository search, orders pinned IDs, and owns transient selected destination. | Query, list, selected object, pinned ID set | Repository plus DataStore | One selected destination exists per ViewModel instance and is not persisted. |
| Destination screen | `TelegramDestinationScreen` | Renders auth fallback, search, empty state, selected banner, list, pin actions, and confirmation. | Title, username, type-derived icon, selected ID | ViewModel state | Lazy list uses `key = { it.id }`; selection compares IDs. |
| Upload selection handoff | `AppNavigation` and `UploadViewModel` | Returns selected destination to upload preparation and stores it in upload ViewModel memory. | Full `TelegramDestination` object, later `destination.id` | Navigation callback | The upload ViewModel uses one global selected destination for the prepared batch. |
| Upload persistence | `UploadEntity` / repository mapper | Stores destination ID with each upload task. | `destinationId: Long` | Room | Destination title/username are not persisted, reducing Telegram metadata retention. |
| Worker input | `UploadManagerImpl` / `UploadWorker` | Serializes only `upload_id` into WorkManager; worker reloads the full task from Room. | Upload ID, then persisted destination ID | WorkManager plus Room | Worker does not depend on mutable UI state. |
| TDLib routing | `TelegramClientImpl.uploadLocalDocument()` | Sends `SendMessage(task.destinationId, ...)` after preliminary upload. | Exact task destination ID | TDLib | Final send uses stable ID, not title, index, or list position. |
| Permission filtering | `TelegramClientImpl.rebuildDestinations()` | Includes only chats where the current status/permissions indicate sending is allowed. | Creator/admin rights, restricted permissions, basic chat permissions | TDLib `Chat`/`Supergroup` data | Runtime permission scenarios remain unverified. |
| Destination persistence | `SettingsDataStore` | Persists pinned destination IDs only. | Set of `Long` IDs | DataStore | Selected destination itself is not persisted. Pinned IDs may outlive chat availability and are only ordering hints. |
| History | `HistoryViewModel` | Displays completed upload history and aggregates without destination metadata. | Upload records, status, time, size | Upload repository | Historical destination is not shown in the current UI. |

## Supported Destination Types

The current builder emits private users, basic groups, supergroups, and channels. A supergroup is classified as a channel when TDLib marks `ChatTypeSupergroup.isChannel`; otherwise it is classified as a supergroup. A destination is excluded when the current permission/status logic indicates that the app cannot send. No assumption is made that every Telegram chat is uploadable.

## Privacy Boundary

The destination model exposes title and optional username to the UI because the user must identify a target. The reviewed diagnostic messages use upload IDs and error codes rather than logging destination titles, usernames, message content, or chat identifiers in the normal destination-routing paths. Pinned IDs are stored in DataStore because pinning is an explicit user preference; selected destination identity is carried in memory and then copied as the upload task’s numeric ID.
