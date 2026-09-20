# Contributing to Telegram Drive Uploader

Thank you for your interest in contributing. This document explains how to set up the project, the engineering standards we follow, and how to submit changes.

## Development Setup

1. **Prerequisites**
   - JDK 17+ (JDK 21 recommended)
   - Android SDK (API 37) and the NDK version specified in `app/build.gradle.kts`
   - A valid Telegram **API ID and API hash** from [my.telegram.org](https://my.telegram.org)

2. **Configure credentials** — never commit them:
   ```bash
   cp .env.example .env
   # fill in TELEGRAM_API_ID and TELEGRAM_API_HASH
   ```

3. **Verify the project**:
   ```bash
   ./scripts/verify-project.sh QUICK
   ```

## Engineering Standards

This project enforces strict engineering rules (see [`AGENTS.md`](AGENTS.md)). The most important ones for contributors:

- **Definition of done**: a change is only "done" when it is runtime-verified. A document or report alone is not a deliverable.
- **One verified change at a time**: finish, test, and merge one fix before starting the next.
- **Reproduce before you fix**: attach a log, screenshot, or failing test that proves the bug.
- **Smallest possible diff**: prefer the minimal change that fixes the confirmed root cause.
- **Mandatory regression test**: every confirmed bug fix ships with a test that fails on the old code and passes on the new.
- **No fake success paths**: never report "uploaded"/"success" from local state alone.
- **Secrets discipline**: never commit keystores, API keys, `.env` values, session data, or built APK/AAB files.

## Before Submitting a Pull Request

Run the full local verification and make sure everything is green:

```bash
./gradlew testDebugUnitTest   # all unit tests must pass
./gradlew lintDebug           # zero lint errors
```

- Keep the change focused; do not include unrelated refactoring.
- Update documentation if behavior or setup changes.
- Link the issue your PR addresses and describe how you verified the change.

## Code Style

- Kotlin with the official code style (`kotlin.code.style=official`).
- Follow the existing module boundaries: `domain` (pure logic), `data` (TDLib/Room/WorkManager), `feature` (Compose UI), `core` (shared utilities), `app` (entry point).

## License

By contributing, you agree that your contributions are licensed under the project's [MIT License](LICENSE).
