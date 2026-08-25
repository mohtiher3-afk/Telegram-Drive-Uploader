# Upload Event Notification Maintenance Record

## Scope and evidence

| Field | Record |
|---|---|
| Request | Post Android notifications when a real upload is confirmed complete or fails permanently. |
| Confirmed write point | `feature/upload/worker/UploadWorker.kt` writes `COMPLETED` only after `UploadEngineResult.Success`, which is backed by confirmed TDLib delivery; it writes terminal `FAILED` only for permanent engine error, unconfirmed stream termination, or exhausted exception retries. |
| Current notification state | No Android notification implementation, channel, or `POST_NOTIFICATIONS` declaration exists. Existing onboarding requests media permission only. |
| Privacy boundary | Notification copy must not reveal filenames, Telegram destinations, account details, TDLib errors, phone numbers, or credentials on the lock screen. |

## Design decision

Introduce a small `UploadEventNotifier` boundary. The worker will emit only `COMPLETED` and permanent `FAILED` events after the existing repository status write. The Android implementation will create one `upload_status` channel, check system notification availability and Android 13+ permission before posting, and reuse a stable per-upload notification ID to update instead of duplicate alerts. The existing onboarding permission request includes Android 13+ notifications for new users, while Settings exposes an explicit request/action route for existing users.

`RETRYING`, `PREPARING`, local worker completion, staging completion, and provisional TDLib events must never generate a terminal notification. A notification remains an observation of stored terminal state—not a new authority over upload state.

## Protected behavior

| Area | Protected behavior |
|---|---|
| TDLib and delivery | Confirmed `UploadEngineResult.Success` remains the only completion signal. No direct TDLib change is permitted. |
| Queue and persistence | Room task IDs, WorkManager policy, retries, cancellation, progress, and upload state writes remain unchanged. |
| Privacy | Notification payloads are generic and localized; no filename, destination, username, phone number, or exception text is shown. |
| Permission | `POST_NOTIFICATIONS` is declared and requested only through the existing Activity Result flow. Denial keeps the app functional and results in no post. |
| Release | No Gradle dependency, signing, ABI, CI, native, or schema change is included. |

## Change record

| Field | Record |
|---|---|
| Smallest safe change | Add a Hilt-bound notifier interface/implementation, a pure terminal-event policy with unit tests, a notification channel/icon/resources, and a version-aware user permission entry point in onboarding/settings. |
| Risk | Notification duplication during retries/process recreation, privacy leakage, Android 13 denial, and unobserved channel suppression. |
| Validation | Focused policy tests; resource and manifest checks; `:app:compileDebugKotlin`; `:app:testDebugUnitTest`; static protected-path review. Device notification permission, channel state, lock-screen behavior, and real Telegram delivery remain runtime checks. |
| Reversal | Revert notifier, policy, worker injection, permission/UI/resource changes, and this record. No persisted schema or upload-engine change exists. |

## Verification status

| Layer | Status | Notes |
|---|---|---|
| Repository evidence | PASS | Worker, Manifest, onboarding, settings, dependency, and notification absence were inspected. |
| Build/test | PASS | `:app:compileDebugKotlin` and `:app:testDebugUnitTest` passed with JDK 17, Android SDK 36, one Gradle worker, and an explicit 1 GiB Gradle heap cap. `UploadEventNotificationPolicyTest` passed. |
| Device notification permission/channel | NOT VERIFIED | Requires Android device or emulator. |
| Real Telegram delivery | NOT VERIFIED / OUT OF SCOPE | This phase preserves—not exercises—credentials and delivery. |

## Post-implementation review

`git diff --check` passed. The manifest declares `POST_NOTIFICATIONS` once, and English/Arabic resource files each contain the six required notification strings. The worker calls the notifier only after writing `COMPLETED` or terminal `FAILED`; retrying paths do not notify. An initial unconstrained Gradle attempt was terminated under sandbox memory pressure; the successful verification used a command-line heap override only and did not alter project build configuration.
