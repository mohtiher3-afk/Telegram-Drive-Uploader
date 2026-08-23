# Database Policy

Room is used for local upload/task persistence, while DataStore holds simple preferences such as settings and pinned destination IDs. Database and DataStore paths are app-private and excluded from the configured Android cloud and device-transfer backup rules.

Any database change requires a schema update, explicit migration, migration tests, upgrade tests, and data-preservation verification. Never delete user data or use destructive migration as a shortcut for a compatibility problem. Analyze the installed version, upgrade path, downgrade risk, and rollback behavior before release.

Backup support is limited by the current Android backup configuration. A release must document whether each user-data category is expected to survive backup/restore, and runtime backup/restore evidence must be labeled `NOT VERIFIED` when no device test has been performed.
