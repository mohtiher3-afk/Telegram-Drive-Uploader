# Impact Analysis

## Risk Classification

**Risk:** `LOW | MEDIUM | HIGH | CRITICAL`

Classify any feature touching TDLib, JNI, ABI, authentication, Upload Engine, WorkManager, database, or security architecture as at least HIGH unless evidence justifies a higher classification.

## Affected Boundaries

| Boundary | Affected? | Evidence and impact |
|---|---:|---|
| UI | No / Yes | `<screens and states>` |
| Navigation | No / Yes | `<routes and arguments>` |
| ViewModels | No / Yes | `<state and lifecycle impact>` |
| Business logic | No / Yes | `<use cases and invariants>` |
| Repositories | No / Yes | `<interfaces and implementations>` |
| Database | No / Yes | `<schema and migration impact>` |
| DataStore | No / Yes | `<keys and compatibility>` |
| Telegram / TDLib | No / Yes | `<exact APIs and auth impact>` |
| Upload Engine | No / Yes | `<queue, worker, progress, retry>` |
| WorkManager | No / Yes | `<constraints and recovery>` |
| Notifications | No / Yes | `<channels and permissions>` |
| Security | No / Yes | `<secrets, storage, network, intents>` |
| Performance | No / Yes | `<baseline and measurement plan>` |
| Localization | No / Yes | `<English, Arabic, RTL>` |
| Accessibility | No / Yes | `<semantics, touch targets, scaling>` |

## Protected-System Decision

State whether a specialized technical plan is required. If yes, stop implementation until the plan is approved.
