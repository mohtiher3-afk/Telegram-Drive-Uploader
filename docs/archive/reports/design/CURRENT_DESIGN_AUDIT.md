# Current Material 3 Design Audit

| Element | Current implementation | Finding | Target |
|---|---|---|---|
| Theme | `TelegramDriveTheme` with light, dark, and Android 12+ dynamic schemes | Material 3 is already in use; dynamic color currently defaults to enabled | Preserve brand palette as fallback and document dynamic-color behavior |
| Colors | `Color.kt` contains Bento palette, dark surfaces, and upload status colors; `Theme.kt` defines semantic light/dark schemes | Some legacy brand tokens and diagnostic colors coexist; a few settings diagnostics use raw colors | Keep semantic theme roles as the primary surface and avoid adding new raw colors |
| Typography | `Type.kt` overrides the most-used styles and relies on Material defaults for the rest | The complete scale is not explicitly documented or customized | Define a coherent complete scale while preserving existing prominent sizes |
| Shapes | `Theme.kt` provides `ExpressiveShapes` from 8dp to 38dp | Shape hierarchy already exists and is consistent | Preserve it as the shared shape source |
| Spacing | Screens use direct `dp` values | No shared spacing token file was found | Add a small semantic spacing token set for new/shared UI only; do not rewrite every screen |
| Buttons/cards | Compose Material 3 components are used across feature screens | Existing components are functional; broad wrapper creation is unnecessary | Standardize through MaterialTheme and only add domain-specific components when reused |
| Progress | Upload progress is rendered by existing feature UI | Real progress behavior must remain untouched | Use theme colors and existing progress semantics |
| Dark mode | `darkColorScheme` and system dark-mode detection are present | Dark mode is explicit and not a simple inversion | Preserve and document the current hierarchy |
| RTL | Arabic resources and Compose UI exist, but layout audit is required | Future changes must use start/end-aware APIs | Add RTL guidance and avoid changing screen layout in this phase |
| Splash/resources | Manifest references `Theme.MyApplication`; no design-system redesign was made | Splash and app branding are outside this token-only slice | Preserve manifest and splash behavior |

## Scope result

The repository already has a Material 3 foundation. The safe improvement is to make shared typography and spacing tokens explicit and document color, shape, dark-mode, dynamic-color, and RTL decisions. No screen-wide visual rewrite is justified in the design-system foundation phase.
