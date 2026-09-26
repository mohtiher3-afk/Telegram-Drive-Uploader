# Telegram Drive Uploader - UI Design Document

## Overview

This document describes the user interface design for the Telegram Drive Uploader application. The focus is on the **Settings** screen, which manages application preferences including theme, glow color, onboarding status, Telegram connection state, and account/pinned destinations.

## Screens

### 1. Settings Screen (Main Settings UI)

#### Layout
- **Header**: App logo + "Settings" tab
- **Sidebar Navigation**: Settings, Profile, Onboarding, Accounts, Pinned Destinations
- **Main Content Area**: Grid of preference cards

#### Preference Cards

**Card 1: Theme**
- Label: "App Theme"
- Options: "System" (default), "Dark", "Light", "Custom"
- Visual: Toggle switch or dropdown
- Default: System

**Card 2: Glow Color**
- Label: "Glow Color"
- Type: Color picker (hex code + color swatch)
- Presets: Cobalt (SEAFOAM), Amber, Blue, Purple, Custom
- Default: Cobalt

**Card 3: Cache Size**
- Label: "Thumbnail Cache"
- Display: Human-readable value (e.g., "128 MB")
- Adjustable slider (optional)
- Default: Auto (128 MB)

**Card 4: Telegram Connection**
- Label: "Telegram Connected"
- Indicator: Green checkmark (connected) / Red X (disconnected)
- Status text: "Connected" / "Disconnected"
- Default: Disconnected (until login)

**Card 5: Onboarding Status**
- Label: "Onboarding Complete"
- Indicator: Checkmark icon
- Text: "Yes" / "Not Started"
- Default: Not Started

**Card 6: Accounts & Pinned Destinations**
- Label: "Accounts & Pins"
- Sub-item: "Accounts" - List of connected Telegram accounts
- Sub-item: "Pinned Destinations" - Selected folders/channels
- Toggle: "Enable Pin" (on/off)

### 2. Theme Selection Detail (Expandable Card)

When "Theme" card is expanded:
- **System Theme**: Uses OS default (light/dark)
- **Dark Theme**: Dark background with light text
- **Light Theme**: Light background with dark text
- **Custom Theme**: User-defined color scheme (primary, secondary, accent)

### 3. Glow Color Detail (Expandable Card)

When "Glow Color" card is expanded:
- **Cobalt (SEAFOAM)**: Classic teal glow
- **Amber**: Warm orange glow
- **Blue**: Cool blue glow
- **Purple**: Violet glow
- **Custom**: Hex code input field + preview

### 4. Cache Size Detail (Expandable Card)

When "Cache Size" card is expanded:
- Real-time counter showing current cache size
- Slider to adjust (increase/decrease)
- Memory usage percentage
- Storage location indicator

### 5. Telegram Connection Detail (Expandable Card)

When "Telegram Connection" card is expanded:
- Connection status (Online/Offline)
- Last sync time
- Error logs (if any)
- Reconnect button

### 6. Onboarding Detail (Expandable Card)

When "Onboarding" card is expanded:
- Step indicator (1/3 or 1/4)
- Completion progress bar
- Brief description of onboarding benefits
- Next steps hint

## Color Palette

| Role | Primary | Secondary | Accent |
|------|---------|-----------|---------|
| Background | #1E1E1E (Dark) | #F5F5F5 (Light) | #007AFF (Blue) |
| Text (Primary) | #FFFFFF | #000000 | #FFFFFF |
| Text (Secondary) | #B0B0B0 | #666666 | #B0B0B0 |
| Success | #34C759 | #34C759 | #34C759 |
| Warning | #FF6B6B | #FF6B6B | #FF6B6B |
| Error | #FF5252 | #FF5252 | #FF5252 |
| Theme Colors | #212121, #757575, #ECECEC, #BDBDBD | #667EEA, #FF6B6B, #FFD166, #ABAAED |

## Screen Refresh Rate Optimization

All screens are optimized for high-refresh-rate displays (60Hz, 90Hz, 120Hz).

### Refresh Rate Configuration
- **AndroidManifest.xml**: `android:hasMaxRefreshRate="true"` and `android:preferredRefreshRate="120"`
- **`RefreshRateHelper`**: Queries and caches the device's refresh rate via `WindowManager`
- **`FrameRateOptimizer`**: Adjusts animation durations and easing curves per refresh rate
- **`FrameRateAwareAnimation`**: Auto-tuned animation system for smooth 60/90/120Hz transitions
- **`RefreshRateMonitor`**: Composable that monitors display changes in real-time

### Animation Durations by Refresh Rate
| Refresh Rate | Animation Duration | Easing Duration |
|-------------|-------------------|-----------------|
| 120Hz | 200ms | 150ms |
| 90Hz | 250ms | 200ms |
| 60Hz | 300ms | 250ms |

### Key Files
- `core/ui/RefreshRateHelper.kt` - Refresh rate detection and caching
- `core/ui/FrameRateOptimizer.kt` - Frame rate-aware modifier
- `core/ui/FrameRateAwareAnimation.kt` - Auto-tuned animation system
- `core/ui/components/RefreshRateMonitor.kt` - Real-time display monitoring

## Interactions

1. **Theme Switching**: Clicking a theme card toggles the theme globally
2. **Glow Color**: Selecting a color updates the UI glow effect instantly
3. **Cache Size**: Slider changes the displayed size; auto-adjusts memory warnings
4. **Telegram Connection**: Button triggers a quick connect dialog
5. **Onboarding**: Progress bar fills as steps are completed
6. **Pin Destinations**: Toggle switches pin visibility

## Responsive Considerations

- **Desktop**: Full-width grid of cards
- **Tablet**: 2-column layout
- **Mobile**: Collapsible cards with bottom sheet navigation

## Accessibility

- High contrast ratios (4.5:1 minimum)
- Keyboard navigable controls
- Screen reader support for all interactive elements
- Sufficient touch targets (48px min)

## Wireframe Sketch (Text-Based)

```
+
| Logo | Settings | Profile | Onboarding | Accounts |
+------+----------+---------+------------+-----------+
|      |          |         |            |           |
|  [ ] |  [+]     |  [+]    |  [+]       |  [+]      |
|      |  Theme   | Glow    |  Connected | Pins      |
|      |          | Color   |            |           |
|      |          |         |            |           |
+------+----------+---------+------------+-----------+
|  Theme Card (expandable)        |  Glow Card (expandable)       |
|  - System                       |  - Cobalt                      |
|  - Dark                         |  - Amber                      |
|  - Light                       |  - Custom                     |
+------------------------------------------------------+
```

## Implementation Notes

- All settings are persisted via `SettingsDataStore.kt`
- Changes are saved to local storage and reflected immediately
- Theme and glow color changes take effect instantly
- Cache size adjustments may trigger background cleanup
- Telegram connection status updates in real-time
- Onboarding tracks completion through state machine

---
*Design Version: 1.0*
*Last Updated: 2026-09-26*
*Project: Telegram Drive Uploader*
