# Dependency Policy

Do not update a dependency merely because a newer version exists. Before any update, review security advisories, changelog and breaking changes, Android compatibility, Kotlin and Compose compatibility, Gradle/AGP compatibility, and TDLib compatibility.

Use this sequence: inventory current versions; review upstream changes; assess risk; update one logical group; compile; run unit tests; run lint; run security/resource/WorkManager/TDLib checks; build a release candidate; and run relevant regression tests. Keep changes focused so a regression can be attributed and rolled back.
