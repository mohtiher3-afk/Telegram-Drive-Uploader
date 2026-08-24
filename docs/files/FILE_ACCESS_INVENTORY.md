# File Access Inventory

| Class or function | Purpose | Input | Output | Thread/context | Risk |
|---|---|---|---|---|---|
| `VideoMetadataExtractor.extract` | Reads display name, size, MIME, duration, width, and height | URI string and `ContentResolver` | Metadata model | Called from application upload preparation | Provider failure or missing metadata must be handled |
| `ContentResolver.takePersistableUriPermission` | Attempts to retain read access for a selected document URI | URI and read grant flag | Permission side effect | Preparation path | Requires a provider that supports persistable grants; result is not universal |
| `ContentResolver.query` | Reads `OpenableColumns.DISPLAY_NAME` and `SIZE` | URI | Cursor values | Preparation path | Null/unknown columns and provider variation |
| `ContentResolver.getType` | Reads provider MIME type | URI | MIME string or null | Preparation path | Provider may omit type |
| `MediaMetadataRetriever` | Reads video duration, width, and height | URI string | Numeric metadata | Metadata extraction path | Unsupported/corrupt media or provider access failure |
| `ContentResolver.openFileDescriptor` | Opens a descriptor for chunk reads | URI | `ParcelFileDescriptor` | Upload reader | Descriptor must be closed; current code wraps it in `AutoCloseInputStream.use` |
| `ContentResolver.openInputStream` | Opens source stream for staging | URI | `InputStream` | Upload worker/engine path | Provider availability and cancellation; current code closes with `use` |
| `StreamingFileReader.readChunk` | Reads a bounded byte range | URI, offset, size | `ByteArray` of requested chunk | Upload path | Memory is bounded by caller-provided chunk size |
| `StreamingFileReader.copyToFile` | Streams URI contents into a seekable temporary file | URI and destination `File` | Copied byte count | Upload worker path | One required staging copy; destination cleanup is required |
| `TelegramUploadEngineImpl` | Stages source and passes local path to Telegram client | `UploadTask` | Upload result flow | Worker coroutine | Temporary file lifetime and provider errors |

The inventory makes no claim that all Android document providers behave identically. Cloud-backed, revoked, moved, or unavailable URIs require device/provider testing.
