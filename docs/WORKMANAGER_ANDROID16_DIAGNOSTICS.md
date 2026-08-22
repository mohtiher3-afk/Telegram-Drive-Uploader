# WorkManager Android 16 diagnostics

The app records `WORKER_ENQUEUED` when WorkManager accepts a unique upload request. v1.0.11 also records one asynchronous state snapshot immediately afterward. This snapshot is diagnostic evidence only; it does not declare the upload successful.

## Required transition

A real upload should produce this sequence:

```text
UPLOAD_CREATED
WORKER_ENQUEUED
WORKER_ENQUEUED: WorkManager state after enqueue: ENQUEUED; runAttemptCount=0
WORKER_STARTED
UPLOAD_STARTED
... TDLib progress ...
UPLOAD_COMPLETED
WORKER_STOPPED
```

`ENQUEUED` means WorkManager is waiting for its constraints or scheduler. `RUNNING` means `UploadWorker.doWork()` has begun. `UPLOAD_COMPLETED` is valid only after the TDLib engine receives the genuine `UpdateMessageSendSucceeded` confirmation.

## Physical-device test on Android API 36

Install the signed `v1.0.11` APK on a physical API 36 device, keep the device connected to a validated network, and create one small test upload. Export diagnostics immediately after creation, then again after one minute, and again after the upload finishes or fails. Confirm that `WORKER_STARTED` follows `WORKER_ENQUEUED`. If it does not, collect filtered Logcat for `WM-`, `WorkManager`, `UploadWorker`, `Hilt`, and `TelegramDrive`.

Do not infer a fix from `WORKER_ENQUEUED` alone. If the state remains `ENQUEUED`, record whether the device has network connectivity, battery-saver restrictions, app background restrictions, a scheduled initial delay, or stale unique work using `ExistingWorkPolicy.KEEP`. If the state becomes `RUNNING` but `WORKER_STARTED` is absent, investigate worker construction or process startup. If `WORKER_STARTED` appears but TDLib confirmation does not, investigate authentication, source-file access, TDLib updates, or Telegram delivery—not WorkManager scheduling.

Never share exported logs containing phone numbers, login codes, API hashes, bot tokens, filenames, or session identifiers.
