# Test Coverage Map

| Layer | Component | Tested | Test type | Missing coverage | Priority |
|---|---|---:|---|---|---|
| Core | SmartFileAssistant | Yes | Unit | More edge cases for malformed names | P2 |
| Core | UploadTelemetryFormatter | Yes | Unit | Locale-specific formatting cases | P2 |
| Core | VideoFormatSupport | Yes | Unit | Additional MIME/container combinations | P2 |
| Domain | UploadCompletionPolicy | Yes | Unit | Failure-reason matrix | P1 |
| Data | UploadWorkPolicy | Yes | Unit | WorkManager TestDriver execution | P1 |
| Telegram | Video message mapping | Yes | Unit boundary | Chat lookup and error mapping doubles | P1 |
| TDLib boundary | JNI load / `Client.create()` | Partial | Instrumentation | Device/emulator execution evidence | P0 |
| Upload engine | Real state transitions | Partial | Unit/integration gap | Queued through confirmed completion with controlled fake boundary | P0 |
| Queue | Persistence and recovery | No direct suite found | Missing | Add repository/Room queue tests | P0 |
| Scheduler | Existing scheduling rules | No direct suite found | Missing | Verify only implemented scheduler behavior | P1 |
| Room | Insert/read/update/delete/order | No direct suite found | Missing | Isolated Room database tests | P1 |
| DataStore | Defaults and persistence | No direct suite found | Missing | Isolated DataStore tests | P1 |
| ViewModels | Loading/content/error/retry states | No direct suite found | Missing | Coroutine-test based state/effect tests | P1 |
| Compose UI | Startup, auth, home, destination, queue, history, settings | No direct suite found | Missing | Semantics-based UI tests | P2 |
| Localization/RTL | English/Arabic IDs and layout | Partial | Script/static | Runtime RTL and placeholder tests | P2 |
| Notifications | Upload lifecycle notifications | No direct suite found | Missing | Notification behavior tests if implementation is present | P1 |

The highest-risk gaps are runtime TDLib evidence, upload lifecycle transitions, queue persistence/recovery, and authentication state mapping. No production code is changed merely to make these gaps easier to test.
