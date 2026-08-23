# Startup Tasks

| Task | Current owner | Blocking? | Async? | Required before UI? | Classification | Risk |
|---|---|---:|---:|---:|---|---|
| Application superclass initialization | `TelegramDriveApp.onCreate()` | Yes for process creation | Platform lifecycle | Yes | `CRITICAL_BEFORE_NAVIGATION` | High |
| Activity creation and edge-to-edge setup | `MainActivity.onCreate()` | Yes for composition | No meaningful app work | Yes | `CRITICAL_BEFORE_NAVIGATION` | Medium |
| Read theme preference | `SettingsDataStore.themePreference` collected by `MainActivity` | No; collection supplies an initial `System` value | Flow-backed | No; theme can compose with initial value | `UI_ONLY` | Low |
| Read onboarding completion | `OnboardingViewModel.completed` in `AppNavigation` | No; its initial value is `false` | Flow-backed | Yes for choosing the gate | `CRITICAL_BEFORE_NAVIGATION` | Medium |
| Persist onboarding completion | `OnboardingViewModel.complete()` | No after user action | Coroutine | No | `NON_CRITICAL` | Medium |
| Room database construction | Hilt `DatabaseModule` provider on first injection | No until a database consumer is needed | Lazy provider path | No for first composition | `LAZY` | High |
| Telegram TDLib client creation | `TelegramClientImpl` when its feature path requires it | No for initial home composition | Client-owned coroutine path | No for first composition | `LAZY` | Critical |
| Telegram session restoration | Existing Telegram client/repository path | No for initial onboarding/home rendering | Client-owned asynchronous path | No for first composition | `BACKGROUND` / feature-owned | Critical |
| WorkManager acquisition | Hilt `WorkModule` via `WorkManager.getInstance(context)` | No until injected/used | Platform-managed | No for initial composition | `LAZY` | High |
| Notification channel initialization | No separate startup channel creation was found in the inspected sources | Not applicable | Not applicable | No | `NON_CRITICAL` | Low |
| File system or media scanning | No global startup scan was found | Not applicable | Not applicable | No | `LAZY` / feature-owned | Medium |

## Classification decision

The application currently performs a small startup path. DataStore flows provide real initial values, while database, WorkManager, Telegram, and upload services remain dependency- and feature-driven rather than eagerly initialized by the Activity. No expensive task should be moved into `MainActivity` merely to make the startup sequence appear explicit.
