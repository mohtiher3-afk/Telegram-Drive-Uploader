# Support Guide

## Authentication and Telegram Connection

Confirm the device has network access, the app is updated, and Telegram authorization reaches a supported state. Ask for the app version, Android version, device/ABI, visible error, and reproduction steps. Never ask for API hashes, bot tokens, passwords, login codes, or session files.

## Upload Failure

Check the displayed upload state, file accessibility, file size/type, Telegram connection, and whether the worker has started. Export sanitized diagnostics if available. A failed or queued item should remain truthful; do not ask the user to delete application data as a first response.

## Queue Problems

Check whether the item is queued, running, paused, cancelled, completed, or failed. Confirm storage permission and network constraints. Reproduce with a small non-sensitive file before investigating a large upload.

## Notifications and Background Upload

Check notification permission where applicable, battery-optimization restrictions, and whether Android has stopped background work. Record the device manufacturer and Android version because behavior varies by device. Do not claim background completion without a visible status or diagnostic event.

## Storage

Confirm the selected file remains readable and that the device has sufficient free space. Do not request the file contents or copy private files into logs.

## Network Problems

Confirm connectivity and whether a VPN, captive portal, or restrictive network is involved. Record network condition only at a high level. The application must not claim that multiple Wi-Fi links are bonded unless a measured implementation exists.
