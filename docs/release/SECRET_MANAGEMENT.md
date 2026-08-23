# Secret Management

Source code must always be `NOT STORED` for every secret listed below. This document records names and handling only; it intentionally contains no values.

| Secret | Purpose | Storage | Consumer | Source Code |
|---|---|---|---|---|
| `TELEGRAM_API_ID` | Telegram application identifier for TDLib configuration | GitHub Actions repository secret / local untracked environment | Release and test preparation workflow | NOT STORED |
| `TELEGRAM_API_HASH` | Telegram application hash for TDLib configuration | GitHub Actions repository secret / local untracked environment | Release and test preparation workflow | NOT STORED |
| `TELEGRAM_BOT_TOKEN` | Optional gateway/bot integration credential | GitHub Actions or deployment secret store | Gateway or operational integration only | NOT STORED |
| `TELEGRAM_CHANNEL_ID` | Optional gateway destination identifier | GitHub Actions or deployment secret store | Gateway or operational integration only | NOT STORED |
| `RELEASE_KEYSTORE_BASE64` | Encoded production signing keystore | GitHub Actions repository secret | Manual Android Release workflow | NOT STORED |
| `RELEASE_STORE_PASSWORD` | Keystore password | GitHub Actions repository secret | Manual Android Release workflow | NOT STORED |
| `RELEASE_KEY_ALIAS` | Signing key alias | GitHub Actions repository secret | Manual Android Release workflow | NOT STORED |
| `RELEASE_KEY_PASSWORD` | Signing key password | GitHub Actions repository secret | Manual Android Release workflow | NOT STORED |
| `UPLOAD_API_KEY` | Gateway upload authentication credential | Deployment secret store | Gateway server | NOT STORED |

## Handling Rules

Secrets must not be committed, printed in CI logs, embedded in APK resources, or included in diagnostic exports. The Release workflow creates the keystore temporarily, uses it for signing, and removes the file during cleanup. Secret names may be documented; values may not.

The production signing keystore must have an encrypted offline backup controlled by the release owner. If it is lost or exposed, stop release publication and follow the security incident procedure. Repository secret access should be limited to maintainers who need to run the manual release workflow.
