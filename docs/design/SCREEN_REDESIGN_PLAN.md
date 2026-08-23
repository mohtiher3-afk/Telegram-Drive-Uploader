# Screen Redesign Plan

The plan includes only user-facing Compose screens found in the current source tree. Redesign work is intentionally sequential so each screen can be validated before the next one is changed.

| # | Feature | Screen | Current Route | Current State | Priority | Complexity | Status |
|---|---|---|---|---|---|---|---|
| 1 | Home | `HomeScreen` | `home` | Reactive `HomeUiState`; connection, statistics, active uploads, empty state | High | Medium | Next |
| 2 | Telegram | `TelegramAuthScreen` | `telegram_auth` | `TelegramConnectionState` branches, form/loading/error/success paths | High | High | Planned |
| 3 | Telegram | `TelegramDestinationScreen` | `telegram_destination` | Destination loading, search, empty/error, selection | High | High | Planned |
| 4 | Upload | `UploadScreen` | `upload_preparation` | Upload preparation, selected videos, destination, schedule, smart suggestions, errors | High | High | Planned |
| 5 | Queue | `QueueScreen` | `queue` | Queue list, status presentation, empty/content states | High | Medium | Planned |
| 6 | History | `HistoryScreen` | `history` | History list, empty/content states, existing actions | Medium | Medium | Planned |
| 7 | Settings | `SettingsScreen` | `settings` | Settings sections, theme controls, diagnostics and logs | Medium | High | Planned |
| 8 | Onboarding | `OnboardingScreen` | Outside the NavHost gate | Page index, permission request, completion/skip | Medium | Medium | Planned |

No standalone scheduler, about, file-picker, upload-details, or separate splash screen was found. File selection is currently part of `HomeScreen` through the Android document picker. Upload progress is currently rendered by shared components and queue/home surfaces rather than a separate upload-details route.

The redesign order starts with Home because it is the primary entry surface and owns the existing file-picker entry point. Each later screen remains blocked until its preceding slice has been inspected, changed only within its UI boundary, statically checked, and validated through the repository CI workflow.
