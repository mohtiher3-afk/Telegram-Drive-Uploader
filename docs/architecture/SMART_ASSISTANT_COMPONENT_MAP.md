# Smart File Assistant Component Map

## Scope

This map covers only existing file-analysis and media-processing code. It does not treat every file-related class as a Smart Assistant component.

| Class | Current path | Responsibility | Classification | Target path | Risk |
|---|---|---|---|---|---|
| `SmartFileAssistant` | `core/ai/SmartFileAssistant.kt` | Deterministically derives a suggested filename and keywords from `UploadTask` filename and media metadata; does not inspect file contents or use a network service | `SUGGESTION_ENGINE` / `CLASSIFIER` | Keep `core/ai` for now; a future package move must update `UploadViewModel` imports only | Medium |
| `SmartFileSuggestion` | `core/ai/SmartFileAssistant.kt` | Immutable result containing task ID, suggested name, and keywords | `SUGGESTION_ENGINE` model | Keep with the existing assistant until a separately reviewed model split | Low |
| `VideoFormatSupport` | `core/util/media/VideoFormatSupport.kt` | Normalizes provider MIME values and recognizes supported video extensions | `TYPE_DETECTOR` / `FILE_UTILITY` | Keep `core/util/media` | Low |
| `VideoMetadataExtractor` | `core/util/media/VideoMetadataExtractor.kt` | Persists content URI permission, reads display name and size, detects MIME, extracts duration and dimensions, and constructs `UploadTask` | `METADATA_ANALYZER` with upload-preparation coupling | Keep `core/util/media` pending a separately tested boundary | Medium |
| `StreamingFileReader` | `data/upload/reader/StreamingFileReader.kt` | Opens content/file URIs and streams bytes through a bounded buffer for upload preparation | `FILE_UTILITY` / `UPLOAD_LOGIC` | Keep under `data/upload/reader` | Medium |
| `TelegramUploadEngineImpl` | `data/upload/TelegramUploadEngineImpl.kt` | Coordinates staging and real Telegram upload events | `UPLOAD_LOGIC` | Keep under `data/upload` | High |
| `UploadViewModel` | `feature/upload/UploadViewModel.kt` | Handles picker interaction and calls metadata extraction before queue insertion | `UI_ONLY` / orchestration | Keep under `feature/upload` | High |
| `UploadTask` | `domain/model/UploadTask.kt` | Domain persistence and upload state model containing file metadata | `GENERIC` / upload model | Keep under `domain/model` | Medium |

## Finding

A real local Smart File Assistant exists in `core/ai/SmartFileAssistant.kt`. It is deterministic and offline: `SmartFileAssistant.suggest(task)` derives a normalized filename and up to four keywords from the existing `UploadTask` filename, dimensions, duration, and creation time. `SmartFileSuggestion` is the result model. No cloud AI, LLM, machine-learning model, API key, or file-content inspection is used.

The assistant is consumed by `feature/upload/UploadViewModel`, which computes suggestions after metadata extraction and allows the user to apply one or all suggested filenames. This is existing upload UI behavior and must not be changed during structural refactoring.

## Safe organization decision

No broad move was performed yet. The current `core.ai` package is a valid generic local-assistant boundary, while `UploadViewModel` remains the feature orchestrator. Moving these classes into a new `smartassistant/` package could be safe but requires updating references and CI validation; it should be treated as a focused package-only change rather than mixed with screen reorganization. `VideoFormatSupport` remains a generic media policy utility, and `VideoMetadataExtractor` remains in `core.util.media` because it constructs `UploadTask` and owns URI/media metadata access.
