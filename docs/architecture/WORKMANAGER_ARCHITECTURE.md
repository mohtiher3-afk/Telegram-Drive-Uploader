# WorkManager Architecture

| Worker | Input | Output | Dependencies | Constraints | Retry/cancellation | Persistence |
|---|---|---|---|---|---|---|
| `UploadWorker` | Persisted upload ID and task metadata | `Result.success`, `Result.retry`, or `Result.failure` | Hilt factory, upload manager/engine, Room, TDLib | Connected network; intentional schedule delay when configured | Cooperative cancellation and bounded retry policy | Room upload state plus WorkManager state |

`TelegramDriveApp` provides WorkManager configuration through `Configuration.Provider` and the injected `HiltWorkerFactory`. AndroidX Startup’s provider metadata removes the default initializer so the application configuration is authoritative. The manifest guard protects this relationship.

The queue must be reconciled against persisted Room state after process death and on app restart. That behavior is a device-QA requirement, not proven by APK assembly alone.
