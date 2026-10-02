# Navigation Map

The current navigation graph starts at `Screen.Home` and uses the following actual routes:

```text
Home
├── upload_preparation
│   └── telegram_destination
│       └── telegram_auth (when disconnected)
├── telegram_auth
├── Queue
├── History
└── Settings
    └── telegram_auth
```

`AppNavigation.kt` owns the `NavHost`, bottom-tab destinations, and callbacks. Upload preparation returns to Queue after enqueue. Telegram destination selection returns to the upload flow. No new routes are proposed in this planning phase. The onboarding flow exists as a feature package but is not assumed to be a current navigation start destination without verifying the corresponding startup state.
