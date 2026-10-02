# Diagnostic Guide

When a bug is reported, collect only the following non-secret context:

| Field | Example format |
|---|---|
| App version | `1.0.15` |
| Android version | API level and marketing version if known |
| Device | Manufacturer and model |
| Architecture | `arm64-v8a`, `armeabi-v7a`, or `x86_64` |
| Network condition | Wi-Fi/mobile/VPN/captive portal at a high level |
| Operation | Search, authentication, upload, queue, or settings |
| Error | Exact visible error text, excluding credentials |
| Reproduction | Numbered steps from clean start to failure |

If available, request the sanitized diagnostics export from the app. Do not request Telegram API credentials, bot tokens, phone numbers, login codes, passwords, session databases, private keys, or original private media. Preserve timestamps and incident IDs so logs can be correlated without exposing sensitive content.

For a reproducibility attempt, use the same app version, ABI, Android API level, operation, and file-class characteristics. Record whether the issue reproduces and attach the relevant test/build evidence.
