# Cline Rules — Telegram Drive Uploader

## Priority
- Read and obey AGENTS.md first. It is the permanent project policy.
- Existing architecture/design documents under docs/ are the current source of truth unless the active task explicitly defines a scoped replacement.
- The supplied `telegram-uploader-ui-design.zip` is a visual reference for the requested UI refresh, not Android source code.

## Engineering rules
1. Inspect the existing implementation before editing.
2. Reproduce a bug before fixing it when the task is a bug report.
3. Make the smallest coherent change that fixes the confirmed root cause.
4. Do not rewrite modules, navigation, upload architecture, or persistence merely for cleanup.
5. Preserve working behavior while changing presentation.
6. Never use fake upload progress, fake success states, fabricated speed/ETA, or placeholder production data.
7. Never hard-code secrets, Telegram credentials, sessions, signing keys, or build outputs.
8. Do not introduce a second theme, token layer, navigation framework, or state-management approach.
9. Verify the affected path before calling work complete. Never claim verification without evidence.
