# Architecture Rules

## Current module boundaries
Use the repository's existing boundaries:
- `app` — application shell, entry point, navigation wiring
- `feature` — Compose screens, ViewModels, theme, shared UI components
- `domain` — domain models and contracts
- `data` — TDLib, Room, repositories, upload engine, WorkManager
- `core` — shared utilities/diagnostics and supporting infrastructure

## Required dependency direction
UI → ViewModel → domain contract → data implementation.

Screens must not construct:
- TDLib clients
- Room databases/DAOs
- WorkManager
- upload engines
- repository implementations

Keep these responsibilities in their existing data/domain boundaries.

## Upload path
Preserve the existing flow:
`UploadViewModel → UploadRepository/UploadManager → WorkManager → UploadWorker → TelegramUploadEngineImpl → TelegramClient/TDLib`

Do not change destination identity, upload completion semantics, WorkManager identities, scheduler timestamps, file URI ownership, or TDLib calls during visual work.

## Lifecycle
Long-running work must not depend on a Composable or Activity remaining alive. Continue to use the existing Room + WorkManager + foreground-control approach unless a task explicitly authorizes an architecture change.
