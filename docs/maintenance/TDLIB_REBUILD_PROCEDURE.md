# TDLib rebuild procedure

**This document is not executed.** It is the exact procedure to rebuild all three
`libtdjni.so` ABIs and `TdApi.java` from the pinned commit, using the repository's own
workflow. It is written so it can be followed literally by whoever has the NDK
installed.

Background and evidence: `docs/maintenance/TDLIB_VERSION_DECISION.md` (why
`022d6020…` is the right pin) and `docs/maintenance/TDLIB_VERSION_CHECK.md` (the
current state: arm64-v8a and x86_64 are 1.8.67 from `td-master`, armeabi-v7a and
`TdApi.java` are 1.8.66).

**Nothing here has been verified at runtime on a device or emulator.** Steps 6 and 7
below are the runtime checks that must pass before this counts as done.

---

## 0. Prerequisites

- Android NDK `26.3.11579264` (NDK r26d) — **not installed on the current host**, and
  the reason this procedure has not been run.
- CMake `3.22.1` + `ninja`.
- Host tools: `gperf`, `php-cli`, `ninja-build`, `wget`, `unzip`.
- JDK 17 (Temurin).
- Push access to the repository (the workflow pushes a branch and opens a PR).

Both NDK and CMake versions are pinned by the workflow itself, so it installs them —
you only need them for a local run.

## 1. Trigger the workflow

`.github/workflows/tdlib-upgrade.yml` is `workflow_dispatch`-only. Run it from
**Actions → tdlib-upgrade → Run workflow** with:

| Input | Value |
|---|---|
| `ref` | `022d60202e446ad1287b9fb68e687c8a0760788b` |
| `expected_version` | `1.8.66` |

Both are now the workflow defaults (commit `08d82d1`), so accepting the defaults is
correct. `ref` **must** be a full 40-char commit SHA — the workflow now asserts this
and fails fast otherwise, because a branch desynchronises the native libraries from
`TdApi.java`.

`TARGET_ABIS` is `arm64-v8a armeabi-v7a x86_64` (workflow-level `env`), and that
single value drives OpenSSL, the TDLib cross-compile, the 16 KB gate and the artifact
check — so all three ABIs necessarily come from the same commit.

## 2. What the workflow does

1. Installs NDK `26.3.11579264` + CMake `3.22.1`, JDK 17, host tools.
2. `FORCE_ALL_ABIS=1 ./scripts/build-openssl-android.sh` — OpenSSL 3.0.16 per ABI.
3. Asserts `ref` is a 40-char SHA.
4. `TDLIB_REF=<ref> TDLIB_VERSION=1.8.66 ./scripts/build-tdlib-android.sh`, which:
   - fetches the source at that exact commit and derives the version from upstream
     `CMakeLists.txt`, **aborting if it disagrees with `1.8.66`**;
   - generates the Java bindings (`tl_generate_java` target) — `TdApi.java` and
     `Client.java` are **generated, never hand-edited**;
   - cross-compiles `libtdjni.so` per ABI with
     `-Wl,-z,max-page-size=16384 -Wl,-z,common-page-size=16384`;
   - regenerates `docs/TDLIB_SHA256SUMS.txt`.
5. **16 KB gate** — fails the job if any ABI has a PT_LOAD segment below `0x4000`.
6. `TDLIB_CHECK_ABI=all ./scripts/check-tdlib-artifacts.sh` — checksums, `e_machine`
   per ABI directory, 16 KB alignment.
7. Commits the artifacts and opens a PR.

## 3. Artifacts produced, and where each must land

