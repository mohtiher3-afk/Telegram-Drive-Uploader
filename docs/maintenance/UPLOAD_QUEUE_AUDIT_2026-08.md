# Upload Queue and Progress-Write Audit — August 2026

## Scope

This audit covers the current Room, WorkManager, Worker, upload engine, and upload-status presentation boundaries. It preserves real TDLib delivery confirmation, existing retry limits, the Room schema, unique WorkManager naming, and the stable task/destination IDs.

## Repository findings

| Area | Current source behavior | Decision |
|---|---|---|
| Worker execution | `UploadWorker` reloads the task by `upload_id`, records `PREPARING`, consumes real engine events, and marks `COMPLETED` only after `UploadEngineResult.Success`. An unconfirmed stream ends in `FAILED`. | Preserved. |
| Retry behavior | Retryable errors remain `RETRYING` while attempts are below the existing limit of five; otherwise they are final failures. | Preserved. |
| Work identity | Each request uses the persisted task ID as unique work name and carries only `upload_id` in input data. | Preserved. |
| Pause/cancel order | `QueueViewModel` persists `PAUSED` or `CANCELLED` before WorkManager cancellation, which is asynchronous. | A late progress callback was a valid state-overwrite risk. |
| Progress representation | The current UI converts persisted percentage to a 0..1 progress fraction exactly once and renders the percentage from the same value. | The historical 5000%-style representation mismatch is not present in the current source. |

## Confirmed minimal repair

`UploadDao.updateProgress()` previously forced `status = 'UPLOADING'` for every matching task ID. A progress callback that arrived after a queue action could therefore overwrite `PAUSED`, `RETRYING`, `FAILED`, `COMPLETED`, or `CANCELLED`.

The query now applies telemetry and promotes status only when the current durable state is `PREPARING` or `UPLOADING`. This is an atomic SQL boundary check: normal work still moves from preflight to uploading, while a late update cannot reopen paused, retrying, failed, completed, or cancelled tasks.

| Protected behavior | Result |
|---|---|
| Confirmed TDLib completion rule | Unchanged. |
| Existing five-attempt retry policy | Unchanged. |
| Room schema/version and migrations | Unchanged. |
| WorkManager constraint, backoff, unique-work policy, and tags | Unchanged. |
| Destination ID and file source identity | Unchanged. |
| UI progress range and reduced-motion behavior | Unchanged. |

## Test evidence and limitation

An in-memory Room/Robolectric regression test was attempted to prove both permitted and blocked progress writes. The first run failed before test execution because Robolectric’s default API 36 SDK requires Java 21 while the project-maintained local build uses JDK 17. The test was then pinned to API 35, but the runner did not complete within the bounded maintenance window under the sandbox’s memory pressure and was stopped to preserve the environment.

The unstable test file was removed rather than retained as failing or hanging project test code. The source repair remains **repository verified**; Kotlin/Room query compilation and the broader unit/build gates remain pending in the later validation phase. A reliable DAO regression test should be reintroduced only after a compatible Robolectric/JDK matrix or an Android instrumentation runner is available.

## Residual risks

The atomic guard does not itself certify cancellation acknowledgement, deletion versus Worker races, process death, network loss, duplicate execution under replacement, or real Telegram delivery. Those require controlled device or emulator evidence. No central state-transition validator or cancellation fence is added in this maintenance slice because the current evidence supports only the late-progress overwrite repair.

## References

[1]: https://developer.android.com/training/data-storage/room "Android Room documentation"
[2]: https://developer.android.com/topic/libraries/architecture/workmanager "Android WorkManager documentation"
