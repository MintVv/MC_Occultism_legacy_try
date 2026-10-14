#!/usr/bin/env python3
"""Rebuild the local Dictionary of Spirits Modonomicon index from upstream data.

The existing local index is the port's allow-list: it decides which upstream
categories, entries and pages are present in the 1.12 book. This generator keeps
that selection intact, but refreshes each selected object from upstream JSON so
Modonomicon fields such as icons, node backgrounds and parents are preserved.
Local entry files override upstream data and also provide entries that are absent
from the older upstream snapshot used by the 1.12 compatibility index.
"""

from __future__ import annotations

import json
import shutil
from pathlib import Path
from typing import Any


ROOT = Path(__file__).resolve().parents[1]
UPSTREAM_ROOT = ROOT / "_reference" / "occultism-upstream"
UPSTREAM_BOOK = (
    UPSTREAM_ROOT
    / "src"
    / "generated"
    / "resources"
    / "data"
    / "occultism"
    / "modonomicon"
    / "books"
    / "dictionary_of_spirits"
)
UPSTREAM_ASSETS = (
    UPSTREAM_ROOT
    / "src"
    / "main"
    / "resources"
    / "assets"
    / "occultism"
)
COMPATIBILITY_ASSETS = (
    ROOT
    / "_reference"
    / "occultism-26.1.2"
    / "src"
    / "main"
    / "resources"
    / "assets"
    / "occultism"
)
LOCAL_INDEX = (
    ROOT
    / "src"
    / "main"
    / "resources"
    / "assets"
    / "occultism"
    / "dictionary"
    / "modonomicon_index.json"
)
LOCAL_ENTRIES = LOCAL_INDEX.parent / "entries"
LOCAL_LANG = LOCAL_INDEX.parent.parent / "lang"
LOCAL_BOOK_TEXTURES = (
    ROOT
    / "src"
    / "main"
    / "resources"
    / "assets"
    / "occultism"
    / "textures"
    / "gui"
    / "book"
)

TOP_LEVEL_KEYS = (
    "type",
    "background",
    "background_height",
    "background_parallax_layers",
    "background_texture_zoom_multiplier",
    "background_width",
    "condition",
    "description",
    "display_mode",
    "icon",
    "max_scroll_x",
    "max_scroll_y",
    "name",
    "show_category_button",
    "sort_number",
)

ENTRY_KEYS = (
    "id",
    "type",
    "background",
    "category",
    "condition",
    "description",
    "icon",
    "name",
    "parents",
    "sort_number",
    "x",
    "y",
)

COMPAT_PAGE_KEYS = ("id", "type", "title", "text", "detail", "sort_number")
UPSTREAM_LANG = (
    UPSTREAM_ROOT
    / "src"
    / "generated"
    / "resources"
    / "assets"
    / "occultism"
    / "lang"
    / "en_us.json"
)

BOOK_BINDING_RECIPES = {
    "occultism:book_of_binding_foliot": "occultism:crafting/book_of_binding_bound_foliot_from_unbound",
    "occultism:book_of_binding_djinni": "occultism:crafting/book_of_binding_bound_djinni_from_unbound",
    "occultism:book_of_binding_afrit": "occultism:crafting/book_of_binding_bound_afrit_from_unbound",
    "occultism:book_of_binding_marid": "occultism:crafting/book_of_binding_bound_marid_from_unbound",
}

UNSUPPORTED_MULTIBLOCKS = {"occultism:storage_stabilizer_demo"}

ADDITIONAL_ALLOW_LIST = {
    "pentacles": {
        "summon_wild_greater_spirit": ("intro", "multiblock", "uses"),
    },
    "familiar_rituals": {
        "familiar_greedy": ("entity", "ritual", "description"),
        "familiar_cthulhu": ("entity", "ritual", "description"),
        "familiar_blacksmith": ("entity", "ritual", "description", "description2"),
        "familiar_dragon": ("entity", "ritual", "description"),
        "familiar_fairy": ("entity", "ritual", "description"),
        "familiar_shub_niggurath": ("entity", "ritual", "description"),
        "familiar_beholder": ("entity", "ritual", "description"),
        "familiar_chimera": ("entity", "ritual", "description", "description2"),
    },
    "possession_rituals": {
        "possess_warden": ("entity", "ritual", "description"),
    },
}


