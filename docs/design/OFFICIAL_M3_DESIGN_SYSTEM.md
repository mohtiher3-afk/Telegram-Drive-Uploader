# Official Material 3 Design System

## Purpose

This project uses Jetpack Compose Material 3 as its platform design system. M3 Expressive techniques are applied selectively to improve hierarchy, feedback, and orientation rather than as decoration.

## Semantic Foundations

`MaterialTheme.colorScheme` is the source of truth for color roles. `MaterialTheme.typography` is the source of truth for type hierarchy. `MaterialTheme.shapes` is the source of truth for shape hierarchy. Shared layout decisions are captured in `core/ui/theme/DesignTokens.kt`.

## Contexts

The design system supports light and dark themes, dynamic color on Android 12 and later with custom fallbacks, compact and larger window widths, Arabic RTL and English LTR, system text scaling, and reduced-motion preferences where motion is decorative.

## Protected Behavior

Design changes must not alter navigation routes, authentication state, Telegram destination IDs, file URIs, Room records, WorkManager identity, scheduler timestamps, TDLib calls, or upload completion semantics.

## Official Sources

See `M3_OFFICIAL_REVIEW.md` and `M3_RESEARCH_NOTES.md` for the current official source links and design decisions.
