# Code Review Checklist

| Area | Review question |
|---|---|
| Correctness | Does the change satisfy the stated requirement and preserve existing contracts? |
| Architecture | Does it respect Core/Data/Domain/Feature/Telegram boundaries? |
| Security | Are secrets, permissions, storage, network, logs, and native surfaces safe? |
| Performance | Is any claimed improvement supported by before/after evidence? |
| Testing | Are focused and affected-area regression tests present and actually run? |
| Error handling | Are loading, empty, error, retry, and failure states truthful? |
| RTL | Are start/end and layout-direction-safe APIs used? |
| Accessibility | Are semantics, labels, focus, contrast, and touch targets appropriate? |
| Logging | Is sensitive data excluded and diagnostic retention bounded? |
| TDLib | Are version, bindings, native artifacts, ABI, and fail-closed behavior preserved? |
| Database | Are schema, migration, upgrade, and data preservation concerns addressed? |
| Regressions | Are queue, upload, authentication, background, navigation, and release risks covered? |

The reviewer must also inspect the complete diff for debug code, temporary files, unexpected dependencies, generated private artifacts, and unrelated changes.
