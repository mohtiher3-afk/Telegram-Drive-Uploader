
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

- [ ] Trace and remove every simulated upload progress, success, and completion path.
- [ ] Require genuine TDLib UpdateFile and message/file confirmation before marking uploads complete.
- [ ] Add regression tests proving failed or unconfirmed sends never report success.
- [ ] Build and verify the permanent real-upload fix across ARM64, ARMv7, and x86_64.
