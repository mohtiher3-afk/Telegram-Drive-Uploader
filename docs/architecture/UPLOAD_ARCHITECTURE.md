# Upload Architecture

## Actual flow

```text
Android file picker
    ↓
UploadViewModel
    ↓
UploadManagerImpl
    ↓
Room UploadRepositoryImpl + WorkManager unique work
    ↓
UploadWorker
    ↓
TelegramUploadEngineImpl
    ↓
TelegramRepositoryImpl / TelegramClientImpl
    ↓
TDLib UploadFile and SendMessage
    ↓
UpdateFile progress + UpdateMessageSendSucceeded confirmation
```

The queue persists upload metadata in Room. WorkManager starts the worker under its network constraint. The worker validates the source URI, stages or streams the file through the bounded reader, updates progress, and returns retry or failure according to the typed result. Pause, resume, cancellation, scheduling, duplicate protection, and cleanup are high-risk behaviors and require characterization tests before refactoring.

Progress is transport telemetry. Completion is separate and must be persisted only after the matching `UpdateMessageSendSucceeded`; `UpdateMessageSendFailed` is terminal failure. No progress event or provisional `Message` callback is a success substitute.
