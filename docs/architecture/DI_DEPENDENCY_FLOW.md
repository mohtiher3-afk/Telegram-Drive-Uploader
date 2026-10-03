# Hilt Dependency Flow

## Application graph

```text
TelegramDriveApp
  ↓ Hilt SingletonComponent
  ├─ DatabaseModule → AppDatabase → UploadDao → UploadRepositoryImpl → UploadRepository
  ├─ RepositoryModule → TelegramClientImpl → TelegramClient
  │                  └→ TelegramRepositoryImpl → TelegramRepository
  ├─ UploadModule → StreamingFileReaderImpl → StreamingFileReader
  │              ├→ TelegramUploadEngineImpl → TelegramUploadEngine
  │              └→ UploadManagerImpl → UploadManager
  └─ WorkModule → WorkManager

Compose feature screens
  ↓ hiltViewModel()
Feature ViewModels
  ↓ constructor injection
Domain contracts and local/core services

UploadWorker
  ↓ HiltWorker + AssistedInject
TelegramUploadEngine + UploadRepository + DiagnosticsManager
```

## Protected boundaries

| Boundary | Current mechanism | Assessment |
|---|---|---|
| Generated TDLib | `TelegramClientImpl` constructor injection through `TelegramClient` binding | Correctly isolated from UI and ViewModels |
| Real upload engine | `TelegramUploadEngineImpl` bound to `TelegramUploadEngine` | Preserve singleton lifetime and event semantics |
| Database | `DatabaseModule` singleton plus DAO provider | Preserve database name and migration behavior |
| Background execution | `WorkModule` provides `WorkManager`; `UploadWorker` uses Hilt worker injection | Do not change scope or startup integration in this phase |
| Feature ViewModels | `@HiltViewModel` and Compose `hiltViewModel()` | Existing screen boundary is consistent |
| Application context | `@ApplicationContext` in DataStore, database, worker, and file reader paths | Preserve qualifier usage |

## DI findings

The graph does not show a duplicate binding, an obvious scope mismatch, or a module responsibility overlap requiring immediate source changes. Moving modules into data- or feature-specific packages would create package/import churn while leaving their `SingletonComponent` responsibilities unchanged. The recommended action is to retain the current `core.di` boundary and add tests or compile validation rather than perform a cosmetic relocation.
