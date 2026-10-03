# Hilt Dependency Graph

## Actual graph

```text
TelegramDriveApp (@HiltAndroidApp)
  ↓ SingletonComponent
  ├── DatabaseModule
  │     └── AppDatabase (singleton)
  │            └── UploadDao
  │                   └── UploadRepositoryImpl → UploadRepository
  ├── RepositoryModule
  │     ├── TelegramClientImpl → TelegramClient
  │     │       └── TelegramRepositoryImpl → TelegramRepository
  │     └── UploadRepositoryImpl → UploadRepository
  ├── UploadModule
  │     ├── UploadManagerImpl → UploadManager
  │     ├── TelegramUploadEngineImpl → TelegramUploadEngine
  │     └── StreamingFileReaderImpl → StreamingFileReader
  └── WorkModule
        └── WorkManager (singleton)

Feature ViewModels
  ├── HomeViewModel
  ├── UploadViewModel → UploadManager / TelegramRepository / SmartFileAssistant
  ├── QueueViewModel
  ├── HistoryViewModel
  ├── SettingsViewModel → SettingsDataStore
  ├── OnboardingViewModel → SettingsDataStore
  ├── TelegramAuthViewModel → TelegramRepository
  └── TelegramDestinationViewModel → TelegramRepository

UploadWorker (@HiltWorker)
  └── TelegramUploadEngine + UploadRepository + WorkManager-managed execution
```

## Dependency direction

The observed direction is application entry point to core/data providers, then feature consumers through domain contracts. Telegram and upload implementations are bound in `core.di`, but their concrete classes remain under data boundaries. UI screens do not construct repositories, TDLib clients, Room databases, or WorkManager instances directly.

## Findings

| Check | Result |
|---|---|
| Circular dependency | No confirmed cycle in constructor/module graph |
| Duplicate unqualified providers | None found |
| Feature-to-feature dependency | No direct ViewModel-to-ViewModel dependency found in the inspected inventory |
| Infrastructure leakage | No UI construction of database, TDLib, or WorkManager found |
| Service locator | No custom service locator or global dependency registry found |
| Unnecessary interfaces | Existing interfaces provide domain/test boundaries; removal is not justified |
| Global singleton risk | Singleton scope is used for long-lived repositories, Telegram client, upload engine, database, and WorkManager; no automatic scope change is justified |

## Decision

The existing four-module graph is coherent. The safe refactor for this phase is documentation and regression validation only. Adding empty `CoreModule`, `DataStoreModule`, `TelegramModule`, `FeatureModule`, or dispatcher modules would increase surface area without a demonstrated dependency problem.
