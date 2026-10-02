# Design System Map

| Token area | Current source | Planning observation |
|---|---|---|
| Theme | `core/ui/theme/Theme.kt` and `themes.xml` | Preserve light/dark behavior before any redesign |
| Colors | `core/ui/theme/Color.kt`, resource colors | Consolidate only after contrast and Arabic review |
| Typography | `core/ui/theme/Type.kt` | Keep readable scaling and RTL compatibility |
| Components | Compose feature screens and reusable UI components | Identify repeated buttons/cards before merging |
| Icons | Material icons and launcher assets | Do not replace application identity during refactor |
| Animation | Onboarding and Compose motion usage | Measure recomposition and honor reduced motion |
| Spacing/shapes | Material 3 theme and screen modifiers | Extract tokens only where duplication is confirmed |
| Localization | `values/strings.xml`, `values-ar/strings.xml` | Every new string must remain resource-backed |

No visual redesign is proposed here. The first safe UI step is a component inventory and screenshot/UI test baseline in both Arabic RTL and English LTR.
