# Telegram API Credentials Device-Error Diagnosis

This document provides a trace, analysis, and secure diagnostic guide for handling the Telegram `Authentication Error` and configuring your own API credentials without exposing sensitive secrets in the repository.

---

## 1. Trace of the "Authentication Error"

When a user attempts to connect or authenticate on the Telegram login screen, an `Authentication Error` can be surfaced in the UI.

### The UI Component (`TelegramAuthScreen.kt`)
If the connection state enters `TelegramConnectionState.ERROR`, the screen renders a liquid-glass styled Error Card with the header `authentication_error`:
- **Header Text**: `R.string.authentication_error` ("Authentication Error" / "خطأ في المصادقة").
- **Detail Text**: Dynamically mapped via `error.messageResId()`.
- If credentials are empty or invalid, it displays `R.string.telegram_error_invalid_credentials` ("Telegram API credentials are not configured correctly.").

### The Connection Guard (`TelegramClientImpl.kt`)
Before initializing the native `tdjni` client or requesting any TDLib operations, `TelegramClientImpl` performs a compile-time and runtime check on the API credentials via the `isConfigured` property:

```kotlin
override val isConfigured: Boolean
    get() = BuildConfig.TELEGRAM_API_ID.toIntOrNull()?.let { it > 0 } == true &&
        BuildConfig.TELEGRAM_API_HASH.isNotBlank() &&
        BuildConfig.TELEGRAM_API_HASH != "placeholder_hash"
```

If `isConfigured` returns `false`, `connect()` immediately halts and fails with:
```kotlin
fail(TelegramError.InvalidCredentials, "Telegram API credentials are not configured.")
```
This transitions the state machine to `TelegramConnectionState.ERROR` and sets `_error` to `TelegramError.InvalidCredentials`, producing the localized warning on the user's screen.

---

## 2. Secure Credential Injection Context

To prevent the exposure of sensitive client-side credentials (like Telegram API IDs and Hashes) in the source code or git history, the project utilizes compile-time injection.

1. **Local Isolation (`.env`)**:
   Credentials are kept in a local, untracked `.env` file at the project root. This file is ignored by git (`.gitignore`).
2. **Secrets Gradle Plugin**:
   The secrets plugin reads `.env` variables during compilation and injects them as static constants into the generated `BuildConfig` class:
   - `BuildConfig.TELEGRAM_API_ID`
   - `BuildConfig.TELEGRAM_API_HASH`
3. **CI/CD Integration**:
   GitHub Actions workflows read these values from GitHub Repository Secrets (`secrets.TELEGRAM_API_ID`, `secrets.TELEGRAM_API_HASH`) and dynamically construct the `.env` file prior to compiling the release builds.

This structure guarantees that secrets are never hardcoded into production code files or visible in pull requests.

---

## 3. Minimum-Risk Secure Configuration Pathways

If you are developing or building a customized version of the app, follow these secure steps to configure your own credentials:

### A. Local Development Configuration
1. Obtain an API ID and API Hash from the official [my.telegram.org](https://my.telegram.org) developer portal.
2. In the root directory of your project, create a file named `.env`:
   ```env
   TELEGRAM_API_ID=1234567
   TELEGRAM_API_HASH="abcdef0123456789abcdef0123456789"
   ```
3. Run the Gradle build task (`./gradlew assembleDebug` or `compile_applet`). The Secrets Gradle Plugin will automatically generate the correct fields in `BuildConfig`.

### B. CI/CD Release Configuration
1. Go to your GitHub Repository Settings.
2. Navigate to **Secrets and variables** -> **Actions**.
3. Create two repository secrets:
   - `TELEGRAM_API_ID`: Set this to your integer API ID.
   - `TELEGRAM_API_HASH`: Set this to your API Hash string.
4. When the GitHub Actions Release workflow runs, it will safely read these secrets, construct the `.env` file, and build a signed release-ready APK.

---

## 4. Manual Device Rebuilding & Testing Steps

To compile, install, and physically test the Telegram auth and upload pathways:

1. **Build the APK**:
   Execute the following command to generate multi-ABI debug APKs containing your `.env` credentials:
   ```bash
   gradle assembleDebug
   ```
2. **Locate the Artifact**:
   The compiled APKs will be located under the build outputs folder:
   ```text
   app/build/outputs/apk/debug/app-arm64-v8a-debug.apk
   app/build/outputs/apk/debug/app-armeabi-v7a-debug.apk
   app/build/outputs/apk/debug/app-x86_64-debug.apk
   ```
3. **Install on Device**:
   Transfer the appropriate APK to your physical Android device or emulator and install it.
4. **App Private Directory Boundaries**:
   The native TDLib instance initializes and safely stores all authentication states, session keys, databases, and temporary files within the app-private sandbox:
   - **Database Location**: `/data/data/com.telegramdrive.uploader/files/tdlib-database`
   - **Files Cache Location**: `/data/data/com.telegramdrive.uploader/files/tdlib-files`
   
   These directories are sandboxed by the Android OS, protecting sensitive user sessions and credentials from access by other applications on the device.
