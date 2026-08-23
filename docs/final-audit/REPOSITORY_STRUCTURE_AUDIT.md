# Repository Structure Audit

The repository contains `app/` Android source, `app/src/main/java` production code, `app/src/test` JVM tests, `app/src/androidTest` instrumentation tests, `app/src/main/res` resources, `scripts/` verification/native-support tools, `.github/workflows/` CI/release workflows, and layered documentation under `docs/architecture`, `docs/design`, `docs/localization`, `docs/resources`, `docs/testing`, `docs/performance`, `docs/security`, `docs/ci`, and `docs/final-audit`.

No APK, AAB, keystore, PEM, class, temporary, or build-output artifact was found in the tracked working tree. No duplicate production package was proven from the repository inventory. A Gradle wrapper is absent; this is documented as a reproducibility limitation rather than hidden.
