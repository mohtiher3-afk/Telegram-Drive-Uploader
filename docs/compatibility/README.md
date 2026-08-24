# Android Compatibility and Platform Behavior

This directory records configured support boundaries and runtime evidence status. It does not claim compatibility for untested devices or OEMs.

| Document | Purpose |
|---|---|
| `SUPPORTED_ENVIRONMENT.md` | Actual SDK, ABI, locale, orientation, window, backup, and background configuration. |
| `ANDROID_VERSION_MATRIX.md` | API-level build and runtime verification matrix. |
| `DEVICE_AND_SCREEN_MATRIX.md` | Representative device classes and screen-size checks. |
| `PLATFORM_BEHAVIOR.md` | Insets, orientation, permissions, background, storage, native, RTL, theme, and font-scale review. |
| `FINAL_COMPATIBILITY_REPORT.md` | Summary of evidence, limitations, and release relationship. |

Runtime status must use `PASS`, `FAIL`, `NOT TESTED`, or `BLOCKED`. Static build evidence must not be presented as device certification.
