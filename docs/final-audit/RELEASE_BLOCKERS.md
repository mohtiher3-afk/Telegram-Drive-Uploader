# Release Blockers

| ID | Severity | Issue | Evidence | Status |
|---|---|---|---|---|
| RB-001 | BLOCKER until verified | Final build/test/lint/TDLib CI conclusion not yet captured for the latest audit commit | GitHub Actions status must be checked after remote execution | Open |
| RB-002 | BLOCKER until verified | Real device/emulator authentication and upload smoke evidence is absent | No connected device in the temporary environment | Open |
| RB-003 | HIGH | Local Gradle wrapper is absent | Repository tree contains no `gradlew` or wrapper jar | Open; CI provisions Gradle 8.9 |
| RB-004 | HIGH | Exact local ELF validation unavailable | `readelf` is unavailable in temporary environment | Open; CI/toolchain gate |

The repository is **NOT READY** for an unconditional release claim until RB-001 and RB-002 have final evidence. No claim of “NO RELEASE BLOCKERS” is made.
