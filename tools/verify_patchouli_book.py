#!/usr/bin/env python3
"""Validate the generated Patchouli book against the things that silently break in game.

The book currently has no unit test guarding it: a wrong page type, a missing
language key, a dangling ritual/multiblock reference or a typo'd item id all
produce a page that renders as an error or a blank placeholder, and none of them
fail the build. Run this after touching rituals, the dictionary index or the
generator.

Checks:
  1. every page `type` is a Patchouli 1.12 built-in or one of our 3 custom types;
  2. no `text`/`title`/`name` still holds a raw translation key;
  3. every ritual has a book entry and no entry points at a missing ritual,
     and the job / tier / sacrifice labels its page renders actually resolve;
  4. every `multiblock_id` is a pentacle registered by PentacleDetector;
  5. every entity/item referenced resolves to a registered id;
  6. `en_us` and `zh_cn` language key sets are identical;
  7. every page carries the fields its type reads - Patchouli renders a page
     with a missing required field as an error page rather than failing a build.

Exit code 1 lists failures. Nothing here is a substitute for opening the book
in game; it only rules out the mechanical mistakes.
"""

from __future__ import annotations

import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/occultism"
BOOK = ASSETS / "patchouli_books/dictionary_of_spirits"
INDEX = ASSETS / "dictionary/modonomicon_index.json"
RITUALS = ASSETS / "rituals/rituals.json"
LANG = ASSETS / "lang"
JAVA = ROOT / "src/main/java/com/occultism/occultism"

# Patchouli 1.0-23.6 registers these without a namespace (ClientBookRegistry.addPageTypes).
BUILTIN_PAGES = {
    "text", "entity", "multiblock", "spotlight", "crafting", "smelting",
    "image", "link", "quest", "relations", "empty", "template", "double_recipe",
}
# Registered into ClientBookRegistry.INSTANCE.pageTypes by PatchouliClient.
CUSTOM_PAGES = {"occultism:ritual", "occultism:spirit_fire", "occultism:spirit_trade"}

RAW_KEY = re.compile(r"^(book\.|item\.|tile\.|multiblock\.|entity\.|gui\.)")

# Fields each page type reads from its json. Patchouli fills these in by name, so a
# missing one shows up as an error page in game and nowhere in the build. An empty
# `text` is legal (an item-only page); a missing pointer field is not.
POINTER_FIELDS = {"entity", "item", "recipe", "multiblock_id", "images", "ritual",
                  "output", "url", "entries", "template"}
REQUIRED_FIELDS = {
    "text": ("text",),
    "crafting": ("recipe", "text"),
    "smelting": ("recipe", "text"),
    "spotlight": ("item", "text"),
    "entity": ("entity", "text"),
    "multiblock": ("multiblock_id", "name", "text"),
    "image": ("images",),
    "link": ("url", "link_text"),
    "relations": ("entries",),
    "quest": ("text",),
    "template": ("template",),
    "occultism:ritual": ("ritual",),
    "occultism:spirit_fire": ("output",),
    "occultism:spirit_trade": ("recipe",),
}


def read_lang(locale):
    path = LANG / f"{locale}.lang"
    result = {}
    for line in path.read_text(encoding="utf-8-sig").splitlines():
        if "=" in line and not line.startswith("#"):
            key, value = line.split("=", 1)
            result[key.strip()] = value
    return result


def registered(names_file, pattern):
    """Collect registry names declared in a Mod* registry class."""
    text = (JAVA / "registry" / names_file).read_text(encoding="utf-8", errors="replace")
    return set(re.findall(pattern, text))


def registered_from_lang(lang, prefixes):
    """Registry names implied by the item/tile/entity name keys.

    Safer than scraping `ModItems.java`: items are created through several
    helpers (item/chalk/callingBook/debugSummon...), so a single call-site
    regex silently misses whole families.
    """
    found = set()
    for key in lang:
        for prefix, suffix in prefixes:
            if key.startswith(prefix) and key.endswith(suffix):
                found.add(key[len(prefix):-len(suffix)])
    return found


