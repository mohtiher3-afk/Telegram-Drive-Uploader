# Test Results

## Local results

`./scripts/check-resource-integrity.sh` passed. `./scripts/check-workmanager-manifest.sh` passed. The TDLib artifact checker located all three ABI native libraries and Java bindings but exited with `TDLIB_ARTIFACTS_PRESENT=false` because exact ELF validation requires `readelf`, unavailable in the temporary sandbox. This is an environment/tooling limitation, not evidence of missing native files.

Gradle commands were not executed locally because the checkout has no Gradle wrapper and the sandbox has no standalone Gradle executable. No local compile, unit-test, lint, APK, or connected-device result is fabricated.

## CI and device results

The GitHub Actions Multi-ABI workflow is the authoritative build/test source. At the time of this report, the resource-phase run had not reached a final conclusion. The Android instrumentation smoke test also requires an available emulator/device runner. Final results must be appended from the corresponding GitHub Actions logs and must list passed, failed, skipped, environment, device, and TDLib-artifact classifications separately.
