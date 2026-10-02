# Official Material 3 Research Notes

## Sources

- Material 3 Expressive: https://m3.material.io/blog/building-with-m3-expressive
- Material 3 design tokens: https://m3.material.io/foundations/design-tokens
- Material 3 navigation rail: https://m3.material.io/components/navigation-rail/overview
- Material 3 in Compose: https://developer.android.com/develop/ui/compose/designsystems/material3

## Findings

Material 3 Expressive is an evolution of Material 3 rather than a new version. Its guidance emphasizes hierarchy, useful interaction, flexible components, expressive but accessible color, emphasized typography, shape variety, and natural motion. It cautions against applying expressive decoration everywhere or weakening the prominence of essential actions.

Material design tokens provide named, reusable decisions for reference, system, and component layers. They are intended to replace scattered hard-coded values and support contextual changes such as dark theme, device form factor, density, and RTL.

The current official navigation-rail guidance places rails on medium and larger window sizes and supports three to seven destinations. The May 2025 Expressive update describes collapsed and expanded rails and favors an expanded rail over a navigation drawer for larger contexts. This project should not introduce a second navigation architecture without a demonstrated large-screen need.

Jetpack Compose Material 3 implements color schemes, typography, shapes, components, accessibility, and large-screen guidance. Dynamic color is available on Android 12 and above with custom light/dark fallback schemes. M3 color roles should be paired with their `on-*` roles, and accessibility must be verified rather than inferred.

## Design Boundary

The redesign must preserve existing routes, TDLib, upload semantics, authentication, persistence, Arabic RTL support, and existing user workflows. Expressiveness should be used only to clarify upload progress, destination selection, onboarding hierarchy, and primary actions.
