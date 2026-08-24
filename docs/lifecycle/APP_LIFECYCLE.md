# Application Lifecycle

## Actual Flow

`Process start → TelegramDriveApp.onCreate → Hilt graph/WorkManager configuration → MainActivity.onCreate → Compose content/theme → AppNavigation → screen/ViewModel → background/foreground or Activity recreation → process death → Android restart`

`TelegramDriveApp` is the process-level `Application` and provides the Hilt `WorkerFactory` to WorkManager. Its `onCreate` records an application-start diagnostic. It also receives memory callbacks. No TDLib client is initialized directly in `MainActivity`.

`MainActivity.onCreate` enables edge-to-edge and sets Compose content. It collects the theme preference from `SettingsDataStore` with a lifecycle-aware collector and renders `TelegramDriveTheme`, a `Surface`, and one `AppNavigation` instance. No `onStart`, `onResume`, `onPause`, `onStop`, or `onDestroy` override was found.

`AppNavigation` obtains `OnboardingViewModel` and gates the normal navigation graph on the persisted onboarding flag. When complete, it creates one remembered navigation controller and uses Hilt ViewModels for feature routes. The normal graph contains Home, upload preparation, Telegram authentication, destination selection, Queue, History, and Settings.

## Background and Foreground

The Activity lifecycle is not used to cancel application-wide WorkManager or TDLib work. Queue execution is owned by WorkManager, and Room is the durable task source. Foreground/background transitions were not exercised on a device. The repository contains no upload foreground service or notification implementation.

## Activity Recreation

Activity recreation re-enters `onCreate` and re-establishes Compose content. Theme preference, onboarding completion, queued uploads, and Telegram session storage have durable backing. Navigation back-stack state, prepared-but-not-queued files, selected destination before queue insertion, and transient form inputs have no explicit `SavedStateHandle` or `rememberSaveable` evidence in the reviewed startup path.

## Process Death and Restart

Room/DataStore and TDLib private directories provide persistence foundations. WorkManager receives task IDs and can persist its own requests. However, no explicit startup reconciliation or process-death test was found. Therefore the repository supports a persistence basis but does not prove complete process-death recovery.

## Safety Decision

No artificial startup delay, duplicate startup graph, duplicate Activity, duplicate TDLib client, or replacement WorkManager implementation was added. Crash recovery remains conditionally documented until real lifecycle testing is performed.
