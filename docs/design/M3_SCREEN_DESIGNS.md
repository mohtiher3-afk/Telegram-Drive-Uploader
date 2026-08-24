# Material 3 Screen Designs

## Home

The Home screen keeps upload selection as the primary task. Connection status, active work, and local statistics remain supporting information. The shared shell now bounds content on large screens without changing picker behavior.

## Destination

Search and destination identity remain the focus. Selection is tied to the stable Telegram ID and is not changed by visual modernization.

## File Selection and Upload Preparation

Selected files remain represented by list/grouped surfaces with filename, size, and remove actions. The add-to-queue action remains the only commit action; scheduling behavior is unchanged.

## Upload Queue

Each item communicates filename, destination, state, actual progress, speed/ETA where available, and the current action. The existing honest progress contract is preserved.

## History

History remains a completed-upload projection with existing search, sort, and deletion behavior. Visual hierarchy may distinguish success and empty states but must not change which records are returned.

## Settings

Settings remain grouped by existing capabilities: appearance, Telegram connection, diagnostics, and application information. No unsupported notification or storage setting is invented.

## Authentication and Onboarding

Authentication remains state-driven through TDLib. Onboarding retains its first-run persistence and real Android permission launchers. UI changes must not move authentication or permission logic into composables.

## Adaptive and Inclusive Rules

All screens inherit the compact/medium/expanded shell decision, semantic theme roles, localized navigation labels, logical start/end layout, Arabic RTL support, text scaling, and bounded motion expectations. Runtime verification is still required for TalkBack, large fonts, dark mode, RTL, and large screens.
