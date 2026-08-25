from pathlib import Path
from PIL import Image

project = Path('/home/ubuntu/package-work/telegram-project')
source = project / 'design' / 'app_icon_concept.png'
image = Image.open(source).convert('RGB')

# Legacy launcher densities use square raster assets. Keep the full concept intact.
for density, size in {
    'mdpi': 48,
    'hdpi': 72,
    'xhdpi': 96,
    'xxhdpi': 144,
    'xxxhdpi': 192,
}.items():
    out_dir = project / 'app/src/main/res' / f'mipmap-{density}'
    out_dir.mkdir(parents=True, exist_ok=True)
    rendered = image.resize((size, size), Image.Resampling.LANCZOS)
    rendered.save(out_dir / 'ic_launcher.webp', 'WEBP', quality=96, method=6)
    rendered.save(out_dir / 'ic_launcher_round.webp', 'WEBP', quality=96, method=6)

# Adaptive foreground is kept inside the safe area by using a smaller rendered image
# inside the drawable. The adaptive background is supplied separately in XML.
foreground_dir = project / 'app/src/main/res/drawable-nodpi'
foreground_dir.mkdir(parents=True, exist_ok=True)
foreground = image.resize((432, 432), Image.Resampling.LANCZOS)
foreground.save(foreground_dir / 'mission_control_logo.png', 'PNG', optimize=True)
