# Cline Implementation Order — Telegram Drive Uploader

Use this order for the redesign/repair programme.

## 0. Baseline
- Open current main/working branch.
- Read AGENTS.md and existing design/architecture docs.
- Run the repository quick verification.
- Record failures before making UI changes.

## 1. Visual foundation
- Consolidate all screen styling on existing MaterialTheme + DesignTokens.
- Remove accidental duplicated palettes and one-off dimensions.
- Ensure the logo and icon resources come from the canonical product assets.
- Validate light/dark and Arabic/English.

## 2. Mission Control / Home
- Refine hierarchy around connection, new upload, active transfer and summary.
- Preserve actual HomeViewModel state.
- Use glow/glass only where it improves hierarchy.

## 3. Upload / Preparation
- Make file selection, destination, scheduling, compression and queue submission visually coherent.
- Preserve URI ownership, destination IDs and existing preparation logic.
- Make progress and loading states truthful.

## 4. Queue
- Make active/retrying/failed/completed states immediately distinguishable.
- Keep queue actions connected to the existing ViewModel and WorkManager path.
- Do not invent transfer state.

## 5. Settings
- Simplify into a clean settings hierarchy.
- Preserve persisted DataStore behavior.
- Theme/glow controls must update the real theme state.

## 6. Adaptive + accessibility pass
- Phone, tablet/large screen, Arabic RTL, font scaling, TalkBack.
- Preserve navigation semantics and stable test tags.

## 7. Runtime reliability
- Authentication
- session restore
- network interruption
- background transfer
- process death
- retry/cancel
- confirmed Telegram completion

## 8. Final verification
- Run the repository verification scripts.
- Inspect CI evidence.
- Produce no release claim without release/runtime evidence.
