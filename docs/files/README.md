# File Handling and Large-File Reliability

This directory documents the existing file-selection, URI, metadata, staging, streaming, and cleanup path. It does not authorize changes to upload semantics.

| Document | Purpose |
|---|---|
| `FILE_FLOW.md` | End-to-end file lifecycle from picker to Telegram and cleanup. |
| `FILE_ACCESS_INVENTORY.md` | Actual file APIs, inputs, outputs, contexts, and risks. |
| `LARGE_FILE_MEMORY_AUDIT.md` | Whole-file buffering and bounded-memory review. |
| `TEMPORARY_FILE_POLICY.md` | Staged-file ownership, lifetime, deletion, and orphan limitations. |
| `FINAL_FILE_RELIABILITY_REPORT.md` | Final findings and unverified runtime/provider cases. |

Future changes involving picker behavior, URI persistence, MIME, filenames, metadata, streams, buffers, temporary files, or large-file upload preparation require focused evidence and regression tests.
