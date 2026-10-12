# TDLib version consistency check

Read-only investigation. **`TdApi.java` was not edited, nothing was rebuilt, and no
binary was modified.** All findings come from strings and headers measured on the
files as they are checked in.

## 1. Verdict

**The native libraries and the Java bindings are NOT the same version, and the three
`libtdjni.so` files are not the same version as each other.**

| Artifact | Version found in the binary | Source revision |
|---|---|---|
| `data/src/main/jniLibs/arm64-v8a/libtdjni.so` | **1.8.67** | built from **`td-master`** |
| `data/src/main/jniLibs/x86_64/libtdjni.so` | **1.8.67** | built from **`td-master`** |
| `data/src/main/jniLibs/armeabi-v7a/libtdjni.so` | **1.8.66** | built from pinned commit **`022d60202e446ad1287b9fb68e687c8a0760788b`** |
| `data/src/main/java/org/drinkless/tdlib/TdApi.java` | **no version string at all** | cannot be determined from the file |

Everything in the repository's documentation says `v1.8.66`. Only the README says
`v1.8.67`. The binaries themselves say: two of them are 1.8.67 from an unpinned
branch, and one is 1.8.66 from the pinned commit.

## 2. Evidence

### 2.1 Version string inside each binary

Extracted by searching the raw bytes for `1.8.<digit>`:

```
arm64-v8a/libtdjni.so  ->  "1.8.67"
   context: ...saved_dialogs_pinned_limit_default.Btrue.default_reaction_needs_sync
            .1.8.67.query_it != pending_load_country_queries_...

x86_64/libtdjni.so     ->  "1.8.67"
   context: byte-identical region to arm64-v8a

armeabi-v7a/libtdjni.so -> "1.8.66"
   context: ...story_caption_length_max.Bfalse.1.8.66.Option
            "ignore_sensitive_content_restrictions" can't be changed...
```

The string sits in TDLib's generated `td_api` schema table, immediately after the
schema's option flags, which is where TDLib records the API schema version. This is
the library's own self-description, not an inference.

### 2.2 Source revision inside each binary

The `.native-build` path baked into the debug/file paths of each binary:

```
arm64-v8a  -> .../.native-build/td-master/td/generate/auto/td/telegram/td_api_0.cpp
x86_64     -> .../.native-build/td-master/td/generate/auto/td/telegram/td_api_0.cpp
armeabi-v7a-> .../.native-build/td-022d60202e446ad1287b9fb68e687c8a0760788b/td/...
```

Count of occurrences of the pinned commit hash `022d6020…` in each file:

```
arm64-v8a  : 0      <-- does not contain the pinned commit at all
armeabi-v7a: 565    <-- the only build from the documented pinned source
x86_64     : 0      <-- does not contain the pinned commit at all
```

This is the decisive line. `docs/TDLIB_ARTIFACT_MANIFEST.md` records
`Pinned source commit = 022d60202e446ad1287b9fb68e687c8a0760788b`. Only
**armeabi-v7a** was built from that commit. arm64-v8a and x86_64 were built from
`td-master` — a moving branch with no fixed revision.

Consequence: the manifest's claim that "The Java bindings and `libtdjni.so` were
generated from the same source revision" is true for armeabi-v7a only, and false for
the other two ABIs.

### 2.3 The Java bindings

`data/src/main/java/org/drinkless/tdlib/TdApi.java` contains **no** version marker:

- No `1.8.x` literal anywhere in the file (the only "version" hits are unrelated
  field docs: "tgcalls versions", "browser version", "cryptocurrency conversion rate").
- No generated-by/commit header — the file starts directly with `package
  org.drinkless.tdlib;` and a class javadoc that carries no provenance.

So the bindings' version cannot be established from the file itself. Given the
manifest states they were generated from `022d6020…`, they presumably correspond to
**1.8.66** — which matches armeabi-v7a and **mismatches** the arm64-v8a and x86_64
libraries. That is the practical risk: `TdApi.java` defines the Java-side schema, and
two of the three native libraries expose a different (1.8.67, from `td-master`)
schema. A function or class present in 1.8.67 but absent from the 1.8.66-generated
bindings would fail with `UnsatisfiedLinkError` or `NoSuchMethodError` at runtime on
those ABIs — and only at the call site, long after build and CI go green.

`Client.java` (11,015 bytes) and `Log.kt` (3,299 bytes) carry no version marker
either.

## 3. Where 1.8.66 and 1.8.67 appear in the repo

**Says 1.8.66 (16 places, the dominant claim):**

