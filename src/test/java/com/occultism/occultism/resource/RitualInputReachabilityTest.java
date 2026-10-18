package com.occultism.occultism.resource;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

/**
 * Every occultism item consumed by a recipe or a ritual must have some way to be obtained.
 *
 * <p>A ritual that demands an item nothing produces is a dead end: the pentacle lights up, the
 * player collects the ingredients, and the ritual can never start. Five such dead ends existed -
 * including the storage controller, whose absence broke the entire storage progression - and none
 * of the other tests noticed, because they all checked internal consistency rather than whether a
 * player can actually walk the chain.</p>
 *
 * <p>Producers counted here: JSON recipe results, ritual results, the Java-side spirit fire, trade,
 * job and smelting tables, entity drop code and world generation. Anything genuinely obtained
 * outside those (a mob drop, a world-gen block) is covered by {@link #KNOWN_UNSOURCED} with the
 * reason spelled out, so the list stays short and reviewable.</p>
 */
class RitualInputReachabilityTest {

    private static final Path ASSETS = Paths.get("src/main/resources/assets/occultism");
    private static final Path JAVA = Paths.get("src/main/java");

    private static final Stream<String> JAVA_PRODUCER_TABLES = Stream.of(
            "SpiritFireRecipeRegistry", "SpiritTradeRecipeRegistry", "SpiritJobRecipe",
            "WonderingTrades", "DimensionalMineshaftRecipe", "ModRecipes");

    /**
     * Inputs that are deliberately allowed to have no producer in the modelled systems.
     *
     * <p>Both are real gaps found by the audit and left in place on purpose: neither has an
     * upstream counterpart, any documentation, or an obvious material in 1.12, so giving them a
     * source would be inventing a design decision rather than porting one. They stay listed here
     * so the audit keeps passing for everything else while the choice is still open.</p>
     */
    /** Packages whose code drops items on death or on use; see {@link #dropAndWorldGenNames}. */
    private static final Set<String> DROP_PACKAGES = new LinkedHashSet<>(Arrays.asList(
            "entity", "handler", "item", "capability"));

    private static final Set<String> KNOWN_UNSOURCED = new LinkedHashSet<>(Arrays.asList(
            // Coloured impure chalks still missing a dye recipe: their 1.21.1 recipes need vanilla
            // items that do not exist in 1.12 (torchflower, glow berries, glow ink sac, tube coral),
            // and the substitutes are the maintainer's call.
            "occultism:chalk_blue_impure", "occultism:chalk_cyan_impure",
            "occultism:chalk_orange_impure", "occultism:chalk_red_impure",
            "occultism:dark_golden_sacrificial_bowl", // consumed by craft_dark_iesnium_sacrificial_bowl
            "occultism:netherite_scrap_dust"));      // consumed by possess_hoglin; a 1.16 material

    @Test
    void everyImpureChalkHasARecipeThatMakesIt() throws Exception {
        // Only white impure chalk could be crafted: the other fifteen colours existed as items,
        // were consumed by spirit fire and by pentacles, and nothing produced them. That made every
        // coloured chalk unobtainable and most pentacles impossible to draw. Checked here so a
        // missing colour cannot hide behind the spirit fire table again.
        Set<String> declared = new LinkedHashSet<>();
        Matcher chalkField = Pattern.compile("public static final Item \\w+\\s*=\\s*\\n?\\s*\\w+\\("
                + "\"(chalk_[a-z_]+_impure)\"")
                .matcher(new String(Files.readAllBytes(
                        Paths.get("src/main/java/com/occultism/occultism/registry/ModItems.java")),
                        StandardCharsets.UTF_8));
        while (chalkField.find()) {
            declared.add(chalkField.group(1));
        }
        assertTrue(declared.size() >= 16, "Expected the full impure chalk palette, found " + declared.size());

        Set<String> craftable = new LinkedHashSet<>();
        for (Path file : jsonFiles(ASSETS.resolve("recipes"))) {
            String result = itemOf(readJson(file).getAsJsonObject().get("result"));
            if (result != null && result.startsWith("occultism:chalk_") && result.endsWith("_impure")) {
                craftable.add(result.substring("occultism:".length()));
            }
        }
        Set<String> missing = new LinkedHashSet<>(declared);
        missing.removeAll(craftable);
        for (String known : KNOWN_UNSOURCED) {
            if (known.startsWith("occultism:")) {
                missing.remove(known.substring("occultism:".length()));
            }
        }
        assertTrue(missing.isEmpty(), "Impure chalks nothing can craft: " + missing);
    }

