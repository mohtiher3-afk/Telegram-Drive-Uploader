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
