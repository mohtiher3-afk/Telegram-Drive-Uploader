# Telegram Drive Uploader — Native UI Design System

## Design direction

The Android product uses a **Mission Control** visual language translated into native Jetpack Compose:
- Obsidian control-room background
- Electric Lime primary action signal
- Signal Cyan for secondary telemetry
- Orbit Violet for identity/orbital decoration
- restrained glass surfaces
- strong information hierarchy
- real transfer state only

The supplied Next.js/Tailwind prototype is a visual reference. It is not a source of production state, telemetry, or business logic.

## Semantic color source of truth

All Material UI color roles come from `MaterialTheme.colorScheme`.

Reference palette:
| Role | Hex |
|---|---|
| Obsidian | #0D0D0F |
| Foreground | #ECECEF |
| Electric Lime | #A3E635 |
| Signal Cyan | #22D3EE |
| Orbit Violet | #7C3AED |
| Muted | #8B8B94 |
| Error | #F87171 |

The theme keeps light and dark semantic schemes, and the user's Glow Color preference controls the Material primary roles. Success, warning, and error remain semantic status colors.

Do not hard-code screen colors.

## Shared tokens

Use:
- `DesignTokens.AppSpacing`
- `AppRadius`
- `AppContentWidth`
- `MaterialTheme.typography`
- `MaterialTheme.colorScheme`

Use existing shared components before creating a new card/surface.

## Surfaces

Preferred hierarchy:
1. Screen background
2. Surface container
3. GlassCard / AppSurface
4. Feature/hero surface
5. Bottom navigation / dialogs

Glass effects are decorative hierarchy, never a state source. Avoid excessive blur, shadows, borders, or animated decoration.

## Screen structure

### Mission Control / Home
Show:
- Telegram connection
- primary new-upload action
- upload snapshot
- active uploads

### Upload Preparation
Show:
- selected files
- destination
- scheduling
- compression
- smart preparation
- one clear Add to Queue action

### Transfer Queue
Show:
- search
- filters
- active/retrying/failed states
- truthful progress
- retry/pause/cancel actions

### History
Show completed uploads from the existing Room-backed upload records.

### Settings
Show:
- theme
- glow color
- Telegram connection
- notification/diagnostics options
- about/version information

## Responsive layout

- Compact phone: bottom navigation
- Expanded width: navigation rail
- Large content is capped by `AppContentWidth.max`
- Use logical start/end APIs
- Do not manually mirror layouts for Arabic

## Accessibility

Minimum requirements:
- 48dp touch targets
- meaningful content descriptions
- decorative icons marked decorative
- visible focus/disabled/loading states
- Arabic RTL and English LTR
- system font scaling
- reduced-motion support

## Motion

Use motion only for orientation and meaningful state changes.
Never animate high-frequency upload telemetry unnecessarily.
Respect Android reduced-motion/animator settings.

## Prototype conversion rule

Never port prototype-only values such as:
- random progress
- fake speed/ETA
- fake byte counters
- mock queue rows
- demo destinations
- fake "LIVE" state
- fake "unlimited capacity" claims

Production UI must reflect the real TDLib, Room, WorkManager, and ViewModel state.
