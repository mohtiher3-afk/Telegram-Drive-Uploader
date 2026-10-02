# Data Retention Policy

This is an engineering retention record, not a legal privacy notice. Durations are not invented where the implementation or policy does not define them.

| Data category | Retain | Delete/clear | Control | Documented duration |
|---|---|---|---|---|
| Active upload metadata and queue state | While needed to process or display the upload | User/application queue/history lifecycle | Application and user action | RETENTION NOT EXPLICITLY DEFINED |
| Upload history | As required by the existing history feature | Existing user/application history deletion behavior | User/application controlled | RETENTION NOT EXPLICITLY DEFINED |
| DataStore settings | Until changed, cleared, or logout-specific cleanup applies | Existing settings/cache/logout actions | User/application controlled | RETENTION NOT EXPLICITLY DEFINED |
| Telegram display identity fields | Until logout clears them or the user changes state | Logout path clears Telegram user settings | Session/user controlled | RETENTION NOT EXPLICITLY DEFINED |
| TDLib session state | While Telegram authorization requires it | TDLib logout/session lifecycle | TDLib/session controlled | RETENTION NOT DEFINED BY THIS REPOSITORY |
| Diagnostic events in memory | For short-term local troubleshooting | Age pruning, 200-event bound, explicit clear, critical memory pressure | System/user controlled | Up to 24 hours and 200 events |
| Logcat output | As retained by Android/device tooling | Android/user tooling | System/user controlled | RETENTION NOT CONTROLLED BY APP |
| Diagnostic export | Only when the user copies or shares it | User-controlled clipboard/file/support handling | User controlled | RETENTION NOT CONTROLLED BY APP |
| File bytes and transient buffers | During active read/upload operation | Existing upload/file lifecycle | Operation controlled | RETENTION NOT EXPLICITLY DEFINED |
| Cache | While rebuildable cached data is useful | Existing cache-clearing/system cache lifecycle | System/user controlled | RETENTION NOT EXPLICITLY DEFINED |
| Notifications | While Android notification state requires them | User/system notification lifecycle | System/user controlled | RETENTION NOT CONTROLLED BY APP |
| Analytics/crash data | None identified | Not applicable | Not collected by identified implementation | NOT COLLECTED |

Android backup configuration excludes the Room database and `datastore/` from cloud backup and device transfer. This is an exclusion rule, not a complete deletion or recovery policy. Any future retention change requires a privacy review and regression validation.
