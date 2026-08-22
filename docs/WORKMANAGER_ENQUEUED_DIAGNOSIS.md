# WorkManager Enqueued diagnosis

The upload flow inserts the task and enqueues a `OneTimeWorkRequest<UploadWorker>`. The worker is registered through `@HiltWorker`, `HiltWorkerFactory`, and `TelegramDriveApp : Configuration.Provider`; the manifest removes the default initializer so the application-provided Hilt configuration is used.

The request currently requires both `NetworkType.CONNECTED` and `requiresBatteryNotLow=true`. A task remains `ENQUEUED` while either constraint is unmet. This is the most concrete application-level explanation for a task that is inserted successfully but never reaches `WORKER_STARTED`, especially on Android 16 devices with battery saver, low battery, or OEM background restrictions. The upload is already gated by real TDLib authorization and should not be changed to bypass the network constraint.

`ExistingWorkPolicy.KEEP` can also preserve an existing work spec with the same upload ID, but it is correct for avoiding duplicate work on a fresh task. It should not be replaced globally because replacing an active upload could cancel a genuine transfer.

The diagnostics export has a separate defect: it hardcodes `App Version: 1.0.0 (Release)` even though the Android release metadata is newer. The diagnostic event store is in-memory, so events before process death are not available after restart. These two facts make the provided export insufficient to prove the worker never ran.

Planned evidence-based changes: remove the unnecessary battery-not-low constraint while retaining network connectivity, log the WorkManager request identity and effective delay without secrets, export `BuildConfig.VERSION_NAME`, and add tests around the work-request policy and diagnostic version output. Real device validation must inspect WorkManager state and Logcat under the same battery/network conditions.
