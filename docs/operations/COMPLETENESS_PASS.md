# Android Application Completeness Pass

## Scope

This pass addresses only confirmed repository gaps that are safe to correct without changing TDLib, authentication, upload transport, persistence schema, WorkManager policy, or release signing.

## Findings

| Finding | Evidence | Decision |
|---|---|---|
| Upload progress unit mismatch | Engine and Room use percentage `0..100`; `UploadStatusIndicator` passed the raw value as a `0..1` bar fraction and multiplied it again for text. | Fix the UI conversion at the rendering boundary and add pure unit tests. |
| Queue UI contains hard-coded English text | Empty states, filter labels, summary, and queue controls are literals in `QueueScreen`. Arabic resources already exist for the same product area. | Externalize these strings in English and Arabic without changing behavior. |
| Onboarding | Existing first-run gate, DataStore completion flag, skip path, version-aware media permission request, and M3 motion/layout are present. | No redesign or duplicate navigation graph added. |
| Build verification | Project already enables configuration cache, local build cache, parallelism, KSP, shrinking, and ABI checks. Local FULL verification remains blocked by missing Android SDK location. | Do not alter build flags without a measured bottleneck and a working SDK. |
| Notifications | No upload notification feature exists. | Do not add notification permissions or feature scope. |

## Hypothesis

Correcting the percentage conversion at the UI boundary will make progress text and the linear indicator represent the persisted upload percentage consistently. Externalizing queue strings will complete the existing Arabic/English localization path.

## Validation

Run focused unit tests, compile, lint, debug packaging, TDLib artifact checks, secret scans, diff checks, and the master self-check. The Android SDK environment limitation must remain explicit if it recurs.

## Reversal

Revert the single UI progress conversion/test change and the queue string/resource changes. No schema, native artifact, or dependency rollback is required.
