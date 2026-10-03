# Current Navigation Graph

```text
AppNavigation
  ├─ onboardingCompleted == false
  │    └─ OnboardingScreen
  │         └─ completion is persisted through OnboardingViewModel/SettingsDataStore
  │
  └─ onboardingCompleted == true
       └─ NavHost(startDestination = home)
            ├─ home
            │    ├─ settings → settings
            │    ├─ connect → telegram_auth
            │    └─ videos selected → upload_preparation
            ├─ upload_preparation
            │    ├─ back → popBackStack
            │    ├─ select destination → telegram_destination
            │    └─ queue added → queue, popUpTo(home)
            ├─ telegram_auth
            │    ├─ back → popBackStack
            │    └─ auth success → popBackStack
            ├─ telegram_destination
            │    ├─ back → popBackStack
            │    ├─ connect → telegram_auth
            │    └─ destination selected → return to caller via popBackStack
            ├─ queue
            ├─ history
            └─ settings
                 └─ connect → telegram_auth
```

## Actual gates

Onboarding is the only pre-`NavHost` gate. There is no separate splash route in the inspected navigation source. The `NavHost` always declares `home` as its start destination after onboarding is complete. Telegram authorization is initiated from screens through callbacks and is not implemented as a separate graph-level authentication gate.

## Bottom navigation

The bottom bar on compact screens and navigation rail on expanded screens contain `home`, `queue`, `history`, and `settings`. They use single-top navigation, start-destination `popUpTo`, saved state, and restored state. Upload preparation, Telegram authentication, and Telegram destination selection are transient routes outside the bottom-navigation set.
