#!/usr/bin/env python3
"""Verify that every relative link in tracked Markdown documentation resolves.

Rationale: documentation is a deliverable in this repository, so stale links are
treated as defects. This check runs in CI to prevent link rot after files move.

Usage:
    python3 scripts/check-doc-links.py [--quiet]

Exit codes:
    0 - all relative links resolve
    1 - one or more links are broken (details printed to stdout)
"""

from __future__ import annotations

import argparse
import os
import re
import subprocess
import sys

# Matches inline Markdown links: [text](target) and images ![alt](target).
LINK_PATTERN = re.compile(r"\]\(\s*([^)\s]+)\s*\)")
FENCE_PATTERN = re.compile(r"^\s*```")
SKIP_SCHEMES = ("http://", "https://", "mailto:", "tel:", "data:")


def tracked_markdown_files() -> list[str]:
    """Return repository-relative paths of all tracked Markdown files."""
    result = subprocess.run(
        ["git", "ls-files", "*.md"],
        capture_output=True,
        text=True,
        check=True,
    )
    return [line.strip() for line in result.stdout.splitlines() if line.strip()]


def strip_anchor_and_query(target: str) -> str:
    return target.split("#", 1)[0].split("?", 1)[0]


def should_skip(target: str) -> bool:
    lowered = target.lower()
    if not target:
        return True
    if lowered.startswith(SKIP_SCHEMES):
        return True
    # Site-absolute or anchor-only links are not filesystem relative links.
    return target.startswith("/") or target.startswith("#")


def iter_link_targets(path: str):
    """Yield (line_number, target) for Markdown links outside fenced code blocks."""
    in_fence = False
    with open(path, encoding="utf-8", errors="replace") as handle:
        for number, line in enumerate(handle, start=1):
            if FENCE_PATTERN.match(line):
                in_fence = not in_fence
                continue
            if in_fence:
                continue
            for match in LINK_PATTERN.finditer(line):
                yield number, match.group(1)


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--quiet",
        action="store_true",
        help="Only print the summary line.",
    )
    args = parser.parse_args()

    repo_root = os.path.abspath(
        subprocess.run(
            ["git", "rev-parse", "--show-toplevel"],
            capture_output=True,
            text=True,
            check=True,
        ).stdout.strip()
    )

    broken: list[tuple[str, int, str]] = []
    checked = 0
    files = tracked_markdown_files()

    for relative in files:
        absolute = os.path.join(repo_root, relative)
        if not os.path.isfile(absolute):
            broken.append((relative, 0, "<file missing from working tree>"))
            continue
        base_dir = os.path.dirname(absolute)
        for line_number, raw_target in iter_link_targets(absolute):
            target = strip_anchor_and_query(raw_target)
            # Normalize separators for repository-relative links written on any OS.
            target = target.replace("\\", "/")
            if should_skip(target):
                continue
            checked += 1
            candidate = os.path.normpath(os.path.join(base_dir, target))
            if not os.path.exists(candidate):
                broken.append((relative, line_number, raw_target))

    if broken:
        if not args.quiet:
            print("Broken relative links detected:\n")
            for relative, line_number, target in broken:
                location = f"{relative}:{line_number}" if line_number else relative
                print(f"  {location}: {target}")
            print()
        print(f"Documentation link check FAILED: {len(broken)} broken of {checked} links.")
        return 1

    print(f"Documentation link check passed: {checked} links across {len(files)} files.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
