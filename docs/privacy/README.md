# Privacy and Data Governance

This directory records the application’s current data model and privacy-sensitive boundaries. It is an engineering record, not a legal privacy notice.

| Document | Purpose |
|---|---|
| `DATA_INVENTORY.md` | Field-level inventory of data, storage, transmission, retention, and sensitivity. |
| `DATA_FLOW_MAP.md` | Actual local, Telegram/TDLib, persistence, and diagnostic flows. |
| `DATA_RETENTION_POLICY.md` | Retention and deletion status, including explicitly undefined durations. |
| `PRIVACY_GOVERNANCE.md` | Classification, access, permissions, notifications, backup, and future-change controls. |
| `FINAL_PRIVACY_REPORT.md` | Summary of the completed review and known limitations. |

Any future change affecting user data, file content or metadata, Telegram state, queue/history, scheduler, settings, diagnostics, notifications, storage, network transmission, permissions, backups, or retention must update this review before implementation.
