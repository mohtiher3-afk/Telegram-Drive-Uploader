# Branding Rules

## Live Android mark

The canonical product asset is the Mission Control orbital upload logo:
`feature/src/main/res/drawable-nodpi/mission_control_logo.png`

Visual ingredients:
- Lime upload arrow + tray
- Violet orbital ring
- Mint/Cyan telemetry highlights
- Obsidian-safe flat surface, no gradients or 3D effects

The launcher icon is its own adaptive vector family under
`app/src/main/res/drawable/ic_launcher_*.xml` (`foreground`, `background`,
`monochrome`) with the density `mipmap-*/ic_launcher*.webp` legacy rasters.
`app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` wires the adaptive layers.

## Rules

- Do not introduce placeholder logos.
- Do not create multiple competing logos.
- Do not use arbitrary gradients or 3D effects for the product mark.
- Keep the launcher foreground, splash, and onboarding aligned to the same mark.
- Preserve the existing resource name only when compatibility requires it.
- New UI should reference the canonical asset rather than adding another copy.
