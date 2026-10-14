package com.occultism.occultism.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
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
import com.occultism.occultism.entity.EntityDragonFamiliar;
import com.occultism.occultism.entity.EntityThrownSword;
import com.occultism.occultism.registry.ModPotions;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.init.Bootstrap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import org.junit.jupiter.api.Test;

class DragonPortContractTest {

    private static final Path ROOT = Paths.get("src", "main");
    private static final Path JAVA_ROOT = ROOT.resolve("java").resolve("com").resolve("occultism")
            .resolve("occultism");
    private static final Path ASSET_ROOT = ROOT.resolve("resources").resolve("assets").resolve("occultism");
    private static final Path TOOLS_ROOT = Paths.get("tools");
    private static final String DRAGON_TEXTURE_SHA256 =
            "d9ebebb1946ae26be89e94f0ec5ba158024f7d2e5e1c64e48c897d7845a58d81";
    private static final String DRAGON_BOOK_ICON_SHA256 =
            "0027f8176f5ee4f96f4120aac29d600a59945898a97cb2cf0f0e2f4702af764d";

    @Test
    void dragonRitualRetainsThe1814TaggedContract() throws IOException {
        JsonArray rituals = readJsonElement(ASSET_ROOT.resolve("rituals").resolve("rituals.json"))
                .getAsJsonArray();
        JsonObject actual = findObject(rituals, "id", "familiar_dragon");
        assertNotNull(actual, "Missing familiar_dragon ritual");
        assertEquals(1, countObjects(rituals, "id", "familiar_dragon"));

        JsonObject expected = new JsonObject();
        expected.addProperty("id", "familiar_dragon");
        expected.addProperty("display_name", "Dragon Familiar");
        expected.addProperty("type", "occultism:ritual");
        expected.addProperty("ritual_type", "occultism:summon_tamed");
        expected.addProperty("activation_item", "occultism:book_of_binding_bound_djinni");
        expected.addProperty("duration_ticks", 1200);

        JsonObject sacrifice = new JsonObject();
        sacrifice.addProperty("display_name", "ritual.occultism.sacrifice.horses");
        sacrifice.addProperty("tag", "c:horses");
        expected.add("entity_to_sacrifice", sacrifice);
        expected.addProperty("entity_to_summon", "occultism:dragon_familiar");

        JsonArray ingredients = new JsonArray();
        ingredients.add("minecraft:lava_bucket");
        ingredients.add("minecraft:flint_and_steel");
        ingredients.add("#c:storage_blocks/coal");
        ingredients.add("#c:storage_blocks/quartz");
        ingredients.add("#c:storage_blocks/gold");
        ingredients.add("#c:gunpowders");
        ingredients.add("#c:obsidians");
        ingredients.add("#c:obsidians");
        expected.add("ingredients", ingredients);
        expected.addProperty("pentacle_id", "occultism:possess_djinni");

        JsonObject result = new JsonObject();
        result.addProperty("id", "occultism:soul_gem");
        expected.add("result", result);
        JsonObject dummy = new JsonObject();
        dummy.addProperty("id", "occultism:ritual_dummy/familiar_dragon");
        expected.add("ritual_dummy", dummy);
        assertEquals(expected, actual,
                "The 1.12 ritual must preserve 1.81.4 ingredient tags and the local summon_tamed result contract");

        String sacrificeSource = readUtf8(JAVA_ROOT.resolve("ritual").resolve("RitualSacrifice.java"));
        assertTrue(Pattern.compile("case \"c:horses\":\\s*return entity instanceof EntityHorse "
                + "\\|\\| entity instanceof EntityDonkey \\|\\| entity instanceof EntityMule\\s*"
                + "\\|\\| entity instanceof EntityLlama;", Pattern.DOTALL).matcher(sacrificeSource).find(),
                "The 1.12 horse tag must accept Horse, Donkey, Mule, and Llama exactly");

        String manager = readUtf8(JAVA_ROOT.resolve("ritual").resolve("RitualRecipeManager.java"));
        assertTrue(manager.contains("if (\"gunpowders\".equals(path))"));
        assertTrue(Pattern.compile("if \\(\"storage_blocks\"\\.equals\\(kind\\)\\) \\{\\s*kind = \"block\";",
                Pattern.DOTALL).matcher(manager).find());
        String oreDictionary = readUtf8(JAVA_ROOT.resolve("registry").resolve("ModOreDictionary.java"));
        assertTrue(oreDictionary.contains("OreDictionary.registerOre(\"obsidians\", Blocks.OBSIDIAN);"));
    }

