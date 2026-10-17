#!/usr/bin/env python3
"""Generate the 1.12 Patchouli book from the pinned dictionary and local recipes.

This is a format conversion, not an upstream sync. Run generate_modonomicon_index
only when deliberately updating the source book. No generated file is deleted.
"""
from pathlib import Path
import json
import re
import argparse
import unicodedata

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/occultism"
BOOK = ASSETS / "patchouli_books/dictionary_of_spirits"
ALIASES = {
    "minecraft:copper_ingot": "occultism:copper_ingot",
    "minecraft:copper_block": "occultism:copper_block",
    "minecraft:raw_copper": "occultism:raw_copper",
    "minecraft:skeleton_skull": "minecraft:skull:0",
    "minecraft:wither_skeleton_skull": "minecraft:skull:1",
    "minecraft:player_head": "minecraft:skull:3",
    "minecraft:lapis_lazuli": "minecraft:dye:4",
    "minecraft:black_dye": "minecraft:dye:0",
    "minecraft:gray_dye": "minecraft:dye:8",
    "minecraft:bone_meal": "minecraft:dye:15",
    "minecraft:amethyst_shard": "occultism:amethyst_dust",
    "minecraft:nautilus_shell": "occultism:nautilus_shell",
    "minecraft:phantom_membrane": "occultism:phantom_membrane",
    "minecraft:leather_horse_armor": "minecraft:leather",
    "minecraft:copper_horse_armor": "occultism:copper_horse_armor",
    "minecraft:music_disc_cat": "minecraft:record_cat",
    "minecraft:music_disc_relic": "minecraft:record_13",
    "minecraft:cobweb": "minecraft:web",
    "minecraft:grass_block": "minecraft:grass",
    "minecraft:oak_log": "minecraft:log",
    "minecraft:oak_sapling": "minecraft:sapling",
    "minecraft:oak_leaves": "minecraft:leaves",
    "minecraft:oak_planks": "minecraft:planks",
    "minecraft:stone_bricks": "minecraft:stonebrick",
    "minecraft:lily_of_the_valley": "minecraft:red_flower",
    "minecraft:cornflower": "minecraft:red_flower",
    "minecraft:torchflower": "minecraft:yellow_flower",
    "minecraft:tube_coral": "minecraft:prismarine_shard",
    "minecraft:sniffer_egg": "minecraft:egg",
    "minecraft:dune_armor_trim_smithing_template": "minecraft:gold_ingot",
    "occultism:dictionary_of_spirits_icon": "occultism:dictionary_of_spirits",
    "occultism:candle_white": "occultism:large_candle",
    "occultism:flame_of_automation": "occultism:spirit_fire",
    "occultism:mysterious_egg_icon": "minecraft:egg",
    "occultism:spawn_egg/wondering_trader": "minecraft:emerald",
    "theurgy:list_filter": "occultism:book_of_calling_foliot_transport_items",
}
ICON_ALIASES = {
    "occultism:pentacle": "occultism:textures/gui/book/pentacle.png",
    "occultism:pentacle_misc": "occultism:textures/gui/book/pentacle.png",
    "occultism:pentacle_possess": "occultism:textures/gui/book/possession.png",
    "occultism:pentacle_summon": "occultism:textures/gui/book/summoning.png",
}
ENTITY_ALIASES = {"minecraft:allay": "occultism:purified_allay",
                  "occultism:otherworld_bird": "occultism:otherworld_bird"}


def read_lang(locale):
    return dict(line.split("=", 1) for line in
                (ASSETS / f"lang/{locale}.lang").read_text(encoding="utf-8-sig").splitlines()
                if "=" in line and not line.startswith("#"))


def item(value):
    if isinstance(value, dict):
        if "texture" in value:
            return value["texture"]
        ident = value.get("item", value.get("id", ""))
        meta = value.get("data", value.get("meta", value.get("metadata")))
        return item(ident) + (f":{meta}" if meta is not None else "")
    return ALIASES.get(value, value)


def icon(value):
    result = item(value)
    return ICON_ALIASES.get(result, result)


