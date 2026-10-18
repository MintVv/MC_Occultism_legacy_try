package com.occultism.occultism.resource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

/**
 * Structural checks on the advancement tree.
 *
 * <p>Advancements fail quietly. A parent that does not exist drops the whole branch, an icon that
 * names an unregistered item leaves a blank slot, a trigger that was never registered means the
 * criterion can never fire, and a reward recipe that is missing simply never grants. Vanilla logs
 * some of this at load and ignores the rest, so none of it fails a build.</p>
 *
 * <p>Two advancements pointed their icon at {@code occultism:ritual_dummy/familiar_shub_niggurath},
 * an item this port never registers: the Shub Niggurath familiar has no ritual - upstream has none
 * either - so no ritual dummy for it exists. The icons now use an item that does exist.</p>
 */
class AdvancementPortContractTest {

    private static final Path ASSETS = Paths.get("src/main/resources/assets/occultism");
    private static final Path ADVANCEMENTS = ASSETS.resolve("advancements");
    private static final Path LANG = ASSETS.resolve("lang/en_us.lang");
    private static final Path ITEMS = Paths.get("src/main/java/com/occultism/occultism/registry/ModItems.java");
    private static final Path BLOCKS = Paths.get("src/main/java/com/occultism/occultism/registry/ModBlocks.java");
    private static final Path TRIGGERS = Paths.get("src/main/java/com/occultism/occultism/advancement");

    /** Vanilla ids that only exist after 1.12; renamed or added later than the target version. */
    private static final Set<String> POST_1_12_IDS = new LinkedHashSet<>(java.util.Arrays.asList(
            "minecraft:poppy", "minecraft:cornflower", "minecraft:oxeye_daisy",
            "minecraft:lily_of_the_valley", "minecraft:cobweb", "minecraft:sugar_cane",
            "minecraft:carved_pumpkin", "minecraft:dead_bush", "minecraft:melon",
            "minecraft:netherite_scrap", "minecraft:netherite_ingot", "minecraft:echo_shard",
            "minecraft:amethyst_shard", "minecraft:calcite", "minecraft:tuff",
            "minecraft:deepslate", "minecraft:raw_iron", "minecraft:raw_gold",
            "minecraft:raw_copper", "minecraft:copper_ingot", "minecraft:glow_ink_sac"));

    @Test
    void everyAdvancementResolvesItsParentIconTriggerAndReward() throws IOException {
        List<Path> files = jsonFiles(ADVANCEMENTS);
        assertFalse(files.isEmpty(), "No advancements found");

        Set<String> knownIds = new LinkedHashSet<>();
        for (Path file : files) {
            knownIds.add(relativeId(file));
        }
        Set<String> itemIds = registeredItemIds();
        Set<String> triggerIds = registeredTriggerIds();
        Set<String> recipeIds = recipeIds();

        Set<String> problems = new LinkedHashSet<>();
        for (Path file : files) {
            String id = relativeId(file);
            JsonObject advancement = read(file).getAsJsonObject();

            String parent = stringOf(advancement, "parent");
            if (parent != null && parent.startsWith("occultism:")
                    && !knownIds.contains(parent.substring("occultism:".length()))) {
                problems.add(id + ": parent " + parent + " does not exist");
            }

            JsonObject display = advancement.has("display")
                    ? advancement.getAsJsonObject("display") : null;
            if (display != null && display.has("icon")) {
                String icon = stringOf(display.getAsJsonObject("icon"), "item");
                if (icon != null && icon.startsWith("occultism:")
                        && !itemIds.contains(icon.substring("occultism:".length()))) {
                    problems.add(id + ": icon " + icon + " is not a registered item");
                }
            }

            if (advancement.has("criteria")) {
                for (java.util.Map.Entry<String, JsonElement> criterion
                        : advancement.getAsJsonObject("criteria").entrySet()) {
                    JsonObject entry = criterion.getValue().getAsJsonObject();
                    String trigger = stringOf(entry, "trigger");
                    if (trigger != null && trigger.startsWith("occultism:")
                            && !triggerIds.contains(trigger.substring("occultism:".length()))) {
                        problems.add(id + "/" + criterion.getKey() + ": trigger " + trigger + " is not registered");
                    }
                }
            }

            if (advancement.has("rewards")) {
                JsonObject rewards = advancement.getAsJsonObject("rewards");
                if (rewards.has("recipes")) {
                    for (JsonElement reward : rewards.getAsJsonArray("recipes")) {
                        String recipe = reward.getAsString();
                        if (recipe.startsWith("occultism:")
                                && !recipeIds.contains(recipe.substring("occultism:".length()))) {
                            problems.add(id + ": reward recipe " + recipe + " does not exist");
                        }
                    }
                }
            }
        }

        assertTrue(problems.isEmpty(), "Advancements that would misbehave in game: " + problems);
    }

