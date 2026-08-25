# Material 3 Navigation Semantics Maintenance Record

## Scope and success condition

This controlled maintenance phase is limited to Material 3 navigation accessibility semantics. Success means that every top-level destination remains a single, clearly labelled navigation target while its adjacent visual icon is not announced redundantly. No TDLib, authentication, destination, persistence, WorkManager, upload, native, signing, dependency, or release behavior is in scope.

## Protected behavior

| Area | Protected behavior | Evidence boundary |
|---|---|---|
| Navigation routes | Home, queue, history, and settings routes and labels remain stable. | Repository verified by `NavigationLabelsTest`. |
| TDLib and uploads | Authorization, upload progress, WorkManager, Room, destination identity, and native artifacts remain unchanged. | Not exercised by this UI-only phase. |
| Localization and RTL | Existing string-resource labels and layout-direction behavior remain authoritative. | Repository verified; runtime RTL check not performed. |
| Theme and adaptive structure | Existing Compose BOM, centralized tokens, navigation bar/rail breakpoint, light/dark theme, and motion behavior are unchanged. | Repository verified. |

## Finding and change record

| Field | Record |
|---|---|
| Finding | `AppNavigation.kt` supplies each navigation icon with the same localized string that is also supplied by the `NavigationBarItem`/`NavigationRailItem` label. The icon is visual support for an already-labelled parent target, so the duplicated child description can add redundant semantics. |
| Hypothesis | Setting the nested icons’ `contentDescription` to `null` retains the visible labels and the parent Material navigation semantics while removing redundant icon descriptions. Android’s Compose accessibility guidance uses `contentDescription = null` for decorative child icons within an already-labelled interactive component. |
| Scope | `AppNavigation.kt`, this maintenance record, and a focused source-level regression test if practical. Debug Kotlin compilation and existing unit tests are validation targets. |
| Risk | Low UI-semantics risk. The change does not alter navigation callbacks, routes, ViewModels, persistence, upload state, TDLib, or native code. Device TalkBack output remains runtime-unverified. |
| Validation | Inspect focused diff; run the navigation-label unit test and `:app:compileDebugKotlin`; run `:app:testDebugUnitTest` if the Android SDK configuration allows it. |
| Reversal | Revert the single commit or restore the two icon descriptions in `AppNavigation.kt`. |

## Checklist

- [x] Audit the current navigation shell, tokens, routes, and existing tests.
- [x] Confirm the change is confined to Material 3 UI semantics.
- [x] Record the protected TDLib/upload behavior and evidence boundary.
- [x] Apply the minimal navigation semantics change.
- [x] Confirm existing focused regression coverage for stable routes and localized destination labels.
- [x] Run compilation and focused unit-test validation.
- [x] Review the diff for protected-path changes and secret exposure.
- [x] Update this record with outcomes and limitations.
- [ ] Commit and push only if explicitly requested by the user.

## References

- [Android Compose: Merging and clearing semantics](https://developer.android.com/develop/ui/compose/accessibility/merging-clearing)
- [Android Compose: Semantics](https://developer.android.com/develop/ui/compose/accessibility/semantics)
- [Material 3: Navigation bar](https://m3.material.io/components/navigation-bar/overview)
- [Material 3: Navigation rail](https://m3.material.io/components/navigation-rail/overview)

## Outcomes and limitations

The initial test invocation was blocked because the sandbox lacked an Android SDK. A user-local Android SDK was installed at `/home/ubuntu/android-sdk`; the first retry then exposed a missing `jlink` executable in the preconfigured Java runtime. After installing JDK 17 and running Gradle with `JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64`, the focused navigation-label test completed successfully. These environment preparations are not repository changes.

The build emitted pre-existing warnings, including a missing `google-services.json`, deprecation warnings, and a future Kotlin annotation-target warning. They did not prevent compilation or the focused test, and were not modified because they are outside this narrow UI-semantics phase.

## Verification status

| Layer | Status | Notes |
|---|---|---|
| Repository audit | PASS | Current code, design records, navigation routes, and unit-test inventory were inspected. |
| Build and focused unit test | PASS | With Android SDK API 36/build-tools 36.0.0 and JDK 17 supplied locally, `:app:testDebugUnitTest --tests com.telegramdrive.uploader.core.navigation.NavigationLabelsTest --no-configuration-cache` completed successfully. This task includes `:app:compileDebugKotlin`. |
| Static diff and protected paths | PASS | `git diff --check` passed. The only code change is `AppNavigation.kt`; no TDLib, upload, WorkManager, native, Gradle, CI, signing, or secret-bearing path changed. |
| Device accessibility | NOT VERIFIED | No connected Android device/emulator or TalkBack session has been used to observe the resulting announcement. |
| Real Telegram authentication/upload | NOT VERIFIED / OUT OF SCOPE | This UI-only phase intentionally does not exercise real credentials or uploads. |
