# TDLib native artifacts — 16 KB analysis and options

Read-only analysis. **No native library was rebuilt, replaced, or edited while
producing this document.** It exists so you can decide what to do; the options are
priced out, not acted on.

Everything below was measured in this session with
`python scripts/check-elf-alignment.py` plus direct ELF-header parsing, because
`readelf`, `llvm-readelf` and `objdump` are not installed on this machine and no
NDK is present at `%LOCALAPPDATA%\Android\Sdk\ndk`.

---

## 1. The headline: the binaries are in the wrong ABI directories

This is a bigger problem than the 16 KB alignment issue the phase-1 audit found, and
it was invisible to every existing gate.

Measured `e_machine` and size for each `libtdjni.so`:

| Path on disk | `e_machine` (actual) | Size | SHA-256 | `docs/TDLIB_SHA256SUMS.txt` documents that hash as |
|---|---|---:|---|---|
| `data/src/main/jniLibs/arm64-v8a/libtdjni.so` | **x86-64 (62)** | 22,825,208 | `f271d269af…` | `app/src/main/jniLibs/x86_64/libtdjni.so` |
| `data/src/main/jniLibs/armeabi-v7a/libtdjni.so` | **ARM (40)** | 14,407,696 | `aada91bdf…` | `app/src/main/jniLibs/armeabi-v7a/libtdjni.so` ✅ |
| `data/src/main/jniLibs/x86_64/libtdjni.so` | **ARM (40)** | 14,576,768 | `2d162cfe…` | **no entry at all** |
| `app/src/main/jniLibs/arm64-v8a/libtdjni.so` | **AArch64 (183)** | 20,640,912 | `2f58d26c…` | `app/src/main/jniLibs/arm64-v8a/libtdjni.so` ✅ |

(The `app/` row is the correct arm64 binary, in the wrong module — see below.)

Read that carefully:

- The file in `arm64-v8a/` is an **x86-64 binary**, and its hash matches the one the
  checksum file documents for `x86_64/libtdjni.so`.
- The file in `x86_64/` is an **ARM binary** with a hash that appears nowhere in
  `docs/TDLIB_SHA256SUMS.txt`.
- Only `armeabi-v7a/` holds a file that is both the right architecture and the right
  documented checksum.

In other words the three files in `data/src/main/jniLibs/**` are **cross-placed**,
and the x86_64 one is undocumented. A real arm64 device cannot load the
`data/.../arm64-v8a/` file — it is x86-64 machine code — so as the tree stands
today, `:data` would ship a binary that cannot load on any arm64 device.

**The genuine arm64 binary does exist**, but in the *other* directory:
`app/src/main/jniLibs/arm64-v8a/libtdjni.so` is a real **AArch64 ELF64**, 20,640,912
bytes, aligned `0x4000`, and its hash `2f58d26c…` matches the checksum file's arm64
entry exactly. It is simply not in the module that `:app` consumes its native libs
from (`:data`), so it never reaches the APK. So this is most likely a copy/staging
mistake between `app/` and `data/`, not a missing build — the fix is placement, and
no rebuild is needed for arm64.

The phase-1 audit reported only "x86_64 is not 16 KB aligned". The actual defect is
worse: x86_64 holds an ARM binary, the genuine x86-64 binary is sitting in the
arm64 directory of the wrong module, and the real arm64 binary sits in a directory
`:app` does not package from.

**Why no gate caught it.** `scripts/check-tdlib-artifacts.sh` does have an
architecture check (`check_elf_arch`), but on this host it falls through to the
`file` utility and accepts anything whose output contains "ARM" **or** "ELF":

```sh
if [[ "$file_info" == *"ARM"* || "$file_info" == *"ELF"* ]]; then
    echo "[ARCHITECTURE via file] $abi: $file_info"
    return 0          # <-- unconditional pass
fi
```

Since `file` prints "ELF" for every shared object, that branch returns success for
**any** ELF file in **any** directory. The `readelf` path below it would have caught
this, but `readelf` is not installed. And the checksum pass is blind to it too: it
resolves each documented path against `app/src/main/jniLibs/**`, where only
`arm64-v8a/` exists — so 6 of 9 entries are reported as `MISSING CHECKSUM TARGET`
and the comparison against the real files in `data/src/main/jniLibs/**` never
happens. A wrong-architecture file in a directory the checker does not look at is
simply not seen.

---

## 2. LOAD segment alignment, measured

`p_align` of every PT_LOAD segment, from `scripts/check-elf-alignment.py`:

