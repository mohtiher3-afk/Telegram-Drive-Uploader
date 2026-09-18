# CURRENT_TASK

Phase 04 — Owned-Channel Search Fix. Status: COMPLETE — verified on-device via user UI confirmation + live trace evidence.

Device: Redmi Note 13 Pro+ 5G (arm64-v8a, Android 16 / SDK 36) — `BUFYHQZXR4BQK7WK`.

## Outcome

- Root cause: `TelegramClientImpl.kt:getDestinations()` case-insensitive `contains`
  filter did not normalize hyphens/spaces. Channel title `Telegram-Drive-Uploader`
  (hyphenated) never matched query `Telegram Drive Uploader` (space-separated).
- Fix: normalize `-` and spaces out of both query and title/username before `contains`.
  Smallest possible diff (4 insertions, 2 deletions, filter only).
- Verified on-device: user typed `Telegram Drive Uploader`, channel appeared in the
  destination list ("ظهرت").
- Live trace: SearchChatsOnServer totalCount=1 for the space-separated query; channel
  upserted `[Telegram-Drive-Uploader]` at 17:58:34.504.
- Known limitation (honest): TDLib server-side search tokenizes on spaces. Query
  `Telegram DriveUploader` (no space) returns totalCount=0 server-side; unfixable
  client-side. Users must type space-separated words.
- Evidence: `docs/evidence/PHASE04_EVIDENCE.md`.

## Next

- Roadmap item after Phase 04. Optional follow-up: reduce/remove the debug diagnostics
  instrumentation (the `Gate` flood from `rebuildDestinations` is noisy), as a separate,
  verified change. Confirm with user before proceeding.