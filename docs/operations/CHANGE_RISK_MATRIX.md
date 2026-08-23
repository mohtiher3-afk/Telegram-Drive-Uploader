# Change Risk Matrix

| Risk class | Typical changes | Required review and verification |
|---|---|---|
| Low | Documentation, comments, formatting | Diff review, secret scan, and relevant documentation check |
| Medium | UI, ViewModel, repository, configuration | Targeted tests, compile, lint, and global verification |
| High | Authentication, TDLib integration, upload engine, database, security, background execution | Dedicated regression evidence, focused review, and full verification |
| Critical | Signing, application identity, data migration, native libraries, production secrets | Owner approval, explicit rollback plan, full verification, release validation, and documented evidence |

Risk classification is additive: a change touching more than one area uses the highest applicable class. High and critical changes are not automatically rejected, but they may not bypass the associated review or verification gates.
