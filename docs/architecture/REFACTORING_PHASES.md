# Refactoring Phases

The phases below are planning stages, not approval to execute them.

| Phase | Files affected | Protected files | Pre/post-check | Risk/rollback |
|---|---|---|---|---|
| A. Characterization | tests and docs | TDLib/JNI, auth, upload, DB, manifest | Baseline tests, artifact checker | LOW; revert one commit |
| B. Core | `core/**` utilities/theme | TDLib and WorkManager | Compile/lint/UI smoke | MEDIUM; feature branch |
| C. Data | repository/local packages | Room schema/migrations | Persistence tests | HIGH; rollback before schema changes |
| D. Telegram | client/repository tests only initially | TDLib bindings/native files | Auth/destination tests | CRITICAL; no behavior move without device evidence |
| E. TDLib verification | scripts/docs/CI | Native artifacts | Checker and JNI smoke | CRITICAL; revert isolated CI commit |
| F. Upload | manager/engine/worker tests | Send/completion contract | Worker and terminal-event tests | CRITICAL; phase branch |
| G. Smart Assistant | local assistant tests | No remote AI dependency | Suggestion tests | MEDIUM |
| H–J. Features/navigation/DI | feature packages and bindings | Routes and Hilt contracts | Compose/navigation/Hilt checks | HIGH; one feature per commit |
| K–M. Design/resources/tests | theme/resources/test suites | Functional upload/auth | RTL, accessibility, release build | MEDIUM |
| N–P. Docs/CI/final audit | docs/workflows | Application behavior | Full CI and release checklist | LOW–HIGH depending on workflow |

Each implementation phase requires a small commit, a clean diff, relevant tests, and a documented rollback point.
