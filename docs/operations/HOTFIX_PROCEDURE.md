# Hotfix Procedure

1. **Reproduce:** Capture app version, Android version, device/ABI, operation, sanitized diagnostics, and deterministic reproduction steps.
2. **Classify severity:** Assign impact and priority; security and data-loss issues take precedence over cosmetic issues.
3. **Create a hotfix branch:** Branch from the affected release commit. Do not rewrite history.
4. **Implement the minimal fix:** Change only the confirmed root-cause surface. Do not add unrelated features, dependency upgrades, or refactors.
5. **Test:** Add or update regression tests for the bug and run the relevant unit, integration, and device checks.
6. **Audit:** Run security, resource, WorkManager, TDLib, diff, and secret checks.
7. **Build:** Run debug and signed Release builds for all supported ABIs through CI.
8. **Release:** Prepare notes, artifact checksums, rollback reference, and manual approval before publishing.
9. **Monitor:** Watch crash, authentication, upload, queue, and background-execution signals and retain a decision record.

Every hotfix must document the bug description, root cause, fix, tests, affected areas, risk, and rollback strategy.
