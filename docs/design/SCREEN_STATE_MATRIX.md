# Screen State Matrix

| Screen | Loading | Empty | Content | Error | Disabled | Special States |
|---|---|---|---|---|---|---|
| Home | No explicit screen loading branch; initial `HomeUiState` is rendered | No active uploads | Telegram status, statistics, active uploads, recent activity data in state | No dedicated Home error state found | Connect action is shown when unauthenticated | Authorized user identity; active statuses include queued, preparing, uploading, retrying |
| Telegram authentication | Explicit connection/authentication loading indicators | Not applicable | Phone/code/password stages and authorized state | Error message and retry action are present in state branches | Form actions depend on current authentication stage | Waiting for code, password required, authorization success/failure |
| Telegram destination | Destination loading indicator | Empty destination/search result state | Searchable destination list and selected destination | Error message and retry path are present | Continue/selection actions depend on valid selection | Search query, selected destination, authorized/unauthorized entry |
| Upload preparation | File preparation/loading state | No selected videos state | Prepared video list, destination, schedule, smart suggestions | Preparation error state | Upload action depends on prepared input and destination | Real SmartFileAssistant suggestions; scheduled timestamp may be set |
| Queue | No separate loading branch identified beyond repository state | Empty queue state | Queue items with current upload status | Failed items/status presentation | Actions depend on item status | Queued, preparing, uploading, retrying, completed, failed, cancelled where represented by the domain model |
| History | Repository-backed state handling | Empty history state | Historical upload items | Failed upload records are content, not a screen-level exception | Existing actions depend on record state | Completed, failed, and cancelled records |
| Settings | Diagnostics loading/collection behavior where present | Empty diagnostics list can render | Theme/settings sections and diagnostics | Diagnostic event severity and error colors | Controls depend on setting/action state | Light/dark/system theme and diagnostic severity presentation |
| Onboarding | No asynchronous screen loading state | Not applicable | Three real onboarding pages | Permission result is handled; no separate error screen found | Completion waits for permission result when permissions are requested | First-launch gate, skip, permission request, persisted completion |

The matrix intentionally distinguishes domain statuses from screen-level loading or error branches. A redesign must not add states merely because they are common in other applications.
