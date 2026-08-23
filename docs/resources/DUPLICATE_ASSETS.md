# Duplicate Asset Review

The audit found byte-identical pairs among launcher resources. These are not removed because Android resource qualifiers and manifest compatibility can require both names even when their bytes match.

| Duplicate group | Evidence | Classification | Action |
|---|---|---|---|
| `mipmap-mdpi/ic_launcher.webp` and `ic_launcher_round.webp` | Identical SHA-256 | Framework/launcher compatibility | KEEP both |
| `mipmap-hdpi/ic_launcher.webp` and `ic_launcher_round.webp` | Identical SHA-256 | Framework/launcher compatibility | KEEP both |
| `mipmap-xhdpi/ic_launcher.webp` and `ic_launcher_round.webp` | Identical SHA-256 | Framework/launcher compatibility | KEEP both |
| `mipmap-xxhdpi/ic_launcher.webp` and `ic_launcher_round.webp` | Identical SHA-256 | Framework/launcher compatibility | KEEP both |
| `mipmap-xxxhdpi/ic_launcher.webp` and `ic_launcher_round.webp` | Identical SHA-256 | Framework/launcher compatibility | KEEP both |
| `mipmap-anydpi-v26/ic_launcher.xml` and `ic_launcher_round.xml` | Identical SHA-256 | Distinct manifest resource names | KEEP both |

No near-duplicate image removal was performed. The launcher naming pair is semantically meaningful to Android even when the current visual asset is shared.
