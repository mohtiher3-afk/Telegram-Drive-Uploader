# Historical Audit Register

**Purpose.** This file is the single surviving record of the controlled-maintenance
audit programme that previously lived as 23 individual `FINAL_*_REPORT.md` documents
under `docs/archive/reports/`. The archive has been removed from the working tree to
stop documentation from outweighing the code (358 markdown files against 146 Kotlin
files). The full original text is not lost: it remains in git history and can be
recovered with `git log --diff-filter=D --name-only -- docs/archive`.

**These are point-in-time records, not current facts.** The reports were written
against an earlier build (versionName 1.0.15 / versionCode 15, minSdk 24, compileSdk 36,
Gradle 8.9, JDK 17). The project has since moved to versionName 1.0.25, minSdk 30,
compileSdk 37, Gradle 9.6, and JDK 21. Where a report states a configuration value,
treat it as historical.

## Verdict legend

| Verdict | Meaning |
|---|---|
| `CONDITIONALLY VERIFIED` | Static review found no defect, but runtime evidence is absent. |
| `NOT VERIFIED` | The claim was not tested at all. |
| `DOCUMENTED` | Scope recorded; no pass/fail asserted. |
| `PENDING` | Blocked on tooling, device, or CI. |

## Register

| # | Area | Source file | Verdict |
|---|---|---|---|
| 1 | Comprehensive audit | `reports/final-audit/FINAL_AUDIT_REPORT.md` | NOT READY for release |
| 2 | Verification matrix | `reports/final-audit/FINAL_VERIFICATION_MATRIX.md` | CONDITIONALLY READY |
| 3 | Repository structure | `reports/final-audit/FINAL_REPOSITORY_STRUCTURE.md` | DOCUMENTED |
| 4 | Accessibility / adaptive UI | `reports/accessibility/FINAL_ACCESSIBILITY_REPORT.md` | CONDITIONALLY VERIFIED |
| 5 | Background execution | `reports/background/FINAL_BACKGROUND_REPORT.md` | RUNTIME VALIDATION PENDING |
| 6 | CI/CD | `reports/ci/FINAL_CI_REPORT.md` | PENDING (remote run) |
| 7 | Compatibility | `reports/compatibility/FINAL_COMPATIBILITY_REPORT.md` | RUNTIME CERTIFICATION PENDING |
| 8 | Error handling | `reports/errors/FINAL_ERROR_HANDLING_REPORT.md` | CONDITIONALLY VERIFIED |
| 9 | File handling / large files | `reports/files/FINAL_FILE_RELIABILITY_REPORT.md` | RUNTIME VALIDATION PENDING |
| 10 | i18n / locale | `reports/i18n/FINAL_I18N_REPORT.md` | CONDITIONALLY VERIFIED |

## Confirmed defects the programme surfaced

These were identified as code-level issues, not environment limitations. They were
documented but not fixed, and they remain the highest-value carry-over from the
archive. Each needs its own reproduce-then-fix cycle.

| Defect | Source | Status |
|---|---|---|
| `UploadDao.updateProgress()` writes `status = 'UPLOADING'` unconditionally, so an in-flight progress callback can overwrite `PAUSED`, `CANCELLED`, `FAILED`, or even `COMPLETED` | #22 | Open |
| Progress unit mismatch: engine/Room persist `0..100`, the UI expects a `0..1` fraction | #22, #21 | Fixed in that phase; confirm still correct |
| No centralised transition fencing - the state table describes call paths, not an enforced algebra | #22 | Open |
| Room insert happens before WorkManager enqueue; a failure in between leaves a durable `QUEUED` row with no runnable work and no reconciler | #21, #22 | Open |
| `STUCK UPLOAD DETECTION NOT IMPLEMENTED` | #22 | Open |
| `DiagnosticEvent.appVersion` is hardcoded `1.0.0` while exported diagnostics use `BuildConfig.VERSION_NAME` | #13 | Open |
| `TELEGRAM_AUTH_STATE` diagnostic is defined but never confirmed to be emitted | #13 | Open |
| No pre-send destination existence check, so a removed/inaccessible chat surfaces only as a send failure | #19 | Open |
| No destination pagination; chat loading is capped at 100 main-list chats | #19 | Open |
| History single-delete has no status guard, so it can delete an in-flight record | #23 | Open |
| Empty `catch (_: Exception) {}` around `takePersistableUriPermission` | #21 | Accepted by design |

