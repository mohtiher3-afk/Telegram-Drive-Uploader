from pathlib import Path
import re
import sys
import xml.etree.ElementTree as ET

root = Path(__file__).resolve().parents[1]
english = root / "app/src/main/res/values/strings.xml"
arabic = root / "app/src/main/res/values-ar/strings.xml"

for path in (english, arabic):
    ET.parse(path)

pattern = re.compile(r'<string\s+name="([^"]+)"')
def keys(path: Path) -> set[str]:
    return set(pattern.findall(path.read_text(encoding="utf-8")))

en_keys = keys(english)
ar_keys = keys(arabic)
missing_ar = sorted(en_keys - ar_keys)
if missing_ar:
    print("ARABIC_RESOURCE_PARITY=FAIL")
    print("Missing Arabic keys:", ", ".join(missing_ar))
    sys.exit(1)

queue_source = (root / "app/src/main/java/com/telegramdrive/uploader/feature/queue/QueueScreen.kt").read_text(encoding="utf-8")
for literal in ("Your queue is empty", "Queue controls", "No matching uploads", '"All"', '"Active"', '"Paused"', '"Failed"'):
    if literal in queue_source:
        print(f"QUEUE_LITERAL_SCAN=FAIL: {literal}")
        sys.exit(1)

progress_source = (root / "app/src/main/java/com/telegramdrive/uploader/core/ui/components/UploadStatusIndicator.kt").read_text(encoding="utf-8")
required = ("uploadProgressFraction", "percentage / 100f", "uploadProgressPercent")
if not all(token in progress_source for token in required):
    print("PROGRESS_CONVERSION=FAIL")
    sys.exit(1)

print("XML_PARSE=PASS")
print("ARABIC_RESOURCE_PARITY=PASS")
print("QUEUE_LITERAL_SCAN=PASS")
print("PROGRESS_CONVERSION=PASS")
