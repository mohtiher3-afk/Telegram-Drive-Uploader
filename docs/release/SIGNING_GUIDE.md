# Signing Guide

Release signing is configured for CI and is intentionally not represented by committed files or values. Required CI secrets are `RELEASE_KEYSTORE_BASE64`, `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`, and `RELEASE_KEY_PASSWORD`. The workflow materializes a temporary keystore with restrictive permissions, validates the alias, signs the release build, and removes the keystore and local configuration in an `always()` cleanup step.

For local signing, provide the same values through a secure environment or an untracked local configuration, set `RELEASE_KEYSTORE_PATH`, and run the project’s release Gradle task after TDLib and Android toolchains are installed. Never place passwords, base64 keystore content, private keys, or real Telegram credentials in source, documentation, workflow text, or commit history.

Production key custody, access, backup, and rotation must be managed outside the repository by the release owner and organization. Loss or compromise requires key-rotation and distribution-impact review; do not generate a replacement key casually.

Current status: **RELEASE SIGNING NOT VERIFIED** in this environment because no release build was executed and no signing secret was accessed.
