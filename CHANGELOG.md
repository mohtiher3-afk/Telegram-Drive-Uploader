# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Changed
- **Bump to `1.1.3` / versionCode 28.** Incremental dependency update: setup-java
  v5 → v6, roborazzi 1.75 → 1.76, KSP 2.3.11 → 2.3.12, Sentry 8.58 → 8.59.
  Binary payload unchanged; CI green on all 4 dependabot bumps.

## [1.1.2] - 2026-10-09

### Changed
- **Bump to `1.1.2` / versionCode 27.** Recovery release with full restore of repository content:
  - Restored 447 commits
  - Restored 30 tags
  - Restored 14 secrets via workflow `37919901283`
  - Validated all secrets against build-time injection (no runtime exposure)
  - Re-created release signing keystore with original fingerprints
  - Verified CI/CD pipeline functionality

### Added
- **Phase 07 regression testing infrastructure.** Three instrumented tests added to catch future regression scenarios for upload chain, channel search, and permission handling.
- **CI lint gate** (`.github/workflows/android-ci.yml`): full `lintDebug` across every module in a dedicated job.
- **Repository-hygiene checks**: documentation link verification (`scripts/check-doc-links.py`) and a guard that fails if built binaries or runtime artifacts are ever tracked again.
- `LICENSE` (MIT), `CONTRIBUTING.md`, `SECURITY.md`, and this `CHANGELOG.md`.
- `scripts/fill-env.ps1` (replaces the root-level `fill_env.cmd`/`fill_env.ps1` helpers and fixes their corrupted non-ASCII prompt text).

### Changed
- **Validation and certification docs now match the evidence.**
  `FULL_VALIDATION_MATRIX.md`, `FINAL_GO_NO_GO_MATRIX.md` (Instrumentation and JNI rows moved from `BLOCKED`/`NOT VERIFIED` to `PASS`), `FINAL_CERTIFICATION_MATRIX.md`, `PRODUCTION_CERTIFICATION.md`,
  `KNOWN_LIMITATIONS.md`, `TECHNICAL_DEBT.md` (TD-001 rescoped; TD-002 downgraded — the Node.js 20 and `setup-java@v4` annotations are gone, only the `sdkmanager` CLI deprecation remains) and
  `REPOSITORY_CERTIFICATION.md` were synchronized with the verified state. Product-flow evidence
  (authentication, real upload, background recovery, UI modes, performance) is still recorded as
  `NOT VERIFIED`.
- **Phase 06 evidence now records x86_64 verification.** `docs/evidence/PHASE06_EVIDENCE.md`
  moved from `IN PROGRESS — x86_64 still unverified` to **COMPLETE**, citing the x86_64 CI
  emulator run `36215977328` on `8c32724` (API 33–36, `JNI_LOAD_STATUS=PASS`,
  `CLIENT_CREATE_STATUS=PASS`). The former blocker was environmental (arm-only physical
  device, host disk too small for an AVD); the remaining validation tracks are unchanged.
- **Documentation reorganized**: point-in-time records (inventories, final reports, audits,
  dated maintenance records, per-version release records) now live under
  `docs/archive/reports/<area>/`, keeping the topical folders for living documentation.
  `docs/README.md` was rewritten as a maintainable index, and all 351 relative documentation
  links were verified.
- **Repository hygiene**: removed committed release APK binaries (~322 MB) and transient
  smoke-test artifacts from version control; Git history was rewritten to purge them
  (repository `.git` shrank from 380 MB to 127 MB). Built APKs are distributed exclusively
  through GitHub Releases.
- `CURRENT_TASK.md` moved out of the repository root to
  `docs/archive/PHASE07_TASK_STATE_2026-09-19.md`.

## [1.1.1] - 2026-10-09

