# Notification Architecture

## Current Repository State

No upload notification architecture was identified. The repository contains no upload notification channel, builder, stable notification ID, progress update, completion/failure notification, notification action, foreground service, `setForeground`, or `ForegroundInfo` path in the reviewed Android source and manifest.

## Consequence

Upload status is communicated through the Room-backed queue and history UI. The Worker writes status/progress to Room, and Compose observes those records. There is no independent notification state that can report `COMPLETED` while the queue reports `UPLOADING`, or remain visible after cancellation.

## Permissions and Privacy

Because no upload notification is implemented, `POST_NOTIFICATIONS` is not declared or requested in the reviewed path, and notification lock-screen content, channel importance, vibration, sound, visibility, channel migration, and action idempotency are not applicable. This document does not recommend adding notification functionality; doing so would be a new feature requiring a separate approved request and test plan.

## Background Visibility Limitation

The absence of a foreground notification means that long-running upload visibility outside the app is not established by this repository review. This is documented as a product/runtime limitation, not treated as a successful notification implementation.
