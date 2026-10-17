package com.occultism.occultism.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.occultism.occultism.entity.EntityFairyFamiliar;
import com.occultism.occultism.entity.EntityThrownSword;
import com.occultism.occultism.entity.IBlacksmithUpgradable;
import com.occultism.occultism.entity.IFamiliar;
import com.occultism.occultism.registry.ModPotions;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.init.Bootstrap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class FairyPortContractTest {

    private static final Path ROOT = Paths.get("src", "main");
    private static final Path JAVA_ROOT = ROOT.resolve("java").resolve("com").resolve("occultism")
            .resolve("occultism");
    private static final Path ASSET_ROOT = ROOT.resolve("resources").resolve("assets").resolve("occultism");
    private static final Path TOOLS_ROOT = Paths.get("tools");
    private static final String FAIRY_TEXTURE_SHA256 =
            "22e4812971d73eb4f383390edddefe2b23fc19008a1671f9a0935929c0f44366";
    private static final String FAIRY_SLEEP_TEXTURE_SHA256 =
            "a116ae3458a09289e757bcad3b0b7b5c1ed3b8539eab134bb772a26640e526cf";
    private static final String FAIRY_BOOK_ICON_SHA256 =
            "5505cb996e791b04ef24252a551bd1d17093a2a1a244be78d0136e2ab69c36ea";

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void fairyRitualAndRegistrationsPreserveTheAppendOnly1814Contract() throws IOException {
        JsonArray rituals = readJsonElement(ASSET_ROOT.resolve("rituals").resolve("rituals.json"))
                .getAsJsonArray();
        JsonObject actual = findObject(rituals, "id", "familiar_fairy");
        assertNotNull(actual, "Missing familiar_fairy ritual");
        assertEquals(1, countObjects(rituals, "id", "familiar_fairy"));

        JsonObject expected = new JsonObject();
        expected.addProperty("id", "familiar_fairy");
        expected.addProperty("display_name", "Fairy Familiar");
        expected.addProperty("type", "occultism:ritual");
        expected.addProperty("ritual_type", "occultism:summon_tamed");
        expected.addProperty("activation_item", "occultism:book_of_binding_bound_djinni");
        expected.addProperty("duration_ticks", 600);
        JsonObject sacrifice = new JsonObject();
        sacrifice.addProperty("display_name", "ritual.occultism.sacrifice.horses");
        sacrifice.addProperty("tag", "c:horses");
        expected.add("entity_to_sacrifice", sacrifice);
        expected.addProperty("entity_to_summon", "occultism:fairy_familiar");
        JsonArray ingredients = new JsonArray();
        ingredients.add("minecraft:golden_apple");
        ingredients.add("minecraft:golden_apple");
        ingredients.add("minecraft:ghast_tear");
        ingredients.add("minecraft:gunpowder");
        ingredients.add("minecraft:gunpowder");
        ingredients.add("minecraft:gunpowder");
        ingredients.add("minecraft:dragon_breath");
        expected.add("ingredients", ingredients);
        expected.addProperty("pentacle_id", "occultism:possess_djinni");
        JsonObject result = new JsonObject();
        result.addProperty("id", "occultism:soul_gem");
        expected.add("result", result);
        JsonObject dummy = new JsonObject();
        dummy.addProperty("id", "occultism:ritual_dummy/familiar_fairy");
        expected.add("ritual_dummy", dummy);
        assertEquals(expected, actual);

        String entities = readUtf8(JAVA_ROOT.resolve("registry").resolve("ModEntities.java"));
        int sword = entities.indexOf("new ResourceLocation(Tags.MOD_ID, \"thrown_sword\")");
        int fairy = entities.indexOf("new ResourceLocation(Tags.MOD_ID, \"fairy_familiar\")");
        assertTrue(sword >= 0 && fairy > sword,
                "Fairy must append after the previous final numeric entity ID");
        assertEquals(1, countOccurrences(entities,
                "new ResourceLocation(Tags.MOD_ID, \"fairy_familiar\")"));
        assertTrue(Pattern.compile("EntityFairyFamiliar\\.class, \"fairy_familiar\", nextEntityId\\+\\+, "
                + "Occultism\\.INSTANCE, 64, 3, true\\);", Pattern.DOTALL).matcher(entities).find());

        String items = readUtf8(JAVA_ROOT.resolve("registry").resolve("ModItems.java"));
        assertEquals(2, countOccurrences(items, "RITUAL_DUMMY_FAMILIAR_FAIRY"));
        String itemArray = section(items, "public static final Item[] ITEMS = {", "};");
        assertTrue(itemArray.indexOf("RITUAL_DUMMY_FAMILIAR_FAIRY")
                        > itemArray.indexOf("RITUAL_DUMMY_FAMILIAR_DRAGON"),
                "Fairy dummy must append after the prior final item without shifting old IDs");

        String potions = readUtf8(JAVA_ROOT.resolve("registry").resolve("ModPotions.java"));
        assertTrue(potions.indexOf("FAIRY_SLOW_FALL") > potions.indexOf("DRAGON_GREED"));
        assertTrue(potions.contains("DRAGON_GREED,\n                FAIRY_SLOW_FALL")
                        || potions.contains("DRAGON_GREED, FAIRY_SLOW_FALL"),
                "Fairy Slow Fall must append to potion registration order");
    }

    @Test
    void fairyConstructsWithAttributesAndRoundTripsVariantsAndLeftHandedNbt() {
        Bootstrap.register();

        TestFairyFamiliar fairy = new TestFairyFamiliar(null);
        assertEquals(0.6F, fairy.width, 0.000001F);
        assertEquals(1.0F, fairy.height, 0.000001F);
        assertAttribute(fairy, SharedMonsterAttributes.MAX_HEALTH, 18.0D);
        assertAttribute(fairy, SharedMonsterAttributes.MOVEMENT_SPEED, 0.3D);
        assertAttribute(fairy, SharedMonsterAttributes.FLYING_SPEED, 0.4D);
        assertAttribute(fairy, SharedMonsterAttributes.ARMOR, 15.0D);
        assertAttribute(fairy, SharedMonsterAttributes.ARMOR_TOUGHNESS, 10.0D);
        assertAttribute(fairy, SharedMonsterAttributes.ATTACK_DAMAGE, 6.0D);
        assertTrue(fairy.ignoreFrustumCheck,
                "The upstream noCulling flag maps to ignoreFrustumCheck on 1.12");
        assertTrue(fairy.hasNoGravity());
        assertTrue(fairy.isImmuneToFire());
        assertTrue(fairy instanceof IFamiliar);
        assertFalse(IBlacksmithUpgradable.class.isInstance(fairy),
                "Fixed upstream Fairy cannot be upgraded by a Blacksmith");
        assertSame(fairy, fairy.getFamiliarEntity());
        assertNull(fairy.getFamiliarOwner());
        assertNull(fairy.getMagicTarget());
        assertEquals(0, fairy.getSaveCooldown());

        invokeBooleanSetter(fairy, "setTeeth", true);
        invokeBooleanSetter(fairy, "setFlower", true);
        invokeBooleanSetter(fairy, "setLeftHanded", true);
        assertTrue(fairy.hasTeeth());
        assertTrue(fairy.hasFlower());
        assertTrue(fairy.isLeftHanded());

        NBTTagCompound saved = new NBTTagCompound();
        fairy.writeEntityToNBT(saved);
        assertEquals(3, saved.getByte("variants") & 0xff);
        assertTrue(saved.getBoolean("LeftHanded"));
        assertFalse(saved.hasKey("saveCooldown"),
                "The selected upstream snapshot intentionally keeps Fairy rescue cooldown transient");

        TestFairyFamiliar loaded = new TestFairyFamiliar(null);
        loaded.readEntityFromNBT(saved);
        assertTrue(loaded.hasTeeth());
        assertTrue(loaded.hasFlower());
        assertTrue(loaded.isLeftHanded());
        assertTrue(loaded.hasNoGravity());
        assertTrue(loaded.isImmuneToFire());

        String entity = readUtf8Unchecked(JAVA_ROOT.resolve("entity").resolve("EntityFairyFamiliar.java"));
        String spawn = section(entity, "public IEntityLivingData onInitialSpawn", "public void onLivingUpdate");
        assertTrue(spawn.contains("this.setTeeth(this.rand.nextBoolean());"));
        assertTrue(spawn.contains("this.setLeftHanded(this.rand.nextBoolean());"));
        assertTrue(spawn.contains("this.setFlower(this.rand.nextDouble() < 0.1D);"));
    }

    @Test
    void magicGoalKeepsTenTickDamageSlowAndHealingCadence() throws IOException {
        String entity = readUtf8(JAVA_ROOT.resolve("entity").resolve("EntityFairyFamiliar.java"));
        String magic = section(entity, "private static final class MagicGoal",
                "private static final class SetAttackTargetGoal");
        assertEquals(2, countOccurrences(magic, "this.attackTick = 10;"),
                "Magic must start and reset on the fixed ten-tick cadence");
        assertTrue(magic.contains("if (this.attackTick <= 0)"));
        assertFalse(magic.contains("this.attackTick--;"),
                "EntityAIAttackMelee already decrements attackTick once per tick");
        assertTrue(magic.contains("DamageSource.causePlayerDamage((EntityPlayer) owner)"));
        assertTrue(magic.contains("DamageSource.causeMobDamage(owner)"));
        assertTrue(magic.contains("enemy.attackEntityFrom(source, 3.0F);"));
        assertTrue(magic.contains("new PotionEffect(MobEffects.SLOWNESS, 40, 1)"),
                "The drained enemy must receive Slowness II for two seconds");
        assertTrue(magic.contains("getEntityBoundingBox().grow(7.0D)"));
        assertTrue(magic.contains("EntityFairyFamiliar.isFamiliarOwnedBy(entity, owner)"));
        assertTrue(magic.contains("allies.add(owner);"));
        assertTrue(magic.contains("ally.heal(3.0F);"));

        String target = entity.substring(entity.indexOf("private static final class SetAttackTargetGoal"));
        assertTrue(target.contains("owner.getDistanceSq(this.fairy) >= 400.0D"));
        assertTrue(target.contains("target instanceof EntityPlayer || isFamiliar(target)"));
        assertTrue(target.contains("owner.getLastAttackedEntityTime()"));
    }

    @Test
    void supportGoalCoversFiveEffectsAndPerTargetTwoHundredTickCooldown() throws Exception {
        assertEquals(200, intConstant(EntityFairyFamiliar.class, "SUPPORT_DURATION"));

        String entity = readUtf8(JAVA_ROOT.resolve("entity").resolve("EntityFairyFamiliar.java"));
        String support = section(entity, "private static final class SupportGoal",
                "private static final class MagicGoal");
        assertTrue(support.contains("private final Map<UUID, Integer> cooldowns"));
        assertTrue(support.contains("this.fairy.ticksExisted % 5 != 0"));
        assertTrue(support.contains("getEntityBoundingBox().grow(10.0D)"));
        assertTrue(support.contains("EntityFairyFamiliar.isFamiliarOwnedBy(entity, owner)"));
        assertTrue(support.contains("familiar.isBurning()"));
        assertTrue(support.contains("MobEffects.FIRE_RESISTANCE"));
        assertTrue(support.contains("familiar.isInWater()"));
        assertTrue(support.contains("MobEffects.WATER_BREATHING"));
        assertTrue(support.contains("familiar.fallDistance > 3.0F"));
        assertTrue(support.contains("ModPotions.FAIRY_SLOW_FALL"));
        assertTrue(support.contains("hasLongPath((EntityLiving) familiar)"));
        assertTrue(support.contains("MobEffects.SPEED"));
        assertTrue(support.contains("familiar.getRevengeTarget() != null"));
        assertTrue(support.contains("MobEffects.RESISTANCE"));
        assertTrue(support.contains("this.cooldowns.put(id, Integer.valueOf(SUPPORT_DURATION));"));
        assertTrue(support.contains("new PotionEffect(potion, SUPPORT_DURATION, 0, false, false)"));

        Bootstrap.register();
        assertEquals(new ResourceLocation("occultism", "fairy_slow_fall"),
                ModPotions.FAIRY_SLOW_FALL.getRegistryName());
        assertEquals("effect.occultism.fairy_slow_fall", ModPotions.FAIRY_SLOW_FALL.getName());
        assertEquals(0xEEE680, ModPotions.FAIRY_SLOW_FALL.getLiquidColor());
        assertTrue(ModPotions.FAIRY_SLOW_FALL.isBeneficial());
        String handler = readUtf8(JAVA_ROOT.resolve("handler").resolve("FamiliarEventHandler.java"));
        String slowFall = section(handler, "public static void onLivingUpdate", "private static void applyFairySave");
        assertTrue(slowFall.contains("entity.motionY = Math.max(entity.motionY, -0.125D);"));
        assertTrue(slowFall.contains("entity.fallDistance = 0.0F;"));
    }

    @Test
    void deathRescueUsesFourHundredTicksOwnerRangeAndExclusions() throws Exception {
        assertEquals(400, intConstant(EntityFairyFamiliar.class, "SAVE_COOLDOWN"));

        String entity = readUtf8(JAVA_ROOT.resolve("entity").resolve("EntityFairyFamiliar.java"));
        String save = section(entity, "public boolean saveFamiliar", "public int getSaveCooldown");
        assertTrue(save.contains("this.saveCooldown > 0 || this.isSitting()"));
        assertFalse(save.contains("isEntityAlive()"),
                "The lethal-damage event invokes rescue after vanilla marks the target dead");
        assertTrue(save.contains("this.saveCooldown = SAVE_COOLDOWN;"));
        assertTrue(save.contains("this.world.setEntityState(this, SUPPORT_STATUS);"));

        String handler = readUtf8(JAVA_ROOT.resolve("handler").resolve("FamiliarEventHandler.java"));
        String rescue = section(handler, "private static void applyFairySave",
                "private static void applyBatLifesteal");
        assertTrue(rescue.contains("source == DamageSource.OUT_OF_WORLD"));
        assertTrue(rescue.contains("source.canHarmInCreative()"));
        assertTrue(rescue.contains("entity instanceof EntityGuardianFamiliar"));
        assertTrue(rescue.contains("entity instanceof EntityFairyFamiliar"));
        assertTrue(rescue.contains("EntityFairyFamiliar.getOwnerOfFamiliar(entity)"));
        assertTrue(rescue.contains("FamiliarUtil.getAllFamiliars"),
                "Fairy rescue must query nearby and equipped Fairies through the shared utility");
        assertTrue(rescue.contains("fairy.isOwner(owner)"));
        assertTrue(rescue.contains("selected.saveFamiliar(entity)"));
        int canceled = rescue.indexOf("event.setCanceled(true);");
        int health = rescue.indexOf("entity.setHealth(2.0F);");
        int absorption = rescue.indexOf("new PotionEffect(MobEffects.ABSORPTION, 20 * 5, 2)");
        assertTrue(canceled >= 0 && health > canceled && absorption > health);
        assertTrue(rescue.contains("selected.spawnSupportParticles(entity);"));
    }

    @Test
    void ringHonoursFamiliarOwnershipAndThrownSwordsTreatIFamiliarAsFriendly() throws Exception {
        String ring = readUtf8(JAVA_ROOT.resolve("item").resolve("ItemFamiliarRing.java"));
        String supported = section(ring,
                "private static boolean isSupportedStoredFamiliar(@Nullable Entity entity) {",
                "private static boolean canCaptureFamiliar");
        assertTrue(supported.contains("entity instanceof EntityFairyFamiliar"));
        // Capture is interface-driven, so Fairy inherits the shared IFamiliar ownership gate.
        // Contract source: upstream FamiliarRingItem.interactLivingEntity, which captures when
        // getFamiliarOwner() == player || getFamiliarOwner() == null (unbound familiars may be claimed).
        String capture = section(ring,
                "private static boolean canCaptureFamiliar(@Nullable Entity entity, EntityPlayer player) {",
                "private static boolean isRitualFamiliarParrot");
        assertTrue(Pattern.compile("if \\(!\\(entity instanceof IFamiliar\\)\\) \\{\\s*return false;\\s*\\}\\s*"
                + "IFamiliar familiar = \\(IFamiliar\\) entity;\\s*"
                + "EntityLivingBase owner = familiar\\.getFamiliarOwner\\(\\);[\\s\\S]*?"
                + "return owner == null \\|\\| owner == player;")
                .matcher(capture).find(),
                "canCaptureFamiliar must gate on IFamiliar and allow only the owner, or any player "
                        + "while the familiar is still unbound");
        assertFalse(capture.contains("isTamed()"),
                "Per-species isTamed() gates were replaced by the shared IFamiliar ownership contract");

        String swordSource = readUtf8(JAVA_ROOT.resolve("entity").resolve("EntityThrownSword.java"));
        assertTrue(swordSource.contains("target instanceof IFamiliar"));
        assertTrue(swordSource.contains("((IFamiliar) target).getFamiliarOwner() == owner"));

        TestFairyFamiliar owner = new TestFairyFamiliar(null);
        TestFairyFamiliar sameOwner = new TestFairyFamiliar(owner);
        TestFairyFamiliar foreignOwner = new TestFairyFamiliar(null);
        TestFairyFamiliar foreign = new TestFairyFamiliar(foreignOwner);
        TestThrownSword sword = new TestThrownSword();
        sword.setThrower(owner);
        assertTrue(isFriendly(sword, sameOwner));
        assertFalse(isFriendly(sword, foreign));
        sword.impact(sameOwner);
        assertFalse(sword.isDead,
                "A Dragon sword overlapping a same-owner Fairy must return before damage and removal");
    }

    @Test
    void fairyModelRendererAndTexturesMatchTheSelectedUpstreamSnapshot()
            throws IOException, NoSuchAlgorithmException {
        String model = readUtf8(JAVA_ROOT.resolve("client").resolve("model")
                .resolve("ModelFairyFamiliar.java"));
        assertTrue(model.contains("this.textureWidth = 64;"));
        assertTrue(model.contains("this.textureHeight = 32;"));
        assertEquals(36, countOccurrences(model, "public final ModelRenderer"));
        assertTrue(model.contains("fairy.getSupportAnim(partialTicks)"));
        assertTrue(model.contains("fairy.hasMagicTarget()"));
        assertTrue(model.contains("fairy.isPartying()"));
        assertTrue(model.contains("fairy.isSitting()"));
        assertTrue(model.contains("this.flower.showModel = fairy.hasFlower();"));
        assertTrue(model.contains("this.leftWand.showModel = isLeftHanded;"));
        assertTrue(model.contains("this.rightWand.showModel = !isLeftHanded;"));

        Path rendererPath = JAVA_ROOT.resolve("client").resolve("render")
                .resolve("RenderFairyFamiliar.java");
        assertTrue(Files.isRegularFile(rendererPath), "Missing dedicated Fairy renderer");
        String renderer = readUtf8(rendererPath);
        assertTrue(renderer.contains("new ModelFairyFamiliar()"));
        assertTrue(renderer.contains("textures/entity/fairy_familiar.png"));
        assertTrue(renderer.contains("textures/entity/fairy_familiar_sleep.png"));
        assertTrue(renderer.contains("SleepLayer"));
        assertTrue(renderer.contains("getMagicPosition"));
        assertTrue(renderer.contains("getMagicRadiusAngle"));
        assertTrue(renderer.contains("isSitting()") && renderer.contains("isPartying()"));

        String doRender = section(renderer, "public void doRender(", "protected void renderLivingAt(");
        int xOffset = doRender.indexOf("x += offset.x;");
        int yOffset = doRender.indexOf("y += offset.y;");
        int zOffset = doRender.indexOf("z += offset.z;");
        int bob = doRender.indexOf("y += entity.getAnimationHeight(partialTicks);");
        int superRender = doRender.indexOf(
                "super.doRender(entity, x, y, z, entityYaw, partialTicks);");
        assertTrue(xOffset >= 0 && yOffset >= 0 && zOffset >= 0);
        assertTrue(bob > xOffset && bob > yOffset && bob > zOffset && superRender > bob,
                "Pass the magic offset and animation bob to the parent renderer as world coordinates");
        assertFalse(doRender.contains("GlStateManager.rotate("),
                "An outer rotation would also rotate the camera-relative nameplate transform");

        String renderLivingAt = section(renderer, "protected void renderLivingAt(",
                "protected void renderModel(");
        int parentTranslation = renderLivingAt.indexOf("super.renderLivingAt(entity, x, y, z);");
        int localRotation = renderLivingAt.indexOf("GlStateManager.rotate(");
        assertTrue(parentTranslation >= 0 && localRotation > parentTranslation,
                "Translate through RenderLivingBase before applying the Fairy-local magic rotation");
        assertEquals(1, countOccurrences(renderer, "GlStateManager.rotate("),
                "Keep the magic rotation local to the model matrix so nameplates remain camera-facing");

        String renderModel = section(renderer, "protected void renderModel(",
                "private static Vec3d interpolate");
        int enableBlend = renderModel.indexOf("GlStateManager.enableBlend();");
        int parentModel = renderModel.indexOf("super.renderModel(");
        int disableBlend = renderModel.indexOf("GlStateManager.disableBlend();");
        assertTrue(enableBlend >= 0 && parentModel > enableBlend && disableBlend > parentModel,
                "Blend the alpha-bearing Fairy texture only while rendering the model");
        assertTrue(Pattern.compile("GlStateManager\\.tryBlendFuncSeparate\\("
                + "GlStateManager\\.SourceFactor\\.SRC_ALPHA,\\s*"
                + "GlStateManager\\.DestFactor\\.ONE_MINUS_SRC_ALPHA,\\s*"
                + "GlStateManager\\.SourceFactor\\.ONE,\\s*"
                + "GlStateManager\\.DestFactor\\.ZERO\\);", Pattern.DOTALL)
                .matcher(renderModel).find());
        assertTrue(Pattern.compile("finally\\s*\\{\\s*GlStateManager\\.disableBlend\\(\\);\\s*\\}",
                Pattern.DOTALL).matcher(renderModel).find(),
                "Blend state must be restored even when model rendering throws");

        String renderers = readUtf8(JAVA_ROOT.resolve("client").resolve("render")
                .resolve("ModEntityRenderers.java"));
        assertTrue(renderers.contains("RenderingRegistry.registerEntityRenderingHandler(EntityFairyFamiliar.class"));
        assertTrue(renderers.contains("return new RenderFairyFamiliar(manager);"));

        assertPng("textures/entity/fairy_familiar.png", 64, 32, FAIRY_TEXTURE_SHA256);
        assertPng("textures/entity/fairy_familiar_sleep.png", 64, 32, FAIRY_SLEEP_TEXTURE_SHA256);
        assertPng("textures/gui/book/familiar_fairy.png", 64, 64, FAIRY_BOOK_ICON_SHA256);

        String rootNotices = readUtf8(Paths.get("THIRD_PARTY_NOTICES.md"));
        String resourceNotices = readUtf8(ROOT.resolve("resources").resolve("THIRD_PARTY_NOTICES.md"));
        for (String notices : Arrays.asList(rootNotices, resourceNotices)) {
            assertTrue(notices.contains("Vemerion Familiar models, textures, and behavior"));
            assertTrue(notices.contains("Dragon, Fairy, Goat, Shub Niggurath, Shub Niggurath Spawn, Beholder"));
            assertTrue(notices.contains("Copyright 2021 vemerion"));
        }

        JsonObject dummy = readJsonObject(ASSET_ROOT.resolve("models").resolve("item")
                .resolve("ritual_dummy").resolve("familiar_fairy.json"));
        assertEquals("occultism:item/ritual_dummy", getString(dummy, "parent"));
    }

    @Test
    void dictionaryHas232Entries788PagesAndFairyFallback() throws IOException {
        JsonObject root = readJsonObject(ASSET_ROOT.resolve("dictionary").resolve("modonomicon_index.json"));
        assertEquals(9, root.getAsJsonArray("categories").size());
        int entries = 0;
        int pages = 0;
        for (JsonElement categoryElement : root.getAsJsonArray("categories")) {
            JsonArray categoryEntries = categoryElement.getAsJsonObject().getAsJsonArray("entries");
            entries += categoryEntries.size();
            for (JsonElement entryElement : categoryEntries) {
                pages += entryElement.getAsJsonObject().getAsJsonArray("pages").size();
            }
        }
        assertEquals(232, entries);
        assertEquals(788, pages);

        JsonObject category = findObject(root.getAsJsonArray("categories"), "id", "familiar_rituals");
        assertNotNull(category);
        JsonObject entry = findObject(category.getAsJsonArray("entries"), "id", "familiar_fairy");
        assertNotNull(entry);
        assertEquals("occultism:textures/gui/book/familiar_fairy.png", getString(entry, "icon"));
        JsonArray fairyPages = entry.getAsJsonArray("pages");
        assertEquals(3, fairyPages.size());
        Set<String> pageIds = new HashSet<>();
        for (JsonElement pageElement : fairyPages) {
            pageIds.add(getString(pageElement.getAsJsonObject(), "id"));
        }
        assertEquals(new HashSet<>(Arrays.asList("entity", "ritual", "description")), pageIds);
        assertEquals("occultism:fairy_familiar",
                getString(findObject(fairyPages, "id", "entity"), "entity_id"));
        assertEquals("occultism:ritual/familiar_fairy",
                getString(findObject(fairyPages, "id", "ritual"), "recipe_id_1"));
        assertEquals("book.occultism.dictionary_of_spirits.familiar_rituals.familiar_fairy.description.text",
                getString(findObject(fairyPages, "id", "description"), "text"));

        String generator = readUtf8(TOOLS_ROOT.resolve("generate_modonomicon_index.py"));
        assertTrue(Pattern.compile("\"familiar_fairy\"\\s*:\\s*\\(\"entity\",\\s*"
                + "\"ritual\",\\s*\"description\"\\)").matcher(generator).find());
        String gui = readUtf8(JAVA_ROOT.resolve("client").resolve("gui")
                .resolve("GuiDictionaryOfSpirits.java"));
        assertTrue(gui.contains("entry(\"familiar_fairy\")"),
                "The no-resource fallback dictionary must expose Fairy");
    }

    @Test
    void bilingualTextDescribesSupportRescueNoUpgradeAndCuriosBoundary() throws IOException {
        String english = readUtf8(ASSET_ROOT.resolve("lang").resolve("en_us.lang"));
        String chinese = readUtf8(ASSET_ROOT.resolve("lang").resolve("zh_cn.lang"));
        assertEquals("Fairy Familiar", langValue(english, "entity.occultism.fairy_familiar.name"));
        assertEquals("\u7cbe\u7075\u4f7f\u9b54", langValue(chinese, "entity.occultism.fairy_familiar.name"));
        assertEquals("Fairy Slow Fall", langValue(english, "effect.occultism.fairy_slow_fall"));
        assertEquals("\u7cbe\u7075\u7f13\u964d", langValue(chinese, "effect.occultism.fairy_slow_fall"));

        String englishTooltip = langValue(english, "item.occultism.ritual_dummy.familiar_fairy.tooltip");
        assertTrue(englishTooltip.contains("saves other familiars from death"));
        assertTrue(englishTooltip.contains("support effects"));
        assertTrue(englishTooltip.contains("cannot be upgraded"));
        assertTrue(englishTooltip.contains("native one-slot ring behavior"));
        String chineseTooltip = langValue(chinese, "item.occultism.ritual_dummy.familiar_fairy.tooltip");
        assertTrue(chineseTooltip.contains("\u6551\u4e0b\u5176\u4ed6\u4f7f\u9b54"));
        assertTrue(chineseTooltip.contains("\u589e\u76ca"));
        assertTrue(chineseTooltip.contains("\u4e0d\u53ef\u88ab\u94c1\u5320\u5347\u7ea7"));
        assertTrue(chineseTooltip.contains("\u539f\u751f\u5355\u69fd\u6212\u6307\u884c\u4e3a"));

        String englishGuide = langValue(english, "guide.occultism.dictionary.familiar_fairy.body");
        String chineseGuide = langValue(chinese, "guide.occultism.dictionary.familiar_fairy.body");
        assertFalse(englishGuide.isEmpty());
        assertFalse(chineseGuide.isEmpty());
        assertFalse((englishTooltip + englishGuide).contains("glass bottle"),
                "Fairy must require Dragon's Breath directly, not a 1.12 glass-bottle substitute");
        assertFalse((chineseTooltip + chineseGuide).contains("\u73bb\u7483\u74f6"),
                "Fairy must require Dragon's Breath directly, not a 1.12 glass-bottle substitute");
        assertFalse(langValue(english,
                "book.occultism.dictionary_of_spirits.familiar_rituals.familiar_fairy.entity.text").isEmpty());
        assertFalse(langValue(chinese,
                "book.occultism.dictionary_of_spirits.familiar_rituals.familiar_fairy.description.text").isEmpty());
    }

    private static void assertAttribute(EntityLivingBase entity, IAttribute attribute, double expected) {
        assertNotNull(entity.getEntityAttribute(attribute), "Missing entity attribute: " + attribute.getName());
        assertEquals(expected, entity.getEntityAttribute(attribute).getBaseValue(), 0.000001D,
                "Unexpected base value for " + attribute.getName());
    }

    private static void assertPng(String relativePath, int width, int height, String expectedSha)
            throws IOException, NoSuchAlgorithmException {
        Path path = ASSET_ROOT.resolve(relativePath);
        BufferedImage image = ImageIO.read(path.toFile());
        assertNotNull(image, relativePath + " must be a readable PNG");
        assertEquals(width, image.getWidth());
        assertEquals(height, image.getHeight());
        assertEquals(expectedSha, sha256(path));
    }

    private static boolean isFriendly(EntityThrownSword sword, Entity target) throws Exception {
        Method method = EntityThrownSword.class.getDeclaredMethod("isFriendly", Entity.class);
        method.setAccessible(true);
        return ((Boolean) method.invoke(sword, target)).booleanValue();
    }

    private static int intConstant(Class<?> type, String name) throws Exception {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        return field.getInt(null);
    }

    private static void invokeBooleanSetter(Object target, String name, boolean value) {
        Class<?> type = target.getClass();
        while (type != null) {
            try {
                Method method = type.getDeclaredMethod(name, boolean.class);
                method.setAccessible(true);
                method.invoke(target, Boolean.valueOf(value));
                return;
            } catch (NoSuchMethodException exception) {
                type = type.getSuperclass();
            } catch (ReflectiveOperationException exception) {
                throw new AssertionError("Unable to call " + name, exception);
            }
        }
        throw new AssertionError("Missing boolean setter: " + name);
    }

    private static JsonObject findObject(JsonArray array, String key, String value) {
        if (array == null) {
            return null;
        }
        for (JsonElement element : array) {
            JsonObject object = element.getAsJsonObject();
            if (value.equals(getString(object, key))) {
                return object;
            }
        }
        return null;
    }

    private static int countObjects(JsonArray array, String key, String value) {
        int count = 0;
        for (JsonElement element : array) {
            if (value.equals(getString(element.getAsJsonObject(), key))) {
                count++;
            }
        }
        return count;
    }

    private static String getString(JsonObject object, String key) {
        JsonElement value = object == null ? null : object.get(key);
        if (value == null || value.isJsonNull()) {
            return null;
        }
        if (value.isJsonObject() && value.getAsJsonObject().has("texture")) {
            return value.getAsJsonObject().get("texture").getAsString();
        }
        return value.getAsString();
    }

    private static String langValue(String contents, String key) {
        String prefix = key + "=";
        for (String line : contents.split("\\r?\\n")) {
            if (line.startsWith(prefix)) {
                return line.substring(prefix.length());
            }
        }
        throw new AssertionError("Missing language key: " + key);
    }

    private static String section(String source, String start, String end) {
        int startIndex = source.indexOf(start);
        assertTrue(startIndex >= 0, "Missing source marker: " + start);
        int endIndex = source.indexOf(end, startIndex + start.length());
        assertTrue(endIndex >= 0, "Missing source marker after " + start + ": " + end);
        return source.substring(startIndex, endIndex);
    }

    private static int countOccurrences(String source, String token) {
        int count = 0;
        int index = 0;
        while ((index = source.indexOf(token, index)) >= 0) {
            count++;
            index += token.length();
        }
        return count;
    }

    private static String sha256(Path path) throws IOException, NoSuchAlgorithmException {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path));
        StringBuilder hex = new StringBuilder(digest.length * 2);
        for (byte value : digest) {
            hex.append(String.format("%02x", value & 0xff));
        }
        return hex.toString();
    }

    private static JsonObject readJsonObject(Path path) throws IOException {
        return readJsonElement(path).getAsJsonObject();
    }

    private static JsonElement readJsonElement(Path path) throws IOException {
        return new JsonParser().parse(readUtf8(path));
    }

    private static String readUtf8(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    private static String readUtf8Unchecked(Path path) {
        try {
            return readUtf8(path);
        } catch (IOException exception) {
            throw new AssertionError("Unable to read " + path, exception);
        }
    }

    private static final class TestFairyFamiliar extends EntityFairyFamiliar {

        private final EntityLivingBase familiarOwner;

        private TestFairyFamiliar(EntityLivingBase familiarOwner) {
            super(null);
            this.familiarOwner = familiarOwner;
        }

        @Override
        public EntityLivingBase getFamiliarOwner() {
            return this.familiarOwner;
        }

        @Override
        public int getGrowingAge() {
            return 0;
        }
    }

    private static final class TestThrownSword extends EntityThrownSword {

        private TestThrownSword() {
            super(null);
        }

        private void impact(EntityLivingBase target) {
            super.onImpact(new RayTraceResult(target));
        }
    }
}
