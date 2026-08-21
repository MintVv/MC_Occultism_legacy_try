package com.occultism.occultism.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

class WildParrotPortContractTest {

    private static final Path ASSET_ROOT = Paths.get("src", "main", "resources", "assets", "occultism");

    @Test
    void wildParrotRitualMatchesUpstreamSemanticsOnVanilla112Entity() throws IOException {
        JsonObject ritual = findRitual("summon_wild_parrot");

        assertEquals("occultism:summon", ritual.get("ritual_type").getAsString());
        assertEquals("occultism:book_of_binding_bound_foliot", ritual.get("activation_item").getAsString());
        assertEquals("occultism:possess_foliot", ritual.get("pentacle_id").getAsString());
        assertEquals("minecraft:parrot", ritual.get("entity_to_summon").getAsString());
        assertEquals("c:chickens", ritual.getAsJsonObject("entity_to_sacrifice").get("tag").getAsString());
        assertEquals(600, ritual.get("duration_ticks").getAsInt());

        Set<String> ingredients = new HashSet<>();
        for (JsonElement element : ritual.getAsJsonArray("ingredients")) {
            ingredients.add(element.getAsString());
        }
        assertEquals(6, ingredients.size());
        assertTrue(ingredients.contains("#c:feathers"));
        assertTrue(ingredients.contains("#c:dyes/green"));
        assertTrue(ingredients.contains("#c:dyes/yellow"));
        assertTrue(ingredients.contains("#c:dyes/red"));
        assertTrue(ingredients.contains("#c:dyes/blue"));
        assertTrue(ingredients.contains("#c:eggs"));
        assertEquals("occultism:ritual_dummy/summon_wild_parrot",
                ritual.getAsJsonObject("ritual_dummy").get("id").getAsString());
    }

    @Test
    void wildParrotIsUntamedAndDummyIsRegisteredInBothLanguages() throws IOException {
        JsonObject ritual = findRitual("summon_wild_parrot");
        assertFalse("occultism:summon_tamed".equals(ritual.get("ritual_type").getAsString()));

        String registry = readUtf8(Paths.get("src", "main", "java", "com", "occultism", "occultism",
                "registry", "ModItems.java"));
        assertTrue(registry.contains("item(\"ritual_dummy/summon_wild_parrot\""));
        assertTrue(registry.contains("RITUAL_DUMMY_SUMMON_WILD_PARROT"));

        String english = readUtf8(ASSET_ROOT.resolve("lang").resolve("en_us.lang"));
        String chinese = readUtf8(ASSET_ROOT.resolve("lang").resolve("zh_cn.lang"));
        assertTrue(english.contains("item.occultism.ritual_dummy.summon_wild_parrot.name="));
        assertTrue(english.contains("item.occultism.ritual_dummy.summon_wild_parrot.tooltip="));
        assertTrue(chinese.contains("item.occultism.ritual_dummy.summon_wild_parrot.name="));
        assertTrue(chinese.contains("item.occultism.ritual_dummy.summon_wild_parrot.tooltip="));
    }

    private static JsonObject findRitual(String id) throws IOException {
        Path path = ASSET_ROOT.resolve("rituals").resolve("rituals.json");
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            JsonArray rituals = new JsonParser().parse(reader).getAsJsonArray();
            for (JsonElement element : rituals) {
                JsonObject ritual = element.getAsJsonObject();
                if (id.equals(ritual.get("id").getAsString())) {
                    assertNotNull(ritual);
                    return ritual;
                }
            }
        }
        throw new AssertionError("Missing ritual definition: " + id);
    }

    private static String readUtf8(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
