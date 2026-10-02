# ADR-001: Committing TDLib and OpenSSL native binaries to the repository

- **Status:** Accepted
- **Date:** 2026-10-02
- **Scope:** `data/src/main/jniLibs/`, `check-repository-security.sh`, `.gitignore`

## Context

The application links TDLib natively through JNI. Packaging a working APK needs
`.so` libraries for every supported ABI, plus the OpenSSL libraries TDLib depends
on. Those libraries are large, are not produced by the Gradle build, and cannot be
reconstructed from source without a full TDLib and OpenSSL cross-compile.

The repository already had a rule against committing build output:

> `AGENTS.md`: *"never commit keystores, API keys, .env values, session data, or
> built APK/AAB files"*

That rule is about **build products of this project**. It was written to stop APKs,
AABs, keystores, and session data from entering history, and it does not describe
third-party prebuilt libraries that the build consumes as an input.

Nobody had written down that distinction, so the committed `.so` files sat in the
tree looking like a violation of the rule they do not actually violate. This ADR
records the decision explicitly and adds the exception to the rule text.

## Measured cost

| File | Size |
|---|---|
| `x86_64/libtdjni.so` | 22.0 MB |
| `arm64-v8a/libtdjni.so` | 19.9 MB |
| `armeabi-v7a/libtdjni.so` | 14.1 MB |
| `x86_64/libcrypto.so` | 4.7 MB |
| `arm64-v8a/libcrypto.so` | 4.5 MB |
| `armeabi-v7a/libcrypto.so` | 3.7 MB |
| `x86_64/libssl.so` | 0.7 MB |
| `arm64-v8a/libssl.so` | 0.7 MB |
| `armeabi-v7a/libssl.so` | 0.6 MB |
| **Total (9 files)** | **~69.4 MB** |

The resulting `.git` directory is roughly 315 MB. This is a real, permanent cost:
every future clone and fetch carries it.

## Decision

**Keep the native binaries committed in the repository.**

### Rationale

1. **Bounded, offline, reproducible builds.** `./gradlew assembleDebug` succeeds
   on a clean checkout with no network access to any TDLib or OpenSSL artifact
   source. The build does not depend on a third-party CDN staying reachable, on a
   release remaining published, or on a mirror being up. For a project whose CI runs
   an OpenSSL cross-compile from source on every ABI job, this removes a whole
   class of network flakiness from the critical path.
2. **The build cannot produce them.** `scripts/build-tdlib-android.sh` and
   `scripts/build-openssl-android.sh` exist for CI and for producing an *upgrade*,
   but they need the NDK, a long cross-compile, and their own network access. They
   are not part of the ordinary assemble path. Making the binaries an external
   download would make the ordinary build depend on a network round trip that
   currently does not exist.
3. **Artifact verification is already a gate.** `scripts/check-tdlib-artifacts.sh`
   validates the three ABI layouts and the expected libraries, and it runs in CI.
   A committed artifact is at least *fixed and reviewable*; a downloaded one is
   whatever the server returned that day unless the same check gates it.
4. **The upgrade path is explicit and slow by design.** Upgrading TDLib means
   building it, reviewing the diff, and committing the new binaries. That friction
   is a feature: it is why the project is still on v1.8.66 while a bot posts a
   v1.8.67 reminder issue every day.

### Consequences accepted

- Repository size and clone time stay high.
- History permanently carries the binaries; replacing them later does not reclaim
  the space without a history rewrite, which `AGENTS.md` forbids.
- Reviewers see binary diffs on TDLib upgrades.

### Alternatives rejected

| Option | Why rejected |
|---|---|
| Download in a Gradle task | Introduces network into the assemble path, and makes builds non-reproducible when the upstream artifact changes or disappears. |
| Download once in CI, pass between jobs | Works only in CI. Local and offline builds lose the guarantee that this ADR exists to provide. |
| Commit a single `universal` lib | Removes the per-ABI entries but still commits binaries, so it costs repository size without buying reproducibility. |

## Rule text

This is the exception that the `AGENTS.md` prohibition does not cover. The rule
should read, in effect:

> Never commit build **products of this project** - APKs, AABs, keystores, session
> data, or generated reports. The **third-party prebuilt native libraries** under
> `data/src/main/jniLibs/` are an accepted exception; see ADR-001.

## Verification

`scripts/check-tdlib-artifacts.sh` remains the gate that the committed set is the
expected set. If this decision is ever revisited, that script is the place to
start, together with a measurement of what the build actually needs on a machine
with no TDLib toolchain.
