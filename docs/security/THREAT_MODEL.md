# Threat Model

| Threat | Impact | Likelihood | Mitigation | Residual risk |
|---|---|---|---|---|
| Malicious Android app invokes component | Unauthorized entry or data exposure | Low | Minimal manifest; provider/startup provider non-exported; only launcher activity exported | Launcher activity accepts normal launcher invocation |
| Physical/rooted device access | Session/files/log exposure | Medium | App-private storage, backup exclusions, sanitized diagnostics | Rooted devices can bypass app isolation |
| Malicious intent/file | Crash, inaccessible upload, or unsafe content | Medium | Content URI flow and controlled streaming | Runtime fuzzing still required |
| Compromised network | Credential/data interception | Medium | Platform TLS/TDLib transport; no trust-all code found | Endpoint/device compromise remains possible |
| Leaked API configuration | API misuse or reverse engineering | Medium | CI/environment injection; do not treat APK configuration as secret | Public client configuration can be extracted from APK |
| Debug APK exposure | Debuggable/test configuration exposure | Medium | Release configuration is separate; release validation remains CI-gated | Debug artifacts must not be distributed as release |
| Backup extraction | Session or metadata migration | Medium | Database and DataStore excluded from cloud/device-transfer rules | Backup runtime test not executed |
| Malicious file | Resource exhaustion or failed upload | Medium | Streaming and metadata validation | Large/malformed-file stress tests remain pending |
