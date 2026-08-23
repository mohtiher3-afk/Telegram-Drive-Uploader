# Privacy Data Inventory

| Data type | Purpose | Storage | Transmission | Retention | User control |
|---|---|---|---|---|---|
| Telegram API configuration | Initialize official TDLib | Build/runtime configuration | Used for Telegram connection | Until configuration/build lifecycle | Owner controls CI/configuration |
| Telegram authentication/session state | Authenticate and restore TDLib session | App-private TDLib storage | Telegram transport | Until logout/clearance | User can log out; backup excluded |
| Phone/code/password input | Complete authentication | Transient UI/TDLib flow | Telegram authentication transport | Transient; not intended for diagnostics | User enters and can abandon flow |
| Selected file metadata | Queue and upload | App-private Room database | Telegram upload path when sent | Until history cleanup/app data removal | User controls selected files and queue actions |
| File bytes | Real upload | Content URI/streaming path; not intentionally stored as DB blob | Telegram/TDLib upload transport | Controlled by source provider and upload lifecycle | User selects/cancels upload |
| Diagnostics | Troubleshooting | App-private diagnostics export/log path | Not intentionally sent by this app | App-defined local lifecycle | User controls export/sharing |
| Notifications | Upload state feedback | Android notification system | Device-local | Notification policy/lifecycle | User controls notification permission/settings |

This inventory is descriptive, not a privacy-policy legal statement. Actual disclosure, retention, and provider terms must be reviewed by the product owner before distribution.
