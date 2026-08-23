# Known Limitations

- The temporary checkout has no Gradle wrapper or standalone Gradle executable, so local release APK/AAB, test, and lint commands are **NOT VERIFIED**.
- TDLib artifact validation is **NOT VERIFIED** locally because exact ELF architecture checks require `readelf`; artifacts must still pass the CI guard.
- Real Telegram authentication, channel permissions, and real upload completion are **NOT VERIFIED** without a configured device/session.
- Instrumentation, background process-death recovery, notifications, RTL/dark-mode/accessibility, and backup/restore require device or emulator execution.
- Large-file support is bounded by Android/TDLib/runtime limits; unlimited file-size support is not claimed.
- Release signing is **RELEASE SIGNING NOT VERIFIED**; no signing secrets were accessed.
