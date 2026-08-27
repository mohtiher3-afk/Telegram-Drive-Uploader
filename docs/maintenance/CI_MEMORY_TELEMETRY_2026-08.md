# CI Memory Telemetry Design — August 2026

## Goal

The Android Multi-ABI CI workflow records actual, bounded process-memory evidence around JVM tests, release lint, and selected-ABI Debug APK assembly. The reports support performance diagnosis without expanding the workflow’s secret, signing, publishing, or release scope.

## Measurement contract

| Metric | Collection method | Meaning | Excluded data |
|---|---|---|---|
| `peak_process_tree_rss_kib` | Samples RSS for the launched Gradle process and its descendants every two seconds; reports the highest aggregate. | Approximate peak resident memory for the observed task process tree. | PIDs, command arguments, source paths, environment variables, and process lists. |
| `peak_java_heap_used_kib` | Samples Java `GC.heap_info` for Java processes in the observed tree; reports the largest observed used-heap value. | Approximate sampled JVM heap peak, not JVM `-Xmx`. | JVM arguments, environment variables, stack traces, and source paths. |
| `sample_count` and interval | Recorded alongside each metric. | Lets readers judge sample resolution. | Fine-grained per-sample traces. |

The reports deliberately contain values only, one file per tracked task, and upload as a separate short-retention CI artifact for each ABI. A task failure still writes and uploads its telemetry report if the monitoring wrapper started.

## Scope and safety

The wrapper does not alter Gradle task arguments, Android ABI selection, credentials, `.env` generation, signing, test/lint gates, artifact verification, artifact uploads, or release publication. It uses runner-standard Bash, `ps`, `pgrep`, `awk`, `sort`, and the JDK-provided `jcmd`; no package installation or externally uploaded build scan is introduced.

The values are sampled rather than event-traced. They are suitable for comparing repeated CI runs under the same workflow, not for claiming exact allocation or garbage-collection behavior. The GitHub-hosted runner and each ABI job can vary independently.
