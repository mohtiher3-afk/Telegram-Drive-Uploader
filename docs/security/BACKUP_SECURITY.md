# Backup Security

Both legacy full-backup and Android 12+ data-extraction rules exclude the Room database files and the app-private DataStore directory for cloud backup and device transfer. This protects upload metadata, URIs, and connection-related settings from unintended backup migration.

`android:allowBackup="true"` remains unchanged because changing backup behavior is a compatibility decision. The current exclusions are documented and should be verified on supported Android versions with an actual backup/restore test before any further policy change.

Status: configuration reviewed; runtime backup/restore test **NOT EXECUTED — DEVICE/TOOLING UNAVAILABLE**.
