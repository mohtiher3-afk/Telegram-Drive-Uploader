# File Migration Plan

This is a planning table only. No file moves are approved by this document.

| Current file/group | Responsibility | Target path | Action | Risk | Validation |
|---|---|---|---|---|---|
| `core/navigation/AppNavigation.kt` | Navigation graph and tabs | `core/navigation/` | KEEP | LOW | Navigation compile and route smoke test |
| `core/di/*.kt` | Hilt database, repository, upload and WorkManager bindings | `core/di/` | KEEP | HIGH | Hilt compile and worker creation test |
| `data/local/*.kt` | Room database, DAO and entity | `data/local/` | KEEP | CRITICAL | Schema/migration and persistence tests |
| `data/telegram/client/*` | TDLib client and authorization/update handling | `data/telegram/client/` | KEEP | CRITICAL | TDLib artifact gate and auth-state tests |
| `data/telegram/repository/*` | Telegram domain adapter | `data/telegram/repository/` | KEEP | CRITICAL | Destination and send-result tests |
| `data/upload/*` | Upload manager, engine, reader and policy | `data/upload/` | KEEP | CRITICAL | Worker, cancellation and completion tests |
| `feature/*` | Screens and ViewModels | Existing feature packages | KEEP | MEDIUM | Compose compilation and UI tests |
| Large manager/engine classes | Mixed orchestration | Possible `manager/` plus `coordinator/` split | REVIEW | HIGH | Characterization tests before any move |
| Generated TDLib bindings and `jniLibs/**` | Official native runtime | Existing protected paths | KEEP | CRITICAL | Artifact checker and JNI smoke test |

Every future MOVE requires an import/reference search, Hilt impact review, test update, resource review, manifest review, and a reversible commit.
