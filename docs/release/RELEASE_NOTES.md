# Release Notes

## v1.0.23 (2026-09-11)

> **Signing key changed.** v1.0.23 is signed with a new release key (SHA-256 `632aea289bfc8d9a96c42b976e665479d687b8822a739c2bedbf27b9563af903`). Devices on older release-signed builds must uninstall first; direct upgrade is blocked by Android signature rules.

### Engineering / Toolchain

Full build-stack update to the current stable line (AGP 9, Gradle 9, JDK 21, API 37):

- **Toolchain**: Android Gradle Plugin 9.4.0, Gradle 9.6.0, JDK 21, compileSdk 37 (targetSdk stays 36 for the Play deadline), build-tools 36.0.0, NDK 26.3.
- **Kotlin/AGP 9**: The `org.jetbrains.kotlin.android` plugin is removed; AGP 9's built-in Kotlin compiles the module. The Compose compiler plugin and KSP 2.3.11 continue to run alongside it.
- **Dependency refresh**: Hilt 2.60.1, KSP 2.3.11, androidx.hilt 1.4.0 / hilt-navigation-compose 1.4.0, Compose BOM 2026.08.00, core-ktx 1.19.0, lifecycle 2.11.0, navigation 2.10.0, Room 2.8.4, WorkManager 2.11.2, DataStore 1.2.1, core-splashscreen 1.2.0, activity-compose 1.13.0, Coroutines 1.11.0, coil3 3.6.2, Roborazzi 1.74.0, androidx.test core/runner 1.7.0.
- **Coil 3**: `coil.compose.AsyncImage` moved to `coil3.compose.AsyncImage` (`io.coil-kt.coil3`). Only a local-file loader is used, so no network artifact is pulled.
- **hiltViewModel**: imports moved to `androidx.hilt.lifecycle.viewmodel.compose` (deprecation cleanup in all screens and navigation).

### Bug Fixes

- **Permanent upload failures after file selection**: Staging and compressed scratch files were written to `cacheDir`, which the OS may evict at any time. Once evicted, every queued upload for that session hit `FileNotFoundException` on the worker's first attempt and failed permanently — there was no source left to re-stage. These files are now written to `filesDir/staged-uploads` and `filesDir/compressed`, which are durable. Owned files are explicitly deleted on terminal worker states (COMPLETED, CANCELLED) and when tasks are removed or cancelled from the queue or history. This resolves the reported cluster of 12 consecutive `SOURCE_FILE_UNAVAILABLE` failures.

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
- Staged snapshots now live in `filesDir` and are explicitly cleaned on terminal states; cache eviction no longer causes silent upload failure.
