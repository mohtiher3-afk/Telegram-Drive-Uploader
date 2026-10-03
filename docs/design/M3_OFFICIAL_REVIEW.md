# Official Material 3 / M3 Expressive Review

## Official Material 3 Version/Direction

This review uses the current official Material 3 guidance available at implementation time. Material 3 Expressive is treated as an evolution of Material 3, not as a replacement system. The app remains Android-native and Compose-based.

## M3 Expressive Guidance

Expressiveness is applied only where it improves hierarchy, comprehension, feedback, or orientation. The app's meaningful emphasis points are the primary upload action, upload progress, destination selection, and first-run onboarding. Decorative shapes, button groups, split buttons, and FABs are not added without a real supported action.

## Color

The existing theme uses Material 3 semantic color roles, including primary, secondary, tertiary, surface containers, outline, and error roles. Dynamic color is available on Android 12 and above with a custom light/dark fallback. The redesign preserves Telegram Drive's warm brand direction and avoids scattering new raw colors through screens.

## Typography

Existing screens use MaterialTheme typography roles. The redesign keeps display/headline/title/body/label hierarchy and avoids introducing a custom font, which protects Arabic rendering and system scaling.

## Shape

The theme already defines a restrained expressive shape hierarchy. Rounded surfaces are kept for meaningful grouping, while not every interactive control receives a decorative shape treatment.

## Elevation

Surface hierarchy and tonal containers are preferred over stacked shadows. Existing cards and surfaces remain only where they group content or communicate state.

## Motion

Existing onboarding and screen transitions use short, bounded Compose motion. No long or blocking animation is introduced. Any future expressive motion must preserve reduced-motion behavior and must not carry essential information alone.

## Components

The implementation uses Compose Material 3 components: Scaffold, TopAppBar, NavigationBar, NavigationRail, Buttons, TextFields, Cards, Lists, Chips, ProgressIndicators, Snackbar, and dialogs where already present. New components are introduced only when the existing user intent clearly matches them.

## Adaptive UI

Compact widths keep NavigationBar. Medium and expanded widths use the existing NavigationRail. The redesign does not introduce a second navigation route graph. Content should use sensible maximum widths on large screens rather than stretching edge-to-edge.

## Accessibility

All primary actions retain visible labels, semantic roles, logical start/end placement, and localized content descriptions. Arabic RTL, English LTR, text scaling, touch-target behavior, contrast, TalkBack, and reduced motion require runtime verification on devices or emulators.

## Android / Compose

Jetpack Compose Material 3 is the implementation source for the Android UI. The project remains on its current compatible toolchain and does not add an unverified Material 3 Expressive dependency or API.

## Design Decisions for This App

The modernization is incremental. First, centralize spacing and content-width decisions, preserve semantic theme roles, and refine the adaptive navigation shell. Then update screen-level hierarchy one screen at a time. Navigation routes, authentication, destination IDs, file URIs, WorkManager scheduling, Room state, and TDLib message delivery are protected behavior and must not change during visual work.

## References

[1]: https://m3.material.io/blog/building-with-m3-expressive "Start building with Material 3 Expressive"
[2]: https://m3.material.io/foundations/design-tokens "Material 3 design tokens"
[3]: https://m3.material.io/components/navigation-rail/overview "Material 3 navigation rail"
[4]: https://developer.android.com/develop/ui/compose/designsystems/material3 "Material 3 in Compose"
