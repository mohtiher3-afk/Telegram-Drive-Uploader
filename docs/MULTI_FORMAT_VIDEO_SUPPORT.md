# Multi-format video upload support

The Android picker now allows providers to show files from any container. The app validates the selected item after selection using the provider MIME type and the filename extension. It recognizes MP4, M4V, MKV, WebM, MOV/QT, AVI, 3GP/3G2, TS/M2TS/MTS, MPEG/MPG, FLV, WMV/ASF, and OGV containers.

The app does **not** transcode or re-encode files. It copies the source to a temporary local file and sends it through official TDLib. Telegram may accept a container but still treat a particular codec or profile differently. MP4 with H.264 video and AAC audio remains the safest compatibility choice; MKV, AVI, FLV, WMV, and uncommon codecs should be tested on the target Telegram account and channel.

If an Android provider reports `application/octet-stream` or no MIME type, the app falls back to the extension. Files that are neither a recognized video MIME type nor a recognized video extension are rejected during metadata preparation rather than being mislabeled as MP4.

For every format, completion still requires TDLib's real `UpdateMessageSendSucceeded` event for the selected channel. Picker acceptance, local staging, `UpdateFile` progress, and the initial `SendMessage` response are not final delivery confirmation.