def read_json(path: Path) -> dict[str, Any]:
    with path.open("r", encoding="utf-8") as handle:
        return json.load(handle)


def read_lang_keys(path: Path) -> set[str]:
    keys: set[str] = set()
    with path.open("r", encoding="utf-8") as handle:
        for raw_line in handle:
            line = raw_line.strip()
            if not line or line.startswith("#") or "=" not in line:
                continue
            keys.add(line.split("=", 1)[0])
    return keys


def extend_allow_list(index: dict[str, Any]) -> None:
    categories = {category["id"]: category for category in index["categories"]}
    for category_id, entries in ADDITIONAL_ALLOW_LIST.items():
        category = categories.get(category_id)
        if category is None:
            category = {"id": category_id, "entries": []}
            index["categories"].append(category)
            categories[category_id] = category

        selected_entries = {entry["id"]: entry for entry in category["entries"]}
        for entry_id, page_ids in entries.items():
            entry = selected_entries.get(entry_id)
            if entry is None:
                entry = {"id": entry_id, "pages": []}
                category["entries"].append(entry)
                selected_entries[entry_id] = entry

            selected_pages = {page["id"] for page in entry.get("pages", [])}
            entry.setdefault("pages", []).extend(
                {"id": page_id} for page_id in page_ids if page_id not in selected_pages
            )


def select_keys(source: dict[str, Any], keys: tuple[str, ...]) -> dict[str, Any]:
    return {key: source[key] for key in keys if key in source}


def category_path(category_id: str) -> Path:
    return UPSTREAM_BOOK / "categories" / f"{category_id}.json"


def entry_path(category_id: str, entry_id: str) -> Path:
    return UPSTREAM_BOOK / "entries" / category_id / f"{entry_id}.json"


def local_entry_path(category_id: str, entry_id: str) -> Path:
    return LOCAL_ENTRIES / category_id / f"{entry_id}.json"


def load_entry(
    category_id: str,
    entry_id: str,
    compatibility_entry: dict[str, Any] | None = None,
) -> tuple[dict[str, Any], dict[str, dict[str, Any]]]:
    upstream_file = entry_path(category_id, entry_id)
    local_file = local_entry_path(category_id, entry_id)
    if not upstream_file.is_file() and not local_file.is_file() and compatibility_entry is None:
        raise FileNotFoundError(
            f"Missing both upstream and local dictionary entry for {category_id}/{entry_id}"
        )

    merged: dict[str, Any] = {}
    embedded_pages: dict[str, dict[str, Any]] = {}
    sources: list[dict[str, Any]] = []
    if compatibility_entry is not None:
        sources.append(compatibility_entry)
    if upstream_file.is_file():
        sources.append(read_json(upstream_file))
    if local_file.is_file():
        sources.append(read_json(local_file))
    for source in sources:
        merged.update(source)
        for page in source.get("pages", []):
            page_id = page.get("id")
            if isinstance(page_id, str):
                embedded_pages[page_id] = page
    return merged, embedded_pages


def page_path(category_id: str, entry_id: str, page_id: str) -> Path:
    return UPSTREAM_BOOK / "entries" / category_id / entry_id / "pages" / f"{page_id}.json"


def find_page_path(category_id: str, entry_id: str, page_id: str) -> Path | None:
    direct = page_path(category_id, entry_id, page_id)
    if direct.is_file():
        return direct
    if "/" in page_id:
        nested = UPSTREAM_BOOK / "entries" / category_id / "pages" / f"{page_id}.json"
        if nested.is_file():
            return nested
    return None


