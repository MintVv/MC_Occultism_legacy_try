#!/usr/bin/env python3
"""Validate the built jars, not just the sources.

Everything else in this repo checks sources and resources. These checks only
apply to the artifact that actually ships, and every one of them corresponds to
a failure that a successful `build` would not catch:

  1. every class is Java 8 bytecode (major 52) - Minecraft 1.12 runs on Java 8,
     so a class compiled for a newer target throws UnsupportedClassVersionError
     on load while the build stays green;
  2. no test or test-framework classes leaked into the jar;
  3. the resources that must ship are present (lang, rituals, book, metadata);
  4. the release jar is really reobfuscated, i.e. its classes differ from the
     dev jar and carry srg names - shipping the dev jar is a silent no-op that
     crashes at runtime;
  5. the zip has no duplicate entries.

Run after `gradlew build`. Exit code 1 lists failures.
"""

from __future__ import annotations

import json
import struct
import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
LIBS = ROOT / "build/libs"
RELEASE = "occultism-1.0.0.jar"
DEV = "occultism-1.0.0-dev.jar"

JAVA_8_MAJOR = 52

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
    if blob[:4] != b"\xca\xfe\xba\xbe":
        return None
    return struct.unpack(">H", blob[6:8])[0]


def check(jar_name: str, failures: list[str]) -> zipfile.ZipFile:
    path = LIBS / jar_name
    if not path.exists():
        failures.append(f"{jar_name}: not built - run `gradlew build` first")
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
        elif major > JAVA_8_MAJOR:
            failures.append(f"{jar_name}: {entry} is bytecode major {major}, but 1.12 runs on Java 8 (52)")
            break

    leaked = [n for n in names if any(marker in n for marker in TEST_MARKERS)]
    if leaked:
        failures.append(f"{jar_name}: test classes or frameworks inside the jar {leaked[:3]}")

    for required in sorted(REQUIRED_ENTRIES):
        if required not in names:
            failures.append(f"{jar_name}: missing required resource {required}")

    rituals = json.loads(jar.read("assets/occultism/rituals/rituals.json").decode("utf-8"))
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
        failures.append(f"{RELEASE} is byte-identical to the dev jar, so it was never reobfuscated")

    probe = b"".join(release.read(n)[:4096] for n in shared[:40])
    if SRG_MARKER not in probe:
        failures.append(f"{RELEASE} shows no srg names, so it does not look like a reobfuscated release")


def main() -> int:
    failures: list[str] = []
    release = check(RELEASE, failures)
    dev = check(DEV, failures)
    check_reobfuscated(release, dev, failures)

    if failures:
        print("Built jar validation FAILED:")
        for failure in failures:
            print("  -", failure)
        return 1

    total = len([n for n in release.namelist() if n.endswith(".class")])
    print(f"Built jars OK: {RELEASE} and {DEV}, {total} classes all Java 8 bytecode, "
          f"resources complete, release is reobfuscated.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
