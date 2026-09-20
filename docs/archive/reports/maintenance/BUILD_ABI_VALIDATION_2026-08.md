# Build, APK, and ABI Validation — August 2026

## Environment

| Field | Value |
|---|---|
| JDK | OpenJDK 17 |
| Android SDK root | `/home/ubuntu/android-sdk` |
| Android compatibility configuration | compileSdk 36, targetSdk 36, minSdk 24 |
| Declared APK ABIs | arm64-v8a, armeabi-v7a, x86_64 |
| Gradle execution mode | `--no-daemon --no-configuration-cache --max-workers=1` with 768 MiB heap and 384 MiB metaspace bounds |

## Current-source build evidence

| Command | Result | Notes |
|---|---|---|
| `:app:compileDebugKotlin` | PASS | Completed successfully after the queue, metadata-diagnostic, Queue, and History changes. |
| `:app:testDebugUnitTest` | PASS | Completed successfully for the current source. |
| `:app:assembleDebug` | PASS | Generated one Debug APK per declared ABI. |
| `scripts/check-tdlib-artifacts.sh` | PASS | Validated expected TDLib Java bindings, ELF structure, architecture, and declared runtime dependencies. |
| `scripts/check-workmanager-manifest.sh` | PASS | Validated manual WorkManager configuration and Hilt worker-factory connection. |
| `scripts/check-secrets.sh` | PASS | Tracked-source scan completed without reporting private-key, bearer-token, or Telegram bot-token-shaped material. |

## Debug APK inspection

| ABI | Artifact | Size | SHA-256 | Native TDLib entry |
|---|---|---:|---|---|
| arm64-v8a | `app-arm64-v8a-debug.apk` | 43 MB | `464e1cf01015a1b99ffc66ae882574ef6c07d90fdbf951a7c4ab629a0ab3eecb` | `lib/arm64-v8a/libtdjni.so` |
| armeabi-v7a | `app-armeabi-v7a-debug.apk` | 37 MB | `6458c72e1ae980fad1afe567c6a0387f8dc8fa6afd690d8c27fb5d0d5a690934` | `lib/armeabi-v7a/libtdjni.so` |
| x86_64 | `app-x86_64-debug.apk` | 39 MB | `0165271db8e9968f69650c6c9918b91e18723b3a76b407b181cc978f7d238dc7` | `lib/x86_64/libtdjni.so` |

## Tooling correction

The first artifact-check attempt could not validate exact ELF architecture because `readelf` was absent from the sandbox. The repository script correctly failed closed. Installing the standard host `binutils` package enabled the intended static inspection; no repository source, TDLib artifact, ABI setting, or signing file was changed.

## Evidence boundary

This evidence proves compilation, unit tests, Debug packaging, and static ABI/TDLib artifact integrity for the listed outputs. It does not prove native loading on a device, `Client.create()` runtime behavior, Android 16 behavior, real account login, destination permissions, WorkManager execution under system constraints, or Telegram delivery.

## References

[1]: https://developer.android.com/build "Android build system documentation"
[2]: https://core.telegram.org/tdlib/docs/ "TDLib documentation"