def build_detail(page: dict[str, Any]) -> str:
    page_type = page.get("type", "")
    if page_type == "modonomicon:spotlight" and "item" in page:
        return f"Item: {page['item']}"
    if page_type == "modonomicon:image" and isinstance(page.get("images"), list):
        return "Image: " + ", ".join(str(image) for image in page["images"])
    if page_type in {
        "occultism:ritual_recipe",
        "occultism:spirit_fire_recipe",
        "modonomicon:smelting_recipe",
        "modonomicon:crafting_recipe",
        "occultism:book_binding_recipe",
        "occultism:spirit_trade_recipe",
    }:
        for key in ("recipe_id", "recipe_id_1", "recipe_id_2"):
            if key in page:
                return f"Recipe: {page[key]}"
    if page_type == "modonomicon:entity" and "entity_id" in page:
        return f"Entity: {page['entity_id']}"
    if page_type == "modonomicon:multiblock" and "multiblock_id" in page:
        return f"Multiblock: {page['multiblock_id']}"
    return ""


def normalize_page(page: dict[str, Any]) -> dict[str, Any]:
    normalized = dict(page)
    page_type = normalized.get("type", "")

    if page_type == "modonomicon:spotlight":
        item = normalized.get("item")
        if isinstance(item, dict) and isinstance(item.get("id"), str):
            normalized["item"] = item["id"]

    if page_type == "occultism:book_binding_recipe":
        unbound_book = normalized.get("unbound_book")
        if isinstance(unbound_book, dict):
            recipe_id = BOOK_BINDING_RECIPES.get(unbound_book.get("id"))
            if recipe_id:
                normalized["recipe_id_1"] = recipe_id

    multiblock_id = normalized.get("multiblock_id")
    if page_type == "modonomicon:multiblock" and multiblock_id in UNSUPPORTED_MULTIBLOCKS:
        normalized["type"] = "modonomicon:text"
        for key in ("multiblock_id", "multiblock_name", "show_visualize_button"):
            normalized.pop(key, None)

    return normalized


def sanitize_entry_read_conditions(value: Any, entry_ids: set[str]) -> Any:
    if isinstance(value, list):
        return [sanitize_entry_read_conditions(item, entry_ids) for item in value]
    if not isinstance(value, dict):
        return value
    if value.get("type") == "modonomicon:entry_read" and value.get("entry_id") not in entry_ids:
        return {"type": "modonomicon:none"}
    return {key: sanitize_entry_read_conditions(item, entry_ids) for key, item in value.items()}


def sanitize_entry_references(index: dict[str, Any], entry_ids: set[str]) -> dict[str, Any]:
    for category in index["categories"]:
        for entry in category["entries"]:
            if "parents" in entry:
                entry["parents"] = [
                    parent
                    for parent in entry["parents"]
                    if isinstance(parent, dict) and parent.get("entry") in entry_ids
                ]
    return sanitize_entry_read_conditions(index, entry_ids)


def collect_occultism_book_textures(value: Any, refs: set[str]) -> None:
    if isinstance(value, str):
        prefix = "occultism:textures/gui/book/"
        if value.startswith(prefix) and value.endswith(".png"):
            refs.add(value[len(prefix) :])
        return
    if isinstance(value, list):
        for item in value:
            collect_occultism_book_textures(item, refs)
        return
    if isinstance(value, dict):
        for item in value.values():
            collect_occultism_book_textures(item, refs)


def collect_book_lang_keys(value: Any, refs: set[str]) -> None:
    if isinstance(value, str):
        if value.startswith("book.occultism.dictionary_of_spirits."):
            refs.add(value)
        return
    if isinstance(value, list):
        for item in value:
            collect_book_lang_keys(item, refs)
        return
    if isinstance(value, dict):
        for item in value.values():
            collect_book_lang_keys(item, refs)
        return


def copy_book_textures(refs: set[str]) -> list[str]:
    LOCAL_BOOK_TEXTURES.mkdir(parents=True, exist_ok=True)
    copied: list[str] = []
    for ref in sorted(refs):
        target = LOCAL_BOOK_TEXTURES / ref
        if target.is_file():
            copied.append(ref)
            continue
        source = UPSTREAM_ASSETS / "textures" / "gui" / "book" / ref
        if not source.is_file():
            source = COMPATIBILITY_ASSETS / "textures" / "gui" / "book" / ref
        if not source.is_file():
            raise FileNotFoundError(f"Missing upstream book texture {source}")
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(source, target)
        copied.append(ref)
    return copied


