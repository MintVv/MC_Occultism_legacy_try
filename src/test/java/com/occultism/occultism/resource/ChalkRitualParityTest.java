package com.occultism.occultism.resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Void and rainbow chalk are ritual-only, and their rituals must match upstream.
 *
 * <p>Both are crafted by a ritual on the eldritch pentacle activated with a chalk brush; upstream
 * has no crafting recipe for either. Checked against the pinned 1.21.1 commit
 * {@code 4877cde} ({@code recipe/ritual/misc_chalk_void.json} and {@code misc_chalk_rainbow.json}),
 * the ingredients, pentacle and activation item already matched, but the void ritual sacrificed a
 * squid where upstream sacrifices a human.</p>
 *
 * <p>Ritual durations are deliberately not asserted here: this port scales them up across the
 * board (the crusher ritual runs 800 ticks against upstream's 60, these two 8240 against 780), so
 * pinning one value would freeze a known difference rather than a parity rule.</p>
 */
class ChalkRitualParityTest {

    private static final Path RITUALS = Paths.get("src/main/resources/assets/occultism/rituals/rituals.json");
    private static JsonArray rituals;

    @BeforeAll
    static void load() throws IOException {
        String json = new String(Files.readAllBytes(RITUALS), StandardCharsets.UTF_8);
        JsonElement root = new JsonParser().parse(json);
        rituals = root.isJsonArray() ? root.getAsJsonArray() : root.getAsJsonObject().getAsJsonArray("rituals");
    }

    @Test
    void bothChalkRitualsUseTheUpstreamPentacleAndActivation() {
        for (String id : new String[] {"misc_chalk_void", "misc_chalk_rainbow"}) {
            JsonObject ritual = find(id);
            assertEquals("occultism:craft", ritual.get("ritual_type").getAsString(), id);
            assertEquals("occultism:contact_eldritch_spirit", ritual.get("pentacle_id").getAsString(), id);
            assertEquals("occultism:brush", ritual.get("activation_item").getAsString(), id);
            assertEquals("occultism:" + id.replace("misc_", ""), resultId(ritual), id + " result");
        }
    }

    @Test
    void theVoidRitualSacrificesAHumanLikeUpstream() {
        assertEquals("occultism:humans", sacrificeTag("misc_chalk_void"),
                "upstream misc_chalk_void sacrifices the occultism:humans tag");
        assertEquals("ritual.occultism.sacrifice.humans", find("misc_chalk_void")
                .getAsJsonObject("entity_to_sacrifice").get("display_name").getAsString());
    }

    @Test
    void theRainbowRitualSacrificesASheepLikeUpstream() {
        assertEquals("c:sheep", sacrificeTag("misc_chalk_rainbow"),
                "upstream misc_chalk_rainbow sacrifices the c:sheep tag");
    }

    @Test
    void theVoidRitualTakesFourChalks() {
        assertEquals(Arrays.asList("chalk_white", "chalk_light_gray", "chalk_gray", "chalk_black"),
                ingredients("misc_chalk_void"));
    }

    @Test
    void theRainbowRitualTakesTwelveChalks() {
        assertEquals(Arrays.asList("chalk_red", "chalk_brown", "chalk_orange", "chalk_gold",
                        "chalk_lime", "chalk_green", "chalk_cyan", "chalk_blue", "chalk_light_blue",
                        "chalk_pink", "chalk_magenta", "chalk_purple"),
                ingredients("misc_chalk_rainbow"));
    }

    @Test
    void neitherChalkHasACraftingRecipe() throws IOException {
        Path recipes = Paths.get("src/main/resources/assets/occultism/recipes");
        for (String name : new String[] {"chalk_void.json", "chalk_rainbow.json"}) {
            assertEquals(false, Files.exists(recipes.resolve(name)),
                    name + " must stay ritual-only, as upstream");
        }
    }

    private static JsonObject find(String id) {
        for (JsonElement element : rituals) {
            JsonObject ritual = element.getAsJsonObject();
            String current = ritual.has("id") ? ritual.get("id").getAsString()
                    : ritual.has("ritual_id") ? ritual.get("ritual_id").getAsString() : null;
            if (id.equals(current)) {
                return ritual;
            }
        }
        throw new AssertionError("ritual not found: " + id);
    }

    private static String sacrificeTag(String id) {
        return find(id).getAsJsonObject("entity_to_sacrifice").get("tag").getAsString();
    }

    private static String resultId(JsonObject ritual) {
        JsonObject result = ritual.getAsJsonObject("result");
        return result.has("id") ? result.get("id").getAsString() : result.get("item").getAsString();
    }

    private static List<String> ingredients(String id) {
        List<String> names = new ArrayList<>();
        for (JsonElement element : find(id).getAsJsonArray("ingredients")) {
            String value = element.isJsonObject() ? element.getAsJsonObject().get("item").getAsString()
                    : element.getAsString();
            names.add(value.replace("occultism:", ""));
        }
        return names;
    }
}
