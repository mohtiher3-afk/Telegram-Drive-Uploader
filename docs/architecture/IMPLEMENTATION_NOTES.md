# Implementation Notes

## Telegram isolation decision

The supplied architecture phase was evaluated against the current source tree rather than applied as a blind directory rename. The existing `data.telegram.client` package already contains the client contract and its implementation, and `data.telegram.repository` contains the repository implementation. This is a meaningful boundary around the official TDLib integration and is already consumed through domain contracts by the feature layer.

Only `TelegramClientImpl` directly imports the generated `Client` and `TdApi` classes. Keeping that coupling in one adapter prevents raw TDLib mechanics from leaking into the UI, ViewModels, domain models, or the upload engine. Moving the implementation to a new top-level package would require a broad import-only change while offering no additional separation, so no source package relocation was performed in this phase.

## Preservation rules applied

The phase did not change TDLib version or generated bindings, native libraries, ABI configuration, credentials, Gradle dependencies, authentication state handling, session persistence, logout behavior, upload construction, progress semantics, confirmed delivery semantics, WorkManager configuration, or UI design. No duplicate wrappers or model copies were introduced.

## Evidence and limitations

The dependency and direct-usage maps are based on a source-tree inventory. Static checks can prove package references and protected-file diffs, but they cannot prove `System.loadLibrary`, `Client.create`, Telegram authorization, or end-to-end delivery on a physical device. Those runtime claims require the existing Android emulator/device smoke tests and real Telegram test account/channel setup.

The local repository lacks a Gradle wrapper and the sandbox lacks a standalone Gradle installation. CI remains the authoritative compile and multi-ABI validation path for this checkout. Any future package relocation should be performed as a separate, explicitly reviewed slice with a rollback commit and full CI validation.
