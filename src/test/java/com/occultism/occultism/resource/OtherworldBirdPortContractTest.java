package com.occultism.occultism.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
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

class OtherworldBirdPortContractTest {

    private static final Path ASSET_ROOT = Paths.get("src", "main", "resources", "assets", "occultism");
    private static final Path JAVA_ROOT = Paths.get("src", "main", "java", "com", "occultism", "occultism");

    @Test
    void bothOtherworldBirdRitualsSummonTheDedicated112Entity() throws IOException {
        JsonObject familiar = findRitual("familiar_otherworld_bird");
        JsonObject wild = findRitual("summon_wild_otherworld_bird");
        assertEquals("occultism:otherworld_bird", familiar.get("entity_to_summon").getAsString());
        assertEquals("occultism:otherworld_bird", wild.get("entity_to_summon").getAsString());
        assertEquals("occultism:summon_tamed", familiar.get("ritual_type").getAsString());
        assertEquals("occultism:summon", wild.get("ritual_type").getAsString());
        assertEquals("occultism:possess_djinni", familiar.get("pentacle_id").getAsString());
        assertEquals("occultism:possess_djinni", wild.get("pentacle_id").getAsString());
        assertEquals("c:parrots", wild.getAsJsonObject("entity_to_sacrifice").get("tag").getAsString());
        Set<String> wildIngredients = new HashSet<>();
        for (JsonElement ingredient : wild.getAsJsonArray("ingredients")) {
            wildIngredients.add(ingredient.getAsString());
        }
        assertTrue(wildIngredients.contains("minecraft:cobweb"));
        assertTrue(wildIngredients.contains("#minecraft:leaves"));
        assertEquals("occultism:ritual_dummy/summon_wild_otherworld_bird",
                wild.getAsJsonObject("ritual_dummy").get("id").getAsString());
    }

    @Test
    void entityKeepsParrotTamingButUsesUpstreamFamiliarMovementAndEffects() throws IOException {
        String source = readJava("entity", "EntityOtherworldBird.java");
        assertTrue(source.contains("extends EntityParrot implements IFamiliar"));
        assertTrue(source.contains("EntityAIFollowOwnerFlying"));
        assertTrue(source.contains("EntityAIWanderAvoidWaterFlying"));
        assertTrue(source.contains("getDistanceSq(owner)"));
        assertTrue(source.contains("ticksExisted % 10 == 0"));
        assertTrue(source.contains("MobEffects.JUMP_BOOST"));
        assertTrue(source.contains("ModPotions.FAIRY_SLOW_FALL"));
        assertTrue(source.contains("canSitOnShoulder()"));
        assertFalse(source.contains("EntityAILandOnOwnersShoulder"));
        assertTrue(source.contains("no local Double Jump effect"));
    }

    @Test
    void registrationRendererTextureAndWildDummyAreWiredInBothLanguages() throws IOException {
        String entities = readJava("registry", "ModEntities.java");
        assertTrue(entities.contains("\"otherworld_bird\""));
        assertTrue(entities.indexOf("\"otherworld_bird\"") > entities.indexOf("\"afrit_wild\""));
        String renderers = readJava("client/render", "ModEntityRenderers.java");
        assertTrue(renderers.contains("EntityOtherworldBird.class"));
        assertTrue(readJava("client/render", "RenderOtherworldBird.java")
                .contains("textures/entity/otherworld_bird.png"));
        assertTrue(Files.exists(ASSET_ROOT.resolve("textures/entity/otherworld_bird.png")));
        String items = readJava("registry", "ModItems.java");
        assertTrue(items.contains("RITUAL_DUMMY_SUMMON_WILD_OTHERWORLD_BIRD"));
        assertTrue(Files.exists(ASSET_ROOT.resolve("models/item/ritual_dummy/summon_wild_otherworld_bird.json")));
        String english = readUtf8(ASSET_ROOT.resolve("lang/en_us.lang"));
        String chinese = readUtf8(ASSET_ROOT.resolve("lang/zh_cn.lang"));
        assertTrue(english.contains("entity.occultism.otherworld_bird.name="));
        assertTrue(english.contains("item.occultism.ritual_dummy.summon_wild_otherworld_bird.tooltip="));
        assertTrue(chinese.contains("entity.occultism.otherworld_bird.name="));
        assertTrue(chinese.contains("item.occultism.ritual_dummy.summon_wild_otherworld_bird.tooltip="));
    }

    private static JsonObject findRitual(String id) throws IOException {
        JsonArray rituals = new JsonParser().parse(readUtf8(ASSET_ROOT.resolve("rituals/rituals.json"))).getAsJsonArray();
        for (JsonElement element : rituals) {
            JsonObject ritual = element.getAsJsonObject();
            if (id.equals(ritual.get("id").getAsString())) {
                return ritual;
            }
        }
        throw new AssertionError("Missing ritual definition: " + id);
    }

    private static String readJava(String directory, String file) throws IOException {
        return readUtf8(JAVA_ROOT.resolve(directory).resolve(file));
    }

    private static String readUtf8(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
