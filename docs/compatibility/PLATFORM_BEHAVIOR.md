# Platform Behavior Review

## Window and Insets

`MainActivity` calls `enableEdgeToEdge()`. Compose navigation disables default content window insets and onboarding applies navigation-bar padding. Status-bar, navigation-bar, cutout, IME, dialog, and OEM-specific inset behavior remain runtime evidence items; no global inset hack was added.

## Orientation and Configuration

No manifest orientation lock was observed. Rotation, activity recreation, process recreation, ViewModel state, navigation state, queue state, authentication state, and UI state require device or emulator verification. The application’s actual orientation policy must not be changed merely to make a test pass.

## Permissions

The manifest declares `INTERNET`, `ACCESS_NETWORK_STATE`, `READ_MEDIA_VIDEO`, API-32-conditional `READ_EXTERNAL_STORAGE`, and `WAKE_LOCK`. No notification, foreground-service, or test-only permission was added in this review. Permission timing and denial behavior remain runtime test items.

## Background and Doze

WorkManager and `WAKE_LOCK` are configured for background upload support. Lock-screen, background, process-death, battery-saver, Doze-like, and OEM background-restriction behavior are not certified without a representative device. The protocol does not authorize disabling background processing to obtain a passing test.

## Storage and Media Access

The upload path uses Android-selected media and content-resolver/URI-based access. Video, large files, Arabic filenames, English filenames, and mixed filenames require runtime verification across supported API classes. No deprecated absolute-path assumption or new filesystem workaround was introduced.

## Native Runtime

Static artifact checks confirm `arm64-v8a`, `armeabi-v7a`, and `x86_64` native libraries and required dependencies. JNI loading and `Client.create()` remain unverified without a supported device or emulator for each ABI.

## UI Modes

Arabic resources and `supportsRtl=true` are present. Light/dark theme, font scaling, large-screen layout, dialogs, progress, forms, long filenames, and navigation require representative runtime evidence. No untested device or OEM is claimed as supported.
