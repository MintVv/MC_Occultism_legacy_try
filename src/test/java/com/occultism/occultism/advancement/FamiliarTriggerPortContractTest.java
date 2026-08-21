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

class FamiliarTriggerPortContractTest {

    @Test
    void triggerIsRegisteredAndCaptureEventIsWired() throws IOException {
        String trigger = readSource("src", "main", "java", "com", "occultism", "occultism", "advancement",
                "FamiliarTrigger.java");
        String registry = readSource("src", "main", "java", "com", "occultism", "occultism", "registry",
                "ModAdvancements.java");
        String ring = readSource("src", "main", "java", "com", "occultism", "occultism", "item",
                "ItemFamiliarRing.java");

        assertTrue(trigger.contains("new ResourceLocation(Tags.MOD_ID, \"familiar\")"));
        assertTrue(trigger.contains("enum Type"));
        assertTrue(trigger.contains("CAPTURE"));
        assertTrue(trigger.contains("type != null && this.type == actual"));
        assertTrue(registry.contains("public static final FamiliarTrigger FAMILIAR"));
        assertTrue(registry.contains("CriteriaTriggers.register(FAMILIAR)"));
        assertTrue(ring.contains("FamiliarTrigger.Type.CAPTURE"));
    }

    @Test
    void captureAdvancementUsesThe1_12CriterionAndBilingualKeys() throws Exception {
        JsonObject advancement = new JsonParser().parse(readSource("src", "main", "resources", "assets",
                "occultism", "advancements", "occultism", "familiar_capture.json")).getAsJsonObject();
        JsonObject criterion = advancement.getAsJsonObject("criteria").getAsJsonObject("capture");
        assertEquals("occultism:familiar", criterion.get("trigger").getAsString());
        assertEquals("CAPTURE", criterion.getAsJsonObject("conditions").get("type").getAsString());
        assertTrue(advancement.getAsJsonObject("display").get("hidden").getAsBoolean());
        assertTrue(readSource("src", "main", "resources", "assets", "occultism", "lang", "en_us.lang")
                .contains("advancements.occultism.familiar.capture.title="));
        assertTrue(readSource("src", "main", "resources", "assets", "occultism", "lang", "zh_cn.lang")
                .contains("advancements.occultism.familiar.capture.title="));
        assertNotNull(FamiliarTrigger.Type.valueOf("CAPTURE"));
    }

    @Test
    void mummyDodgeAdvancementFiresOnlyAfterTheDamageEventIsCanceled() throws Exception {
        JsonObject advancement = new JsonParser().parse(readSource("src", "main", "resources", "assets",
                "occultism", "advancements", "occultism", "familiar_mummy_dodge.json")).getAsJsonObject();
        JsonObject criterion = advancement.getAsJsonObject("criteria").getAsJsonObject("mummy_dodge");
        assertEquals("occultism:familiar", criterion.get("trigger").getAsString());
        assertEquals("MUMMY_DODGE", criterion.getAsJsonObject("conditions").get("type").getAsString());
        assertTrue(advancement.getAsJsonObject("display").get("hidden").getAsBoolean());

        String handler = readSource("src", "main", "java", "com", "occultism", "occultism", "handler",
                "FamiliarEventHandler.java");
        int canceled = handler.indexOf("event.setCanceled(true);");
        int trigger = handler.indexOf("FamiliarTrigger.Type.MUMMY_DODGE");
        assertTrue(canceled >= 0 && trigger > canceled,
                "Mummy Dodge must grant the advancement only after a successful dodge");
        assertTrue(readSource("src", "main", "resources", "assets", "occultism", "lang", "en_us.lang")
                .contains("advancements.occultism.familiar.mummy_dodge.title="));
        assertTrue(readSource("src", "main", "resources", "assets", "occultism", "lang", "zh_cn.lang")
                .contains("advancements.occultism.familiar.mummy_dodge.title="));
        assertNotNull(FamiliarTrigger.Type.valueOf("MUMMY_DODGE"));
    }

