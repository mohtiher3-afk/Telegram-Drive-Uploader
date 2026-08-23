# Technical Debt Register

Only confirmed debt is listed.

| ID | Area | Description | Risk | Priority | Recommendation |
|---|---|---|---|---|---|
| TD-001 | Runtime validation | Real-device evidence for Telegram authentication, upload, background recovery, and critical UI modes is incomplete | Production regressions may remain undetected | P1 | Maintain a reproducible emulator/device smoke lane |
| TD-002 | CI maintenance | GitHub Actions reports Node.js 20 and `setup-java@v4` deprecation warnings | Future runner changes may break workflows | P2 | Upgrade actions in a dedicated, tested CI change |
| TD-003 | Test coverage | Broad ViewModel, Room, DataStore, worker recovery, and Compose UI coverage remains incomplete | Regressions may be detected late | P1 | Add focused tests before changing those paths |
| TD-004 | Build tooling | Local builds depend on the committed wrapper and a provisioned Android SDK/NDK toolchain | New contributors may lack a ready environment | P2 | Keep onboarding and CI toolchain versions synchronized |
| TD-005 | Performance evidence | Startup, memory, battery, and real upload throughput lack repeatable device baselines | Optimization decisions cannot be measured reliably | P2 | Establish controlled profiler baselines before optimization |