    @Test
    void dragonSwordPotionAndDummyUseAppendOnlyRegistrationOrder() throws IOException {
        String entities = readUtf8(JAVA_ROOT.resolve("registry").resolve("ModEntities.java"));
        int blacksmith = entities.indexOf("new ResourceLocation(Tags.MOD_ID, \"blacksmith_familiar\")");
        int dragon = entities.indexOf("new ResourceLocation(Tags.MOD_ID, \"dragon_familiar\")");
        int sword = entities.indexOf("new ResourceLocation(Tags.MOD_ID, \"thrown_sword\")");
        assertTrue(blacksmith >= 0 && dragon > blacksmith && sword > dragon,
                "Dragon and its projectile must be appended after every existing numeric entity ID");
        assertEquals(1, countOccurrences(entities,
                "new ResourceLocation(Tags.MOD_ID, \"dragon_familiar\")"));
        assertEquals(1, countOccurrences(entities,
                "new ResourceLocation(Tags.MOD_ID, \"thrown_sword\")"));
        assertTrue(Pattern.compile("EntityDragonFamiliar\\.class, \"dragon_familiar\", nextEntityId\\+\\+, "
                + "Occultism\\.INSTANCE, 64, 3, true\\);", Pattern.DOTALL).matcher(entities).find());
        assertTrue(Pattern.compile("EntityThrownSword\\.class, \"thrown_sword\", nextEntityId\\+\\+, "
                + "Occultism\\.INSTANCE, 64, 1, true\\);", Pattern.DOTALL).matcher(entities).find());

        String items = readUtf8(JAVA_ROOT.resolve("registry").resolve("ModItems.java"));
        assertEquals(2, countOccurrences(items, "RITUAL_DUMMY_FAMILIAR_DRAGON"),
                "The Dragon dummy must be declared once and included in ITEMS once");
        String itemArray = section(items, "public static final Item[] ITEMS = {", "};");
        assertTrue(itemArray.indexOf("RITUAL_DUMMY_FAMILIAR_DRAGON") > itemArray.indexOf("BEAVER_NUGGET"),
                "The Dragon dummy must append after the previous final item without shifting old IDs");

        String potions = readUtf8(JAVA_ROOT.resolve("registry").resolve("ModPotions.java"));
        assertTrue(potions.indexOf("DRAGON_GREED") > potions.indexOf("BAT_LIFESTEAL"));
        assertTrue(potions.contains("BAT_LIFESTEAL, DRAGON_GREED"));

        String renderers = readUtf8(JAVA_ROOT.resolve("client").resolve("render")
                .resolve("ModEntityRenderers.java"));
        assertTrue(renderers.contains("RenderingRegistry.registerEntityRenderingHandler(EntityDragonFamiliar.class"));
        assertTrue(renderers.contains("return new RenderDragonFamiliar(manager);"));
        assertTrue(renderers.contains("RenderingRegistry.registerEntityRenderingHandler(EntityThrownSword.class"));
        assertTrue(renderers.contains("return new RenderThrownSword(manager);"));
    }