def main() -> None:
    current = read_json(LOCAL_INDEX)
    extend_allow_list(current)
    selected_entry_ids = {
        f"occultism:{category['id']}/{entry['id']}"
        for category in current["categories"]
        for entry in category["entries"]
    }
    known_lang_keys = set(read_json(UPSTREAM_LANG).keys())
    for local_lang_file in LOCAL_LANG.glob("*.lang"):
        known_lang_keys.update(read_lang_keys(local_lang_file))
    texture_refs: set[str] = set()
    lang_refs: set[str] = set()
    rebuilt_categories: list[dict[str, Any]] = []
    compatibility_overlay_count = 0

    for current_category in current["categories"]:
        category_id = current_category["id"]
        upstream_category = read_json(category_path(category_id))
        rebuilt_category = {"id": category_id}
        rebuilt_category.update(select_keys(upstream_category, TOP_LEVEL_KEYS))
        rebuilt_category["entries"] = []

        for current_entry in current_category["entries"]:
            entry_id = current_entry["id"]
            if (not entry_path(category_id, entry_id).is_file()
                    and not local_entry_path(category_id, entry_id).is_file()):
                compatibility_overlay_count += 1
            entry_source, embedded_pages = load_entry(category_id, entry_id, current_entry)
            rebuilt_entry = select_keys(entry_source, ENTRY_KEYS)
            rebuilt_entry["id"] = entry_id
            rebuilt_entry["pages"] = []

            for current_page in current_entry["pages"]:
                page_id = current_page["id"]
                upstream_page_path = find_page_path(category_id, entry_id, page_id)
                if page_id in embedded_pages:
                    page_source = embedded_pages[page_id]
                elif upstream_page_path:
                    page_source = read_json(upstream_page_path)
                else:
                    page_source = dict(current_page)
                rebuilt_page = normalize_page(page_source)
                for key in COMPAT_PAGE_KEYS:
                    if key in current_page and key not in rebuilt_page:
                        rebuilt_page[key] = current_page[key]
                for text_key in ("title", "text"):
                    value = rebuilt_page.get(text_key)
                    if isinstance(value, str) and value.startswith("book.") and value not in known_lang_keys:
                        previous = current_page.get(text_key, "")
                        rebuilt_page[text_key] = previous if not previous.startswith("book.") else ""
                rebuilt_page["detail"] = build_detail(rebuilt_page)
                rebuilt_entry["pages"].append(rebuilt_page)
                collect_occultism_book_textures(rebuilt_page, texture_refs)
                collect_book_lang_keys(rebuilt_page, lang_refs)

            rebuilt_category["entries"].append(rebuilt_entry)
            collect_occultism_book_textures(rebuilt_entry, texture_refs)
            collect_book_lang_keys(rebuilt_entry, lang_refs)

        rebuilt_categories.append(rebuilt_category)
        collect_occultism_book_textures(rebuilt_category, texture_refs)
        collect_book_lang_keys(rebuilt_category, lang_refs)

    rebuilt = {
        "source": "occultism-upstream generated Modonomicon dictionary_of_spirits"
        " + local 1.12 compatibility overlays",
        "policy": (
            "The upstream generated Modonomicon book is authoritative. "
            "Local compatibility overlays are retained only for 1.12 content "
            "that has no entry in the pinned upstream snapshot."
        ),
        "categories": rebuilt_categories,
    }
    rebuilt = sanitize_entry_references(rebuilt, selected_entry_ids)

    copied = copy_book_textures(texture_refs)
    LOCAL_INDEX.write_text(json.dumps(rebuilt, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"Wrote {LOCAL_INDEX}")
    print(f"Copied/verified {len(copied)} occultism book PNG resource(s)")
    print(f"Retained {compatibility_overlay_count} local 1.12 compatibility overlay entry/entries")
    for ref in copied:
        print(f" - {ref}")
    missing_lang = sorted(lang_refs - known_lang_keys)
    if missing_lang:
        print("Skipped missing dictionary language key(s):")
        for key in missing_lang:
            print(f" - {key}")


if __name__ == "__main__":
    main()