| Artifact | Must be at |
|---|---|
| arm64-v8a `libtdjni.so` | `data/src/main/jniLibs/arm64-v8a/libtdjni.so` |
| armeabi-v7a `libtdjni.so` | `data/src/main/jniLibs/armeabi-v7a/libtdjni.so` |
| x86_64 `libtdjni.so` | `data/src/main/jniLibs/x86_64/libtdjni.so` |
| `TdApi.java` | `data/src/main/java/org/drinkless/tdlib/TdApi.java` |
| `Client.java` | `data/src/main/java/org/drinkless/tdlib/Client.java` |
| per-ABI OpenSSL | `data/src/main/jniLibs/<abi>/{libssl.so,libcrypto.so}` |
| checksums | `docs/TDLIB_SHA256SUMS.txt` |
| manifest | `docs/TDLIB_ARTIFACT_MANIFEST.md` |

The workflow's own `git add` already covers exactly these paths, so a green run
produces a correctly-placed diff. **Verify placement anyway** — the defect this whole
sequence exists to fix was binaries in the wrong ABI directories.

## 4. Checks to run after the build

Run these on the PR branch, locally, before merging.

```bash
# 4.1 Architecture: each directory must hold its own machine type
for abi in arm64-v8a armeabi-v7a x86_64; do
  printf '%-14s %s\n' "$abi" "$(file data/src/main/jniLibs/$abi/libtdjni.so | sed 's|.*: ||')"
done
# expect: aarch64 / ARM, EABI5 / x86-64   (NOT the same arch twice)

# 4.2 16 KB alignment: every PT_LOAD must be >= 0x4000
for abi in arm64-v8a armeabi-v7a x86_64; do
  printf '%-14s %s\n' "$abi" "$(python scripts/check-elf-alignment.py data/src/main/jniLibs/$abi/libtdjni.so | tr '\n' ' ')"
done

# 4.3 Version: all three binaries must now report 1.8.66 and embed the pinned commit
for abi in arm64-v8a armeabi-v7a x86_64; do
  f=data/src/main/jniLibs/$abi/libtdjni.so
  printf '%-14s ver=%s commit_hits=%s\n' "$abi" \
    "$(grep -aoh '1\.8\.[0-9]\+' "$f" | head -1)" \
    "$(grep -aoc '022d60202e446ad1287b9fb68e687c8a0760788b' "$f" || true)"
done
# commit_hits must be > 0 for ALL THREE. It is 0 for arm64-v8a and x86_64 today.

# 4.4 Full local gate
./scripts/verify-project.sh FULL
```

**The decisive check is 4.3.** `commit_hits` is `565` for armeabi-v7a and `0` for
arm64-v8a and x86_64 right now (measured with a regex over the raw bytes; `grep -c`
reports `37` for armeabi-v7a because it counts *lines* containing the hash, not
occurrences — either way the signal is non-zero vs zero). After a correct rebuild all
three must be non-zero. If any is still `0`, the rebuild did **not** use the pinned
ref — stop and investigate rather than committing.

Also confirm `git diff --stat` on `TdApi.java`: it should show the 41 master-only
classes disappearing and `canSendWelcomeMessages` /
`hasOutgoingMessageAccentColor` being removed, i.e. it moves **back** to what is
already checked in. If `TdApi.java` does not change at all, the rebuild did not use
the pinned commit.

## 5. Checksums into the manifest

`docs/TDLIB_SHA256SUMS.txt` is regenerated automatically by stage 6 of
`build-tdlib-android.sh`. `docs/TDLIB_ARTIFACT_MANIFEST.md` section 3 carries the
per-artifact table and section 4 points at the checksum file — **update the manifest's
sizes and the `Pinned source commit` only if they refer to the same unchanged
binaries; never invent a checksum.** The manifest's OpenSSL note (section 4) says
`libssl.so`/`libcrypto.so` are deliberately not pinned because the CI rebuild is
authoritative — keep that policy.

Confirm the checker agrees:

```bash
TDLIB_CHECK_ABI=all ./scripts/check-tdlib-artifacts.sh
```

## 6. Inspect the built APK

The source-level checks do not prove what ships. Build and inspect the APK itself:

