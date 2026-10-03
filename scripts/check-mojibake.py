#!/usr/bin/env python3
"""Detect and optionally repair double-encoded (mojibake) UTF-8 text.

The repository previously shipped resource strings where characters such as
'...' and the middle dot were decoded as Windows-1252 and re-encoded as UTF-8,
producing sequences like 'a-circumflex euro ellipsis' and 'A-circumflex middle
dot'.  This checker looks for maximal runs of non-ASCII characters that can be
recovered by CP1252 -> UTF-8 decoding and fails when any are found, so the same
corruption cannot silently return.

Usage:
    python3 scripts/check-mojibake.py            # scan tracked text files
    python3 scripts/check-mojibake.py --fix      # repair files in place
    python3 scripts/check-mojibake.py FILE...    # explicit files
"""

from __future__ import annotations

import subprocess
import sys
from pathlib import Path

TEXT_EXTENSIONS = {
    ".xml",
    ".md",
    ".kt",
    ".kts",
    ".java",
    ".yml",
    ".yaml",
    ".json",
    ".properties",
    ".txt",
    ".toml",
    ".gradle",
    ".sh",
    ".ps1",
    ".py",
}

EXCLUDED_PREFIXES = (
    "docs/archive/",
    ".opencode/",
    "build/",
    ".gradle/",
)

REPLACEMENT_CHARACTER = "\ufffd"


def cp1252_high_byte(character: str) -> int | None:
    """Return the CP1252 byte for a non-ASCII character, or None."""
    try:
        encoded = character.encode("cp1252")
    except UnicodeEncodeError:
        return None
    if len(encoded) != 1 or encoded[0] < 0x80:
        return None
    return encoded[0]


def repair_group(group: str) -> str | None:
    """Return repaired text, or None when the group is not double-encoded."""
    try:
        decoded = group.encode("cp1252").decode("utf-8")
    except (UnicodeEncodeError, UnicodeDecodeError):
        return None
    return decoded if decoded != group else None


def repair_line(line: str) -> tuple[str, list[str]]:
    """Repair every repairable CP1252 run in a line; return text and findings."""
    findings: list[str] = []
    output: list[str] = []
    index = 0
    length = len(line)

    while index < length:
        character = line[index]
        if character == REPLACEMENT_CHARACTER:
            findings.append(character)
            output.append(character)
            index += 1
            continue

        if cp1252_high_byte(character) is None:
            output.append(character)
            index += 1
            continue

        start = index
        while index < length and cp1252_high_byte(line[index]) is not None:
            index += 1

        group = line[start:index]
        repaired = repair_group(group)
        if repaired is None:
            output.append(group)
        else:
            findings.append(group)
            output.append(repaired)

    return "".join(output), findings


def tracked_text_files() -> list[Path]:
    raw = subprocess.check_output(["git", "ls-files", "-z"]).decode("utf-8")
    files: list[Path] = []
    for name in raw.split("\0"):
        if not name:
            continue
        normalized = name.replace("\\", "/")
        if normalized.startswith(EXCLUDED_PREFIXES):
            continue
        if Path(normalized).suffix.lower() not in TEXT_EXTENSIONS:
            continue
        files.append(Path(normalized))
    return files


def decode_text(path: Path) -> tuple[str, bool] | None:
    data = path.read_bytes()
    had_bom = data.startswith(b"\xef\xbb\xbf")
    try:
        return data.decode("utf-8-sig"), had_bom
    except UnicodeDecodeError:
        return None


def encode_text(text: str, had_bom: bool) -> bytes:
    payload = text.encode("utf-8")
    return (b"\xef\xbb\xbf" + payload) if had_bom else payload


def scan(path: Path, fix: bool) -> int:
    decoded = decode_text(path)
    if decoded is None:
        return 0

    text, had_bom = decoded
    repair_all = fix
    repaired_lines: list[str] = []
    findings: list[tuple[int, str, str]] = []

    for number, line in enumerate(text.split("\n"), start=1):
        repaired, line_findings = repair_line(line)
        if line_findings:
            findings.append((number, line.strip(), repaired.strip()))
        repaired_lines.append(repaired if repair_all else line)

    if not findings:
        return 0

    print(f"{path}: {len(findings)} mojibake finding(s)")
    for number, original, repaired in findings[:10]:
        print(f"  line {number}: {original[:120]}")
        if fix:
            print(f"    -> {repaired[:120]}")

    if fix:
        path.write_bytes(encode_text("\n".join(repaired_lines), had_bom))
    return len(findings)


def main() -> int:
    arguments = sys.argv[1:]
    fix = "--fix" in arguments
    explicit = [Path(arg) for arg in arguments if arg != "--fix"]
    files = explicit or tracked_text_files()

    total = 0
    for path in files:
        if path.is_file():
            total += scan(path, fix)

    if total == 0:
        print("STATUS: MOJIBAKE=0")
        return 0

    if fix:
        print(f"STATUS: MOJIBAKE_REPAIRED={total}")
        return 0

    print(f"STATUS: MOJIBAKE_FOUND={total}", file=sys.stderr)
    return 1


if __name__ == "__main__":
    raise SystemExit(main())