def readable_item(ident, lang):
    ident = item(ident)
    parts = ident.split(":")
    namespace, path = parts[:2] if len(parts) >= 2 else ("minecraft", ident)
    for key in (f"item.{namespace}.{path}.name", f"tile.{namespace}.{path}.name",
                f"item.{path}.name", f"tile.{path}.name"):
        if key in lang:
            return lang[key]
    # Keep unsupported modern references legible without emitting a broken link.
    return path.replace("_", " ")


def convert_text(value, lang, entries):
    text = lang.get(value, value or "")
    text = text.replace("\\\\n", "\n").replace("\\n", "\n")
    text = re.sub(r"\\+\n", "\n", text)
    text = re.sub(r"\[#\]\(([A-Fa-f0-9]{6})\)", r"$(#\1)", text).replace("[#]()", "$()")

    def link(match):
        label, scheme, target = match.groups()
        if scheme == "entry":
            target = target.replace("occultism:dictionary_of_spirits/", "").removeprefix("occultism:")
            if target.split("#")[0] in entries:
                return f"$(l:{target}){label or target.rsplit('/', 1)[-1]}$(/l)"
        if scheme == "item":
            return "$(item)" + (label or readable_item(target, lang)) + "$()"
        return label or target

    text = re.sub(r"\[([^\]]*)\]\((entry|item)://([^)]*)\)", link, text)
    # Patchouli 1.12 text links target entries, not category screens.
    text = re.sub(r"\[([^\]]*)\]\(category://([^)]*)\)", r"$(thing)\1$()", text)
    text = re.sub(r"\[([^\]]+)\]\((https?://[^)]*)\)", r"$(l:\2)\1$(/l)", text)
    text = re.sub(r"\*\*(.*?)\*\*", r"$(bold)\1$()", text, flags=re.S)
    text = re.sub(r"(?m)^- ", "$(li)", text)
    text = re.sub(r"\n[ \t]*\n+", "$(br2)", text)
    return text.replace("\n", "$(br)").strip().rstrip("\\")


def paginate(text):
    """Conservative pixel-width approximation; split before the page bottom.

    Formatting/link runs remain atomic so a page never contains a dangling link.
    Actual Patchouli font layout still needs client verification.
    """
    tokens = re.findall(r"\$\(l:[^)]*\).*?\$\(/l\)|\$\([^)]*\)|[\u2e80-\uffff]|[^\s$\u2e80-\uffff]+\s*|\s+|.", text)
    pages, current, width, lines = [], "", 0, 1
    for token in tokens:
        visible = re.sub(r"\$\([^)]*\)", "", token)
        pixels = sum(9 if unicodedata.east_asian_width(c) in "WF" else 6 for c in visible)
        extra = (width + pixels) // 108
        if token in ("$(br)", "$(li)"):
            extra += 1
        elif token == "$(br2)":
            extra += 2
        if lines + extra > 14 and current:
            pages.append(current + "$()")
            current, width, lines = "", 0, 1
            extra = pixels // 108
            if token in ("$(br)", "$(li)"):
                extra += 1
            elif token == "$(br2)":
                extra += 2
        current += token
        lines += extra
        width = 0 if token in ("$(br)", "$(br2)", "$(li)") else (width + pixels) % 108
    if current:
        pages.append(current)
    return pages


