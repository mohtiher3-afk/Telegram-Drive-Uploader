# Permission Audit

| Permission | Purpose | Minimum/API behavior | Status |
|---|---|---|---|
| `INTERNET` | Telegram/TDLib network communication | Normal permission | Required |
| `ACCESS_NETWORK_STATE` | Network constraint/state decisions | Normal permission | Required by current worker/network flow |
| `READ_MEDIA_VIDEO` | User-selected video access on modern Android | Runtime permission on applicable API levels | Required for current media flow |
| `READ_EXTERNAL_STORAGE` | Legacy media access | Capped with `maxSdkVersion=32` | Compatibility-only; retained |
| `WAKE_LOCK` | Preserve upload execution when required | Normal permission | Retained for background upload behavior |

No permission was removed or weakened without runtime evidence. Permission denial, permanent denial, and revocation remain device-test requirements.
