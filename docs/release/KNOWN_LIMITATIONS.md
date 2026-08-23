# Known Limitations

- Local source checkout validation is separate from the authoritative signed release workflow; the v1.0.15 CI build, tests, lint, TDLib checks, artifact packaging, and APK signature verification all passed.
- Real Telegram authentication, channel permissions, session restoration, and real upload completion are **NOT VERIFIED** without a configured device/session.
- Instrumentation, background process-death recovery, notifications, runtime RTL/dark-mode/accessibility, and backup/restore require device or emulator execution.
- Startup, memory, battery, and real upload-throughput measurements are **NOT MEASURED**; no performance improvement is claimed.
- Large-file support is bounded by Android/TDLib/runtime limits; unlimited file-size support is not claimed.
- The current release workflow publishes signed per-ABI APKs and checksum files; it does not publish an AAB.
- Release certification remains **NOT CERTIFIED** until the outstanding device/runtime evidence is collected.
