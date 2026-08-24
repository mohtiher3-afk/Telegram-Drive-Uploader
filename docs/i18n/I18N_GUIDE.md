# Internationalization Guide

The application baseline supports English and Arabic only. Android string resources are the source for app-owned copy. Do not add languages or an in-app selector without a concrete product requirement and a separate maintenance phase.

## Translation Boundaries

Translate app-owned labels, instructions, errors, actions, accessibility labels, and state copy. Do not translate filenames, Telegram chat names, URLs, email addresses, URIs, IDs, UUIDs, hashes, tokens, or other user-generated/technical values.

## Resources and Placeholders

Every user-facing app string belongs in `values/strings.xml` and must have a compatible Arabic entry in `values-ar/strings.xml`. Parameter order and types must remain compatible, including `%1$s`, `%1$d`, and `%2$d`. Do not concatenate translated fragments manually when quantity grammar matters; use plural resources for any newly introduced quantity-sensitive copy.

## Direction and Mixed Text

Use logical alignment and layout direction. Keep URLs, emails, URIs, IDs, filenames, and chat names semantically unchanged in RTL contexts. Test mixed Arabic/English, Arabic plus numbers, English plus Arabic, emoji, filenames with spaces, and long names. Do not mirror nondirectional icons such as upload, settings, refresh, play, or pause merely because the layout is RTL.

## Formatting

Keep business calculations and persisted values locale-independent. Apply locale-aware formatting only at the display boundary. Use platform date/time and number APIs rather than hard-coded separators or decimal assumptions. Technical units such as KB/s, MB/s, and file-size labels should remain understandable and numerically safe.

## Verification

Use explicit golden inputs for formatting tests. Do not depend on the machine's current date or timezone. Test English/LTR and Arabic/RTL with light/dark themes, large text, scheduled time display, filenames, chat names, errors, upload progress, and history. Runtime locale evidence is separate from resource parity or unit-test evidence.
