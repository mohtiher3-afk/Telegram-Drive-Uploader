# Technical Debt Register

Only confirmed debt is listed.

| ID | Area | Description | Risk | Priority | Recommendation |
|---|---|---|---|---|---|
| TD-001 | Runtime validation | Native loading (JNI and `Client.create()`) is verified for all three shipped ABIs, but real-device evidence for Telegram authentication, upload, background recovery, and critical UI modes is still incomplete | Production regressions may remain undetected | P1 | The reproducible emulator smoke lane now exists (`.github/workflows/android-device-smoke.yml`, re-triggered whenever its artifact contract changes); the remaining gap needs a controlled account, destination and device |
| TD-002 | CI maintenance | The Node.js 20 and `setup-java@v4` annotations are resolved (workflows now use `checkout@v7`, `setup-java@v5/v6`, `upload-artifact@v7`, `setup-gradle@v6`, `download-artifact@v8`); the 2026-09-26 runs emit only the `sdkmanager` CLI deprecation warning | Future runner changes may break workflows | P3 | Track the `sdkmanager` deprecation and keep action versions current in dedicated, tested CI changes |
| TD-003 | Test coverage | Broad ViewModel, Room, DataStore, worker recovery, and Compose UI coverage remains incomplete | Regressions may be detected late | P1 | Add focused tests before changing those paths |
| TD-004 | Build tooling | Local builds depend on the committed wrapper and a provisioned Android SDK/NDK toolchain | New contributors may lack a ready environment | P2 | Keep onboarding and CI toolchain versions synchronized |
| TD-005 | Performance evidence | Startup, memory, battery, and real upload throughput lack repeatable device baselines | Optimization decisions cannot be measured reliably | P2 | Establish controlled profiler baselines before optimization |
