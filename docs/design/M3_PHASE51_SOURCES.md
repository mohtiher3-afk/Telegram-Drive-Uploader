# Phase 51 Material 3 Sources

This working note records official sources consulted for the component audit.

| Area | Official source | Evidence used |
|---|---|---|
| Buttons | https://m3.material.io/components/buttons/overview | M3 supports elevated, filled, tonal, outlined, and text variants; labels should be concise and sentence case; button size, shape, toggle, icon, and state choices should match intent. |
| Cards | https://m3.material.io/components/cards/overview | Cards contain related elements; use elevated, filled, or outlined variants only when the grouping is meaningful; avoid unnecessary containment. |
| Lists | https://m3.material.io/components/lists/overview | Lists are continuous indexes for finding and acting on items; use consistent slots and logical ordering; expressive segmented/selection styles are available but should be used only where selection semantics exist. |
| Progress indicators | https://m3.material.io/components/progress-indicators/overview | Use linear or circular indicators according to context; determinate progress must represent actual progress; consistent configuration and accessible end-stop/contrast behavior matter. |
| Navigation bar | https://m3.material.io/components/navigation-bar/overview | Navigation bars are for compact and medium widths and should hold 3–5 stable destinations of equal importance. |
| Navigation rail | https://m3.material.io/components/navigation-rail/overview | Rails are for medium/expanded sizes, should remain in a consistent place, and can hold 3–7 destinations with an optional FAB. |
| Text fields | https://m3.material.io/components/text-fields/overview | Filled fields have more emphasis and outlined fields less; labels, state, supporting text, and brief actionable errors should be visible. |
| Dialogs | https://m3.material.io/components/dialogs/overview | Dialogs interrupt for important prompts and should be dedicated to a single task; avoid using them for routine status messages. |

Supporting implementation links are available from each official page to Android Jetpack Compose documentation.

| Search | https://m3.material.io/components/search/overview | Search is for keyword queries; a search bar may include leading search, hint, and trailing clear icons, and should show results/suggestions in a list when applicable. Existing simple filtering fields are retained where they are embedded in a form/list flow. |
| Chips | https://m3.material.io/components/chips/overview | Filter chips are appropriate for filtering content; assist/suggestion/input chips have different semantics and should not be used as decorative badges. |
| Snackbar | https://m3.material.io/components/snackbar/overview | Snackbars show short, non-interruptive updates near the bottom; they may be dismissive or persistent until action. |
| Tooltips | https://m3.material.io/components/tooltips/overview | Plain tooltips add context to icon-only buttons; accessible content descriptions remain required. |