| File | Line |
|---|---|
| `app/build.gradle.kts` | 36 — "Package the verified TDLib v1.8.66 native artifacts" |
| `docs/TDLIB_ARTIFACT_MANIFEST.md` | 8 (Version table), 22 |
| `scripts/check-tdlib-artifacts.sh` | 4 — `TDLIB_VERSION="1.8.66"` (drives all gate output) |
| `docs/architecture/adr-001-tdlib-binaries.md` | 69 |
| `docs/architecture/REFACTORING_STATUS.md` | 144 |
| `docs/architecture/TELEGRAM_DEPENDENCY_MAP.md` | 5 |
| `docs/dependencies/CURRENT_TOOLCHAIN.md` | 16 |
| `docs/dependencies/TDLIB_NATIVE_DEPENDENCIES.md` | 3 |
| `docs/maintenance/COMPREHENSIVE_MAINTENANCE_LOG.md` | 55 |
| `docs/release/FINAL_GO_NO_GO_MATRIX.md` | 13 |
| `docs/release/RELEASE_CANDIDATE_REPORT.md` | 17 |
| `docs/release/RELEASE_CERTIFICATION.md` | 7 |
| `docs/release/RELEASE_ENVIRONMENT.md` | 14 |
| `docs/TDLIB_SHA256SUMS.txt` | 1 (now — the header read `v1.8.67` before this phase) |

**Says 1.8.67 (3 places):**

| File | Line | Note |
|---|---|---|
| `README.md` | 11 — "`v1.8.67` (native, checked in per-ABI)" | matches the arm64/x86_64 binaries, not armeabi-v7a |
| `.github/workflows/tdlib-upgrade.yml` | 16 | example text in an input description, not a pin |
| `docs/architecture/adr-001-tdlib-binaries.md` | 70 | prose about a reminder issue |

**Historical note:** the pre-phase-2 header of `docs/TDLIB_SHA256SUMS.txt` read
`# TDLib v1.8.67 Android native artifacts — generated 2026-09-11`. It was rewritten
during Phase 2 to `v1.8.66` to match the manifest, while the binaries it lists were
**not** changed — so that header now reflects a decision, not a measurement. The
measured truth is in section 2.1 above: two binaries are 1.8.67.

**CI note:** `.github/workflows/tdlib-release-check-stable.yml` parses the pinned
version out of `docs/TDLIB_ARTIFACT_MANIFEST.md` (line 35) and compares it against
upstream `TDLib VERSION` in `CMakeLists.txt`. Because the manifest says `1.8.66`, CI
asserts 1.8.66 — and reports the repo as current regardless of what the arm64/x86_64
binaries actually contain. The gate cannot see the discrepancy.

## 4. What this means, and what would settle it

The manifest is the only document that pins a source commit, and only one of three
binaries matches it. There is no way to resolve which version is "intended" from the
repository alone — `td-master` has no recorded revision, and the 1.8.67 binaries
carry no commit hash.

To settle it definitively, one of:

1. **Rebuild all three ABIs from the pinned `022d6020…`** via
   `scripts/build-tdlib-artifacts.sh`, then re-extract the version strings and
   confirm all three read `1.8.66` and all three contain the pinned hash. This also
   guarantees bindings/binary schema agreement.
2. **Or** decide 1.8.67 is intended, regenerate `TdApi.java`/`Client.java`/`Log.kt`
   from the same `td-master` revision the arm64/x86_64 binaries were built from, and
   re-pin that revision by hash in the manifest — `td-master` must never be a pin.

Until one of those happens, treat `TDLIB_VERSION=1.8.66` in the gate as a label for
log text only, and do not rely on `TdApi.java` and the arm64/x86_64 libraries sharing
a schema.

## 5. Provenance

- Commands run: byte-search of each `libtdjni.so` for `1.8.<digit>`,
  `022d60202e446ad1287b9fb68e687c8a0760788b`, `/home/runner/...` and
  `.native-build/<ref>/`; `head` and `grep` over `TdApi.java`, `Client.java`,
  `Log.kt`; repo-wide grep for `1.8.66` and `1.8.67`.
- Files read: `docs/TDLIB_ARTIFACT_MANIFEST.md`, `README.md`,
  `.github/workflows/tdlib-upgrade.yml`,
  `.github/workflows/tdlib-release-check-stable.yml`,
  `scripts/check-tdlib-artifacts.sh`, `app/build.gradle.kts`, and the docs listed in
  section 3.
- No native library, `TdApi.java`, or R8 rule was modified.
