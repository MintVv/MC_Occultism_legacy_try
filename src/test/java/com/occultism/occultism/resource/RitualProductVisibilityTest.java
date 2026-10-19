package com.occultism.occultism.resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Rituals must advertise what they actually produce.
 *
 * <p>Both essences looked missing in JEI. They were not: the unbound spirit's loot table drops them,
 * and the ritual that summons it carries that item as its result. Two things hid it. The JEI wrapper
 * always reported the ritual dummy as the recipe output, so no page could ever be found by looking up
 * a product, and the two unbound rituals stored the dummy in their own result field instead of the
 * essence. Both are fixed; these checks keep the data honest.</p>
 */
class RitualProductVisibilityTest {

    private static final Path RITUALS = Paths.get("src/main/resources/assets/occultism/rituals/rituals.json");
    private static final Path WRAPPER =
            Paths.get("src/main/java/com/occultism/occultism/compat/jei/RitualRecipeWrapper.java");
    private static JsonArray rituals;

    @BeforeAll
    static void load() throws IOException {
        String json = new String(Files.readAllBytes(RITUALS), StandardCharsets.UTF_8);
        JsonElement root = new JsonParser().parse(json);
        rituals = root.isJsonArray() ? root.getAsJsonArray() : root.getAsJsonObject().getAsJsonArray("rituals");
    }

    @Test
    void theUnboundRitualsProduceTheirEssence() {
        assertEquals("occultism:afrit_essence", resultId("summon_unbound_afrit"),
                "the unbound afrit ritual must advertise afrit essence, as upstream does");
        assertEquals("occultism:marid_essence", resultId("summon_unbound_marid"),
                "the unbound marid ritual must advertise marid essence, as upstream does");
    }

    @Test
    void everyEssenceHasARitualThatProducesIt() {
        for (String essence : new String[] {"afrit_essence", "marid_essence"}) {
            List<String> producers = new ArrayList<>();
            for (JsonElement element : rituals) {
                JsonObject ritual = element.getAsJsonObject();
                if (("occultism:" + essence).equals(resultId(ritual))) {
                    producers.add(id(ritual));
                }
            }
            assertTrue(!producers.isEmpty(), essence + " has no ritual result pointing at it");
        }
    }

    @Test
    void theJeiWrapperReportsTheRealProduct() throws IOException {
        String wrapper = new String(Files.readAllBytes(WRAPPER), StandardCharsets.UTF_8);
        assertTrue(wrapper.contains("ritual.getResult().createStack()"),
                "the JEI page must use the ritual result, not always the dummy");
        assertTrue(wrapper.contains("getRitualDummy()"),
                "the dummy must stay as the fallback for rituals without an item product");
    }

    @Test
    void mostRitualsAdvertiseARealProduct() {
        int real = 0;
        int dummy = 0;
        for (JsonElement element : rituals) {
            String result = resultId(element.getAsJsonObject());
            if (result == null) {
                continue;
            }
            if (result.startsWith("occultism:ritual_dummy")) {
                dummy++;
            } else {
                real++;
            }
        }
        assertTrue(real > dummy * 5,
                "the vast majority of rituals produce a real item; found " + real + " real against " + dummy);
    }

    private static String id(JsonObject ritual) {
        return ritual.has("id") ? ritual.get("id").getAsString() : ritual.get("ritual_id").getAsString();
    }

    private static String resultId(String ritualId) {
        for (JsonElement element : rituals) {
            JsonObject ritual = element.getAsJsonObject();
            if (ritualId.equals(id(ritual))) {
                return resultId(ritual);
            }
        }
        throw new AssertionError("ritual not found: " + ritualId);
    }

    private static String resultId(JsonObject ritual) {
        if (!ritual.has("result") || !ritual.get("result").isJsonObject()) {
            return null;
        }
        JsonObject result = ritual.getAsJsonObject("result");
        if (result.has("id")) {
            return result.get("id").getAsString();
        }
        return result.has("item") ? result.get("item").getAsString() : null;
    }
}
