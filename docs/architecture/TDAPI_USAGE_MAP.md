# Direct TdApi Usage Map

## Scope

This document classifies application files that directly reference official TDLib Java bindings or client APIs. It is an analysis artifact for the Telegram isolation phase; it does not alter generated bindings or runtime behavior.

## Usage inventory

| Class | Direct usage observed | Classification | Recommended action |
|---|---|---|---|
| `data.telegram.client.TelegramClientImpl` | Imports `org.drinkless.tdlib.Client` and `org.drinkless.tdlib.TdApi`; calls `Client.create`, sends authorization requests, resolves chats, creates input media, calls `UploadFile`/`SendMessage`, consumes `UpdateFile` and message delivery updates, and handles close/error states | `DIRECT_TDLIB_REQUIRED` and `TELEGRAM_APPLICATION_LOGIC` | Keep as the single TDLib adapter boundary. Do not split or wrap without a behavior-preserving test plan. |
| `data.telegram.repository.TelegramRepositoryImpl` | Delegates to `TelegramClient`; exposes domain state and operations; no generated TdApi import | `TELEGRAM_APPLICATION_LOGIC` | Keep in the existing `data.telegram.repository` package. |
| `data.upload.TelegramUploadEngineImpl` | Uses `TelegramClient` and `TelegramUploadEvent`; stages files and translates client events to upload-engine results; no generated TdApi import | `GENERIC_APPLICATION_LOGIC` with Telegram-specific orchestration | Keep outside the raw TDLib boundary. Depend on the client contract only. |
| `domain.repository.TelegramRepository` | Application contract for connection, authentication, destination lookup, and logout; no generated TdApi import | `GENERIC_APPLICATION_LOGIC` | Keep as the domain-facing abstraction. |
| `feature.telegram.*` | Uses domain repository and domain models; no generated TdApi import identified | `UI_LEAK` not observed | Keep raw TDLib types out of these packages. |
| `org.drinkless.tdlib.*` generated sources | Generated `Client`, `TdApi`, and `Log` bindings | `DIRECT_TDLIB_REQUIRED` but protected | Do not modify, regenerate, duplicate, or relocate. |

## Boundary assessment

The current source inventory shows one application class with direct generated-type coupling: `TelegramClientImpl`. This is intentional and appropriate because the class owns TDLib client construction, native-runtime activation, authorization state transitions, destination resolution, upload requests, progress updates, and confirmed delivery handling.

The repository and upload-engine layers use application contracts or event models instead of generated TdApi types. The UI and ViewModel layers use domain models and repository contracts. No broad package move is required to achieve a safer TDLib boundary in this phase.

## Required regression checks

Any future change to this boundary must preserve the existing authorization states actually handled by the implementation, including `AuthorizationStateWaitTdlibParameters`, `AuthorizationStateWaitPhoneNumber`, `AuthorizationStateWaitCode`, `AuthorizationStateWaitPassword`, `AuthorizationStateReady`, and `AuthorizationStateClosed` where present. It must also preserve real `UpdateFile` progress and confirmed message delivery semantics. Build success alone is not runtime proof of JNI loading or Telegram authorization.
