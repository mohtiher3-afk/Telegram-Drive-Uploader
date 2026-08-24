# State Restoration Matrix

| State | Owner | Restored after Activity recreation | Restored after process death | Source / limitation |
|---|---|---|---|---|
| Onboarding completion | SettingsDataStore / `OnboardingViewModel` | Yes, via DataStore Flow | Yes, if DataStore is readable | Repository-backed; not device-tested. |
| Theme preference | SettingsDataStore / `MainActivity` | Yes, via DataStore Flow | Yes, if DataStore is readable | Repository-backed; not device-tested. |
| Telegram TDLib session | TDLib private database/files | Client/session basis exists | Intended persistence basis exists | Real login restoration not tested. |
| Cached Telegram user | SettingsDataStore | Yes, when read | Yes, if DataStore is readable | Cache is not live authorization authority. |
| Pinned destination IDs | SettingsDataStore | Yes | Yes, if DataStore is readable | Destination list itself comes from TDLib. |
| Queued upload records | Room | Yes, through Room Flow | Yes, persisted rows | Worker recovery after process death not tested. |
| WorkManager request | WorkManager | Independent of Activity | Platform-persistent basis | No explicit app reconciliation found. |
| Upload progress/status | Room | Yes, through queue Flow | Persisted last write | Late callback race and crash timing untested. |
| Prepared but unqueued URI list | `UploadViewModel` memory | Usually only while ViewModel survives | No explicit durable owner | Can be lost on process death. |
| Selected destination before queue insertion | `UploadViewModel` memory | Usually while ViewModel survives | No explicit durable owner | Copied into task only at queue insertion. |
| Scheduled timestamp after queue insertion | Room upload task | Yes | Yes, as task field | One-time delay behavior after restart untested. |
| History query text/period/sort | `HistoryViewModel` memory | Not explicitly saved | No | Reinitialized on ViewModel recreation. |
| Navigation back stack | `NavHostController` remembered in composition | May be restored by navigation/runtime mechanisms | No explicit saved-state claim in reviewed code | Device test required. |
| Auth form input | Auth ViewModel/UI | Scope-dependent | No explicit durable owner | Must not persist secrets or verification codes. |
| Settings form transient input | Settings UI/ViewModel | Scope-dependent | No explicit durable owner | Runtime behavior untested. |

## Interpretation

A durable storage path is not equivalent to a verified process-death guarantee. The table distinguishes repository evidence from device behavior. No claim is made that prepared files, transient selections, navigation state, or authentication form input survive process death.
