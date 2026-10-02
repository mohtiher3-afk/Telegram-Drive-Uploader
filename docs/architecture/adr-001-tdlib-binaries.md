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

## Consequence: `:data` is not covered, and why

Committing `TdApi.java` here is not what broke anything, but this is where the
investigation that found that out is recorded.

### The `Illegal Capacity` failures were a corrupt Gradle cache

Several `:data` test tasks failed with:

```
Execution failed for task ':data:testDebugUnitTest'
> java.lang.IllegalArgumentException: Illegal Capacity: -96150052
```

This was initially attributed to coverage instrumentation of `TdApi.java`
(136,572 lines, 3,169 nested classes) and to the JVM bytecode probe arrays. **That
attribution was wrong.** The stack trace shows where it actually originates:

```
Caused by: java.lang.IllegalArgumentException: Illegal Capacity: -96150052
  at org.gradle.api.internal.tasks.testing.results.serializable.SerializableTestResult$Serializer.deserializeFailure
  at org.gradle.api.internal.tasks.testing.results.serializable.SerializableTestResult$Serializer.deserialize
  at org.gradle.api.internal.tasks.testing.Test.getPreviousFailedTestClasses
  at com.android.build.gradle.tasks.factory.AndroidUnitTest.executeTests
```

Gradle deserialises **previously cached** test outcomes from
`<module>/build/test-results` *before* it runs any test. A truncated or corrupt
file there aborts the task. Evidence that this is the cause and not the tests:

- The negative value is byte-identical across every failure.
- It occurs with any `--tests` filter, including a single plain non-Robolectric class.
- Raising the test-worker heap to 2 GB changed nothing.
- It still occurred at a clean checkout, with Kover removed from `:data`
  entirely, and with over 3 GB free on disk.
- Clearing the cached results directory makes the task pass immediately.

**Nothing about TDLib, Kover, Robolectric, memory, or disk caused it.** The
hypotheses were tested and falsified rather than assumed.

### The fix that shipped

`scripts/verify-project.ps1` now clears every module's `build/test-results`
before running, so no run can be decided by a corrupt or stale cache, and a
`PASS` can only mean outcomes this run actually produced. This costs a re-run of
`:data` and `:feature` tests that Gradle might otherwise mark up-to-date, which is
the intended trade: an up-to-date test result is not evidence.

### Coverage scope

Kover is applied to `:feature` only. `:data` has no coverage plugin and must not
be reported as a measured 0% or as "not applicable" - it is unmeasured.

```
./gradlew :feature:koverHtmlReport :feature:koverXmlReport
  -> feature/build/reports/kover/html/index.html
  -> feature/build/reports/kover/report.xml
```

`:feature` measures **47.9%** line coverage, with 18 namespaces under 5%. Any
figure quoted from this repository must come from an artifact written by the same
run.

### Permanent fix for the `:data` gap

Coverage of `:data` needs the generated binding out of the instrumented module:

| Module | Contents | Coverage |
|---|---|---|
| `:tdlib-bindings` | `org.drinkless.tdlib.**` (generated, vendored) | excluded by design |
| `:data` | repositories, TDLib client, upload engine, Room DAO | measurable |

That split is independent of the cache bug above and is still outstanding.

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
