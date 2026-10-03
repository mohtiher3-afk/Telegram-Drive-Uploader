# Telegram Authentication Architecture

## Layer Ownership

The repository follows a single-direction ownership model for Telegram authorization.

| Layer | Owner | Responsibility |
|---|---|---|
| TDLib boundary | `TelegramClientImpl` | Owns the nullable `Client`, sends TDLib functions, receives callbacks, and interprets `UpdateAuthorizationState`. |
| Data/repository boundary | `TelegramRepositoryImpl` | Forwards client flows and commands without creating a competing authentication state machine. |
| Domain contract | `TelegramRepository` / `TelegramClient` | Defines the operations and state streams consumed by features. |
| ViewModel | `TelegramAuthViewModel` | Launches user actions in `viewModelScope`, exposes state, and holds transient input fields for the screen lifetime. |
| UI | `TelegramAuthScreen` | Renders the state, collects input, disables actions while processing, and reports authorized success to navigation. |
| Navigation | `AppNavigation` | Owns the single Compose navigation graph and route transitions. It does not declare Telegram authentication from a cached flag. |
| Persistence | `SettingsDataStore` and TDLib directories | Stores cached state/user metadata in DataStore; TDLib stores its session/database/files under app-private files. |

## Source of Truth

Live Telegram authorization is authoritative in TDLib’s authorization-state updates as interpreted by `TelegramClientImpl`. The application-facing live state is `_connectionState`, exposed as `connectionState` and forwarded through the repository to the ViewModel and UI.

The DataStore value `telegram_connection_state` and the cached Telegram user record are persisted metadata. They are not sufficient evidence that Telegram is currently authenticated. The reviewed navigation shell does not use these values to force the user into an authenticated route or to bypass TDLib authorization.

## Client Lifecycle

`TelegramClientImpl` is a Hilt singleton. Its `tdClient` reference is guarded by `clientLock`. `connect()` returns without creating another client when the state is `CONNECTING` or `AUTHORIZED`; otherwise it initializes the native runtime once and creates a `Client` only when the reference is null. `Client.create()` registers one update callback and two failure callbacks at construction time.

The client sends `SetTdlibParameters` when TDLib reports `AuthorizationStateWaitTdlibParameters`. The parameter setup uses app-private directories named `tdlib-database` and `tdlib-files`. `AuthorizationStateReady` triggers `GetMe` and an initial `GetChats` request. `logout()` sends `TdApi.LogOut()`, clears the in-memory reference and application state, clears the cached user, and records `DISCONNECTED`.

The repository contains no explicit `Client.runUpdates()` call, because the Java client callback model is used. No separate Activity-level client or ViewModel-level client was found in the reviewed path. Device-level proof of repeated Activity recreation, process death, and re-login is still unavailable.

## Update Handler Lifecycle

The callback installed by `Client.create()` dispatches authorization, user, chat, file, and message updates through `handleTdLibObject()`. The singleton client centralizes this registration. The reviewed code does not expose a public add/remove listener API or register an Activity reference. The audit cannot prove behavior after process death or an interrupted logout without runtime evidence, so duplicate-listener safety is recorded as repository-supported but runtime-unverified.

## Coroutine and UI Lifecycle

The ViewModel launches user actions in `viewModelScope`. The client uses a `SupervisorJob`-backed `CoroutineScope` for persistence writes and does not tie its lifetime to an Activity. The screen uses lifecycle-aware state collection. Existing `isProcessing` state disables action buttons during submissions. There is no evidence of explicit cancellation of a prior authentication request before starting a new one; the UI guard is the current duplicate-submission control.

## Security Boundary

The reviewed authentication path does not intentionally log phone numbers, verification codes, passwords, or session tokens. TDLib API credentials are read from `BuildConfig`, not hardcoded in the client source. The cached Telegram phone number is stored in DataStore and displayed in the settings account section; this is an existing data-minimization/privacy consideration and was not altered during this review. Unknown TDLib error text is retained in the domain error, so future security review should confirm that raw server messages cannot expose sensitive values.
