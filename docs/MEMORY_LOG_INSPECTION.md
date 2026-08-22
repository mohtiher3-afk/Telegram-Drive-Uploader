# Memory and long-upload log inspection

## Evidence reviewed

- No local Android logcat, crash, ANR, or heap-dump files were present in the repository.
- Recent Android Multi-ABI CI and signed release logs completed successfully and contained no `OutOfMemoryError`, allocation failure, GC-overhead, file-descriptor leak, or closeable-leak signatures. The only memory-related text was the Gradle runner's normal `-Xmx512m` and heap-dump-on-OOM configuration.
- The device smoke workflow has not supplied runtime heap evidence; a pending/cancelled smoke workflow is not evidence of a leak or of leak-free behavior.

## Code lifecycle observations

- `StreamingFileReaderImpl` closes `AssetFileDescriptor`, `ParcelFileDescriptor.AutoCloseInputStream`, source input streams, and destination output streams with `use`.
- Local staging uses a bounded 1 MiB copy buffer and deletes the temporary staged file in `TelegramUploadEngineImpl`'s `finally` block.
- TDLib upload progress uses `callbackFlow`; `awaitClose` removes pending uploads whose channel matches the flow, and success/failure handlers remove the matching pending upload and close the channel.
- `TelegramClientImpl` owns a process-lifetime `CoroutineScope(SupervisorJob + Dispatchers.Default)`, which is appropriate for the singleton TDLib client but requires explicit shutdown cleanup if the client is ever destroyed.
- The pending-upload maps are bounded by active uploads and are removed on terminal events or flow cancellation. No unbounded per-progress sample list is present in the upload engine; the speed calculator retains only a five-measurement window.

## Conclusion

The available evidence does not show a memory leak during long uploads. It supports correct stream and temporary-file cleanup and bounded progress sampling, but only a physical-device run with Android Studio Profiler or repeated `dumpsys meminfo` can establish whether native TDLib memory, decoder resources, or process RSS grows and fails to return after multiple long uploads.
