# Local Android Build Verification

**Repository:** [Telegram-Drive-Uploader](https://github.com/mohtiher3-afk/Telegram-Drive-Uploader)

**Date:** 2026-08-24

**Scope:** Configure a local Android SDK and verify the existing application without changing TDLib, JNI, ABI, upload behavior, database behavior, WorkManager behavior, or navigation routes.

## Environment

The sandbox initially had Java 17 installed but was using Java 21 as the default. The Java 21 installation did not include `jlink`, which caused Android Gradle’s `androidJdkImage` transformation to fail. The project was therefore verified with `/usr/lib/jvm/java-17-openjdk-amd64`, which includes `jlink` and matches the project’s Android build requirement.

Google’s official Android command-line tools package `commandlinetools-linux-15859902_latest.zip` was downloaded, verified against the published SHA-256 checksum, and installed under `/home/ubuntu/android-sdk`. The following packages were installed:

| Package | Version |
|---|---:|
| Android SDK Platform 36 | 2 |
| Android SDK Build-Tools | 36.0.0 |
| Android SDK Platform-Tools | 37.0.1 |

The reusable environment file is `/home/ubuntu/android-sdk-env.sh`. It exports `ANDROID_HOME`, `ANDROID_SDK_ROOT`, and the required SDK tool paths. It is intentionally outside the repository and contains no credentials.

## Compile blockers found and repaired

The first SDK-enabled Gradle run exposed actual source errors that had previously been hidden by the missing SDK:

| File/problem | Safe correction |
|---|---|
| `DesignTokens.kt` and `Dimensions.kt` both declared `AppSpacing` | Merged both existing token name sets into the centralized `DesignTokens.kt` object and removed the duplicate declaration. Numeric values and existing call-site behavior were preserved. |
| `HomeScreen.kt` referenced `AppSpacing.sm` and `AppSpacing.lg` | Restored those existing short aliases in the centralized token object with their original values: 8dp and 16dp. |
| `OnboardingScreen.kt` used `rememberSaveable` without its import | Added the missing import only. |

No business or platform integration logic was changed by these repairs.

## Gradle verification

The following command completed successfully with JDK 17 and the configured SDK:

```bash
source /home/ubuntu/android-sdk-env.sh
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew test lint assembleRelease --no-daemon --no-configuration-cache --stacktrace
```

| Check | Result |
|---|---|
| Kotlin/Java/Compose compilation | PASS |
| Debug JVM unit tests | PASS |
| Release JVM unit tests | PASS |
| Android lint | PASS with 0 errors and 121 warnings |
| Release resource shrinking and packaging | PASS |
| `assembleRelease` | PASS |
| Build duration | 3m 24s |

The lint warnings are non-blocking findings, primarily dependency-update recommendations and existing informational warnings. They should not be silently treated as zero issues; they are recorded for a future controlled dependency-maintenance phase.

## Release artifacts

The release build produced three ABI-specific **unsigned** APKs. The build is therefore a successful release assembly, not a signed distribution release.

| ABI | Artifact | Size | SHA-256 |
|---|---|---:|---|
| `arm64-v8a` | `app-arm64-v8a-release-unsigned.apk` | 20,456,370 bytes | `f3c45e0ac69d770ed42162b731ff70b4d684a3ac95f23e79b400e32ba986d8f7` |
| `armeabi-v7a` | `app-armeabi-v7a-release-unsigned.apk` | 15,920,581 bytes | `70f8d2bc1cde2dfe95f7ae12f389f1d875e84c9dd303a02e216c2fda8c724060` |
| `x86_64` | `app-x86_64-release-unsigned.apk` | 17,315,436 bytes | `665cc02c1a31ad705dfe92b2b9c59a609115ba5642b099830e2411967e5b436b` |

The release metadata reports application ID `com.telegramdrive.uploader`, version code `15`, version name `1.0.15`, and min SDK `24`. The output filenames are unsigned because release signing secrets/keystore were not present in this local sandbox invocation. No signing secret was added to source or committed.

## Remaining validation

This verification does not replace a device or emulator smoke test. JNI loading, Telegram authentication, channel discovery, destination selection, WorkManager execution, real TDLib upload, progress telemetry, cancellation, history projection, RTL layout, TalkBack, reduced-motion behavior, and ABI runtime loading still require device or emulator evidence. The project remains **NO-GO / NOT CERTIFIED** for a production release until those checks pass and the release APK is signed through the protected CI signing path.

## References

[1]: https://developer.android.com/studio "Android Studio and command-line tools — Android Developers"
[2]: https://developer.android.com/studio/command-line "Command-line tools — Android Developers"
