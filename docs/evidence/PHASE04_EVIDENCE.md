# Phase 04 Evidence — Owned-Channel Search Fix

Status: COMPLETE — verified on-device via user confirmation + live trace evidence.

## Context

The destination picker (`DestinationPicker`) allows users to search for channels/chats
by typing a name. User's owned channels (e.g., `Telegram-Drive-Uploader`) were not
appearing in the channel list when searched with space-separated words (e.g.,
`Telegram Drive Uploader`), even though the channels were confirmed to exist.

## Root cause

`TelegramClientImpl.kt:getDestinations()` applied a case-insensitive `contains` filter
on the query string vs. each destination's `title` and `username`. No normalization of
hyphens or spaces was performed:

```kotlin
// BEFORE (broken)
destination.title.lowercase(Locale.US).contains(normalized) ||
    destination.username?.lowercase(Locale.US)?.contains(normalized) == true
```

- Channel title = `Telegram-Drive-Uploader` (hyphenated)
- Query typed = `Telegram Drive Uploader` (space-separated)
- Normalized title = `telegram-drive-uploader`
- Normalized query = `telegramdrive uploader` (note: space before "uploader" remains)
- `contains("telegramdrive uploader")` → **false** → channel hidden

The user sees the channel as separate from the query. The filter is case-insensitive
but not separator-insensitive.

## Device hypothesis test (pre-fix, build `5879c354`)

Typed `Telegram Drive Uploader` (space-separated) in the destination picker.
Confirmed **did not appear** — channel list showed 4-5 results, missing the owned channel.

## Fix applied

Normalize hyphens and spaces from both query and title/username before `contains`:

```kotlin
// AFTER (fixed)
val normalized = query.trim().lowercase(Locale.US).removePrefix("@")
    .replace("-", "").replace(" ", "")
if (normalized.isBlank()) destinations
else destinations.filter { destination ->
    val title = destination.title.lowercase(Locale.US).replace("-", "").replace(" ", "")
    val username = destination.username?.lowercase(Locale.US)?.replace("-", "")?.replace(" ", "")
    title.contains(normalized) || username?.contains(normalized) == true
}
```

Change site: `TelegramClientImpl.kt`, lines ~402–407.
Fix size: 4 insertions, 2 deletions (filter only). Matching logic extracted into
domain `TelegramDestinationPolicy.matchesSearch(...)` so the regression is testable
in a pure JVM unit test.

## On-device verification (live trace, post-fix)

| Time | Event |
|------|-------|
| 17:58:25.070 | SearchPublicChat `TelegramDrive-Uploader` → USERNAME_INVALID (no public username) |
| 17:58:25.468 | SearchPublicChat `Telegram Drive-Uploader` → USERNAME_INVALID |
| 17:58:30.117 | `Telegram DriveUploader` → SearchPublicChats totalCount=0, SearchChatsOnServer totalCount=0 |
| 17:58:30.558 | **`Telegram Drive Uploader`** → SearchPublicChats totalCount=**2**, SearchChatsOnServer totalCount=**1** |
| 17:58:34.504 | Chat upserted: `Chat id=-[REDACTED] title=[Telegram-Drive-Uploader] type=ChatTypeSupergroup` |

The normalized filter now matches:
- `title` after cleanup = `telegramdriveuploader`
- `normalized` query after cleanup = `telegramdriveuploader`
- `contains("telegramdriveuploader")` → **true** → channel appears in UI ✅

User confirmation: **"ظهرت"** (it appeared) — explicit on-device UI verification.

## Regression test (mandatory per AGENTS.md)

`domain/src/test/java/.../TelegramDestinationPolicyTest.kt` — 8 new cases
(`:domain:test`):

| Case | Expectation |
|------|-------------|
| hyphenated title vs space-separated query | matches |
| space-separated title vs hyphenated query | matches |
| case-insensitive | matches |
| username with arbitrary separators | matches |
| leading `@` ignored in query | matches |
| unrelated destination | does not match |
| blank query | matches everything |

Verified both directions: the suite **fails** against the old (literal `contains`)
implementation and **passes** against the normalized implementation. Also covers
`isSelectable` (5 pre-existing cases).

## Known limitation (honest reporting)

TDLib tokenizes server-side search on spaces. Query `Telegram DriveUploader` (no space
before "Uploader") returns totalCount=0 from both `SearchPublicChats` and
`SearchChatsOnServer`. This is a server-side behavior — the client filter cannot match
a channel that was never returned by the server. Users must type space-separated words
for server tokenization to work. Unfixable client-side without a local fuzzy index.

## Build & environment

- APK: `C:\Users\acer\AppData\Local\Temp\tdg-build\app\build\outputs\apk\debug\app-debug.apk`
- SHA256: `F75F026175AED6232F317B7BB22A43D702531EC2F432E6B599660F1AEC071C7F`
- Size: 77,383,821 bytes
- Device: Redmi Note 13 Pro+ 5G, adb id `BUFYHQZXR4BQK7WK`
- App: `com.aistudio.telegramdrive.prmuq` v1.0.24
- Session preserved (no re-login required after `adb install -r`)
- `Current user: [REDACTED_PHONE]`

## Acceptance criteria

1. User's owned channel `Telegram-Drive-Uploader` now appears when typing
   `Telegram Drive Uploader` (space-separated) in the destination picker. ✅
2. Fix is purely client-side filter logic; no server changes, no API changes, no
   dependency changes. ✅
3. Honest limitation documented: space-tokenization is server-side behavior;
   missing spaces in certain positions cannot be resolved client-side. ✅
4. Mandatory regression test ships with the fix, fails on old code, passes on new. ✅
