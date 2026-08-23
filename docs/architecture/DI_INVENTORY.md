# Dependency Injection Inventory

## Hilt entry points and modules

| Module/class | Type | Scope | Dependencies | Consumers | Risk |
|---|---|---|---|---|---|
| `TelegramDriveApp` | `@HiltAndroidApp` application | Application | Android runtime | Hilt application graph | Critical |
| `DatabaseModule` | `@Module` / `@InstallIn(SingletonComponent)` | `AppDatabase`: singleton; `UploadDao`: provider scope | `@ApplicationContext Context`, `AppDatabase` | `UploadRepositoryImpl` through `UploadDao` | High |
| `RepositoryModule` | `@Module` / `@InstallIn(SingletonComponent)` | Singleton bindings | Repository/client implementations | ViewModels and upload/application classes through contracts | High |
| `UploadModule` | `@Module` / `@InstallIn(SingletonComponent)` | Singleton bindings | Upload implementations | `UploadViewModel`, `UploadWorker` and upload flow | High |
| `WorkModule` | `@Module` / `@InstallIn(SingletonComponent)` | Singleton | `@ApplicationContext Context` | Upload scheduling and worker flow | Critical |

## Providers and bindings

| Definition | Return/interface | Implementation | Scope | Reason to keep |
|---|---|---|---|---|
| `provideAppDatabase` | `AppDatabase` | `Room.databaseBuilder(context, AppDatabase::class.java, "telegram_drive_db")` | `@Singleton` | One Room database instance and stable persisted identity |
| `provideUploadDao` | `UploadDao` | `database.uploadDao()` | Component-provided | Exposes the DAO from the singleton database |
| `bindUploadRepository` | `UploadRepository` | `UploadRepositoryImpl` | `@Singleton` | Domain contract and repository test seam |
| `bindTelegramClient` | `TelegramClient` | `TelegramClientImpl` | `@Singleton` | Isolates official TDLib client lifecycle behind a contract |
| `bindTelegramRepository` | `TelegramRepository` | `TelegramRepositoryImpl` | `@Singleton` | Isolates Telegram application operations behind a contract |
| `bindUploadManager` | `UploadManager` | `UploadManagerImpl` | `@Singleton` | Upload orchestration contract and test seam |
| `bindTelegramUploadEngine` | `TelegramUploadEngine` | `TelegramUploadEngineImpl` | `@Singleton` | Real upload engine contract and worker seam |
| `bindStreamingFileReader` | `StreamingFileReader` | `StreamingFileReaderImpl` | `@Singleton` | Bounded file access abstraction for upload engine |
| `provideWorkManager` | `WorkManager` | `WorkManager.getInstance(context)` | `@Singleton` | Uses the platform singleton and preserves startup behavior |

## Injectable consumers

All eight feature ViewModels use `@HiltViewModel` and constructor injection. `UploadWorker` uses `@HiltWorker` with `@AssistedInject` for `Context` and `WorkerParameters` plus injected application dependencies. `SettingsDataStore`, repositories, Telegram client, upload manager/engine, and streaming reader use constructor injection.

## Audit result

The re-audit found four modules, three providers, six bindings, eight Hilt ViewModels, one Hilt Worker, and one Hilt application entry point. No duplicate provider returning the same unqualified type was found. No scope change is justified. No new module is created solely to match a conceptual list.
