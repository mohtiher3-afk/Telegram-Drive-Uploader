# RTL and Arabic Design Guidelines

## Current contract

Arabic support is a product requirement. Future UI work must treat layout direction as a first-class concern while leaving the existing navigation, upload, Telegram, and authentication flows unchanged.

| Area | Guideline | Safe implementation |
|---|---|---|
| Alignment | Align text according to content direction | Prefer `Text` defaults or `TextAlign.Start`; avoid hardcoded left alignment for user text |
| Spacing | Use logical edges | Prefer `paddingStart`/`paddingEnd` equivalents and Compose `start`/`end` APIs |
| Rows | Keep semantic order | Do not reverse a row merely because the locale is Arabic; let `LayoutDirection` handle physical placement where appropriate |
| Arrows | Mirror directional navigation icons | Use auto-mirrored icons for back/forward semantics; do not mirror upload, Telegram, status, or media icons that are not directional |
| Navigation | Preserve selected-state semantics | Bottom navigation and rail selection must remain tied to route identity, not physical left/right position |
| Numbers and sizes | Keep technical values legible | Preserve numeric file sizes, percentages, speed, and ETA semantics; avoid concatenating localized strings unsafely |
| Input | Preserve cursor and label clarity | Use Material 3 text fields and semantic labels; do not infer direction from placeholder text alone |
| Accessibility | Keep content descriptions meaningful | Describe actions and status, not physical placement |
| Testing | Test both directions | Validate key screens under Arabic RTL and English LTR, including upload progress, errors, empty states, and navigation |

## Design-system rule

Shared components should use logical start/end parameters and Material 3 semantic roles. No new left/right-specific token or component should be introduced unless it represents a genuinely physical asset or platform constraint.

## Scope and limitations

This phase establishes guidance and shared-token expectations. It does not rewrite every existing screen, introduce a locale picker, change string resources, or alter navigation and upload behavior.
