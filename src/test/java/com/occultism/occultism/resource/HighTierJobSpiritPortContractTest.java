package com.occultism.occultism.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.occultism.occultism.entity.EntityAfritJob;
import com.occultism.occultism.entity.EntityDjinniJob;
import com.occultism.occultism.entity.EntityFoliotJob;
import com.occultism.occultism.entity.EntityMaridJob;
import com.occultism.occultism.config.ModConfig;
import com.occultism.occultism.spirit.SpiritJobRecipe;
import com.occultism.occultism.spirit.SpiritJobType;
import com.occultism.occultism.spirit.SpiritTier;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Bootstrap;
import net.minecraft.world.World;

/** Contract coverage for dedicated high-tier job spirits in the 1.12 port. */
class HighTierJobSpiritPortContractTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void factoryUsesDedicatedJobEntitiesAndKeepsFoliotFallback() {
        EntityFoliotJob djinni = EntityFoliotJob.createForTier(null, SpiritJobType.CRUSHER, SpiritTier.DJINNI);
        EntityFoliotJob afrit = EntityFoliotJob.createForTier(null, SpiritJobType.SMELTER, SpiritTier.AFRIT);
        EntityFoliotJob marid = EntityFoliotJob.createForTier(null, SpiritJobType.CRYSTALLIZER, SpiritTier.MARID);
        EntityFoliotJob foliot = EntityFoliotJob.createForTier(null, SpiritJobType.FARMER, SpiritTier.FOLIOT);

