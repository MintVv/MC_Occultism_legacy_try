package com.occultism.occultism.resource;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.occultism.occultism.entity.WonderingTrades;
import com.occultism.occultism.mineshaft.DimensionalMineshaftRecipe;
import com.occultism.occultism.registry.ModRecipes;
import com.occultism.occultism.ritual.RitualBundleLoadTest;
import com.occultism.occultism.ritual.SpiritFireRecipeRegistry;
import com.occultism.occultism.spirit.SpiritJobRecipe;
import com.occultism.occultism.spirit.SpiritJobType;
import com.occultism.occultism.spirit.SpiritTradeRecipeRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class RitualInputReachabilityTest {
    private static final Path ASSETS = Path.of("src/main/resources/assets/occultism");
    private static final Path JAVA = Path.of("src/main/java");
    private static final Set<String> DROP_PACKAGES = Set.of("entity", "handler", "item", "capability");
    private static final Map<String, String> FIELD_TO_REGISTRY = buildFieldMap();

    @BeforeAll
    static void bootstrap() throws Exception {
        RitualBundleLoadTest.bootstrap();
        ModRecipes.register();
    }

    @Test
    void everyImpureChalkHasARecipeThatMakesIt() throws Exception {
        Set<String> declared = new LinkedHashSet<>();
        Matcher matcher = Pattern.compile("public static final Item \\w+\\s*=\\s*\\w+\\("
                + "\"(chalk_[a-z_]+_impure)\"").matcher(readText(
                        Path.of("src/main/java/com/occultism/occultism/registry/ModItems.java")));
        while (matcher.find()) {
            declared.add("occultism:" + matcher.group(1));
        }
        assertEquals(16, declared.size());
        Set<String> crafted = new LinkedHashSet<>();
        for (Path file : files(ASSETS.resolve("recipes"), ".json")) {
            crafted.add(itemOf(readJson(file).get("result")));
        }
        declared.removeAll(crafted);
        assertTrue(declared.isEmpty(), "Impure chalks without crafting recipes: " + declared);
    }

    @Test
    void everyRitualOrRecipeInputHasADeclaredSource() throws Exception {
        Map<String, List<String>> inputs = new LinkedHashMap<>();
        Set<String> produced = new LinkedHashSet<>();
        for (Path file : files(ASSETS.resolve("recipes"), ".json")) {
            JsonObject recipe = readJson(file);
            produced.add(itemOf(recipe.get("result")));
            if (recipe.has("key")) {
                recipe.getAsJsonObject("key").entrySet().forEach(entry ->
                        collectInput(entry.getValue(), file.getFileName().toString(), inputs));
            }
            if (recipe.has("ingredients")) {
                collectInput(recipe.get("ingredients"), file.getFileName().toString(), inputs);
            }
        }
        JsonElement bundle = new JsonParser().parse(readText(ASSETS.resolve("rituals/rituals.json")));
        for (JsonElement element : bundle.getAsJsonArray()) {
            JsonObject ritual = element.getAsJsonObject();
            String id = ritual.get("id").getAsString();
            if (!ritual.has("entity_to_summon") && !ritual.has("spirit_job_type") && !ritual.has("spirit_tier")) {
                produced.add(itemOf(ritual.get("result")));
            }
            collectInput(ritual.get("activation_item"), id, inputs);
            collectInput(ritual.get("ingredients"), id, inputs);
            collectInput(ritual.get("item_to_use"), id, inputs);
        }
        produced.addAll(javaProducerNames());
        produced.addAll(dropAndWorldGenNames());
        Set<String> missing = new LinkedHashSet<>();
        for (var input : inputs.entrySet()) {
            if (!produced.contains(input.getKey())) {
                missing.add(input.getKey() + " (" + input.getValue().getFirst() + ")");
            }
        }
        assertTrue(missing.isEmpty(), "Inputs without declared producers: " + missing);
    }

    @Test
    void runtimeInputsAreNotMistakenForOutputs() {
        Set<String> outputs = javaProducerNames();
        assertTrue(outputs.contains("occultism:iron_dust"));
        assertTrue(outputs.contains("occultism:netherite_dust"));
        assertFalse(outputs.contains("occultism:raw_silver"));
        assertFalse(outputs.contains("occultism:netherite_scrap_dust"));
        assertFalse(outputs.contains("occultism:chalk_red_impure"));
    }

    private static Set<String> javaProducerNames() {
        Set<String> names = new LinkedHashSet<>();
        SpiritFireRecipeRegistry.getRecipes().forEach(recipe -> addOutput(names, recipe.getOutput()));
        for (SpiritJobType type : List.of(SpiritJobType.CRUSHER, SpiritJobType.CRYSTALLIZER)) {
            SpiritJobRecipe.getRecipes(type).forEach(recipe -> addOutput(names, recipe.getOutput()));
        }
        SpiritTradeRecipeRegistry.getRecipes().forEach(recipe -> addOutput(names, recipe.getOutput()));
        WonderingTrades.createOtherworldTrades().forEach(recipe -> addOutput(names, recipe.getItemToSell()));
        DimensionalMineshaftRecipe.getRecipes().forEach(recipe ->
                recipe.getOutputs().forEach(output -> addOutput(names, output.getStack())));
        FurnaceRecipes.instance().getSmeltingList().values().forEach(stack -> addOutput(names, stack));
        return names;
    }

    private static void addOutput(Set<String> names, ItemStack output) {
        if (!output.isEmpty() && output.getItem().getRegistryName() != null) {
            names.add(output.getItem().getRegistryName().toString());
        }
    }

    private static Set<String> dropAndWorldGenNames() throws IOException {
        Set<String> names = new LinkedHashSet<>();
        Pattern dropCall = Pattern.compile("dropItem|entityDropItem|spawnAtLocation|addDrop");
        for (Path file : files(JAVA, ".java")) {
            if (DROP_PACKAGES.contains(file.getParent().getFileName().toString())) {
                String source = readText(file);
                if (dropCall.matcher(source).find()) {
                    names.addAll(modItemNames(source));
                }
            }
        }
        for (Path file : files(ASSETS.resolve("loot_tables"), ".json")) {
            Matcher matcher = Pattern.compile("\"name\"\\s*:\\s*\"([a-z_]+:[a-z_/0-9]+)\"")
                    .matcher(readText(file));
            while (matcher.find()) {
                names.add(matcher.group(1));
            }
        }
        for (Path file : files(JAVA, ".java")) {
            Matcher matcher = Pattern.compile("\"([a-z_/0-9]*natural[a-z_/0-9]*)\"").matcher(readText(file));
            while (matcher.find()) {
                names.add("occultism:" + matcher.group(1));
            }
        }
        return names;
    }

    private static Set<String> modItemNames(String source) {
        Set<String> names = new LinkedHashSet<>();
        Matcher matcher = Pattern.compile("Mod(?:Items|Blocks)\\.([A-Z_0-9]+)").matcher(source);
        while (matcher.find()) {
            String name = FIELD_TO_REGISTRY.get(matcher.group(1));
            if (name != null) {
                names.add("occultism:" + name);
            }
        }
        return names;
    }

    private static Map<String, String> buildFieldMap() {
        Map<String, String> map = new LinkedHashMap<>();
        Pattern declaration = Pattern.compile("public static final \\w+ ([A-Z_0-9]+)\\s*=\\s*\\w+\\(\"([a-z_0-9/]+)\"");
        try {
            for (String name : List.of("ModItems", "ModBlocks")) {
                Matcher matcher = declaration.matcher(readText(Path.of(
                        "src/main/java/com/occultism/occultism/registry/" + name + ".java")));
                while (matcher.find()) {
                    map.put(matcher.group(1), matcher.group(2));
                }
            }
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
        return map;
    }

    private static void collectInput(JsonElement element, String source, Map<String, List<String>> inputs) {
        if (element == null || element.isJsonNull()) {
            return;
        }
        if (element.isJsonArray()) {
            element.getAsJsonArray().forEach(child -> collectInput(child, source, inputs));
            return;
        }
        String item = itemOf(element);
        if (item != null && item.startsWith("occultism:")) {
            inputs.computeIfAbsent(item, key -> new ArrayList<>()).add(source);
        }
    }

    private static String itemOf(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        if (element.isJsonPrimitive()) {
            return element.getAsString();
        }
        JsonObject object = element.getAsJsonObject();
        return object.has("item") ? object.get("item").getAsString()
                : object.has("id") ? object.get("id").getAsString() : null;
    }

    private static JsonObject readJson(Path file) throws IOException {
        return new JsonParser().parse(readText(file)).getAsJsonObject();
    }

    private static String readText(Path file) throws IOException {
        return Files.readString(file, StandardCharsets.UTF_8);
    }

    private static List<Path> files(Path root, String suffix) throws IOException {
        try (Stream<Path> stream = Files.walk(root)) {
            return stream.filter(path -> path.toString().endsWith(suffix)).toList();
        }
    }
}
