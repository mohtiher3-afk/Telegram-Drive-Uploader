#!/usr/bin/env python3
"""Print PT_LOAD p_align values from an ELF file for the 16 KB gate."""

from __future__ import annotations

import struct
import sys
from pathlib import Path

PT_LOAD = 1


def load_alignments(path: Path) -> list[int]:
    data = path.read_bytes()
    if data[:4] != b"\x7fELF" or len(data) < 64:
        raise ValueError(f"{path} is not a valid ELF file")

    elf_class = data[4]
    byte_order = data[5]
    endian = "<" if byte_order == 1 else ">"

    if elf_class == 1:
        if len(data) < 52:
            raise ValueError(f"{path} has a truncated ELF32 header")
        phoff = struct.unpack_from(endian + "I", data, 28)[0]
        phentsize, phnum = struct.unpack_from(endian + "HH", data, 42)
        phdr_fmt = endian + "IIIIIIII"
        align_index = 7
        min_phdr_size = 32
    elif elf_class == 2:
        phoff = struct.unpack_from(endian + "Q", data, 32)[0]
        phentsize, phnum = struct.unpack_from(endian + "HH", data, 54)
        phdr_fmt = endian + "IIQQQQQQ"
        align_index = 7
        min_phdr_size = 56
    else:
        raise ValueError(f"{path} has an unsupported ELF class {elf_class}")

    if phentsize < min_phdr_size or phnum == 0:
        raise ValueError(f"{path} has an invalid ELF program-header table")

    alignments: list[int] = []
    for index in range(phnum):
        offset = phoff + index * phentsize
        if offset + min_phdr_size > len(data):
            raise ValueError(f"{path} has a truncated program header")
        fields = struct.unpack_from(phdr_fmt, data, offset)
        if fields[0] == PT_LOAD:
            alignments.append(int(fields[align_index]))

    if not alignments:
        raise ValueError(f"{path} contains no PT_LOAD segments")
    return alignments


def main() -> int:
    if len(sys.argv) != 2:
        print(f"usage: {Path(sys.argv[0]).name} <elf-file>", file=sys.stderr)
        return 2

    try:
        alignments = load_alignments(Path(sys.argv[1]))
    except (OSError, ValueError, struct.error) as exc:
        print(str(exc), file=sys.stderr)
        return 1

    for alignment in alignments:
        print(f"0x{alignment:x}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
