# Documentation Index

This index lists the **living documentation** — the documents that describe how the
project works today and that must be updated when behavior changes.

Point-in-time records (inventories, final reports, audits, dated maintenance records, and
per-version release records) live under [`archive/reports/`](archive/reports/), which
mirrors the area names used below. Earlier superseded material lives under
[`archive/`](archive/).

All relative links in this documentation tree are verified by
[`scripts/check-doc-links.py`](../scripts/check-doc-links.py), which runs in CI.

## Onboarding

| Document | Purpose |
|---|---|
| [DEVELOPER_ONBOARDING.md](DEVELOPER_ONBOARDING.md) | Set up the project and run a first successful build |
| [DEVELOPMENT_RULES.md](DEVELOPMENT_RULES.md) | Engineering rules applied to every change |

## Architecture and design

| Area | Location | Entry points |
|---|---|---|
| Architecture | [`architecture/`](architecture/) | [TARGET_ARCHITECTURE.md](architecture/TARGET_ARCHITECTURE.md), [DATA_FLOW.md](architecture/DATA_FLOW.md), [DEPENDENCY_GRAPH.md](architecture/DEPENDENCY_GRAPH.md), [TELEGRAM_ARCHITECTURE.md](architecture/TELEGRAM_ARCHITECTURE.md), [UPLOAD_ARCHITECTURE.md](architecture/UPLOAD_ARCHITECTURE.md), [WORKMANAGER_ARCHITECTURE.md](architecture/WORKMANAGER_ARCHITECTURE.md), [NAVIGATION_ARCHITECTURE.md](architecture/NAVIGATION_ARCHITECTURE.md), [DI_GRAPH.md](architecture/DI_GRAPH.md) |
| Design system | [`design/`](design/) | [DESIGN_SYSTEM.md](design/DESIGN_SYSTEM.md), [DESIGN_PRINCIPLES.md](design/DESIGN_PRINCIPLES.md), [M3_COMPONENT_MAP.md](design/M3_COMPONENT_MAP.md), [M3_SCREEN_DESIGNS.md](design/M3_SCREEN_DESIGNS.md), [MOTION_SYSTEM.md](design/MOTION_SYSTEM.md), [RTL_DESIGN_GUIDELINES.md](design/RTL_DESIGN_GUIDELINES.md), [SCREEN_STATE_MATRIX.md](design/SCREEN_STATE_MATRIX.md) |

## Runtime behavior

| Area | Location | Entry points |
|---|---|---|
| Upload pipeline and state | [`upload/`](upload/) | [ACTUAL_UPLOAD_STATE_MACHINE.md](upload/ACTUAL_UPLOAD_STATE_MACHINE.md), [DIRECT_TDLIB_UPLOAD.md](upload/DIRECT_TDLIB_UPLOAD.md), [END_TO_END_UPLOAD_FLOW.md](upload/END_TO_END_UPLOAD_FLOW.md), [QUEUE_CONSISTENCY.md](upload/QUEUE_CONSISTENCY.md), [UPLOAD_RECOVERY.md](upload/UPLOAD_RECOVERY.md), [SCHEDULER_ARCHITECTURE.md](upload/SCHEDULER_ARCHITECTURE.md), [NOTIFICATION_ARCHITECTURE.md](upload/NOTIFICATION_ARCHITECTURE.md), [HISTORY_ARCHITECTURE.md](upload/HISTORY_ARCHITECTURE.md) |
| Telegram integration | [`telegram/`](telegram/) | [AUTHENTICATION_ARCHITECTURE.md](telegram/AUTHENTICATION_ARCHITECTURE.md), [AUTHENTICATION_FLOW.md](telegram/AUTHENTICATION_FLOW.md), [SESSION_LIFECYCLE.md](telegram/SESSION_LIFECYCLE.md), [DESTINATION_ARCHITECTURE.md](telegram/DESTINATION_ARCHITECTURE.md), [DESTINATION_FLOW.md](telegram/DESTINATION_FLOW.md) |
| Background execution | [`background/`](background/) | [README.md](background/README.md), [BACKGROUND_ARCHITECTURE.md](background/BACKGROUND_ARCHITECTURE.md), [RELIABILITY_AND_RECOVERY.md](background/RELIABILITY_AND_RECOVERY.md) |
| File handling | [`files/`](files/) | [README.md](files/README.md), [FILE_FLOW.md](files/FILE_FLOW.md), [TEMPORARY_FILE_POLICY.md](files/TEMPORARY_FILE_POLICY.md) |
| Application lifecycle | [`lifecycle/`](lifecycle/) | [APP_LIFECYCLE.md](lifecycle/APP_LIFECYCLE.md), [CRASH_RECOVERY.md](lifecycle/CRASH_RECOVERY.md), [STATE_RESTORATION_MATRIX.md](lifecycle/STATE_RESTORATION_MATRIX.md) |
| Error handling | [`errors/`](errors/) | [ERROR_FLOW_ARCHITECTURE.md](errors/ERROR_FLOW_ARCHITECTURE.md), [ERROR_TEST_MATRIX.md](errors/ERROR_TEST_MATRIX.md) |

