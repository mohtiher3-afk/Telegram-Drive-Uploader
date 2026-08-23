# Localization Audit

The English resource file already contains the main navigation, upload, Telegram, history, settings, and onboarding labels. The source audit also found user-visible English literals in Compose screens that should be moved to resources. The audit distinguishes UI text from user content and technical values.

| Location | Current Text or Pattern | User Visible | Classification | Resource Needed | Priority |
|---|---|---:|---|---|---:|
| `feature/home/HomeScreen.kt` | `"Connected"` content description | Yes | UI_TEXT | Yes | Medium |
| `feature/home/HomeScreen.kt` | Dynamic Telegram user name and `@username` | Yes | TELEGRAM_CONTENT | No translation | High |
| `feature/upload/UploadScreen.kt` | `"Choose"` / `"Change"` schedule actions | Yes | UI_TEXT | Yes | High |
| `feature/upload/UploadScreen.kt` | `"Total size: %s"` | Yes | UI_TEXT | Yes, parameterized | High |
| `feature/history/HistoryScreen.kt` | `"%s matches · %s"` | Yes | UI_TEXT with quantity | Yes, plural-aware review | High |
| `feature/history/HistoryScreen.kt` | Elapsed format `%dm %02ds` / `%ds` | Yes | TECHNICAL_PRESENTATION | Locale-safe presentation review | Medium |
| `feature/settings/SettingsScreen.kt` | Diagnostic timestamp `HH:mm:ss` | Yes | TECHNICAL_PRESENTATION | Locale-safe time review | Medium |
| `feature/onboarding/OnboardingScreen.kt` | Existing page copy and permission labels | Yes | UI_TEXT | Already resource-backed in part; audit completion needed | High |
| `core` and repositories | IDs, MIME values, URI strings, filenames, Telegram usernames | Sometimes | TECHNICAL_VALUE / USER_CONTENT / FILE_NAME | No translation | Critical |
| Logs and diagnostic exports | Event names and technical messages | User-visible when exported | LOGGING | Preserve technical values; localize only explanatory UI wrapper | Medium |

The first safe extraction target is the small set of hardcoded labels and parameterized summaries in feature screens. Telegram names, channel names, usernames, filenames, URLs, IDs, hashes, MIME types, and API values must not be translated.

`AndroidManifest.xml` already declares `android:supportsRtl="true"`; no manifest change is required for RTL readiness.
