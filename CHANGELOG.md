# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Fixed
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

