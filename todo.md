
## Direct TDLib upload path

- [x] Replace the local upload simulator with real TDLib UploadFile and SendMessage flow.
- [x] Add TDLib channel discovery and destination selection for public and private channels.
- [x] Track real UpdateFile progress and message completion in the queue.
- [x] Keep fail-closed behavior for missing native libraries, unauthenticated TDLib, and invalid destinations.
- [x] Add direct-TDLib upload tests and update release documentation.

- [x] Refine channel selection UX with complete localized labels, explicit loading/empty states, and clear direct-upload context.
- [x] Bump the Android app version to 1.0.7 and align release notes with official TDLib v1.8.66.
- [ ] Validate local Gradle tests and artifact gates; GitHub's signed multi-ABI workflow is verified successfully.
- [x] Trigger and verify the signed GitHub Release v1.0.7.
- [x] Provide physical-device test instructions for Telegram authorization, channel selection, staging, and upload progress.

- [x] Trace and remove every simulated upload progress, success, and completion path.
- [x] Require genuine TDLib UpdateFile and message/file confirmation before marking uploads complete.
- [x] Add regression tests proving failed or unconfirmed sends never report success.
- [x] Build and verify the permanent real-upload fix across ARM64, ARMv7, and x86_64.

- [x] Publish a new signed release containing the permanent confirmed-delivery fix; do not reuse the old v1.0.7 tag.

- [x] Add real elapsed upload-time tracking from TDLib transfer start through confirmed message delivery.
- [x] Ensure upload UI and history never show completion or timing from local staging alone.
- [x] Remove dual-Wi-Fi UI, monitoring logic, permissions, tests, and documentation from the Android app.
- [x] Add regression tests for real elapsed timing and absence of dual-Wi-Fi behavior.
- [x] Build and verify the updated app across ARM64, ARMv7, and x86_64.

- [x] Publish a new signed release containing real upload timing and the removed dual-Wi-Fi feature.

- [x] Calculate current upload speed from real TDLib UpdateFile progress samples.
- [x] Calculate estimated remaining time only when speed and remaining bytes are sufficient.
- [x] Display speed and ETA beside the real upload progress bar with Arabic/localized labels.
- [x] Add regression tests for speed, ETA, stalled transfers, and confirmed completion.
- [x] Build and verify the Android app across ARM64, ARMv7, and x86_64.
- [x] Publish a fresh signed Android release containing the real upload speed and ETA UI.

- [x] Collect available Android/CI logs and scan for memory pressure, OOM, and upload-resource warnings; no leak signatures were found.
- [x] Inspect long-upload resource lifecycle for stream, coroutine, TDLib client, and WorkManager leaks; cleanup paths are bounded and explicit.
- [x] Review evidence-based leak safeguards and regression coverage; no concrete leak requiring a code change was found.
- [x] Document the evidence limits and physical-device profiler steps required for definitive leak confirmation.

- [ ] Trace WorkManager enqueue, worker registration, Hilt factory, constraints, and startup logs for tasks stuck at Enqueued.
- [ ] Fix the verified WorkManager startup or constraint defect without bypassing real TDLib confirmation.
- [ ] Add safe worker-start and worker-failure diagnostics plus regression coverage.
- [ ] Build and verify the repaired queue path across ARM64, ARMv7, and x86_64.
- [ ] Publish a new signed Android release containing the WorkManager enqueue fix and corrected diagnostics version.
- [ ] Publish a fresh signed Android release containing the WorkManager enqueue repair and diagnostic version fix.

## Telegram channel delivery and video upload blocker

- [ ] Trace the complete video path from picker and metadata through WorkManager, TDLib upload, and channel message delivery.
- [ ] Verify channel destination resolution and permissions using the official TDLib path.
- [ ] Repair genuine video sending with terminal confirmation from TDLib and preserve progress, speed, ETA, retry, and failure states.
- [ ] Add regression tests for video message construction, channel destination handling, and confirmed completion semantics.
- [ ] Run multi-ABI CI and document the physical-device test that verifies the message appears in the Telegram channel.

## Channel search and upload failure report

