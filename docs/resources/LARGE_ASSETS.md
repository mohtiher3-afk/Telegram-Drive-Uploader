# Large Asset Review

The threshold for this review is practical rather than destructive: assets are reported by measured size, but are not recompressed or replaced without visual and runtime evidence.

| File | Type | Size | Decision | Reason |
|---|---|---:|---|---|
| `res/drawable/ic_tg_drive_uploader_1786926729865.jpg` | JPEG application image | 577,141 bytes | REVIEW / KEEP | Largest production image; purpose and source reference require confirmation before optimization |
| `res/drawable-nodpi/ic_launcher_foreground_image.png` | PNG launcher foreground | 145,923 bytes | KEEP | Indirectly referenced by adaptive launcher foreground; quality and transparent bounds matter |
| `res/values/strings.xml` | XML resources | 8,334 bytes | KEEP | Not a bitmap asset; contains localized application contract |
| `res/values-ar/strings.xml` | XML resources | 10,439 bytes | KEEP | Not a bitmap asset; Arabic locale contract |

The density-specific launcher WebP files are all small (944–4,736 bytes each) and remain unchanged. No large asset was automatically reduced because the supplied instructions explicitly prohibit blind recompression and branding changes.
