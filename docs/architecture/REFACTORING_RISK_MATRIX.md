# Refactoring Risk Matrix

| Risk | Areas | Why | Required control |
|---|---|---|---|
| CRITICAL | TDLib, JNI, authentication, upload completion, WorkManager, Room schema, ABI packaging | A regression can lose sessions, uploads, delivery guarantees, or app startup | Characterization tests, artifact gates, device evidence, isolated commits |
| HIGH | Repositories, managers, navigation, Hilt modules, file handling | Wiring or lifecycle changes can fail only at runtime | Import graph, Hilt compile, process-death and error tests |
| MEDIUM | ViewModels, UI state, localization, performance tuning | User-visible regressions or recomposition issues | Compose/UI tests, RTL review, measured benchmarks |
| LOW | Documentation, comments, unused-import cleanup, package-only moves | Limited behavior impact if references are preserved | Diff review and build/lint |

A change touching more than one critical area must be split or explicitly reviewed before implementation.
