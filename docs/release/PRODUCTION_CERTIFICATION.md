# Production Certification

## Application

Telegram Drive Uploader, application ID `com.telegramdrive.uploader`.

## Version

`1.0.15`, versionCode `15`, release tag `v1.0.15`.

## Release Commit

Candidate source commit: `30ac9c902984ca247e2f97e45f95ca890c21e59c`. Documentation-only follow-up commit on `main`: `1a938d9`.

## Build Environment

JDK 17, Gradle 8.9, AGP 8.7.3, Kotlin 2.2.10, compile/target SDK 36, Build Tools 36.0.0, NDK 26.3.11579264.

## TDLib

Official TDLib integration and matching native artifacts were validated by the release workflow for the supported ABIs. Device runtime fields such as JNI load, `Client.create()`, authorization, and real upload remain `NOT VERIFIED` in this handoff.

## Security

Repository security, resource, manifest, backup, signing, and sensitive-logging reviews passed for the documented scope. No signing values or private keys are stored in source.

## Testing

JVM unit tests and release lint passed in the signed release workflow. Device/emulator authentication, upload, background recovery, and accessibility tests are `NOT VERIFIED`.

## Performance

No unsupported performance claims are made. Device startup, memory, battery, and real upload throughput baselines remain `NOT VERIFIED`.

## CI/CD

The signed multi-ABI release workflow completed successfully for v1.0.15. The primary CI and release workflows remain subject to future action-maintenance warnings documented in technical debt.

## Signing

APK signatures were verified by the release workflow using repository secrets. Secret values were not exposed.

## APK

Signed APKs for `arm64-v8a`, `armeabi-v7a`, and `x86_64` are published in GitHub Release `v1.0.15`; checksums are recorded in `RELEASE_ARTIFACTS.md`.

## AAB

`NOT APPLICABLE` for the current release workflow, which publishes per-ABI APKs only.

## Known Limitations

Real Telegram login, channel permission behavior, upload delivery, process-death recovery, device-specific background execution, and runtime RTL/accessibility evidence remain incomplete.

## Rollback

Use [ROLLBACK_PLAN.md](../operations/ROLLBACK_PLAN.md) and the previous known-good GitHub Release/tag. Do not downgrade or destroy databases without migration analysis.

## Final Verdict

**NOT CERTIFIED** for unrestricted production handoff because runtime/device evidence remains incomplete. The signed v1.0.15 artifacts are published, but certification does not imply that every user flow has been proven on a device.
