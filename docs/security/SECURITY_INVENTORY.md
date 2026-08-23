# Security Inventory

| Area | Asset/Data | Location | Risk | Status |
|---|---|---|---|---|
| Authentication | Telegram phone/code/password state | TDLib client boundary and in-memory UI state | High | Boundary reviewed; secrets not logged |
| API configuration | API ID/hash supplied through build configuration | `BuildConfig` / CI environment | High | No literal values committed in source |
| Session data | TDLib-managed application data | App-private runtime storage | High | Excluded from configured backup domains where applicable |
| Local database | Upload metadata, filenames, URIs, status | Room app-private database | Medium | Excluded from backup rules |
| DataStore | Settings and connection state | App-private `datastore/` | Medium/High | Excluded from backup rules |
| Files | Selected content and temporary copies | App-private/cache paths | Medium | FileProvider not present; streaming reader closes resources |
| Logs | Sanitized diagnostics | `DiagnosticsManager` | Medium | Review required for future additions; no secret values intentionally emitted |
| Network | Telegram/TDLib and configured HTTPS services | Android network stack | High | No trust-all TLS code found; no custom cleartext policy found |
| Components | Main activity and AndroidX startup provider | `AndroidManifest.xml` | Medium | Provider non-exported; launcher activity exported only for launcher intent |
| Native libraries | Official TDLib JNI artifacts | `app/src/main/jniLibs/` | High | Protected and unchanged |
| Backups | Database/DataStore exclusion rules | `res/xml/*backup*` | High | Explicit exclusions present |
