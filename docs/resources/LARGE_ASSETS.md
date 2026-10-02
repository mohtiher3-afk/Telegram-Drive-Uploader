# Large Asset Review

The threshold for this review is practical rather than destructive: assets are reported by measured size, but are not recompressed or replaced without visual and runtime evidence.

| File | Type | Size | Decision | Reason |
|---|---|---:|---|---|
| `res/drawable-nodpi/mission_control_logo.png` | PNG Mission Control logo | 3,020,017 bytes | KEEP | Current launcher, onboarding, and splash branding asset |
| `res/drawable-nodpi/ic_launcher_foreground_image.png` | Legacy PNG launcher foreground | 145,923 bytes | REMOVED | Replaced by `mission_control_logo.png` |
| `res/values/strings.xml` | XML resources | 8,334 bytes | KEEP | Not a bitmap asset; contains localized application contract |
| `res/values-ar/strings.xml` | XML resources | 10,439 bytes | KEEP | Not a bitmap asset; Arabic locale contract |

The density-specific launcher WebP files were regenerated from the current Mission Control logo and remain small. The legacy JPG and launcher foreground PNG were deleted after the source-reference audit; no unrelated assets were recompressed.
