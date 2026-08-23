# Bug Fix Workflow

1. Reproduce the issue with sanitized context.
2. Isolate the affected boundary.
3. Identify the root cause.
4. Define the smallest safe fix.
5. Implement only that fix.
6. Add or update the focused test.
7. Add regression coverage for affected areas.
8. Inspect the complete diff.
9. Run relevant quality gates: build, tests, lint, security, TDLib, and resource/WorkManager checks where applicable.
10. Document cause, fix, risk, and evidence.
11. Release only when the fix affects published behavior and all release gates pass.

Do not fix a visible symptom while ignoring an identifiable root cause. Do not mix new features or unrelated refactors into a bug fix.

## Bug Template

Use [BUG_TEMPLATE.md](BUG_TEMPLATE.md) for important issues.
