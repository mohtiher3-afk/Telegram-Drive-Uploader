# Pinterest Reference — Futuristic SaaS Dashboard UI with Glassmorphism

**Reference:** https://www.pinterest.com/pin/1152006779747004850/

## Observed visual language

The reference uses a near-black blue field, translucent charcoal cards, magenta-to-cyan edge light, compact telemetry, and a strong device-card focal point. The hierarchy is modular: one high-energy hero surface carries the glow, while smaller cards preserve contrast with restrained borders and muted labels.

## Translation to Telegram Drive Uploader

| Reference element | Safe adaptation |
|---|---|
| Frosted dashboard surfaces | Use `surfaceContainer`/tonal Material 3 cards with a subtle outline; do not reduce contrast for text, progress, or errors. |
| Cyan/magenta accent light | Keep it as a clipped Aurora edge treatment for the upload hero or notification sheet only; do not apply it to every row. |
| Compact data chips | Apply to upload state, speed, ETA, and notification permission state; each chip must retain a text label and semantic state. |
| Modular card rhythm | Use larger spacing around the upload hero, then smaller grouped status cards for queue/history/settings. |
| Glass layering | Use it for depth only. Failed uploads, permission denial, and connection errors continue to use Material semantic error colors rather than decorative neon. |

## Guardrails

This reference is inspiration, not a UI copy. The app will retain Arabic RTL, Material 3 accessibility, reduced-motion behavior, real TDLib confirmation semantics, and generic private notification copy. No change to upload, WorkManager, authentication, or notification authority is implied by this visual review.