## Quality, platform, and governance

| Area | Location | Entry points |
|---|---|---|
| Testing | [`testing/`](testing/) | [QA_GUIDE.md](testing/QA_GUIDE.md), [TEST_COVERAGE_MAP.md](testing/TEST_COVERAGE_MAP.md), [SMOKE_TEST_SUITE.md](testing/SMOKE_TEST_SUITE.md), [REGRESSION_MATRIX.md](testing/REGRESSION_MATRIX.md), [CRITICAL_PATHS.md](testing/CRITICAL_PATHS.md), [FLAKY_TESTS.md](testing/FLAKY_TESTS.md) |
| Runtime evidence | [`evidence/`](evidence/) | [PHASE07_EVIDENCE.md](evidence/PHASE07_EVIDENCE.md) and the earlier phase evidence records |
| Security | [`security/`](security/) | [THREAT_MODEL.md](security/THREAT_MODEL.md), [SECURITY_ARCHITECTURE.md](security/SECURITY_ARCHITECTURE.md), [BACKUP_SECURITY.md](security/BACKUP_SECURITY.md), [ONGOING_SECURITY_POLICY.md](security/ONGOING_SECURITY_POLICY.md) |
| Privacy and data governance | [`privacy/`](privacy/) | [README.md](privacy/README.md), [DATA_FLOW_MAP.md](privacy/DATA_FLOW_MAP.md), [DATA_RETENTION_POLICY.md](privacy/DATA_RETENTION_POLICY.md), [PRIVACY_GOVERNANCE.md](privacy/PRIVACY_GOVERNANCE.md) |
| Observability and diagnostics | [`observability/`](observability/) | [OBSERVABILITY_ARCHITECTURE.md](observability/OBSERVABILITY_ARCHITECTURE.md), [EVENT_CATALOG.md](observability/EVENT_CATALOG.md), [PRIVACY_LOGGING_POLICY.md](observability/PRIVACY_LOGGING_POLICY.md) |
| Performance | [`performance/`](performance/) | [PERFORMANCE_GUIDE.md](performance/PERFORMANCE_GUIDE.md), [PERFORMANCE_BASELINE.md](performance/PERFORMANCE_BASELINE.md), [OPTIMIZATION_LOG.md](performance/OPTIMIZATION_LOG.md) |
| Android compatibility | [`compatibility/`](compatibility/) | [README.md](compatibility/README.md), [SUPPORTED_ENVIRONMENT.md](compatibility/SUPPORTED_ENVIRONMENT.md), [ANDROID_VERSION_MATRIX.md](compatibility/ANDROID_VERSION_MATRIX.md), [DEVICE_AND_SCREEN_MATRIX.md](compatibility/DEVICE_AND_SCREEN_MATRIX.md), [PLATFORM_BEHAVIOR.md](compatibility/PLATFORM_BEHAVIOR.md) |
| Accessibility and adaptive UI | [`accessibility/`](accessibility/) | [ACCESSIBILITY_GUIDE.md](accessibility/ACCESSIBILITY_GUIDE.md), [ADAPTIVE_UI_GUIDE.md](accessibility/ADAPTIVE_UI_GUIDE.md), [KNOWN_ACCESSIBILITY_LIMITATIONS.md](accessibility/KNOWN_ACCESSIBILITY_LIMITATIONS.md) |
| Internationalization | [`i18n/`](i18n/), [`localization/`](localization/) | [I18N_GUIDE.md](i18n/I18N_GUIDE.md), [TIMEZONE_POLICY.md](i18n/TIMEZONE_POLICY.md), [TERMINOLOGY.md](localization/TERMINOLOGY.md), [LOCALE_FORMATTING.md](localization/LOCALE_FORMATTING.md) |
| Resources | [`resources/`](resources/) | [RESOURCE_ARCHITECTURE.md](resources/RESOURCE_ARCHITECTURE.md), [UNUSED_RESOURCES.md](resources/UNUSED_RESOURCES.md), [DUPLICATE_ASSETS.md](resources/DUPLICATE_ASSETS.md), [LARGE_ASSETS.md](resources/LARGE_ASSETS.md) |
| Dependencies | [`dependencies/`](dependencies/) | [CURRENT_TOOLCHAIN.md](dependencies/CURRENT_TOOLCHAIN.md), [TDLIB_NATIVE_DEPENDENCIES.md](dependencies/TDLIB_NATIVE_DEPENDENCIES.md) |

