# CI and Signed Release Readiness Audit — August 2026

## CI configuration review

| Control | Current workflow behavior | Audit result |
|---|---|---|
| Trigger scope | `android-ci.yml` runs on `main`, pull requests to `main`, and manual dispatch. | Repository verified. |
| ABI scope | CI executes arm64-v8a, armeabi-v7a, and x86_64 independently. | Repository verified. |
| Build gates | Each ABI job validates TDLib, WorkManager manifest, resources, unit tests, release lint, and selected-ABI Debug packaging. | Repository verified. |
| Sensitive files | CI writes `.env` and Debug keystore transiently, then removes `.env`, `debug.keystore`, and `local.properties`. | Repository verified. |
| Repository security | A dedicated read-only job runs the tracked-source secret scan. | Repository verified. |
| Latest published CI | The latest `main` run for `02b13bc` succeeded: [run 33026804063][1]. | Remote verified. |
| Current local changes | The maintenance changes in this pass are uncommitted and therefore have not triggered remote CI. | Repository verified. |

## Local release gate

`./gradlew :app:lintVitalRelease` completed successfully for the current source. The only observed manifest warning states that the removal marker for the default WorkManager initializer had no competing declaration in the merged manifest. The marker is intentional because the app supplies WorkManager configuration through its application class, and the repository WorkManager gate passed.

## Signed-release workflow boundary

The tag-only release workflow validates tag/version correspondence, rejects an existing GitHub Release for the same tag, prepares Telegram configuration and the release keystore from GitHub Secrets, runs static/build gates for all three ABIs, verifies APK signatures and checksums, and only then publishes the verified assets.

| Required external configuration | Readiness state |
|---|---|
| `TELEGRAM_API_ID` and `TELEGRAM_API_HASH` | Required for a real release build; values were not accessed or changed in this maintenance pass. |
| `RELEASE_KEYSTORE_BASE64`, `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD` | Required for signed releases; values were not accessed or changed. |
| GitHub Actions secret permission | Not treated as a readiness claim because secret names cannot be safely confirmed from the current token context. |
| Version/tag release action | Not performed. No tag, release draft, asset upload, or GitHub publication was created. |

## Decision

CI configuration and local release lint are ready for the current source, subject to remote CI after a future commit/push. A signed release remains deliberately blocked by the user-controlled secrets, valid Telegram configuration, and an explicit user authorization to create a tag/release.

## References

[1]: https://github.com/mohtiher3-afk/Telegram-Drive-Uploader/actions/runs/33026804063 "Latest successful multi-ABI CI run"
[2]: https://docs.github.com/actions/security-guides/using-secrets-in-github-actions "GitHub Actions secrets"
[3]: https://developer.android.com/build "Android build system documentation"
