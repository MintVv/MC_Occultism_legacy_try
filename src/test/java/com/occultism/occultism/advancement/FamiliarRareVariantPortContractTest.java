package com.occultism.occultism.advancement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.jupiter.api.Test;

/** Source/resource contract for the 1.12 familiar events ported from upstream. */
class FamiliarRareVariantPortContractTest {

    @Test
    void rareAndEmotionAdvancementsAreHiddenAndBilingual() throws Exception {
        String[][] cases = {
                { "familiar_rare_variant", "rare_variant", "RARE_VARIANT" },
                { "familiar_cthulhu_sad", "cthulhu_sad", "CTHULHU_SAD" },
                { "familiar_devil_fire", "devil_fire", "DEVIL_FIRE" }
        };
        String en = readSource("src", "main", "resources", "assets", "occultism", "lang", "en_us.lang");
        String zh = readSource("src", "main", "resources", "assets", "occultism", "lang", "zh_cn.lang");
        for (String[] testCase : cases) {
            JsonObject advancement = new JsonParser().parse(readSource("src", "main", "resources", "assets",
                    "occultism", "advancements", "occultism", testCase[0] + ".json")).getAsJsonObject();
            JsonObject criterion = advancement.getAsJsonObject("criteria").getAsJsonObject(testCase[1]);
            assertEquals("occultism:familiar", criterion.get("trigger").getAsString());
            assertEquals(testCase[2], criterion.getAsJsonObject("conditions").get("type").getAsString());
            assertTrue(advancement.getAsJsonObject("display").get("hidden").getAsBoolean());
            assertTrue(en.contains("advancements.occultism.familiar." + testCase[1] + ".title="));
            assertTrue(en.contains("advancements.occultism.familiar." + testCase[1] + ".description="));
            assertTrue(zh.contains("advancements.occultism.familiar." + testCase[1] + ".title="));
            assertTrue(zh.contains("advancements.occultism.familiar." + testCase[1] + ".description="));
            assertNotNull(FamiliarTrigger.Type.valueOf(testCase[2]));
        }
    }

    @Test
    void rareVariantChecksMatchUpstreamBindingPaths() throws Exception {
        assertRareVariant("EntityCthulhuFamiliar.java", "hasHat");
        assertRareVariant("EntityChimeraFamiliar.java", "hasHat");
        assertRareVariant("EntityMummyFamiliar.java", "hasCrown");
        assertRareVariant("EntityGuardianFamiliar.java", "hasTree");
        assertRareVariant("EntityHeadlessFamiliar.java", "hasGlasses");
        assertRareVariant("EntityFairyFamiliar.java", "hasFlower");
    }

    @Test
    void cthulhuSadIsWiredAfterSuccessfulOwnerDamage() throws Exception {
        String source = readEntity("EntityCthulhuFamiliar.java");
        int successfulDamage = source.indexOf("super.attackEntityFrom(source, amount)");
        int trigger = source.indexOf("FamiliarTrigger.Type.CTHULHU_SAD");
        assertTrue(successfulDamage >= 0 && trigger > successfulDamage);
        assertTrue(source.contains("attacker == this.getOwner()"));
        assertTrue(source.contains("!this.world.isRemote"));
    }

    @Test
    void devilFireIsWiredAfterADevilAttackWithTargets() throws Exception {
        String source = readEntity("EntityDevilFamiliar.java");
        int attack = source.indexOf("this.attack(enemies)");
        int trigger = source.indexOf("FamiliarTrigger.Type.DEVIL_FIRE");
        assertTrue(attack >= 0 && trigger > attack);
        assertTrue(source.contains("this.familiar instanceof EntityDevilFamiliar"));
        assertTrue(source.contains("!enemies.isEmpty()"));
        assertTrue(source.contains("target.setFire(4)"));
    }

    private static void assertRareVariant(String file, String getter) throws IOException {
        String source = readEntity(file);
        assertTrue(source.contains("void setFamiliarOwner(EntityLivingBase owner)"));
        assertTrue(source.contains("super.setFamiliarOwner(owner)"));
        assertTrue(source.contains("this." + getter + "()"));
        assertTrue(source.contains("FamiliarTrigger.Type.RARE_VARIANT"));
    }

    private static String readEntity(String file) throws IOException {
        return readSource("src", "main", "java", "com", "occultism", "occultism", "entity", file);
    }

    private static String readSource(String... path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path[0], java.util.Arrays.copyOfRange(path, 1, path.length))),
                StandardCharsets.UTF_8);
    }
}
