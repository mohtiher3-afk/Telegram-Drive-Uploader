# User-Facing Limitations

The application requires an active Telegram connection before channels and groups can be searched or selected. Telegram authorization and channel visibility depend on the account’s permissions and the device’s network connection.

Uploads may remain queued when Android background restrictions, storage access, network loss, or Telegram authorization state prevent immediate execution. The application should report the actual state rather than implying that an upload completed.

The published release provides separate APKs for `arm64-v8a`, `armeabi-v7a`, and `x86_64`; users should install the artifact matching their device architecture. Background behavior may vary by Android version and device manufacturer because battery-optimization policies differ.

Pinned channels are stored locally on the device and affect search-result ordering on that device. Pinning does not change Telegram channel permissions or upload authorization.
