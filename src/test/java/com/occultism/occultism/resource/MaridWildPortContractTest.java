package com.occultism.occultism.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.jupiter.api.Test;

/** Contract coverage for the 1.12 unbound Marid ritual path. */
class MaridWildPortContractTest {

    private static final Path ROOT = Paths.get("src", "main", "resources", "assets", "occultism");
    private static final Path JAVA_ROOT = Paths.get("src", "main", "java", "com", "occultism", "occultism");

    @Test
    void unboundRitualsUseHostileEntityIdsInsteadOfBoundTierFallback() throws IOException {
        JsonObject afrit = findRitual("summon_unbound_afrit");
        JsonObject prototype = findRitual("summon_unbound_afrit_prototype");
        JsonObject marid = findRitual("summon_unbound_marid");

        assertEquals("occultism:afrit_wild", afrit.get("entity_to_summon").getAsString());
        assertEquals("occultism:afrit", afrit.get("spirit_tier").getAsString());
        assertEquals("occultism:afrit_wild", prototype.get("entity_to_summon").getAsString());
        assertEquals("occultism:marid_wild", marid.get("entity_to_summon").getAsString());
        assertEquals("occultism:marid", marid.get("spirit_tier").getAsString());
    }

    @Test
    void maridEntityMatchesUpstreamAttributesAndGuardianEntourage() throws IOException {
        String source = readJava("entity", "EntityMaridWild.java");
        assertTrue(source.contains("MAX_HEALTH).setBaseValue(300.0D"));
        assertTrue(source.contains("ATTACK_DAMAGE).setBaseValue(14.0D"));
        assertTrue(source.contains("KNOCKBACK_RESISTANCE).setBaseValue(1.0D"));
        assertTrue(source.contains("3 + this.rand.nextInt(6)"));
        assertTrue(source.contains("new EntityGuardian(this.world)"));
        assertTrue(source.contains("source == DamageSource.DROWN"));
        assertTrue(source.contains("entities/marid_wild"));
    }

    @Test
    void registrationRendererLootAndExplicitSpawnPrecedenceAreWired() throws IOException {
        assertTrue(readJava("registry", "ModEntities.java").contains("\"marid_wild\""));
        assertTrue(readJava("client/render", "ModEntityRenderers.java").contains("EntityMaridWild.class"));
        String bowl = readJava("tileentity", "TileEntitySacrificialBowl.java");
        assertTrue(bowl.indexOf("definition.summonsEntity()") < bowl.indexOf("definition.hasSpiritTier()"));

        JsonObject loot = readJson(ROOT.resolve("loot_tables/entities/marid_wild.json")).getAsJsonObject();
        JsonArray pools = loot.getAsJsonArray("pools");
        assertEquals(1, pools.size());
        assertEquals("occultism:marid_essence",
                pools.get(0).getAsJsonObject().getAsJsonArray("entries").get(0).getAsJsonObject().get("name")
                        .getAsString());
        String english = new String(Files.readAllBytes(ROOT.resolve("lang/en_us.lang")), StandardCharsets.UTF_8);
        String chinese = new String(Files.readAllBytes(ROOT.resolve("lang/zh_cn.lang")), StandardCharsets.UTF_8);
        assertTrue(english.contains("entity.occultism.marid_wild.name=Unbound Marid"));
        assertTrue(chinese.contains("entity.occultism.marid_wild.name=未受缚的魔王"));
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
