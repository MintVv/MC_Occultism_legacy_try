#!/usr/bin/env python3
"""Validate Java 25 Cleanroom release and MCP jars, resources, and reobfuscation."""

from __future__ import annotations

import json
import struct
import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
LIBS = ROOT / "build/libs"
PROPERTIES = dict(tuple(part.strip() for part in line.split("=", 1)) for line in
                  (ROOT / "gradle.properties").read_text(encoding="utf-8").splitlines()
                  if "=" in line and not line.lstrip().startswith("#"))
VERSION = PROPERTIES["mod_version"]
MOD_ID = PROPERTIES["mod_id"]
RELEASE = f"{MOD_ID}-{VERSION}.jar"
DEV = f"{MOD_ID}-{VERSION}-mcp.jar"
JAVA_MAJOR = 69

REQUIRED_ENTRIES = {
    "mcmod.info",
    "pack.mcmeta",
    "assets/occultism/lang/en_us.lang",
    "assets/occultism/lang/zh_cn.lang",
    "assets/occultism/rituals/rituals.json",
    "assets/occultism/dictionary/modonomicon_index.json",
    "assets/occultism/patchouli_books/dictionary_of_spirits/book.json",
}

TEST_MARKERS = ("Test.class", "org/junit", "org/opentest4j", "org/apiguardian")
SRG_MARKER = b"func_"


def class_major(blob: bytes):
    if len(blob) < 8 or blob[:4] != b"\xca\xfe\xba\xbe":
        return None
    return struct.unpack(">H", blob[6:8])[0]


def check(jar_name: str, failures: list[str]) -> zipfile.ZipFile:
    path = LIBS / jar_name
    if not path.exists():
        failures.append(f"{jar_name}: not built - run `gradlew assemble` first")
        return None

    jar = zipfile.ZipFile(path)
    names = jar.namelist()
    duplicates = sorted({n for n in names if names.count(n) > 1})
    if duplicates:
        failures.append(f"{jar_name}: duplicate zip entries {duplicates[:3]}")

    classes = [n for n in names if n.endswith(".class")]
    if not classes:
        failures.append(f"{jar_name}: contains no classes at all")
    for entry in classes:
        major = class_major(jar.read(entry))
        if major is None:
            failures.append(f"{jar_name}: {entry} is not a class file")
        elif major != JAVA_MAJOR:
            failures.append(f"{jar_name}: {entry} is bytecode major {major}, expected Java 25 ({JAVA_MAJOR})")
            break

    leaked = [n for n in names if any(marker in n for marker in TEST_MARKERS)]
    if leaked:
        failures.append(f"{jar_name}: test classes or frameworks inside the jar {leaked[:3]}")

    for required in sorted(REQUIRED_ENTRIES):
        if required not in names:
            failures.append(f"{jar_name}: missing required resource {required}")

    ritual_path = "assets/occultism/rituals/rituals.json"
    if ritual_path in names:
        rituals = json.loads(jar.read(ritual_path).decode("utf-8"))
        if len(rituals) < 100:
            failures.append(f"{jar_name}: only {len(rituals)} rituals shipped, expected the full table")
        for ritual in rituals:
            if "duration_ticks" not in ritual and "duration" not in ritual:
                failures.append(f"{jar_name}: ritual {ritual['id']} ships without an explicit duration")
                break

    return jar


def check_reobfuscated(release: zipfile.ZipFile, dev: zipfile.ZipFile, failures: list[str]) -> None:
    if release is None or dev is None:
        return
    shared = [n for n in release.namelist() if n.endswith(".class") and n in dev.namelist()]
    if not shared:
        failures.append(f"{RELEASE} and {DEV} share no classes - cannot verify reobfuscation")
        return
    if all(release.read(n) == dev.read(n) for n in shared):
        failures.append(f"{RELEASE} is byte-identical to the MCP jar, so it was never reobfuscated")

    probe = b"".join(release.read(n)[:4096] for n in shared[:40])
    if SRG_MARKER not in probe:
        failures.append(f"{RELEASE} shows no srg names, so it does not look like a reobfuscated release")


def main() -> int:
    failures: list[str] = []
    release = check(RELEASE, failures)
    dev = check(DEV, failures)
    try:
        check_reobfuscated(release, dev, failures)
        if failures:
            print("Built jar validation FAILED:")
            for failure in failures:
                print("  -", failure)
            return 1

        total = len([n for n in release.namelist() if n.endswith(".class")])
        print(f"Built jars OK: {RELEASE} and {DEV}, {total} classes all Java 25 bytecode, "
              f"resources complete, release is reobfuscated.")
        return 0
    finally:
        for jar in (release, dev):
            if jar is not None:
                jar.close()


if __name__ == "__main__":
    sys.exit(main())
