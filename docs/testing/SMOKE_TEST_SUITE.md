# Smoke Test Suite

| Check | Status | Evidence |
|---|---|---|
| Application starts | NOT VERIFIED | Requires device/emulator execution |
| Authentication state loads | NOT VERIFIED | Requires real TDLib runtime session |
| Telegram initializes | NOT VERIFIED | Device JNI/runtime evidence required |
| File selection works | NOT VERIFIED | Requires Android picker execution |
| Upload starts | NOT VERIFIED | Requires real Telegram upload |
| Upload completes | NOT VERIFIED | Requires real Telegram delivery confirmation |
| Queue updates | NOT VERIFIED | Requires runtime worker execution |
| History updates | NOT VERIFIED | Requires end-to-end completed task |

The release workflow provides build-time, lint, JVM, TDLib-artifact, and signature evidence, but these results must not be substituted for a real-device smoke test. No smoke result is fabricated.