    @Test
    void everyRitualOrRecipeInputCanBeObtained() throws Exception {
        Map<String, List<String>> inputs = new LinkedHashMap<>();
        Set<String> produced = new LinkedHashSet<>();

        collectRecipes(inputs, produced);
        collectRituals(inputs, produced);
        produced.addAll(javaProducerNames());
        produced.addAll(dropAndWorldGenNames());

        Set<String> unsourced = new LinkedHashSet<>();
        for (String input : inputs.keySet()) {
            if (produced.contains(input) || KNOWN_UNSOURCED.contains(input)) {
                continue;
            }
            unsourced.add(input + " (used by " + inputs.get(input).get(0) + ")");
        }

        assertTrue(unsourced.isEmpty(),
                "Ritual/recipe inputs nothing can supply, so the chain dead-ends: " + unsourced);
    }

    // -- resource data -----------------------------------------------------------------------

    private static void collectRecipes(Map<String, List<String>> inputs, Set<String> produced)
            throws IOException {
        for (Path file : jsonFiles(ASSETS.resolve("recipes"))) {
            JsonObject recipe = readJson(file).getAsJsonObject();
            String result = itemOf(recipe.get("result"));
            if (result != null) {
                produced.add(result);
            }
            List<JsonElement> keys = new ArrayList<>();
            if (recipe.has("key")) {
                keys.addAll(recipe.getAsJsonObject("key").entrySet().stream()
                        .map(Map.Entry::getValue).collect(java.util.stream.Collectors.toList()));
            }
            if (recipe.has("ingredients")) {
                for (JsonElement ingredient : recipe.getAsJsonArray("ingredients")) {
                    addIngredient(ingredient, keys);
                }
            }
            for (JsonElement key : keys) {
                for (JsonElement single : flatten(key)) {
                    String item = itemOf(single);
                    if (item != null && item.startsWith("occultism:")) {
                        inputs.computeIfAbsent(item, k -> new ArrayList<>()).add("recipe:" + file.getFileName());
                    }
                }
            }
        }
    }

    private static void collectRituals(Map<String, List<String>> inputs, Set<String> produced)
            throws IOException {
        try (InputStreamReader reader = reader("/assets/occultism/rituals/rituals.json")) {
            for (JsonElement element : new JsonParser().parse(reader).getAsJsonArray()) {
                JsonObject ritual = element.getAsJsonObject();
                String id = ritual.get("id").getAsString();
                String result = itemOf(ritual.get("result"));
                if (result != null) {
                    produced.add(result);
                }
                List<JsonElement> singles = new ArrayList<>();
                if (ritual.has("activation_item")) {
                    for (JsonElement single : flatten(ritual.get("activation_item"))) {
                        singles.add(single);
                    }
                }
                if (ritual.has("ingredients")) {
                    for (JsonElement ingredient : ritual.getAsJsonArray("ingredients")) {
                        addIngredient(ingredient, singles);
                    }
                }
                for (JsonElement single : singles) {
                    String item = itemOf(single);
                    if (item != null && item.startsWith("occultism:")) {
                        inputs.computeIfAbsent(item, k -> new ArrayList<>()).add("ritual:" + id);
                    }
                }
            }
        }
    }

    // -- java-side sources -------------------------------------------------------------------

    private static Set<String> javaProducerNames() throws IOException {
        Set<String> names = new LinkedHashSet<>();
        for (String table : JAVA_PRODUCER_TABLES.collect(java.util.stream.Collectors.toList())) {
            for (Path file : javaFiles()) {
                if (file.getFileName().toString().equals(table + ".java")) {
                    names.addAll(modItemNames(readText(file)));
                }
            }
        }
        return names;
    }

