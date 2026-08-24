# Privacy Governance Record

## Classification

The application handles internal operational data, personal file metadata, sensitive file content during upload, Telegram identity and destination data, authentication/session state, and credential or secret boundaries. Classification is based on actual application behavior and does not treat all metadata as harmless.

## Access

The Android application accesses user-selected media through Android picker/content-resolver permissions and its own local persistence. TDLib accesses Telegram authorization and destination state required to perform the Telegram function. Support access is limited to voluntarily shared, sanitized diagnostic evidence. No application code grants an external component access to private file contents.

## Transmission

File contents and required metadata are transmitted to Telegram through TDLib when the user initiates or schedules an upload. Network state is read for operation handling. No third-party analytics, advertising, remote logging, or crash-reporting data service was identified. The repository therefore records: **NO THIRD-PARTY DATA SERVICE IDENTIFIED**.

## Permissions

| Permission | Purpose | Requested/used for | Optional or conditional | Current status |
|---|---|---|---|---|
| `INTERNET` | Telegram/TDLib network communication | Authentication, destination operations, upload | Core functionality | Required |
| `ACCESS_NETWORK_STATE` | Observe high-level connectivity | Queue/upload constraints and diagnostics | Operational | Required by current behavior |
| `READ_MEDIA_VIDEO` | Access selected video media on newer Android versions | User-selected upload content | Requested only when applicable | Required for supported picker path |
| `READ_EXTERNAL_STORAGE` up to API 32 | Access selected media on older Android versions | User-selected upload content | Version-conditional | Required only on applicable API levels |
| `WAKE_LOCK` | Support background upload work | WorkManager/upload lifecycle | Operational | Required by current background behavior |

No permission is approved for broader collection than its documented purpose. Permission timing and user-facing rationale remain governed by the onboarding and Android permission flow documentation.

## Notifications and Screen Privacy

Notifications must avoid unnecessary private filenames, destination names, authentication information, or file contents. The reviewed manifest does not expose application components beyond the launcher activity and required provider configuration. A global `FLAG_SECURE` policy was not added because no specific screen requirement was established.

## Backup and Storage

Room and `datastore/` are excluded from Android cloud backup and device transfer by the existing XML rules. This prevents silent backup of those stores but does not establish a recovery copy. No encryption change or undocumented copying mechanism is introduced by this review.

## Governance for Future Changes

Any change affecting user data, Telegram data, file metadata/content, queue/history, scheduler, session state, diagnostics, notifications, backups, storage, network transmission, permissions, or retention requires a privacy impact review. The review must state what data changes, why it is needed, where it is stored, who can access it, when it is transmitted, how long it is retained, when it is deleted, whether it is logged, and whether third parties receive it.
