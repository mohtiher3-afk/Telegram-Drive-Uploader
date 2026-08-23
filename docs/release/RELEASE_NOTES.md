# Release Notes

## Highlights

Added persistent pinning for Telegram channels and destinations directly from search results. Pinned destinations appear first while preserving Telegram’s existing result order within each group.

## Improvements

Added Arabic and RTL-friendly pin and unpin labels, accessible content descriptions, deterministic local persistence of pinned destination IDs, and unit coverage for serialization and toggle behavior.

## Bug Fixes

No Telegram authorization, TDLib, WorkManager, or upload behavior was changed. Pinning is a local destination-selection preference only.

## Performance

No new performance measurements or speculative optimizations were introduced.

## Security

No signing keys or secret values were added. Release workflow publication remains manual.

## Known Limitations

The release workflow still requires the configured GitHub signing secrets and manual tag dispatch. Real Telegram authentication, upload, and device background testing remain separate validation steps.