## Coverage gaps recorded at the time

The QA pass (#20) marked these as `MISSING COVERAGE`, and they were the reason the
programme could not certify a release:

- No ViewModel test suite
- No repository test suite
- No isolated Room test suite
- No DataStore persistence/corruption suite
- No WorkManager queue recovery / process-death suite
- No Compose UI suite
- No scheduler suite
- No network-failure integration suite
- No accessibility, RTL runtime, dark-mode contrast, or large-font suite

**Reading the current tree:** the repository now carries a materially larger JVM test
suite than at the time of these reports, so several gaps may have been closed since.
Verify against the tree before acting on any line above.

## What the programme never established

No report claimed runtime proof. Explicitly **NOT VERIFIED** across the programme:
real Telegram authentication, session restoration after process death, logout and
re-login, real upload delivery, background execution across process death and device
restart, network loss recovery, TalkBack and accessibility services, RTL runtime
layout, dark-mode contrast, tablet and landscape layout, large-file upload, and
performance measurement of any kind.

A green static build is not evidence for any of these. Per `AGENTS.md`, anything
touching TDLib, WorkManager, upload state, or authentication requires a device check
before being marked resolved.

## Superseded records

Also removed from the working tree and retained only in git history: the baseline,
phase, sprint, and evidence documents under `docs/archive/`. Several quote
configuration values that no longer hold (see the banner at the top of this file).
Use them for provenance, not as a description of the current build.

| 11 | Lifecycle / crash recovery | `reports/lifecycle/FINAL_LIFECYCLE_REPORT.md` | CONDITIONALLY VERIFIED |
| 12 | Repository cleanup | `reports/maintenance/FINAL_CLEANUP_REPORT.md` | PASS (phase 27) |
| 13 | Observability | `reports/observability/FINAL_OBSERVABILITY_REPORT.md` | DOCUMENTED |
| 14 | Self-check system | `reports/operations/FINAL_SELF_CHECK_REPORT.md` | CONDITIONALLY READY |
| 15 | Performance | `reports/performance/FINAL_PERFORMANCE_REPORT.md` | NOT MEASURED - TOOLING UNAVAILABLE |
| 16 | Privacy / data governance | `reports/privacy/FINAL_PRIVACY_REPORT.md` | DOCUMENTED WITH LIMITATIONS |
| 17 | Security | `reports/security/FINAL_SECURITY_REPORT.md` | PASS (0 critical/high confirmed) |
| 18 | Telegram authentication | `reports/telegram/FINAL_AUTHENTICATION_REPORT.md` | NOT VERIFIED |
| 19 | Telegram destination routing | `reports/telegram/FINAL_DESTINATION_REPORT.md` | CONDITIONALLY VERIFIED |
| 20 | QA / test inventory | `reports/testing/FINAL_QA_REPORT.md` | DOCUMENTED |
| 21 | End-to-end upload | `reports/upload/FINAL_END_TO_END_UPLOAD_REPORT.md` | CONDITIONALLY VERIFIED |
| 22 | Upload state / queue consistency | `reports/upload/FINAL_UPLOAD_STATE_REPORT.md` | CONDITIONALLY VERIFIED |
| 23 | History / scheduler / notification | `reports/upload/FINAL_HISTORY_SCHEDULER_NOTIFICATION_REPORT.md` | CONDITIONALLY VERIFIED |
