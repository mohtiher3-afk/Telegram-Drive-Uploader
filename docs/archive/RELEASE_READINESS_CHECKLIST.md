# Release Readiness Checklist (Sprint 34, 2026-09-10)

- [x] BUILD — PASS (`clean test assembleDebug`, FULL green)
- [x] TESTS — PASS (105/105 unit; 2 androidTests compile, need device to execute)
- [x] TDLIB — PASS (v1.8.66 bindings + prebuilt `.so` all 3 ABIs verified; cloud smoke 4/4)
- [x] JNI — PASS (absolute-path load with fallback, errors rethrown; fake shim deleted)
- [ ] AUTHENTICATION — NOT TESTED (needs real account on device)
- [ ] CHAT DISCOVERY — NOT TESTED (needs real account on device)
- [x] UPLOAD — PASS static (real PreliminaryUploadFile → SendMessage → gated confirm)
- [ ] TELEGRAM CONFIRMATION — NOT TESTED (needs device; see `SPRINT_32_E2E_CHECKLIST.md`)
- [x] WORKMANAGER — PASS static (unique work, exp backoff, bounded retry, foreground)
- [x] ROOM — PASS (v6 + MIGRATION_5_6 tested; fail-closed completion)
- [x] SECURITY — PASS (no hardcoded secrets, no secret logging, providers not exported)
- [x] PERFORMANCE — PASS static (streaming, 1 Hz coalescing, no full-RAM reads found)
- [x] MEMORY — PASS static (retriever/codec releases, 1 MB chunks, owned snapshots)
- [x] BATTERY — PASS static (foreground dataSync + notification + cancel; no wake locks)
- [x] NOTIFICATIONS — PASS static (single channel, stable IDs, success only on confirm)
- [ ] ACCESSIBILITY — NOT TESTED
- [ ] RTL — NOT TESTED (supportsRtl=true declared)
- [ ] DARK MODE — NOT TESTED
- [ ] REAL DEVICE QA — NOT TESTED (no local ADB/AVD; manual procedure documented)

Release build: `assembleRelease` green locally (unsigned, 49.5 MB, R8 keeps for
`org.drinkless.tdlib.**` present, native `.so` per ABI correct — arm64 statically
linked, others shared). Signed release via CI `RELEASE_KEYSTORE_*` secrets.
