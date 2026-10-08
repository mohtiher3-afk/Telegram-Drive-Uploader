# UI Rules — Mission Control Refresh

The target is a premium, modern Android UI inspired by the supplied prototype: dark technical surfaces, controlled glow, strong hierarchy, glass-like cards, and clear upload state.

## Visual tokens
Target palette:
- Obsidian: #0D0D0F
- Foreground: #ECECEF
- Electric Lime: #A3E635
- Signal Cyan: #22D3EE
- Orbit Violet: #7C3AED
- Muted: #8B8B94
- Error reference: #F87171

Implement these through the project's semantic Material 3 roles and shared tokens. Do not scatter raw hex colors across screens.
`docs/design/DESIGN_SYSTEM.md` names the theme's semantic colour scheme as the system of record, and
`DesignTokens.AppColors` delegates to it; do not restate raw hex values in a second palette.

## Existing source of truth
Use:
- `MaterialTheme`
- `feature/src/main/java/com/telegramdrive/uploader/core/ui/theme/DesignTokens.kt`
- `feature/src/main/java/com/telegramdrive/uploader/core/ui/components/`

Do not create a parallel theme/token system.

## Typography
Use existing MaterialTheme typography for Android. Do not add Inter/Space Grotesk solely to imitate the web prototype. Preserve Arabic RTL rendering, font scaling, and accessibility.

## Surfaces
- Prefer the existing `GlassCard`, `LiquidGlassSurface`, glow/bento components where appropriate.
- Keep elevation restrained; use tonal contrast, hairlines, and controlled glow.
- Avoid decorative gradients, borders, shadows, and animations that do not communicate state.
- Keep touch targets at least 48dp.

## Screen mapping
- Mission Control → `feature/src/main/java/com/telegramdrive/uploader/feature/home/HomeScreen.kt`
- Live Uplink / preparation → `feature/src/main/java/com/telegramdrive/uploader/feature/upload/UploadScreen.kt`
- Transfer Queue → `feature/src/main/java/com/telegramdrive/uploader/feature/queue/QueueScreen.kt`
- Settings → `feature/src/main/java/com/telegramdrive/uploader/feature/settings/SettingsScreen.kt`

Use the prototype's concepts, but keep the app's real terminology and actual data.

## Logo and icons
The canonical product mark is the repository's Mission Control orbital upload logo. Do not reintroduce placeholder-logo assets or invent a replacement logo without an explicit branding task. Keep one coherent icon family.