```bash
./gradlew --no-daemon :app:assembleDebug
APK=app/build/outputs/apk/debug/app-debug.apk     # redirected on this host, see below
unzip -l "$APK" | grep 'libtdjni'

# extract each ABI's lib and re-verify architecture + alignment
mkdir -p /tmp/apkcheck && cd /tmp/apkcheck
unzip -o -j "$APK" 'lib/*/libtdjni.so' -d .
for abi in arm64-v8a armeabi-v7a x86_64; do
  printf '%-14s %s\n' "$abi" "$(file libtdjni.so | sed 's|.*: ||')"
done
```

Note on this host: `app/build.gradle.kts` redirects `buildDir` to
`%LOCALAPPDATA%\Temp\tdg-build` on Windows, so the APK is at
`%LOCALAPPDATA%\Temp\tdg-build\app\build\outputs\apk\debug\app-debug.apk`. Pass native
Windows paths to `unzip`/`python`, not MSYS `/c/...` paths.

## 7. Runtime verification (mandatory, and NOT done)

Per `AGENTS.md`, anything touching TDLib requires a runtime check before being called
resolved. **None of this has been performed, and it cannot be on the current host** —
no emulator is configured and no NDK is installed.

Required before merge:

1. Install the APK on an **arm64-v8a** device or emulator (the ABI that is currently
   wrong).
2. Confirm `System.loadLibrary("tdjni")` succeeds — the app logs
   `"All TDLib native libraries loaded successfully"` from
   `TelegramClientImpl.ensureNativeRuntime()`; the absence of a native load failure is
   the signal.
3. Complete a real Telegram authentication (`AuthorizationStateWaitPhoneNumber` →
   `Ready`).
4. Perform one real upload end-to-end, and confirm the completion signal comes from
   Telegram, not from local staged state.
5. Optionally repeat on the x86_64 emulator, which
   `.github/workflows/android-device-smoke.yml` already covers
   (`TdLibRuntimeSmokeTest`).

Until 1–4 pass, this procedure is **not** complete regardless of how green CI is.

## 8. Rollback

A full backup of every native library as it stood before Phase 2 is at:

```
/c/Users/acer/Telegram-Drive-Uploader/tdlib-backup-2026-10-10/
  app/<abi>/{libtdjni.so,libssl.so,libcrypto.so}      # original app/ set
  data/<abi>/{libtdjni.so,libssl.so,libcrypto.so}     # original data/ set
  data/x86_64/libtdjni.so.armstray.moved              # the stray ARM binary (2d162cfe…)
  dead-core-modules/core/{ui,navigation,utils}/src/... # the 8 untracked dead files
```

To roll back the **Phase 2 relocation** (not recommended — it reintroduces the
wrong-architecture placement):

```bash
# restore the pre-Phase-2 tree, then re-apply nothing
cp tdlib-backup-2026-10-10/data/*/libtdjni.so data/src/main/jniLibs/
```

To roll back a **failed rebuild PR**, simply do not merge it — the current
`chore/phase2-fixes` branch holds the known-good state, and the shipped binaries
(`2f58d26c…`, `aada91bd…`, `f271d269…`) are all present in the backup.

To roll back **after** merging a bad rebuild, restore from the backup and commit:

```bash
git checkout tdlib-backup-2026-10-10/data/<abi>/libtdjni.so -- data/src/main/jniLibs/<abi>/libtdjni.so
# verify with 4.1–4.4, then commit
```

Verify any restored file with `sha256sum` against the values printed in the backup
listing before trusting it.

---

## Provenance

- Written from: `.github/workflows/tdlib-upgrade.yml`, `scripts/build-tdlib-android.sh`,
  `scripts/build-openssl-android.sh`, `scripts/check-tdlib-artifacts.sh`,
  `scripts/check-elf-alignment.py`, `docs/TDLIB_ARTIFACT_MANIFEST.md`,
  `docs/maintenance/TDLIB_VERSION_DECISION.md`,
  `docs/maintenance/TDLIB_VERSION_CHECK.md`.
- The procedure was **not executed**. No native library was built, replaced, or
  edited, and `TdApi.java` was not touched.
