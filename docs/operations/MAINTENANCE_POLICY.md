# Maintenance Policy

Normal maintenance may include documentation updates, localized UI corrections, test improvements, and narrowly scoped bug fixes that preserve existing contracts. Every change must be traceable to an issue or maintenance reason.

Additional review is required for security, dependency, TDLib, database, upload-engine, architecture, release, and performance changes. Database and TDLib changes require dedicated migration or artifact procedures. Release changes require build, test, lint, security, TDLib, artifact, signing, checksum, and rollback evidence.

Prohibited actions include disabling tests or artifact gates, fabricating success, committing secrets or signing files, replacing native libraries with placeholders, changing application identity without a migration plan, deleting user data to solve a migration problem, and bundling unrelated refactors into a fix.
