# Test Inventory

The current repository contains a focused JVM regression suite and one Android instrumentation smoke test. It does not contain a broad Compose UI, Room, DataStore, WorkManager test harness, or fake-Telegram integration suite yet.

| Test | Type | Location | Covers | Status | Quality |
|---|---|---|---|---|---|
| `SmartFileAssistantTest` | UNIT | `app/src/test/.../core/ai/` | Deterministic filename/keyword suggestions | Present | Good observable behavior coverage |
| `UploadTelemetryFormatterTest` | UNIT | `app/src/test/.../core/ui/components/` | Speed, ETA, stalled/terminal presentation formatting | Present | Good focused pure-function coverage |
| `VideoFormatSupportTest` | UNIT | `app/src/test/.../core/util/media/` | Common video extensions, MIME fallback, rejection | Present | Good format boundary coverage |
| `TelegramVideoMessageContentTest` | UNIT / TDLib boundary | `app/src/test/.../data/telegram/client/` | Video/document message mapping and metadata conversion | Present | Good mapping coverage; no real Telegram connection |
| `UploadWorkPolicyTest` | UNIT | `app/src/test/.../data/upload/` | Constraints and unique-work policy | Present | Good policy coverage |
| `UploadCompletionPolicyTest` | UNIT | `app/src/test/.../domain/upload/` | Confirmed-delivery completion gate | Present | Good fail-closed regression coverage |
| `TdLibRuntimeSmokeTest` | INSTRUMENTATION | `app/src/androidTest/.../tdlib/` | Native library loading and `Client.create()` runtime gate | Present | Device/emulator dependent |

The suite contains **14 JVM `@Test` methods** according to the repository’s current source inventory, plus one instrumentation test class. The exact executed count remains a CI result rather than a local claim because this checkout has no Gradle wrapper or standalone Gradle executable.
