# Final Production Handoff

## Executive Summary

The repository has a documented production baseline and a published signed v1.0.15 release. This handoff certifies the available build, signing, artifact, security, and CI evidence without claiming unperformed device validation.

## Production Baseline

See [PRODUCTION_BASELINE.md](PRODUCTION_BASELINE.md) for application identity, version, toolchain, TDLib, ABIs, and release commit.

## Release Candidate

The candidate is tag `v1.0.15`, source commit `30ac9c902984ca247e2f97e45f95ca890c21e59c`, with signed APKs published in the GitHub Release. Documentation follow-up is recorded separately and does not alter the binaries.

## Verification Results

The release workflow passed JVM tests, release lint, TDLib artifact checks, per-ABI builds, APK native-entry checks, checksum generation, and signature verification. Resource, WorkManager, and repository security guards pass locally. Authentication, real upload, process-death recovery, and device UI validation are `NOT VERIFIED`.

## Security

Secrets are stored outside source code and consumed through GitHub Actions secrets. The workflow removes temporary keystore material. No credentials, signing values, session data, or private media are included in this handoff.

## Performance

No unmeasured performance improvement is claimed. Startup, memory, battery, recomposition, and real upload throughput require controlled device baselines.

## TDLib

TDLib artifacts are official and ABI-validated by the release workflow. Future updates must follow [TDLIB_UPDATE_POLICY.md](../operations/TDLIB_UPDATE_POLICY.md). Runtime JNI and authorization evidence remains a device follow-up.

## Build Artifacts

See [RELEASE_ARTIFACTS.md](RELEASE_ARTIFACTS.md) for published APK names, sizes, and SHA-256 checksums. The current workflow publishes per-ABI APKs, not an AAB.

## Known Limitations

See [KNOWN_LIMITATIONS.md](KNOWN_LIMITATIONS.md) and [USER_FACING_LIMITATIONS.md](USER_FACING_LIMITATIONS.md). The principal limitations are missing real-device evidence and device-specific background behavior coverage.

## Maintenance Rules

Use [CHANGE_MANAGEMENT.md](../operations/CHANGE_MANAGEMENT.md), [DEVELOPMENT_RULES.md](../DEVELOPMENT_RULES.md), and the operations index before any future change. Every change requires scope, risk, tests, and a rollback reference where applicable.

## Incident Response

Use [INCIDENT_RESPONSE.md](../operations/INCIDENT_RESPONSE.md) and collect only sanitized diagnostics.

## Rollback

Use [ROLLBACK_PLAN.md](../operations/ROLLBACK_PLAN.md). Preserve user data and never use destructive database downgrade as a shortcut.

## Hotfix

Use [HOTFIX_PROCEDURE.md](../operations/HOTFIX_PROCEDURE.md) for narrowly scoped, regression-tested fixes.

## Technical Debt

See [TECHNICAL_DEBT.md](../operations/TECHNICAL_DEBT.md) for confirmed gaps in runtime evidence, test breadth, action maintenance, tooling, and performance baselines.

## Post-Release Backlog

Review [POST_RELEASE_BACKLOG.md](../final-audit/POST_RELEASE_BACKLOG.md) and classify work as P0 through P3 before implementation. No backlog item is implemented by this handoff.

## Certification Status

**NOT CERTIFIED** for unrestricted production handoff. The release artifacts are signed and published, but real Telegram authentication, upload, background recovery, and device-level UI evidence remain incomplete. The release candidate is therefore not declared frozen under the certification rule.
