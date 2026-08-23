# Performance Inventory

| Component | Risk | Evidence | Priority | Status |
|---|---|---|---|---|
| Startup | Potential eager work | Existing startup audit shows no artificial delay or duplicate TDLib initialization | P1 | Review complete; no change |
| Compose screens | Recomposition/list rendering | Lazy lists use item identity keys in inspected queue path | P2 | Review complete; device profiling unavailable |
| ViewModels/Flows | Scope and duplicate collection risk | Existing code uses lifecycle/ViewModel-owned coroutines; no GlobalScope found | P1 | Review complete |
| Room | Progress-write frequency | Upload repository/DAO persist state and duration | P1 | Needs measured write-frequency profiling |
| DataStore | Transient-state misuse | Settings persistence is feature-owned | P2 | No speculative change |
| File handling | Large-file memory risk | `StreamingFileReader` uses streams and `use` blocks | P0 | Positive evidence |
| Upload engine | Buffer/progress/retry tradeoff | Real TDLib path; no benchmark available | P0 | No buffer change |
| WorkManager | Wakeups/retries/constraints | Existing policy and manifest guards pass | P1 | No behavior change |
| Images | Decode/memory | Launcher and application assets are finite; no image library added | P2 | Review complete |
| Notifications | Update frequency | Requires device/runtime observation | P1 | Not measured |
