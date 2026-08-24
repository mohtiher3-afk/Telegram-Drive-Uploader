# Telegram Drive Design System

## Foundation

The Android UI uses Jetpack Compose Material 3 through `MaterialTheme`. The system of record is the theme's semantic color scheme, typography, shapes, and the shared layout tokens in `core/ui/theme/DesignTokens.kt`.

## Tokens

Use semantic Material roles such as `primary`, `primaryContainer`, `secondaryContainer`, `surface`, `surfaceContainer`, `onSurface`, `onSurfaceVariant`, `error`, and their corresponding `on-*` roles. Use `AppSpacing` and `AppContentWidth` for shared layout decisions rather than introducing arbitrary screen-specific values.

## Component Rules

Use the established Material 3 component that matches the user intent. Buttons express action hierarchy, fields collect input, lists show files or destinations, progress indicators show actual upload state, and NavigationBar/NavigationRail switch between existing app destinations.

## Themes and Contexts

Light and dark themes use semantic roles. Dynamic color is available on Android 12 and above with custom fallback schemes. Arabic RTL and English LTR are supported through logical layout direction and localized resources. Runtime verification remains required for contrast, scaling, TalkBack, and large screens.

## Protected Behavior

Visual work must not change routes, authentication, Telegram destination IDs, file URIs, Room schemas, WorkManager identities, scheduler timestamps, TDLib calls, or upload completion semantics.

## References

See `M3_OFFICIAL_REVIEW.md`, `M3_COMPONENT_MAP.md`, `M3_SCREEN_DESIGN_MATRIX.md`, and `M3_ADAPTIVE_UI.md`.
