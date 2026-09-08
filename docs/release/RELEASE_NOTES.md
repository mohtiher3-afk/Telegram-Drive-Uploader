# Release Notes

## v1.0.22

### Highlights

Fixed uploads failing right after selection on recent Android versions. Files picked through the Storage Access Framework are now snapshotted into app-owned storage at selection time, so uploads keep working after the process is killed or the device reboots.

### Bug Fixes

- **Dead content grants**: A `content://` URI picked via SAF loses its read grant once the process dies. The upload worker previously hit an unreadable source, treated it as a transient error, and burned all five retries without ever starting a transfer. The source is now copied to `cacheDir/staged-uploads` while the grant is still live, and the upload runs from that owned file.
- **Fail fast on unreadable sources**: If a source can no longer be opened, the reader throws `FileNotFoundException` (non-retryable) with a clear "re-select the file" message instead of looping through retries.
- **Duplicate selection**: Re-selecting the same URI in one batch is now deduplicated.

### Improvements

- Prepared uploads report an accurate `totalBytes` from the staged snapshot.
- Added regression coverage: a non-retryable source-open failure test and a `StreamingFileReader` copy test.

### Security

- **No session backup**: `android:allowBackup` is set to `false`, excluding TDLib session and database files from cloud backup and device-to-device transfer. Re-authentication is required after a fresh install.

### Performance

No new performance measurements or speculative optimizations were introduced.

### Known Limitations

- The release workflow still requires the configured GitHub signing secrets and a `v*` tag push.
- Real Telegram authentication, upload, and device background testing remain separate validation steps.
- Staged snapshots live in `cacheDir`; the OS may evict them under storage pressure, which surfaces as a non-retryable "re-select the file" error rather than a silent failure.
