# Telegram Session Lifecycle

## Lifecycle Model

```text
No active client / DISCONNECTED
        ↓ connect()
CONNECTING
        ↓ AuthorizationStateWaitTdlibParameters
TDLib parameters sent
        ↓
WAITING_FOR_PHONE / WAITING_FOR_CODE / WAITING_FOR_PASSWORD / WAITING_FOR_QR
        ↓ successful TDLib authentication
AUTHORIZED / AuthorizationStateReady
        ↓ user logout
CLOSING / AuthorizationStateClosing
        ↓
DISCONNECTED / AuthorizationStateClosed
        ↓ connect()
Re-authentication path
```

## Session Restoration

TDLib is configured with app-private `filesDir/tdlib-database` and `filesDir/tdlib-files` directories. This indicates that session/database persistence is delegated to TDLib’s local storage rather than to plaintext credentials managed by the app. The application also writes a cached connection-state marker and cached user metadata to Android DataStore.

The repository inspection supports the intended restoration mechanism, but no connected Android device or emulator with a previously authenticated test account was available for proof. Therefore, the following sequence remains **NOT VERIFIED**:

```text
Authenticated account → process killed → app reopened → TDLib restores → Ready
```

The application does not use the cached `AUTHORIZED` marker as a substitute for a live TDLib `AuthorizationStateReady` update.

## Logout and Re-login

The logout path sends `TdApi.LogOut()`, clears the in-memory user and chat state, clears the QR link and error state, clears cached Telegram user metadata, and records `DISCONNECTED`. Upload history and statistics are intentionally not cleared by the settings flow.

Repository inspection supports reset of the in-memory and cached application state. Real-device logout, interruption during logout, TDLib storage invalidation, and re-login after logout remain **NOT VERIFIED**.

## Connection and Network Loss

The client maps TDLib errors with codes 500–599 to `NetworkUnavailable`, code 420 to `RateLimited`, and code 401 to `SessionExpired`, with additional message-based mappings for phone, code, password, and update-required errors. The reviewed code does not route a transient network error directly to the login screen. It does not contain a separately documented network-recovery state machine or an automated network-loss/recovery test.

A temporary disconnection must not be treated as unauthentication unless TDLib reports an authorization state that requires it. The current implementation’s exact runtime behavior during startup loss, authentication loss, and recovery needs device/network validation.

## Process Recreation and Interruption

No explicit recovery transaction was found for process death during `connect()`, phone/code/password submission, QR login, or logout. No second persistence mechanism was introduced to compensate. These cases are maintained as known validation gaps rather than filled with speculative recovery logic.

## Account Model

The application currently represents one Telegram account through one singleton client, one current-user flow, one cached user record, and one TDLib storage location. **MULTI-ACCOUNT NOT SUPPORTED.** Account switching was not added.

## Lifecycle Safety Decision

The architecture is sufficiently explicit for controlled maintenance: TDLib owns live authorization, the singleton client owns the callback lifecycle, the repository forwards state, the ViewModel owns transient UI actions, and the navigation shell owns routes. Runtime lifecycle certification is withheld until real-device evidence covers restoration, logout, re-login, and network interruption.
