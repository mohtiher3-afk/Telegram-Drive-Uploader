# Security Policy

## Reporting a Vulnerability

If you discover a security vulnerability in Telegram Drive Uploader, **please do not open a public issue**.

Instead, report it privately by opening a [GitHub Security Advisory](https://github.com/mohtiher3-afk/Telegram-Drive-Uploader/security/advisories/new) or by contacting the maintainer directly through the repository owner profile.

We will acknowledge your report and work on a fix as quickly as possible.

## Scope

This application handles Telegram credentials and session data. The following are especially security-sensitive and in scope:

- TDLib authentication and session storage
- Local database (Room) contents
- Native library loading (`libtdjni.so`, `libssl.so`, `libcrypto.so`)
- Network transfer of files to Telegram
- Diagnostic logging (must redact sensitive identifiers)

## Security Design Notes

- **Local-only credentials**: Telegram API ID/hash are supplied at build time and never committed. Session data is stored only in private application storage.
- **No cloud backup**: `allowBackup="false"` prevents TDLib session files from being included in cloud backups or device-to-device transfers.
- **Secrets discipline**: Keystores, API keys, `.env` values, session data, and built APK/AAB files must never be committed (enforced by `.gitignore` and `scripts/check-secrets.sh`).
- **Native library safety**: Native libraries are loaded from the application's private `nativeLibraryDir`, not from external/writable storage.

## Supported Versions

Only the latest release receives security fixes. Please update to the most recent version before reporting issues that may already be resolved.
