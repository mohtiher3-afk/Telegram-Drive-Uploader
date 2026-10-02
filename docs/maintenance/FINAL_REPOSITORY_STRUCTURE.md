# Final Repository Structure

**Status: CURRENT — structural snapshot for the cleanup phase.**

```text
.
├── .github/
│   ├── ISSUE_TEMPLATE/
│   ├── pull_request_template.md
│   └── workflows/
├── app/
│   └── src/
│       ├── main/
│       │   ├── java/com/telegramdrive/
│       │   ├── java/org/drinkless/tdlib/
│       │   ├── jniLibs/
│       │   │   ├── arm64-v8a/
│       │   │   ├── armeabi-v7a/
│       │   │   └── x86_64/
│       │   └── res/
│       │       ├── drawable/
│       │       ├── drawable-nodpi/
│       │       ├── mipmap-* /
│       │       ├── values/
│       │       ├── values-ar/
│       │       └── xml/
│       ├── test/java/com/telegramdrive/
│       └── androidTest/java/com/telegramdrive/
├── design/
├── docs/
│   ├── architecture/
│   ├── ci/
│   ├── design/
│   ├── features/
│   ├── final-audit/
│   ├── localization/
│   ├── maintenance/
│   ├── operations/
│   ├── performance/
│   ├── release/
│   ├── resources/
│   ├── security/
│   ├── testing/
│   ├── DEVELOPER_ONBOARDING.md
│   ├── MANUS_DEVELOPMENT_PROTOCOL.md
│   ├── README.md
│   └── TDLIB_ARTIFACT_MANIFEST.md
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/
├── scripts/
├── .env.example
├── .gitignore
├── build.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
├── README.md
├── settings.gradle.kts
└── todo.md
```

Ignored local directories such as `.native-build/`, `.gradle/`, `build/`, and `app/build/` are intentionally omitted from the tracked structure. The repository contains one Gradle module, `:app`; no `buildSrc` or convention-plugin directory exists.
