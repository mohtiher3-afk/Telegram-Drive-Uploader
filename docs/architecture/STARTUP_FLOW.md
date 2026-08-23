# Startup Flow

## Observed sequence

```text
Android process
  ↓
TelegramDriveApp.onCreate()
  ↓
MainActivity.onCreate()
  ├── enableEdgeToEdge()
  ├── collect SettingsDataStore.themePreference
  ├── resolve System/Light/Dark preference
  ├── TelegramDriveTheme
  └── AppNavigation()
        ├── collect SettingsDataStore.onboardingCompleted
        ├── false → OnboardingScreen
        └── true  → unchanged NavHost, start destination `home`
```

`TelegramDriveApp` performs only its existing application-level initialization. `MainActivity` is the startup owner for theme collection and composition. `AppNavigation` owns the first-launch gate and the existing navigation graph. There is no separate `SplashActivity`, fake delay, startup progress percentage, or second navigation graph.

## First launch and returning user

`OnboardingViewModel.completed` reads the persisted `onboardingCompleted` flow from `SettingsDataStore`, with an initial `false` value. The onboarding screen is therefore rendered when completion is not known as true. `OnboardingViewModel.complete()` persists completion through the existing DataStore key. Once completion is true, `AppNavigation` renders the existing graph with `home` as its start destination.

## Authentication and TDLib

The startup shell does not reimplement Telegram authentication or directly create the TDLib client. Telegram state is consumed by the relevant feature ViewModels and repositories after the navigation shell is available. This avoids duplicating the protected `TelegramClientImpl` lifecycle during app startup.

## Decision

No new startup coordinator, splash Activity, fixed delay, fake progress, or additional initialization path is justified by the observed code. The safe startup improvement for this phase is documentation and validation rather than moving business initialization into `MainActivity`.