    @Test
    void dragonAttributesUpgradeVariantsAndTimersRoundTripThroughNbt() {
        Bootstrap.register();

        EntityDragonFamiliar dragon = new TestDragonFamiliar();
        assertEquals(1.0F, dragon.width, 0.000001F);
        assertEquals(0.8F, dragon.height, 0.000001F);
        assertAttribute(dragon, SharedMonsterAttributes.MAX_HEALTH, 30.0D);
        assertAttribute(dragon, SharedMonsterAttributes.ARMOR, 25.0D);
        assertAttribute(dragon, SharedMonsterAttributes.ARMOR_TOUGHNESS, 10.0D);
        assertAttribute(dragon, SharedMonsterAttributes.MOVEMENT_SPEED, 0.3D);
        assertAttribute(dragon, SharedMonsterAttributes.ATTACK_DAMAGE, 6.0D);
        assertEquals(EntityDragonFamiliar.MAX_PET_TIMER, dragon.getPetTimer());
        assertFalse(dragon.hasBlacksmithUpgrade());
        assertTrue(dragon.canBlacksmithUpgrade());
        assertFalse(dragon.hasSword());
        assertSame(dragon, dragon.getFamiliarEntity());
        assertNull(dragon.getFamiliarOwner());

        dragon.blacksmithUpgrade();
        assertTrue(dragon.hasBlacksmithUpgrade());
        assertFalse(dragon.canBlacksmithUpgrade());
        assertTrue(dragon.hasSword());

        NBTTagCompound legacy = new NBTTagCompound();
        dragon.writeEntityToNBT(legacy);
        legacy.removeTag("variants");
        legacy.setBoolean("hasFez", true);
        legacy.setBoolean("hasEars", true);
        legacy.setBoolean("hasArms", true);
        legacy.setBoolean("hasStick", true);
        legacy.setInteger("greedyTimer", 4321);

        EntityDragonFamiliar loaded = new TestDragonFamiliar();
        loaded.readEntityFromNBT(legacy);
        assertTrue(loaded.hasFez());
        assertTrue(loaded.hasEars());
        assertTrue(loaded.hasArms());
        assertTrue(loaded.hasStick());
        assertTrue(loaded.hasBlacksmithUpgrade());

        NBTTagCompound saved = new NBTTagCompound();
        loaded.writeEntityToNBT(saved);
        assertEquals(15, saved.getByte("variants") & 0xff);
        assertTrue(saved.getBoolean("hasBlacksmithUpgrade"));
        assertEquals(4321, saved.getInteger("greedyTimer"));

        EntityDragonFamiliar roundTrip = new TestDragonFamiliar();
        roundTrip.readEntityFromNBT(saved);
        assertTrue(roundTrip.hasFez() && roundTrip.hasEars() && roundTrip.hasArms() && roundTrip.hasStick());
        assertTrue(roundTrip.hasBlacksmithUpgrade());
        roundTrip.blacksmithUpgrade();
        assertTrue(roundTrip.hasBlacksmithUpgrade(), "A second Blacksmith pass must remain idempotent");
    }

    @Test
    void ownerEffectSlowFallAndInteractionOrderMatch1814() throws IOException {
        String entity = readUtf8(JAVA_ROOT.resolve("entity").resolve("EntityDragonFamiliar.java"));
        assertTrue(entity.contains("private static final int GREEDY_INCREMENT = 6000;"));
        assertTrue(entity.contains("private static final double OWNER_EFFECT_RANGE_SQ = 64.0D;"));
        assertTrue(entity.contains("this.isImmuneToFire = true;"));
        assertTrue(Pattern.compile("public void fall\\(float distance, float damageMultiplier\\) \\{\\s*\\}")
                .matcher(entity).find(), "Dragon fall damage must remain disabled");
        assertTrue(entity.contains("return super.getMountedYOffset() * 0.4D;"));

        String update = section(entity, "public void onLivingUpdate() {", "public boolean processInteract");
        assertTrue(update.contains("this.greedyTimer--;"));
        assertTrue(update.contains("!this.onGround && this.motionY < 0.0D"));
        assertTrue(update.contains("this.motionY *= 0.5D;"));
        assertTrue(update.contains("this.ticksExisted % 10 == 0"));
        assertTrue(update.contains("new PotionEffect(ModPotions.DRAGON_GREED, 300,"));
        assertTrue(update.contains("this.greedyTimer > 0 ? 1 : 0"),
                "Gold must raise Dragon's Greed from amplifier zero to one");

        String interaction = section(entity,
                "public boolean processInteract(EntityPlayer player, EnumHand hand) {", "public void swingArm");
        int returnStick = interaction.indexOf("if (this.hasStick())");
        int feedGold = interaction.indexOf("matchesOre(stack, \"nuggetGold\")");
        int pet = interaction.indexOf("stack.isEmpty() && player.isSneaking()");
        int sit = interaction.indexOf("hand == EnumHand.MAIN_HAND && this.isOwner(player)");
        assertTrue(returnStick >= 0 && feedGold > returnStick && pet > feedGold && sit > pet,
                "Stick return, gold feeding, petting, and owner sitting must keep the upstream branch order");
        assertTrue(interaction.contains("ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(Items.STICK))"));
        assertTrue(interaction.contains("this.greedyTimer += GREEDY_INCREMENT;"));
        assertTrue(interaction.contains("stack.shrink(1);"));
        assertTrue(interaction.contains("this.petTimer = 0;"));

        assertTrue(entity.contains("this.setFez(this.rand.nextDouble() < 0.1D);"));
        assertTrue(entity.contains("this.setEars(this.rand.nextDouble() < 0.5D);"));
        assertTrue(entity.contains("this.setArms(this.rand.nextDouble() < 0.5D);"));
    }