    @Test
    void noAdvancementUsesAnItemIdThatDoesNotExistIn1_12() throws IOException {
        // A real server run caught this: an advancement icon named minecraft:poppy, an id that
        // only exists from 1.13 onwards. Vanilla logged a parsing error and dropped the whole
        // advancement, and no static check noticed because the id is not in our namespace.
        Set<String> problems = new LinkedHashSet<>();
        for (Path file : jsonFiles(ADVANCEMENTS)) {
            JsonObject advancement = read(file).getAsJsonObject();
            if (!advancement.has("display")) {
                continue;
            }
            JsonObject icon = advancement.getAsJsonObject("display").has("icon")
                    ? advancement.getAsJsonObject("display").getAsJsonObject("icon") : null;
            String item = stringOf(icon, "item");
            if (item != null && POST_1_12_IDS.contains(item)) {
                problems.add(relativeId(file) + ": " + item + " does not exist in 1.12");
            }
        }
        assertTrue(problems.isEmpty(), "Advancement icons that cannot resolve in 1.12: " + problems);
    }

    @Test
    void everyAdvancementTitleAndDescriptionHasALanguageKey() throws IOException {
        Set<String> lang = languageKeys();
        Set<String> missing = new LinkedHashSet<>();
        for (Path file : jsonFiles(ADVANCEMENTS)) {
            JsonObject advancement = read(file).getAsJsonObject();
            if (!advancement.has("display")) {
                continue;
            }
            JsonObject display = advancement.getAsJsonObject("display");
            for (String field : new String[] {"title", "description"}) {
                if (!display.has(field) || !display.get(field).isJsonObject()) {
                    continue;
                }
                String key = stringOf(display.getAsJsonObject(field), "translate");
                if (key != null && !lang.contains(key)) {
                    missing.add(relativeId(file) + ": " + key);
                }
            }
        }
        assertTrue(missing.isEmpty(), "Advancement display keys that would render raw: " + missing);
    }

    // -- helpers -----------------------------------------------------------------------------

    private static Set<String> registeredItemIds() throws IOException {
        Set<String> ids = new LinkedHashSet<>();
        for (Path source : new Path[] {ITEMS, BLOCKS}) {
            Matcher matcher = Pattern.compile("public static final (?:Item|Block) \\w+\\s*=\\s*\\n?\\s*\\w+\\(\"([^\"]+)\"")
                    .matcher(readText(source));
            while (matcher.find()) {
                ids.add(matcher.group(1));
            }
        }
        return ids;
    }

    private static Set<String> registeredTriggerIds() throws IOException {
        Set<String> ids = new LinkedHashSet<>();
        for (Path file : javaFiles(TRIGGERS)) {
            Matcher matcher = Pattern.compile("new ResourceLocation\\(Tags\\.MOD_ID, \"([a-z_/]+)\"\\)")
                    .matcher(readText(file));
            while (matcher.find()) {
                ids.add(matcher.group(1));
            }
        }
        return ids;
    }

    private static Set<String> recipeIds() throws IOException {
        Set<String> ids = new LinkedHashSet<>();
        try (Stream<Path> stream = Files.walk(ASSETS.resolve("recipes"))) {
            stream.filter(path -> path.toString().endsWith(".json"))
                    .forEach(path -> ids.add(path.getFileName().toString().replace(".json", "")));
        }
        return ids;
    }

    private static Set<String> languageKeys() throws IOException {
        Set<String> keys = new LinkedHashSet<>();
        for (String line : readText(LANG).split("\\R")) {
            int split = line.indexOf('=');
            if (split > 0 && !line.startsWith("#")) {
                keys.add(line.substring(0, split).trim());
            }
        }
        return keys;
    }

    private static String relativeId(Path file) {
        return ADVANCEMENTS.relativize(file).toString().replace('\\', '/').replace(".json", "");
    }

    private static String stringOf(JsonObject object, String field) {
        if (object == null || !object.has(field) || object.get(field).isJsonNull()) {
            return null;
        }
        JsonElement value = object.get(field);
        return value.isJsonPrimitive() ? value.getAsString() : null;
    }

    private static JsonElement read(Path file) throws IOException {
        return new JsonParser().parse(readText(file));
    }

    private static String readText(Path file) throws IOException {
        return new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
    }

    private static List<Path> jsonFiles(Path root) throws IOException {
        return collect(root, ".json");
    }

    private static List<Path> javaFiles(Path root) throws IOException {
        return collect(root, ".java");
    }

    private static List<Path> collect(Path root, String suffix) throws IOException {
        List<Path> files = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(root)) {
            stream.filter(path -> path.toString().endsWith(suffix)).forEach(files::add);
        }
        return files;
    }
}
