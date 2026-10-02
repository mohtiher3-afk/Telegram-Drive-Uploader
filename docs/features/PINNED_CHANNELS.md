# Pinned Channels in Search Results

The Telegram destination screen now supports locally pinning destinations from search results. A pinned destination is identified only by its Telegram chat ID; channel metadata continues to come from the existing Telegram repository and TDLib adapter.

## Behavior

The pin button is available on every destination row. Pinning does not select the destination and does not invoke any Telegram mutation. Pinned results are moved to the top of the current result set while preserving the original TDLib/repository order within the pinned and unpinned groups. Unpinning returns the destination to the unpinned group.

## Persistence

Pinned IDs are stored atomically in the existing `SettingsDataStore` under the `pinned_destination_ids` preference key as a sorted comma-separated list of `Long` IDs. Malformed values are ignored. No channel title, username, message content, authentication state, or upload state is duplicated in this preference.

## Accessibility and localization

The icon button exposes localized `Pin destination` and `Unpin destination` content descriptions. English and Arabic resources are kept in parity, and the row uses Compose layout primitives that respect the active layout direction.

## Protected boundaries

This feature does not change TDLib calls, Telegram authorization, destination discovery, upload workers, upload requests, Room state, or queue behavior. The pin state is UI preference state only.