def convert_page(page, lang, entries):
    kind = page["type"]
    title = convert_text(page.get("title", ""), lang, entries)
    text = convert_text(page.get("text", ""), lang, entries)
    result = []
    if kind == "modonomicon:text":
        pass
    elif kind == "modonomicon:spotlight":
        result.append({"type": "spotlight", "item": item(page["item"]), "text": ""})
    elif kind == "modonomicon:entity":
        ident = page["entity_id"]
        result.append({"type": "entity", "entity": ENTITY_ALIASES.get(ident, ident), "text": ""})
    elif kind == "modonomicon:image":
        result.append({"type": "image", "images": page["images"], "border": page.get("border", True)})
    elif kind == "modonomicon:multiblock":
        ident = page["multiblock_id"]
        result.append({"type": "multiblock", "multiblock_id": ident,
                       "name": lang.get("multiblock." + ident.replace(":", "."),
                                        lang.get("book.occultism.dictionary_of_spirits.pentacles."
                                                 + ident.split(":")[-1] + ".name", title)),
                       "enable_visualize": True, "text": ""})
    elif kind in ("modonomicon:crafting_recipe", "occultism:book_binding_recipe"):
        for key in ("recipe_id_1", "recipe_id_2"):
            if not page.get(key):
                continue
            recipe = page[key].replace(":crafting/", ":")
            recipe = recipe.replace(":lens_frame_alt", ":lens_frame_otherrock")
            path = recipe.split(":", 1)[1]
            if not (ASSETS / f"recipes/{path}.json").is_file():
                raise ValueError(f"Missing local crafting recipe {recipe}")
            result.append({"type": "crafting", "recipe": recipe, "text": ""})
    elif kind == "modonomicon:smelting_recipe":
        ident = page["recipe_id_1"].rsplit("/", 1)[-1]
        input_id = {"burnt_otherstone": "occultism:polished_otherstone",
                    "silver_ingot": "occultism:silver_ore"}[ident]
        result.append({"type": "smelting", "recipe": input_id, "text": ""})
    elif kind == "occultism:ritual_recipe":
        for key in ("recipe_id_1", "recipe_id_2"):
            if page.get(key):
                result.append({"type": "occultism:ritual", "ritual": page[key].rsplit("/", 1)[-1]})
    elif kind == "occultism:spirit_fire_recipe":
        result.append({"type": "occultism:spirit_fire",
                       "output": "occultism:" + page["recipe_id_1"].rsplit("/", 1)[-1]})
    elif kind == "occultism:spirit_trade_recipe":
        ident = page["recipe_id_1"].rsplit("/", 1)[-1]
        result.append({"type": "occultism:spirit_trade", "recipe": ident})
    else:
        raise ValueError(f"Unconverted page type: {kind}")
    for chunk in paginate(text):
        result.append({"type": "text", "title": title, "text": chunk})
    if not result:
        result.append({"type": "text", "title": title, "text": ""})
    if page.get("id"):
        result[0]["anchor"] = page["id"]
    return result


def generate(check=False):
    index = json.loads((ASSETS / "dictionary/modonomicon_index.json").read_text(encoding="utf-8"))
    entries = {c["id"] + "/" + e["id"] for c in index["categories"] for e in c["entries"]}
    count = 0
    differences = []
    for locale in ("en_us", "zh_cn"):
        lang = read_lang(locale)
        for category in index["categories"]:
            category_id = category["id"]
            documents = {f"categories/{category_id}.json": {
                "name": lang.get(category["name"], category["name"]),
                "description": convert_text(category.get("description", category["name"]), lang, entries),
                "icon": icon(category["icon"]), "sortnum": category.get("sort_number", 0)}}
            for order, entry in enumerate(category["entries"]):
                pages = [p for source in entry["pages"] for p in convert_page(source, lang, entries)]
                documents[f"entries/{category_id}/{entry['id']}.json"] = {
                    "name": lang.get(entry["name"], entry["name"]),
                    "icon": icon(entry["icon"]), "category": category_id,
                    "sortnum": entry.get("sort_number", order), "pages": pages}
                count += len(pages)
            for relative, document in documents.items():
                path = BOOK / locale / relative
                data = json.dumps(document, ensure_ascii=False, indent=2) + "\n"
                if check:
                    if not path.exists() or path.read_text(encoding="utf-8") != data:
                        differences.append(str(path.relative_to(ROOT)))
                else:
                    path.parent.mkdir(parents=True, exist_ok=True)
                    path.write_text(data, encoding="utf-8", newline="\n")
    if differences:
        raise SystemExit("Patchouli generated files are stale:\n" + "\n".join(differences))
    print(f"Patchouli: {len(index['categories'])} categories, {len(entries)} entries per language; "
          f"{count} pages across en_us + zh_cn.")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true")
    generate(parser.parse_args().check)
