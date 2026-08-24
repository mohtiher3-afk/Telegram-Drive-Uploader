# Lint and GitHub Actions Quality Review

**Date:** 2026-08-24

## Result

The Android project’s latest local lint and GitHub Actions checks are passing. No source-code or dependency-version change was required to preserve correctness. The review found 121 non-blocking lint warnings and CI annotations about older action runtimes; these are documented separately from failures.

## Lint inventory

| Warning ID | Count | Classification | Decision |
|---|---:|---|---|
| `GradleDependency` | 81 | Available newer versions for AGP, Compose, AndroidX, Camera, Room, WorkManager, Hilt, Firebase, OkHttp, and related dependencies | Defer. Dependency upgrades are a separate compatibility phase and were not applied merely to reduce warnings. |
| `TypographyEllipsis` | 10 | Literal three-dot text instead of the ellipsis character | Cosmetic and localized UI cleanup candidate; no behavior or build risk demonstrated in this review. |
| `UnusedResources` | 9 | Resources not referenced by the current source graph | Defer until an asset/resource ownership review; removal can affect dynamic lookup or future UI. |
| `IconDuplicates` | 5 | Launcher and round launcher files have identical contents | Defer; launcher branding is outside this CI-only review. |
| `PluralsCandidate` | 4 | Strings that may be better represented as plural resources | Defer until English/Arabic grammar and all call sites are reviewed together. |
| `AndroidGradlePluginVersion` | 3 | Newer AGP available | Defer with dependency upgrades. |
| `Recycle` | 2 | Reusable resource/layout hints | Review later with UI behavior and accessibility tests. |
| `PrivateResource` | 2 | Private framework/resource references | Review later with API compatibility evidence. |
| Other IDs | 5 | `SelectedPhotoAccess`, `RedundantLabel`, `IconLocation`, `IconLauncherShape`, `ChromeOsAbiSupport` | No automatic change applied; each needs context-specific validation. |

No lint errors were reported. The warnings do not invalidate the successful debug/release build, but dependency warnings should not be treated as a safe upgrade list because TDLib, Compose, Hilt, Room, WorkManager, and release tooling compatibility must be tested as a group.

## GitHub Actions review

The repository uses three workflows: Android CI, the API 36 x86_64 TDLib device smoke test, and the signed multi-ABI release workflow. All latest main-branch jobs reviewed during this phase completed successfully, including the three ABI build jobs and the security gate.

The API 36 smoke workflow completed successfully with `JNI_LOAD_STATUS=PASS` and `CLIENT_CREATE_STATUS=PASS`. Its remaining annotation states that several actions target Node.js 20 and are being forced to run on Node.js 24, and specifically recommends migrating `actions/setup-java` from v4 to v5. The official repositories currently expose newer release lines, but changing every action major is not a safe lint-only edit. A dedicated CI action-refresh phase should update one action family at a time and rerun every workflow.

| Current action family | Current repository usage | Review decision |
|---|---|---|
| `actions/checkout` | `@v4` | Keep for now; no failing behavior observed. |
| `actions/setup-java` | `@v4` | Explicit future maintenance candidate: official latest stable line is v5. Do not mix into this warning-only review. |
| `android-actions/setup-android` | `@v3` | Keep for now; the smoke and build jobs pass. |
| `gradle/actions/setup-gradle` | `@v4` | Keep for now; the smoke and build jobs pass. |
| `actions/upload-artifact` | `@v4` | Keep for now; artifacts uploaded successfully. |
| `actions/download-artifact` | `@v4` | Keep for now; release workflow was not broken by this version. |
| `softprops/action-gh-release` | `@v2` | Keep for now; signing/release behavior is outside this review. |

## Verification evidence

The following repository gates passed during the review:

- `./scripts/check-workmanager-manifest.sh` — `STATUS: WORKMANAGER_MANIFEST=PASS`.
- `./scripts/check-resource-integrity.sh` — `STATUS: RESOURCE_INTEGRITY=PASS`.
- `./scripts/check-repository-security.sh` — `STATUS: SECURITY_SCAN=PASS`.
- `git diff --check` — PASS.
- TDLib artifact checker — `STATUS: TDLIB_ARTIFACTS_PRESENT=true` for `arm64-v8a`, `armeabi-v7a`, and `x86_64`.
- GitHub Actions Multi-ABI CI run `32696668335` — success for all three ABI build jobs and the security gate.
- Latest local source tree check — no protected TDLib/upload/WorkManager path changes from the review itself.

## References

[1]: https://github.com/actions/checkout "actions/checkout official repository"
[2]: https://github.com/actions/setup-java "actions/setup-java official repository"
[3]: https://github.com/actions/upload-artifact "actions/upload-artifact official repository"
[4]: https://github.com/ReactiveCircus/android-emulator-runner "ReactiveCircus Android Emulator Runner official repository"