        assertTrue(djinni instanceof EntityDjinniJob);
        assertTrue(afrit instanceof EntityAfritJob);
        assertTrue(marid instanceof EntityMaridJob);
        assertEquals(EntityFoliotJob.class, foliot.getClass());
        assertEquals(SpiritTier.DJINNI, djinni.getTier());
        assertEquals(SpiritTier.AFRIT, afrit.getTier());
        assertEquals(SpiritTier.MARID, marid.getTier());
    }

    @Test
    void forgeRegisteredJobEntitiesExposeTheRequiredWorldConstructor() {
        assertDoesNotThrow(() -> EntityFoliotJob.class.getConstructor(World.class));
        assertDoesNotThrow(() -> EntityDjinniJob.class.getConstructor(World.class));
        assertDoesNotThrow(() -> EntityAfritJob.class.getConstructor(World.class));
        assertDoesNotThrow(() -> EntityMaridJob.class.getConstructor(World.class));
    }

    @Test
    void dedicatedJobAttributesMatchTheUpstreamTiers() {
        EntityFoliotJob djinni = new EntityDjinniJob(null, SpiritJobType.SMELTER);
        EntityFoliotJob afrit = new EntityAfritJob(null, SpiritJobType.CRUSHER);
        EntityFoliotJob marid = new EntityMaridJob(null, SpiritJobType.CRYSTALLIZER);

        assertAttributes(djinni, 100.0D, 0.30D, 3.0D, 4.0D, 5.0D, 50.0D);
        assertAttributes(afrit, 200.0D, 0.40D, 8.0D, 8.0D, 50.0D, 50.0D);
        assertAttributes(marid, 300.0D, 0.40D, 16.0D, 16.0D, 100.0D, 50.0D);
    }

    @Test
    void djinniCrusherUsesTheUpstreamExpandedBodyAndOtherJobsUseNormalBody() {
        EntityDjinniJob crusher = new EntityDjinniJob(null, SpiritJobType.CRUSHER);
        EntityDjinniJob smelter = new EntityDjinniJob(null, SpiritJobType.SMELTER);

        assertEquals(1.2F, crusher.width, 0.000001F);
        assertEquals(2.4F, crusher.height, 0.000001F);
        assertEquals(0.7F, smelter.width, 0.000001F);
        assertEquals(1.6F, smelter.height, 0.000001F);

        crusher.setJobType(SpiritJobType.CRYSTALLIZER);
        assertEquals(0.7F, crusher.width, 0.000001F);
        assertEquals(1.6F, crusher.height, 0.000001F);
        crusher.setJobType(SpiritJobType.CRUSHER);
        assertEquals(1.2F, crusher.width, 0.000001F);
        assertEquals(2.4F, crusher.height, 0.000001F);
    }

    @Test
    void jobTierIsFixedAndAttributesArePersistentlyReapplied() {
        EntityFoliotJob djinni = new EntityDjinniJob(null, SpiritJobType.CRUSHER);
        EntityFoliotJob afrit = new EntityAfritJob(null, SpiritJobType.SMELTER);
        EntityFoliotJob marid = new EntityMaridJob(null, SpiritJobType.CRYSTALLIZER);

        assertAttributes(djinni, 100.0D, 0.30D, 3.0D, 4.0D, 5.0D, 50.0D);
        assertAttributes(afrit, 200.0D, 0.40D, 8.0D, 8.0D, 50.0D, 50.0D);
        assertAttributes(marid, 300.0D, 0.40D, 16.0D, 16.0D, 100.0D, 50.0D);

        djinni.setTier(SpiritTier.MARID);
        afrit.setTier(SpiritTier.DJINNI);
        marid.setTier(SpiritTier.AFRIT);
        assertEquals(SpiritTier.DJINNI, djinni.getTier());
        assertEquals(SpiritTier.AFRIT, afrit.getTier());
        assertEquals(SpiritTier.MARID, marid.getTier());
        assertAttributes(djinni, 100.0D, 0.30D, 3.0D, 4.0D, 5.0D, 50.0D);
        assertAttributes(afrit, 200.0D, 0.40D, 8.0D, 8.0D, 50.0D, 50.0D);
        assertAttributes(marid, 300.0D, 0.40D, 16.0D, 16.0D, 100.0D, 50.0D);
    }

    @Test
    void tieredProcessingDefaultsAreAppliedToTheShared112JobLoop() throws Exception {
        assertEquals(2.0D, ModConfig.SPIRIT_JOBS.crusherFoliot.timeMultiplier);
        assertEquals(1.0D, ModConfig.SPIRIT_JOBS.crusherDjinni.timeMultiplier);
        assertEquals(0.5D, ModConfig.SPIRIT_JOBS.crusherAfrit.timeMultiplier);
        assertEquals(0.2D, ModConfig.SPIRIT_JOBS.crusherMarid.timeMultiplier);
        assertEquals(1.0D, ModConfig.SPIRIT_JOBS.crusherFoliot.outputMultiplier);
        assertEquals(1.5D, ModConfig.SPIRIT_JOBS.crusherDjinni.outputMultiplier);
        assertEquals(2.0D, ModConfig.SPIRIT_JOBS.crusherAfrit.outputMultiplier);
        assertEquals(3.0D, ModConfig.SPIRIT_JOBS.crusherMarid.outputMultiplier);

        assertEquals(1.0D, ModConfig.SPIRIT_JOBS.crystallizerFoliot.timeMultiplier);
        assertEquals(0.5D, ModConfig.SPIRIT_JOBS.crystallizerDjinni.timeMultiplier);
        assertEquals(0.3D, ModConfig.SPIRIT_JOBS.crystallizerAfrit.timeMultiplier);
        assertEquals(0.1D, ModConfig.SPIRIT_JOBS.crystallizerMarid.timeMultiplier);
        assertEquals(1.0D, ModConfig.SPIRIT_JOBS.smelterFoliot.timeMultiplier);
        assertEquals(0.5D, ModConfig.SPIRIT_JOBS.smelterDjinni.timeMultiplier);
        assertEquals(0.1D, ModConfig.SPIRIT_JOBS.smelterAfrit.timeMultiplier);
        assertEquals(0.01D, ModConfig.SPIRIT_JOBS.smelterMarid.timeMultiplier);

        EntityFoliotJob foliotCrusher = new EntityFoliotJob(null, SpiritJobType.CRUSHER, SpiritTier.FOLIOT);
        EntityFoliotJob djinniCrusher = new EntityDjinniJob(null, SpiritJobType.CRUSHER);
        EntityFoliotJob maridSmelter = new EntityMaridJob(null, SpiritJobType.SMELTER);
        assertEquals(200, invokeProcessingTime(foliotCrusher));
        assertEquals(100, invokeProcessingTime(djinniCrusher));
        assertEquals(2, invokeProcessingTime(maridSmelter));
    }

    @Test
    void machineManagementTooltipDescribesTheImplementedRoute() throws Exception {
        String english = readSource("src", "main", "resources", "assets", "occultism", "lang", "en_us.lang");
        String chinese = readSource("src", "main", "resources", "assets", "occultism", "lang", "zh_cn.lang");
        assertTrue(english.contains("machine operator"));
        assertTrue(english.contains("transfers matching items"));
        assertTrue(chinese.contains("机器操作员"));
        assertTrue(chinese.contains("转移匹配物品"));
        assertTrue(!english.contains("Machine management AI is not available in this port yet"));
        assertTrue(!chinese.contains("机器管理 AI 暂未移植"));
    }

    @Test
    void maridCrystallizerUsesTheLocalBuddingAmethystPathOnlyAtTheRequiredTier() {
        SpiritJobRecipe maridRecipe = SpiritJobRecipe.getRecipes(SpiritJobType.CRYSTALLIZER).stream()
                .filter(recipe -> recipe.getMinimumTier() == SpiritTier.MARID.getJobTier())
                .findFirst().orElse(null);

        assertTrue(maridRecipe != null);
        assertTrue(!maridRecipe.isAvailableTo(SpiritTier.FOLIOT));
        assertTrue(maridRecipe.isAvailableTo(SpiritTier.MARID));
        assertEquals(SpiritTier.MARID.getJobTier(), maridRecipe.getMinimumTier());
        assertEquals(net.minecraft.item.Item.getItemFromBlock(
                com.occultism.occultism.registry.ModBlocks.BUDDING_AMETHYST), maridRecipe.getOutput().getItem());
    }

    @Test
    void ritualDebugAndRegistrationPathsUseTheDedicatedFactory() throws Exception {
        String bowl = readSource("src", "main", "java", "com", "occultism", "occultism", "tileentity",
                "TileEntitySacrificialBowl.java");
        String debug = readSource("src", "main", "java", "com", "occultism", "occultism", "item",
                "ItemDebugSummonSpirit.java");
        String entities = readSource("src", "main", "java", "com", "occultism", "occultism", "registry",
                "ModEntities.java");
        String renderers = readSource("src", "main", "java", "com", "occultism", "occultism", "client", "render",
                "ModEntityRenderers.java");

        assertTrue(bowl.contains("EntityFoliotJob.createForTier"));
        assertTrue(debug.contains("EntityFoliotJob.createForTier"));
        assertTrue(entities.contains("\"djinni_job\""));
        assertTrue(entities.contains("\"afrit_job\""));
        assertTrue(entities.contains("\"marid_job\""));
        assertTrue(renderers.contains("registerEntityRenderingHandler(EntityDjinniJob.class"));
        assertTrue(renderers.contains("registerEntityRenderingHandler(EntityAfritJob.class"));
        assertTrue(renderers.contains("registerEntityRenderingHandler(EntityMaridJob.class"));
    }

    @Test
    void bilingualHighTierDocumentationMatchesTheImplementedCompatibilityBoundary() throws Exception {
        String english = readSource("src", "main", "resources", "assets", "occultism", "lang", "en_us.lang");
        String chinese = readSource("src", "main", "resources", "assets", "occultism", "lang", "zh_cn.lang");

        assertTrue(english.contains("Selected Djinni, Afrit, and Marid crusher, crystallizer, smelter, gambler, and machine-operator routes"));
        assertTrue(chinese.contains("巨灵、火灵和魔王的部分碎矿、铸晶、熔炼、赌徒与机器操作员路线"));
        assertTrue(english.contains("dedicated local Marid-tier worker entities"));
        assertTrue(chinese.contains("专用的本地魔王阶级工人实体"));
        assertFalse(english.contains("Dedicated Marid worker AI, model, and full upstream behavior still need later work"));
        assertFalse(chinese.contains("专属魔王工人 AI、模型和完整上游行为仍需后续补齐"));

        assertTrue(english.contains("registered local wild Afrit compatibility entity with its Blaze entourage"));
        assertTrue(chinese.contains("已注册的本地野性火灵兼容实体，并生成烈焰人随从"));
        assertFalse(english.contains("maps the result into the local Afrit-tier bound spirit entity"));
        assertFalse(chinese.contains("结果会落到本地火灵阶级已束缚灵体实体"));

        assertFalse(english.contains("Full high-tier jobs are still migrated separately"));
        assertFalse(chinese.contains("完整高阶职业仍在后续迁移"));
    }

    private static void assertAttributes(EntityFoliotJob spirit, double health, double speed, double attack,
            double armor, double toughness, double followRange) {
        assertEquals(health, spirit.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getBaseValue());
        assertEquals(speed, spirit.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getBaseValue());
        assertEquals(attack, spirit.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue());
        assertEquals(armor, spirit.getEntityAttribute(SharedMonsterAttributes.ARMOR).getBaseValue());
        assertEquals(toughness, spirit.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getBaseValue());
        assertEquals(followRange, spirit.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).getBaseValue());
    }

    private static int invokeProcessingTime(EntityFoliotJob spirit) throws Exception {
        java.lang.reflect.Method method = EntityFoliotJob.class.getDeclaredMethod("getProcessTime");
        method.setAccessible(true);
        return ((Integer) method.invoke(spirit)).intValue();
    }

    private static String readSource(String... path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path[0], java.util.Arrays.copyOfRange(path, 1, path.length))),
                StandardCharsets.UTF_8);
    }
}
