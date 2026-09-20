# KSP ApplicationManager CI Annotation Fix — August 2026

## Diagnosis

The completed arm64-v8a CI job for commit `b8156ba` reported a non-blocking exception during `:app:kspDebugUnitTestKotlin`:

> `Exception in thread "AWT-EventQueue-0" ... ApplicationManager.getApplication() is null`

The project used KSP `2.3.5` with Kotlin `2.2.10`. The KSP project recorded the same exception as issue [#2763][1], marked it closed, and linked merged pull request [#2785][2]. The merged change overrides the affected IntelliJ-core sources and is explicitly described as fixing #2763.

## Minimal source change

Only `gradle/libs.versions.toml` changes: `googleDevtoolsKsp` is updated from `2.3.5` to `2.3.6`, the first release following the linked fix. Kotlin, AGP, Hilt, Room, Moshi, KSP processor declarations, ABI splits, signing, CI gates, and release configuration are unchanged.

## Local validation

| Command | Result | ApplicationManager exception |
|---|---|---|
| `:app:kspDebugUnitTestKotlin` | PASS | Not emitted |
| `:app:lintVitalRelease` | PASS | Not emitted |

The observed local warnings are unrelated to this KSP defect: missing optional `google-services.json` in the local environment, the intentional WorkManager initializer-removal marker, Kotlin annotation-target migration warnings, Android/Room API deprecations, and Moshi Kapt deprecation. None is masked or disabled by this change.

## Residual evidence boundary

This validates the source update and the two local KSP-related paths. A new remote Android Multi-ABI CI run after a commit/push is required to confirm the GitHub-hosted runner no longer emits the annotation for all ABIs. No commit, push, tag, release, signing change, or CI configuration change has been made in this investigation.

## References

[1]: https://github.com/google/ksp/issues/2763 "KSP issue #2763"
[2]: https://github.com/google/ksp/pull/2785 "KSP pull request #2785"
