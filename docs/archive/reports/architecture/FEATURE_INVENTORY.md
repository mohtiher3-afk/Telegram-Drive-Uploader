# Feature Inventory

## Scope

This inventory reflects the current Compose source and navigation graph. It records existing features only; no new screens or routes are introduced.

| Feature | Screen | Route | ViewModel | UI state/events | Dependencies |
|---|---|---|---|---|---|
| ONBOARDING | `OnboardingScreen` | Conditional pre-navigation content | `OnboardingViewModel` | `completed: StateFlow<Boolean>`; callback-based completion | `SettingsDataStore` |
| HOME | `HomeScreen` | `home` | None | Callback-based actions for settings, Telegram connection, and video selection | Navigation callbacks; shared UI components |
| AUTH | `TelegramAuthScreen` | `telegram_auth` | `TelegramAuthViewModel` via Hilt | Repository-backed connection/user/error/QR state plus input flows and processing state | `TelegramRepository` |
| DESTINATION | `TelegramDestinationScreen` | `telegram_destination` | `TelegramDestinationViewModel` via Hilt | Search query, selected destination, connection state, destination list | `TelegramRepository` |
| FILE_SELECTION | Home action into upload preparation | `upload_preparation` | `UploadViewModel` | `UploadUiState`, prepared tasks, smart suggestions, destination, schedule | `VideoMetadataExtractor`, `SmartFileAssistant`, repositories, upload manager |
| UPLOAD_QUEUE | `QueueScreen` | `queue` | Screen-level ViewModel not identified in current inventory | State collected from upload repository/manager through existing screen code | Upload repository and shared UI |
| HISTORY | `HistoryScreen` | `history` | Screen-level ViewModel not identified in current inventory | Existing screen state and repository consumption | Upload repository and shared UI |
| SETTINGS | `SettingsScreen` | `settings` | `SettingsViewModel` | `SettingsUiState` from combined settings flows | `SettingsDataStore`, diagnostics |
| SCHEDULER | No standalone screen found | None | No standalone ViewModel found | Scheduling controls currently live in `UploadScreen`/`UploadViewModel` | Existing upload preparation flow |
| ABOUT | No screen found | None | None | Not implemented as a separate feature | None |
| SHARED | `core.ui.components.*` | N/A | N/A | Reusable composables and formatting helpers | Consumed by multiple feature screens |

## Classification notes

`feature.telegram` currently combines authentication and destination selection. The prompt's conceptual `auth` and `destination` directories are therefore logical classifications, not permission to blindly split files. Any split would change packages and imports across navigation and Hilt consumers and requires a focused, reversible move.

`feature.upload` currently combines file selection/preparation, local metadata extraction coordination, SmartFileAssistant suggestions, destination selection handoff, scheduling, and queue insertion. This is an existing cohesive upload-preparation flow; it must not be split into invented `selectvideo`, `scheduler`, or `uploadqueue` packages during a structural-only phase without a dedicated migration plan.

No separate `about` or standalone scheduler screen exists in the source tree, so neither feature is created.
