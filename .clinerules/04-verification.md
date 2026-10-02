# Verification Rules

## Before completion
- Run the narrowest relevant test/build first.
- Then run the repository verification appropriate to the change.
- UI changes require Compose/semantics or screenshot validation when available.
- Changes to TDLib, WorkManager, authentication, upload state, queue persistence, notifications, or background execution require runtime/device evidence before being called resolved.

## Project verification
Prefer the repository scripts:
- Windows: `scripts/verify-project.ps1 -Mode QUICK`
- Full: `scripts/verify-project.sh FULL`
- Release: `scripts/verify-project.sh RELEASE`

Also preserve the existing security/resource/WorkManager/TDLib gates.

## UI regression matrix
Check:
- dark theme
- Arabic RTL and English LTR
- long filenames
- empty queue
- active transfer at 0%, middle, and 100%
- failed/retry state
- offline/network-loss state
- process recreation/navigation away and back
- small phone and large-screen/adaptive layout
- accessibility labels and visible disabled/loading states

## Evidence
Never write "verified" or "confirmed" without a test result, CI artifact, runtime log, or screenshot. If a device/credential/environment is unavailable, state the limitation instead.
