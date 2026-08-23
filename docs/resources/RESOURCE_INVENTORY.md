# Android Resource Inventory

**Audit basis:** repository source tree at the current checkout. This is an inventory and safety classification; it does not imply that any resource is safe to delete.

| Resource | Type | Location | Used by / owner | Status |
|---|---|---|---|---|
| `ic_launcher.xml` | Adaptive icon | `res/mipmap-anydpi-v26/` | Manifest `android:icon` | KEEP; framework-referenced |
| `ic_launcher_round.xml` | Adaptive icon | `res/mipmap-anydpi-v26/` | Manifest `android:roundIcon` | KEEP; framework-referenced |
| `ic_launcher.webp` / round variants | Density launcher assets | `res/mipmap-*/` | Adaptive-icon fallback and launcher compatibility | KEEP; framework-referenced |
| `ic_launcher_background.xml` | Vector/background drawable | `res/drawable/` | Adaptive launcher icon XML | KEEP |
| `ic_launcher_foreground.xml` | Drawable wrapper | `res/drawable/` | Adaptive launcher icon XML | KEEP |
| `ic_launcher_foreground_image.png` | Raster foreground asset | `res/drawable-nodpi/` | `ic_launcher_foreground.xml` | KEEP; indirectly referenced |
| `ic_launcher_monochrome.xml` | Monochrome vector | `res/drawable/` | Adaptive launcher icon XML | KEEP |
| `ic_tg_drive_uploader_1786926729865.jpg` | Raster application asset | `res/drawable/` | Requires source-reference review before any removal | REVIEW; no deletion |
| `strings.xml` | English string resources | `res/values/` | Compose `stringResource` and Android manifest | KEEP; duplicate IDs removed in prior fix |
| `strings.xml` | Arabic string resources | `res/values-ar/` | Locale-qualified Compose resources | KEEP; IDs/placeholders must stay aligned |
| `colors.xml` | Legacy XML colors | `res/values/` | No primary Compose theme ownership found; usage review required | REVIEW; no deletion |
| `themes.xml` | Android XML theme | `res/values/` | Manifest application/activity theme | KEEP; framework-referenced |
| `backup_rules.xml` | Backup policy XML | `res/xml/` | Manifest `android:fullBackupContent` | KEEP; protected system configuration |
| `data_extraction_rules.xml` | Android data-extraction policy | `res/xml/` | Manifest `android:dataExtractionRules` | KEEP; protected system configuration |

## Directory coverage

The current production resource tree contains `drawable`, `drawable-nodpi`, `mipmap-anydpi-v26`, density-specific `mipmap` folders, `values`, `values-ar`, and `xml`. There are currently no production `font`, `raw`, `values-night`, `values-night-ar`, `ldrtl`, or `assets` files in the audited checkout. Test resource directories were also checked for this inventory.

## Protected boundaries

The manifest references launcher, theme, backup, and data-extraction resources indirectly. Adaptive-icon XML references foreground, background, and monochrome drawables indirectly. These resources must not be removed based only on source-level `R` usage. Compose colors and shapes are defined in `core/ui/theme/Theme.kt`; XML colors are therefore classified for review rather than automatically deleted.
