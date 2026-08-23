# Change Management

Every change begins with a problem statement and an explicit scope. Inspect the current behavior, identify affected boundaries, estimate risk, define tests, and record a rollback path when applicable.

| Change type | Required review |
|---|---|
| Feature | User value, affected UI/data paths, localization/RTL, tests, and rollback |
| Bug fix | Reproduction, root cause, minimal fix, regression test, affected areas, and rollback |
| Security fix | Exposure scope, containment, secret rotation if needed, security scan, and incident record |
| Dependency update | Compatibility, changelog, security review, full tests, release build, and TDLib/Compose compatibility |
| TDLib update | Version/source review, native rebuild, ABI/JNI/binding checks, authentication/upload tests, and release build |
| Database change | Schema version, migration, migration tests, upgrade test, and data preservation verification |
| Release change | Version, commit, artifacts, signatures, checksums, notes, limitations, and rollback reference |

Changes must be committed in focused units. Do not disable tests, bypass artifact validation, rewrite history, or bundle unrelated cleanup with a production fix.
