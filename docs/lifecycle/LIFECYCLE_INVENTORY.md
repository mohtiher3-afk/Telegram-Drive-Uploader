# Application Lifecycle Inventory

**Scope:** Startup, Activity recreation, Compose collection, ViewModels, TDLib, WorkManager, persistence, coroutines, callbacks, and crash-recovery boundaries.  
**Mode:** Controlled maintenance; documentation-only review.

| Component | Lifecycle owner | Created | Destroyed | Persistent state | Risk |
|---|---|---|---|---|---|
| `TelegramDriveApp` | Process/Application | Process start | Process death | DI graph and WorkManager configuration are process-scoped | `onTrimMemory` clears diagnostics and calls `System.gc()` under pressure; runtime impact unverified. |
| `MainActivity` | Activity | Android launch/recreation | Activity destruction | Theme preference in DataStore; Compose state is not Activity-persisted here | Navigation is created by Compose `rememberNavController`; recreation restoration is unverified. |
| `AppNavigation` | Composition under Activity | Composition | Composition disposal | Onboarding completion in DataStore; upload task state in Room | `AppNavigation` obtains navigation and feature ViewModels through Hilt; recreation behavior needs device evidence. |
| `OnboardingViewModel` | Hilt ViewModel | First composition while onboarding is shown | ViewModel scope destruction | Completion in DataStore | Correctly separates completion persistence from upload/TDLib logic. |
| `UploadViewModel` | Navigation/Activity-scoped Hilt ViewModel in current shell | Main navigation composition | Scope destruction | Prepared list, selected destination, schedule are in memory; queued tasks in Room | Prepared-but-not-queued UI state is not process-death durable. |
| `TelegramAuthViewModel` | Auth destination ViewModel | Auth route composition | Route/ViewModel destruction | TDLib session/private directories and cached settings | Login restoration and route behavior after process death are runtime-unverified. |
| `TelegramDestinationViewModel` | Destination route ViewModel | Destination route composition | Route/ViewModel destruction | Pinned destination IDs in DataStore; live chats in TDLib | Selected UI destination is transient until copied into an upload task. |
| `QueueViewModel` | Queue route ViewModel | Queue route composition | Route/ViewModel destruction | Upload tasks/statuses in Room and WorkManager | Queue UI state is database-backed; active Worker recovery is runtime-unverified. |
| `HistoryViewModel` | History route ViewModel | History route composition | Route/ViewModel destruction | Completed upload records in Room | Query/period/sort filters are transient. |
| `SettingsViewModel` | Settings route ViewModel | Settings route composition | Route/ViewModel destruction | SettingsDataStore | Settings crash behavior requires runtime verification. |
| `UploadWorker` | WorkManager | WorkManager dispatch | Completion, retry, cancellation, or process termination | Input is `upload_id`; task is in Room | Late callbacks and cancellation races remain known risks. |
| `TelegramClientImpl` | Hilt singleton | Dependency graph/client initialization | Process/client shutdown | TDLib database/files directories and settings/cache | Duplicate-client and process-recreation behavior not device-tested. |
| Room/DataStore | Application storage | First access | Process-independent | Upload rows, settings, onboarding, pins, cached account metadata | Corruption/recovery after abrupt termination unverified. |
| Compose Flow collectors | Lifecycle-aware composition | Active lifecycle state | Lifecycle STOP/disposal | No independent persistence | Existing `collectAsStateWithLifecycle` use is static evidence; duplicate collectors need runtime observation. |

## Findings

No second Activity, Fragment, foreground service, or upload notification service was found. The application is single-Activity Compose with Hilt, Room/DataStore, WorkManager, and a singleton Telegram client. No lifecycle fix was applied because no reproducible crash or leak was established by static review.
