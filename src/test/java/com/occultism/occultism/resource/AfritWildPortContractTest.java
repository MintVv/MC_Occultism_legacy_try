package com.occultism.occultism.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.occultism.occultism.ritual.PentacleDefinition;
import com.occultism.occultism.ritual.PentacleDetector;
import org.junit.jupiter.api.Test;

class AfritWildPortContractTest {

    private static final Path ROOT = Paths.get("src", "main", "resources", "assets", "occultism");
    private static final Path JAVA_ROOT = Paths.get("src", "main", "java", "com", "occultism", "occultism");

    @Test
    void wildAfritRitualUsesDedicated112EntityAndRecipeContract() throws IOException {
        JsonObject ritual = findRitual("summon_wild_afrit");
        assertEquals("occultism:summon", ritual.get("ritual_type").getAsString());
        assertEquals("occultism:book_of_binding_bound_afrit", ritual.get("activation_item").getAsString());
        assertEquals("occultism:summon_wild_afrit", ritual.get("pentacle_id").getAsString());
        assertEquals("occultism:afrit_wild", ritual.get("entity_to_summon").getAsString());
        assertEquals("c:cows", ritual.getAsJsonObject("entity_to_sacrifice").get("tag").getAsString());
        assertEquals(1200, ritual.get("duration_ticks").getAsInt());
        Set<String> ingredients = new HashSet<>();
        for (JsonElement ingredient : ritual.getAsJsonArray("ingredients")) {
            ingredients.add(ingredient.getAsString());
        }
        assertEquals(new HashSet<>(Arrays.asList("#c:netherracks", "#c:gems/quartz",
                "minecraft:flint_and_steel", "#c:gunpowders")), ingredients);
        assertEquals("occultism:ritual_dummy/summon_wild_afrit",
                ritual.getAsJsonObject("ritual_dummy").get("id").getAsString());
    }

    @Test
    void dedicatedPentacleMatchesUpstreamGeometryAndSkullParaphernalia() throws IOException {
        PentacleDefinition definition = PentacleDetector.getDefinitionById("summon_wild_afrit");
        assertNotNull(definition);
        assertEquals(11, definition.getWidth());
        assertEquals(11, definition.getHeight());
        assertEquals(Arrays.asList("___________", "____PPP____", "___WCWCW___", "__W_WNW_W__",
                "_PCWP_PWCP_", "_PWZ_0_ZWP_", "_PCWP_PWCP_", "__W_WNW_W__", "___WCWCW___",
                "____PPP____", "___________"), Arrays.asList(definition.getPattern()));
        assertTrue(readJava("ritual", "PentacleDetector.java").contains("case 'N':"));
        assertTrue(readJava("ritual", "PentacleDetector.java").contains("case 'Z':"));
    }

    @Test
    void entityHasUpstreamAttributesAndBlazeEntourageImplementation() throws IOException {
        String source = readJava("entity", "EntityAfritWild.java");
        assertTrue(source.contains("MAX_HEALTH).setBaseValue(100.0D"));
        assertTrue(source.contains("ATTACK_DAMAGE).setBaseValue(8.0D"));
        assertTrue(source.contains("FOLLOW_RANGE).setBaseValue(50.0D"));
        assertTrue(source.contains("3 + this.rand.nextInt(6)"));
        assertTrue(source.contains("new EntityBlaze(this.world)"));
        assertTrue(source.contains("source.isFireDamage()"));
        assertTrue(source.contains("entities/afrit_wild"));
    }

    @Test
    void entityRegistrationRendererDummyAndLootAreWired() throws IOException {
        assertTrue(readJava("registry", "ModEntities.java").contains("\"afrit_wild\""));
        assertTrue(readJava("client/render", "ModEntityRenderers.java").contains("EntityAfritWild.class"));
        String items = readJava("registry", "ModItems.java");
        assertTrue(items.contains("RITUAL_DUMMY_SUMMON_WILD_AFRIT"));
        assertTrue(Files.exists(ROOT.resolve("models/item/ritual_dummy/summon_wild_afrit.json")));
        JsonObject loot = readJson(ROOT.resolve("loot_tables/entities/afrit_wild.json")).getAsJsonObject();
        JsonArray pools = loot.getAsJsonArray("pools");
        assertEquals(1, pools.size());
        assertEquals("occultism:afrit_essence",
                pools.get(0).getAsJsonObject().getAsJsonArray("entries").get(0).getAsJsonObject().get("name")
                        .getAsString());
    }

    @Test
    void wildAfritIsIncludedInTheUpstreamDemonDreamHealingTag() throws IOException {
        String source = readJava("item", "ItemDemonDreamFood.java");
        assertTrue(source.contains("EntityAfritWild"));
        assertTrue(source.contains("target instanceof EntityAfritWild"));
    }

    private static JsonObject findRitual(String id) throws IOException {
        JsonArray rituals = readJson(ROOT.resolve("rituals/rituals.json")).getAsJsonArray();
        for (JsonElement element : rituals) {
            JsonObject ritual = element.getAsJsonObject();
            if (id.equals(ritual.get("id").getAsString())) {
                return ritual;
            }
        }
        throw new AssertionError("Missing ritual definition: " + id);
    }

    private static JsonElement readJson(Path path) throws IOException {
        return new JsonParser().parse(new String(Files.readAllBytes(path), StandardCharsets.UTF_8));
    }

    private static String readJava(String directory, String file) throws IOException {
        return new String(Files.readAllBytes(JAVA_ROOT.resolve(directory).resolve(file)), StandardCharsets.UTF_8);
    }
}
