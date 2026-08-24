# Crash Recovery and Lifecycle Audit

## Static Findings

The application uses a single Hilt `Application`, one `MainActivity`, Compose navigation, lifecycle-aware Flow collection, ViewModel scopes, Room/DataStore persistence, a singleton TDLib client, and WorkManager for background uploads. No second startup system, second Activity, duplicate client initialization, or foreground upload service was found.

`MainActivity` performs only Activity/content setup in `onCreate`; application-wide WorkManager configuration belongs to `TelegramDriveApp`. Feature work launched from ViewModels uses `viewModelScope`, and Worker work is owned by WorkManager. Existing Compose screens use `collectAsStateWithLifecycle` for observed state. These are static ownership findings, not proof under process death.

## Recovery Basis

Queued upload tasks persist in Room, while WorkManager persists its request and passes only `upload_id` to the Worker. The Worker reloads task data, which avoids dependence on transient prepared-list memory after queue insertion. TDLib uses app-private session/database/file paths. DataStore persists onboarding, theme, cached user metadata, and pinned destination IDs.

## Recovery Gaps

No explicit application-start reconciliation was found for Room rows that have no WorkManager request. No process-death test was available for `QUEUED`, `PREPARING`, `UPLOADING`, `FAILED`, or scheduled work. No proof exists that a cancelled or replaced Worker cannot emit a late state write. Prepared-but-not-queued files and transient navigation/form state have no explicit process-death persistence.

## Coroutine and Callback Boundaries

ViewModel coroutines are tied to ViewModel lifetime. Compose observers use lifecycle-aware collection. Upload Manager's post-enqueue WorkManager inspection uses a one-shot future listener rather than an indefinitely retained observer. The reviewed code does not establish a retained Activity or View reference in a singleton. TDLib callback cleanup and duplicate callback behavior require runtime lifecycle evidence.

## Crash Finding

No reproducible crash was discovered by this static review. No exception was removed blindly and no new recovery behavior was invented. A real crash should be handled through the controlled Bug Fix Protocol with reproduction evidence, root cause, focused fix, and regression test.

## Required Runtime Tests

Perform cold start, warm start, background/foreground, rotation, font-scale/display-size changes, process death during each actual upload state, restart with queued/scheduled/history records, logout/re-login, network loss, and cancellation. Record queue, Worker, TDLib, navigation, authentication, and resource outcomes separately.
