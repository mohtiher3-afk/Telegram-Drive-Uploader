# Large-File Memory Audit

## Findings

| Area | Classification | Evidence |
|---|---|---|
| Source staging | SAFE / bounded streaming | `StreamingFileReader.copyToFile` uses `InputStream` to `OutputStream` with a 1 MiB buffer rather than `readBytes()` or a whole-file byte array. |
| Chunk reads | MEDIUM RISK / bounded by caller | `readChunk` allocates `ByteArray(size)` and reads a requested range; callers must keep `size` bounded. |
| Video metadata | MEDIUM RISK | `MediaMetadataRetriever` reads scalar duration and dimensions; it does not load the complete video into a Kotlin byte array. |
| Upload engine | SAFE for whole-file buffering | TDLib receives a staged local path rather than a complete in-memory file payload. |
| Progress | SAFE | Progress contains scalar byte and timing fields and is persisted as metadata. |
| Native/TDLib init | SAFE for this audit | The reviewed initialization includes an empty `ByteArray(0)` for TDLib parameters, not file contents. |

No `readBytes()` or whole-file `toByteArray()` path was identified in the reviewed upload implementation. No buffer-size change is proposed without measured memory and throughput evidence. Large-file runtime behavior, including memory pressure and provider read speed, remains untested.
