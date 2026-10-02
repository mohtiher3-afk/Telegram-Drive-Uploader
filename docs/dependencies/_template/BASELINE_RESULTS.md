# Dependency Update Baseline Results

Record the exact commit, toolchain, commands, and outputs before changing versions.

| Gate | Command | Result | Evidence |
|---|---|---|---|
| Clean | `./gradlew clean` | PASS / FAIL | `<output or log reference>` |
| Full tests | `./gradlew test` | PASS / FAIL | `<output or log reference>` |
| JVM tests | `./gradlew :app:testDebugUnitTest` | PASS / FAIL | `<output or log reference>` |
| Lint | `./gradlew :app:lint` | PASS / FAIL | `<output or log reference>` |
| Debug build | `./gradlew :app:assembleDebug` | PASS / FAIL | `<output or log reference>` |
| TDLib artifacts | `./scripts/check-tdlib-artifacts.sh` | PASS / FAIL | `<output or log reference>` |

If the baseline fails, stop and record the pre-existing failure before considering any update.