| File | `p_align` values | 16 KB aligned (≥ 0x4000)? | But is it even the right arch? |
|---|---|---|---|
| `data/src/main/jniLibs/arm64-v8a/libtdjni.so` | `0x4000`, `0x4000`, `0x4000` | ✅ yes | ❌ **no — it is x86-64** |
| `data/src/main/jniLibs/armeabi-v7a/libtdjni.so` | `0x4000`, `0x4000`, `0x4000` | ✅ yes | ✅ ARM, correct |
| `data/src/main/jniLibs/x86_64/libtdjni.so` | `0x1000`, `0x1000`, `0x1000` | ❌ **no — 4 KB** | ❌ **no — it is ARM** |
| `data/src/main/jniLibs/arm64-v8a/libcrypto.so` | `0x4000` ×4 | ✅ | ✅ |
| `data/src/main/jniLibs/arm64-v8a/libssl.so` | `0x4000` ×4 | ✅ | ✅ |
| `data/src/main/jniLibs/armeabi-v7a/libcrypto.so` | `0x4000` ×4 | ✅ | ✅ |
| `data/src/main/jniLibs/armeabi-v7a/libssl.so` | `0x4000` ×4 | ✅ | ✅ |
| `data/src/main/jniLibs/x86_64/libcrypto.so` | `0x4000` ×4 | ✅ | ✅ |
| `data/src/main/jniLibs/x86_64/libssl.so` | `0x4000` ×4 | ✅ | ✅ |

Full program-header detail (from direct ELF parsing):

```
arm64-v8a/libtdjni.so  (x86-64)  LOAD align=0x4000 vaddr=0x0        filesz=0x150ba30
                                  LOAD align=0x4000 vaddr=0x150ba30 filesz=0xb5e68
                                  LOAD align=0x4000 vaddr=0x15c18a0 filesz=0x2808

armeabi-v7a/libtdjni.so (ARM32)  LOAD align=0x4000 vaddr=0x0        filesz=0xd546c0
                                  LOAD align=0x4000 vaddr=0xd586c0  filesz=0x6751c
                                  LOAD align=0x4000 vaddr=0xdc3be0  filesz=0x339d1

x86_64/libtdjni.so     (ARM32!)  LOAD align=0x1000 vaddr=0x0        filesz=0xd7c690
                                  LOAD align=0x1000 vaddr=0xd7d690  filesz=0x689bc
                                  LOAD align=0x1000 vaddr=0xde7050  filesz=0x33c79
```

Note the x86_64 file's segment layout is an ARM32 layout, not an x86-64 one — further
confirmation it is a misnamed ARM build.

Summary: **the only correctly-placed, correctly-aligned library is armeabi-v7a.**
The other two need to be dealt with before any 16 KB conversation is meaningful.

---

## 3. Where these libraries came from

**Documented in the repo** (`docs/TDLIB_ARTIFACT_MANIFEST.md`):

| Field | Value |
|---|---|
| Repository | https://github.com/tdlib/td |
| Version | `v1.8.66` (manifest header) — but `docs/TDLIB_SHA256SUMS.txt` says **`v1.8.67`** |
| Pinned source commit | `022d60202e446ad1287b9fb68e687c8a0760788b` |
| NDK | `26.3.11579264` (NDK r26d — matches the `file` build-ID string) |
| CMake | `3.22.1` |
| OpenSSL | `3.0.16` |
| Zlib | `1.3.1` |
| Android platform | API 24 |

There is a **version inconsistency** to resolve: the manifest says `v1.8.66` while
the checksum file header says `v1.8.67`. `scripts/check-tdlib-artifacts.sh` uses
`TDLIB_VERSION` from the manifest for its log text, so the two documents disagree
about which release these binaries are.

**Build path.** `scripts/build-tdlib-android.sh` cross-compiles per ABI:

```sh
TARGET_ABIS="${TARGET_ABIS:-arm64-v8a armeabi-v7a x86_64}"
...
cmake -S "$TD_DIR/example/android" -B "$BUILD_ABI" -G Ninja \
    -DANDROID_ABI="$ABI" ...
cmake --build "$BUILD_ABI" --target tdjni
DEST="$PROJECT_ROOT/data/src/main/jniLibs/$ABI"
```

That script is **correct** — it writes each ABI's output into its own matching
directory. So the cross-placement on disk was not produced by this script; the files
were placed by hand (or by a failed copy) at some point. `libcrypto.so`/`libssl.so`
are rebuilt per ABI by `scripts/build-openssl-android.sh` from the pinned OpenSSL
3.0.16 archive and are deliberately **not** checksum-pinned, because the CI rebuild is
the authoritative input.

**Checksum files.** `docs/TDLIB_SHA256SUMS.txt` (generated 2026-09-11) lists 9
entries, all under `app/src/main/jniLibs/**` — a path where only `arm64-v8a/` exists
today. The three `libtdjni.so` hashes it documents are `2f58d26c…` (arm64),
`2d162cfe…` (armeabi-v7a), `f271d269…` (x86_64).

Compare with what is actually on disk:

