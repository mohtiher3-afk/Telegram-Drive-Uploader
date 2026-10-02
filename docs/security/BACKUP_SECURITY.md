# Backup Security

Both legacy full-backup and Android 12+ data-extraction rules exclude the Room database files and the app-private DataStore directory for cloud backup and device transfer. This protects upload metadata, URIs, and connection-related settings from unintended backup migration.

`android:allowBackup="false"` prevents new full-backup and device-transfer payloads from including app-private data. The Android 12+ extraction rules remain documented as defense-in-depth for OEM/tooling behavior.

Status: configuration reviewed; runtime backup/restore behavior still **NOT EXECUTED — DEVICE/TOOLING UNAVAILABLE**.
