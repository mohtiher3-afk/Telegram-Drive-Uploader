# Home Screen Redesign

## Scope

This slice redesigns only the existing `HomeScreen`. It keeps the current route, callbacks, test tags, Android document-picker contract, `HomeViewModel`, repository flows, statistics, Telegram connection state, active-upload list, and real progress presentation.

## Visual changes

The Home surface now uses the established semantic spacing tokens for the page rhythm and top spacing. The upload hero and statistic tiles use elevated Material 3 cards to create clearer hierarchy between the primary upload action, local statistics, and supporting status content. Existing color roles, typography roles, shapes, labels, icons, and state branches remain the source of truth.

## Preserved behavior

The settings action still invokes `onSettingsClick`; the connection action still invokes `onConnectClick`; and selecting videos still launches `OpenMultipleDocuments` with the existing broad `*/*` filter before forwarding non-empty URI results to `onVideosSelected`. The Home screen continues to render the real `HomeUiState`, including authorized user information, total counts, pending counts, active uploads, empty state, and `UploadStatusIndicator` values.

No new data, statistics, actions, route, ViewModel, repository, animation, permission, upload behavior, or navigation behavior was added. The screen remains RTL-ready through Compose layout primitives and semantic Material 3 roles.
