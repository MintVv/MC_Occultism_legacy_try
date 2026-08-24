package com.occultism.occultism.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.occultism.occultism.entity.EntityPossessedElderGuardian;
import com.occultism.occultism.entity.EntityBatFamiliar;
import com.occultism.occultism.entity.EntityBoundSpirit;
import com.occultism.occultism.entity.EntityDemonicHusband;
import com.occultism.occultism.entity.EntityFoliotJob;
import com.occultism.occultism.entity.EntityPossessedEnderman;
import com.occultism.occultism.entity.EntityPossessedEndermite;
import com.occultism.occultism.entity.EntityPossessedGhast;
import com.occultism.occultism.entity.EntityPossessedBlaze;
import com.occultism.occultism.entity.EntityPossessedBee;
import com.occultism.occultism.entity.EntityPossessedGuardian;
import com.occultism.occultism.entity.EntityPossessedHoglin;
import com.occultism.occultism.entity.EntityPossessedPhantom;
import com.occultism.occultism.entity.EntityPossessedWarden;
import com.occultism.occultism.entity.EntityPossessedShulker;
import com.occultism.occultism.entity.EntityPossessedSkeleton;
import com.occultism.occultism.entity.EntityPossessedWeakShulker;
import com.occultism.occultism.entity.EntityPossessedWitch;
import com.occultism.occultism.entity.EntityPossessedZombifiedPiglin;
import com.occultism.occultism.entity.EntityShubNiggurathSpawn;
import com.occultism.occultism.entity.EntityWildHuntSkeleton;
import com.occultism.occultism.item.ItemDemonDreamFood;
import com.occultism.occultism.ritual.RitualSacrifice;
import com.occultism.occultism.registry.ModItems;
import com.occultism.occultism.spirit.SpiritJobType;
import com.occultism.occultism.spirit.SpiritTier;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityElderGuardian;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityEndermite;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityParrot;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootTableManager;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class PossessedEntityPortContractTest {

    private static final Path ROOT = Paths.get("src", "main");
    private static final Path JAVA_ROOT = ROOT.resolve("java").resolve("com").resolve("occultism")
            .resolve("occultism");
    private static final Path ASSET_ROOT = ROOT.resolve("resources").resolve("assets").resolve("occultism");
    private static final List<String> ENTITY_IDS = Arrays.asList(
            "possessed_endermite",
            "possessed_skeleton",
            "possessed_enderman",
            "possessed_ghast",
            "possessed_weak_shulker",
            "possessed_shulker",
            "possessed_elder_guardian",
            "possessed_phantom",
            "possessed_hoglin",
            "possessed_warden",
            "possessed_witch",
            "possessed_blaze",
            "possessed_guardian",
            "possessed_zombie_piglin");

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void entitiesAppendAfterWildHuntAndRegisterAllRenderers() throws IOException {
        String entities = readJava("registry", "ModEntities.java");
        int previous = entities.indexOf("\"wild_hunt_wither_skeleton\"");
        for (String id : ENTITY_IDS) {
            int current = entities.indexOf("\"" + id + "\"");
            assertTrue(current > previous, id + " must append without changing existing entity IDs");
            assertEquals(1, countOccurrences(entities,
                    "new ResourceLocation(Tags.MOD_ID, \"" + id + "\")"));
            previous = current;
        }
        assertEquals(59, countOccurrences(entities, "nextEntityId++"));
        int otherworldBirdRegistration = entities.lastIndexOf("EntityOtherworldBird.class");
        int iesniumGolemRegistration = entities.lastIndexOf("EntityIesniumGolem.class");
        assertTrue(iesniumGolemRegistration > otherworldBirdRegistration);
        int phantomRegistration = entities.lastIndexOf("EntityPossessedPhantom.class");
        assertTrue(phantomRegistration >= 0);
        assertTrue(entities.substring(phantomRegistration, Math.min(entities.length(), phantomRegistration + 220))
                .contains("256, 3, true"));

        String renderers = readJava("client/render", "ModEntityRenderers.java");
        assertRenderer(renderers, "EntityPossessedEndermite", "RenderEndermite");
        assertRenderer(renderers, "EntityPossessedSkeleton", "RenderSkeleton");
        assertRenderer(renderers, "EntityPossessedEnderman", "RenderEnderman");
        assertRenderer(renderers, "EntityPossessedGhast", "RenderGhast");
        assertRenderer(renderers, "EntityPossessedWeakShulker", "RenderShulker");
        assertRenderer(renderers, "EntityPossessedShulker", "RenderShulker");
        assertRenderer(renderers, "EntityPossessedElderGuardian", "RenderElderGuardian");
        assertRenderer(renderers, "EntityPossessedPhantom", "RenderPossessedPhantom");
        assertRenderer(renderers, "EntityPossessedHoglin", "RenderPossessedHoglin");
        assertRenderer(renderers, "EntityPossessedWarden", "RenderPossessedWarden");
        assertRenderer(renderers, "EntityPossessedWitch", "RenderWitch");
        assertRenderer(renderers, "EntityPossessedBlaze", "RenderBlaze");
        assertRenderer(renderers, "EntityPossessedGuardian", "RenderGuardian");
        assertRenderer(renderers, "EntityPossessedZombifiedPiglin", "RenderPigZombie");

        String items = readJava("registry", "ModItems.java");
        assertEquals(2, countOccurrences(items, "RITUAL_DUMMY_POSSESS_WARDEN"));
        assertTrue(section(items, "public static final Item[] ITEMS = {", "};")
                .contains("RITUAL_DUMMY_POSSESS_WARDEN"));
        assertTrue(Files.exists(ASSET_ROOT.resolve("models").resolve("item").resolve("ritual_dummy")
                .resolve("possess_warden.json")));
    }

    @Test
    void entitiesConstructWithFixedInheritanceDimensionsAttributesAndFireFlags() {
        EntityPossessedEndermite endermite = new EntityPossessedEndermite(null);
        assertTrue(endermite instanceof EntityEndermite);
        assertEquals(0.4F, endermite.width, 0.000001F);
        assertEquals(0.3F, endermite.height, 0.000001F);

        EntityPossessedSkeleton skeleton = new EntityPossessedSkeleton(null);
        assertTrue(skeleton instanceof EntitySkeleton);
        assertEquals(0.6F, skeleton.width, 0.000001F);
        assertEquals(1.99F, skeleton.height, 0.000001F);
        assertAttribute(skeleton, SharedMonsterAttributes.MAX_HEALTH, 30.0D);
        assertAttribute(skeleton, SharedMonsterAttributes.ATTACK_DAMAGE, 4.0D);

        EntityPossessedEnderman enderman = new EntityPossessedEnderman(null);
        assertTrue(enderman instanceof EntityEnderman);
        assertEquals(0.6F, enderman.width, 0.000001F);
        assertEquals(2.9F, enderman.height, 0.000001F);
        assertFalse(enderman.isImmuneToFire());

        EntityPossessedGhast ghast = new EntityPossessedGhast(null);
        assertTrue(ghast instanceof EntityGhast);
        assertEquals(4.0F, ghast.width, 0.000001F);
        assertEquals(4.0F, ghast.height, 0.000001F);
        assertTrue(ghast.isImmuneToFire());

        EntityPossessedWeakShulker weakShulker = new EntityPossessedWeakShulker(null);
        assertTrue(weakShulker instanceof EntityShulker);
        assertAttribute(weakShulker, SharedMonsterAttributes.MAX_HEALTH, 10.0D);
        assertTrue(weakShulker.isImmuneToFire());

        EntityPossessedShulker shulker = new EntityPossessedShulker(null);
        assertTrue(shulker instanceof EntityShulker);
        assertAttribute(shulker, SharedMonsterAttributes.MAX_HEALTH, 40.0D);
        assertTrue(shulker.isImmuneToFire());

        EntityPossessedElderGuardian guardian = new EntityPossessedElderGuardian(null);
        assertTrue(guardian instanceof EntityElderGuardian);
        assertEquals(2.0F, guardian.width, 0.000001F);
        assertEquals(2.0F, guardian.height, 0.000001F);
        assertAttribute(guardian, SharedMonsterAttributes.MAX_HEALTH, 160.0D);
        assertTrue(guardian.isImmuneToFire());

        EntityPossessedPhantom phantom = new EntityPossessedPhantom(null);
        assertTrue(phantom instanceof net.minecraft.entity.monster.EntityMob);
        assertEquals(0.9F, phantom.width, 0.000001F);
        assertEquals(0.5F, phantom.height, 0.000001F);
        assertAttribute(phantom, SharedMonsterAttributes.MAX_HEALTH, 20.0D);
        assertAttribute(phantom, SharedMonsterAttributes.ATTACK_DAMAGE, 3.0D);
        assertTrue(phantom.isImmuneToFire());

        EntityPossessedHoglin hoglin = new EntityPossessedHoglin(null);
        assertTrue(hoglin instanceof net.minecraft.entity.monster.EntityMob);
        assertEquals(1.88F, hoglin.width, 0.000001F);
        assertEquals(1.88F, hoglin.height, 0.000001F);
        assertAttribute(hoglin, SharedMonsterAttributes.MAX_HEALTH, 66.0D);
        assertAttribute(hoglin, SharedMonsterAttributes.ATTACK_DAMAGE, 12.0D);
        assertTrue(hoglin.isImmuneToFire());

        EntityPossessedWarden warden = new EntityPossessedWarden(null);
        assertTrue(warden instanceof net.minecraft.entity.monster.EntityMob);
        assertEquals(1.8F, warden.width, 0.000001F);
        assertEquals(5.8F, warden.height, 0.000001F);
        assertAttribute(warden, SharedMonsterAttributes.MAX_HEALTH, 250.0D);
        assertAttribute(warden, SharedMonsterAttributes.ATTACK_DAMAGE, 15.0D);
        assertAttribute(warden, SharedMonsterAttributes.MOVEMENT_SPEED, 0.6D);
        assertTrue(warden.isImmuneToFire());

        EntityPossessedWitch witch = new EntityPossessedWitch(null);
        assertTrue(witch instanceof EntityWitch);
        assertAttribute(witch, SharedMonsterAttributes.MAX_HEALTH, 35.0D);
        assertAttribute(witch, SharedMonsterAttributes.MOVEMENT_SPEED, 0.3D);

        EntityPossessedBlaze blaze = new EntityPossessedBlaze(null);
        assertTrue(blaze instanceof EntityBlaze);
        assertAttribute(blaze, SharedMonsterAttributes.MAX_HEALTH, 66.6D);
        assertAttribute(blaze, SharedMonsterAttributes.MOVEMENT_SPEED, 0.4D);
        assertAttribute(blaze, SharedMonsterAttributes.ATTACK_DAMAGE, 12.0D);
        assertTrue(blaze.isImmuneToFire());
        assertFalse(blaze.isWet());

        EntityPossessedGuardian possessedGuardian = new EntityPossessedGuardian(null);
        assertTrue(possessedGuardian instanceof EntityGuardian);
        assertAttribute(possessedGuardian, SharedMonsterAttributes.MAX_HEALTH, 60.0D);
        assertAttribute(possessedGuardian, SharedMonsterAttributes.ARMOR, 6.0D);

        EntityPossessedZombifiedPiglin piglin = new EntityPossessedZombifiedPiglin(null);
        assertTrue(piglin instanceof EntityPigZombie);
        assertAttribute(piglin, SharedMonsterAttributes.MAX_HEALTH, 80.0D);
        assertAttribute(piglin, SharedMonsterAttributes.MOVEMENT_SPEED, 0.26D);
        assertAttribute(piglin, SharedMonsterAttributes.ATTACK_DAMAGE, 6.0D);
        assertAttribute(piglin, SharedMonsterAttributes.ARMOR, 6.0D);
        assertAttribute(piglin, SharedMonsterAttributes.ARMOR_TOUGHNESS, 20.0D);
        assertTrue(piglin.isImmuneToFire());
    }

    @Test
    void peacefulRemovalGuardAndSkeletonSunlightMappingPreserveRealRemoval() throws IOException {
        String guard = readJava("entity", "PeacefulRemovalGuard.java");
        assertTrue(guard.contains("entity.world.getDifficulty() == EnumDifficulty.PEACEFUL"));
        assertTrue(guard.contains("this.setDeadCalls == 1"));
        assertTrue(guard.contains("entity.getHealth() > 0.0F"));
        assertTrue(guard.contains("entity.isDead = false;"));

        for (String file : Arrays.asList(
                "EntityPossessedEndermite.java",
                "EntityPossessedSkeleton.java",
                "EntityPossessedEnderman.java",
                "EntityPossessedGhast.java",
                "EntityPossessedElderGuardian.java",
                "EntityPossessedPhantom.java",
                "EntityPossessedHoglin.java",
                "EntityPossessedWarden.java",
                "EntityPossessedWitch.java",
                "EntityPossessedBlaze.java",
                "EntityPossessedGuardian.java",
                "EntityPossessedZombifiedPiglin.java")) {
            String source = readJava("entity", file);
            assertTrue(source.contains("this.peacefulRemovalGuard.begin(this)"), file);
            assertTrue(source.contains("this.peacefulRemovalGuard.finish(this, protect)"), file);
            assertTrue(source.contains("this.peacefulRemovalGuard.recordSetDead()"), file);
        }

        String skeleton = readJava("entity", "EntityPossessedSkeleton.java");
        String sunlight = section(skeleton, "public void onLivingUpdate", "public void onUpdate");
        assertTrue(sunlight.contains("new ItemStack(Items.PAPER)"));
        assertTrue(sunlight.contains("try {"));
        assertTrue(sunlight.contains("finally {"));
        assertTrue(sunlight.contains("this.setItemStackToSlot(EntityEquipmentSlot.HEAD, originalHelmet);"));

        String phantom = readJava("entity", "EntityPossessedPhantom.java");
        assertTrue(phantom.contains("this.setNoGravity(true)"));
        assertTrue(phantom.contains("this.setSwooping(true)"));
        assertTrue(phantom.contains("this.moveToward("));
        assertTrue(phantom.contains("this.attackEntityAsMob(target)"));
        assertTrue(phantom.contains("this.peacefulRemovalGuard.begin(this)"));

        String hoglin = readJava("entity", "EntityPossessedHoglin.java");
        assertTrue(hoglin.contains("this.setSize(1.88F, 1.88F)"));
        assertTrue(hoglin.contains("this.isImmuneToFire = true"));
        assertTrue(hoglin.contains("target.knockBack(this, 1.0F"));
        assertTrue(hoglin.contains("new EntityAIHurtByTarget(this, true)"));
        assertTrue(hoglin.contains("new EntityAINearestAttackableTarget<EntityPlayer>"));

        String warden = readJava("entity", "EntityPossessedWarden.java");
        assertTrue(warden.contains("this.setSize(1.8F, 5.8F)"));
        assertTrue(warden.contains("this.isImmuneToFire = true"));
        assertTrue(warden.contains("this.performSonicPulse(target)"));
        assertTrue(warden.contains("MobEffects.BLINDNESS"));
        assertTrue(warden.contains("target.knockBack(this, 0.5F"));
        assertTrue(warden.contains("this.peacefulRemovalGuard.begin(this)"));

        String piglin = readJava("entity", "EntityPossessedZombifiedPiglin.java");
        assertTrue(piglin.contains("SPAWN_REINFORCEMENTS_CHANCE"));
        assertTrue(piglin.contains("setBaseValue(0.0D)"));
    }

    @Test
    void wardenPulseCompatibilityIsBoundedObservableAndPersistent() throws IOException {
        assertEquals(60, EntityPossessedWarden.SONIC_PULSE_COOLDOWN_TICKS);
        assertEquals(6, EntityPossessedWarden.SONIC_PULSE_ACTIVE_TICKS);
        assertEquals(40, EntityPossessedWarden.BLINDNESS_INTERVAL_TICKS);
        assertEquals(16.0D, EntityPossessedWarden.SONIC_PULSE_MIN_DISTANCE_SQ, 0.000001D);
        assertEquals(576.0D, EntityPossessedWarden.SONIC_PULSE_MAX_DISTANCE_SQ, 0.000001D);

        EntityPossessedWarden warden = new EntityPossessedWarden(null);
        assertFalse(warden.isSonicBoom());
        NBTTagCompound state = new NBTTagCompound();
        state.setInteger("WardenSonicCooldown", EntityPossessedWarden.SONIC_PULSE_COOLDOWN_TICKS);
        state.setInteger("WardenSonicPulseTicks", EntityPossessedWarden.SONIC_PULSE_ACTIVE_TICKS);
        warden.readEntityFromNBT(state);
        assertEquals(EntityPossessedWarden.SONIC_PULSE_COOLDOWN_TICKS, warden.getSonicCooldown());
        assertEquals(EntityPossessedWarden.SONIC_PULSE_ACTIVE_TICKS, warden.getSonicPulseTicks());
        assertTrue(warden.isSonicBoom());

        NBTTagCompound saved = new NBTTagCompound();
        warden.writeEntityToNBT(saved);
        assertEquals(EntityPossessedWarden.SONIC_PULSE_COOLDOWN_TICKS,
                saved.getInteger("WardenSonicCooldown"));
        assertEquals(EntityPossessedWarden.SONIC_PULSE_ACTIVE_TICKS,
                saved.getInteger("WardenSonicPulseTicks"));

        String source = readJava("entity", "EntityPossessedWarden.java");
        assertTrue(source.contains("findVibrationTarget()"));
        assertTrue(source.contains("getEntitiesWithinAABB(EntityPlayer.class, area)"));
        assertTrue(source.contains("!((EntityPlayer) target).isCreative()"));
        assertTrue(source.contains("target.attackEntityFrom(DamageSource.MAGIC, 6.0F)"));
        assertTrue(source.contains("if (target.attackEntityFrom(DamageSource.MAGIC, 6.0F))"));
        assertTrue(source.contains("WardenSonicPulseTicks"));
        assertTrue(source.contains("Minecraft 1.12 has no vibration event bus")
                || source.contains("1.12 has no vibration event bus"));
    }

    @Test
    void hoglinFungusAvoidanceAndNetherConversionUseExplicitOneTwelveSurrogates() throws IOException {
        assertEquals(-1, EntityPossessedHoglin.NETHER_DIMENSION);
        assertEquals(300, EntityPossessedHoglin.ZOMBIFICATION_DELAY_TICKS);
        assertEquals(8, EntityPossessedHoglin.FUNGUS_AVOIDANCE_RADIUS);
        assertEquals(3, EntityPossessedHoglin.FUNGUS_AVOIDANCE_VERTICAL);
        assertEquals(12.0D, EntityPossessedHoglin.ZOMBIFIED_ATTACK_DAMAGE, 0.000001D);
        assertTrue(EntityPossessedHoglin.isNetherDimension(-1));
        assertFalse(EntityPossessedHoglin.isNetherDimension(0));
        assertTrue(EntityPossessedHoglin.isFungusSubstitute(Blocks.RED_MUSHROOM.getDefaultState()));
        assertTrue(EntityPossessedHoglin.isFungusSubstitute(Blocks.BROWN_MUSHROOM.getDefaultState()));
        assertFalse(EntityPossessedHoglin.isFungusSubstitute(Blocks.NETHERRACK.getDefaultState()));

        EntityPossessedHoglin hoglin = new EntityPossessedHoglin(null);
        assertEquals(0, hoglin.getOutsideNetherTicks());
        NBTTagCompound state = new NBTTagCompound();
        state.setInteger("HoglinOutsideNetherTicks", 42);
        hoglin.readEntityFromNBT(state);
        assertEquals(42, hoglin.getOutsideNetherTicks());
        NBTTagCompound saved = new NBTTagCompound();
        hoglin.writeEntityToNBT(saved);
        assertEquals(42, saved.getInteger("HoglinOutsideNetherTicks"));

        NBTTagCompound corrupt = new NBTTagCompound();
        corrupt.setInteger("HoglinOutsideNetherTicks", 9999);
        hoglin.readEntityFromNBT(corrupt);
        assertEquals(EntityPossessedHoglin.ZOMBIFICATION_DELAY_TICKS, hoglin.getOutsideNetherTicks());

        String source = readJava("entity", "EntityPossessedHoglin.java");
        assertTrue(source.contains("new FungusAvoidanceGoal(this"));
        assertTrue(source.contains("findRandomTargetBlockAwayFrom"));
        assertTrue(source.contains("isFungusSubstitute"));
        assertTrue(source.contains("transformToZombiePigman()"));
        assertTrue(source.contains("new EntityPigZombie(this.world)"));
        assertTrue(source.contains("this.world.spawnEntity(converted)"));
        assertTrue(source.contains("HoglinOutsideNetherTicks"));
        assertTrue(source.contains("converted.setCustomNameTag"));
        assertTrue(source.contains("converted.setAttackTarget(this.getAttackTarget())"));
        assertTrue(source.contains("ZOMBIFIED_ATTACK_DAMAGE"));
    }

    @Test
    void ritualsUseCanonicalEntitiesDurationsMaterialsAndDisplayResults() throws IOException {
        JsonArray rituals = readJson(ASSET_ROOT.resolve("rituals").resolve("rituals.json")).getAsJsonArray();
        assertRitual(rituals, "possess_endermite", 600, "occultism:possessed_endermite", 4,
                "minecraft:end_stone");
        assertRitual(rituals, "possess_skeleton", 600, "occultism:possessed_skeleton", 4,
                "minecraft:skull");
        assertRitual(rituals, "possess_enderman", 1200, "occultism:possessed_enderman", 4,
                "minecraft:ender_pearl");
        assertRitual(rituals, "possess_ghast", 1200, "occultism:possessed_ghast", 10,
                "minecraft:ghast_tear");
        assertRitual(rituals, "possess_weak_shulker", 1200, "occultism:possessed_weak_shulker", 4,
                "minecraft:chorus_fruit");
        assertRitual(rituals, "possess_shulker", 1200, "occultism:possessed_shulker", 4,
                "minecraft:shulker_shell");
        assertRitual(rituals, "possess_elder_guardian", 1800, "occultism:possessed_elder_guardian", 10,
                "occultism:nautilus_shell");
        assertRitual(rituals, "possess_phantom", 600, "occultism:possessed_phantom", 4,
                "occultism:phantom_membrane");
        assertRitual(rituals, "possess_hoglin", 1200, "occultism:possessed_hoglin", 8,
                "occultism:soul_gem");
        assertRitual(rituals, "possess_warden", 1200, "occultism:possessed_warden", 12,
                "occultism:echo_dust");
        assertRitual(rituals, "possess_witch", 400, "occultism:possessed_witch", 4,
                "occultism:soul_gem");
        assertRitual(rituals, "possess_blaze", 800, "occultism:possessed_blaze", 6,
                "occultism:soul_gem");
        assertRitual(rituals, "possess_guardian", 1000, "occultism:possessed_guardian", 5,
                "occultism:soul_gem");
        assertRitual(rituals, "possess_zombified_piglin", 1000,
                "occultism:possessed_zombie_piglin", 4, "occultism:soul_gem");

        JsonObject skeleton = findObject(rituals, "id", "possess_skeleton");
        assertEquals("c:chickens", string(skeleton.getAsJsonObject("entity_to_sacrifice"), "tag"));
        assertFalse(skeleton.has("item_to_use"));
        assertEquals(0, skeleton.getAsJsonObject("result").get("metadata").getAsInt());

        JsonObject endermite = findObject(rituals, "id", "possess_endermite");
        assertEquals("#c:eggs", endermite.get("item_to_use").getAsString());
        assertFalse(endermite.has("entity_to_sacrifice"));

        JsonObject ghast = findObject(rituals, "id", "possess_ghast");
        assertEquals(3, countPrimitiveIngredient(ghast, "minecraft:netherrack"));
        assertEquals(3, countPrimitiveIngredient(ghast, "minecraft:magma"));
        assertEquals(3, countPrimitiveIngredient(ghast, "minecraft:lava_bucket"));

        JsonArray weakIngredients = findObject(rituals, "id", "possess_weak_shulker")
                .getAsJsonArray("ingredients");
        assertEquals(10, weakIngredients.get(1).getAsJsonObject().get("metadata").getAsInt());
        assertEquals(2, weakIngredients.get(3).getAsJsonObject().get("metadata").getAsInt());

        JsonArray elderIngredients = findObject(rituals, "id", "possess_elder_guardian")
                .getAsJsonArray("ingredients");
        assertEquals("occultism:copper_block", elderIngredients.get(0).getAsString());
        assertEquals(3, countPrimitiveIngredient(findObject(rituals, "id", "possess_elder_guardian"),
                "minecraft:water_bucket"));

        JsonObject phantom = findObject(rituals, "id", "possess_phantom");
        assertEquals("occultism:flying_passive", string(phantom.getAsJsonObject("entity_to_sacrifice"), "tag"));
        assertEquals("ritual.occultism.sacrifice.flying_passive",
                string(phantom.getAsJsonObject("entity_to_sacrifice"), "display_name"));

        RitualSacrifice flyingPassive = new RitualSacrifice("occultism:flying_passive", "flying passive");
        assertTrue(flyingPassive.matches(new EntityBat(null)));
        assertTrue(flyingPassive.matches(new EntityParrot(null)));
        assertFalse(flyingPassive.matches(new EntityPossessedBee(null)));
        assertFalse(flyingPassive.matches(new EntityChicken(null)));

        RitualSacrifice unavailableModernMob = new RitualSacrifice("c:wardens", "warden");
        assertFalse(unavailableModernMob.matches(new EntityZombie(null)));
        RitualSacrifice wardenSacrifice = new RitualSacrifice("occultism:warden_sacrifice", "warden");
        assertTrue(wardenSacrifice.matches(new EntitySquid(null)));
        assertFalse(wardenSacrifice.matches(new EntityCow(null)));
    }

    @Test
    void genericSummonsUseSpecialSpawnNeverDropDisplayResultsAndTriggerCompletion() throws IOException {
        String bowl = readJava("tileentity", "TileEntitySacrificialBowl.java");
        String completion = section(bowl, "private void completeRitual", "private ItemStack createRepairResult");
        assertTrue(completion.contains("this.triggerRitualAdvancement(definition);"));
        assertTrue(completion.contains("this.hasNonItemRitualResult(definition)"));
        assertTrue(completion.contains("this.trySpawnRitualEntity();"));
        assertFalse(completion.contains("!this.trySpawnRitualEntity()"));

        String nonItemResult = section(bowl, "private boolean hasNonItemRitualResult",
                "private boolean isWildHuntRitual");
        assertTrue(nonItemResult.contains("isWorldEffectJob()"));
        assertTrue(nonItemResult.contains("this.getFoliotJobRitualType(definition) != null"));
        assertTrue(nonItemResult.contains("definition.hasSpiritTier()"));
        assertTrue(nonItemResult.contains("this.isWildHuntRitual(definition)"));
        assertTrue(nonItemResult.contains("definition.summonsEntity()"));

        String prepare = section(bowl, "private void prepareRitualLivingEntity",
                "private void triggerSummonedEntityAdvancement");
        assertTrue(prepare.contains("ForgeEventFactory.doSpecialSpawn(entity"));
        assertTrue(prepare.contains("entity.onInitialSpawn("));
        assertTrue(prepare.indexOf("ForgeEventFactory.doSpecialSpawn(entity")
                < prepare.indexOf("entity.onInitialSpawn("));

        String boundSpawn = section(bowl, "private boolean spawnBoundSpiritEntity", "private void setTimeOfDay");
        assertSpawnOrder(boundSpawn, "spirit");
        String jobSpawn = section(bowl, "private boolean spawnFoliotJobEntity",
                "private boolean spawnConfiguredEntities");
        assertSpawnOrder(jobSpawn, "spirit");
        String genericSpawn = section(bowl, "private boolean spawnConfiguredEntities", "private Entity createRitualEntity");
        assertSpawnOrder(genericSpawn, "entity");
        assertTrue(genericSpawn.indexOf("this.applyTamedOwner(definition, entity);")
                < genericSpawn.indexOf("this.prepareRitualLivingEntity((EntityLiving) entity);"));

        String wildHunt = section(bowl, "private boolean spawnWildHunt", "private void triggerRitualAdvancement");
        assertFalse(wildHunt.contains("ModAdvancements.RITUAL.trigger"));
        assertTrue(wildHunt.contains("this.prepareRitualLivingEntity(boss);"));
        assertTrue(wildHunt.contains("this.triggerSummonedEntityAdvancement(boss);"));
        assertTrue(wildHunt.indexOf("this.prepareRitualLivingEntity(boss);")
                < wildHunt.indexOf("this.triggerSummonedEntityAdvancement(boss);"));
        assertTrue(wildHunt.indexOf("this.triggerSummonedEntityAdvancement(boss);")
                < wildHunt.indexOf("this.world.spawnEntity(boss)"));
        String summonedEntityTrigger = section(bowl, "private void triggerSummonedEntityAdvancement",
                "private void triggerRitualAdvancement");
        assertTrue(summonedEntityTrigger.contains("entity.getEntityBoundingBox().grow(50.0D)"));
        assertTrue(summonedEntityTrigger.contains("CriteriaTriggers.SUMMONED_ENTITY.trigger(player, entity)"));
        String trigger = section(bowl, "private void triggerRitualAdvancement", "private boolean applyWorldEffectJob");
        assertTrue(trigger.contains("ModAdvancements.RITUAL.trigger"));

        String dreamFood = readJava("item", "ItemDemonDreamFood.java");
        assertTrue(dreamFood.contains("target instanceof EntityPossessedEndermite"));
        assertTrue(dreamFood.contains("target instanceof EntityPossessedEnderman"));
        assertTrue(dreamFood.contains("target instanceof EntityPossessedGhast"));
        assertTrue(dreamFood.contains("target instanceof IFamiliar"));
        assertTrue(dreamFood.contains("target instanceof EntityParrot"));
        assertTrue(dreamFood.contains("target instanceof EntityShubNiggurathSpawn"));
        assertTrue(dreamFood.contains("target instanceof EntityDemonicPartner"));
    }

    @Test
    void demonsDreamHealingMatchesTheFixedUpstreamTagAndAmounts() throws Exception {
        Method healable = ItemDemonDreamFood.class.getDeclaredMethod("isHealableSpirit", Entity.class);
        healable.setAccessible(true);

        for (Entity entity : Arrays.asList(
                new EntityFoliotJob(null, SpiritJobType.CRUSHER),
                new EntityBoundSpirit(null, SpiritTier.DJINNI),
                new EntityWildHuntSkeleton(null),
                new EntityPossessedEndermite(null),
                new EntityPossessedEnderman(null),
                new EntityPossessedGhast(null),
                new EntityPossessedBlaze(null),
                new EntityPossessedGuardian(null),
                new EntityPossessedWitch(null),
                new EntityPossessedZombifiedPiglin(null),
                new EntityBatFamiliar(null),
                new EntityParrot(null),
                new EntityShubNiggurathSpawn(null),
                new EntityDemonicHusband(null))) {
            assertTrue((Boolean) healable.invoke(null, entity), entity.getClass().getSimpleName());
        }
        assertFalse((Boolean) healable.invoke(null, new EntityPossessedSkeleton(null)));
        assertFalse((Boolean) healable.invoke(null, new EntityPossessedPhantom(null)));
        assertFalse((Boolean) healable.invoke(null, new EntityCow(null)));

        Field healing = ItemDemonDreamFood.class.getDeclaredField("spiritHealing");
        healing.setAccessible(true);
        assertEquals(2, healing.getInt(ModItems.DATURA));
        assertEquals(10, healing.getInt(ModItems.DEMONS_DREAM_ESSENCE));
        assertEquals(20, healing.getInt(ModItems.OTHERWORLD_ESSENCE));
    }

    @Test
    void lootTablesUseThe112SchemaAndPreserveAllAvailableFixedPools() throws Exception {
        Field gsonField = LootTableManager.class.getDeclaredField("GSON_INSTANCE");
        gsonField.setAccessible(true);
        Gson lootGson = (Gson) gsonField.get(null);
        Path lootRoot = ASSET_ROOT.resolve("loot_tables").resolve("entities");
        ResourceLocation nautilusShellId = new ResourceLocation("occultism", "nautilus_shell");
        if (!ForgeRegistries.ITEMS.containsKey(nautilusShellId)) {
            ForgeRegistries.ITEMS.register(ModItems.NAUTILUS_SHELL);
        }
        ResourceLocation phantomMembraneId = new ResourceLocation("occultism", "phantom_membrane");
        if (!ForgeRegistries.ITEMS.containsKey(phantomMembraneId)) {
            ForgeRegistries.ITEMS.register(ModItems.PHANTOM_MEMBRANE);
        }
        ResourceLocation echoDustId = new ResourceLocation("occultism", "echo_dust");
        if (!ForgeRegistries.ITEMS.containsKey(echoDustId)) {
            ForgeRegistries.ITEMS.register(ModItems.ECHO_DUST);
        }
        ResourceLocation demonicMeatId = new ResourceLocation("occultism", "demonic_meat");
        if (!ForgeRegistries.ITEMS.containsKey(demonicMeatId)) {
            ForgeRegistries.ITEMS.register(ModItems.DEMONIC_MEAT);
        }
        for (String id : ENTITY_IDS) {
            String json = readUtf8(lootRoot.resolve(id + ".json"));
            assertNotNull(ForgeHooks.loadLootTable(lootGson,
                    new ResourceLocation("occultism", "entities/" + id), json, true, null), id);
        }

        JsonObject endermite = readJson(lootRoot.resolve("possessed_endermite.json")).getAsJsonObject();
        assertEquals(1, endermite.getAsJsonArray("pools").size());
        assertCountAndLooting(findItemEntry(endermite, "minecraft:end_stone"), 1.0D, 2.0D);

        JsonObject skeleton = readJson(lootRoot.resolve("possessed_skeleton.json")).getAsJsonObject();
        assertEquals(3, skeleton.getAsJsonArray("pools").size());
        JsonObject skull = findItemEntry(skeleton, "minecraft:skull");
        assertNotNull(skull);
        assertEquals(0, findFunction(skull, "set_data").get("data").getAsInt());
        assertCountAndLooting(skull, 1.0D, 1.0D);

        JsonObject enderman = readJson(lootRoot.resolve("possessed_enderman.json")).getAsJsonObject();
        assertCountAndLooting(findItemEntry(enderman, "minecraft:ender_pearl"), 1.0D, 3.0D);
        assertFalse(enderman.toString().contains("armor_trim"));

        JsonObject ghast = readJson(lootRoot.resolve("possessed_ghast.json")).getAsJsonObject();
        assertCountAndLooting(findItemEntry(ghast, "minecraft:ghast_tear"), 1.0D, 3.0D);
        assertCountAndLooting(findItemEntry(ghast, "minecraft:gunpowder"), 1.0D, 4.0D);

        JsonObject weak = readJson(lootRoot.resolve("possessed_weak_shulker.json")).getAsJsonObject();
        assertEquals(2, weak.getAsJsonArray("pools").size());
        assertNotNull(findItemEntry(weak, "minecraft:shulker_shell"));
        assertNotNull(findItemEntry(weak, "minecraft:chorus_fruit"));

        JsonObject shulker = readJson(lootRoot.resolve("possessed_shulker.json")).getAsJsonObject();
        assertEquals(2, shulker.getAsJsonArray("pools").size());
        assertEquals(2, countItemEntries(shulker, "minecraft:shulker_shell"));
        assertFalse(shulker.toString().contains("armor_trim"));

        JsonObject elder = readJson(lootRoot.resolve("possessed_elder_guardian.json")).getAsJsonObject();
        assertEquals(5, elder.getAsJsonArray("pools").size());
        assertCount(findItemEntry(elder, "occultism:nautilus_shell"), 2.0D, 4.0D);
        assertEquals(1, findFunction(findItemEntry(elder, "minecraft:sponge"), "set_data")
                .get("data").getAsInt());
        JsonObject rareFishPool = findPoolWithCondition(elder, "random_chance_with_looting");
        assertNotNull(rareFishPool);
        JsonObject rareFishChance = findCondition(rareFishPool, "random_chance_with_looting");
        assertEquals(0.025D, rareFishChance.get("chance").getAsDouble(), 0.000001D);
        assertEquals(0.01D, rareFishChance.get("looting_multiplier").getAsDouble(), 0.000001D);
        JsonArray rareFishEntries = rareFishPool.getAsJsonArray("entries");
        assertEquals(4, rareFishEntries.size());
        assertFishEntry(rareFishEntries, 0, 60);
        assertFishEntry(rareFishEntries, 1, 25);
        assertFishEntry(rareFishEntries, 2, 2);
        assertFishEntry(rareFishEntries, 3, 13);
        assertFalse(elder.toString().contains("heart_of_the_sea"));
        assertFalse(elder.toString().contains("armor_trim"));

        JsonObject phantom = readJson(lootRoot.resolve("possessed_phantom.json")).getAsJsonObject();
        assertEquals(1, phantom.getAsJsonArray("pools").size());
        assertCountAndLooting(findItemEntry(phantom, "occultism:phantom_membrane"), 1.0D, 4.0D);

        JsonObject hoglin = readJson(lootRoot.resolve("possessed_hoglin.json")).getAsJsonObject();
        assertEquals(2, hoglin.getAsJsonArray("pools").size());
        assertCountAndLooting(findItemEntry(hoglin, "minecraft:porkchop"), 1.0D, 3.0D);
        assertCountAndLooting(findItemEntry(hoglin, "minecraft:leather"), 0.0D, 2.0D);

        JsonObject warden = readJson(lootRoot.resolve("possessed_warden.json")).getAsJsonObject();
        assertEquals(3, warden.getAsJsonArray("pools").size());
        assertCountAndLooting(findItemEntry(warden, "occultism:echo_dust"), 1.0D, 4.0D);
        assertNotNull(findEntryWithWeight(warden.getAsJsonArray("pools").get(1).getAsJsonObject(),
                "minecraft:record_11", 1));
        assertNotNull(findEntryWithWeight(warden.getAsJsonArray("pools").get(2).getAsJsonObject(),
                "minecraft:record_cat", 1));

        JsonObject witch = readJson(lootRoot.resolve("possessed_witch.json")).getAsJsonObject();
        assertEquals(2, witch.getAsJsonArray("pools").size());
        assertNotNull(findItemEntry(witch, "minecraft:experience_bottle"));
        assertNotNull(findItemEntry(witch, "minecraft:glass_bottle"));
        assertNotNull(findItemEntry(witch, "minecraft:potion"));

        JsonObject blaze = readJson(lootRoot.resolve("possessed_blaze.json")).getAsJsonObject();
        assertEquals(3, blaze.getAsJsonArray("pools").size());
        assertCountAndLooting(findItemEntry(blaze, "minecraft:blaze_rod"), 2.0D, 6.0D);
        assertCountAndLooting(findItemEntry(blaze, "minecraft:blaze_powder"), 0.0D, 13.0D);

        JsonObject guardian = readJson(lootRoot.resolve("possessed_guardian.json")).getAsJsonObject();
        assertEquals(3, guardian.getAsJsonArray("pools").size());
        assertCountAndLooting(findItemEntry(guardian, "minecraft:prismarine_shard"), 2.0D, 5.0D);
        assertCountAndLooting(findItemEntry(guardian, "minecraft:prismarine_crystals"), 1.0D, 3.0D);

        JsonObject piglin = readJson(lootRoot.resolve("possessed_zombie_piglin.json")).getAsJsonObject();
        assertEquals(2, piglin.getAsJsonArray("pools").size());
        assertCountAndLooting(findItemEntry(piglin, "occultism:demonic_meat"), 1.0D, 4.0D);
        assertCountAndLooting(findItemEntry(piglin, "minecraft:gold_nugget"), 0.0D, 2.0D);
    }

    @Test
    void dictionaryBilingualNamesAndFourHiddenAdvancementsUseCanonicalIds() throws IOException {
        JsonObject dictionary = readJson(ASSET_ROOT.resolve("dictionary").resolve("modonomicon_index.json"))
                .getAsJsonObject();
        String dictionaryText = dictionary.toString();
        for (String id : ENTITY_IDS) {
            assertTrue(dictionaryText.contains("occultism:" + id));
        }
        assertTrue(dictionaryText.contains("occultism:nautilus_shell"));
        assertTrue(dictionaryText.contains("occultism:phantom_membrane"));
        assertFalse(dictionaryText.contains("minecraft:phantom_membrane"));
        assertFalse(dictionaryText.contains("minecraft:wind_charge"));
        assertFalse(dictionaryText.contains("minecraft:heart_of_the_sea"));

        String english = readUtf8(ASSET_ROOT.resolve("lang").resolve("en_us.lang"));
        String chinese = readUtf8(ASSET_ROOT.resolve("lang").resolve("zh_cn.lang"));
        for (String id : ENTITY_IDS) {
            assertTrue(english.contains("entity.occultism." + id + ".name="), id);
            assertTrue(chinese.contains("entity.occultism." + id + ".name="), id);
        }
        assertFalse(english.contains("minecraft:heart_of_the_sea"));
        assertFalse(chinese.contains("minecraft:heart_of_the_sea"));
        assertTrue(english.contains("item.occultism.phantom_membrane.name=Phantom Membrane"));
        assertTrue(chinese.contains("item.occultism.phantom_membrane.name=幻翼膜"));
        assertTrue(english.contains("ritual.occultism.sacrifice.flying_passive="));
        assertTrue(chinese.contains("ritual.occultism.sacrifice.flying_passive="));

        String gui = readJava("client/gui", "GuiDictionaryOfSpirits.java");
        String substitutes = section(gui, "private static String getLocalEntitySubstitute",
                "private static GuideReference createMultiblockReference");
        for (String id : ENTITY_IDS) {
            assertFalse(substitutes.contains("occultism:" + id), id);
        }

        for (String id : Arrays.asList("possess_endermite", "possess_enderman", "possess_ghast",
                "possess_weak_shulker", "possess_shulker", "possess_elder_guardian", "possess_witch",
                "possess_blaze", "possess_guardian", "possess_zombified_piglin")) {
            String key = "item.occultism.ritual_dummy." + id + ".tooltip";
            assertFalse(languageValue(english, key).contains("local substitute"), id);
            assertFalse(languageValue(chinese, key).contains("本地替代"), id);
        }

        assertFalse(languageValue(english, "item.occultism.ritual_dummy.possess_phantom.tooltip")
                .contains("Bat as the local"));
        assertFalse(languageValue(english, "item.occultism.ritual_dummy.possess_hoglin.tooltip")
                .contains("Zombie Pigman"));
        assertFalse(languageValue(chinese, "item.occultism.ritual_dummy.possess_hoglin.tooltip")
                .contains("僵尸猪人"));
        assertTrue(languageValue(english, dictionaryKey("possess_hoglin", "entity.text"))
                .contains("item://minecraft:porkchop"));
        assertTrue(languageValue(chinese, dictionaryKey("possess_hoglin", "entity.text"))
                .contains("item://minecraft:porkchop"));
        assertFalse(languageValue(chinese, "item.occultism.ritual_dummy.possess_phantom.tooltip")
                .contains("原版蝙蝠"));
        assertTrue(languageValue(english, dictionaryKey("possess_phantom", "entity.text"))
                .contains("item://occultism:phantom_membrane"));
        assertTrue(languageValue(chinese, dictionaryKey("possess_phantom", "entity.text"))
                .contains("item://occultism:phantom_membrane"));
        assertTrue(languageValue(english, dictionaryKey("possess_phantom", "description.text"))
                .contains("item://occultism:phantom_membrane"));
        assertTrue(languageValue(chinese, dictionaryKey("possess_phantom", "description.text"))
                .contains("item://occultism:phantom_membrane"));
        assertFalse(languageValue(english, dictionaryKey("possess_phantom", "entity.text"))
                .contains("wind_charge"));
        assertFalse(languageValue(chinese, dictionaryKey("possess_phantom", "entity.text"))
                .contains("wind_charge"));

        assertTrue(languageValue(english, dictionaryKey("possess_warden", "entity.text"))
                .contains("item://occultism:echo_dust"));
        assertTrue(languageValue(chinese, dictionaryKey("possess_warden", "entity.text"))
                .contains("item://occultism:echo_dust"));
        assertTrue(languageValue(english, dictionaryKey("possess_warden", "description.text"))
                .contains("1.12"));
        assertTrue(languageValue(chinese, dictionaryKey("possess_warden", "description.text"))
                .contains("1.12"));
        assertTrue(languageValue(english, "item.occultism.ritual_dummy.possess_warden.tooltip")
                .contains("compatibility entity"));
        assertTrue(languageValue(chinese, "item.occultism.ritual_dummy.possess_warden.tooltip")
                .contains("兼容实体"));
        assertTrue(english.contains("item.occultism.demonic_meat.name=Demonic Meat"));
        assertTrue(chinese.contains("item.occultism.demonic_meat.name=恶魔肉"));
        assertTrue(languageValue(english, "item.occultism.demonic_meat.auto_tooltip")
                .contains("Fire Resistance for 5 minutes"));
        assertTrue(languageValue(chinese, "item.occultism.demonic_meat.auto_tooltip")
                .contains("5 分钟抗火"));

        String items = readJava("registry", "ModItems.java");
        assertTrue(items.contains("public static final Item DEMONIC_MEAT = demonDreamFood(\"demonic_meat\", 0)"));
        assertTrue(items.contains(".addEffect(MobEffects.STRENGTH, 30 * 20, 0, 1.0F)"));
        assertTrue(items.contains(".addEffect(MobEffects.FIRE_RESISTANCE, 5 * 60 * 20, 0, 1.0F)"));
        assertTrue(section(items, "public static final Item[] ITEMS = {", "};").contains("DEMONIC_MEAT"));

        assertFalse(languageValue(english, dictionaryKey("possess_endermite", "entity.text"))
                .contains("25%%"));
        assertFalse(languageValue(chinese, dictionaryKey("possess_endermite", "entity.text"))
                .contains("25%%"));
        assertFalse(languageValue(english, dictionaryKey("possess_enderman", "entity.text"))
                .contains("eye_armor_trim"));
        assertFalse(languageValue(chinese, dictionaryKey("possess_enderman", "entity.text"))
                .contains("eye_armor_trim"));
        assertTrue(languageValue(english, dictionaryKey("possess_shulker", "description.text"))
                .contains("does not include the modern projectile-based Shulker multiplication mechanic"));
        assertTrue(languageValue(chinese, dictionaryKey("possess_shulker", "description.text"))
                .contains("不含现代版本的受弹增殖机制"));
        assertFalse(languageValue(english, dictionaryKey("possess_shulker", "entity.text"))
                .contains("chorus_flower"));
        assertFalse(languageValue(english, dictionaryKey("possess_shulker", "entity.text"))
                .contains("spire_armor_trim"));
        assertTrue(languageValue(english, dictionaryKey("possess_weak_shulker", "description.text"))
                .contains("does not include the modern projectile-based Shulker multiplication mechanic"));
        assertTrue(languageValue(chinese, dictionaryKey("possess_weak_shulker", "description.text"))
                .contains("不含现代版本的受弹增殖机制"));
        String englishSubstitutes = languageValue(english, "guide.occultism.dictionary.possession_substitutes.body");
        String chineseSubstitutes = languageValue(chinese, "guide.occultism.dictionary.possession_substitutes.body");
        assertTrue(englishSubstitutes.contains("Thirteen possession rituals now use registered Occultism entities"));
        assertTrue(chineseSubstitutes.contains("十三种附身仪式现已使用 Occultism 专属注册实体"));
        assertFalse(englishSubstitutes.contains("endermite, witch, bat-as-phantom, enderman"));
        assertFalse(chineseSubstitutes.contains("末影螨、女巫、蝙蝠替代幻翼、末影人"));

        Path advancementRoot = ASSET_ROOT.resolve("advancements").resolve("occultism");
        for (String id : Arrays.asList("possess_skeleton", "possess_endermite", "possess_enderman", "possess_ghast")) {
            JsonObject advancement = readJson(advancementRoot.resolve(id + ".json")).getAsJsonObject();
            assertEquals("occultism:occultism/root", string(advancement, "parent"));
            JsonObject display = advancement.getAsJsonObject("display");
            assertTrue(display.get("hidden").getAsBoolean());
            assertFalse(display.get("show_toast").getAsBoolean());
            assertFalse(display.get("announce_to_chat").getAsBoolean());
            JsonObject criterion = advancement.getAsJsonObject("criteria").getAsJsonObject(id);
            assertEquals("occultism:ritual", string(criterion, "trigger"));
            assertEquals("occultism:ritual/" + id,
                    string(criterion.getAsJsonObject("conditions").getAsJsonObject("ritual_predicate"), "ritual_id"));
            assertTrue(english.contains("advancements.occultism." + id + ".title="));
            assertTrue(english.contains("advancements.occultism." + id + ".description="));
            assertTrue(chinese.contains("advancements.occultism." + id + ".title="));
            assertTrue(chinese.contains("advancements.occultism." + id + ".description="));
        }
    }

    private static void assertRenderer(String source, String entityClass, String rendererClass) {
        String registration = "registerEntityRenderingHandler(" + entityClass + ".class";
        int start = source.indexOf(registration);
        assertTrue(start >= 0, entityClass);
        int end = source.indexOf("});", start);
        assertTrue(end > start, entityClass);
        assertTrue(source.substring(start, end).contains("new " + rendererClass + "(manager)"), entityClass);
    }

    private static void assertSpawnOrder(String source, String entityVariable) {
        String prepare = "entity".equals(entityVariable)
                ? "this.prepareRitualLivingEntity((EntityLiving) entity);"
                : "this.prepareRitualLivingEntity(" + entityVariable + ");";
        String trigger = "this.triggerSummonedEntityAdvancement(" + entityVariable + ");";
        String spawn = "this.world.spawnEntity(" + entityVariable + ")";
        assertTrue(source.contains(prepare));
        assertTrue(source.contains(trigger));
        assertTrue(source.contains(spawn));
        assertTrue(source.indexOf(prepare) < source.indexOf(trigger));
        assertTrue(source.indexOf(trigger) < source.indexOf(spawn));
    }

    private static void assertRitual(JsonArray rituals, String id, int duration, String entityId,
            int ingredientCount, String resultId) {
        JsonObject ritual = findObject(rituals, "id", id);
        assertNotNull(ritual, id);
        assertEquals(duration, ritual.get("duration_ticks").getAsInt(), id);
        assertEquals(entityId, string(ritual, "entity_to_summon"), id);
        assertEquals(ingredientCount, ritual.getAsJsonArray("ingredients").size(), id);
        assertEquals(resultId, string(ritual.getAsJsonObject("result"), "id"), id);
    }

    private static int countPrimitiveIngredient(JsonObject ritual, String itemId) {
        int count = 0;
        for (JsonElement element : ritual.getAsJsonArray("ingredients")) {
            if (element.isJsonPrimitive() && itemId.equals(element.getAsString())) {
                count++;
            }
        }
        return count;
    }

    private static void assertAttribute(EntityLivingBase entity, IAttribute attribute, double expected) {
        assertNotNull(entity.getEntityAttribute(attribute));
        assertEquals(expected, entity.getEntityAttribute(attribute).getBaseValue(), 0.000001D);
    }

    private static void assertCountAndLooting(JsonObject entry, double minimum, double maximum) {
        assertCount(entry, minimum, maximum);
        assertNotNull(findFunction(entry, "looting_enchant"));
    }

    private static void assertCount(JsonObject entry, double minimum, double maximum) {
        assertNotNull(entry);
        JsonObject count = findFunction(entry, "set_count");
        assertNotNull(count);
        JsonObject range = count.getAsJsonObject("count");
        assertEquals(minimum, range.get("min").getAsDouble(), 0.000001D);
        assertEquals(maximum, range.get("max").getAsDouble(), 0.000001D);
    }

    private static JsonObject findItemEntry(JsonObject lootTable, String itemId) {
        for (JsonElement poolElement : lootTable.getAsJsonArray("pools")) {
            JsonObject found = findItemEntry(poolElement.getAsJsonObject().getAsJsonArray("entries"), itemId);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private static JsonObject findItemEntry(JsonArray entries, String itemId) {
        if (entries == null) {
            return null;
        }
        for (JsonElement element : entries) {
            JsonObject object = element.getAsJsonObject();
            if (itemId.equals(string(object, "name"))) {
                return object;
            }
            JsonObject nested = findItemEntry(object.getAsJsonArray("children"), itemId);
            if (nested != null) {
                return nested;
            }
        }
        return null;
    }

    private static JsonObject findEntryWithWeight(JsonObject pool, String itemId, int weight) {
        JsonObject entry = findItemEntry(pool.getAsJsonArray("entries"), itemId);
        if (entry == null || !entry.has("weight") || entry.get("weight").getAsInt() != weight) {
            return null;
        }
        return entry;
    }

    private static int countItemEntries(JsonObject lootTable, String itemId) {
        int count = 0;
        for (JsonElement poolElement : lootTable.getAsJsonArray("pools")) {
            JsonArray entries = poolElement.getAsJsonObject().getAsJsonArray("entries");
            for (JsonElement entryElement : entries) {
                if (itemId.equals(string(entryElement.getAsJsonObject(), "name"))) {
                    count++;
                }
            }
        }
        return count;
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

    private static JsonObject findPoolWithCondition(JsonObject lootTable, String conditionName) {
        for (JsonElement element : lootTable.getAsJsonArray("pools")) {
            JsonObject pool = element.getAsJsonObject();
            if (findCondition(pool, conditionName) != null) {
                return pool;
            }
        }
        return null;
    }

    private static JsonObject findCondition(JsonObject object, String conditionName) {
        JsonArray conditions = object == null ? null : object.getAsJsonArray("conditions");
        if (conditions == null) {
            return null;
        }
        for (JsonElement element : conditions) {
            JsonObject condition = element.getAsJsonObject();
            if (conditionName.equals(string(condition, "condition"))) {
                return condition;
            }
        }
        return null;
    }

    private static void assertFishEntry(JsonArray entries, int metadata, int weight) {
        for (JsonElement element : entries) {
            JsonObject entry = element.getAsJsonObject();
            JsonObject setData = findFunction(entry, "set_data");
            if ("minecraft:fish".equals(string(entry, "name")) && setData != null
                    && setData.get("data").getAsInt() == metadata) {
                assertEquals(weight, entry.get("weight").getAsInt());
                assertNotNull(findFunction(entry, "furnace_smelt"));
                return;
            }
        }
        throw new AssertionError("Missing fish metadata " + metadata);
    }

    private static JsonObject findObject(JsonArray array, String key, String value) {
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

    private static String dictionaryKey(String ritualId, String suffix) {
        return "book.occultism.dictionary_of_spirits.possession_rituals." + ritualId + "." + suffix;
    }

    private static String languageValue(String language, String key) {
        String prefix = key + "=";
        for (String line : language.split("\\r?\\n")) {
            if (line.startsWith(prefix)) {
                return line.substring(prefix.length());
            }
        }
        throw new AssertionError("Missing language key " + key);
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
