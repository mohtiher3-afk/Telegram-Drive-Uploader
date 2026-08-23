# Locale-Safe Formatting Audit

Formatting changes in this phase are presentation-only. Stored timestamps, byte counts, upload percentages, Telegram IDs, filenames, and API values remain unchanged.

| Value | Current source | Required presentation rule | Translation/localization boundary | Status |
|---|---|---|---|---|
| File size | `formatFileSize` shared utility | Keep binary units and stable technical symbols; localize surrounding label only | `KB`, `MB`, `GB`, `TB` are technical symbols | Review before changing utility |
| Percentages | Upload progress/domain values | Preserve calculation; use a localized string resource for any label | Numeric value may follow locale policy, but must not alter actual progress | Review |
| Duration | History elapsed formatter | Preserve seconds/minutes semantics; avoid translating the numeric value as content | Unit labels can be resource-backed in a later focused change | Review |
| Schedule time | Upload screen timestamp presentation | Use locale-aware date/time display for user-facing scheduled values | Stored epoch milliseconds remain unchanged | Review |
| Diagnostic time | Settings `SimpleDateFormat("HH:mm:ss", Locale.getDefault())` | Prefer locale-aware formatter without changing event timestamp | Event names/messages remain logging content | Review |
| Counts | Home/history summaries | Use parameterized strings or plurals where source evidence requires them | Arabic plural rules apply to UI count sentences | Review |
| Telegram IDs | Repository/domain/TDLib | Never localize, reformat, or translate | Technical identifier | Protected |
| Phone numbers | Authentication UI | Preserve user-entered number and use English numerals where required for technical identity | Never translate digits inside account identity | Protected |
| Filenames | File picker/upload UI | Preserve exactly; truncate visually only | User content | Protected |
| Usernames/chat names | Telegram UI | Preserve exactly; no translation | Telegram content | Protected |

The current audit does not justify changing the shared size formatter or stored time model before the UI string extraction is complete. Any later formatter change must be covered by tests for zero, one, two, and large values, as well as Arabic and English locale expectations.
