# Post-Release Backlog

These items are not silently treated as release blockers without final CI/device evidence:

- Add broad ViewModel, repository, Room, DataStore, WorkManager, queue, and Compose UI test suites.
- Obtain clean CI evidence for all ABI builds, lint, unit tests, and TDLib artifact checks.
- Run physical-device/emulator validation for authentication, real upload, pause/resume/cancel/retry, background recovery, RTL, dark mode, accessibility, backup/restore, and process death.
- Add a Gradle wrapper after compatibility review, or document a supported wrapper bootstrap process.
- Run controlled dependency and historical secret scanning in CI.
- Capture measured startup, memory, CPU, battery, and upload-throughput baselines before any optimization.
