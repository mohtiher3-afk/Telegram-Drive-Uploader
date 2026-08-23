# Unused Resource Review

The audit follows a conservative rule: a resource is not removed merely because a direct Kotlin reference was not found. Manifest, adaptive-icon, Android framework, generated, and indirect references are treated as live until proven otherwise.

| Resource or group | Evidence | Classification | Action |
|---|---|---|---|
| `@mipmap/ic_launcher`, `@mipmap/ic_launcher_round` | Direct manifest references | KEEP | No change |
| `mipmap-anydpi-v26/ic_launcher*.xml` | Manifest-selected adaptive icon resources | KEEP | No change |
| `ic_launcher_background`, `ic_launcher_foreground`, `ic_launcher_monochrome` | Adaptive icon XML references | KEEP | No change |
| `ic_launcher_foreground_image.png` | Referenced by `ic_launcher_foreground.xml` | KEEP | No change |
| Density-specific launcher WebP files | Launcher fallback resources | KEEP | No change |
| `backup_rules.xml`, `data_extraction_rules.xml` | Direct manifest references | KEEP | No change |
| `themes.xml` | Direct manifest theme reference | KEEP | No change |
| `values/strings.xml`, `values-ar/strings.xml` | Compose and manifest resource usage | KEEP | No change |
| `colors.xml` | Legacy-looking names; Compose theme is primary source | REVIEW | Do not delete without full generated-resource/lint evidence |
| `ic_tg_drive_uploader_1786926729865.jpg` | Requires source and indirect-reference confirmation | REVIEW | Do not delete |

No resource is classified `SAFE_TO_REMOVE` in this phase. Android lint/resource-shrinking reports were not available in the temporary checkout because the repository does not include the Gradle wrapper; CI remains the authoritative build gate. The absence of a local report is not evidence that a resource is unused.

## Dynamic and indirect access

No `Resources.getIdentifier()` call was found in the audited application source. Resource access is primarily generated `R` access, Compose resource APIs, manifest references, and adaptive-icon XML references. Reflection-sensitive library resources and generated resources remain outside the deletion scope.
