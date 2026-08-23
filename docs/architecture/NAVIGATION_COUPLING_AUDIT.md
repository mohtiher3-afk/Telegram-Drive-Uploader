# Navigation Coupling Audit

## Current navigation owner

`core.navigation.AppNavigation` owns the `NavHostController`, bottom navigation, adaptive rail/bar selection, start destination, routes, and cross-screen callbacks. This is an intentional application-shell responsibility.

| Location | Coupling observed | Classification | Recommendation |
|---|---|---|---|
| `AppNavigation` | Direct `NavHostController` usage and route declarations | LOW | Keep navigation ownership in the navigation shell. |
| `HomeScreen` | Receives callbacks for settings, Telegram auth, and upload preparation | LOW | Keep callback-based boundary. |
| `UploadScreen` | Receives back, destination, and queue-completed callbacks | MEDIUM | Keep callbacks; do not move navigation into the ViewModel. |
| `TelegramAuthScreen` | Receives back and auth-success callbacks | MEDIUM | Keep callbacks and preserve auth behavior. |
| `TelegramDestinationScreen` | Receives back, connect, and selected-destination callbacks | MEDIUM | Keep callbacks; destination selection is returned to the upload flow. |
| `SettingsScreen` | Receives connect callback | LOW | Keep callback-based navigation. |
| Reusable `core.ui.components.*` | No route ownership identified in the inspected source | LOW | Keep reusable components navigation-agnostic. |
| ViewModels | No direct `NavController` usage identified in the inspected feature ViewModels | LOW | Preserve this boundary. |

## Routes currently present

`home`, `queue`, `history`, `settings`, `upload_preparation`, `telegram_auth`, and `telegram_destination`. The bottom navigation includes only `home`, `queue`, `history`, and `settings`.

## Risk summary

No navigation redesign is justified in this phase. The main medium-risk coupling is callback orchestration between upload preparation, destination selection, and queue completion. A route or package rename would require coordinated updates in `AppNavigation` and feature imports and should be a separate reversible change.
