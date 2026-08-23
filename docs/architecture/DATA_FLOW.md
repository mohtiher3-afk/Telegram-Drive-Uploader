# Data Flow

## UI and persistence

```text
Compose screen
    ↓ events
ViewModel state/action
    ↓ domain contract
Repository or manager
    ↓
Room DAO / DataStore-like preferences / Android file APIs
    ↓
StateFlow and recomposition
```

Upload state is persisted in the upload entity and exposed through repository flows. Mutable state exists in ViewModels, coroutine flows, TDLib pending-request maps, Room rows, and WorkManager records. The main duplication risk is disagreement between Room status and WorkManager execution state; reconciliation and lifecycle tests should be added before any package move.

## UI and Telegram

```text
UploadViewModel
    ↓
UploadManagerImpl
    ↓
UploadWorker
    ↓
TelegramUploadEngineImpl
    ↓
TelegramRepositoryImpl
    ↓
TDLib
```

TDLib updates are converted into domain progress and terminal events. The UI must not infer delivery from local staging or a provisional send callback.