- [ ] Investigate why channels are absent from search results, including local chat loading limits and missing global username search.
- [ ] Investigate why the upload path still does not execute or deliver the selected video to the chosen channel.
- [ ] Implement evidence-based fixes and add regression tests for channel search, channel destination IDs, and real upload completion.
- [ ] Re-run multi-ABI CI and document the exact device diagnostics needed if the physical device still fails.

## Apply fixes and browser inspection

- [ ] Apply the requested channel-search, channel-permission, and real-video-upload changes in the Android repository.
- [ ] Inspect the browser-visible gateway and setup pages without performing a real external upload or sensitive submission.
- [ ] Run available automated validation and report any environment or physical-device limitations honestly.

## Multi-format video upload requirement

- [ ] Inspect picker MIME filters, extension fallback, metadata extraction, and TDLib video/document classification.
- [ ] Support multiple video MIME types and common extensions without fake conversion or forced re-encoding.
- [ ] Add regression tests for format recognition and real confirmed delivery.
- [ ] Run multi-ABI CI and document codec/container limitations for physical-device testing.

## Repository upload request

- [x] Review the current multi-format video changes and repository version state.
- [x] Push the new version to the selected GitHub repository.
- [x] Verify the triggered multi-ABI CI run and report the available APK artifacts.

## Primary-flow reliability improvement request

- [ ] Audit why the installed app still does not complete the required channel discovery and real video upload flow.
- [ ] Improve channel selection, upload lifecycle feedback, retry behavior, and actionable error reporting.
- [ ] Preserve official TDLib and confirmed delivery semantics; do not add simulated success.
- [ ] Add regression coverage and publish a validated new Android build.

## Repository and browser correction request

- [x] Inspect the latest repository, diagnostics, and browser-visible gateway state.
- [x] Identify remaining defects in channel discovery, WorkManager startup, file access, and real TDLib delivery.
- [x] Apply targeted fixes with regression tests and validate Android CI.
- [x] Recheck the browser-visible project without performing a real external upload.

## Android emulator JNI smoke-test runner blocker

- [ ] Inspect why the TDLib JNI smoke-test job is waiting for `ubuntu-24.04-4core`.
- [ ] Verify repository runner availability and workflow label configuration.
- [ ] Apply a safe scheduling correction or document the required self-hosted runner setup.
- [ ] Recheck the smoke-test workflow status and report whether the test actually executed.

## Cancelled emulator smoke-test check

- [ ] Determine why the Android TDLib emulator smoke test was cancelled while all ABI builds succeeded.
- [ ] Correct concurrency or workflow triggering so the smoke test receives a final result.
- [ ] Verify a subsequent smoke-test run and document whether JNI validation actually executed.

## Repository-wide error audit

- [ ] Audit source, Gradle, workflows, TDLib integration, WorkManager, channel search, and upload paths.
- [ ] Reproduce or classify confirmed errors and separate blockers from warnings.
- [ ] Apply targeted fixes and add regression coverage.
- [ ] Run local and GitHub Actions validation before delivery.

## Manual TDLib JNI smoke test

- [ ] Dispatch the Android TDLib Device Smoke Test for the latest repository state.
- [ ] Monitor the cloud emulator job until it completes or reports a runner/environment failure.
- [ ] Inspect explicit `JNI_LOAD_STATUS` and `CLIENT_CREATE_STATUS` markers and report the result.

## Confirmed JNI dependency blocker

- [ ] Inspect `libtdjni.so` ELF NEEDED entries for every ABI and compare them with APK contents.
- [ ] Add or rebuild official-source-compatible OpenSSL/zlib native dependencies required by TDLib without using simulated binaries.
- [ ] Update artifact checks, Gradle packaging, and smoke-test validation for the complete native dependency set.
- [ ] Re-run cloud emulator JNI smoke test and verify `JNI_LOAD_STATUS=PASS` and `CLIENT_CREATE_STATUS=PASS`.

## Repository screenshot gallery

- [x] Create truthful application screenshots for the key Android flows and upload them to the repository.
- [x] Add a README screenshot gallery with captions and GitHub-relative image links.
- [x] Verify image files, README rendering links, and repository cleanliness before delivery.
