# Telegram Architecture

## Actual flow

```text
TelegramAuthScreen / TelegramDestinationScreen
        ↓
TelegramAuthViewModel / TelegramDestinationViewModel
        ↓
TelegramRepository contract
        ↓
TelegramRepositoryImpl
        ↓
TelegramClient contract
        ↓
TelegramClientImpl
        ↓
org.drinkless.tdlib.Client and TdApi
        ↓
Official TDLib native runtime
        ↓
Telegram
```

`TelegramClientImpl` owns client creation, TDLib parameter setup, authorization-state observation, request callbacks, update handling, and controlled shutdown. The repository adapts those operations to domain models such as `TelegramConnectionState`, `TelegramDestination`, and `TelegramError`.

Authorization is state-driven. Phone number, verification code, password, QR/other-device confirmation, ready, closing, and closed states must remain distinct. Destination search and `GetChat`/`GetSupergroup` errors are destination concerns and must not overwrite authentication state. Native-load failure remains fail-closed.

The client is protected infrastructure because Java bindings, native libraries, database paths, and callback lifecycle must remain version-compatible. Any future change requires TDLib artifact, lifecycle, and device-smoke validation.
