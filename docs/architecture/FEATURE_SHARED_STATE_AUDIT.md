# Feature Shared-State Audit

| Shared or long-lived state | Current owner | Consumers | Risk | Recommended future owner |
|---|---|---|---|---|
| Onboarding completion | `OnboardingViewModel` backed by `SettingsDataStore` | `AppNavigation` and `OnboardingScreen` | Medium | Keep behind the onboarding/settings boundary |
| Telegram connection/user/error/QR state | Telegram client/repository state flows | Telegram auth/destination ViewModels and screens; upload preparation reads connection/destination state through contracts | High | Keep in Telegram application layer and domain contracts |
| Upload preparation list | `UploadViewModel` private mutable list | `UploadScreen` through `UploadUiState` | High | Keep in upload feature; do not make global |
| Smart suggestions | `UploadViewModel` private `StateFlow<Map<String, SmartFileSuggestion>>` | `UploadScreen` through existing state/actions | Medium | Keep local to upload preparation or future assistant boundary |
| Selected destination | `UploadViewModel` local state, synchronized through navigation callback | `UploadScreen` and destination screen callback | High | Keep in upload preparation flow until a dedicated state contract exists |
| Persisted upload tasks/status | Room repository and WorkManager/upload engine | Queue, history, worker, upload screen state | High | Keep in data/application upload layers |
| Theme/settings values | `SettingsDataStore` and `SettingsViewModel` | Activity/theme and settings screen | Medium | Keep in settings persistence boundary |

## Findings

No singleton ViewModel, composition-local application state, or static mutable UI state was identified as a target for this structural phase. State is mostly owned by the relevant ViewModel or persisted repository. Cross-feature state is passed through callbacks and domain/application contracts rather than direct feature-to-feature ViewModel references.

The upload preparation ViewModel intentionally owns several related states because it coordinates the existing selection, metadata, suggestion, destination, scheduling, and queue insertion flow. Splitting it without characterization tests would be a behavioral redesign and is deferred.
