#!/usr/bin/env python3
"""Print PT_LOAD p_align values from an ELF file for the 16 KB gate."""

from __future__ import annotations

import struct
import sys
from pathlib import Path

PT_LOAD = 1

# e_machine values, named the way readelf -h prints them, so the shell checker can
# compare against the same expected strings it already uses.
MACHINES = {
    0x03: "Intel 80386",
    0x28: "ARM",
    0x3E: "Advanced Micro Devices X86-64",
    0xB7: "AArch64",
}


def _header(path: Path) -> tuple[bytes, str, int]:
    data = path.read_bytes()
    if data[:4] != b"\x7fELF" or len(data) < 64:
        raise ValueError(f"{path} is not a valid ELF file")
    byte_order = data[5]
    if byte_order == 1:
        endian = "<"
    elif byte_order == 2:
        endian = ">"
    else:
        raise ValueError(f"{path} has an unsupported byte order {byte_order}")
    machine = struct.unpack_from(endian + "H", data, 18)[0]
    return data, endian, machine


def machine_name(path: Path) -> str:
    """Return the readelf-style machine name for an ELF file."""
    _, _, machine = _header(path)
    name = MACHINES.get(machine)
    return name if name else f"unknown-machine-0x{machine:x}"


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
    args = sys.argv[1:]
    if args and args[0] == "--machine":
        if len(args) != 2:
            print("usage: check-elf-alignment.py --machine <elf-file>", file=sys.stderr)
            return 2
        try:
            print(machine_name(Path(args[1])))
        except (OSError, ValueError, struct.error) as exc:
            print(str(exc), file=sys.stderr)
            return 1
        return 0

    if len(args) != 1:
        print(f"usage: {Path(sys.argv[0]).name} [--machine] <elf-file>", file=sys.stderr)
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
