# Critical User Flows

## Primary upload flow

Fresh install enters the existing startup/onboarding gate. The user may authenticate through the existing Telegram state machine, reach Home, select a Telegram destination, select supported media, prepare metadata, add work to the local queue, and observe real TDLib-backed progress. Completion is valid only after confirmed Telegram delivery; local staging alone is not success. History reflects persisted upload state.

## Background upload flow

The upload request is persisted and enqueued through WorkManager. The worker uses the existing constraints and retry policy. Automated tests must control the worker boundary rather than depend on wall-clock sleeps or a real Telegram account. Physical validation must check app backgrounding, relaunch, progress recovery, and terminal state using a controlled test account/channel.

## Authentication flow

The existing TDLib authorization states include logged out, connecting, phone number, verification code, two-step password, QR/other-device confirmation where supported, ready, closing, and closed/error states. Automated tests should fake the client boundary; they must not perform real login.

## Existing feature paths

The repository has routes and screens for onboarding, Home, Telegram authentication, destination selection, upload preparation, queue, history, and settings. Scheduler and notification behavior must be tested only where the current source implements it. No test document claims an end-to-end feature that was not found in source inventory.

## Manual-only evidence

JNI loading, `Client.create()`, real authorization, channel permission, actual upload delivery, and Android lifecycle behavior require an emulator or physical device. Build success cannot substitute for these runtime claims.
