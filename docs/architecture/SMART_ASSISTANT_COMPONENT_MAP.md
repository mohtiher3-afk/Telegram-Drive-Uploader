# Smart File Assistant Component Map

## Scope

This map covers only existing file-analysis and media-processing code. It does not treat every file-related class as a Smart Assistant component.

| Class | Current path | Responsibility | Classification | Target path | Risk |
|---|---|---|---|---|---|
| `VideoFormatSupport` | `core/util/media/VideoFormatSupport.kt` | Normalizes provider MIME values and recognizes supported video extensions | `TYPE_DETECTOR` / `FILE_UTILITY` | Keep `core/util/media` | Low |
| `VideoMetadataExtractor` | `core/util/media/VideoMetadataExtractor.kt` | Persists content URI permission, reads display name and size, detects MIME, extracts duration and dimensions, and constructs `UploadTask` | `METADATA_ANALYZER` with upload-preparation coupling | Keep `core/util/media` pending a separately tested boundary | Medium |
| `StreamingFileReader` | `data/upload/reader/StreamingFileReader.kt` | Opens content/file URIs and streams bytes through a bounded buffer for upload preparation | `FILE_UTILITY` / `UPLOAD_LOGIC` | Keep under `data/upload/reader` | Medium |
| `TelegramUploadEngineImpl` | `data/upload/TelegramUploadEngineImpl.kt` | Coordinates staging and real Telegram upload events | `UPLOAD_LOGIC` | Keep under `data/upload` | High |
| `UploadViewModel` | `feature/upload/UploadViewModel.kt` | Handles picker interaction and calls metadata extraction before queue insertion | `UI_ONLY` / orchestration | Keep under `feature/upload` | High |
| `UploadTask` | `domain/model/UploadTask.kt` | Domain persistence and upload state model containing file metadata | `GENERIC` / upload model | Keep under `domain/model` | Medium |

## Finding

No class named or behaving as a Smart Assistant, suggestion engine, recommendation engine, classifier, AI service, or machine-learning model was found. The existing media utilities perform deterministic upload preflight and metadata extraction; they do not generate recommendations or intelligent classifications.

## Safe organization decision

No new `smartassistant/` package is created because it would be empty of a real assistant implementation. `VideoFormatSupport` remains a generic media policy utility. `VideoMetadataExtractor` remains in `core.util.media` for this phase because it currently constructs the domain `UploadTask` and is directly used by `UploadViewModel`; moving it without first splitting and testing responsibilities would be a behavioral refactor, not a safe package move.
