# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Fixed
- **TDLib smoke lane no longer freezes on a stale failure.** The `Android TDLib Device`
  `Smoke Test` workflow now re-runs when the artifact gate it depends on changes
  (`scripts/check-tdlib-artifacts.sh`, `scripts/check-elf-alignment.py`,
  `scripts/build-tdlib-android.sh`, `docs/TDLIB_SHA256SUMS.txt`,
  `docs/TDLIB_ARTIFACT_MANIFEST.md`), and its JNI-library filter covers every ABI
  instead of `x86_64` only. The checksum-gate fix in `7eeebef` matched no trigger
  path, so the lane stayed red on its pre-fix run `36028821677` (`3536441`) even
  though the gate passes; the lane now re-verifies itself on the fix.
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