    @Test
    void dragonAiFetchesSticksAndSwitchesFromFireToThrownSwords() throws IOException {
        String entity = readUtf8(JAVA_ROOT.resolve("entity").resolve("EntityDragonFamiliar.java"));
        assertTrue(entity.contains("this.tasks.addTask(0, new EntityAIPanic(this, 1.25D));"));
        assertTrue(entity.contains("this.tasks.addTask(0, new EntityAISwimming(this));"));
        assertTrue(entity.contains("this.tasks.addTask(1, this.aiSit);"));
        assertTrue(entity.contains("this.tasks.addTask(2, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));"));
        assertTrue(entity.contains("this.tasks.addTask(3, new EntityGreedyFamiliar.FindItemGoal(this)"));
        assertTrue(entity.contains("anyMatch(entity -> entity instanceof EntityGreedyFamiliar)"));
        assertTrue(entity.contains("this.tasks.addTask(4, new FetchGoal(this));"));
        assertTrue(entity.contains("this.tasks.addTask(5, new EntityAIFollowOwner(this, 1.0D, 3.0F, 1.0F));"));
        assertTrue(entity.contains("new EntityDevilFamiliar.AttackGoal(this, 5.0F)"));
        assertTrue(entity.contains("super.shouldExecute() && !EntityDragonFamiliar.this.hasBlacksmithUpgrade()"));
        assertTrue(entity.contains("this.tasks.addTask(6, new ThrowSwordGoal(this, 100.0F));"));
        assertTrue(entity.contains("this.tasks.addTask(7, new EntityAIWanderAvoidWater(this, 1.0D));"));
        assertTrue(entity.contains("this.tasks.addTask(8, new EntityAIFollow(this, 1.0D, 3.0F, 7.0F));"));

        String fetch = entity.substring(entity.indexOf("private static final class FetchGoal"));
        assertTrue(fetch.contains("this.setMutexBits(5);"));
        assertTrue(fetch.contains("getEntityBoundingBox().grow(8.0D)"));
        assertTrue(fetch.contains("item.getItem().getItem() == Items.STICK"));
        assertTrue(fetch.contains("this.dragon.getDistanceSq(this.stick) < 3.0D"));
        assertTrue(fetch.contains("ItemStack stack = this.stick.getItem();"));
        assertTrue(fetch.contains("stack.isEmpty() || stack.getItem() != Items.STICK"),
                "A second Dragon must revalidate a cached item before consuming the same stick entity");
        assertTrue(fetch.indexOf("stack.isEmpty() || stack.getItem() != Items.STICK")
                        < fetch.indexOf("this.dragon.setStick(true);"));
        assertTrue(fetch.contains("this.dragon.setStick(true);"));
        assertTrue(fetch.contains("stack.shrink(1);"));
        assertTrue(fetch.contains("this.stick.setDead();"),
                "An exhausted stick entity must die in the same goal tick to prevent duplicate fetches");
        assertTrue(fetch.contains("this.stick.setItem(stack);"),
                "Fetch must dirty EntityItem metadata so stacked stick counts stay synchronized");

        String throwing = section(entity, "private static final class ThrowSwordGoal",
                "private static final class FetchGoal");
        assertTrue(throwing.contains("super.shouldExecute() && this.dragon.hasBlacksmithUpgrade()"));
        assertTrue(throwing.contains("new EntityThrownSword(this.dragon.world, this.dragon.getOwner())"));
        assertTrue(throwing.contains("sword.shoot(xDirection, yDirection, zDirection, 0.5F, 3.0F);"));
        assertTrue(throwing.contains("this.dragon.world.spawnEntity(sword);"));

        String sharedAttack = readUtf8(JAVA_ROOT.resolve("entity").resolve("EntityDevilFamiliar.java"));
        assertTrue(sharedAttack.contains("private static final int MAX_COOLDOWN = 20;"));
        assertTrue(sharedAttack.contains("this.familiar.getDistanceSq(target) < this.range"),
                "Dragon attack ranges are squared-distance thresholds: 5 for fire and 100 for swords");
    }

