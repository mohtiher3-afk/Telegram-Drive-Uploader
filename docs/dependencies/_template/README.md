# Controlled Dependency Update Template

Copy this directory to `docs/dependencies/changes/<change-id>/` before changing a dependency or build tool. Replace placeholders with repository evidence and official sources. Do not choose the newest version automatically, and do not combine unrelated cleanup or redesign.

| Document | Required purpose |
|---|---|
| `REQUEST.md` | Requested dependency, current/target versions, reason, scope, and benefit. |
| `IMPACT_ANALYSIS.md` | Build, UI, data, Telegram, native, upload, security, and release risk. |
| `COMPATIBILITY_MATRIX.md` | Current/target compatibility with Gradle, AGP, Kotlin, Compose, AndroidX, NDK, and TDLib. |
| `SOURCES.md` | Official release notes and compatibility evidence. |
| `BASELINE_RESULTS.md` | Pre-change build, test, lint, debug, and TDLib baseline. |
| `AFTER_DEPENDENCY_GRAPH.md` | Post-change dependency graph, conflicts, and transitive changes. |
| `CHANGE_REPORT.md` | Actual update, validation, release, runtime, and rollback results. |
| `ROLLBACK_PLAN.md` | Previous commit/versions and safe rollback constraints. |

A generic dependency update must not silently include TDLib changes. If NDK/CMake, TDLib, JNI, ABI, database schema, authentication, Upload Engine, or security architecture is affected, stop and use the applicable specialized protocol.
