# Design System

The Dusk design system is a high-density, instrument-inspired visual language built on Material 3. It focuses on technical precision and high-contrast legibility for file management.

## Foundation
- **Base**: Material 3 (M3) specification.
- **Palette**: Dusk palette (detailed in `TOKENS.md`).
- **Dynamic Color**: Full integration with Android 12+ dynamic theming. The Dusk palette serves as the fallback scheme when dynamic color is disabled.
- **Typography**:
    - **Doto**: Used for titles, numbers, and percentages to provide a technical, digital-readout feel.
    - **Space Mono**: Used for all body text and metadata for consistent character width and readability.
    - **Arabic Fallback**: System-native Arabic font for RTL support.
- **Shapes & Spacing**: Defined by consistent radius and spacing tokens (see `TOKENS.md`).

## Component Catalog

### Transfer Instruments
- **DotProgressBar**: `(progress: Float, dotCount: Int = 26)`
  Compact dot-matrix bar used in list rows and notifications.
- **DotProgressRing**: `(progress: Float, size: Dp = 224.dp, dotCount: Int = 72)`
  Large circular readout with a central Doto percentage.

### Layout & Containers
- **AppCard**: `(borderColor, containerColor, content)`
  Raised surface with a hairline border for all content blocks.
- **AppNavPill**: `(items: List<NavPillItem>, selectedIndex: Int, onSelect: (Int) -> Unit)`
  Floating navigation element for primary screen switching.
- **SectionHeader**: `(number: String, label: String, title: String, subtitle: String?)`
  Numbered technical header with an accent dot.

### Controls & Indicators
- **AppSearchField**: `(query: String, onQueryChange: (String) -> Unit, placeholder: String)`
  Clean, outlined search input with a clear-action button.
- **UploadStatusIndicator**: `(video: UploadTask, onPauseClick, onResumeClick, onRetryClick, onCancelClick)`
  Dynamic status block with automated color-coding and a detailed Action Log.
- **PartsMap**: `(totalParts: Int, sentParts: Int, failedPartIndex: Int?, onFailedPartClick)`
  36-cell grid representing the upload chunks of a file.
