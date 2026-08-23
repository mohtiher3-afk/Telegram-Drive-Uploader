# Telegram Dependency Map

## Scope

This map records the application-layer integration around official TDLib v1.8.66. It does not describe or modify the generated `org.drinkless.tdlib.*` bindings, native libraries, ABI configuration, or TDLib build scripts.

## Current dependency flow

```text
UI
  ↓
ViewModel
  ↓
Domain repository contract
  ↓
Data Telegram repository implementation
  ↓
Application Telegram client contract
  ↓
TDLib adapter/client implementation
  ↓
org.drinkless.tdlib.Client and TdApi
  ↓
Telegram network/session
```

## Actual classes

| Layer | Actual class or package | Responsibility | Direct TDLib coupling |
|---|---|---|---|
| UI | `feature.telegram.TelegramAuthScreen` | Renders authentication state and collects user input | No |
| UI | `feature.telegram.TelegramDestinationScreen` | Renders destination search and selection | No |
| ViewModel | `feature.telegram.TelegramAuthViewModel` | Coordinates authentication actions and exposes domain state | No |
| ViewModel | `feature.telegram.TelegramDestinationViewModel` | Debounces destination search through the domain contract | No |
| Domain contract | `domain.repository.TelegramRepository` | Stable application-facing Telegram operations and state | No |
| Domain models | `domain.model.TelegramConnectionState`, `TelegramDestination`, `TelegramError`, `TelegramUser` | Application-facing state and error values | No |
| Data repository | `data.telegram.repository.TelegramRepositoryImpl` | Delegates Telegram operations to the client abstraction | No |
| Client contract | `data.telegram.client.TelegramClient` | Application-facing client boundary for authentication, destinations, and upload events | No generated-type usage in the contract |
| TDLib adapter | `data.telegram.client.TelegramClientImpl` | Owns TDLib client creation, authorization updates, destination resolution, file upload, and message confirmation | Yes; intentional boundary |
| Upload orchestration | `data.upload.TelegramUploadEngineImpl` | Stages local files and consumes client upload events for the domain upload flow | No direct generated TDLib types |
| DI wiring | Existing Hilt module(s) under `core.di` | Provides and binds the Telegram client/repository implementations | Indirect |

## Boundary decision

The existing `data.telegram.client` and `data.telegram.repository` packages already form a coherent Telegram application-integration boundary. A broad move to a new top-level `telegram/` package would create a large import-only diff and increase rollback risk without improving the current responsibility split. Therefore this phase preserves the existing package locations and documents them as the canonical boundary.

The only class that directly couples to generated TDLib types is `TelegramClientImpl`, which is the correct location for that coupling because it owns client lifecycle, authorization state handling, destination lookup, upload requests, progress updates, and confirmed message delivery. Generic UI, ViewModel, domain, and upload orchestration code depend on application contracts or domain models instead.

## Protected surfaces

The following remain outside this refactoring boundary: `org.drinkless.tdlib.*`, `app/src/main/jniLibs/**`, `libtdjni.so`, generated bindings, native artifacts, ABI configuration, TDLib build scripts and version, authentication behavior, session behavior, logout behavior, upload behavior, WorkManager behavior, credentials, and UI design.
