# Telegram Drive Uploader — Build Stability

## Validated baseline

The repository is built in GitHub Actions with JDK 17, Android API 36, Android build tools 36.0.0, NDK 26.3.11579264, and Gradle 8.9. The CI matrix builds one ABI per job: `arm64-v8a`, `armeabi-v7a`, and `x86_64`.

The project uses the official TDLib v1.8.66 Java bindings and matching native integration. The OpenSSL Android shared-library dependencies are prepared from the documented official source pipeline, then `scripts/check-tdlib-artifacts.sh` validates architecture, bindings, native files, dependencies, and checksums before Gradle packaging.

## CI gates

| Gate | Workflow step | Purpose |
|---|---|---|
| Dependency preparation | `Prepare official OpenSSL runtime dependencies` | Build Android-compatible shared dependencies per ABI |
| TDLib integrity | `Verify official TDLib artifacts` | Reject missing, malformed, mismatched, or wrong-architecture artifacts |
| JVM tests | `:app:testDebugUnitTest` | Validate pure Kotlin/domain behavior |
| Release lint | `:app:lintVitalRelease` | Catch release configuration and resource issues |
| APK assembly | `:app:assembleDebug` or signed release assembly | Produce the selected ABI package |
| Signature verification | Release workflow | Confirm the signed APK is structurally valid and signed with the release key |
| Runtime smoke | Android TDLib device-smoke workflow | Validate JNI loading and real `Client.create()` where the emulator job runs |

## Current release evidence

The signed v1.0.14 release was published from the WorkManager startup repair. Its three ABI jobs completed successfully, including unit tests, release lint, TDLib artifact verification, signed APK assembly, APK signature verification, and release publication.

The release page is [Telegram Drive Uploader v1.0.14](https://github.com/mohtiher3-afk/Telegram-Drive-Uploader/releases/tag/v1.0.14). The release contains ABI-specific APKs and SHA-256 checksum files.

## Local validation

The repository currently does not commit a Gradle wrapper. GitHub Actions provisions Gradle 8.9 through `gradle/actions/setup-gradle@v4`. On a local machine with a compatible Gradle installation, the intended commands are:

```text
gradle --no-daemon --max-workers=2 :app:testDebugUnitTest
gradle --no-daemon --max-workers=2 :app:lintVitalRelease
gradle --no-daemon --max-workers=1 -PtargetAbi=arm64-v8a :app:assembleRelease
./scripts/check-tdlib-artifacts.sh
```

The artifact checker’s default mode is strict all-ABI validation. CI passes `TDLIB_CHECK_ABI` for the matrix ABI so a job validates the files it has generated. The Android smoke workflow uses the x86_64 package and records the native-load and `Client.create()` outcomes.

## Interpretation rules

A successful build proves source compilation, configured checks, and package creation only. It does not prove that a real Telegram account can authenticate, that a user can discover a particular private channel, that the account can post there, or that a video reaches Telegram. Those claims require physical-device testing with a real account and must be supported by sanitized diagnostics, including the genuine `UpdateMessageSendSucceeded` event for final delivery.

## Maintenance notes

The current CI run reports upstream maintenance warnings for actions that target Node.js 20 and for `setup-java@v4`. They do not currently fail the build. Migrating those actions should be isolated from functional changes so a toolchain update can be diagnosed independently.

## References

1. [Official TDLib repository](https://github.com/tdlib/td)
2. [Android WorkManager custom configuration](https://developer.android.com/develop/background-work/background-tasks/persistent/configuration/custom-configuration)
3. [GitHub Actions Android workflow](https://github.com/mohtiher3-afk/Telegram-Drive-Uploader/blob/main/.github/workflows/gradle.yml)
4. [Telegram Drive Uploader v1.0.14 release](https://github.com/mohtiher3-afk/Telegram-Drive-Uploader/releases/tag/v1.0.14)
