# Change Protocol for Cline

For each requested change:

### Phase A — Inspect
Read:
1. AGENTS.md
2. relevant docs under docs/
3. the target screen/component
4. the target ViewModel/state source
5. relevant tests

Search for existing components/tokens before creating new ones.

### Phase B — State the scope internally
Identify:
- exact files that need change
- whether the change is visual, behavioral, or both
- protected paths that must remain untouched
- tests required

Do not broaden the scope silently.

### Phase C — Implement
Prefer the smallest coherent patch.
Reuse existing components.
Do not duplicate existing tokens or helpers.
Do not move code between modules unless explicitly required.

### Phase D — Verify
Run the narrowest relevant checks immediately.
For visual changes, run Compose/screenshot tests where available.
For behavior changes, run unit/regression tests.
For upload/TDLib/WorkManager changes, obtain device/runtime evidence before claiming resolution.

### Phase E — Review
Before finishing:
- inspect the diff
- search for accidental hard-coded colors
- search for fake/mock production values
- search for untranslated literals
- verify testTags used by affected tests still exist
- verify no protected behavior was modified unintentionally

### Phase F — Report
Report only what was actually changed and verified.
Use "not verified" for anything requiring unavailable device credentials or runtime conditions.

Never respond with a generic "done" after only editing source.
