# Comprehensive Controlled Maintenance Record — August 2026

## Scope and operating boundary

This record governs the approved comprehensive maintenance pass for the Android Telegram Drive Uploader. The objective is evidence-based audit and repair across authentication, destinations, file handling, upload state, background work, history, notifications, mobile UI, accessibility, JNI/ABI packaging, build, CI, and release readiness.

The protected runtime chain is:

> `UI → ViewModel → Repository → Room/DataStore → WorkManager → UploadWorker → TelegramUploadEngine → TelegramClient/TDLib → persisted status/history`

No change may replace real TDLib behavior, invent upload progress or completion, add credentials to source, alter stable numeric destination IDs, migrate Room data, weaken WorkManager semantics, substitute JNI artifacts, change signing/ABI scope, or automatically publish a release.

## Baseline

| Field | Observed baseline | Evidence state |
|---|---|---|
| Local and remote branch | `main` and `origin/main` both resolve to `02b13bc34d8fb335dbfc4cb9500c68483fa82fb3` | Repository verified |
| Worktree | Clean at baseline | Repository verified |
| Current application identity | `com.telegramdrive.uploader`, versionCode `18`, versionName `1.0.18` | Repository verified |
| Android compatibility settings | minSdk 24, compileSdk 36, targetSdk 36 | Repository verified |
| Declared APK ABIs | arm64-v8a, armeabi-v7a, x86_64 | Repository verified |
| Debug build evidence before this pass | `:app:assembleDebug :app:testDebugUnitTest` completed, with three Debug APKs produced | Build verified from the preceding maintenance slice |
| Secret and binary boundaries | `.env`, `*.keystore`, `.gradle/`, and `**/build/` are ignored | Repository verified |
| Existing workflows | `android-ci.yml`, `android-device-smoke.yml`, `android-release.yml` | Repository verified |

## Change record

| Field | Maintenance decision |
|---|---|
| Finding | Earlier reports record possible upload-progress representation and late-callback state risks, while later source changes introduced notification and mobile UI work. Historical reports may therefore be stale. |
| Hypothesis | Re-auditing the current `main` source by ownership boundary will distinguish current defects from superseded findings and permit only minimal targeted repairs. |
| Scope | Current `main` source, tests, resources, Gradle/build scripts, CI workflows, maintenance records, and local validation artifacts. |
| Protected behavior | Real TDLib authorization and delivery confirmation; app-private session paths; stable `destinationId: Long`; Room/WorkManager identity; streaming file path; existing signing and ABI gates; Arabic RTL and accessibility contracts. |
| Risks | Race conditions, state regression, file-provider loss, secret leakage, native ABI mismatch, regressions during UI compaction, false release-readiness claims, and unobserved device behavior. |
| Validation | Focused tests; Kotlin compilation; Debug packaging; release lint; repository artifact/security/resource checks; ABI inspection; CI review; and user-controlled device evidence where prerequisites exist. |
| Reversal | Revert only the focused commit for an evidence-backed repair; do not reset data, delete artifacts, remove tags, or change production secrets. |

## Evidence classification

| State | Meaning in this pass |
|---|---|
| Repository verified | The current tracked source, resource, workflow, or configuration directly proves the claim. |
| Build verified | A recorded command completed for the relevant current source and artifact. |
| Device/runtime verified | A controlled emulator or physical-device observation proves the scenario. |
| Not verified | The required device, account, channel, file, Android version, ABI, or runtime condition has not been observed. |
| Environment-limited | A required local or remote tool is not available; the constraint is retained with its output. |

## Initial open conditions

1. The Debug variant lacks usable Telegram API configuration, so it must fail closed rather than claiming real login readiness.
2. A local build or an x86_64 JNI smoke test does not prove authentication, destination permission, Worker execution, delivery, ARM runtime, or Android 16 runtime behavior.
3. The mobile-first Compose redesign is build-verified but requires fresh device screenshots and accessibility checks before visual acceptance is claimed.
4. Any older statement that notifications are absent must be reconciled against the currently tracked notification implementation before it is relied upon.

## Planned validation order

1. Establish repository ownership and reconcile maintenance records.
2. Audit authentication and destination routing boundaries.
3. Audit and repair only confirmed upload state or background-work defects.
4. Audit file, history, scheduler, and terminal-notification behavior.
5. Audit mobile UI, Arabic RTL, accessibility, and reduced motion.
6. Verify build, JNI, ABI artifacts, and environment boundaries.
7. Review CI, lint, and signed-release readiness without publication.
8. Produce a final evidence matrix and request only the user-controlled runtime evidence still required.

## References

[1]: https://github.com/mohtiher3-afk/Telegram-Drive-Uploader "Telegram Drive Uploader repository"
[2]: https://core.telegram.org/tdlib/docs/ "TDLib documentation"
[3]: https://developer.android.com/topic/libraries/architecture/workmanager "Android WorkManager documentation"
