# Lifecycle and Crash-Recovery Test Matrix

**Rule:** Static code evidence is not runtime evidence. Mark a scenario `NOT VERIFIED` unless it was executed on a device or emulator and recorded.

| Scenario | Startup | Auth | Navigation | Queue | Upload | Scheduler | Notifications | Result |
|---|---|---|---|---|---|---|---|---|
| Cold start | Application and Activity setup are defined | TDLib restoration not runtime-tested | Home/onboarding route is code-defined | Room basis exists | No active runtime proof | No startup reconciler found | Not implemented | NOT VERIFIED |
| Warm start | No explicit repeated app initialization found | Existing session behavior untested | Current route restoration untested | Room Flow remains source | WorkManager owns work | Existing request behavior untested | Not implemented | NOT VERIFIED |
| Background | Activity has no custom pause logic | Session continuity untested | Screen state untested | WorkManager/Room basis exists | Background delivery untested | Delayed work untested | No foreground notification | NOT VERIFIED |
| Foreground | Activity content re-enters normally | Session rebind untested | Navigation restoration untested | Queue Flow reload basis exists | Worker/TDLib continuity untested | Schedule continuity untested | Not implemented | NOT VERIFIED |
| Rotation | `onCreate` rebuilds Compose content | ViewModel/session behavior untested | No explicit saved navigation claim | Room-backed queue basis exists | Prepared in-memory list may depend on ViewModel scope | Task schedule persists after insertion | Not implemented | NOT VERIFIED |
| Process death | Android restart path is not explicitly reconciled | TDLib session restoration untested | Back-stack restoration untested | Room and WorkManager persistence basis exists | Worker recreation untested | Scheduled execution untested | No notification restoration | NOT VERIFIED |
| Restart | Application initializes once per process | Auth route decision untested | Start route is code-defined | Tasks re-read from Room | Real upload recovery untested | No explicit reconciler | Not implemented | NOT VERIFIED |
| Logout | Settings/auth action path exists | TDLib logout behavior reviewed separately | Navigation response untested | Queued tasks are not automatically invalidated | Session/task interaction untested | Scheduled tasks after logout untested | Not implemented | NOT VERIFIED |
| Re-login | Application can show auth route | New session behavior untested | Stale route/destination untested | Queue remains Room-backed | Account/task binding untested | Schedule behavior untested | Not implemented | NOT VERIFIED |

## Automated Tests

No timing-based lifecycle test was added during this documentation-only phase. Existing unit tests cover selected upload policies and helpers, but not Android process recreation or Worker execution. Runtime tests require an emulator/device and must record actual observations.
