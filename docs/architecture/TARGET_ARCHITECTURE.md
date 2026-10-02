# Target Architecture

## Principle

The target architecture is an incremental refinement of the current package layout. It does not require creating every directory in the supplied example, and it must not change application behavior during the planning phase.

## Proposed boundaries

```text
com.telegramdrive.uploader/
├── core/
│   ├── di/                 # Hilt modules already present
│   ├── navigation/         # AppNavigation and Screen definitions
│   ├── ui/theme/           # Material 3 theme, colors, typography
│   └── util/               # format and metadata utilities
├── data/
│   ├── local/              # Room database, DAO, entity
│   ├── repository/         # upload repository implementation
│   ├── telegram/client/    # TDLib client implementation
│   ├── telegram/repository/# Telegram repository implementation
│   └── upload/             # manager, engine, reader, policy
├── domain/
│   ├── model/              # upload, Telegram, destination and error models
│   ├── repository/         # repository contracts
│   └── upload/             # upload manager/engine/completion contracts
├── feature/
│   ├── onboarding/
│   ├── home/
│   ├── queue/
│   ├── history/
│   ├── settings/
│   ├── telegram/
│   └── upload/
└── MainActivity.kt / TelegramDriveApp.kt
```

## Boundary rules

Feature screens and ViewModels may depend on domain contracts and UI utilities, but should not call TDLib or Room directly. Data implementations own TDLib, Room, Android file access, and WorkManager integration. Domain models remain free of Android framework types where practical. Hilt modules bind interfaces at the application boundary. Native TDLib bindings and artifact scripts remain protected infrastructure.

## Migration posture

The current layout already expresses most of these boundaries. Proposed moves are therefore `REVIEW` until dependency graphs and characterization tests prove that a move reduces coupling without changing lifecycle, persistence, or upload behavior.
