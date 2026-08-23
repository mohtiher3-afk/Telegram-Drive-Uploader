# Pre-Push Checklist

Before pushing a meaningful change, inspect the affected files and classify the change using [CHANGE_RISK_MATRIX.md](CHANGE_RISK_MATRIX.md). Run the required targeted tests, then run the appropriate verification mode. Inspect the complete staged and unstaged diff, run the redacted secret check, and confirm that no generated APK, keystore, private key, credential, session data, or local user data is included.

Commit only the focused change with a descriptive message. Do not install Git hooks automatically. For high- or critical-risk changes, obtain the required review and preserve the exact validation output and rollback reference in the pull request or release record.
