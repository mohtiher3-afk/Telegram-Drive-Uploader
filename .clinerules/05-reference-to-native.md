# Prototype → Native Android Conversion Rules

The supplied UI reference is a Next.js/Tailwind showcase. It is a visual specification, not production behavior.

## Keep
Translate these concepts into native Compose:
- Obsidian/dark control-room base
- restrained glass surfaces
- Electric Lime as primary action signal
- Signal Cyan as secondary telemetry
- Orbit Violet as system/identity accent
- compact telemetry-style metadata
- large numeric progress when useful
- clear queue hierarchy
- bottom/rail navigation appropriate to screen size
- subtle, purposeful motion

## Do not copy
The prototype contains intentionally fake/demo behavior. Never port:
- `Math.random()` progress
- hard-coded fake transfer speeds
- hard-coded ETA
- hard-coded uploaded byte counts
- fake queue entries
- fake storage totals
- fake channel names or demo telemetry
- "LIVE" states unless a real observable state is live
- "∞ capacity" claims as factual product storage guarantees
- "Launch to Drive" wording when the actual destination is Telegram

Every production value must come from the existing app state, Room, TDLib, WorkManager, or another explicitly implemented source.

## Translation examples
Prototype | Android
---|---
GlassCard | existing `GlassCard` / `LiquidGlassSurface`
ShimmerProgress | actual progress indicator driven by persisted upload state
Radar | optional Compose visualization driven by real upload/connection state
Telemetry stats | derived values from real state
Queue tabs | existing queue filters/state; do not invent new storage states
Navigation icons | existing `AppNavigation` routes
Orbit/glow | theme tokens and existing glow components

## Copywriting
Use product-accurate language:
- Telegram destination
- Upload queue
- Uploading
- Completed
- Retry
- Cancel
- Connection
- Channel / Group / Chat
- File preparation

Do not imply Google Drive storage, unlimited capacity, or successful delivery before Telegram confirms completion.
