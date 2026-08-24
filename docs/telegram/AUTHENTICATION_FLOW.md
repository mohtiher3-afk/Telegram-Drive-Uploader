# Telegram Authentication Flow

This document records the authentication flow implemented by the repository. It is a behavioral inventory, not a proposal to redesign the flow.

## Startup and First Connection

```text
MainActivity
  ↓
TelegramDriveTheme
  ↓
AppNavigation
  ↓
Onboarding completion gate
  ├─ incomplete → OnboardingScreen
  └─ complete → single NavHost, startDestination = home
                       ↓
                 user opens Telegram auth route
                       ↓
                 TelegramAuthScreen
                       ↓
                 TelegramAuthViewModel.connect()
                       ↓
                 TelegramRepositoryImpl
                       ↓
                 TelegramClientImpl.connect()
                       ↓
                 System.loadLibrary("tdjni") once
                       ↓
                 Client.create() once per active client reference
                       ↓
                 AuthorizationState updates
```

The application does not automatically route the app start destination from the persisted Telegram connection marker. The normal navigation graph starts at `home`; Telegram authentication is entered from home, settings, or destination selection. This avoids presenting a login screen merely because TDLib is still initializing, but it also means startup restoration is not a separately verified navigation flow.

## Authorization-State Mapping

| TDLib state handled by the app | Application state/action | UI consequence |
|---|---|---|
| `AuthorizationStateWaitTdlibParameters` | Sends `SetTdlibParameters` with the app’s configured API ID/hash, TDLib database directory, TDLib files directory, language, device model, OS version, and app version. | The screen remains in the connection/loading path until a later state arrives. |
| `AuthorizationStateWaitPhoneNumber` | Clears any QR link and sets `WAITING_FOR_PHONE`. | Phone input and continue action are shown. |
| `AuthorizationStateWaitCode` | Sets `WAITING_FOR_CODE`. | Verification-code input and submission are shown. |
| `AuthorizationStateWaitPassword` | Sets `WAITING_FOR_PASSWORD`. | Masked two-step-verification password input is shown. |
| `AuthorizationStateWaitOtherDeviceConfirmation` | Stores TDLib’s QR link and sets `WAITING_FOR_QR`. | QR-login information and copy behavior are shown when the link is present. |
| `AuthorizationStateReady` | Sets `AUTHORIZED`, requests `GetMe`, and requests the initial chat list. | The auth screen calls its success callback and pops back to the previous route. |
| `AuthorizationStateClosing` | Sets `CLOSING`. | Closing/loading feedback is shown. |
| `AuthorizationStateClosed` | Sets `DISCONNECTED`. | The user is no longer represented as connected. |
| Any other authorization state | Sets `ERROR` with an unknown-state mapping. | An actionable generic error path is shown, but the exact unknown state is not exposed as the UI message. |

Only states observed in the application’s actual handler are listed. The application’s `WAITING_FOR_QR` state corresponds to TDLib’s `AuthorizationStateWaitOtherDeviceConfirmation`.

## Login Inputs

`sendPhoneNumber()` trims the value and requires a non-blank string beginning with `+` before sending `SetAuthenticationPhoneNumber`. The code path does not log or persist the phone input as a login credential. `sendCode()` trims the code, rejects blank input, and sends `CheckAuthenticationCode`; it does not persist or log the code. `sendPassword()` rejects a blank password and sends `CheckAuthenticationPassword`; the ViewModel clears the password field on logout, and the screen uses a password-masked field. No resend-code operation was identified in the reviewed authentication contract.

The UI disables the relevant action while `isProcessing` is true. This is state-based duplicate-submission protection, not an arbitrary delay. Runtime behavior under rapid repeated calls remains a test limitation.

## Error Mapping

TDLib errors are mapped by `TelegramClientImpl.mapError()` to application-level categories: update-required, invalid phone, invalid code, invalid password, rate limited, session expired, network unavailable, or unknown. Error diagnostics include the TDLib error code and message. The reviewed authentication path does not include the phone number, verification code, password, or session token in these diagnostics. The unknown category retains the TDLib message in the domain error and therefore should be reviewed if a future TDLib error could contain sensitive data.

## Logout

```text
Settings confirmation dialog
  ↓
SettingsViewModel.logoutTelegram()
  ↓
TelegramRepositoryImpl.logout()
  ↓
TelegramClientImpl.logout()
  ├─ state = CLOSING
  ├─ send TdApi.LogOut()
  ├─ release the client reference
  ├─ clear current user, QR link, error, chats, and destinations
  ├─ clear cached Telegram user metadata
  └─ state = DISCONNECTED
```

The settings UI explicitly communicates that local upload history and statistics remain intact. No unrelated upload data is intentionally deleted by the reviewed logout code.

## Re-authentication and Recovery Boundary

After logout, the in-memory Telegram state and cached Telegram user metadata are reset. A later connection starts from the client’s disconnected state and may create a new TDLib client. The repository does not contain a dedicated automated proof for logout interruption, process recreation during logout, re-login after logout, or duplicate callback behavior after re-login. These are therefore documented as **NOT VERIFIED**, not claimed as passed.

## Navigation and Source of Truth

`TelegramClientImpl` owns live Telegram authorization state. `TelegramRepositoryImpl` forwards it. `TelegramAuthViewModel` exposes it. `TelegramAuthScreen` consumes it and maps `AUTHORIZED` to route back navigation. `AppNavigation` owns the route graph and does not independently inspect the persisted Telegram marker to declare the user authenticated. The persisted DataStore state and cached user are secondary metadata; they must not replace TDLib’s live authorization state.
