# Class Responsibility Map

| Class/group | Primary responsibility | Dependencies | Target package | Split decision |
|---|---|---|---|---|
| `TelegramDriveApp` | Application startup and WorkManager configuration | Hilt, WorkManager | `core/app` or KEEP | KEEP; protected |
| `MainActivity` | Compose host and system UI entry | Compose, navigation | `ui` or KEEP | KEEP initially |
| `AppNavigation` | NavHost, tabs and route transitions | Compose Navigation, ViewModels | `core/navigation` | KEEP |
| `TelegramClientImpl` | TDLib client lifecycle, authorization and requests | TDLib, coroutines | `data/telegram/client` | REVIEW; split only with tests |
| `TelegramRepositoryImpl` | Domain-facing Telegram operations | Telegram client, models | `data/telegram/repository` | KEEP |
| `UploadManagerImpl` | Queue orchestration and WorkManager enqueue | Repository, WorkManager | `data/upload` | REVIEW |
| `TelegramUploadEngineImpl` | File preparation, send request and terminal confirmation | TDLib repository, completion policy | `data/upload` | REVIEW |
| `UploadWorker` | Background execution and retry result | Hilt, manager, WorkManager | `feature/upload/worker` or `data/upload/worker` | KEEP until worker tests exist |
| `UploadRepositoryImpl` | Room-backed upload persistence | DAO, entities | `data/repository` | KEEP |
| `*ViewModel` classes | Screen state and user actions | Domain contracts | Existing feature packages | KEEP |
| `VideoFormatSupport`, `VideoMetadataExtractor` | Media validation and metadata | Android media APIs | `core/util` | KEEP |
| `SmartFileAssistant` classes | Local filename analysis and suggestions | Pure Kotlin utilities | `core/ai` or current package | REVIEW |

No class is classified for deletion. Any responsibility split is `REVIEW` until tests characterize lifecycle and error behavior.
