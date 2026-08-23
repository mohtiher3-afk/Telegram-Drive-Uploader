# File Analysis Flow

## Actual flow

```text
UploadScreen
  ↓ user selects a content URI
UploadViewModel
  ↓ calls VideoMetadataExtractor.extractMetadata on Dispatchers.IO
VideoMetadataExtractor
  ├─ persists readable content-URI permission when possible
  ├─ queries OpenableColumns.DISPLAY_NAME and SIZE
  ├─ resolves MIME through ContentResolver, then VideoFormatSupport extension fallback
  ├─ validates supported video format
  ├─ uses MediaMetadataRetriever for duration, width, and height
  └─ constructs domain UploadTask with QUEUED status
UploadViewModel
  ↓ inserts task and enqueues the upload workflow
Room / WorkManager
  ↓
TelegramUploadEngineImpl
  ├─ parses the source URI
  ├─ stages or streams the source through StreamingFileReader
  └─ delegates genuine upload to TelegramClient
```

## Ownership table

| Concern | Current owner | Result |
|---|---|---|
| URI selection | `UploadScreen` and `UploadViewModel` | Selected URI is passed into the ViewModel |
| Persistable URI permission | `VideoMetadataExtractor` | Best-effort permission request for content URIs |
| Display name and size | `VideoMetadataExtractor` through `ContentResolver.query` | Stored in `UploadTask` |
| MIME normalization and extension policy | `VideoFormatSupport` | Deterministic supported-video decision |
| Duration and dimensions | `VideoMetadataExtractor` through `MediaMetadataRetriever` | Stored in `UploadTask` |
| Stream opening and bounded reads | `StreamingFileReaderImpl` | Upload preparation only |
| Upload and delivery confirmation | `TelegramUploadEngineImpl` and `TelegramClientImpl` | Separate from file analysis |

## Boundary assessment

The current flow is a deterministic upload-preparation pipeline, not an intelligent assistant flow. `VideoMetadataExtractor` has a real but mixed responsibility because it both extracts metadata and constructs the upload-domain object. Splitting those responsibilities would require characterization tests and is outside this package-only phase. No UI rewrite or upload behavior change is justified.
