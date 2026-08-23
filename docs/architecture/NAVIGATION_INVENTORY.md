# Navigation Inventory

| Route | Screen | Entry point | Arguments | Start destination | Authentication required |
|---|---|---|---|---|---|
| `home` | `HomeScreen` | `AppNavigation` after onboarding completion | None | Yes, after onboarding gate | No route guard; Telegram actions remain available through existing UI |
| `queue` | `QueueScreen` | Bottom navigation bar/rail | None | No | No route guard |
| `history` | `HistoryScreen` | Bottom navigation bar/rail | None | No | No route guard |
| `settings` | `SettingsScreen` | Bottom navigation or Home callback | None | No | No route guard |
| `upload_preparation` | `UploadScreen` | Home video-selection callback | Selected URIs are held by the existing `UploadViewModel`, not encoded in the route | No | No route guard; upload flow validates its own prerequisites |
| `telegram_auth` | `TelegramAuthScreen` | Home, Settings, or Destination connect callback | None | No | Screen performs the existing Telegram authorization flow |
| `telegram_destination` | `TelegramDestinationScreen` | Upload preparation destination callback | None | No | Screen exposes existing connection/search state |

## Route definitions

All route strings are now defined in `core.navigation.AppRoutes`. Existing values remain unchanged: `home`, `queue`, `history`, `settings`, `upload_preparation`, `telegram_auth`, and `telegram_destination`. No deep-link URI or typed route argument exists in the inspected source.

## Saved-state impact

The bottom navigation uses `popUpTo(findStartDestination())` with `saveState = true`, `launchSingleTop = true`, and `restoreState = true`. The upload, authentication, and destination routes use existing callback navigation and `popBackStack()` behavior. No route rename or argument encoding change was introduced.