## Delivery and operations

| Area | Location | Entry points |
|---|---|---|
| CI | [`ci/`](ci/) | [CI_GUIDE.md](ci/CI_GUIDE.md), [BRANCH_PROTECTION.md](ci/BRANCH_PROTECTION.md) |
| Release | [`release/`](release/) | [README.md](release/README.md), [RELEASE_CHECKLIST.md](release/RELEASE_CHECKLIST.md), [SIGNING_GUIDE.md](release/SIGNING_GUIDE.md), [SECRET_MANAGEMENT.md](release/SECRET_MANAGEMENT.md), [KNOWN_LIMITATIONS.md](release/KNOWN_LIMITATIONS.md), [RELEASE_NOTES.md](release/RELEASE_NOTES.md) |
| Operations | [`operations/`](operations/) | [README.md](operations/README.md), [MAINTENANCE_INDEX.md](operations/MAINTENANCE_INDEX.md), [CHANGE_RISK_MATRIX.md](operations/CHANGE_RISK_MATRIX.md), [CHANGE_MANAGEMENT.md](operations/CHANGE_MANAGEMENT.md), [CODE_REVIEW_CHECKLIST.md](operations/CODE_REVIEW_CHECKLIST.md), [LINT_CI_REVIEW.md](operations/LINT_CI_REVIEW.md), [PRE_PUSH_CHECKLIST.md](operations/PRE_PUSH_CHECKLIST.md), [INCIDENT_RESPONSE.md](operations/INCIDENT_RESPONSE.md), [ROLLBACK_PLAN.md](operations/ROLLBACK_PLAN.md), [HOTFIX_PROCEDURE.md](operations/HOTFIX_PROCEDURE.md), [POST_RELEASE_MONITORING.md](operations/POST_RELEASE_MONITORING.md), [DIAGNOSTIC_GUIDE.md](operations/DIAGNOSTIC_GUIDE.md), [TECHNICAL_DEBT.md](operations/TECHNICAL_DEBT.md) |
| Maintenance | [`maintenance/`](maintenance/) | [README.md](maintenance/README.md), [COMPREHENSIVE_MAINTENANCE_LOG.md](maintenance/COMPREHENSIVE_MAINTENANCE_LOG.md), [GITHUB_SIGNED_RELEASE_AUTOMATION.md](maintenance/GITHUB_SIGNED_RELEASE_AUTOMATION.md), [REPOSITORY_CERTIFICATION.md](maintenance/REPOSITORY_CERTIFICATION.md) |

## Controlled change records

These folders hold the most recent controlled-change records together with the templates
that define each workflow:

| Workflow | Location | Entry points |
|---|---|---|
| Feature development | [`features/`](features/) | [PINNED_CHANNELS.md](features/PINNED_CHANNELS.md) (most recent record) and the templates in [`features/_template/`](features/_template/README.md) |
| Bug fixing | [`bugs/_template/`](bugs/_template/README.md) | Templates: [BUG_REPORT.md](bugs/_template/BUG_REPORT.md), [ROOT_CAUSE.md](bugs/_template/ROOT_CAUSE.md), [HIGH_RISK_PLAN.md](bugs/_template/HIGH_RISK_PLAN.md), [BUG_FIX_REPORT.md](bugs/_template/BUG_FIX_REPORT.md) |
| Dependency updates | [`dependencies/`](dependencies/) | [CURRENT_TOOLCHAIN.md](dependencies/CURRENT_TOOLCHAIN.md) and the templates in [`dependencies/_template/`](dependencies/_template/README.md) |

## History

| Location | Content |
|---|---|
| [`archive/reports/`](archive/reports/) | Point-in-time inventories, final reports, audits, dated maintenance records, and per-version release records, grouped by area |
| [`archive/`](archive/) | Earlier superseded audits, SPRINT/MANUS reports, and dated one-off documents |

## Adding documentation

1. Put living documents in the area folder that owns the behavior.
2. Put point-in-time records under `archive/reports/<area>/`.
3. Add an entry to this index so the document is discoverable.
4. Run `python3 scripts/check-doc-links.py` before committing.


