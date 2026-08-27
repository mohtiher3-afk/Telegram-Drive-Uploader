# Authentication and Destination Audit — August 2026

## Scope

This audit rechecks the current `main` source for Telegram configuration, TDLib authorization, destination discovery, selection, persistence, and handoff to queued uploads. It is a source audit only; it does not claim device login, channel permission, session restoration, or real delivery.

## Repository-verified findings

| Boundary | Current behavior | Evidence |
|---|---|---|
| Client ownership | `TelegramClientImpl` is a Hilt singleton and guards nullable `Client` creation with `clientLock`. | `data/telegram/client/TelegramClientImpl.kt` |
| Credential gate | `isConfigured` requires a positive numeric API ID and nonblank, non-placeholder hash. `connect()` fails with `InvalidCredentials` before native initialization when the gate is not satisfied. | `TelegramClientImpl.kt:73-85` |
| Native failure mode | `System.loadLibrary("tdjni")` is invoked through an atomic one-time guard before `Client.create()`; failure clears the client and maps to `TdLibRuntimeUnavailable`. | `TelegramClientImpl.kt:90-112,545-553` |
| Authorization source of truth | TDLib authorization updates drive the app connection state through parameters, phone, code, password, QR, ready, closing, and closed states. | `TelegramClientImpl.kt:349-402` |
| Destination discovery | Authorized text search uses TDLib `SearchPublicChat` and `SearchChatsOnServer`; normal not-found search errors are not mapped to authentication errors. | `TelegramClientImpl.kt:180-214` |
| Permission filtering | Destination projection filters to sendable chats before it emits `TelegramDestination`; administrator channels require `canPostMessages`. | `TelegramClientImpl.kt:467-503` |
| Stable identity | UI list keys, selection checks, pinning, and task insertion use `TelegramDestination.id` / `Long`, not title or list position. | `TelegramDestinationScreen.kt:252-259`; `TelegramDestinationViewModel.kt:60-63`; `UploadViewModel.kt:138-153` |
| Pin persistence | DataStore encodes a `Set<Long>` of pinned destination IDs. | `SettingsDataStore.kt:59-61,84-90` |
| Safe user-error rendering | Tests prove known Telegram errors map to localized resources and unknown user-facing errors map to a generic resource. | `TelegramErrorTest.kt` |

## Confirmed configuration and privacy boundaries

The Debug build’s absent or invalid Telegram API configuration is a real fail-closed configuration condition, not a visual failure. This audit did not add an API ID, API hash, test account, phone number, code, password, or session artifact.

The application persists a Telegram user metadata record, including phone number, after a real authorized user update. The reviewed UI error mapping intentionally renders a generic localized message for `TelegramError.Unknown`; however, TDLib diagnostic context remains a continuing privacy-review surface. No source change is made in this phase because the current audit does not establish a new leak in the configured diagnostic destination.

## Test coverage and evidence gaps

| Scenario | State | Reason |
|---|---|---|
| Error resource mapping | Repository verified | `TelegramErrorTest` covers known and unknown resource selection. |
| Credential gate and auth-state transitions | Repository verified, no focused unit test identified | The behavior is visible in the singleton client; no runtime account is available. |
| TDLib native loading and `Client.create()` | Not verified in this phase | Requires smoke/instrumentation evidence on an emulator/device. |
| Login, QR, code, 2FA, logout, re-login, and session restoration | Not verified | Require a user-controlled account and Android runtime. |
| Channel results and write permission | Not verified | Require an authorized test account and controlled channel. |
| Destination ID through real Worker/TDLib delivery | Not verified | Static handoff is present; real queue execution and delivery need device evidence. |

## Decision

No authentication or destination repair is justified from the current source audit. The runtime clients, credentials, numeric identity, and destination permission boundaries remain protected. The next phase may audit the queued-upload state writers independently; it must not reinterpret a build result as proof of Telegram authorization or destination permission.

## References

[1]: https://core.telegram.org/tdlib/docs/ "TDLib documentation"
[2]: https://developer.android.com/topic/libraries/architecture/datastore "Android DataStore documentation"
