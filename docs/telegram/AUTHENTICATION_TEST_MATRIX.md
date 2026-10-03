# Telegram Authentication Test Matrix

**Rule:** Only repository evidence and completed automated checks are recorded as actual. Real Telegram authentication is not claimed without a connected device or emulator and a real test account.

| Scenario | Expected | Actual | Status |
|---|---|---|---|
| Fresh start | Onboarding gate appears when incomplete; otherwise the normal home route is rendered. | `AppNavigation` gates on onboarding completion and uses `home` as the NavHost start destination. | PASS — repository verified |
| Wait for TDLib parameters | TDLib parameters are sent after `AuthorizationStateWaitTdlibParameters`. | `sendTdlibParameters()` creates app-private TDLib directories and sends `SetTdlibParameters`. | PASS — repository verified |
| Phone number | Non-blank `+`-prefixed input is submitted; invalid input maps to an actionable error. | `TelegramClientImpl.sendPhoneNumber()` trims and validates before `SetAuthenticationPhoneNumber`; UI disables while processing. | PASS — repository verified; runtime unverified |
| Verification code | Non-blank code is submitted and wrong/expired errors are recoverable. | `sendCode()` trims and rejects blank input; `CheckAuthenticationCode` is sent. Error mapping exists. | CONDITIONAL — error/runtime behavior unverified |
| 2FA password | Password is masked, blank input is rejected, and failure is recoverable. | Screen masks the field; client rejects blank input and sends `CheckAuthenticationPassword`. | PASS — repository verified; runtime unverified |
| QR login | TDLib-provided link is rendered and can be copied when available. | `AuthorizationStateWaitOtherDeviceConfirmation` stores the link and maps to `WAITING_FOR_QR`. | PASS — repository verified; runtime unverified |
| Ready | `AuthorizationStateReady` marks the account authorized and loads user/chats. | Client sets `AUTHORIZED`, sends `GetMe`, requests chats, and the auth screen pops on success. | PASS — repository verified; runtime unverified |
| Restart/session restoration | Previously authenticated user remains authenticated after process restart. | TDLib receives stable app-private database/files directories; no real restart test was available. | NOT VERIFIED |
| Logout | Logout reaches closing/disconnected, clears cached account state, and preserves unrelated upload history. | `logout()` sends `TdApi.LogOut`, clears client/user/chat state and cached user, then records `DISCONNECTED`; settings copy preserves history. | PASS — repository verified; interruption unverified |
| Re-login | A user can authenticate again without stale client, state, or listeners. | Reset code exists; no real re-login test or duplicate-listener test was available. | NOT VERIFIED |
| Network loss | Temporary network loss is distinct from unauthentication and has a recoverable UI path. | Network errors are mapped; no dedicated network recovery state machine or runtime test was found. | NOT VERIFIED |
| Network restore | Restored connectivity does not force an unnecessary login. | Not demonstrated by automated or device evidence. | NOT VERIFIED |
| Authentication error | TDLib errors map to localized/actionable UI categories without credential leakage. | Mapping covers update-required, phone, code, password, rate limit, expired session, network, and unknown. Diagnostics do not intentionally log credentials. | CONDITIONAL — raw unknown-message privacy needs ongoing review |
| JNI/client runtime | Native library loads and `Client.create()` can be reached. | Existing Android smoke test covers `System.loadLibrary("tdjni")` and `Client.create()` only. | PASS — smoke-test scope; device evidence required |
| Authenticated upload regression | Authenticated state, destination loading, file selection, and upload start work with real auth. | Not run against a real authenticated Telegram session in this phase. | NOT VERIFIED |
