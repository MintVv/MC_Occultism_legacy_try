package com.occultism.occultism.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.occultism.occultism.entity.EntityWildHuntSkeleton;
import com.occultism.occultism.entity.EntityWildHuntWitherSkeleton;
import com.occultism.occultism.entity.IWildHuntEntity;
import com.occultism.occultism.ritual.PentacleDefinition;
import com.occultism.occultism.ritual.PentacleDetector;
import com.occultism.occultism.ritual.RitualEntityToSummon;
import com.occultism.occultism.ritual.RitualRecipeManager;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityWitherSkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.init.Bootstrap;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class WildHuntPortContractTest {

    private static final Path ROOT = Paths.get("src", "main");
    private static final Path JAVA_ROOT = ROOT.resolve("java").resolve("com").resolve("occultism")
            .resolve("occultism");
    private static final Path ASSET_ROOT = ROOT.resolve("resources").resolve("assets").resolve("occultism");

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void entitiesAppendAfterTheExistingRegistryAndReuseVanillaRenderers() throws IOException {
        String entities = readJava("registry", "ModEntities.java");
        int chimera = entities.indexOf("\"chimera_familiar\"");
        int skeleton = entities.indexOf("\"wild_hunt_skeleton\"");
        int witherSkeleton = entities.indexOf("\"wild_hunt_wither_skeleton\"");
        assertTrue(chimera >= 0 && skeleton > chimera && witherSkeleton > skeleton);
        assertEquals(1, countOccurrences(entities,
                "new ResourceLocation(Tags.MOD_ID, \"wild_hunt_skeleton\")"));
        assertEquals(1, countOccurrences(entities,
                "new ResourceLocation(Tags.MOD_ID, \"wild_hunt_wither_skeleton\")"));

        String renderers = readJava("client/render", "ModEntityRenderers.java");
        assertTrue(renderers.contains("registerEntityRenderingHandler(EntityWildHuntSkeleton.class"));
        assertTrue(renderers.contains("return new RenderSkeleton(manager);"));
        assertTrue(renderers.contains("registerEntityRenderingHandler(EntityWildHuntWitherSkeleton.class"));
        assertTrue(renderers.contains("return new RenderWitherSkeleton(manager);"));
    }

    @Test
    void entitiesConstructWithFixedSizesAttributesAndFireBehavior() {
        EntityWildHuntSkeleton skeleton = new EntityWildHuntSkeleton(null);
        assertTrue(skeleton instanceof EntitySkeleton);
        assertTrue(skeleton instanceof IWildHuntEntity);
        assertEquals(0.6F, skeleton.width, 0.000001F);
        assertEquals(2.9F, skeleton.height, 0.000001F);
        assertAttribute(skeleton, SharedMonsterAttributes.MAX_HEALTH, 20.0D);
        assertAttribute(skeleton, SharedMonsterAttributes.ATTACK_DAMAGE, 4.0D);

        EntityWildHuntWitherSkeleton boss = new EntityWildHuntWitherSkeleton(null);
        assertTrue(boss instanceof EntityWitherSkeleton);
        assertTrue(boss instanceof IWildHuntEntity);
        assertEquals(0.6F, boss.width, 0.000001F);
        assertEquals(2.9F, boss.height, 0.000001F);
        assertAttribute(boss, SharedMonsterAttributes.MAX_HEALTH, 60.0D);
        assertAttribute(boss, SharedMonsterAttributes.ATTACK_DAMAGE, 6.0D);
        assertFalse(boss.isImmuneToFire(),
                "The fixed custom entity type is not fire-immune like vanilla 1.12 WitherSkeleton");
    }

    @Test
    void bossSpawnsThreeToEightNamedMinionsAndMinionsUseTheSeventyPercentSwordContract()
            throws IOException {
        String boss = readJava("entity", "EntityWildHuntWitherSkeleton.java");
        String initialSpawn = section(boss, "public IEntityLivingData onInitialSpawn", "public void onLivingUpdate");
        assertTrue(initialSpawn.contains("3 + this.world.rand.nextInt(6)"));
        assertTrue(initialSpawn.contains("new EntityWildHuntSkeleton(this.world)"));
        assertTrue(initialSpawn.contains("minion.onInitialSpawn(difficulty, livingdata);"));
        assertTrue(initialSpawn.contains("minion.setCustomNameTag(ItemNBTUtil.generateName());"));
        assertTrue(initialSpawn.contains("if (this.world.spawnEntity(minion)) {"));
        assertTrue(initialSpawn.contains("minion.setMaster(this);"));
        assertTrue(initialSpawn.contains("this.minions.add(minion);"));
        assertTrue(initialSpawn.contains("this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE)"
                + ".setBaseValue(6.0D);"), "super.onInitialSpawn resets 1.12 WitherSkeleton attack to four");

        String minion = readJava("entity", "EntityWildHuntSkeleton.java");
        String equipment = section(minion, "protected void setEquipmentBasedOnDifficulty", "public void onLivingUpdate");
        assertTrue(equipment.contains("super.setEquipmentBasedOnDifficulty(difficulty);"));
        assertTrue(equipment.contains("this.rand.nextFloat() <= 0.7F"));
        assertTrue(equipment.contains("new ItemStack(Items.STONE_SWORD)"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void directAndTrueWildHuntSourcesAreFriendlyAndMinionsShieldTheirBoss() throws Exception {
        EntityWildHuntSkeleton minion = new EntityWildHuntSkeleton(null);
        EntityWildHuntWitherSkeleton boss = new EntityWildHuntWitherSkeleton(null);
        EntityZombie ordinary = new EntityZombie(null);

        DamageSource directWild = new EntityDamageSourceIndirect("wild_hunt_test", minion, ordinary);
        DamageSource trueWild = new EntityDamageSourceIndirect("wild_hunt_test", ordinary, minion);
        assertTrue(minion.isEntityInvulnerable(directWild));
        assertTrue(minion.isEntityInvulnerable(trueWild));
        assertTrue(boss.isEntityInvulnerable(directWild));
        assertTrue(boss.isEntityInvulnerable(trueWild));

        Field minionsField = EntityWildHuntWitherSkeleton.class.getDeclaredField("minions");
        minionsField.setAccessible(true);
        List<EntityWildHuntSkeleton> minions = (List<EntityWildHuntSkeleton>) minionsField.get(boss);
        minions.add(minion);
        minion.setMaster(boss);
        assertTrue(boss.isEntityInvulnerable(DamageSource.GENERIC));
        assertFalse(boss.isEntityInvulnerable(DamageSource.OUT_OF_WORLD));
        boss.notifyMinionDeath(minion);
        assertFalse(boss.isEntityInvulnerable(DamageSource.GENERIC));

        String minionSource = readJava("entity", "EntityWildHuntSkeleton.java");
        assertTrue(minionSource.contains("source.getTrueSource()"));
        assertTrue(minionSource.contains("source.getImmediateSource()"));
        String removal = section(minionSource, "public void onRemovedFromWorld", "public boolean isEntityInvulnerable");
        assertTrue(removal.contains("boss -> boss.notifyMinionDeath(this)"));
    }

    @Test
    void sunlightAndPeacefulCompatibilityPreserveRealRemovalAndHelmetState() throws IOException {
        for (String file : Arrays.asList("EntityWildHuntSkeleton.java", "EntityWildHuntWitherSkeleton.java")) {
            String source = readJava("entity", file);
            String sunlight = section(source, "public void onLivingUpdate", "public void onUpdate");
            assertTrue(sunlight.contains("EntityEquipmentSlot.HEAD"));
            assertTrue(sunlight.contains("new ItemStack(Items.PAPER)"));
            assertTrue(sunlight.contains("try {"));
            assertTrue(sunlight.contains("finally {"));
            assertTrue(sunlight.contains("this.setItemStackToSlot(EntityEquipmentSlot.HEAD, originalHelmet);"));

            String peaceful = section(source, "public void onUpdate", "public void setDead");
            assertTrue(peaceful.contains("this.world.getDifficulty() == EnumDifficulty.PEACEFUL"));
            assertTrue(peaceful.contains("this.trackedSetDeadCalls = 0;"));
            assertTrue(peaceful.contains("this.trackedSetDeadCalls == 1"));
            assertTrue(peaceful.contains("this.isDead = false;"));
            String setDead = section(source, "public void setDead", "public ");
            assertTrue(setDead.contains("this.trackedSetDeadCalls++;"));
            assertTrue(setDead.contains("super.setDead();"));
        }
    }

    @Test
    void masterAndMinionLinksSurviveEntityReloadWithoutForcingChunkLoads() throws IOException {
        String minion = readJava("entity", "EntityWildHuntSkeleton.java");
        String boss = readJava("entity", "EntityWildHuntWitherSkeleton.java");

        assertTrue(minion.contains("private UUID masterId;"));
        assertTrue(minion.contains("compound.setUniqueId(\"WildHuntMaster\", this.masterId);"));
        assertTrue(minion.contains("compound.hasUniqueId(\"WildHuntMaster\")"));
        assertTrue(minion.contains("getEntityFromUuid(this.masterId)"));
        assertTrue(minion.contains("this.ticksExisted % 20 == 0"));

        assertTrue(boss.contains("private final Set<UUID> minionIds"));
        assertTrue(boss.contains("compound.setTag(\"WildHuntMinions\", ids);"));
        assertTrue(boss.contains("compound.getTagList(\"WildHuntMinions\", 10)"));
        assertTrue(boss.contains("getEntityFromUuid(id)"));
        assertTrue(boss.contains("this.minionIds.remove(minion.getUniqueID())"));
        assertTrue(boss.contains("this.world instanceof WorldServer"));
    }

    @Test
    void lootTablesPreserveTheFixedPoolsAnd112SkullMetadata() throws IOException {
        JsonObject minionLoot = readJson(ASSET_ROOT.resolve("loot_tables").resolve("entities")
                .resolve("wild_hunt_skeleton.json")).getAsJsonObject();
        JsonArray minionPools = minionLoot.getAsJsonArray("pools");
        assertEquals(2, minionPools.size());
        assertCountAndLooting(findItemEntry(minionPools, "minecraft:arrow"), 0.0D, 2.0D);
        assertCountAndLooting(findItemEntry(minionPools, "minecraft:bone"), 0.0D, 2.0D);
        assertNull(findItemEntry(minionPools, "minecraft:skull"));

        JsonObject bossLoot = readJson(ASSET_ROOT.resolve("loot_tables").resolve("entities")
                .resolve("wild_hunt_wither_skeleton.json")).getAsJsonObject();
        JsonArray bossPools = bossLoot.getAsJsonArray("pools");
        assertEquals(3, bossPools.size());
        assertCountAndLooting(findItemEntry(bossPools, "minecraft:coal"), -1.0D, 1.0D);
        assertCountAndLooting(findItemEntry(bossPools, "minecraft:bone"), 0.0D, 2.0D);

        JsonObject skull = findItemEntry(bossPools, "minecraft:skull");
        assertNotNull(skull);
        JsonObject data = findFunction(skull, "set_data");
        assertNotNull(data);
        assertEquals(1, data.get("data").getAsInt());
        assertCountAndLooting(skull, 0.67D, 1.0D);
        JsonObject skullPool = findPool(bossPools, "minecraft:skull");
        assertNotNull(skullPool);
        assertEquals("killed_by_player", skullPool.getAsJsonArray("conditions").get(0)
                .getAsJsonObject().get("condition").getAsString());

        assertTrue(readJava("entity", "EntityWildHuntSkeleton.java")
                .contains("entities/wild_hunt_skeleton"));
        assertTrue(readJava("entity", "EntityWildHuntWitherSkeleton.java")
                .contains("entities/wild_hunt_wither_skeleton"));
    }

    @Test
    void canonicalRitualUsesItsDedicatedTypeAndThreeBossTileBranch()
            throws IOException, ReflectiveOperationException {
        JsonArray rituals = readJson(ASSET_ROOT.resolve("rituals").resolve("rituals.json")).getAsJsonArray();
        JsonObject ritual = findObject(rituals, "id", "summon_wild_hunt");
        assertNotNull(ritual);
        assertNull(findObject(rituals, "id", "wild_hunt"));
        assertEquals("occultism:summon_wild_hunt", string(ritual, "ritual_type"));
        assertEquals("occultism:summon_wild_greater_spirit", string(ritual, "pentacle_id"));
        assertEquals(600, ritual.get("duration_ticks").getAsInt());
        assertEquals("occultism:wild_hunt_wither_skeleton", string(ritual, "entity_to_summon"));
        assertFalse(ritual.has("summon_number"), "The dedicated ritual branch owns the fixed three-boss count");

        Method readEntityToSummon = RitualRecipeManager.class.getDeclaredMethod(
                "readEntityToSummon", JsonObject.class, String.class);
        readEntityToSummon.setAccessible(true);
        RitualEntityToSummon summonMetadata = (RitualEntityToSummon) readEntityToSummon.invoke(
                null, ritual, "summon_wild_hunt");
        assertEquals(3, summonMetadata.getSummonNumber(),
                "JEI and debug metadata must match the dedicated three-boss runtime branch");

        JsonObject activation = ritual.getAsJsonObject("activation_item");
        assertEquals("minecraft:skull", string(activation, "item"));
        assertEquals(0, activation.get("metadata").getAsInt());
        JsonObject sacrifice = ritual.getAsJsonObject("entity_to_sacrifice");
        assertEquals("occultism:humans", string(sacrifice, "tag"));
        assertEquals(6, ritual.getAsJsonArray("ingredients").size());
        JsonObject result = ritual.getAsJsonObject("result");
        assertEquals("minecraft:skull", string(result, "id"));
        assertEquals(1, result.get("metadata").getAsInt());
        assertEquals("occultism:ritual_dummy/summon_wild_hunt",
                string(ritual.getAsJsonObject("ritual_dummy"), "id"));

        String bowl = readJava("tileentity", "TileEntitySacrificialBowl.java");
        assertTrue(bowl.contains("\"summon_wild_hunt\".equals(definition.getRitualType())"));
        assertTrue(bowl.contains("definition.getEntityToSummon().pickEntityId(this.world.rand)"));
        assertTrue(bowl.contains("this.createRitualEntity(definition, entityId)"));
        assertTrue(bowl.contains("for (int i = 0; i < 3; i++)"));
        assertTrue(countOccurrences(bowl, "this.world.rand.nextGaussian()") >= 2);
        assertTrue(bowl.contains("boss.setCustomNameTag(ItemNBTUtil.generateName());"));
        assertTrue(bowl.contains("this.prepareRitualLivingEntity(boss);"));
        String prepare = section(bowl, "private void prepareRitualLivingEntity",
                "private void triggerSummonedEntityAdvancement");
        assertTrue(prepare.contains("ForgeEventFactory.doSpecialSpawn(entity"));
        assertTrue(prepare.contains("entity.onInitialSpawn("));
        assertTrue(bowl.contains("this.world.spawnEntity(boss)"));
        assertTrue(bowl.contains("Never drop a free skull when entity spawning is cancelled"));
    }

    @Test
    void canonicalDummyAppendsAndTheDedicatedPentacleMatchesTheFixedGeometry() throws IOException {
        String items = readJava("registry", "ModItems.java");
        assertTrue(items.contains("public static final Item RITUAL_DUMMY_SUMMON_WILD_HUNT"));
        assertEquals(2, countOccurrences(items, "RITUAL_DUMMY_SUMMON_WILD_HUNT"));
        assertTrue(items.contains("item(\"ritual_dummy/summon_wild_hunt\","
                + " \"ritual_dummy.summon_wild_hunt\", 1)"));
        String itemArray = section(items, "public static final Item[] ITEMS = {", "};");
        assertTrue(itemArray.indexOf("RITUAL_DUMMY_SUMMON_WILD_HUNT")
                > itemArray.indexOf("RITUAL_DUMMY_FAMILIAR_CHIMERA"));

        JsonObject model = readJson(ASSET_ROOT.resolve("models").resolve("item").resolve("ritual_dummy")
                .resolve("summon_wild_hunt.json")).getAsJsonObject();
        assertEquals("item/generated", string(model, "parent"));
        assertEquals("occultism:item/ritual_dummy_summon",
                string(model.getAsJsonObject("textures"), "layer0"));

        PentacleDefinition pentacle = PentacleDetector.getDefinitionById("summon_wild_greater_spirit");
        assertNotNull(pentacle);
        assertEquals(11, pentacle.getWidth());
        assertEquals(11, pentacle.getHeight());
        assertEquals(Arrays.asList(
                "___________",
                "____XXX____",
                "___W_W_W___",
                "__W_W2W_W__",
                "_X_WX_XW_X_",
                "_XW2_0_2WX_",
                "_X_WX_XW_X_",
                "__W_W2W_W__",
                "___W_W_W___",
                "____XXX____",
                "___________"), Arrays.asList(pentacle.getPattern()));
        assertEquals(20, countPattern(pentacle, 'W'));
        assertEquals(16, countPattern(pentacle, 'X'));
        assertEquals(4, countPattern(pentacle, '2'));
    }

    @Test
    void dictionaryAndBilingualTextExposeTheCanonicalWildHunt() throws IOException {
        JsonObject dictionary = readJson(ASSET_ROOT.resolve("dictionary").resolve("modonomicon_index.json"))
                .getAsJsonObject();
        assertEquals(9, dictionary.getAsJsonArray("categories").size());
        int entries = 0;
        int pages = 0;
        for (JsonElement categoryElement : dictionary.getAsJsonArray("categories")) {
            JsonArray categoryEntries = categoryElement.getAsJsonObject().getAsJsonArray("entries");
            entries += categoryEntries.size();
            for (JsonElement entryElement : categoryEntries) {
                pages += entryElement.getAsJsonObject().getAsJsonArray("pages").size();
            }
        }
        assertEquals(203, entries);
        assertEquals(717, pages);
        String dictionaryJson = dictionary.toString();
        assertTrue(dictionaryJson.contains("occultism:ritual/summon_wild_hunt"));

        String english = readUtf8(ASSET_ROOT.resolve("lang").resolve("en_us.lang"));
        String chinese = readUtf8(ASSET_ROOT.resolve("lang").resolve("zh_cn.lang"));
        assertTrue(english.contains("entity.occultism.wild_hunt_skeleton.name=Wild Hunt Skeleton"));
        assertTrue(english.contains("entity.occultism.wild_hunt_wither_skeleton.name=Wild Hunt Wither Skeleton"));
        assertTrue(english.contains("item.occultism.ritual_dummy.summon_wild_hunt.name="));
        assertTrue(chinese.contains("entity.occultism.wild_hunt_skeleton.name="));
        assertTrue(chinese.contains("entity.occultism.wild_hunt_wither_skeleton.name="));
        assertTrue(chinese.contains("item.occultism.ritual_dummy.summon_wild_hunt.name="));
        assertFalse(english.contains("Summons one 1.12 Wither Skeleton as the local substitute"));
        assertFalse(chinese.contains("召唤一只 1.12 原版凋灵骷髅"));
    }

    @Test
    void hiddenAdvancementUsesThe112RitualTriggerAndFiresOnCompletion() throws IOException {
        Path advancementRoot = ASSET_ROOT.resolve("advancements").resolve("occultism");
        JsonObject root = readJson(advancementRoot.resolve("root.json")).getAsJsonObject();
        assertNull(root.getAsJsonObject("display"));
        assertEquals("minecraft:tick",
                string(root.getAsJsonObject("criteria").getAsJsonObject("occultism_present"), "trigger"));

        JsonObject advancement = readJson(advancementRoot.resolve("summon_wild_hunt.json")).getAsJsonObject();
        assertEquals("occultism:occultism/root", string(advancement, "parent"));
        JsonObject display = advancement.getAsJsonObject("display");
        assertTrue(display.get("hidden").getAsBoolean());
        assertFalse(display.get("show_toast").getAsBoolean());
        assertFalse(display.get("announce_to_chat").getAsBoolean());
        JsonObject criterion = advancement.getAsJsonObject("criteria").getAsJsonObject("summon_wild_hunt");
        assertEquals("occultism:ritual", string(criterion, "trigger"));
        assertEquals("occultism:ritual/summon_wild_hunt",
                string(criterion.getAsJsonObject("conditions").getAsJsonObject("ritual_predicate"), "ritual_id"));

        String trigger = readJava("advancement", "RitualTrigger.java");
        assertTrue(trigger.contains("implements ICriterionTrigger<RitualTrigger.Instance>"));
        assertTrue(trigger.contains("new ResourceLocation(Tags.MOD_ID, \"ritual\")"));
        assertTrue(trigger.contains("ritual_id"));
        assertTrue(trigger.contains("void trigger(EntityPlayerMP player"));

        String registry = readJava("registry", "ModAdvancements.java");
        assertTrue(registry.contains("CriteriaTriggers.register(RITUAL)"));
        assertTrue(readJava("", "Occultism.java").contains("ModAdvancements.register();"));
        String bowl = readJava("tileentity", "TileEntitySacrificialBowl.java");
        assertTrue(bowl.contains("ModAdvancements.RITUAL.trigger"));

        String titleKey = advancement.getAsJsonObject("display").getAsJsonObject("title")
                .get("translate").getAsString();
        String descriptionKey = advancement.getAsJsonObject("display").getAsJsonObject("description")
                .get("translate").getAsString();
        String english = readUtf8(ASSET_ROOT.resolve("lang").resolve("en_us.lang"));
        String chinese = readUtf8(ASSET_ROOT.resolve("lang").resolve("zh_cn.lang"));
        assertTrue(english.contains(titleKey + "="));
        assertTrue(english.contains(descriptionKey + "="));
        assertTrue(chinese.contains(titleKey + "="));
        assertTrue(chinese.contains(descriptionKey + "="));
    }

    private static void assertAttribute(EntityLivingBase entity, IAttribute attribute, double expected) {
        assertNotNull(entity.getEntityAttribute(attribute));
        assertEquals(expected, entity.getEntityAttribute(attribute).getBaseValue(), 0.000001D);
    }

    private static void assertCountAndLooting(JsonObject entry, double minimum, double maximum) {
        assertNotNull(entry);
        JsonObject count = findFunction(entry, "set_count");
        assertNotNull(count);
        JsonObject range = count.getAsJsonObject("count");
        assertEquals(minimum, range.get("min").getAsDouble(), 0.000001D);
        assertEquals(maximum, range.get("max").getAsDouble(), 0.000001D);
        assertNotNull(findFunction(entry, "looting_enchant"));
    }

    private static JsonObject findPool(JsonArray pools, String itemId) {
        for (JsonElement poolElement : pools) {
            JsonObject pool = poolElement.getAsJsonObject();
            if (findItemEntry(pool.getAsJsonArray("entries"), itemId) != null) {
                return pool;
            }
        }
        return null;
    }

    private static JsonObject findItemEntry(JsonArray pools, String itemId) {
        if (pools == null) {
            return null;
        }
        for (JsonElement element : pools) {
            JsonObject object = element.getAsJsonObject();
            if (object.has("entries")) {
                JsonObject nested = findItemEntry(object.getAsJsonArray("entries"), itemId);
                if (nested != null) {
                    return nested;
                }
            }
            if (itemId.equals(string(object, "name"))) {
                return object;
            }
        }
        return null;
    }

    private static JsonObject findFunction(JsonObject entry, String functionName) {
        JsonArray functions = entry == null ? null : entry.getAsJsonArray("functions");
        if (functions == null) {
            return null;
        }
        for (JsonElement element : functions) {
            JsonObject function = element.getAsJsonObject();
            if (functionName.equals(string(function, "function"))) {
                return function;
            }
        }
        return null;
    }

    private static JsonObject findObject(JsonArray array, String key, String value) {
        if (array == null) {
            return null;
        }
        for (JsonElement element : array) {
            JsonObject object = element.getAsJsonObject();
            if (value.equals(string(object, key))) {
                return object;
            }
        }
        return null;
    }

    private static String string(JsonObject object, String key) {
        JsonElement value = object == null ? null : object.get(key);
        return value == null || value.isJsonNull() ? null : value.getAsString();
    }

    private static int countPattern(PentacleDefinition definition, char target) {
        int count = 0;
        for (String row : definition.getPattern()) {
            for (int index = 0; index < row.length(); index++) {
                if (row.charAt(index) == target) {
                    count++;
                }
            }
        }
        return count;
    }

    private static String section(String source, String start, String end) {
        int startIndex = source.indexOf(start);
        assertTrue(startIndex >= 0, "Missing marker: " + start);
        int endIndex = source.indexOf(end, startIndex + start.length());
        assertTrue(endIndex >= 0, "Missing marker: " + end);
        return source.substring(startIndex, endIndex);
    }

    private static int countOccurrences(String source, String token) {
        int count = 0;
        for (int index = 0; (index = source.indexOf(token, index)) >= 0; index += token.length()) {
            count++;
        }
        return count;
    }

    private static JsonElement readJson(Path path) throws IOException {
        return new JsonParser().parse(readUtf8(path));
    }

    private static String readJava(String directory, String file) throws IOException {
        Path path = directory.isEmpty() ? JAVA_ROOT.resolve(file) : JAVA_ROOT.resolve(directory).resolve(file);
        return readUtf8(path);
    }

    private static String readUtf8(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
