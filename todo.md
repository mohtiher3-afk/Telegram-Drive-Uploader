
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

- [ ] Inspect the latest repository, diagnostics, and browser-visible gateway state.
- [ ] Identify remaining defects in channel discovery, WorkManager startup, file access, and real TDLib delivery.
- [ ] Apply targeted fixes with regression tests and validate Android CI.
- [ ] Recheck the browser-visible project without performing a real external upload.
