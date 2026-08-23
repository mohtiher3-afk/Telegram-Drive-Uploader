# Screen Functional Contracts

## HomeScreen

### Existing functionality

Displays the application title, Telegram connection status and user identity, local upload statistics, active uploads, and the empty active-upload state. It launches the Android `OpenMultipleDocuments` picker with a broad MIME filter and forwards selected URIs through `onVideosSelected`.

### Existing actions

Settings opens the settings route. Connect opens Telegram authentication. Select videos launches the document picker.

### Existing navigation

Navigation is callback-owned by `AppNavigation`; the screen does not navigate directly.

### Existing state

`HomeViewModel` exposes `HomeUiState` from upload and Telegram repository flows. It derives total count, total size, completed count, pending count, active uploads, recent activity, connection state, and current Telegram user.

### Existing dependencies

`HomeViewModel`, `UploadRepository`, `TelegramRepository`, `UploadStatusIndicator`, `VideoItem`, Material 3 components, and Android document picker APIs.

### Must remain unchanged

Picker contract, test tags, callback semantics, status derivation, real upload progress presentation, and repository ownership.

## TelegramAuthScreen

### Existing functionality

Presents the existing Telegram authentication stages and user inputs, with state-specific loading, errors, retry, and success behavior.

### Existing actions

Phone/code/password submission, retry, clipboard-related action where present, and back navigation through existing callbacks.

### Existing navigation

Route is `telegram_auth`; route ownership remains in `AppNavigation`.

### Existing state

`TelegramConnectionState` drives the `AnimatedContent` branches and form availability.

### Existing dependencies

`TelegramAuthViewModel`, domain authentication models, Hilt ViewModel injection, Material 3 input/buttons, and official TDLib-backed repository contracts.

### Must remain unchanged

Authentication sequence, credentials handling, state transitions, error text semantics, and TDLib integration.

## TelegramDestinationScreen

### Existing functionality

Loads Telegram destinations, supports search, shows empty/error/loading states where implemented, and allows selecting and confirming a destination.

### Existing actions

Back, connect when unauthenticated, change/clear search, select destination, and continue with the selected destination.

### Existing navigation

Route is `telegram_destination`; all route transitions remain callback-owned.

### Existing state

Destination list, search query, selected destination, loading, error, and authorization-dependent content.

### Existing dependencies

`TelegramDestinationViewModel`, Telegram repository contract, destination domain models, and Material 3 list/input components.

### Must remain unchanged

Search behavior, destination identity, selection rules, and authorization requirements.

## UploadScreen

### Existing functionality

Displays prepared files, selected destination, optional schedule, SmartFileAssistant suggestions, preparation errors, and the real upload action.

### Existing actions

Back, select destination, clear schedule, apply smart suggestions, remove or manage prepared videos where present, and start upload.

### Existing navigation

Route is `upload_preparation`; transitions are controlled by `AppNavigation` callbacks.

### Existing state

`UploadUiState`, selected destination, scheduled timestamp, and `smartSuggestions` flows.

### Existing dependencies

`UploadViewModel`, `VideoMetadataExtractor`, `SmartFileAssistant`, upload repository/domain models, and Material 3 components.

### Must remain unchanged

URI preparation, metadata validation, suggestion application, schedule values, upload request creation, and real progress semantics.

## QueueScreen

### Existing functionality

Displays repository-backed queue items and their current statuses.

### Existing actions

Only actions actually exposed by the current queue screen may be retained; no new pause, cancel, or retry action may be invented.

### Existing navigation

Route is `queue` and remains part of the existing bottom navigation graph.

### Existing state

Queue collection with empty/content branches and domain upload statuses.

### Existing dependencies

Queue ViewModel, upload repository contract, `VideoItem`, `UploadStatusIndicator`, and Material 3 list components.

### Must remain unchanged

Status labels, ordering, stable item keys, and repository-derived state.

## HistoryScreen

### Existing functionality

Displays historical upload records with the existing empty/content behavior and any actions already exposed by the screen.

### Existing actions

Only current history actions may remain; no open/details/retry/delete affordance may be added without source evidence.

### Existing navigation

Route is `history` in the existing bottom navigation graph.

### Existing state

History collection and its empty/content branches.

### Existing dependencies

History ViewModel/repository contract, upload domain models, and Material 3 list components.

### Must remain unchanged

Record identity, timestamps, status, destination, and existing action callbacks.

## SettingsScreen

### Existing functionality

Displays application settings, theme controls, and diagnostic events/log information.

### Existing actions

Existing theme preference changes, diagnostic interactions, and navigation/back actions.

### Existing navigation

Route is `settings`, entered from Home and existing app navigation callbacks.

### Existing state

Settings ViewModel state, theme preference, and diagnostic event collection.

### Existing dependencies

`SettingsViewModel`, `SettingsDataStore`, diagnostic sources, Material 3 semantic colors, and shared theme tokens.

### Must remain unchanged

DataStore keys, theme persistence, diagnostic content, and log redaction behavior.

## OnboardingScreen

### Existing functionality

Shows the three existing onboarding pages, supports skip/continue, requests the current media permission when needed, and completes the persisted onboarding gate.

### Existing actions

Skip, continue, choose permissions, and system permission-result handling.

### Existing navigation

Onboarding is rendered outside the regular `NavHost` as the first-launch gate.

### Existing state

Remembered page index and persisted completion via `OnboardingViewModel`.

### Existing dependencies

Android permission launcher, `OnboardingViewModel`, existing theme, strings, icons, and motion tokens.

### Must remain unchanged

Permission contract, completion timing, page count/content, and first-launch routing.
