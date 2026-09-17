# Upload reliability improvements

The primary upload flow now distinguishes a first enqueue from a retry or resume. Initial work keeps an existing active request to avoid duplicate uploads. A retry or resume replaces a cancelled or failed unique request, which prevents `ExistingWorkPolicy.KEEP` from silently preserving a terminal request and blocking future work.

The real TDLib success contract remains unchanged: progress events describe transport only, while completion requires `UpdateMessageSendSucceeded` for the selected Telegram chat and provisional message.

When diagnosing a device, the minimum useful sequence is `WORKER_ENQUEUED`, `WORKER_STARTED`, `UPLOAD_STARTED`, `UpdateFile`, and `UpdateMessageSendSucceeded`. A request that remains `ENQUEUED` is a scheduler or constraint issue; a request that starts and fails is an upload, file-access, authorization, channel-permission, or TDLib issue.
