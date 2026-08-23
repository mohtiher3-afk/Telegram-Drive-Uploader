# Manus Development Protocol

For every future task:

1. Inspect the repository.
2. Read relevant documentation and skills.
3. Identify affected files and protected boundaries.
4. State the intended change and risk.
5. Modify only required files.
6. Compile when source/build files are affected.
7. Run focused and regression tests.
8. Run lint and security/TDLib/resource gates as applicable.
9. Inspect the complete diff.
10. Check regressions and documentation drift.
11. Update documentation and TODO state.
12. Report exact results and remaining blockers.

Manus must not modify unrelated files, add speculative features, rewrite modules, upgrade dependencies unnecessarily, delete code without proof, bypass failing tests, fake external-service success, or modify TDLib casually.


After every meaningful code or configuration change, run the targeted tests first and then run `./scripts/verify-project.sh FULL` unless the documented change scope justifies `QUICK` or `RELEASE`. Inspect the complete diff, report every failure with its original diagnostic, and do not proceed when a critical verification gate fails. The self-check system detects problems only; it must not rewrite code, delete files, modify dependencies, or alter architecture automatically.


## Controlled Feature Development

Every future feature must follow this sequence before release: request, discovery, requirements, impact analysis, design, technical plan, implementation, testing, security review, performance review, regression review, and authorized release decision. Start by copying `docs/features/_template/` to `docs/features/<feature-name>/` and completing its request, impact, requirements, user-flow, technical-design, implementation-plan, and status records. Add `DATABASE_PLAN.md` when persistence schema changes are proposed.

No feature may be implemented directly from a short request. The source code remains the final source of truth, and existing components, repositories, models, services, navigation, permissions, database, DataStore, notifications, and design tokens must be discovered before creating new ones. Work in small logical groups; after each group compile, run focused tests, inspect the diff, and stop on critical failures.

Any feature affecting TDLib, JNI, ABI, authentication, Upload Engine, WorkManager, database, or security architecture is HIGH RISK or CRITICAL according to actual impact. Stop implementation and create a specialized technical plan before continuing. Database changes require a version increment, migration, migration tests, upgrade tests, and data-preservation verification. Telegram changes require boundary/integration coverage; upload changes require upload regression coverage.

All user-visible strings must support English and Arabic, preserve RTL/LTR behavior, and use resources rather than hardcoded text. UI changes must preserve Material 3, dark mode, accessibility, and loading/empty/error/success states. Performance changes require measurement against a baseline rather than assumption.

The feature status must be one of `PLANNED`, `IN_PROGRESS`, `TESTING`, `BLOCKED`, `READY_FOR_RELEASE`, or `RELEASED`. The release decision must be explicitly recorded as `READY_FOR_RELEASE` or `BLOCKED`; no feature is released automatically. Before review, run the applicable focused tests, `./scripts/verify-project.sh`, security and TDLib/resource gates, the complete diff review, and documentation checks. Report exact results and remaining risks using the feature status and final review record.
