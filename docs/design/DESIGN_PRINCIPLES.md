# Material 3 Design Principles

The application is a utility for selecting files, preparing real Telegram uploads, monitoring background work, and reviewing history. Its design system should make the next safe action obvious without competing with upload progress or diagnostic information.

| Principle | Application decision |
|---|---|
| Material 3 first | Use `MaterialTheme` roles, typography, shapes, and standard Compose components as the visual source of truth. |
| Clear hierarchy | Reserve display/headline styles for onboarding and screen titles; use title and body roles for file and upload metadata. |
| Primary action clarity | Keep one dominant action per task state, especially for selecting files, connecting Telegram, choosing a destination, and starting an upload. |
| Minimal noise | Avoid decorative gradients, arbitrary borders, excessive shadows, and one-off card treatments. |
| Consistent spacing | Prefer a small shared spacing scale for new or refactored shared UI. |
| Accessible contrast | Pair semantic container colors with their corresponding `on*` roles and preserve visible focus and enabled/disabled states. |
| Touch-friendly controls | Keep standard Material 3 minimum interactive sizes and do not replace platform components with custom button implementations without a concrete need. |
| Responsive layout | Preserve the existing compact bottom bar and expanded navigation rail behavior; use start/end-aware layout APIs. |
| Honest states | Loading, empty, error, retry, queued, uploading, and confirmed completion states must remain visually distinct and must not imply fake upload success. |
| Purposeful motion | Retain existing motion and avoid adding animations to high-frequency upload progress or background state updates. |
| Performance | Keep media previews and upload lists bounded; design tokens must not introduce expensive recomposition or large in-memory assets. |

## Brand and dynamic color

The existing warm red brand palette remains the fallback identity. Android 12+ dynamic color remains supported through the existing `dynamicColor` parameter so the caller can opt into system-derived schemes without removing the branded fallback. This phase does not change that default or replace brand colors blindly.

## Scope

This document governs shared theme and token work. It does not authorize a screen-by-screen redesign, changes to business state, navigation, upload behavior, or Telegram integration.
