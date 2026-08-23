# Final Repository Structure

```text
app/
  src/main/java/com/telegramdrive/uploader/
    core/                 # shared utilities, diagnostics, navigation, theme
    data/                 # local persistence, Telegram boundary, upload data
    domain/               # models, repositories, upload contracts
    feature/              # onboarding, home, Telegram, upload, queue, history, settings
  src/main/res/            # values, values-ar, icons, XML backup resources
  src/test/                # JVM unit tests
  src/androidTest/         # TDLib runtime smoke test
scripts/                   # artifact, resource, WorkManager, security, CI checks
.github/
  workflows/               # android-ci, device smoke, manual release
  pull_request_template.md
docs/
  architecture/ design/ localization/ resources/ testing/
  performance/ security/ ci/ final-audit/
```

This reflects the actual architecture; no artificial package split was introduced during final audit.
