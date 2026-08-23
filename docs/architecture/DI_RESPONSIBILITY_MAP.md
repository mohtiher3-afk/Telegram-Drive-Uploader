# Hilt DI Responsibility Map

## Module inventory

| Module | Install scope | DI responsibility | Current risk |
|---|---|---|---|
| `DatabaseModule` | `SingletonComponent` | Provides singleton `AppDatabase` and its `UploadDao` | High because the database name and migration policy are runtime-sensitive |
| `RepositoryModule` | `SingletonComponent` | Binds `UploadRepositoryImpl`, `TelegramClientImpl`, and `TelegramRepositoryImpl` to domain/application contracts | High because Telegram and persistence lifetimes are long-lived |
| `UploadModule` | `SingletonComponent` | Binds upload manager, Telegram upload engine, and streaming reader implementations to interfaces | High because upload lifecycle and real delivery semantics depend on these bindings |
| `WorkModule` | `SingletonComponent` | Provides singleton `WorkManager` from `ApplicationContext` | Critical because startup and background execution are protected behavior |

## Injectable class inventory

| Class | Constructor style | Scope | Consumed by |
|---|---|---|---|
| `SettingsDataStore` | `@Inject` | Unscoped binding created as needed | `OnboardingViewModel`, `SettingsViewModel` |
| `UploadRepositoryImpl` | `@Inject` | Bound as `@Singleton` | Upload ViewModel, queue/history flows |
| `TelegramClientImpl` | `@Inject` | Bound as `@Singleton` | `TelegramRepositoryImpl` and upload/destination flows through contracts |
| `TelegramRepositoryImpl` | `@Inject` | Bound as `@Singleton` | Telegram ViewModels and upload preparation |
| `TelegramUploadEngineImpl` | `@Inject` | Bound as `@Singleton` | `UploadWorker` through `TelegramUploadEngine` |
| `UploadManagerImpl` | `@Inject` | Bound as `@Singleton` | `UploadViewModel` |
| `StreamingFileReaderImpl` | `@Inject` | Bound as `@Singleton` | Upload engine |
| Feature ViewModels | `@HiltViewModel` + `@Inject` | ViewModel-managed | Compose screens via `hiltViewModel()` |
| `UploadWorker` | `@HiltWorker` + `@AssistedInject` | WorkManager-managed | WorkManager worker creation through Hilt |

## Decision

The current modules are already separated by responsibility and installed in the appropriate application-wide component. No module split, scope change, binding replacement, dependency upgrade, or provider rewrite is justified by the audit. The safe result for this phase is documentation and regression verification only.