    /**
     * Items dropped by entity code or placed by world generation. The registry field name is a
     * faithful upper-case spelling of the registry path, so lower-casing it recovers the id.
     */
    private static Set<String> dropAndWorldGenNames() throws IOException {
        Set<String> names = new LinkedHashSet<>();
        Pattern dropCall = Pattern.compile("dropItem|entityDropItem|spawnAtLocation|addDrop");
        for (Path file : javaFiles()) {
            // Only where drops actually happen. Scanning every file that merely mentions a drop
            // call made the terminal blocks look obtainable just because a tile entity refers to
            // them, which is exactly the mistake this test is supposed to catch.
            if (!DROP_PACKAGES.contains(file.getParent().getFileName().toString())) {
                continue;
            }
            String source = readText(file);
            if (dropCall.matcher(source).find()) {
                names.addAll(modItemNames(source));
            }
        }
        for (Path file : jsonFiles(ASSETS.resolve("loot_tables"))) {
            Matcher matcher = Pattern.compile("\"name\"\\s*:\\s*\"([a-z_]+:[a-z_/0-9]+)\"")
                    .matcher(readText(file));
            while (matcher.find()) {
                names.add(matcher.group(1));
            }
        }
        for (Path file : javaFiles()) {
            Matcher matcher = Pattern.compile("\"([a-z_/0-9]*natural[a-z_/0-9]*)\"").matcher(readText(file));
            while (matcher.find()) {
                names.add("occultism:" + matcher.group(1));
            }
        }
        return names;
    }

    private static Set<String> modItemNames(String source) {
        // Registry names are read from the declarations instead of lower-casing the field name:
        // after a rename (chalk_yellow -> chalk_gold) the field and the registry path differ.
        Set<String> names = new LinkedHashSet<>();
        Matcher matcher = Pattern.compile("Mod(?:Items|Blocks)\\.([A-Z_0-9]+)").matcher(source);
        while (matcher.find()) {
            String registryName = FIELD_TO_REGISTRY.get(matcher.group(1));
            if (registryName != null) {
                names.add("occultism:" + registryName);
            }
        }
        return names;
    }

    /** Field name -> registry path, parsed once from the registry classes. */
    private static final java.util.Map<String, String> FIELD_TO_REGISTRY = buildFieldMap();

    private static java.util.Map<String, String> buildFieldMap() {
        java.util.Map<String, String> map = new java.util.LinkedHashMap<>();
        Pattern declaration = Pattern.compile(
                "public static final \\w+ ([A-Z_0-9]+)\\s*=\\s*\\n?\\s*\\w+\\(\"([a-z_0-9/]+)\"");
        try {
            for (Path file : new Path[] {
                    Paths.get("src/main/java/com/occultism/occultism/registry/ModItems.java"),
                    Paths.get("src/main/java/com/occultism/occultism/registry/ModBlocks.java") }) {
                Matcher matcher = declaration.matcher(
                        new String(Files.readAllBytes(file), StandardCharsets.UTF_8));
                while (matcher.find()) {
                    map.put(matcher.group(1), matcher.group(2));
                }
            }
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
        return map;
    }

    // -- helpers -----------------------------------------------------------------------------

    private static void addIngredient(JsonElement ingredient, List<JsonElement> sink) {
        for (JsonElement single : flatten(ingredient)) {
            sink.add(single);
        }
    }

    /** A slot may be a single stack, an object, or an array of alternatives. */
    private static List<JsonElement> flatten(JsonElement element) {
        List<JsonElement> singles = new ArrayList<>();
        if (element == null || element.isJsonNull()) {
            return singles;
        }
        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            for (JsonElement child : array) {
                singles.addAll(flatten(child));
            }
            return singles;
        }
        singles.add(element);
        return singles;
    }

    private static String itemOf(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        // Ingredients are written as bare id strings ("occultism:copper_block") far more often
        // than as objects. Treating only objects as items made this whole audit skip them.
        if (element.isJsonPrimitive()) {
            return element.getAsString();
        }
        if (!element.isJsonObject()) {
            return null;
        }
        JsonObject object = element.getAsJsonObject();
        if (object.has("item")) {
            return object.get("item").getAsString();
        }
        if (object.has("id")) {
            return object.get("id").getAsString();
        }
        return null;
    }

    private static JsonElement readJson(Path file) throws IOException {
        return new JsonParser().parse(readText(file));
    }

    private static String readText(Path file) throws IOException {
        return new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
    }

    private static InputStreamReader reader(String resource) {
        java.io.InputStream stream = RitualInputReachabilityTest.class.getResourceAsStream(resource);
        assertTrue(stream != null, "Missing resource " + resource);
        return new InputStreamReader(stream, StandardCharsets.UTF_8);
    }

    private static List<Path> jsonFiles(Path root) throws IOException {
        return collectFiles(root, ".json");
    }

    private static List<Path> javaFiles() throws IOException {
        return collectFiles(JAVA, ".java");
    }

    private static List<Path> collectFiles(Path root, String suffix) throws IOException {
        List<Path> files = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(root)) {
            stream.filter(path -> path.toString().endsWith(suffix)).forEach(files::add);
        }
        return files;
    }
}
