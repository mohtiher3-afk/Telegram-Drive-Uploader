# Regression Policy

Every fix must identify the affected areas before selecting tests. A change to authentication includes Telegram initialization, session state, navigation, and error handling. A change to the upload engine includes queue, notifications, background execution, history, progress, retry, pause, resume, and cancel. A persistence change includes schema/version, migration, data preservation, and backup behavior. A UI change includes loading, empty, error, success, RTL, localization, accessibility, and configuration changes.

Run focused unit and regression tests for every affected area. Run the full release matrix when the change affects TDLib, native artifacts, application configuration, dependencies, database schema, upload correctness, or release signing. Do not skip critical coverage, but do not spend time on unrelated expensive tests when the change cannot affect them.

Every test result must be labeled `PASS`, `FAIL`, `NOT VERIFIED`, or `NOT APPLICABLE`.
