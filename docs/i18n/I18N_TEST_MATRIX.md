# Internationalization Test Matrix

**Rule:** Do not claim global localization support beyond English and Arabic. Runtime locale and timezone results require explicit execution.

| Scenario | English | Arabic | RTL | Dark | Large Font | Status |
|---|---|---|---|---|---|---|
| Startup | App-owned strings resource-backed | Arabic resources exist | Manifest supports RTL | Theme preference exists | Not tested | NOT VERIFIED |
| Authentication | Labels/resources present | Phone/help/error resources present | Form order untested | Not tested | Not tested | NOT VERIFIED |
| Home | Resource-backed UI baseline | Resource-backed UI baseline | Not tested | Not tested | Not tested | NOT VERIFIED |
| Destination | Search/actions localized | Arabic resources/parity present | Chat names/mixed text untested | Not tested | Not tested | NOT VERIFIED |
| File selection | Filename remains user value | Filename remains user value | Bidi/long names untested | Not tested | Not tested | NOT VERIFIED |
| Upload | Status/progress resources present | Status/progress resources present | Mixed metadata untested | Not tested | Not tested | NOT VERIFIED |
| History | Date display uses platform formatter where present | Date display untested | Long names untested | Not tested | Not tested | NOT VERIFIED |
| Scheduler | Locale-aware display formatter | Arabic display untested | Date/time order untested | Not tested | Not tested | NOT VERIFIED |
| Settings | Diagnostic time uses default locale | Arabic screen untested | Direction untested | Not tested | Not tested | NOT VERIFIED |
| Notifications | No upload notification implementation | Not applicable | Not applicable | Not applicable | Not applicable | NOT APPLICABLE |
| Placeholders | Counts/signatures statically match | Counts/signatures statically match | Not applicable | Not applicable | Not applicable | PASS (static) |
| Technical IDs | Must remain unlocalized | Must remain unlocalized | Copy/order untested | Not applicable | Not applicable | POLICY |
| Timezones | UTC/Saudi/DST runtime untested | UTC/Saudi/DST runtime untested | Not applicable | Not applicable | Not applicable | NOT VERIFIED |

## Golden Values

Future deterministic tests should use explicit epoch timestamps, byte counts, percentages, speeds, durations, and quantities. They must not depend on the machine's current date, timezone, or locale.
