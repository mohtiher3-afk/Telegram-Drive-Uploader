# Telegram Authentication Inventory

**Repository state:** Production / Controlled Maintenance Mode  
**Audit scope:** Telegram authentication, TDLib client lifecycle, session persistence, UI ownership, and security boundaries.  
**Audit decision:** Documentation-only review. No authentication architecture, TDLib artifact, JNI artifact, upload behavior, or database schema was changed.

## Inventory

| Component | Location | Responsibility | State | Persistence | Risk / Review Note |
|---|---|---|---|---|---|
| Application entry point | `app/src/main/java/com/telegramdrive/uploader/MainActivity.kt` | Applies the persisted theme and renders `AppNavigation`; it does not decide Telegram authorization. | UI startup | Theme only through `SettingsDataStore` | Telegram auth is not owned by the Activity. |
| Navigation shell | `app/src/main/java/com/telegramdrive/uploader/core/navigation/AppNavigation.kt` | Gates onboarding, owns the single navigation graph, and routes to Telegram auth/destination screens. | Onboarding completion and current route | Onboarding completion through `SettingsDataStore` | Telegram auth is a route, not the app start destination. |
| Telegram domain contract | `app/src/main/java/com/telegramdrive/uploader/domain/repository/TelegramRepository.kt` | Exposes connection, user, error, QR link, and Telegram commands to the UI layer. | Repository-facing `StateFlow`s | None directly | Thin boundary; it does not create a second auth authority. |
| Repository adapter | `app/src/main/java/com/telegramdrive/uploader/data/telegram/repository/TelegramRepositoryImpl.kt` | Forwards Telegram operations and state to `TelegramClient`. | Same client-backed state | None directly | No competing persistence or state machine was found here. |
| TDLib client owner | `app/src/main/java/com/telegramdrive/uploader/data/telegram/client/TelegramClientImpl.kt` | Singleton client owner; loads `tdjni`, creates one `Client`, sends TDLib functions, dispatches updates, maps authorization states, and exposes state flows. | `TelegramConnectionState`, user, error, QR link | TDLib database/files directories plus cached user/state metadata | Client lifecycle is centralized, but abrupt-process and real-device lifecycle behavior remain unverified. |
| TDLib client instance | `TelegramClientImpl.tdClient` | Holds the single nullable `Client` reference guarded by `clientLock`. | `null` or active client | TDLib manages session data in app files | `connect()` prevents creation while `CONNECTING` or `AUTHORIZED`; behavior after other states is documented as a residual risk rather than assumed safe. |
| TDLib update dispatcher | `TelegramClientImpl.handleTdLibObject()` | Routes `UpdateAuthorizationState`, chat/file/message updates, user objects, and errors. | Callback-driven | No update persistence | Callback errors are diagnostic-only; duplicate listener registration was not found in the reviewed client. |
| Authentication ViewModel | `app/src/main/java/com/telegramdrive/uploader/feature/telegram/TelegramAuthViewModel.kt` | Exposes repository flows and launches connect, phone, code, password, QR, logout, and error-clear operations in `viewModelScope`. | UI processing flag plus repository state | Input fields survive rotation through ViewModel lifetime; credentials are not deliberately persisted by the ViewModel. | Each action sets a local processing flag; repeated calls are state-disabled in the UI but concurrency behavior is not device-verified. |
| Authentication screen | `app/src/main/java/com/telegramdrive/uploader/feature/telegram/TelegramAuthScreen.kt` | Consumes connection state and renders connect, phone, code, QR, password, error, closing, and authorized states. | State-driven Compose UI | None directly | Password is masked; phone/account data is visible where the UI intentionally renders it. |
| Settings account section | `app/src/main/java/com/telegramdrive/uploader/feature/settings/SettingsScreen.kt` | Shows current Telegram account metadata and initiates confirmed logout. | Repository-backed authorized/disconnected state | Cached account metadata is displayed from the Telegram ViewModel/client path | Phone number exposure in settings should be treated as an intentional privacy consideration. |
| Settings persistence | `app/src/main/java/com/telegramdrive/uploader/data/local/datastore/SettingsDataStore.kt` | Stores a string connection-state marker and cached Telegram user fields. | Default marker is `DISCONNECTED` | Android DataStore preferences | Persisted state is a cache/UX aid, not authoritative authentication; TDLib state remains authoritative. |
| TDLib parameter setup | `TelegramClientImpl.sendTdlibParameters()` | Creates `filesDir/tdlib-database` and `filesDir/tdlib-files`, then sends `SetTdlibParameters` with configured API values and app/device metadata. | Initialization stage | TDLib database and file directories | Directory creation and real restoration need device validation. |
| Logout | `TelegramClientImpl.logout()` plus `TelegramAuthViewModel.logout()` | Sends `TdApi.LogOut`, clears client/user/chat/QR/error state, clears cached user metadata, and writes `DISCONNECTED`. | `CLOSING` then `DISCONNECTED` | Cached user cleared; TDLib logout semantics apply | Interruption during logout is not covered by an automated test. |
| Runtime smoke test | `app/src/androidTest/java/com/telegramdrive/uploader/tdlib/TdLibRuntimeSmokeTest.kt` | Loads `tdjni`, calls `Client.create()`, observes authorization-state class names, and closes the client. | Runtime boundary only | None | Does not prove real login, restoration, logout, re-login, or upload. |

## Scope Boundaries

The repository does not implement multi-account support. The reviewed architecture exposes one singleton `TelegramClientImpl`, one current user, one Telegram session directory, and one set of cached Telegram user fields. **MULTI-ACCOUNT NOT SUPPORTED.**

No authentication secret is intentionally logged by the reviewed client. Phone numbers, verification codes, and passwords are passed to TDLib but are not included in the client’s diagnostic messages. The account phone number is cached in DataStore and rendered in the settings account section; this is an existing behavior documented for privacy review, not changed during this phase.

## Evidence Boundary

Repository inspection supports the architecture and state-transition descriptions above. It does not establish real-device success. Real Telegram login, session restoration after process death, logout interruption recovery, re-login, network-loss recovery, and end-to-end authenticated upload remain **NOT VERIFIED** until a connected device or emulator with a real Telegram test account is available.
