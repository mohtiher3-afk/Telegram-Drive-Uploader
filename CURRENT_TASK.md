# CURRENT_TASK

Phase 02 — Prove Real Upload Delivery (CRITICAL BLOCKER). Status: IN PROGRESS.

Device: Redmi Note 13 Pro+ 5G (arm64-v8a, Android 16 / SDK 36) — `BUFYHQZXR4BQK7WK`. Debug APK (1.0.24/24) installed via adb. App launches and renders UI.

Verified on device so far:
- [x] Debug signing fix (repo-root debug.keystore -> ~/.android fallback), commit 1f10b4c
- [x] Sentry auto-init crash fixed (io.sentry.auto-init=false + guarded manual init), commit 7fd01bd — app boots, no crash, `[APP_START] initialized`

BLOCKED ON (user-provided only):
1. `.env` at repo root with real TELEGRAM_API_ID / TELEGRAM_API_HASH (my.telegram.org). App currently shows "Telegram API credentials are not configured correctly" (TelegramClientImpl.kt:718).
2. Test Telegram phone number (login + SMS code on device).
3. Target Telegram channel.
4. Second device/account to visually confirm delivery.

Next after inputs: trace Logcat file pick -> enqueue -> Worker start -> TDLib UploadFile -> progress -> SendMessage -> messageId; then second-device confirmation.