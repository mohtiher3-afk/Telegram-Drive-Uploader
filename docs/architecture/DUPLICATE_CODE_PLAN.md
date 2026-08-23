# Duplicate Code Plan

No duplicate is approved for merging during this planning phase. Initial review should compare the following likely overlap points:

| Candidate A | Candidate B | Canonical candidate | Risk |
|---|---|---|---|
| Upload progress formatting in UI | `UploadTelemetryFormatter` | Formatter utility | MEDIUM |
| Telegram error mapping in client | Error labels in ViewModels/screens | One typed mapper with localized presentation | HIGH |
| Destination loading state | Auth/disconnected state rendering | Explicit destination state model | HIGH |
| Upload enqueue policy | Worker retry/resume policy | `UploadWorkPolicy` plus documented worker contract | HIGH |
| Video extension/MIME checks | Metadata extraction preflight | `VideoFormatSupport` as policy; extractor as metadata source | MEDIUM |
| UI status labels | Resource strings across features | Resource-backed semantic status vocabulary | MEDIUM |

Before merging any pair, search all references, add characterization tests, and validate Hilt, navigation, resources, and release builds.
