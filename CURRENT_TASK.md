# CURRENT_TASK

Phase 03 — 16 KB Memory Page Size Compliance. Status: COMPLETE — verified at workflow guard level and independently post-merge.

Device: Redmi Note 13 Pro+ 5G (arm64-v8a, Android 16 / SDK 36) — `BUFYHQZXR4BQK7WK`.

## Outcome

- Rebuilt `libtdjni.so` (TDLib v1.8.66, source SHA `022d602…`) + OpenSSL per ABI with
  `-DCMAKE_SHARED_LINKER_FLAGS="-Wl,-z,max-page-size=16384 -Wl,-z,common-page-size=16384"`
  (NDK r26 does not default to 16 KB ELF alignment).
- All three ABIs now have LOAD p_align = 0x4000 (16384): arm64-v8a, armeabi-v7a, x86_64.
- Guard `scripts/check-tdlib-artifacts.sh` gained a portable `check_16kb_alignment`
  (readelf -lW / llvm-objdump -p; rejects < 0x4000).
- Merged via PR #30 (`b02b854`); android-ci matrix + security gate green.
- Evidence: `docs/evidence/PHASE03_EVIDENCE.md`.

## Next

- Roadmap item after Phase 03. Run a device smoke (login + upload) on the rebuilt
  libs if a native-runtime regression check is warranted, or proceed to the next
  critical item per governance (one verified change at a time; confirm with user).