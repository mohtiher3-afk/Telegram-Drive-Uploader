# Logging Inventory

This inventory records the current implementation. It is an audit, not an authorization to add analytics, remote telemetry, or a crash-reporting provider.

| Location | Logger | Information | Sensitivity | Release behavior |
|---|---|---|---|---|
| `core/diagnostics/DiagnosticsManager.kt` | `android.util.Log` | Sanitized category, severity, incident/event identifiers, bounded message context | Medium before sanitization; sanitized before storage/logcat | ERROR uses `Log.e`; non-error events use `Log.i` |
| `core/diagnostics/DiagnosticsManager.kt` | In-memory `StateFlow` | Up to 200 events retained for up to 24 hours | Sanitized fields only; not durable | Cleared on retention, explicit clear, and critical memory pressure |
| `feature/upload/UploadViewModel.kt` | `DiagnosticsManager` | Metadata preparation, queue insertion, database errors | Upload IDs and errors pass through sanitization | Release-safe event path |
| `feature/upload/worker/UploadWorker.kt` | `DiagnosticsManager` | Worker lifecycle, upload preparation, terminal status, retry, sanitized errors | No file contents or raw credentials | Release-safe event path; progress is intentionally bounded |
| `data/upload/UploadManagerImpl.kt` | `DiagnosticsManager` | WorkManager enqueue and state snapshot | Safe operation identifiers and state | Release-safe event path |
| `data/telegram/client/TelegramClientImpl.kt` | `DiagnosticsManager` | TDLib initialization failures, auth errors, destination-resolution outcomes | No raw TdApi payloads or credentials | Release-safe event path |
| `TelegramDriveApp.kt` | `DiagnosticsManager` | Startup and memory-pressure callbacks | High-level state only | In-memory diagnostics may be cleared under pressure |
| `feature/settings/SettingsScreen.kt` | `DiagnosticsManager.exportDiagnostics()` | User-initiated sanitized export and local display | Export remains user-controlled; sensitive fields are prohibited | No automatic external upload |

No Timber, remote analytics SDK, automatic crash-reporting provider, or continuous telemetry pipeline was identified in the reviewed source. `println` and indiscriminate raw `TdApi` logging were not authorized by this audit.
