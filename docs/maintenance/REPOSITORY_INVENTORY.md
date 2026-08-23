# Repository Inventory

**Status: CURRENT — generated for the final repository cleanup phase.**

## Root-level inventory

| Entry | Classification | Purpose |
|---|---|---|
| `app/` | Required | The single Android application module, including Kotlin/Compose source, tests, resources, generated TDLib Java bindings, and ABI-native libraries. |
| `build.gradle.kts` | Configuration | Root Gradle plugin declarations. |
| `settings.gradle.kts` | Configuration | Plugin repositories, dependency repositories, project name, and `:app` inclusion. |
| `gradle/` | Required | Gradle Wrapper JAR and properties. |
| `gradlew`, `gradlew.bat` | Required | Reproducible Gradle entry points for Unix-like systems and Windows. |
| `gradle.properties` | Configuration | Gradle, Android, Kotlin, and build-cache settings. |
| `scripts/` | Required maintenance tooling | Native dependency preparation, artifact/resource/security/manifest checks, device smoke-test helpers, and master verification. |
| `docs/` | Documentation | Architecture, design, localization, testing, performance, security, CI, release, operations, maintenance, and audit records. |
| `.github/` | Repository automation | CI/release/device workflows, pull-request template, and issue templates. |
| `README.md` | Documentation | Public project overview, requirements, setup, and limitations. |
| `.env.example` | Configuration template | Obvious non-secret configuration placeholders. |
| `.gitignore` | Git configuration | Excludes local build outputs, IDE state, native build output, signing material, and local configuration. |
| `todo.md` | Maintenance ledger | Historical and current task tracking. |
| `design/` | Reference assets | Application icon concept and multi-device UI preview. |
| `.native-build/`, `build/`, `.gradle/`, `debug.keystore` | Local/ignored output | Present in the working environment but ignored and not tracked. |

No `buildSrc/`, convention-plugin module, separate native source tree, or separate test module exists in the audited tree. Native TDLib artifacts are stored under `app/src/main/jniLibs/` and generated Java bindings under `app/src/main/java/org/drinkless/tdlib/`.

## Application structure

The app contains `main`, `test`, and `androidTest` source sets. The main resources include drawable, density-specific launcher resources, `values`, `values-ar`, and XML backup configuration. The supported native ABI directories are `arm64-v8a`, `armeabi-v7a`, and `x86_64`.

## Configuration and integration boundaries

Gradle is the build configuration source; `.env` and release-signing environment variables are external inputs and are not committed. Room and DataStore are application persistence boundaries. WorkManager handles background queue execution. TDLib is isolated under the Telegram integration boundary and is validated by `scripts/check-tdlib-artifacts.sh`. CI configuration is under `.github/workflows/`.

## Inventory conclusion

The tree has one Android module and one coherent scripts directory. The apparent duplicate `FINAL_*` documents are historical or area-specific reports rather than filename-only deletion candidates. No deletion or move is justified by this inventory alone.


## Licensing

No root-level `LICENSE` file was found in the audited repository. **LICENSE STATUS NOT DEFINED.** This phase does not choose or invent a license.
