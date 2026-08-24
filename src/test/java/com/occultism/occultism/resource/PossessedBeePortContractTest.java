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
import com.occultism.occultism.entity.EntityPossessedBee;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.init.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class PossessedBeePortContractTest {

    private static final Path ROOT = Paths.get("src", "main");
    private static final Path JAVA_ROOT = ROOT.resolve("java").resolve("com").resolve("occultism")
            .resolve("occultism");
    private static final Path ASSET_ROOT = ROOT.resolve("resources").resolve("assets").resolve("occultism");

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void entityKeepsThe112FlyingBaseAndFixedCombatProfile() {
        EntityPossessedBee bee = new EntityPossessedBee(null);
        assertTrue(bee instanceof EntityBat);
        assertEquals(0.5F, bee.width, 0.000001F);
        assertEquals(0.45F, bee.height, 0.000001F);
        assertEquals(10.0D, bee.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getBaseValue(),
                0.000001D);
        assertTrue(bee.isNoDespawnRequired());
    }

    @Test
    void damageSpawnsBoundedReinforcementsAndIdleBeesReturnToTheLocalNest() throws IOException {
        String source = readUtf8(JAVA_ROOT.resolve("entity").resolve("EntityPossessedBee.java"));
        assertEquals(10, EntityPossessedBee.ATTACK_COOLDOWN_TICKS);
        assertEquals(60, EntityPossessedBee.REINFORCEMENT_COOLDOWN_TICKS);
        assertEquals(8, EntityPossessedBee.MAX_SWARM_SIZE);
        assertTrue(source.contains("public boolean attackEntityFrom(DamageSource source, float amount)"));
        assertTrue(source.contains("this.spawnPossessedBeeSwarm();"));
        assertTrue(source.contains("getEntitiesWithinAABB(EntityPossessedBee.class"));
        assertTrue(source.contains("MAX_SWARM_SIZE - currentSwarmSize"));
        assertTrue(source.contains("this.findNearestBeeNest()"));
        assertTrue(source.contains("ModBlocks.BEE_NEST"));
        assertTrue(source.contains("this.setDead();"));
        assertTrue(source.contains("new PotionEffect(MobEffects.POISON, 4 * 20, 0)"));
    }

    @Test
    void ritualAndLanguagesDescribeTheCompatibilityBehavior() throws IOException {
        JsonArray rituals = new JsonParser().parse(readUtf8(ASSET_ROOT.resolve("rituals").resolve("rituals.json")))
                .getAsJsonArray();
        JsonObject ritual = findRitual(rituals, "possess_bee");
        assertEquals("occultism:possessed_bee", ritual.get("entity_to_summon").getAsString());
        assertEquals("occultism:bee_nest", ritual.getAsJsonArray("ingredients").get(3).getAsString());

        String english = readUtf8(ASSET_ROOT.resolve("lang").resolve("en_us.lang"));
        String chinese = readUtf8(ASSET_ROOT.resolve("lang").resolve("zh_cn.lang"));
        assertTrue(english.contains("item.occultism.ritual_dummy.possess_bee.tooltip=Summons a local Possessed Bee"));
        assertTrue(chinese.contains("item.occultism.ritual_dummy.possess_bee.tooltip=召唤本地附身蜂"));
        assertTrue(english.contains("reinforcement swarm"));
        assertTrue(chinese.contains("蜂群增援"));
        assertTrue(!english.contains("reinforcement swarms ... still future work"));
        assertTrue(!chinese.contains("召唤蜂群仍是后续工作"));
    }

    private static JsonObject findRitual(JsonArray rituals, String id) {
        for (JsonElement element : rituals) {
            JsonObject ritual = element.getAsJsonObject();
            if (id.equals(ritual.get("id").getAsString())) {
                return ritual;
            }
        }
        throw new AssertionError("Missing ritual: " + id);
    }

    private static String readUtf8(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
