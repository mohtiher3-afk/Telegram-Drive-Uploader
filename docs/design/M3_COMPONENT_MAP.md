# Material 3 Component Map

| App Need | Material 3 Component | Screen | Reason |
|---|---|---|---|
| Primary upload | Filled `Button` | Home, Upload preparation | Clear dominant action without inventing a new action. |
| Destination search | `OutlinedTextField` with search affordance | Destination | Supports text refinement and clear action. |
| Destination selection | List row with selected container and trailing action | Destination | Preserves stable destination identity and scanability. |
| File selection | System picker plus list rows | Home, Upload preparation | Uses Android file selection semantics and visible metadata. |
| Upload progress | Linear progress indicator and textual status | Queue | Communicates actual progress and state without relying only on color. |
| Queue controls | Text/outlined actions | Queue | Keeps retry, pause, resume, and cancel distinct by emphasis. |
| Authentication | `OutlinedTextField`, `Button`, `OutlinedButton` | Authentication | Matches input and recovery intent. |
| Error feedback | `Snackbar` for transient authentication feedback | Authentication | Existing short-lived feedback surface; raw messages are sanitized. |
| History filtering | Search field, buttons, and list | History | Refines an existing history projection without altering data rules. |
| Settings choices | `RadioButton`, `Switch` where the setting is boolean, and `ListItem` grouping | Settings | Matches actual selection semantics. |
| App navigation | `NavigationBar` compact, `NavigationRail` medium/expanded | App shell | Adapts presentation while preserving routes. |
| Onboarding | Cards, buttons, progress, and permission controls | Onboarding | Supports first-run hierarchy and actual Android permission actions. |

No split button, FAB menu, toolbar, or third-party UI library was added because the current product does not expose an additional related action that requires it.
