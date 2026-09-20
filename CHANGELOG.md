# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Changed
- Repository hygiene: removed committed release APK binaries (~322 MB) from version
  control. Built APKs are now distributed exclusively through GitHub Releases.
- Moved transient smoke-test runtime artifacts (`artifacts/`) out of version control;
  permanent evidence remains documented under `docs/evidence/`.

### Fixed
- Resolved all Android Lint errors across every module (now zero errors).
- Fixed `NonObservableLocale` in the settings diagnostics screen by using the
  observable `LocalConfiguration` locale inside `remember`.
- Replaced `Uri.parse` with the KTX `String.toUri()` extension and removed a
  redundant numeric conversion.

### Added
- `LICENSE` (MIT), `CONTRIBUTING.md`, `SECURITY.md`, and this `CHANGELOG.md`.

For earlier per-version release notes, see the GitHub Releases page and the
archived records under `docs/operations/` and `docs/release/`.
