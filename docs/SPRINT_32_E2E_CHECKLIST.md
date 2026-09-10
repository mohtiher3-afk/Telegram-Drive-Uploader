# Sprint 32 — Real Device E2E Checklist (manual, real account only)

APK: fresh `assembleDebug` → `%TEMP%\tdg-build\app\build\outputs\apk\debug\app-debug.apk`
(`buildDir` is redirected to temp; see root `build.gradle.kts:9`).
No Telegram secrets are stored in the repo; enter credentials on-device only.

- [ ] Install clean build (uninstall previous `com.aistudio.telegramdrive.prmuq` first)
- [ ] Launch app, no crash, no `UnsatisfiedLinkError`
      (native load already proven 4/4 on cloud emulators API 33–36)
- [ ] Real Telegram login (phone → code → 2FA password if enabled)
- [ ] Authorization reaches READY, `GetMe` shows your real user
- [ ] Session restored after app restart (no re-login)
- [ ] Real chats discovered (your actual channels/groups/users)
- [ ] Real destination selected (verify chat title matches Telegram)
- [ ] Real video selected via system picker
- [ ] Upload job queued, worker starts, notification shows
- [ ] Real TDLib progress received (bytes climb, not timer-based)
- [ ] Preliminary upload completes, send starts
- [ ] Telegram confirms message (`UpdateMessageSendSucceeded`, message link)
- [ ] Real message ID obtained (open `t.me/c/...` link, verify video plays)
- [ ] Database marked COMPLETED only after confirmation (History screen)
- [ ] Restart app, record persists with same status/link
- [ ] Verify the video in Telegram (other client)

Record each item PASS/FAIL with timestamp. Any FAIL → file the exact
DiagnosticsManager category + message; do not retry blindly.
