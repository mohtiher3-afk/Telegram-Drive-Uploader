# Dead Code Plan

No deletion is performed.

| Classification | Current assessment | Required proof |
|---|---|---|
| CONFIRMED UNUSED | None declared from filename inspection alone | Static reference search plus build/test evidence |
| POSSIBLY UNUSED | Feature classes not reachable from current navigation may fall here | Startup/navigation verification |
| REFLECTION / RUNTIME DEPENDENCY | TDLib bindings, Hilt-generated code, WorkManager provider, Room-generated code | Keep protected; inspect generated/runtime references |
| TEST ONLY | Unit and instrumented test helpers | Preserve for regression coverage |
| GENERATED | TDLib Java bindings and build-generated sources | Never hand-delete |
| UNKNOWN | Any utility or resource without clear call-site evidence | Mark REVIEW and do not remove |