def main():
    failures = []
    book_files = sorted(BOOK.rglob("*.json"))
    index = json.loads(INDEX.read_text(encoding="utf-8"))
    rituals = json.loads(RITUALS.read_text(encoding="utf-8"))

    en, zh = read_lang("en_us"), read_lang("zh_cn")
    if set(en) != set(zh):
        only_en = sorted(set(en) - set(zh))[:5]
        only_zh = sorted(set(zh) - set(en))[:5]
        failures.append(f"language keys differ: en-only {only_en}, zh-only {only_zh}")

    # -- 1 / 2: page types and unresolved keys ------------------------------
    ritual_refs = set()
    multiblock_refs = set()
    entity_refs = set()
    for path in book_files:
        doc = json.loads(path.read_text(encoding="utf-8"))
        rel = path.relative_to(BOOK).as_posix()
        if path.name != "book.json" and RAW_KEY.match(str(doc.get("name", ""))):
            failures.append(f"{rel}: unresolved name key {doc['name']}")
        for page in doc.get("pages", []):
            kind = page.get("type")
            if kind not in BUILTIN_PAGES and kind not in CUSTOM_PAGES:
                failures.append(f"{rel}: unknown page type {kind!r}")
            for field in ("text", "title"):
                if RAW_KEY.match(str(page.get(field, ""))):
                    failures.append(f"{rel}: unresolved {field} key {page[field]}")
            if kind == "occultism:ritual":
                ritual_refs.add(page.get("ritual"))
            elif kind == "multiblock":
                multiblock_refs.add(page.get("multiblock_id"))
            elif kind == "entity":
                entity_refs.add(page.get("entity"))

    # -- 7: required fields per page type ----------------------------------
    for path in book_files:
        doc = json.loads(path.read_text(encoding="utf-8"))
        rel = path.relative_to(BOOK).as_posix()
        for index, page in enumerate(doc.get("pages", [])):
            kind = page.get("type")
            for field in REQUIRED_FIELDS.get(kind, ()):
                if field not in page:
                    failures.append(f"{rel} page {index} ({kind}): missing required field {field!r}")
                elif field in POINTER_FIELDS and not page.get(field):
                    failures.append(f"{rel} page {index} ({kind}): empty required field {field!r}")

    # -- 3: ritual coverage -------------------------------------------------
    ritual_ids = {r["id"] for r in rituals}
    covered = {r for r in ritual_refs if r}
    for missing in sorted(ritual_ids - covered):
        failures.append(f"ritual {missing!r} has no book entry")
    for dangling in sorted(covered - ritual_ids):
        failures.append(f"book references unknown ritual {dangling!r}")

    # PageRitual renders the job name and spirit tier through I18n at draw time,
    # so a missing key shows the raw key on the page instead of failing any build.
    for ritual in rituals:
        job = ritual.get("spirit_job_type")
        if job:
            key = "job.occultism." + str(job).split(":")[-1]
            if key not in en:
                failures.append(f"ritual {ritual['id']!r}: page label {key!r} missing in en_us")
            if key not in zh:
                failures.append(f"ritual {ritual['id']!r}: page label {key!r} missing in zh_cn")
        tier = ritual.get("spirit_tier")
        if tier:
            key = "spirit_tier.occultism." + str(tier).split(":")[-1]
            if key not in en:
                failures.append(f"ritual {ritual['id']!r}: tier label {key!r} missing in en_us")
        sacrifice = ritual.get("entity_to_sacrifice")
        if isinstance(sacrifice, dict):
            key = sacrifice.get("display_name")
            if isinstance(key, str) and key.startswith("ritual."):
                if key not in en:
                    failures.append(f"ritual {ritual['id']!r}: sacrifice label {key!r} missing in en_us")
                if key not in zh:
                    failures.append(f"ritual {ritual['id']!r}: sacrifice label {key!r} missing in zh_cn")

    # -- 4: multiblock ids --------------------------------------------------
    pentacles = set(re.findall(
        r'new PentacleDefinition\("([a-z_]+)"',
        (JAVA / "ritual/PentacleDetector.java").read_text(encoding="utf-8")))
    for mid in sorted(m for m in multiblock_refs if m):
        namespace, _, path = str(mid).partition(":")
        if namespace != "occultism" or path not in pentacles:
            failures.append(f"multiblock {mid!r} is not a registered pentacle")
    undrawn = pentacles - {str(m).partition(":")[2] for m in multiblock_refs if m}
    if undrawn:
        failures.append(f"pentacles registered but never shown in the book: {sorted(undrawn)}")

    # -- 5: entity and item ids --------------------------------------------
    # Union of both sources: lang keys miss entities that reuse a shared name
    # key, and some entities are registered without a dedicated .name entry.
    entities = registered_from_lang(en, [("entity.occultism.", ".name")])
    entities |= registered("ModEntities.java", r'new ResourceLocation\(Tags\.MOD_ID, "([a-z_/]+)"\)')
    entities |= {"minecraft:" + n for n in
                 ("parrot", "bat", "chicken", "cow", "pig", "sheep", "wolf", "cat", "horse")}
    for eid in sorted(e for e in entity_refs if e):
        namespace, _, path = str(eid).partition(":")
        if namespace == "occultism" and path not in entities:
            failures.append(f"entity page references unregistered entity {eid!r}")

    items = registered_from_lang(en, [("item.occultism.", ".name"), ("tile.occultism.", ".name")])
    for path in book_files:
        doc = json.loads(path.read_text(encoding="utf-8"))
        rel = path.relative_to(BOOK).as_posix()
        for page in doc.get("pages", []):
            if page.get("type") not in ("spotlight", "crafting", "smelting"):
                continue
            parts = str(page.get("item", "")).split(":")
            if len(parts) < 2 or parts[0] != "occultism":
                continue
            if parts[1] not in items:
                failures.append(f"{rel}: references unregistered item {page['item']!r}")

    if failures:
        print("Patchouli book validation FAILED:")
        for f in failures:
            print("  -", f)
        return 1
    print(f"Patchouli book OK: {len(book_files)} files, {len(covered)}/{len(ritual_ids)} rituals covered, "
          f"{len([m for m in multiblock_refs if m])} multiblocks, 0 unresolved keys.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
