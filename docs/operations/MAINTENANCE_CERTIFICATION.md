# Maintenance Certification

## Repository Stability

The repository has focused source boundaries, a committed Gradle Wrapper, guarded resources/WorkManager/TDLib checks, and no tracked private artifacts in the reviewed state.

## CI Stability

The canonical Android CI and manual signed release workflows exist. v1.0.15 release workflow `32630539974` passed its signed multi-ABI build and publication. Action deprecation warnings remain tracked technical debt.

## Testing Strategy

Focused JVM, lint, artifact, security, resource, and WorkManager gates are available. Real-device authentication, upload, background, accessibility, and performance evidence remains a required follow-up.

## Security Maintenance

Secret names and handling are documented; values are not stored in source. Redacted scanning and security procedures are required for future changes.

## TDLib Maintenance

TDLib changes require exact source/version review, native rebuild, ABI/JNI/artifact validation, authentication, upload, background, regression, and release evidence.

## Dependency Maintenance

Dependencies are updated one logical group at a time with compatibility, security, compile, test, lint, release, and regression checks.

## Database Maintenance

Schema changes require migrations, migration/upgrade tests, and data-preservation evidence. Destructive downgrade is prohibited as a shortcut.

## Performance Maintenance

Performance changes require before/after measurements. If unavailable, record `PERFORMANCE CHANGE NOT QUANTITATIVELY VERIFIED` and do not claim improvement.

## Release Process

Each release records versionName, versionCode, commit, artifacts, verification, checksums, notes, limitations, signing status, and rollback reference.

## Hotfix Process

Hotfixes follow reproduction, isolation, root cause, minimal fix, regression test, quality gates, audit, build, release, and monitoring.

## Documentation

Project, release, operations, testing, performance, security, CI, architecture, onboarding, and maintenance indexes are available from `docs/README.md`.

## Manus Development Protocol

Future tasks follow inspection, impact analysis, smallest safe change, tests, lint, security/TDLib checks, diff review, documentation, and exact reporting.

## Final Status

**CONDITIONALLY MAINTENANCE READY**. The control system is documented and guarded, but runtime/device evidence and broad regression coverage remain limitations that must be closed before an unrestricted production-certification claim.
