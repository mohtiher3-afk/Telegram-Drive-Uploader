# Resource Architecture

## Scope and safety

This phase reorganizes knowledge about the existing Android resources without changing application behavior. Telegram, TDLib, JNI, ABI packaging, authentication, upload logic, WorkManager, database, navigation, notifications, and file-sharing behavior are protected boundaries.

## Structure

Resources remain in Android-valid type directories. Semantic organization is expressed through names and documentation rather than arbitrary nested folders:

- `drawable/`: vector drawables, drawable wrappers, and application raster artwork.
- `drawable-nodpi/`: raster artwork whose pixel dimensions must not be density-scaled.
- `mipmap-*/`: launcher fallback assets.
- `mipmap-anydpi-v26/`: adaptive launcher definitions.
- `values/`: English strings, legacy XML colors, and the manifest theme.
- `values-ar/`: Arabic string resources with matching IDs and placeholders.
- `xml/`: backup and data-extraction policy resources referenced by the manifest.

## Naming

New resources use lowercase snake case with semantic names, such as `ic_upload`, `drawable_empty_upload`, and `shape_card`. Existing launcher names are retained because they are manifest contracts. Existing timestamped artwork is not renamed without complete reference analysis.

## Icons and assets

The application uses the existing adaptive launcher stack: background, foreground wrapper, foreground image, monochrome icon, and density fallback WebP files. No second icon framework or random replacement asset is introduced. Identical `ic_launcher`/`ic_launcher_round` bytes are retained because the resource names are distinct Android launcher contracts.

## Typography, colors, and themes

Compose `MaterialTheme.typography`, `MaterialTheme.colorScheme`, and shared theme tokens are the primary design-system sources. `values/colors.xml` is classified as legacy/review and is not deleted because generated or indirect references require a Gradle/lint-backed decision. `themes.xml` remains because the manifest references it. Dark-mode behavior currently comes from Compose light/dark color schemes and dynamic color; no `values-night` resources are present.

## Strings and RTL

English and Arabic string IDs are kept in parity. Placeholders must match exactly. User-controlled content such as filenames, Telegram names, URLs, IDs, hashes, MIME types, and diagnostic values remains content, not translatable UI. `android:supportsRtl="true"` remains enabled. Compose layouts should use logical `start`/`end` semantics and avoid physical left/right assumptions.

## XML and framework resources

Backup rules, data-extraction rules, adaptive icons, and manifest themes are protected. They are not removed based on direct Kotlin reference searches. The current inventory found no production FileProvider paths, network-security XML, notification icon resource, raw file, font, or asset-directory file in the checkout.

## Shrinking policy

Release resource shrinking and R8 are already enabled in `app/build.gradle.kts`. This phase does not add aggressive keep rules or remove resources solely to reduce APK size. Any future deletion requires source, manifest, XML, generated-resource, and CI evidence.

## Validation policy

After each meaningful resource batch, run resource compilation and Kotlin compilation, then unit tests and the multi-ABI CI workflow. Runtime validation must distinguish build verification from physical-device verification and must not claim TDLib runtime success from resource compilation alone.