    @Test
    void remainingFamiliarAdvancementsUseTheSharedCriterionAndBilingualKeys() throws Exception {
        String[][] cases = {
                { "familiar_guardian_ultimate_sacrifice", "guardian_ultimate_sacrifice", "GUARDIAN_ULTIMATE_SACRIFICE" },
                { "familiar_fairy_save", "fairy_save", "FAIRY_SAVE" },
                { "familiar_headless_cthulhu_head", "headless_cthulhu_head", "HEADLESS_CTHULHU_HEAD" },
                { "familiar_headless_rebuilt", "headless_rebuilt", "HEADLESS_REBUILT" }
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
    void familiarEventHandlerWiresOnlySuccessfulOutcomes() throws Exception {
        String handler = readSource("src", "main", "java", "com", "occultism", "occultism", "handler",
                "FamiliarEventHandler.java");
        String headless = readSource("src", "main", "java", "com", "occultism", "occultism", "entity",
                "EntityHeadlessFamiliar.java");

        int stealCall = handler.indexOf("boolean stolen = stealHeadFor(player, entity)");
        int cthulhuCheck = handler.indexOf("entity instanceof EntityCthulhuFamiliar && stolen");
        assertTrue(stealCall >= 0 && cthulhuCheck > stealCall,
                "Head theft must remain enabled for all supported targets; only the trigger is Cthulhu-specific");
        assertTrue(handler.contains("FamiliarTrigger.Type.HEADLESS_CTHULHU_HEAD"));
        assertTrue(handler.contains("boolean ultimateSacrifice = guardian != null && guardian.getLives() == 1"));
        assertTrue(handler.contains("FamiliarTrigger.Type.GUARDIAN_ULTIMATE_SACRIFICE"));
        int support = handler.indexOf("selected.spawnSupportParticles(entity);");
        int fairyTrigger = handler.indexOf("FamiliarTrigger.Type.FAIRY_SAVE");
        assertTrue(support >= 0 && fairyTrigger > support,
                "Fairy Save must grant the advancement only after the save succeeds");
        assertTrue(headless.contains("if (this.isFullyRebuilt())"));
        assertTrue(headless.contains("FamiliarTrigger.Type.HEADLESS_REBUILT"));
    }

    @Test
    void familiarEventHandlerUsesTheForgeMainEventBus() throws Exception {
        String occultism = readSource("src", "main", "java", "com", "occultism", "occultism", "Occultism.java");
        String handler = readSource("src", "main", "java", "com", "occultism", "occultism", "handler",
                "FamiliarEventHandler.java");
        assertTrue(!occultism.contains("FamiliarEventHandler.class"));
        assertTrue(!occultism.contains("MinecraftForge.TERRAIN_GEN_BUS.register"));
        assertTrue(handler.contains("@Mod.EventBusSubscriber(modid = Tags.MOD_ID)"));
    }

    @Test
    void familiarInteractionAdvancementsAreHiddenAndWiredAfterSuccess() throws Exception {
        String[][] cases = {
                { "familiar_deer_poop", "deer_poop", "DEER_POOP" },
                { "familiar_bat_eat", "bat_eat", "BAT_EAT" },
                { "familiar_dragon_nugget", "dragon_nugget", "DRAGON_NUGGET" }
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

        JsonObject bestFriend = new JsonParser().parse(readSource("src", "main", "resources", "assets",
                "occultism", "advancements", "occultism", "familiar_dragon_best_friend.json")).getAsJsonObject();
        assertTrue(bestFriend.getAsJsonObject("display").get("hidden").getAsBoolean());
        assertEquals("DRAGON_PET", bestFriend.getAsJsonObject("criteria").getAsJsonObject("dragon_pet")
                .getAsJsonObject("conditions").get("type").getAsString());
        assertEquals("DRAGON_FETCH", bestFriend.getAsJsonObject("criteria").getAsJsonObject("dragon_fetch")
                .getAsJsonObject("conditions").get("type").getAsString());
        assertTrue(en.contains("advancements.occultism.familiar.dragon_best_friend.title="));
        assertTrue(en.contains("advancements.occultism.familiar.dragon_best_friend.description="));
        assertTrue(zh.contains("advancements.occultism.familiar.dragon_best_friend.title="));
        assertTrue(zh.contains("advancements.occultism.familiar.dragon_best_friend.description="));

        String deer = readSource("src", "main", "java", "com", "occultism", "occultism", "entity",
                "EntityDeerFamiliar.java");
        assertTrue(deer.indexOf("entityDropItem(new ItemStack(ModItems.DATURA_SEEDS)")
                < deer.indexOf("FamiliarTrigger.Type.DEER_POOP"));
        String bat = readSource("src", "main", "java", "com", "occultism", "occultism", "entity",
                "EntityBatFamiliar.java");
        assertTrue(bat.indexOf("bat.attackEntityFrom") < bat.indexOf("FamiliarTrigger.Type.BAT_EAT"));
        String dragon = readSource("src", "main", "java", "com", "occultism", "occultism", "entity",
                "EntityDragonFamiliar.java");
        assertTrue(dragon.contains("FamiliarTrigger.Type.DRAGON_NUGGET"));
        assertTrue(dragon.contains("FamiliarTrigger.Type.DRAGON_PET"));
        assertTrue(dragon.indexOf("this.dragon.setStick(true)")
                < dragon.indexOf("FamiliarTrigger.Type.DRAGON_FETCH"));
    }

    @Test
    void additionalFamiliarAdvancementsAreHiddenAndFollowSuccessfulServerOutcomes() throws Exception {
        String[][] cases = {
                { "familiar_chimera_ride", "chimera_ride", "CHIMERA_RIDE" },
                { "familiar_goat_detach", "goat_detach", "GOAT_DETACH" },
                { "familiar_shub_niggurath_summon", "shub_niggurath_summon", "SHUB_NIGGURATH_SUMMON" },
                { "familiar_shub_cthulhu_friends", "shub_cthulhu_friends", "SHUB_CTHULHU_FRIENDS" },
                { "familiar_shub_niggurath_spawn", "shub_niggurath_spawn", "SHUB_NIGGURATH_SPAWN" },
                { "familiar_party", "party", "PARTY" }
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

        String chimera = readSource("src", "main", "java", "com", "occultism", "occultism", "entity",
                "EntityChimeraFamiliar.java");
        assertTrue(chimera.indexOf("this.world.spawnEntity(goat)")
                < chimera.indexOf("FamiliarTrigger.Type.GOAT_DETACH"));
        assertTrue(chimera.indexOf("player.startRiding(this)")
                < chimera.indexOf("FamiliarTrigger.Type.CHIMERA_RIDE"));

        String goat = readSource("src", "main", "java", "com", "occultism", "occultism", "entity",
                "EntityGoatFamiliar.java");
        assertTrue(goat.indexOf("this.transform();") < goat.indexOf("FamiliarTrigger.Type.SHUB_NIGGURATH_SUMMON"));
        assertTrue(goat.contains("if (!this.world.isRemote)"));

        String shub = readSource("src", "main", "java", "com", "occultism", "occultism", "entity",
                "EntityShubNiggurathFamiliar.java");
        assertTrue(shub.indexOf("this.rider.startRiding(this.mount)")
                < shub.indexOf("FamiliarTrigger.Type.SHUB_CTHULHU_FRIENDS"));
        String spawn = readSource("src", "main", "java", "com", "occultism", "occultism", "entity",
                "EntityShubNiggurathSpawn.java");
        assertTrue(spawn.indexOf("enemy.attackEntityFrom(source, damage)")
                < spawn.indexOf("FamiliarTrigger.Type.SHUB_NIGGURATH_SPAWN"));

        String playerHandler = readSource("src", "main", "java", "com", "occultism", "occultism", "handler",
                "PlayerEventHandler.java");
        assertTrue(playerHandler.contains("event.getWorld().isRemote"));
        assertTrue(playerHandler.contains("instanceof ItemRecord"));
        assertTrue(playerHandler.contains("BlockJukebox.HAS_RECORD"));
        assertTrue(playerHandler.indexOf("hasFamiliar")
                < playerHandler.indexOf("FamiliarTrigger.Type.PARTY"));
    }

    private static String readSource(String... path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path[0], java.util.Arrays.copyOfRange(path, 1, path.length))),
                StandardCharsets.UTF_8);
    }
}