    @Test
    void greedyRideGoalUsesSameOwnerAvailableDragonsAndMoveJumpMutex() throws IOException {
        String greedy = readUtf8(JAVA_ROOT.resolve("entity").resolve("EntityGreedyFamiliar.java"));
        assertTrue(greedy.contains("this.tasks.addTask(3, new RideDragonGoal(this));"));
        String ride = section(greedy, "private static class RideDragonGoal", "private static class FindBlockGoal");
        assertTrue(ride.contains("this.setMutexBits(5);"),
                "Riding must block Greedy's own movement and jump goals, including priority-four item pickup");
        assertTrue(ride.contains("getEntitiesWithinAABB(EntityDragonFamiliar.class"));
        assertTrue(ride.contains("getEntityBoundingBox().grow(5.0D)"));
        assertTrue(ride.contains("dragon.getOwner() == owner"));
        assertTrue(ride.contains("!dragon.isBeingRidden() && !dragon.isSitting()"));
        assertTrue(ride.contains("this.rider.getDistanceSq(this.mount) < 5.0D"));
        assertTrue(ride.contains("this.rider.startRiding(this.mount);"));
        assertTrue(ride.contains("this.mount.getNavigator().clearPath();"));
        assertTrue(ride.contains("this.rider.dismountRidingEntity();"));

        String findItem = section(greedy, "public static class FindItemGoal", "private static class RideDragonGoal");
        assertTrue(findItem.contains("private final EntityTameable familiar;"),
                "Dragon must be able to reuse the capacity-aware Greedy pickup goal while carrying a Greedy");
        assertTrue(findItem.contains("new PlayerMainInvWrapper(player.inventory)"));
        assertTrue(findItem.contains("ItemHandlerHelper.insertItemStacked(inventory, stack, true)"));
        assertTrue(findItem.contains("stack.getTagCompound().getBoolean(\"PreventRemoteMovement\")"));
        assertTrue(findItem.contains("item.getEntityData().getBoolean(\"PreventRemoteMovement\")"));
        assertTrue(findItem.contains("item.onCollideWithPlayer((EntityPlayer) owner);"));
    }

    @Test
    void dragonGreedPotionAndExperienceBonusUseOnePlusAmplifier() throws IOException {
        Bootstrap.register();

        assertEquals(new ResourceLocation("occultism", "dragon_greed"),
                ModPotions.DRAGON_GREED.getRegistryName());
        assertEquals("effect.occultism.dragon_greed", ModPotions.DRAGON_GREED.getName());
        assertEquals(0xFFD700, ModPotions.DRAGON_GREED.getLiquidColor());
        assertTrue(ModPotions.DRAGON_GREED.isBeneficial());

        String handler = readUtf8(JAVA_ROOT.resolve("handler").resolve("LootEventHandler.java"));
        String experience = section(handler,
                "public static void onExperienceDrop(LivingExperienceDropEvent event) {",
                "public static void giveStoneToBlacksmith");
        assertTrue(experience.contains("if (event.getDroppedExperience() == 0)"));
        assertTrue(experience.contains("EntityPlayer player = event.getAttackingPlayer();"));
        assertTrue(experience.contains("player.getActivePotionEffect(ModPotions.DRAGON_GREED)"));
        assertTrue(experience.contains("event.getDroppedExperience() + greed.getAmplifier() + 1"),
                "Normal Dragon's Greed adds one XP and gold-fed amplifier one adds two XP");
    }

