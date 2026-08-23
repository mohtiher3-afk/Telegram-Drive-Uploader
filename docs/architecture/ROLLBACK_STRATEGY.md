# Rollback Strategy

Use the existing `main` history and isolated feature branches rather than manually reversing large moves. Recommended branch names are `refactor/phase-core`, `refactor/phase-telegram`, `refactor/phase-upload`, and `refactor/phase-ui`.

Before each high-risk phase, record the baseline commit, CI run, artifact-check result, and release/device evidence. Keep one logical purpose per commit. If a phase fails, revert or roll back to the last verified checkpoint; do not force-push destructive history. TDLib/JNI, authentication, upload, WorkManager, and database changes must never be bundled with unrelated UI or documentation changes.
