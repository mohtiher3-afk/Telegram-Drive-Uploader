# Android Version Matrix

The build configuration establishes API 24 as the minimum and API 36 as compile/target. No connected device or emulator evidence was available for this review, so runtime columns remain `NOT TESTED` or `BLOCKED`.

| Android Version/Class | Build | Install | Startup | Auth | Upload | Background | Notifications | UI | Status |
|---|---|---|---|---|---|---|---|---|---|
| API 24–28 | PASS by configured minSdk/build compatibility; device build not exercised | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT CERTIFIED |
| API 29–32 | PASS by configured minSdk/build compatibility; device build not exercised | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT CERTIFIED |
| API 33–35 | PASS by configured minSdk/build compatibility; device build not exercised | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT CERTIFIED |
| API 36 | PASS; local compile/lint/build and artifact checks passed | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | BLOCKED for runtime certification |

Build PASS means the repository compiled and its configured artifacts were validated. It does not imply install, startup, authentication, upload, background, notification, or UI PASS on a physical device.
