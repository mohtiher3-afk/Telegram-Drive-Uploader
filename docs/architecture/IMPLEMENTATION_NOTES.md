# Implementation Notes

## Telegram isolation decision

The supplied architecture phase was evaluated against the current source tree rather than applied as a blind directory rename. The existing `data.telegram.client` package already contains the client contract and its implementation, and `data.telegram.repository` contains the repository implementation. This is a meaningful boundary around the official TDLib integration and is already consumed through domain contracts by the feature layer.

Only `TelegramClientImpl` directly imports the generated `Client` and `TdApi` classes. Keeping that coupling in one adapter prevents raw TDLib mechanics from leaking into the UI, ViewModels, domain models, or the upload engine. Moving the implementation to a new top-level package would require a broad import-only change while offering no additional separation, so no source package relocation was performed in this phase.

## Preservation rules applied

The phase did not change TDLib version or generated bindings, native libraries, ABI configuration, credentials, Gradle dependencies, authentication state handling, session persistence, logout behavior, upload construction, progress semantics, confirmed delivery semantics, WorkManager configuration, or UI design. No duplicate wrappers or model copies were introduced.

## Evidence and limitations

The dependency and direct-usage maps are based on a source-tree inventory. Static checks can prove package references and protected-file diffs, but they cannot prove `System.loadLibrary`, `Client.create`, Telegram authorization, or end-to-end delivery on a physical device. Those runtime claims require the existing Android emulator/device smoke tests and real Telegram test account/channel setup.

The local repository lacks a Gradle wrapper and the sandbox lacks a standalone Gradle installation. CI remains the authoritative compile and multi-ABI validation path for this checkout. Any future package relocation should be performed as a separate, explicitly reviewed slice with a rollback commit and full CI validation.

## Smart Assistant phase decision

The source inventory found no real Smart File Assistant, suggestion engine, recommendation engine, AI service, model, or intelligent classifier. The only related code is deterministic media/upload preparation: `VideoFormatSupport`, `VideoMetadataExtractor`, and `StreamingFileReaderImpl`.

No files were moved, renamed, merged, or deleted in this phase. No package, dependency, Hilt, database, UI, navigation, upload, Telegram, TDLib, authentication, or WorkManager changes were required. The phase is therefore intentionally documentation-only, as permitted when the requested feature does not exist.

The unresolved items are the mixed responsibility of `VideoMetadataExtractor`—metadata extraction plus `UploadTask` construction—and the temporary-file staging behavior in `TelegramUploadEngineImpl`. Both are high-risk behavior boundaries and remain unchanged pending dedicated characterization tests. No duplicate implementation was confirmed safe to remove.

Protected files and surfaces remain unchanged: generated TDLib bindings, `app/src/main/jniLibs/**`, ABI configuration, TDLib build scripts and version, AndroidManifest/WorkManager configuration, credentials, database schema/entities/DAOs/migrations, upload engine behavior, and UI/navigation code.

## Smart Assistant correction

The earlier note stating that no Smart File Assistant existed was incorrect. Current source inspection found `core.ai.SmartFileAssistant` and `SmartFileSuggestion`, with deterministic offline filename and keyword suggestions consumed by `UploadViewModel`. The status and component maps were corrected. No assistant behavior or tests were changed.

## Features and Screens phase

The current feature package is already organized by existing responsibilities: `onboarding`, `home`, `telegram`, `upload`, `queue`, `history`, and `settings`. `AppNavigation` owns routes and cross-screen callbacks. The inventory found no standalone `about`, `scheduler`, `selectvideo`, or separate `uploadqueue` screen packages. No files were moved or split because the conceptual target would require broad package and navigation changes without a demonstrated behavioral benefit.

The phase created `FEATURE_INVENTORY.md`, `SCREEN_RESPONSIBILITY_MAP.md`, `NAVIGATION_COUPLING_AUDIT.md`, `FEATURE_DEPENDENCY_MAP.md`, and `FEATURE_SHARED_STATE_AUDIT.md`. The UI, navigation, ViewModel state, Telegram, upload, WorkManager, database, dependency, and Material 3 behavior remain unchanged.

## DI/Hilt re-audit

The current Hilt implementation was re-audited after the Navigation phase. Four `SingletonComponent` modules are present: `DatabaseModule`, `RepositoryModule`, `UploadModule`, and `WorkModule`. They expose three providers and six bindings. Eight feature ViewModels use Hilt ViewModel injection, `UploadWorker` uses `@HiltWorker` with `@AssistedInject`, and `TelegramDriveApp` is the Hilt application entry point.

No duplicate unqualified provider, confirmed constructor cycle, custom service locator, or clearly unsafe scope was found. Existing interfaces remain justified as domain and test boundaries. No empty conceptual modules were added, and no scopes, providers, bindings, dependencies, or runtime behavior were changed. TDLib client lifecycle, real upload delivery, WorkManager startup, Room database identity, DataStore keys, authentication, navigation, and UI remain protected.

## Material 3 Design System phase

The current Compose theme already provides light and dark semantic color schemes, Android 12+ dynamic-color support, and an expressive shape scale. The foundation phase preserves those decisions and adds a complete shared typography scale in `core.ui.theme.Type`, semantic spacing tokens in `core.ui.theme.Dimensions`, and design documentation under `docs/design`.

The settings diagnostic log now uses `MaterialTheme.colorScheme` roles for error, warning, informational, container, and secondary text colors rather than raw colors embedded in the feature screen. This keeps the existing diagnostic states while improving theme and dark-mode consistency. No upload, Telegram, authentication, WorkManager, database, navigation, or screen-state behavior changed. RTL guidance was documented without rewriting existing screen layouts; future components must prefer logical start/end APIs and auto-mirrored directional icons only where semantically correct.

## Splash and Startup Experience phase

The verified startup flow is process creation through `TelegramDriveApp`, then `MainActivity`, theme preference collection, `TelegramDriveTheme`, and `AppNavigation`. `AppNavigation` reads the existing `SettingsDataStore.onboardingCompleted` flow through `OnboardingViewModel`; false renders onboarding and true renders the unchanged graph with `home` as its start destination.

No separate splash Activity, fake progress, fixed delay, `Thread.sleep`, duplicate TDLib initialization, or StartupViewModel is present or justified by the observed code. No `androidx.core.splashscreen` dependency or explicit splash API usage exists in the current checkout, so no new dependency or startup coordination was introduced. Database, WorkManager, Telegram, TDLib, and session work remain lazy or feature-owned. The new startup documents record these decisions and their limitations without changing runtime behavior.

## App-wide Motion and Animation phase

The motion audit found two existing custom state-driven transitions: onboarding page changes and Telegram authentication state changes. Both use built-in Compose `AnimatedContent`; no infinite animation, custom framework, list-item entrance animation, or navigation transition was found. `AppMotion` now provides short semantic duration/easing tokens, and those two existing transitions use the tokens without changing their triggers or state sources.

Upload progress remains Material-driven and reflects real state and byte values. No business logic, worker behavior, progress calculation, navigation destination, Telegram/TDLib flow, or upload confirmation semantics changed. The repository does not yet expose a dedicated reduced-motion abstraction; this phase keeps motion short and non-essential and documents device-level animator-scale validation as future work rather than inventing a new accessibility layer.
