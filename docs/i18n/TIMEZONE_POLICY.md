# Timezone and Date/Time Policy

## Storage

Upload timestamps such as `createdAt`, `completedAt`, and `scheduledAt` are stored as `Long` epoch values. This review does not change their semantics, units, or persistence location. Scheduling delay is calculated from the stored timestamp and the current epoch time.

## Display

The upload preparation screen uses Android's locale-aware `DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)` for scheduled time display. Settings diagnostic events use `SimpleDateFormat("HH:mm:ss", Locale.getDefault())`; this is a deliberate technical clock format rather than a persisted timestamp format.

Display timezone is supplied by the device/platform APIs. The repository does not hard-code Asia/Riyadh, UTC, or another display timezone. No custom DST algorithm exists and none should be introduced while platform date/time APIs are sufficient.

## Required Test Matrix

When a device/emulator is available, compare explicit epoch inputs in UTC, Saudi Arabia, and a representative DST-observing timezone. Verify that the displayed local date/time changes appropriately while the persisted epoch and scheduler delay remain unchanged. Test dates around a DST transition and scheduled timestamps in the past and future.

## Safety Rules

Do not rewrite stored timestamps for display. Do not use local time implicitly in business calculations. Do not parse dates with fixed `MM/dd/yyyy` or `dd/MM/yyyy` assumptions. Do not infer timezone correctness from one machine's output.
