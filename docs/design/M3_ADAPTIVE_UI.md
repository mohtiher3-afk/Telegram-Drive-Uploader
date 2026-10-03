# Material 3 Adaptive UI

## Window Strategy

Compact windows use the existing `NavigationBar`. Windows at 600dp and above use the existing `NavigationRail`. The route graph and destination list remain identical in both contexts.

## Content Width

The navigation shell now constrains the main content to the shared `AppContentWidth.max` token on large screens and centers it without changing compact behavior. This prevents forms and lists from becoming unnecessarily wide while keeping the full available width on phones.

## Screen Adaptation

Home, upload preparation, destination, queue, history, settings, authentication, and onboarding remain single route destinations. Future two-pane treatment must be introduced only where the content and interaction model demonstrate a real benefit; no duplicate screen implementation is permitted.

## Verification Matrix

| Context | Expected behavior | Runtime evidence |
|---|---|---|
| Compact portrait | NavigationBar and readable single-column content | Pending device/emulator test |
| Medium width | NavigationRail and centered bounded content | Pending device/emulator test |
| Expanded/tablet | NavigationRail remains stable and content does not stretch edge-to-edge | Pending device/emulator test |
| Large font/display size | Text wraps without clipping and controls remain usable | Pending runtime test |
| Arabic RTL | Logical start/end placement and readable mixed text | Pending runtime test |
| Dark/light | Semantic roles remain legible | Pending runtime test |
