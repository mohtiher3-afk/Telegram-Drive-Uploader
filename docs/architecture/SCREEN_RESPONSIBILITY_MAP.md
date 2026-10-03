# Screen Responsibility Map

| Screen | UI | Business logic | Navigation | Telegram | Upload | Database | File access | Risk |
|---|---|---|---|---|---|---|---|---|
| `OnboardingScreen` | Renders first-run content and permission/onboarding actions | Delegated to `OnboardingViewModel` and settings persistence | Completion callback is supplied by navigation shell | No direct calls | No direct calls | No direct access | No direct access | Medium |
| `HomeScreen` | Renders home dashboard and selection actions | Minimal callback dispatch | Emits callbacks to settings, Telegram auth, and upload preparation | No direct calls | Starts selection callback | No direct access | Launches existing picker callback path | Low |
| `TelegramAuthScreen` | Renders auth state, phone/code/password/QR controls | Delegated to `TelegramAuthViewModel` | Back and auth-success callbacks | Through `TelegramRepository` via ViewModel | None | None | None | High |
| `TelegramDestinationScreen` | Renders search, destination list, and selection | Delegated to `TelegramDestinationViewModel` | Back, connect, and destination-selected callbacks | Through `TelegramRepository` via ViewModel | Supplies destination to upload flow through navigation callback | None | None | High |
| `UploadScreen` | Renders selected videos, suggestions, destination, schedule, progress/preparation states | Delegated to `UploadViewModel`, with existing scheduling and suggestion actions | Back, destination navigation, queue-added navigation | Indirectly through destination selection | Coordinates upload preparation and queue handoff through ViewModel | Indirectly through ViewModel | Picker data arrives through callback; no raw resolver logic in screen | High |
| `QueueScreen` | Renders persisted upload queue | Existing screen-level orchestration only | Bottom navigation route `queue` | Indirect | Existing upload repository/manager flow | Indirect | None identified | Medium |
| `HistoryScreen` | Renders upload history | Existing screen-level orchestration only | Bottom navigation route `history` | Indirect | Existing upload state | Indirect | None identified | Medium |
| `SettingsScreen` | Renders settings controls and diagnostics actions | Delegated to `SettingsViewModel` | Connect callback to Telegram auth | Indirect through callback | None | Indirect through settings/diagnostics | None | Medium |

## Boundary findings

No screen directly imports generated `TdApi` or `Client` types. `UploadScreen` and `UploadViewModel` are intentionally close to upload preparation because they own the existing user flow; separating them during this phase would risk changing scheduling, smart suggestions, destination selection, and queue insertion.

The `TelegramAuthScreen` and `TelegramDestinationScreen` are logically distinct screens but remain under the existing `feature.telegram` package to avoid a broad package-only split that would touch navigation and Hilt construction without an identified behavioral benefit.