    @Test
    void thrownSwordKeepsLifetimeNoGravityDamageAndFriendlyFireContract() throws IOException {
        String sword = readUtf8(JAVA_ROOT.resolve("entity").resolve("EntityThrownSword.java"));
        assertTrue(sword.contains("extends EntityThrowable implements IThrowableEntity"));
        assertTrue(sword.contains("private static final int MAX_DURATION = 100;"));
        assertTrue(sword.contains("this.duration++;"));
        assertTrue(sword.contains("this.duration > MAX_DURATION"));
        assertTrue(Pattern.compile("protected float getGravityVelocity\\(\\) \\{\\s*return 0\\.0F;",
                Pattern.DOTALL).matcher(sword).find());
        assertEquals(3, countOccurrences(sword, "this.setNoGravity(true);"),
                "Both constructors and NBT restore must enforce no gravity");
        assertTrue(sword.contains("DamageSource.causeThrownDamage(this, this.getThrower()), 6.0F"));
        assertTrue(sword.contains("compound.setInteger(\"duration\", this.duration);"));
        assertTrue(sword.contains("this.duration = compound.getInteger(\"duration\");"));
        assertTrue(sword.contains("if (target == owner)"));
        assertTrue(sword.contains("target instanceof IBlacksmithUpgradable"));
        assertTrue(sword.contains("getFamiliarOwner() == owner"));
        assertTrue(sword.contains("target instanceof EntityDevilFamiliar"));
        assertTrue(sword.contains("target instanceof EntityBlacksmithFamiliar"));
        assertTrue(sword.contains("ItemFamiliarRing.TAG_RITUAL_FAMILIAR_PARROT"));
        assertTrue(sword.contains("public void setThrower(Entity entity)"));
        assertTrue(sword.contains("this.thrower = entity instanceof EntityLivingBase"),
                "Forge spawn packets need IThrowableEntity to restore the owner on the client");

        String impact = section(sword,
                "protected void onImpact(RayTraceResult result) {",
                "public void writeEntityToNBT");
        int friendly = impact.indexOf("this.isFriendly(result.entityHit)");
        int keepFlying = impact.indexOf("return;", friendly);
        int damage = impact.indexOf("DamageSource.causeThrownDamage");
        int remove = impact.indexOf("this.setDead();");
        assertTrue(friendly >= 0 && keepFlying > friendly && damage > keepFlying && remove > damage,
                "Friendly impacts must return before damage and removal so a sword can leave its Dragon mount");

        Bootstrap.register();
        EntityLivingBase owner = new TestDragonFamiliar();
        TestThrownSword friendlyProjectile = new TestThrownSword();
        friendlyProjectile.setThrower(owner);
        assertSame(owner, friendlyProjectile.getThrower(),
                "The Forge client spawn hook must be able to restore the projectile owner");
        friendlyProjectile.impact(owner);
        assertFalse(friendlyProjectile.isDead,
                "A sword spawned inside its owner or same-owner passenger must keep flying");

        EntityThrownSword projectile = new EntityThrownSword(null);
        NBTTagCompound input = new NBTTagCompound();
        projectile.writeEntityToNBT(input);
        input.setInteger("duration", 73);
        projectile.readEntityFromNBT(input);
        NBTTagCompound output = new NBTTagCompound();
        projectile.writeEntityToNBT(output);
        assertEquals(73, output.getInteger("duration"));
    }

    @Test
    void familiarRingCapturesUnboundOrOwnedDragonAndStripsPassengerNbt() throws IOException {
        String ring = readUtf8(JAVA_ROOT.resolve("item").resolve("ItemFamiliarRing.java"));
        String supported = section(ring,
                "private static boolean isSupportedStoredFamiliar(@Nullable Entity entity) {",
                "private static boolean canCaptureFamiliar");
        assertTrue(supported.contains("entity instanceof EntityDragonFamiliar"));

        // Capture is interface-driven, so Dragon inherits the shared IFamiliar ownership gate.
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

        String captureFlow = section(ring,
                "public EnumActionResult tryCapture(ItemStack stack, EntityPlayer player, Entity target, EnumHand hand) {",
                "public void addInformation");
        int dismount = captureFlow.indexOf("familiar.dismountRidingEntity();");
        int removePassengers = captureFlow.indexOf("familiar.removePassengers();");
        int serialize = captureFlow.indexOf("storeFamiliar(stack, familiar)");
        int removeEntity = captureFlow.indexOf("familiar.setDead();");
        assertTrue(dismount >= 0 && removePassengers > dismount && serialize > removePassengers
                        && removeEntity > serialize,
                "Dragon and Greedy must dismount before serialization so Passengers cannot be duplicated on release");
    }

