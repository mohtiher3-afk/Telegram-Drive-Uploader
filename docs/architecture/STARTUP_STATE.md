# Startup State and Splash Decision

## Verified state sources

| Concern | Existing source | State used |
|---|---|---|
| Theme | `SettingsDataStore.themePreference` collected in `MainActivity` | `System`, `Light`, or `Dark` preference with a real initial value of `System` |
| First launch | `SettingsDataStore.onboardingCompleted` collected by `OnboardingViewModel` | `false` means onboarding is shown; `true` opens the existing navigation graph |
| Telegram authentication | Existing Telegram feature ViewModels and repository/client state | Feature-owned, not duplicated in startup |
| TDLib availability | Existing `TelegramClientImpl` and native runtime gate | Feature-owned, fail-closed behavior preserved |
| WorkManager | Existing `TelegramDriveApp` `Configuration.Provider` and Hilt factory | Platform-managed initialization, not eagerly started by the Activity |

## Startup states actually required

The current code requires only a composition-ready state plus the persisted onboarding gate. A separate `Initializing`, `Ready`, `NeedsAuthentication`, or `Error` state model is not justified because no startup coordinator currently performs a multi-step asynchronous initialization sequence. Authentication and TDLib states remain owned by their existing feature and data layers.

## Splash decision

No dedicated `SplashActivity` or post-startup delay is present. No `androidx.core.splashscreen` dependency or explicit splash API usage was found in the current checkout. Adding the dependency and coordinating an exit condition would introduce a new startup contract without a measured initialization requirement, so this phase preserves the platform launch behavior and documents the decision.

A future splash implementation may use the official Android mechanism if startup work becomes genuinely blocking, but it must be introduced as a separate reviewed change with cold/warm/first-launch tests and no fake progress.
