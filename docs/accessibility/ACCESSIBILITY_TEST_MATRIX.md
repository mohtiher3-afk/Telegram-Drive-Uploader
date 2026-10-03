# Accessibility Test Matrix

**Rule:** No formal accessibility certification is claimed. `NOT VERIFIED` means the scenario was not executed on a device or emulator.

| Scenario | English | Arabic | Light | Dark | Small | Large | Status |
|---|---|---|---|---|---|---|---|
| TalkBack | Semantics statically present in Material controls | Resource/RTL path exists | Not tested | Not tested | Not tested | Not tested | NOT VERIFIED |
| Large font | No font scaling disabled | Arabic wrapping untested | Not tested | Not tested | Not tested | Not tested | NOT VERIFIED |
| Display size | Compose width/weight foundations present | Long labels untested | Not tested | Not tested | Not tested | Not tested | NOT VERIFIED |
| Navigation | Material navigation labels present | Spoken order untested | Not tested | Not tested | Not tested | Not tested | NOT VERIFIED |
| Forms | Auth/search controls use Compose inputs | Arabic IME/focus untested | Not tested | Not tested | Not tested | Not tested | NOT VERIFIED |
| Upload | Status/action labels visible; progress value exists | Localized state wording needs test | Not tested | Not tested | Not tested | Not tested | NOT VERIFIED |
| Dialogs | Material dialog semantics where used | Translation/focus untested | Not tested | Not tested | Not tested | Not tested | NOT VERIFIED |
| Errors | Visible error paths exist | Arabic error flow untested | Not tested | Not tested | Not tested | Not tested | NOT VERIFIED |
| Reduced motion | AppMotion exists; runtime preference untested | Same | Not tested | Not tested | Not tested | Not tested | NOT VERIFIED |

## Automated Coverage

The repository contains focused JVM tests for upload progress formatting and related domain behavior. No brittle exact-semantics test was added during this documentation-only review. Compose semantics tests should be added only for user-facing behavior after a confirmed issue or a testable semantics contract is approved.
