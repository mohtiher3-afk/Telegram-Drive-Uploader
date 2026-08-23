# DI Circular Dependency Audit

## Audit scope

The audit covered Hilt modules, constructor-injected classes, `@Provides`, `@Binds`, ViewModels, the Hilt Worker, Telegram integration, upload integration, Room, DataStore, and WorkManager references.

## Confirmed cycles

No confirmed constructor or provider cycle was found. The observed dependency paths are directional:

```text
Database → DAO → UploadRepository
TelegramClient → TelegramRepository
StreamingFileReader → TelegramUploadEngine → UploadWorker
SettingsDataStore → Onboarding/Settings ViewModels
```

No path was found that returns from a feature ViewModel or worker back into the originating feature. No `Telegram → Upload → Telegram` constructor cycle was confirmed; the upload engine consumes the Telegram client abstraction and is consumed by the worker.

## Risk review

| Candidate cycle | Evidence | Status |
|---|---|---|
| Feature → Upload → Feature | ViewModels consume domain/upload contracts; no ViewModel is injected into another ViewModel | Not found |
| Telegram → Upload → Telegram | Upload engine depends on Telegram-facing abstractions, but no reverse upload-engine dependency into Telegram repository was found | Not found; preserve boundary |
| Database → Repository → Database | Repository consumes DAO; database does not consume repository | Not found |
| Worker → WorkManager → Worker | WorkManager is provided by platform singleton and worker creation is Hilt-managed; no worker injection into `WorkModule` | Not found |
| DataStore → ViewModel → DataStore | ViewModels consume DataStore; DataStore does not consume ViewModels | Not found |

## Conclusion

No low-risk cycle-breaking change is justified. Future cycle analysis should be repeated if new scheduling, upload callbacks, or feature-level services are introduced. No fake implementation, provider indirection, lazy locator, or manual registry was added for this phase.
