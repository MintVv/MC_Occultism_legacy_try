package com.occultism.occultism.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
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
import com.occultism.occultism.entity.EntityChimeraFamiliar;
import com.occultism.occultism.entity.IBlacksmithUpgradable;
import com.occultism.occultism.entity.IFamiliar;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Bootstrap;
import net.minecraft.nbt.NBTTagCompound;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ChimeraPortContractTest {

    private static final Path ROOT = Paths.get("src", "main");
    private static final Path JAVA_ROOT = ROOT.resolve("java").resolve("com").resolve("occultism")
            .resolve("occultism");
    private static final Path ASSET_ROOT = ROOT.resolve("resources").resolve("assets").resolve("occultism");
    private static final String TEXTURE_SHA256 =
            "a362b81171ea53c64c5f926ecad9c7aca132f4dc803510bc3dd6d39b3da3318b";
    private static final String BOOK_ICON_SHA256 =
            "1002f914e188b4b6c54f7476c8b95d6f721a00b033c21ba7e74daf17fd814f09";

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void ritualPreservesTheNineIngredient1814Contract() throws IOException {
        JsonArray rituals = readJson(ASSET_ROOT.resolve("rituals").resolve("rituals.json")).getAsJsonArray();
        JsonObject actual = findObject(rituals, "id", "familiar_chimera");
        assertNotNull(actual);

        JsonObject expected = new JsonObject();
        expected.addProperty("id", "familiar_chimera");
        expected.addProperty("display_name", "Chimera Familiar");
        expected.addProperty("type", "occultism:ritual");
        expected.addProperty("ritual_type", "occultism:summon_tamed");
        expected.addProperty("activation_item", "occultism:book_of_binding_bound_djinni");
        expected.addProperty("duration_ticks", 1200);
        JsonObject sacrifice = new JsonObject();
        sacrifice.addProperty("display_name", "ritual.occultism.sacrifice.sheep");
        sacrifice.addProperty("tag", "c:sheep");
        expected.add("entity_to_sacrifice", sacrifice);
        expected.addProperty("entity_to_summon", "occultism:chimera_familiar");
        JsonArray ingredients = new JsonArray();
        for (String ingredient : Arrays.asList("#c:leathers", "#c:strings", "#c:feathers",
                "#minecraft:wool", "#c:bones", "minecraft:mutton", "minecraft:beef",
                "minecraft:porkchop", "minecraft:chicken")) {
            ingredients.add(ingredient);
        }
        expected.add("ingredients", ingredients);
        expected.addProperty("pentacle_id", "occultism:possess_djinni");
        JsonObject result = new JsonObject();
        result.addProperty("id", "occultism:soul_gem");
        expected.add("result", result);
        JsonObject dummy = new JsonObject();
        dummy.addProperty("id", "occultism:ritual_dummy/familiar_chimera");
        expected.add("ritual_dummy", dummy);
        assertEquals(expected, actual);
    }

    @Test
    void entityConstructsScalesAttributesAndRoundTripsFiveVariants() throws Exception {
        EntityChimeraFamiliar chimera = new TestChimeraFamiliar();
        assertEquals(0.85F, chimera.width, 0.000001F);
        assertEquals(1.05F, chimera.height, 0.000001F);
        assertEquals(20.0D, chimera.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getBaseValue(), 0.000001D);
        assertEquals(4.0D, chimera.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue(), 0.000001D);
        assertEquals(0.25D, chimera.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getBaseValue(),
                0.000001D);
        assertEquals(0.0D, chimera.getEntityAttribute(SharedMonsterAttributes.ARMOR).getBaseValue(), 0.000001D);
        assertTrue(chimera instanceof IFamiliar);
        assertFalse(chimera instanceof IBlacksmithUpgradable);
        assertSame(chimera, chimera.getFamiliarEntity());
        assertNull(chimera.getFamiliarOwner());

        invoke(chimera, "setFlaps", true);
        invoke(chimera, "setRing", true);
        invoke(chimera, "setBeard", true);
        invoke(chimera, "setHat", true);
        invoke(chimera, "setGoat", true);
        chimera.setSize((byte) 100);
        assertEquals(2.0F, chimera.getScale(), 0.000001F);
        assertEquals(1.7F, chimera.width, 0.000001F);
        assertEquals(2.1F, chimera.height, 0.000001F);
        assertEquals(7.0D,
                chimera.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue(), 0.000001D);
        assertEquals(0.33D,
                chimera.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue(), 0.000001D);

        NBTTagCompound saved = new NBTTagCompound();
        chimera.writeEntityToNBT(saved);
        assertEquals(31, saved.getByte("variants") & 0xff);
        assertEquals(100, saved.getByte("getSize"));
        assertTrue(saved.getBoolean("hasFlaps") && saved.getBoolean("hasRing"));
        assertTrue(saved.getBoolean("hasBeard") && saved.getBoolean("hasHat") && saved.getBoolean("hasGoat"));

        EntityChimeraFamiliar loaded = new TestChimeraFamiliar();
        loaded.readEntityFromNBT(saved);
        assertTrue(loaded.hasFlaps() && loaded.hasRing() && loaded.hasBeard());
        assertTrue(loaded.hasHat() && loaded.hasGoat());
        assertEquals(100, loaded.getSize());
        assertEquals(7.0D,
                loaded.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue(), 0.000001D);
    }

    @Test
    void feedingDetachAndRideInteractionOrderKeepsTheSurvivalChainReachable() throws IOException {
        String source = readUtf8(JAVA_ROOT.resolve("entity").resolve("EntityChimeraFamiliar.java"));
        String interaction = section(source, "public boolean processInteract", "public void travel");
        int detach = interaction.indexOf("this.hasGoat() && stack.getItem() == Items.GOLDEN_APPLE");
        int meat = interaction.indexOf("stack.getItem() instanceof ItemFood");
        int ride = interaction.indexOf("!this.isSitting() && !this.isBeingRidden()");
        int sit = interaction.indexOf("hand == EnumHand.MAIN_HAND && stack.isEmpty()");
        assertTrue(detach >= 0 && meat > detach && ride > meat && sit > ride);
        assertTrue(interaction.contains("player == this.getOwner()"));
        assertTrue(interaction.contains("((ItemFood) stack.getItem()).isWolfsFavoriteMeat()"));
        assertTrue(interaction.contains("this.setSize((byte) (this.getSize() + nutrition));"));
        assertTrue(interaction.contains("this.heal(4.0F);"));
        assertTrue(interaction.contains("new EntityGoatFamiliar(this.world, this.hasRing(), this.hasBeard(),"));
        assertTrue(interaction.contains("this.getSize(), this.getOwner())"));
        assertTrue(interaction.contains("this.setGoat(false);"));
        assertTrue(interaction.contains("this.getSize() > RIDING_SIZE"));
        assertTrue(source.contains("this.rand.nextDouble() < SHRINK_CHANCE"));
        assertTrue(source.contains("this.setSize((byte) (this.getSize() - 1));"));
    }

    @Test
    void ridingUsesThe112JumpPacketContractWithoutClientOnlyInterfaceMethods() throws IOException {
        String source = readUtf8(JAVA_ROOT.resolve("entity").resolve("EntityChimeraFamiliar.java"));
        String travel = section(source, "public void travel", "public boolean canBeSteered");
        assertTrue(travel.contains("this.rotationYaw = rider.rotationYaw;"));
        assertTrue(travel.contains("strafe = rider.moveStrafing * 0.5F;"));
        assertTrue(travel.contains("forward *= 0.25F;"));
        assertTrue(travel.contains("this.jumpTimer = JUMP_COOLDOWN;"));
        assertTrue(travel.contains("this.motionX += -MathHelper.sin(yaw) * 0.7D;"));
        assertTrue(travel.contains("getAttributeValue() * 0.8F"));

        String jump = section(source, "public void setJumpPower", "public void handleStopJump");
        assertFalse(jump.contains("@SideOnly"),
                "IJumpingMount#setJumpPower is invoked by the dedicated server packet handler");
        assertTrue(source.contains("return super.getJumpUpwardsMotion() * 1.35F;"));
        assertTrue(source.contains("super.fall(distance - 3.0F, damageMultiplier);"));
        assertTrue(source.contains("return super.getMountedYOffset() * 0.6D;"));
        assertTrue(source.contains("this.getSize() <= RIDING_SIZE && this.isBeingRidden()"));
    }

    @Test
    void meleeRandomizesOnlyAttachedHeadsAndAppliesTheThreeFixedEffects() throws IOException {
        String source = readUtf8(JAVA_ROOT.resolve("entity").resolve("EntityChimeraFamiliar.java"));
        String attackers = section(source, "private byte[] possibleAttackers", "private static final class");
        assertTrue(attackers.contains("LION_ATTACKER, GOAT_ATTACKER, SNAKE_ATTACKER"));
        assertTrue(attackers.contains("LION_ATTACKER, SNAKE_ATTACKER"));

        String melee = source.substring(source.indexOf("private static final class ChimeraMeleeAttackGoal"));
        assertTrue(melee.contains("this.attackTick = 20;"));
        assertTrue(melee.contains("DamageSource.causePlayerDamage((EntityPlayer) this.chimera.getOwner())"));
        assertTrue(melee.contains("enemy.setFire(4);"));
        assertTrue(melee.contains("enemy.knockBack(this.chimera, 2.0F"));
        assertTrue(melee.contains("new PotionEffect(MobEffects.POISON, 20 * 10)"));
        assertTrue(melee.contains("return 9.0D;"));
        assertFalse(source.contains("new ChimeraRangedAttackGoal"),
                "The fixed upstream marks its unregistered ranged goal as buggy");
    }

    @Test
    void modelRendererResourcesDictionaryRingAndTextCloseTheFeature()
            throws IOException, NoSuchAlgorithmException {
        String model = readUtf8(JAVA_ROOT.resolve("client").resolve("model")
                .resolve("ModelChimeraFamiliar.java"));
        assertEquals(48, countOccurrences(model, "public final ModelRenderer"));
        assertTrue(model.contains("this.goatNeck.showModel = chimera.hasGoat();"));
        assertTrue(model.contains("this.goatRing.showModel = chimera.hasRing();"));
        assertTrue(model.contains("this.goatBeard.showModel = chimera.hasBeard();"));
        assertTrue(model.contains("chimera.getAttackProgress(partialTick)"));

        String renderer = readUtf8(JAVA_ROOT.resolve("client").resolve("render")
                .resolve("RenderChimeraFamiliar.java"));
        assertTrue(renderer.contains("GlStateManager.scale(scale, scale, scale);"));
        assertTrue(renderer.contains("GlStateManager.translate(0.0F, -0.23F, 0.0F);"));
        assertTrue(renderer.contains("textures/entity/chimera_familiar.png"));

        Path texture = ASSET_ROOT.resolve("textures").resolve("entity").resolve("chimera_familiar.png");
        BufferedImage entityImage = ImageIO.read(texture.toFile());
        assertEquals(64, entityImage.getWidth());
        assertEquals(32, entityImage.getHeight());
        assertEquals(TEXTURE_SHA256, sha256(texture));
        Path icon = ASSET_ROOT.resolve("textures").resolve("gui").resolve("book")
                .resolve("familiar_chimera.png");
        assertEquals(BOOK_ICON_SHA256, sha256(icon));

        JsonObject index = readJson(ASSET_ROOT.resolve("dictionary").resolve("modonomicon_index.json"))
                .getAsJsonObject();
        JsonObject category = findObject(index.getAsJsonArray("categories"), "id", "familiar_rituals");
        JsonObject entry = findObject(category.getAsJsonArray("entries"), "id", "familiar_chimera");
        Set<String> pages = new HashSet<>();
        for (JsonElement element : entry.getAsJsonArray("pages")) {
            pages.add(string(element.getAsJsonObject(), "id"));
        }
        assertEquals(new HashSet<>(Arrays.asList("entity", "ritual", "description", "description2")), pages);

        String ring = readUtf8(JAVA_ROOT.resolve("item").resolve("ItemFamiliarRing.java"));
        assertTrue(section(ring, "private static boolean isSupportedStoredFamiliar(@Nullable Entity entity) {",
                "private static boolean canCaptureFamiliar").contains("entity instanceof EntityChimeraFamiliar"),
                "The familiar ring must be able to restore a stored Chimera");
        // Capture is interface-driven, so Chimera inherits the shared IFamiliar ownership gate.
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
        String english = readUtf8(ASSET_ROOT.resolve("lang").resolve("en_us.lang"));
        String chinese = readUtf8(ASSET_ROOT.resolve("lang").resolve("zh_cn.lang"));
        assertTrue(english.contains("Chimera itself cannot be upgraded"));
        assertTrue(english.contains("the detached goat familiar gets a warning bell"));
        assertTrue(chinese.contains("奇美拉本身不可升级"));
        assertTrue(chinese.contains("分离出的山羊使魔会获得警铃"));
    }

    private static void invoke(EntityChimeraFamiliar entity, String name, boolean value) throws Exception {
        Method method = EntityChimeraFamiliar.class.getDeclaredMethod(name, boolean.class);
        method.setAccessible(true);
        method.invoke(entity, Boolean.valueOf(value));
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

    private static String sha256(Path path) throws IOException, NoSuchAlgorithmException {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path));
        StringBuilder result = new StringBuilder();
        for (byte value : digest) {
            result.append(String.format("%02x", value & 0xff));
        }
        return result.toString();
    }

    private static JsonElement readJson(Path path) throws IOException {
        return new JsonParser().parse(readUtf8(path));
    }

    private static String readUtf8(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    private static final class TestChimeraFamiliar extends EntityChimeraFamiliar {

        private TestChimeraFamiliar() {
            super(null);
        }

        @Override
        public int getGrowingAge() {
            return 0;
        }
    }
}