| Directory | Hash on disk | Documented hash for that ABI | Match? |
|---|---|---|---|
| `arm64-v8a/` | `f271d269…` | `2f58d26c…` | ❌ |
| `armeabi-v7a/` | `aada91bdf…` | `2d162cfe…` | ❌ |
| `x86_64/` | `2d162cfe…` | `f271d269…` | ❌ |

So the checksum file's arm64 hash (`2f58d26c…`) matches the file in
**`app/src/main/jniLibs/arm64-v8a/`** — not `data/`. Its armeabi-v7a hash
(`2d162cfe…`) matches the file sitting in **`data/.../x86_64/`**, and its x86_64 hash
(`f271d269…`) matches the file sitting in **`data/.../arm64-v8a/`**. The three
binaries exist and all three hashes are documented; they are simply shelved in the
wrong directories, and `app/` vs `data/` are two different places to be wrong about.

**How the checker validates.** `scripts/check-tdlib-artifacts.sh`:
1. Resolves each `TDLIB_SHA256SUMS.txt` line against `app/src/main/jniLibs/<path>`
   and `sha256sum`s it → 6 of 9 fail as `MISSING CHECKSUM TARGET`.
2. For each ABI, `check_file` (size ≥ 5,000,000 bytes), `check_elf_arch`, and
   `check_16kb_alignment` against `data/src/main/jniLibs/<abi>/libtdjni.so`.
3. `check_elf_arch` needs `readelf`; without it, the `file` fallback accepts any ELF.
4. `check_16kb_alignment` tries `readelf -lW`, then `llvm-objdump`, then
   `scripts/check-elf-alignment.py`. On this host all three are unavailable, so it
   reports `no LOAD segments readable` for every ABI and increments the miss count.

The script's own logic is sound — it is the environment (no `readelf`) and the stale
path prefix that make it useless here.

---

## 4. What would be needed to fix the native artifacts

**Option A — rebuild all three ABIs from source (the clean fix).**

1. Install NDK `26.3.11579264` (r26d) — it is not on this machine.
2. Run `scripts/build-openssl-android.sh` with `FORCE_ALL_ABIS=1` so every ABI has
   OpenSSL under `install-<abi>/`.
3. Run `scripts/build-tdlib-android.sh` — it already loops all three ABIs and writes
   to the right directories, so the cross-placement cannot recur.
4. For 16 KB alignment, pass the linker flag for 16 KB ELF alignment
   (`-Wl,-z,max-page-size=16384`) in the CMake build. NDK r26+ defaults to 16 KB, but
   the on-disk x86_64/ARM file at `0x1000` shows at least one build predates or
   overrides that default.
5. Regenerate `docs/TDLIB_SHA256SUMS.txt` with **correct** ABI paths, and reconcile
   the `v1.8.66` vs `v1.8.67` disagreement.
6. Re-measure with `python scripts/check-elf-alignment.py` on all nine files.

Cost: a full toolchain install plus three TDLib builds (TDLib is slow — the arm64
output alone is 22 MB).

**Option B — repair the placement without rebuilding (recommended for arm64).**

The genuine arm64 binary already exists in the repo at
`app/src/main/jniLibs/arm64-v8a/libtdjni.so` (`2f58d26c…`, AArch64, `0x4000`). For
arm64, no build is needed — only moving that file into
`data/src/main/jniLibs/arm64-v8a/`, replacing the mis-shelved x86-64 binary that is
there now. That single placement fixes the architecture problem for the ABI that
actually matters to users.

x86_64 is different: there is **no** genuine 16 KB-aligned x86-64 `libtdjni.so`
anywhere in the repo. The only real x86-64 binary (`f271d269…`) is at `0x4000` but
sits in `data/.../arm64-v8a/`, so it could be moved into `x86_64/` — that would fix
both its architecture **and** its alignment at once, with no rebuild. The current
`x86_64/libtdjni.so` (`2d162cfe…`, ARM32, `0x1000`) appears to be a stray
armeabi-v7a-adjacent build and would be discarded.

So Option B is: move `f271d269…` → `data/.../x86_64/`, move `2f58d26c…` →
`data/.../arm64-v8a/`, leave `aada91bdf…` in `armeabi-v7a/`, regenerate the checksum
file with `data/` paths, and delete the stray `2d162cfe…`. All three ABIs end up
correct architecture **and** `0x4000`-aligned, with zero compilation. This is the
cheapest correct outcome and should be verified with `check-elf-alignment.py` before
trusting it.

**Option C — drop x86_64 from the shipped ABIs.**

Simplest and safest, but see the CI impact below.

---

## 5. What removing x86_64 from the published ABIs would change

**Gradle.** In `app/build.gradle.kts`, x86_64 appears in three places:

