# Feature Dependency Map

## Actual dependency flow

```text
Onboarding
  ↓
OnboardingViewModel
  ↓
SettingsDataStore

Home
  ↓ callbacks
AppNavigation
  ↓
Upload preparation / Telegram auth / Settings

Telegram Auth and Destination
  ↓
Telegram ViewModels
  ↓
TelegramRepository contract
  ↓
TelegramRepositoryImpl
  ↓
TelegramClient contract
  ↓
TelegramClientImpl / official TDLib

Upload preparation
  ↓
UploadViewModel
  ├─ VideoMetadataExtractor / VideoFormatSupport
  ├─ SmartFileAssistant / SmartFileSuggestion
  ├─ UploadRepository
  ├─ UploadManager
  └─ TelegramRepository for destination state

Queue and History
  ↓
Existing upload repository and application upload state
  ↓
Room / WorkManager / upload engine

Settings
  ↓
SettingsViewModel
  ↓
SettingsDataStore and DiagnosticsManager
```

## Feature-to-boundary table

| Feature | ViewModel | Application boundary | Repository/data dependency | Cross-feature dependency |
|---|---|---|---|---|
| Onboarding | `OnboardingViewModel` | Settings persistence | `SettingsDataStore` | App shell only |
| Home | None | Callback shell | None directly | Starts upload, settings, or auth routes |
| Telegram auth | `TelegramAuthViewModel` | Telegram repository | `TelegramRepository` → `TelegramClient` → TDLib | Returns to caller on success |
| Destination | `TelegramDestinationViewModel` | Telegram repository | `TelegramRepository` → `TelegramClient` → TDLib | Returns selected destination to upload preparation |
| Upload preparation | `UploadViewModel` | Upload manager, upload repository, media utilities, local Smart Assistant | Room-backed upload repository; `UploadManager`; media utilities; `SmartFileAssistant` | Receives destination from Telegram feature |
| Queue | Existing screen code | Upload state | Upload repository / WorkManager-backed flow | Bottom navigation only |
| History | Existing screen code | Upload state | Upload repository | Bottom navigation only |
| Settings | `SettingsViewModel` | Settings persistence and diagnostics | `SettingsDataStore`, `DiagnosticsManager` | Can enter Telegram auth |

## Boundary assessment

No feature depends directly on another feature's ViewModel or internal composable. Cross-feature coordination occurs through `AppNavigation` callbacks and domain/application contracts. The upload feature's use of the local SmartFileAssistant is an existing dependency and does not create a Telegram or network dependency.
