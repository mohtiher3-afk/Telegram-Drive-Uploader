# Android Multi-ABI CI Performance Evidence — `69fc26f`

**Source run:** [Android Multi-ABI CI #33030237575](https://github.com/mohtiher3-afk/Telegram-Drive-Uploader/actions/runs/33030237575)
**Commit:** [`69fc26f`](https://github.com/mohtiher3-afk/Telegram-Drive-Uploader/commit/69fc26f271756bd0ea813a89b532bc96a6a75da1)
**Outcome:** All jobs succeeded.

## Per-ABI elapsed time

| ABI | Job elapsed | Android SDK setup | SDK package install | OpenSSL runtime preparation | Gradle setup | JVM unit tests | Release lint | Debug APK build | Artifact upload |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| arm64-v8a | 389 s (6m 29s) | 25 s | 23 s | 1 s | 20 s | 98 s | 85 s | 103 s | 8 s |
| armeabi-v7a | 533 s (8m 53s) | 21 s | 16 s | 78 s | 21 s | 121 s | 110 s | 133 s | 3 s |
| x86_64 | 535 s (8m 55s) | 18 s | 17 s | 79 s | 25 s | 125 s | 109 s | 132 s | 3 s |

The ABI jobs run concurrently, so their elapsed times must not be added together as CI wall-clock duration. The selected timed stages account for 363 s of arm64-v8a, 503 s of armeabi-v7a, and 508 s of x86_64; the difference is ordinary checkout, configuration, static verification, cleanup, and post-job work.

## Interpretation

| Observation | Evidence-based interpretation |
|---|---|
| arm64-v8a completed 144–146 s before the other two jobs | The visible cause is a 77–78 s longer OpenSSL preparation in the two later jobs, together with 23–30 s longer test, lint, and APK stages. The logs do not establish why the OpenSSL work differed. |
| APK assembly is the largest explicitly timed build stage | Debug APK assembly took 103–133 s, followed by JVM tests at 98–125 s and release lint at 85–110 s. This is an observation for this run, not a general benchmark. |
| Artifact upload is small | Upload took 3–8 s and is not the dominant recorded stage. |

## Memory evidence

The GitHub Actions logs **do not publish peak RSS, JVM heap consumption, Kotlin compiler memory, Gradle worker memory, GC metrics, or process-level memory time series**. Therefore, actual per-ABI memory usage is **not available** from this run and cannot be calculated truthfully.

| Available configuration or log item | Meaning | Not a memory-usage measurement |
|---|---|---|
| `org.gradle.jvmargs=-Xmx4g -Dfile.encoding=UTF-8` | Maximum Gradle heap configuration in the repository. | It is a limit, not observed heap usage. |
| `org.gradle.workers.max=4` | Project-level upper worker limit. | CI commands cap tests/lint at 2 workers and packaging at 1; neither states process memory. |
| `kotlin.compiler.execution.strategy=in-process` | Kotlin compilation shares the Gradle process. | It does not report how much memory compilation used. |
| Gradle cache summaries, e.g. 947 MB restored / 936 MB saved | Disk/network cache transfer and storage information. | It is not RAM consumption. |
| No `OutOfMemory`, heap, metaspace, or RSS failure | The jobs completed successfully. | It proves only that no such failure was emitted, not a peak-memory figure. |

To obtain measured per-ABI memory in a future run, the workflow would need an explicit telemetry step that samples the Gradle/Kotlin process tree and uploads a redacted summary. That is a CI workflow change and has not been made in this analysis.
