# Screen-by-Screen Quality Gates

Before considering a visual screen complete, inspect its code and validate:

## Structure
- one clear screen title
- one primary action for the current state
- logical grouping of related information
- no redundant card nesting
- no arbitrary one-off spacing values when an existing token fits

## Visual
- semantic Material colors or approved shared tokens only
- consistent shape/radius family
- consistent icon sizing
- restrained glow
- no accidental high-contrast borders
- no giant headings that consume the useful viewport
- long text ellipsizes or wraps safely
- progress remains visually stable at 0/50/100%

## Interaction
- all actionable controls have a real callback
- disabled/loading states visibly communicate why action is unavailable
- destructive actions require the existing confirmation pattern where appropriate
- retry/cancel/pause actions map to real ViewModel/domain operations
- no fake button whose only purpose is visual polish

## Accessibility
- minimum 48dp touch target
- content descriptions for meaningful icons
- decorative icons marked decorative
- adequate contrast
- TalkBack order follows visual/logical order
- Arabic RTL is not broken by manual left/right positioning

## Adaptive UI
Verify compact phone and expanded layout paths. Keep the project's existing bottom navigation / navigation rail behavior instead of creating a third navigation model.

## Performance
- avoid per-frame recomposition from unstable collections
- do not animate high-frequency upload progress with expensive effects
- avoid large bitmap allocations in scrolling lists
- use stable list keys
