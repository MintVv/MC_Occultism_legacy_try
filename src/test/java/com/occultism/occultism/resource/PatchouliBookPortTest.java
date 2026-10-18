package com.occultism.occultism.resource;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.occultism.occultism.registry.ModBlocks;
import com.occultism.occultism.registry.ModItems;
import com.occultism.occultism.ritual.PentacleDetector;
import com.occultism.occultism.spirit.SpiritTradeRecipeRegistry;
import com.occultism.occultism.ritual.SpiritFireRecipeRegistry;
import net.minecraft.block.Block;
import net.minecraft.init.Bootstrap;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class PatchouliBookPortTest {
    private static final Path ASSETS = Paths.get("src/main/resources/assets/occultism");
    private static final Path BOOK = ASSETS.resolve("patchouli_books/dictionary_of_spirits");
    private static final Set<String> ITEMS = new HashSet<>();

    @BeforeAll
    static void bootstrap() throws Exception {
        Bootstrap.register();
        for (ResourceLocation name : Item.REGISTRY.getKeys()) {
            ITEMS.add(name.toString());
        }
        for (Class<?> owner : new Class<?>[] {ModItems.class, ModBlocks.class}) {
            for (Field field : owner.getFields()) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                Object value = field.get(null);
                if (value instanceof Item) {
                    ITEMS.add(((Item) value).getRegistryName().toString());
                } else if (value instanceof Block) {
                    ITEMS.add(((Block) value).getRegistryName().toString());
                    Block block = (Block) value;
                    if (!net.minecraftforge.fml.common.registry.ForgeRegistries.ITEMS.containsKey(block.getRegistryName())) {
                        net.minecraftforge.fml.common.registry.ForgeRegistries.ITEMS.register(
                                new net.minecraft.item.ItemBlock(block).setRegistryName(block.getRegistryName()));
                    }
                }
            }
        }
    }

    @Test
    void bothLanguagesContainEverySelectedEntryAndCategory() throws Exception {
        JsonObject index = json(ASSETS.resolve("dictionary/modonomicon_index.json"));
        Set<String> expected = new HashSet<>();
        for (JsonElement categoryElement : index.getAsJsonArray("categories")) {
            JsonObject category = categoryElement.getAsJsonObject();
            String id = category.get("id").getAsString();
            for (String locale : new String[] {"en_us", "zh_cn"}) {
                assertTrue(Files.isRegularFile(BOOK.resolve(locale + "/categories/" + id + ".json")));
            }
            for (JsonElement entry : category.getAsJsonArray("entries")) {
                expected.add(id + "/" + entry.getAsJsonObject().get("id").getAsString() + ".json");
            }
        }
        for (String locale : new String[] {"en_us", "zh_cn"}) {
            Path root = BOOK.resolve(locale + "/entries");
            try (Stream<Path> paths = Files.walk(root)) {
                assertEquals(expected, paths.filter(Files::isRegularFile)
                        .map(p -> root.relativize(p).toString().replace('\\', '/')).collect(Collectors.toSet()));
            }
        }
        assertEquals("occultism:dictionary_of_spirits", json(BOOK.resolve("book.json"))
                .get("custom_book_item").getAsString());
    }

    @Test
    void allPageInputsResolveAgainstTheLocalPort() throws Exception {
        Set<String> rituals = new HashSet<>();
        for (JsonElement ritual : new JsonParser().parse(read(ASSETS.resolve("rituals/rituals.json"))).getAsJsonArray()) {
            rituals.add(ritual.getAsJsonObject().get("id").getAsString());
        }
        Set<String> fireOutputs = SpiritFireRecipeRegistry.getRecipes().stream()
                .map(r -> r.getOutput().getItem().getRegistryName().toString()).collect(Collectors.toSet());
        try (Stream<Path> paths = Files.walk(BOOK)) {
            for (Path path : paths.filter(p -> p.toString().endsWith(".json")).collect(Collectors.toList())) {
                JsonObject document = json(path);
                if (document.has("icon")) {
                    assertItemOrTexture(document.get("icon").getAsString(), path);
                }
                if (!document.has("pages")) {
                    continue;
                }
                for (JsonElement element : document.getAsJsonArray("pages")) {
                    JsonObject page = element.getAsJsonObject();
                    String type = page.get("type").getAsString();
                    assertFalse(type.startsWith("modonomicon:"), path.toString());
                    if (page.has("item")) {
                        assertItemOrTexture(page.get("item").getAsString(), path);
                    }
                    if ("crafting".equals(type)) {
                        String recipe = page.get("recipe").getAsString().replace("occultism:", "");
                        assertTrue(Files.exists(ASSETS.resolve("recipes/" + recipe + ".json")), path.toString());
                    } else if ("occultism:ritual".equals(type)) {
                        assertTrue(rituals.contains(page.get("ritual").getAsString()), path.toString());
                    } else if ("occultism:spirit_fire".equals(type)) {
                        assertTrue(fireOutputs.contains(page.get("output").getAsString()), path.toString());
                    } else if ("occultism:spirit_trade".equals(type)) {
                        SpiritTradeRecipeRegistry.Recipe trade =
                                SpiritTradeRecipeRegistry.find(page.get("recipe").getAsString());
                        assertNotNull(trade, path.toString());
                        assertFalse(trade.getDisplayInputs().isEmpty(), path.toString());
                    } else if ("multiblock".equals(type)) {
                        assertNotNull(PentacleDetector.getDefinitionById(
                                page.get("multiblock_id").getAsString().replace("occultism:", "")), path.toString());
                        assertFalse(page.get("name").getAsString().isEmpty(), path.toString());
                    } else if ("image".equals(type)) {
                        for (JsonElement image : page.getAsJsonArray("images")) {
                            assertItemOrTexture(image.getAsString(), path);
                        }
                    }
                }
            }
        }
    }

    @Test
    void internalLinksAndAnchorsResolveInBothLanguages() throws Exception {
        Pattern links = Pattern.compile("\\$\\(l:([^)]*)\\)");
        for (String locale : new String[] {"en_us", "zh_cn"}) {
            Path entries = BOOK.resolve(locale + "/entries");
            try (Stream<Path> paths = Files.walk(entries)) {
                for (Path path : paths.filter(Files::isRegularFile).collect(Collectors.toList())) {
                    String content = read(path);
                    assertFalse(content.contains("entry://"), path.toString());
                    assertFalse(content.contains("item://"), path.toString());
                    assertFalse(content.contains("category://"), path.toString());
                    Matcher matcher = links.matcher(content);
                    while (matcher.find()) {
                        String link = matcher.group(1);
                        if (link.startsWith("https://") || link.startsWith("http://")) {
                            continue;
                        }
                        String[] target = link.split("#", 2);
                        Path destination = entries.resolve(target[0] + ".json");
                        assertTrue(Files.isRegularFile(destination), path + " -> " + link);
                        if (target.length > 1) {
                            boolean found = false;
                            for (JsonElement page : json(destination).getAsJsonArray("pages")) {
                                JsonObject object = page.getAsJsonObject();
                                found |= object.has("anchor") && target[1].equals(object.get("anchor").getAsString());
                            }
                            assertTrue(found, path + " -> " + link);
                        }
                    }
                }
            }
        }
    }

    @Test
    void tierZeroStabilizersHaveTheUpstreamPedestalConversion() throws Exception {
        for (String suffix : new String[] {"", "_dark"}) {
            JsonObject recipe = json(ASSETS.resolve("recipes/storage_stabilizer_tier0" + suffix + ".json"));
            assertEquals("minecraft:crafting_shapeless", recipe.get("type").getAsString());
            assertEquals(1, recipe.getAsJsonArray("ingredients").size());
            // Upstream 1.21.1 keeps the dark pedestal on a legacy id: otherstone_pedestal_silver.
            assertEquals(suffix.isEmpty() ? "occultism:otherstone_pedestal" : "occultism:otherstone_pedestal_silver",
                    recipe.getAsJsonArray("ingredients").get(0).getAsJsonObject().get("item").getAsString());
            assertEquals("occultism:storage_stabilizer_tier0" + suffix,
                    recipe.getAsJsonObject("result").get("item").getAsString());
        }
    }

    @Test
    void storageGuideDescribesTheLocalCapacityConfigurationAndRecipeTransfer() throws Exception {
        for (String locale : new String[] {"en_us", "zh_cn"}) {
            JsonObject entry = json(BOOK.resolve(locale + "/entries/storage/storage_controller.json"));
            StringBuilder text = new StringBuilder();
            for (JsonElement page : entry.getAsJsonArray("pages")) {
                JsonObject object = page.getAsJsonObject();
                if (object.has("text")) {
                    text.append(object.get("text").getAsString());
                }
            }
            String guide = text.toString();
            assertTrue(guide.contains("128"), locale);
            assertTrue(guide.contains("1024"), locale);
            assertTrue(guide.contains("config/occultism.cfg"), locale);
            assertTrue(guide.contains("3x3") && guide.contains("JEI"), locale);
            assertFalse(guide.contains("256000"), locale);
            assertFalse(guide.contains("occultism-server.toml"), locale);
        }
    }

    /**
     * The book is the only place a player can discover a ritual, but nothing used
     * to check the ritual -> book direction: 23 rituals shipped with no entry at
     * all, and the ones that did exist were found by hand.
     */
    @Test
    void everyRitualIsDocumentedInTheBook() throws Exception {
        Set<String> documented = new HashSet<>();
        try (Stream<Path> paths = Files.walk(BOOK.resolve("en_us/entries"))) {
            for (Path path : paths.filter(Files::isRegularFile).collect(Collectors.toList())) {
                for (JsonElement page : json(path).getAsJsonArray("pages")) {
                    JsonObject object = page.getAsJsonObject();
                    if ("occultism:ritual".equals(object.get("type").getAsString())) {
                        documented.add(object.get("ritual").getAsString());
                    }
                }
            }
        }

        Set<String> missing = new HashSet<>();
        for (JsonElement element : rituals()) {
            String id = element.getAsJsonObject().get("id").getAsString();
            if (!documented.contains(id)) {
                missing.add(id);
            }
        }
        assertTrue(missing.isEmpty(), "Rituals with no Dictionary entry: " + missing);
    }

    /**
     * A page whose text still holds a raw translation key renders that key to the
     * player. Copying an upstream entry while forgetting one of its language keys
     * is the usual cause.
     */
    @Test
    void noGeneratedPageKeepsAnUnresolvedLanguageKey() throws Exception {
        Pattern rawKey = Pattern.compile("^(book|item|tile|multiblock|entity|gui)\\.");
        Set<String> offenders = new HashSet<>();
        for (String locale : new String[] {"en_us", "zh_cn"}) {
            try (Stream<Path> paths = Files.walk(BOOK.resolve(locale + "/entries"))) {
                for (Path path : paths.filter(Files::isRegularFile).collect(Collectors.toList())) {
                    JsonObject document = json(path);
                    if (document.has("name") && rawKey.matcher(document.get("name").getAsString()).find()) {
                        offenders.add(path + " -> name " + document.get("name").getAsString());
                    }
                    for (JsonElement page : document.getAsJsonArray("pages")) {
                        JsonObject object = page.getAsJsonObject();
                        for (String field : new String[] {"text", "title"}) {
                            if (object.has(field) && rawKey.matcher(object.get(field).getAsString()).find()) {
                                offenders.add(path + " -> " + field + " " + object.get(field).getAsString());
                            }
                        }
                    }
                }
            }
        }
        assertTrue(offenders.isEmpty(), "Pages showing raw translation keys: " + offenders);
    }

    /**
     * PageRitual resolves the job, tier and sacrifice labels at draw time through
     * I18n, so a missing key never fails a build - it just prints the key. Ten job
     * labels and two sacrifice labels were missing exactly this way.
     */
    @Test
    void ritualPagesOnlyRenderLabelsThatResolve() throws Exception {
        Set<String> en = readLang("en_us").keySet();
        Set<String> zh = readLang("zh_cn").keySet();
        Set<String> missing = new HashSet<>();
        for (JsonElement element : rituals()) {
            JsonObject ritual = element.getAsJsonObject();
            String id = ritual.get("id").getAsString();
            if (ritual.has("spirit_job_type")) {
                String key = "job.occultism." + ritual.get("spirit_job_type").getAsString().split(":")[1];
                if (!en.contains(key) || !zh.contains(key)) {
                    missing.add(id + " -> " + key);
                }
            }
            if (ritual.has("spirit_tier")) {
                String key = "spirit_tier.occultism." + ritual.get("spirit_tier").getAsString().split(":")[1];
                if (!en.contains(key) || !zh.contains(key)) {
                    missing.add(id + " -> " + key);
                }
            }
            if (ritual.has("entity_to_sacrifice")
                    && ritual.getAsJsonObject("entity_to_sacrifice").has("display_name")) {
                String key = ritual.getAsJsonObject("entity_to_sacrifice").get("display_name").getAsString();
                if (key.startsWith("ritual.") && (!en.contains(key) || !zh.contains(key))) {
                    missing.add(id + " -> " + key);
                }
            }
        }
        assertTrue(missing.isEmpty(), "Ritual pages with unresolved labels: " + missing);
    }

    private static com.google.gson.JsonArray rituals() throws IOException {
        return new JsonParser().parse(read(ASSETS.resolve("rituals/rituals.json"))).getAsJsonArray();
    }

    private static java.util.Map<String, String> readLang(String locale) throws IOException {
        java.util.Map<String, String> result = new java.util.LinkedHashMap<>();
        for (String line : read(ASSETS.resolve("lang/" + locale + ".lang")).split("\\R")) {
            if (line.startsWith("#") || !line.contains("=")) {
                continue;
            }
            int split = line.indexOf('=');
            result.put(line.substring(0, split).trim(), line.substring(split + 1));
        }
        return result;
    }

    private static void assertItemOrTexture(String value, Path source) {
        if (value.endsWith(".png")) {
            String[] parts = value.split(":", 2);
            assertTrue(Files.exists(Paths.get("src/main/resources/assets").resolve(parts[0]).resolve(parts[1])),
                    source + " -> " + value);
            return;
        }
        String[] parts = value.split(":", 3);
        assertTrue(parts.length >= 2 && ITEMS.contains(parts[0] + ":" + parts[1].split("[#{]", 2)[0]),
                source + " -> " + value);
    }

    private static String read(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    private static JsonObject json(Path path) throws IOException {
        return new JsonParser().parse(read(path)).getAsJsonObject();
    }
}
