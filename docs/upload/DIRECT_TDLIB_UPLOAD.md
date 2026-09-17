# Direct TDLib channel uploads

The Android app now uploads directly from the authenticated Telegram account through official TDLib. The runtime path is:

```text
Android content URI → local TDLib staging file → PreliminaryUploadFile → SendMessage → selected Telegram channel
```

This path does not require a relay server, `TELEGRAM_BOT_TOKEN`, `TELEGRAM_CHANNEL_ID`, or `UPLOAD_API_KEY`. It is therefore suitable for free operation and avoids exposing a bot token in the APK.

## Channel selection

After TDLib reaches `AUTHORIZED`, the app loads chats from the main chat list. Channels and chats where the account has permission to send messages appear in the destination selector. Select the target channel there; the app persists its numeric TDLib `chat.id` in the upload task.

For a private channel, the signed-in account must be a member and have permission to post. A private invite link is not entered as a channel ID. The bot relay is not involved in this direct mode.

## Upload implementation

The worker copies the Android content URI to a temporary app-private file using a 1 MiB buffer. TDLib then performs the network transfer. Video MIME types use `FileTypeVideo`; all other permitted files use `FileTypeDocument`. TDLib `UpdateFile.remote.uploadedSize` drives real progress reporting, and completion is recorded only after `SendMessage` returns a `Message`.

The worker remains fail-closed. Missing native TDLib binaries, an unauthenticated account, an invalid destination, an unreadable source URI, or a TDLib error produces a real failed/retryable result rather than simulated success.

## Performance notes

The direct path avoids an extra server hop and does not load the entire file into memory. It uses one local staging pass followed by TDLib’s upload scheduler at priority 32. WorkManager retries transient failures. The operating system may still pause background work under aggressive battery restrictions; users should disable battery optimization for this app when uploading large files.

## Security note

The TDLib API ID and hash remain Android application credentials and must not be confused with a Bot Token. The user’s Telegram session database remains inside the app-private storage directory. Never log authorization codes, passwords, session paths, or native credentials.