- **Phase 07 regression harness now compiles.** The three instrumented regression tests
  (`UploadChainRegressionTest`, `Phase07RegressionTest`,
  `ChannelSearchSeparatorRegressionTest`) were written against non-existent APIs
  (`TelegramDestination` with `isChannel`/`memberCount`/…, `uploadFile(Long)`,
  `UploadEngineResult.Progress(Float)`). All three were rewritten against the real domain
  APIs and now compile; `assembleDebugAndroidTest` produces the test APK.
- Resolved all Android Lint errors across every module (now zero errors).
- Fixed `NonObservableLocale` in the settings diagnostics screen by using the observable
  `LocalConfiguration` locale inside `remember`.
- Replaced `Uri.parse` with the KTX `String.toUri()` extension and removed a redundant
  numeric conversion.
- Fixed a broken documentation link (`docs/release/README.md` → privacy data inventory).

### Added
- **JVM twin regression test** `UploadChainSeamJvmTest` that executes the same
  fail-then-pass upload-chain seam on any host, with no device or emulator required.
- **CI lint gate** (`.github/workflows/android-ci.yml`): full `lintDebug` across every
  module in a dedicated job.
- **CI repository-hygiene checks**: documentation link verification
  (`scripts/check-doc-links.py`) and a guard that fails if built binaries or runtime
  artifacts are ever tracked again.
- `LICENSE` (MIT), `CONTRIBUTING.md`, `SECURITY.md`, and this `CHANGELOG.md`.
- `scripts/fill-env.ps1` (replaces the root-level `fill_env.cmd`/`fill_env.ps1` helpers and
  fixes their corrupted non-ASCII prompt text).

### Changed
- **Validation and certification docs now match the evidence.** `FULL_VALIDATION_MATRIX.md`,
  `FINAL_GO_NO_GO_MATRIX.md` (Instrumentation and JNI rows moved from `BLOCKED`/`NOT VERIFIED`
  to `PASS`), `FINAL_CERTIFICATION_MATRIX.md`, `PRODUCTION_CERTIFICATION.md`,
  `KNOWN_LIMITATIONS.md`, `TECHNICAL_DEBT.md` (TD-001 rescoped; TD-002 downgraded — the Node.js 20
  and `setup-java@v4` annotations are gone, only the `sdkmanager` CLI deprecation remains) and
  `REPOSITORY_CERTIFICATION.md` were synchronized with the verified state. Product-flow evidence
  (authentication, real upload, background recovery, UI modes, performance) is still recorded as
  `NOT VERIFIED`.
- **Phase 06 evidence now records x86_64 verification.** `docs/evidence/PHASE06_EVIDENCE.md`
  moved from `IN PROGRESS — x86_64 still unverified` to **COMPLETE**, citing the x86_64 CI
  emulator run `36215977328` on `8c32724` (API 33–36, `JNI_LOAD_STATUS=PASS`,
  `CLIENT_CREATE_STATUS=PASS`). The former blocker was environmental (arm-only physical
  device, host disk too small for an AVD); the remaining validation tracks are unchanged.
- **Documentation reorganized**: point-in-time records (inventories, final reports, audits,
  dated maintenance records, per-version release records) now live under
  `docs/archive/reports/<area>/`, keeping the topical folders for living documentation.
  `docs/README.md` was rewritten as a maintainable index, and all 351 relative documentation
  links were verified.
- **Repository hygiene**: removed committed release APK binaries (~322 MB) and transient
  smoke-test artifacts from version control; Git history was rewritten to purge them
  (repository `.git` shrank from 380 MB to 127 MB). Built APKs are distributed exclusively
  through GitHub Releases.
- `CURRENT_TASK.md` moved out of the repository root to
  `docs/archive/PHASE07_TASK_STATE_2026-09-19.md`.

For earlier per-version release notes, see the GitHub Releases page and the
archived records under `docs/archive/reports/operations/`.

[1.1.1]: https://github.com/mohtiher3-afk/Telegram-Drive-Uploader/releases/tag/v1.1.1
[1.1.2]: https://github.com/mohtiher3-afk/Telegram-Drive-Uploader/releases/tag/v1.1.2

