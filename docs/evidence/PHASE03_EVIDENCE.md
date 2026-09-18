# Phase 03 Evidence — 16 KB Memory Page Size Compliance

Status: COMPLETE — verified at workflow level (guard) and independently via ELF parsing of the merged artifacts.

## Context

Google Play requires apps targeting Android 15 (API 35+) to support 16 KB memory
page sizes on 64-bit devices. Native ELF libraries must have LOAD segments
aligned to at least `0x4000` (16384). This repo ships a source-built TDLib
(`libtdjni.so`) plus OpenSSL shared libs per ABI under
`data/src/main/jniLibs/<abi>/`.

## Before (2026-09-18)

| ABI       | ELF  | LOAD p_align        | Verdict    |
|-----------|------|---------------------|------------|
| arm64-v8a | ELF64| [4096, 4096, 4096]  | UNALIGNED  |
| armeabi-v7a | ELF32 | [4096, 4096, 4096]| UNALIGNED  |
| x86_64    | ELF64| [16384, 16384, 16384] | COMPLIANT |

Verified with a pure-Python ELF parser (LOAD p_type==1, p_align at program-header
offset 0x30 for ELF64 / 28 for ELF32).

## Root cause & fix

- Build scripts used NDK `26.3.11579264` (r26). NDK r27 and older do NOT default
  to 16 KB ELF alignment for CMake builds.
- Fix (`6af0519`): added explicit linker flags in `scripts/build-tdlib-android.sh`:
  `-DCMAKE_SHARED_LINKER_FLAGS="-Wl,-z,max-page-size=16384 -Wl,-z,common-page-size=16384"`
- Hardened the artifact guard `scripts/check-tdlib-artifacts.sh` with a portable
  `check_16kb_alignment` function (reads `readelf -lW` or `llvm-objdump -p` LOAD
  Align values, rejects values below 0x4000, bumping MISSING_COUNT).
  Unit-tested with 6 fixtures (readelf pass/fail, llvm pass/fail ×2, bad token).

## Rebuild & verification (workflow run 35335248238)

- Dispatched `tdlib-upgrade.yml` at ref `main`, building TDLib from the pinned
  upstream SHA `022d60202e446ad1287b9fb68e687c8a0760788b` (v1.8.66 — no version
  change, only a same-version native rebuild with the new flags).
- OpenSSL built per ABI → success. TDLib JNI build + Java bindings → success.
- Guard output (workflow log, "Verify artifacts" step):

```
[16KB CHECK] arm64-v8a: LOAD segments aligned (aligns: 0x4000 0x4000 0x4000)
[16KB CHECK] armeabi-v7a: LOAD segments aligned (aligns: 0x4000 0x4000 0x4000)
[16KB CHECK] x86_64: LOAD segments aligned (aligns: 0x4000 0x4000 0x4000)
STATUS: TDLIB_ARTIFACTS_PRESENT=true
```

- PR auto-creation step failed on the repository's GitHub Actions "create pull
  request" permission; branch `tdlib/v1.8.66` was already pushed, so the PR was
  created manually ([#30]).

## Independent post-merge verification (artifacts from PR branch)

Same-blob download (`git hash-object` matched the Git tree SHAs
`74eaa70…`, `9c5f77…`, `cb70773…`) parsed with an independent ELF routine:

| ABI       | ELF  | LOAD p_align        | Verdict    |
|-----------|------|---------------------|------------|
| arm64-v8a | ELF64| [16384, 16384, 16384] | COMPLIANT ✅ |
| armeabi-v7a | ELF32 | [16384, 16384, 16384] | COMPLIANT ✅ |
| x86_64    | ELF64| [16384, 16384, 16384] | COMPLIANT ✅ |

## Merge & CI

- PR #30 merged via squash (`b02b854`, "chore: rebuild TDLib v1.8.66 with 16 KB ELF
  page alignment (Phase 03)"); PR branch deleted.
- Post-PR CI (`android-ci.yml` matrix, "Test and build Android …" for
  arm64-v8a/armeabi-v7a/x86_64) and "Repository security gate" all passed.

## Acceptance criteria

1. All three ABIs (`arm64-v8a`, `armeabi-v7a`, `x86_64`) individually verified:
   LOAD segments aligned to 0x4000 (16 KB) on the merged artifacts.
2. Exact build command/flag documented in `scripts/build-tdlib-android.sh`
   (CMake `CMAKE_SHARED_LINKER_FLAGS` with the `-Wl,-z,max-page-size=16384`
   / `-Wl,-z,common-page-size=16384` pair).
3. No binary patching performed; compliance achieved purely via rebuild from
   source with the documented flags.