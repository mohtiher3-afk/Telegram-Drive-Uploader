# Motion System

## Principles

Motion communicates a change in page, state, hierarchy, progress, or feedback. It must be fast enough that it never delays an action, predictable enough to preserve context, and restrained enough not to compete with upload status or diagnostic content.

| Context | Pattern | Guidance |
|---|---|---|
| Onboarding page change | Short fade plus small horizontal movement | Keep the existing direction and avoid large travel distances |
| Authentication state change | Short fade | Preserve the current state as the single source of truth |
| Upload progress | Material progress rendering | Reflect actual bytes/state only; never animate fake progress |
| Error/empty/content | Immediate state change unless a short transition improves comprehension | Errors must remain visible immediately |
| Top-level navigation | Existing navigation behavior | Do not add graph-wide transitions in this phase |
| Lists | Stable keys, no blanket entrance animation | Animate only real insertion/removal in a future focused slice |
| Dialogs and sheets | Native Material 3 behavior | Do not replace built-in motion |

## Accessibility and performance

All animation is non-essential. Content, errors, and upload states must remain understandable when motion is reduced or unavailable. The current source does not expose a reduced-motion abstraction, so this phase does not invent one; future device validation should confirm Android animator-scale behavior before centralizing it. Motion must remain limited to transform/opacity-like effects and must not trigger expensive bitmap or list work.

## Scope

This system documents and lightly tokenizes the two existing state-driven transitions. It does not add animation to every composable, introduce a new framework, alter navigation destinations, or change business logic.