    @Test
    void dragonModelRenderersAndTexturesMatchTheSelectedUpstreamSnapshot()
            throws IOException, NoSuchAlgorithmException {
        String model = readUtf8(JAVA_ROOT.resolve("client").resolve("model")
                .resolve("ModelDragonFamiliar.java"));
        assertTrue(model.contains("this.textureWidth = 64;"));
        assertTrue(model.contains("this.textureHeight = 32;"));
        assertEquals(42, countOccurrences(model, "public final ModelRenderer"));
        assertTrue(model.contains("public void setLivingAnimations("));
        assertTrue(model.contains("dragon.getAttackProgress(partialTick)"));
        assertTrue(model.contains("dragon.getFlyingTimer(partialTick)"));
        assertTrue(model.contains("dragon.getWingspan(partialTick)"));
        assertTrue(model.contains("dragon.getPetTimer() + partialTick"));
        assertTrue(model.contains("dragon.isPartying()"));
        assertTrue(model.contains("this.fez1.showModel = dragon.hasFez();"));
        assertTrue(model.contains("this.leftEar.showModel = hasEars;"));
        assertTrue(model.contains("this.leftHorn1.showModel = !hasEars;"));
        assertTrue(model.contains("this.leftArm1.showModel = hasArms;"));

        String renderer = readUtf8(JAVA_ROOT.resolve("client").resolve("render")
                .resolve("RenderDragonFamiliar.java"));
        assertTrue(renderer.contains("extends RenderLiving<EntityDragonFamiliar>"));
        assertTrue(renderer.contains("super(renderManagerIn, new ModelDragonFamiliar(), 0.3F);"));
        assertTrue(renderer.contains("textures/entity/dragon_familiar.png"));
        assertTrue(renderer.contains("new StickLayer(this)"));
        assertTrue(renderer.contains("new SwordLayer(this)"));
        assertTrue(renderer.contains("if (!entity.hasStick())"));
        assertTrue(renderer.contains("if (!entity.hasSword())"));
        assertTrue(renderer.contains("ItemCameraTransforms.TransformType.GROUND"));
        assertTrue(renderer.contains("I18n.format(\"dialog.occultism.dragon.pet\")"));

        String projectileRenderer = readUtf8(JAVA_ROOT.resolve("client").resolve("render")
                .resolve("RenderThrownSword.java"));
        assertTrue(projectileRenderer.contains("extends RenderSnowball<EntityThrownSword>"));
        assertTrue(projectileRenderer.contains("Items.IRON_SWORD"));
        assertTrue(projectileRenderer.contains("ageInTicks * 20.0F"));

        Path texture = ASSET_ROOT.resolve("textures").resolve("entity").resolve("dragon_familiar.png");
        BufferedImage textureImage = ImageIO.read(texture.toFile());
        assertNotNull(textureImage);
        assertEquals(64, textureImage.getWidth());
        assertEquals(32, textureImage.getHeight());
        assertEquals(DRAGON_TEXTURE_SHA256, sha256(texture));

        Path icon = ASSET_ROOT.resolve("textures").resolve("gui").resolve("book")
                .resolve("familiar_dragon.png");
        BufferedImage iconImage = ImageIO.read(icon.toFile());
        assertNotNull(iconImage);
        assertEquals(64, iconImage.getWidth());
        assertEquals(64, iconImage.getHeight());
        assertEquals(DRAGON_BOOK_ICON_SHA256, sha256(icon));

        JsonObject dummy = readJsonObject(ASSET_ROOT.resolve("models").resolve("item")
                .resolve("ritual_dummy").resolve("familiar_dragon.json"));
        assertEquals("item/generated", getString(dummy, "parent"));
        assertEquals("occultism:item/ritual_dummy_possess",
                getString(dummy.getAsJsonObject("textures"), "layer0"));
    }

