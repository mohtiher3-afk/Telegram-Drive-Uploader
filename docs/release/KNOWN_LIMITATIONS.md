# Known Limitations

- Local source checkout validation is separate from the authoritative signed release workflow; the v1.0.15 CI build, tests, lint, TDLib checks, artifact packaging, and APK signature verification all passed.
- Native loading is verified for all three shipped ABIs (`JNI_LOAD_STATUS=PASS`, `CLIENT_CREATE_STATUS=PASS`: arm64-v8a and armeabi-v7a on physical hardware, x86_64 on the CI emulator, API 33-36). Real Telegram authentication, channel permissions, session restoration, and real upload completion remain **NOT VERIFIED** without a configured device/session.
- The instrumented JNI smoke test and the Phase-07 regression suite run in CI emulator lanes. Background process-death recovery, notifications, runtime RTL/dark-mode/accessibility, and backup/restore still require controlled execution on a device or emulator with a real account.
- Startup, memory, battery, and real upload-throughput measurements are **NOT MEASURED**; no performance improvement is claimed.
- Large-file support is bounded by Android/TDLib/runtime limits; unlimited file-size support is not claimed.
- The current release workflow publishes signed per-ABI APKs and checksum files; it does not publish an AAB.
- Release certification remains **NOT CERTIFIED** until the outstanding device/runtime evidence is collected.
