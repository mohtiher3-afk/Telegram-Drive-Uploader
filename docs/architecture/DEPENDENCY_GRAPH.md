# Dependency Graph

```text
Home / Upload / Queue / History / Settings screens
    ↓
Feature ViewModels
    ↓
Domain repositories and upload contracts
    ↓
Data implementations
    ├── Room DAO/entities
    ├── Android file APIs
    ├── WorkManager
    └── Telegram repository/client
            ↓
        Official TDLib
```

The main infrastructure dependency flow is:

```text
UploadViewModel → UploadManagerImpl → WorkManager → UploadWorker
                                   → UploadRepositoryImpl → Room
                                   → TelegramUploadEngineImpl → TelegramClientImpl → TDLib
```

Potential violations to watch are UI calls into data implementations, feature-to-feature imports, and infrastructure types leaking into domain models. No circular dependency is declared without a complete import graph; suspected cases are `REVIEW`.