    @Test
    void dictionaryExposesDragonEntityRitualDescriptionAndFallback() throws IOException {
        JsonObject root = readJsonObject(ASSET_ROOT.resolve("dictionary").resolve("modonomicon_index.json"));
        JsonObject category = findObject(root.getAsJsonArray("categories"), "id", "familiar_rituals");
        assertNotNull(category);
        JsonObject entry = findObject(category.getAsJsonArray("entries"), "id", "familiar_dragon");
        assertNotNull(entry, "Missing familiar_dragon dictionary entry");
        assertEquals("occultism:textures/gui/book/familiar_dragon.png", getString(entry, "icon"));

        JsonArray pages = entry.getAsJsonArray("pages");
        assertEquals(3, pages.size());
        Set<String> pageIds = new HashSet<>();
        for (JsonElement pageElement : pages) {
            pageIds.add(getString(pageElement.getAsJsonObject(), "id"));
        }
        assertEquals(new HashSet<>(Arrays.asList("entity", "ritual", "description")), pageIds);
        JsonObject entityPage = findObject(pages, "id", "entity");
        assertEquals("modonomicon:entity", getString(entityPage, "type"));
        assertEquals("occultism:dragon_familiar", getString(entityPage, "entity_id"));
        JsonObject ritualPage = findObject(pages, "id", "ritual");
        assertEquals("occultism:ritual_recipe", getString(ritualPage, "type"));
        assertEquals("occultism:ritual/familiar_dragon", getString(ritualPage, "recipe_id_1"));
        JsonObject description = findObject(pages, "id", "description");
        assertEquals("modonomicon:text", getString(description, "type"));
        assertEquals("book.occultism.dictionary_of_spirits.familiar_rituals.familiar_dragon.description.text",
                getString(description, "text"));

        String generator = readUtf8(TOOLS_ROOT.resolve("generate_modonomicon_index.py"));
        assertTrue(Pattern.compile("\"familiar_dragon\"\\s*:\\s*\\(\"entity\",\\s*"
                + "\"ritual\",\\s*\"description\"\\)").matcher(generator).find());
        String gui = readUtf8(JAVA_ROOT.resolve("client").resolve("gui")
                .resolve("GuiDictionaryOfSpirits.java"));
        assertTrue(gui.contains("entry(\"familiar_dragon\")"));
    }

    @Test
    void bilingualTextDescribesXpSticksRideUpgradeAndCuriosBoundary() throws IOException {
        String english = readUtf8(ASSET_ROOT.resolve("lang").resolve("en_us.lang"));
        String chinese = readUtf8(ASSET_ROOT.resolve("lang").resolve("zh_cn.lang"));
        assertEquals("Dragon Familiar", langValue(english, "entity.occultism.dragon_familiar.name"));
        assertEquals("\u9f99\u4f7f\u9b54", langValue(chinese, "entity.occultism.dragon_familiar.name"));
        assertEquals("Dragon's Greed", langValue(english, "effect.occultism.dragon_greed"));
        assertEquals("\u9f99\u4e4b\u8d2a\u6b32", langValue(chinese, "effect.occultism.dragon_greed"));
        assertFalse(langValue(english, "dialog.occultism.dragon.pet").isEmpty());
        assertFalse(langValue(chinese, "dialog.occultism.dragon.pet").isEmpty());

        String englishTooltip = langValue(english, "item.occultism.ritual_dummy.familiar_dragon.tooltip");
        assertTrue(englishTooltip.contains("experience"));
        assertTrue(englishTooltip.contains("sticks"));
        assertTrue(englishTooltip.contains("Greedy Familiar"));
        assertTrue(englishTooltip.contains("thrown swords"));
        assertTrue(englishTooltip.contains("native one-slot ring effects"));
        String chineseTooltip = langValue(chinese, "item.occultism.ritual_dummy.familiar_dragon.tooltip");
        assertTrue(chineseTooltip.contains("\u7ecf\u9a8c"));
        assertTrue(chineseTooltip.contains("\u6728\u68cd"));
        assertTrue(chineseTooltip.contains("\u8d2a\u5a6a\u4f7f\u9b54"));
        assertTrue(chineseTooltip.contains("\u63b7\u5251"));
        assertTrue(chineseTooltip.contains("\u539f\u751f\u5355\u69fd\u6212\u6307\u6548\u679c"));
    }

    private static void assertAttribute(EntityLivingBase entity, IAttribute attribute, double expected) {
        assertNotNull(entity.getEntityAttribute(attribute), "Missing entity attribute: " + attribute.getName());
        assertEquals(expected, entity.getEntityAttribute(attribute).getBaseValue(), 0.000001D,
                "Unexpected base value for " + attribute.getName());
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

    private static final class TestDragonFamiliar extends EntityDragonFamiliar {

        private TestDragonFamiliar() {
            super(null);
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
