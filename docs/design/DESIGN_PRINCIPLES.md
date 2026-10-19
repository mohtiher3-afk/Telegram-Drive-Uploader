# Design Principles

The Dusk identity is governed by the "Technical Instrument" philosophy.

## Core Directives
1. **Dot-Matrix Language**: Use the dot-matrix motif (bars, rings, maps) for all progress and status indicators to evoke hardware readouts.
2. **Real Data Only**: Avoid placeholder animations. Every movement must correspond to a real state change or data stream.
3. **Accessibility First**: 
    - All interactive elements maintain a minimum 48dp touch target.
    - Contrast ratios follow WCAG AA standards for all text and iconography.
4. **Reduced Motion**: All non-essential animations are gated by `rememberSystemMotionEnabled()` to respect system-level accessibility settings.
