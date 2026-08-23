# Refactoring Status

## Current status

The incremental refactoring is in the Telegram application-layer isolation phase. The previous low-risk moves are complete: media utilities are under `core.util.media`, Room database classes are under `data.local.database`, and `SettingsDataStore` is under `data.local.datastore`.

## Telegram isolation result

The repository already contains a coherent Telegram boundary under `data.telegram`. `TelegramClientImpl` is the single application class with direct generated TDLib coupling. `TelegramRepositoryImpl` delegates through the client contract, while ViewModels and UI consume domain contracts and models. `TelegramUploadEngineImpl` consumes the client abstraction and upload events rather than raw generated TdApi types.

Because the existing package layout already separates Telegram integration from UI and generic core code, this phase records the boundary and usage maps without performing a risky top-level package relocation. No duplicate wrappers, repositories, models, or handlers were created.

## Protected behavior and assets

No TDLib version, generated binding, native artifact, ABI configuration, credential, authentication flow, session behavior, logout path, upload behavior, WorkManager behavior, application ID, or UI was changed by this phase.

## Verification status

Static verification must confirm that there is no stale `core.datastore` import, the WorkManager manifest guard remains passing, and the diff contains no protected native or manifest changes. Android compilation and unit tests are delegated to the repository GitHub Actions workflow because the local checkout does not include `gradlew` and the sandbox has no standalone `gradle` command.

The prior DataStore refactor commit `5e1f185` was validated by GitHub Actions run `32610172806`, which completed successfully for `arm64-v8a`, `armeabi-v7a`, and `x86_64`.
