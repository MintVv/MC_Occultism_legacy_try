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

/**
 * Locks the five familiar action advancements to their successful 1.12 port
 * branches. These checks intentionally inspect source ordering because the
 * actions are driven by private AI goals rather than directly testable events.
 */
class FamiliarUpgradeAdvancementPortContractTest {

    @Test
    void actionAdvancementsUseTheSharedHiddenCriterionAndBilingualKeys() throws Exception {
        String[][] cases = {
                { "familiar_blacksmith_upgrade", "blacksmith_upgrade", "BLACKSMITH_UPGRADE" },
                { "familiar_greedy_item", "greedy_item", "GREEDY_ITEM" },
                { "familiar_beholder_ray", "beholder_ray", "BEHOLDER_RAY" },
                { "familiar_beholder_eat", "beholder_eat", "BEHOLDER_EAT" },
                { "familiar_beaver_woodchop", "beaver_woodchop", "BEAVER_WOODCHOP" }
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
    void familiarActionsTriggerOnlyAfterARealSuccess() throws Exception {
        String blacksmith = readJava("entity", "EntityBlacksmithFamiliar.java");
        int upgrade = blacksmith.indexOf("this.target.blacksmithUpgrade();");
        int upgradedGuard = blacksmith.indexOf("if (!this.target.canBlacksmithUpgrade())", upgrade);
        int blacksmithTrigger = blacksmith.indexOf("FamiliarTrigger.Type.BLACKSMITH_UPGRADE", upgrade);
        assertTrue(upgrade >= 0 && upgradedGuard > upgrade && blacksmithTrigger > upgradedGuard,
                "Blacksmith advancement must follow a confirmed upgrade transition");

        String greedy = readJava("entity", "EntityGreedyFamiliar.java");
        int collide = greedy.indexOf("item.onCollideWithPlayer");
        int greedyTrigger = greedy.indexOf("FamiliarTrigger.Type.GREEDY_ITEM", collide);
        assertTrue(collide >= 0 && greedyTrigger > collide,
                "Greedy advancement must follow the inventory pickup attempt");
        assertTrue(greedy.indexOf("item.isDead || item.getItem().isEmpty()", collide) < greedyTrigger,
                "Greedy advancement must be guarded by a changed/dead item");
        int ride = greedy.indexOf("this.rider.startRiding(this.mount);");
        int rideTrigger = greedy.indexOf("FamiliarTrigger.Type.DRAGON_RIDE", ride);
        assertTrue(ride >= 0 && rideTrigger > ride,
                "Dragon ride advancement must follow a successful mount operation");

        String beholder = readJava("entity", "EntityBeholderFamiliar.java");
        int damage = beholder.indexOf("target.attackEntityFrom");
        int rayGuard = beholder.indexOf("if (hit)", damage);
        int rayTrigger = beholder.indexOf("FamiliarTrigger.Type.BEHOLDER_RAY", damage);
        assertTrue(damage >= 0 && rayGuard > damage && rayTrigger > rayGuard,
                "Beholder ray advancement must follow a successful damage result");
        int food = beholder.indexOf("food.setDead()");
        int eatTrigger = beholder.indexOf("FamiliarTrigger.Type.BEHOLDER_EAT", food);
        assertTrue(food >= 0 && eatTrigger > food,
                "Beholder eat advancement must follow consuming the spawn");

        String beaver = readJava("entity", "EntityBeaverFamiliar.java");
        int destroy = beaver.lastIndexOf("this.beaver.world.destroyBlock");
        int choppedGuard = beaver.indexOf("if (chopped)", destroy);
        int woodchopTrigger = beaver.indexOf("FamiliarTrigger.Type.BEAVER_WOODCHOP", destroy);
        assertTrue(destroy >= 0 && choppedGuard > destroy && woodchopTrigger > choppedGuard,
                "Beaver advancement must follow at least one destroyed log");
    }

    private static String readJava(String... path) throws IOException {
        String[] full = new String[6 + path.length];
        full[0] = "src";
        full[1] = "main";
        full[2] = "java";
        full[3] = "com";
        full[4] = "occultism";
        full[5] = "occultism";
        full[6] = path[0];
        full[7] = path[1];
        for (int i = 2; i < path.length; i++) {
            full[6 + i] = path[i];
        }
        return readSource(full);
    }

    private static String readSource(String... path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path[0], java.util.Arrays.copyOfRange(path, 1, path.length))),
                StandardCharsets.UTF_8);
    }
}
