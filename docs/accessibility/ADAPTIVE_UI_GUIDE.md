# Adaptive UI Guide

## Layout Strategy

The current application uses Compose layouts with `fillMaxSize`, `fillMaxWidth`, weighted spacers, lazy lists, wrapping columns, and horizontal scrolling for queue filters. This provides a reasonable adaptive foundation without a second tablet-specific navigation system.

## Small Screens

Onboarding uses navigation-bar insets and full-width actions. Queue content uses a lazy column and horizontal scrolling filter chips. Destination content contains a horizontally scrollable action/filter region. Small-screen verification must check that long Arabic labels, filename rows, error text, and action buttons do not clip or become unreachable.

## Large Screens and Tablets

No dedicated tablet pane, window-size-class navigation, or desktop keyboard contract was found. The app should therefore be described as responsive through Compose sizing rather than claiming tablet optimization. Test large widths for excessive whitespace, list readability, and sensible action placement before adding adaptive branches.

## Landscape

No landscape-specific layout or orientation lock was identified in this audit. Test onboarding, authentication, destination search, upload preparation, queue, history, and settings in landscape. Record overflow or inaccessible actions before any change.

## Font and Display Scaling

Do not disable system font scaling. Evaluate normal, large, and very large font sizes, plus small/default/large display sizes. Check top bars, dialogs, file names, chat names, upload speed/ETA, errors, and settings. Prefer wrapping or scrolling to fixed heights when evidence shows clipping.

## Themes and Direction

The Activity applies a user/system theme preference through Material 3 color roles. Both light and dark modes need contrast review. Arabic RTL is enabled by the manifest and resource support exists. Do not mirror every icon; only directional icons should follow layout direction where the asset requires it.

## Keyboard and Focus

Keyboard navigation is applicable mainly to authentication, search, settings, and dialogs. The current app is Android-first and does not claim desktop support. Test Tab/Shift+Tab only where a physical or accessibility keyboard is available, and avoid custom focus order unless a real ordering defect is observed.