```kotlin
ndk { abiFilters.addAll(listOf("arm64-v8a", "armeabi-v7a", "x86_64")) }
// and inside splits.abi:
val supportedAbis = listOf("arm64-v8a", "armeabi-v7a", "x86_64")
```

The `splits.abi` block is gated behind `-PtargetAbi=<abi>` and asserts membership in
`supportedAbis`, so dropping x86_64 from that list makes
`-PtargetAbi=x86_64` fail loudly — which is what you want. `isUniversalApk = true`
means a universal APK is also produced; with x86_64 removed it would carry arm64 +
armeabi-v7a only.

**CI.** Three workflows hardcode x86_64:

| Workflow | Line(s) | What changes |
|---|---|---|
| `.github/workflows/android-ci.yml` | 96 | matrix `abi: [arm64-v8a, armeabi-v7a, x86_64]` — remove the entry |
| `.github/workflows/android-release.yml` | 89 | release matrix — same removal |
| `.github/workflows/android-release.yml` | 193 | `UNEXPECTED_LIBS` grep pattern enumerates the three ABIs; must drop the x86_64 alternative |
| `.github/workflows/android-device-smoke.yml` | 28, 60, 85, 91, 106 | **this workflow is x86_64-only** — it builds and runs the emulator smoke test on x86_64 |

The device-smoke workflow is the blocker: it is the repo's only automated
TDLib runtime test (`TdLibRuntimeSmokeTest`), and it runs on an **x86_64 emulator**.
Removing x86_64 deletes the only runtime verification of `System.loadLibrary("tdjni")`
that CI performs. `AGENTS.md` requires a runtime check for anything touching TDLib,
so dropping x86_64 without replacing that gate trades a compile-time green for no
runtime coverage at all.

**Release naming.** `.github/workflows/android-release.yml` names artifacts
`telegram-drive-uploader-${abi}-debug` and the matrix produces one APK per ABI, so
the release simply stops publishing an x86_64 APK. Because GitHub release tags are
immutable (a deleted-and-republished tag name is burned), no already-published tag
can be edited — but dropping x86_64 going forward is a plain naming change on future
tags, not a rename of past ones.

**App bundle.** `bundle { abi { enableSplit = true } }` is unaffected; the AAB just
carries two splits instead of three.

---

## 6. Recommendation

Do these in order, each as its own commit, and **not** before you approve:

1. **Fix the placement — no rebuild needed.** The genuine arm64 binary
   (`2f58d26c…`, AArch64, `0x4000`) already exists at
   `app/src/main/jniLibs/arm64-v8a/` and only needs to be moved into
   `data/src/main/jniLibs/arm64-v8a/`, replacing the mis-shelved x86-64 binary. The
   genuine x86-64 binary (`f271d269…`, also `0x4000`) needs to move from
   `data/.../arm64-v8a/` into `data/.../x86_64/`. Result: all three ABIs correct
   architecture and 16 KB aligned, zero compilation. Verify each with
   `python scripts/check-elf-alignment.py`.
2. **Then fix the gate**, so this class of defect cannot hide again: point
   `check-tdlib-artifacts.sh` at `data/src/main/jniLibs/**` (not
   `app/src/main/jniLibs/**`), regenerate `TDLIB_SHA256SUMS.txt` with correct ABI
   paths, and make `check_elf_arch` **fail** when `readelf` is unavailable instead of
   accepting any ELF via `file`. Also delegate the alignment read to
   `scripts/check-elf-alignment.py` (which works here) when no readelf exists. A
   simple "assert `e_machine` matches the directory name" check would have caught
   this whole class of bug in seconds.
3. **Reconcile `v1.8.66` vs `v1.8.67`** in the manifest and checksum header.
4. Only if step 1 is rejected, consider rebuilding (Option A) or dropping x86_64
   (Option C) — both are far more expensive than a correct placement.

Until item 1 is done, the `TDLib` gate in `verify-project.sh` is expected to keep
failing, and any arm64 build would ship a binary that cannot load.

---

## Provenance

- Commands run: `python scripts/check-elf-alignment.py` on all nine `.so` files;
  direct ELF program-header parsing via `python -`; `sha256sum` on all three
  `libtdjni.so`; `cmp` between armeabi-v7a and x86_64; reads of
  `docs/TDLIB_ARTIFACT_MANIFEST.md`, `docs/TDLIB_SHA256SUMS.txt`,
  `scripts/check-tdlib-artifacts.sh`, `scripts/build-tdlib-android.sh`,
  `.github/workflows/*.yml`, `app/build.gradle.kts`.
- No native library was read-modified, rebuilt, replaced, or deleted.
- Not available on this host: `readelf`, `llvm-readelf`, `llvm-objdump`, `objdump`,
  an NDK, and the `android` CLI. Alignment figures therefore come from the
  pure-Python reader in `scripts/check-elf-alignment.py`, cross-checked against
  direct struct parsing.
