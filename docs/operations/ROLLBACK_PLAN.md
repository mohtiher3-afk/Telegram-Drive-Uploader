# Rollback Plan

## Identify the Previous Release

Use the GitHub Releases page and tags to identify the last known-good version. Record its tag, release commit, ABI artifact, checksum, and observed issue. For v1.0.15, the release tag is `v1.0.15` and the candidate source commit is documented in `docs/release/PRODUCTION_BASELINE.md`.

## Restore Previous Source State

Use a reviewed branch or a new rollback branch based on the previous release tag. Do not rewrite shared history or use destructive reset operations on the main branch. Build the rollback candidate through the same CI gates as a normal release.

## Stop a Problematic Rollout

Pause distribution or remove the affected release from the distribution channel according to the channel’s controls. Do not delete evidence, logs, or release assets needed for investigation.

## Preserve User Data

Keep Room databases, DataStore preferences, local files, and Telegram session behavior intact. A rollback must not delete user data merely to restore application startup.

## Database Downgrade Safety

Never perform a destructive database downgrade as a rollback shortcut. Analyze schema versions, provide a tested forward migration or compatible fallback, and verify data preservation before changing the database path.

## Verification

Run security/resource/WorkManager/TDLib guards, JVM tests, lint, ABI builds, APK signature checks, and device smoke tests relevant to the incident. Document the reason, scope, evidence, and recovery outcome.
