# File, History, Scheduler, and Notification Audit — August 2026

## Repository-verified flow

| Boundary | Current behavior | Evidence |
|---|---|---|
| Video preparation | The metadata extractor attempts a persistable read grant for `content://` URIs, reads provider metadata, accepts supported video MIME/extension combinations, and releases `MediaMetadataRetriever`. | `core/util/media/VideoMetadataExtractor.kt` |
| Streaming and staging | The reader copies source content using a bounded 1 MiB buffer. The upload engine stages a temporary file, streams TDLib events, and deletes the staged file in `finally`. | `data/upload/reader/StreamingFileReader.kt`; `TelegramUploadEngineImpl.kt` |
| Video versus document | TDLib content selection depends on the current task MIME type; unsupported source formats fail before queue insertion. | `TelegramClientImpl.kt`; `VideoMetadataExtractor.kt` |
| History ownership | History is a filtered projection of Room uploads with `COMPLETED` status. Deletion targets an individual record or completed records only. | `feature/history/HistoryViewModel.kt` |
| Schedule handoff | Scheduling is an initial WorkManager delay stored with the task; the queue persists the task before scheduling. | `UploadViewModel.kt`; `UploadManagerImpl.kt` |
| Terminal notification policy | Only `COMPLETED` and final `FAILED` map to notification events. The Worker invokes the notifier at those terminal writes only. | `UploadEventNotificationPolicy.kt`; `UploadWorker.kt` |
| Android notification privacy | The notifier checks Android 13 permission, checks notification enablement, uses private visibility, and displays localized generic text only. | `AndroidUploadEventNotifier.kt`; `AndroidManifest.xml` |

## Confirmed low-risk correction

`VideoMetadataExtractor` used raw `printStackTrace()` calls when provider or media metadata extraction failed. Exception messages and stack traces can contain provider-specific context and are not appropriate for user-device logs in this flow.

The raw prints are replaced with bounded diagnostics that contain only a general state message, the opaque upload ID, and a stable error code. The fallback metadata values, URI, MIME validation, queue insertion, and upload behavior are unchanged.

## Existing automated evidence

`UploadEventNotificationPolicyTest` proves that completed and failed states emit their corresponding event and all active, paused, retrying, queued, and cancelled states emit none. The policy test does not prove Android permission grant, channel display, notification delivery, or the user’s system-level notification settings.

## Runtime limits

| Scenario | Current status |
|---|---|
| Provider retains read access through restart | Not verified on a device/provider. |
| Large-file memory and temporary-file cleanup under interruption | Not verified at runtime. |
| Scheduler behavior after reboot/Doze | Not verified. |
| Android 13 notification permission and posted notification | Not verified on a device. |
| History deletion while a Worker is actively settling | Not verified. |
| Real video/doc upload and Telegram delivery | Not verified; requires configured credentials, test account, channel, and non-sensitive test media. |

## References

[1]: https://developer.android.com/training/data-storage/shared/documents-files "Android document and file access"
[2]: https://developer.android.com/develop/ui/views/notifications/notification-permission "Android notification permission"
[3]: https://developer.android.com/topic/libraries/architecture/workmanager "Android WorkManager documentation"
