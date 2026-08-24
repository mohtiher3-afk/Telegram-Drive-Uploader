# Lint and GitHub Actions Quality Review

**Date:** 2026-08-24

## Result

The Android project’s latest local lint and GitHub Actions checks are passing. A low-risk source correction removed the paired `TypographyEllipsis` findings, and workflow action majors were refreshed without changing build commands, secrets, signing, ABI scope, or application behavior. The post-fix local lint report contains 111 non-blocking warnings.

## Lint inventory

| Warning ID | Count | Classification | Decision |
|---|---:|---|---|
| `GradleDependency` | 81 | Available newer versions for AGP, Compose, AndroidX, Camera, Room, WorkManager, Hilt, Firebase, OkHttp, and related dependencies | Defer. Dependency upgrades are a separate compatibility phase and were not applied merely to reduce warnings. |
| `TypographyEllipsis` | 10 → 0 | Literal three-dot text instead of the ellipsis character | Fixed in English and Arabic resources using the Unicode ellipsis character. |
| `UnusedResources` | 9 | Resources not referenced by the current source graph | Defer until an asset/resource ownership review; removal can affect dynamic lookup or future UI. |
| `IconDuplicates` | 5 | Launcher and round launcher files have identical contents | Defer; launcher branding is outside this CI-only review. |
| `PluralsCandidate` | 4 | Strings that may be better represented as plural resources | Defer until English/Arabic grammar and all call sites are reviewed together. |
| `AndroidGradlePluginVersion` | 3 | Newer AGP available | Defer with dependency upgrades. |
| `Recycle` | 2 | Reusable resource/layout hints | Review later with UI behavior and accessibility tests. |
| `PrivateResource` | 2 | Private framework/resource references | Review later with API compatibility evidence. |
| Other IDs | 5 | `SelectedPhotoAccess`, `RedundantLabel`, `IconLocation`, `IconLauncherShape`, `ChromeOsAbiSupport` | No automatic change applied; each needs context-specific validation. |

No lint errors were reported. The remaining 111 warnings do not invalidate the successful debug/release build. The 10 `TypographyEllipsis` findings were removed, leaving the dependency, resource, icon, plural, and API-compatibility categories documented as compatibility-sensitive or context-sensitive work. Dependency warnings remain an informational upgrade list; dependency upgrades should be handled in a separate compatibility phase because TDLib, Compose, Hilt, Room, WorkManager, and release tooling must be tested as a group.

## GitHub Actions review

The repository uses three workflows: Android CI, the API 36 x86_64 TDLib device smoke test, and the signed multi-ABI release workflow. All latest main-branch jobs reviewed during this phase completed successfully, including the three ABI build jobs and the security gate.

The API 36 smoke workflow completed successfully with `JNI_LOAD_STATUS=PASS` and `CLIENT_CREATE_STATUS=PASS` after the action refresh. The refreshed action majors were confirmed by the successful post-fix runs on GitHub-hosted runners. Local YAML parsing and `git diff --check` also passed.

| Current action family | Current repository usage | Review decision |
|---|---|---|
| `actions/checkout` | `@v7` | Updated; official current major verified. |
| `actions/setup-java` | `@v5` | Updated from deprecated v4; official current major verified. |
| `android-actions/setup-android` | `@v4` | Updated; official current major verified. |
| `gradle/actions/setup-gradle` | `@v6` | Updated; official current major verified. |
| `actions/upload-artifact` | `@v7` | Updated; official current major verified. |
| `actions/download-artifact` | `@v8` | Updated; official current major verified. |
| `softprops/action-gh-release` | `@v3` | Updated; official current major verified. |

## Verification evidence

The following repository gates passed during the review:

- `./scripts/check-workmanager-manifest.sh` — `STATUS: WORKMANAGER_MANIFEST=PASS`.
- `./scripts/check-resource-integrity.sh` — `STATUS: RESOURCE_INTEGRITY=PASS`.
- `./scripts/check-repository-security.sh` — `STATUS: SECURITY_SCAN=PASS`.
- `git diff --check` — PASS.
- TDLib artifact checker — `STATUS: TDLIB_ARTIFACTS_PRESENT=true` for `arm64-v8a`, `armeabi-v7a`, and `x86_64`.
- GitHub Actions Multi-ABI CI run `32699608172` — success for all three ABI build jobs and the security gate after the action refresh.
- GitHub Actions API 36 device smoke run `32699608147` — success after the action refresh, including emulator execution and evidence upload.
- Latest local source tree check — no protected TDLib/upload/WorkManager path changes from the review itself.

The post-refresh workflows still expose two non-blocking build annotations: an OpenSSL `__ANDROID_API__` macro redefinition warning and a KSP service lookup annotation while the job remains successful. These originate in native/dependency tooling rather than the app’s Kotlin behavior; they were not silenced with broad compiler flags or source changes.

## References

[1]: https://github.com/actions/checkout "actions/checkout official repository"
[2]: https://github.com/actions/setup-java "actions/setup-java official repository"
[3]: https://github.com/actions/upload-artifact "actions/upload-artifact official repository"
[4]: https://github.com/ReactiveCircus/android-emulator-runner "ReactiveCircus Android Emulator Runner official repository"
