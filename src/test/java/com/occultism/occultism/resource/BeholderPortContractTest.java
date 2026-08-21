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
import com.occultism.occultism.entity.EntityBeholderFamiliar;
import com.occultism.occultism.entity.IBlacksmithUpgradable;
import com.occultism.occultism.entity.IFamiliar;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.init.Bootstrap;
import net.minecraft.nbt.NBTTagCompound;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class BeholderPortContractTest {

    private static final Path ROOT = Paths.get("src", "main");
    private static final Path JAVA_ROOT = ROOT.resolve("java").resolve("com").resolve("occultism")
            .resolve("occultism");
    private static final Path ASSET_ROOT = ROOT.resolve("resources").resolve("assets").resolve("occultism");
    private static final String TEXTURE_SHA256 =
            "4633ea7bb0d6fae0e73769350fa2fc2e1c28cb71e8f177a9f3a3e8a2400b13e5";
    private static final String SLEEP_TEXTURE_SHA256 =
            "bbc0ee90cff092e29d397059a65fb2d38ecce0c002bc9dc21677d9ce0156e7cc";
    private static final String BOOK_ICON_SHA256 =
            "4b575f7189971707968377e94eb657d2784174c5a55259b4f7d89eddf813fce0";

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void ritualAndRegistrationsPreserveTheAppendOnly1814Contract() throws IOException {
        JsonArray rituals = readJson(ASSET_ROOT.resolve("rituals").resolve("rituals.json")).getAsJsonArray();
        JsonObject actual = findObject(rituals, "id", "familiar_beholder");
        assertNotNull(actual);

        JsonObject expected = new JsonObject();
        expected.addProperty("id", "familiar_beholder");
        expected.addProperty("display_name", "Beholder Familiar");
        expected.addProperty("type", "occultism:ritual");
        expected.addProperty("ritual_type", "occultism:summon_tamed");
        expected.addProperty("activation_item", "occultism:book_of_binding_bound_djinni");
        expected.addProperty("duration_ticks", 600);
        JsonObject sacrifice = new JsonObject();
        sacrifice.addProperty("display_name", "ritual.occultism.sacrifice.spiders");
        sacrifice.addProperty("tag", "c:spiders");
        expected.add("entity_to_sacrifice", sacrifice);
        expected.addProperty("entity_to_summon", "occultism:beholder_familiar");
        JsonArray ingredients = new JsonArray();
        for (int index = 0; index < 4; index++) {
            ingredients.add("minecraft:spider_eye");
        }
        for (int index = 0; index < 4; index++) {
            ingredients.add("#c:dusts/glowstone");
        }
        expected.add("ingredients", ingredients);
        expected.addProperty("pentacle_id", "occultism:possess_djinni");
        JsonObject result = new JsonObject();
        result.addProperty("id", "occultism:soul_gem");
        expected.add("result", result);
        JsonObject dummy = new JsonObject();
        dummy.addProperty("id", "occultism:ritual_dummy/familiar_beholder");
        expected.add("ritual_dummy", dummy);
        assertEquals(expected, actual);

        String sacrificeSource = readUtf8(JAVA_ROOT.resolve("ritual").resolve("RitualSacrifice.java"));
        assertTrue(sacrificeSource.contains("case \"c:spiders\":"));
        assertTrue(sacrificeSource.contains("return entity instanceof EntitySpider;"),
                "The Forge spider tag must include CaveSpider through EntitySpider inheritance");

        String entities = readUtf8(JAVA_ROOT.resolve("registry").resolve("ModEntities.java"));
        int spawn = entities.indexOf("\"shub_niggurath_spawn\"");
        int beholder = entities.indexOf("\"beholder_familiar\"");
        int chimera = entities.indexOf("\"chimera_familiar\"");
        assertTrue(spawn >= 0 && beholder > spawn && chimera > beholder);

        String items = readUtf8(JAVA_ROOT.resolve("registry").resolve("ModItems.java"));
        String itemArray = section(items, "public static final Item[] ITEMS = {", "};");
        assertTrue(itemArray.indexOf("RITUAL_DUMMY_FAMILIAR_BEHOLDER")
                > itemArray.indexOf("RITUAL_DUMMY_FAMILIAR_FAIRY"));
        assertTrue(itemArray.indexOf("RITUAL_DUMMY_FAMILIAR_CHIMERA")
                > itemArray.indexOf("RITUAL_DUMMY_FAMILIAR_BEHOLDER"));
    }

    @Test
    void entityConstructsWithAttributesAndRoundTripsAppearanceAndUpgradeNbt() throws Exception {
        EntityBeholderFamiliar beholder = new TestBeholderFamiliar();
        assertEquals(1.6F, beholder.width, 0.000001F);
        assertEquals(1.5F, beholder.height, 0.000001F);
        assertAttribute(beholder, SharedMonsterAttributes.MAX_HEALTH, 30.0D);
        assertAttribute(beholder, SharedMonsterAttributes.ARMOR, 15.0D);
        assertAttribute(beholder, SharedMonsterAttributes.ARMOR_TOUGHNESS, 10.0D);
        assertAttribute(beholder, SharedMonsterAttributes.MOVEMENT_SPEED, 0.3D);
        assertAttribute(beholder, SharedMonsterAttributes.ATTACK_DAMAGE, 6.0D);
        assertTrue(beholder instanceof IFamiliar);
        assertTrue(beholder instanceof IBlacksmithUpgradable);
        assertSame(beholder, beholder.getFamiliarEntity());
        assertNull(beholder.getFamiliarOwner());
        assertFalse(beholder.hasBlacksmithUpgrade());

        invoke(beholder, "setBeard", Boolean.TRUE);
        invoke(beholder, "setSpikes", Boolean.TRUE);
        invoke(beholder, "setTongue", Boolean.TRUE);
        invoke(beholder, "setRed", Float.valueOf(0.2F));
        invoke(beholder, "setGreen", Float.valueOf(0.4F));
        invoke(beholder, "setBlue", Float.valueOf(0.8F));
        beholder.blacksmithUpgrade();

        NBTTagCompound saved = new NBTTagCompound();
        beholder.writeEntityToNBT(saved);
        assertEquals(7, saved.getByte("variants") & 0xff);
        assertTrue(saved.getBoolean("hasBlacksmithUpgrade"));
        assertEquals(0.2F, saved.getFloat("red"), 0.000001F);
        assertEquals(0.4F, saved.getFloat("green"), 0.000001F);
        assertEquals(0.8F, saved.getFloat("blue"), 0.000001F);

        EntityBeholderFamiliar loaded = new TestBeholderFamiliar();
        loaded.readEntityFromNBT(saved);
        assertTrue(loaded.hasBeard() && loaded.hasSpikes() && loaded.hasTongue());
        assertTrue(loaded.hasBlacksmithUpgrade());
        assertFalse(loaded.canBlacksmithUpgrade());
        assertEquals(0.2F, loaded.getRed(), 0.000001F);
        assertEquals(0.4F, loaded.getGreen(), 0.000001F);
        assertEquals(0.8F, loaded.getBlue(), 0.000001F);
    }

    @Test
    void rayVolleyIsReachableAndRetainsTargetDamageAndTimingSemantics() throws IOException {
        String source = readUtf8(JAVA_ROOT.resolve("entity").resolve("EntityBeholderFamiliar.java"));
        String ray = section(source, "private static final class RayGoal", "private static final class EatGoal");
        assertTrue(ray.contains("boolean cooldownReady = this.cooldown-- < 0;"));
        assertTrue(ray.contains("this.targetIds == null && cooldownReady && !enemies.isEmpty()"),
                "Pending volleys must not be overwritten by the shorter upstream cooldown");
        assertTrue(ray.contains("this.attackTimer = 23;"));
        assertTrue(ray.contains("this.beholder.getDistanceSq(target) < OWNER_ENEMY_RANGE_SQ"));
        assertTrue(ray.contains("owner.getRevengeTarget()"));
        assertTrue(ray.contains("owner.getLastAttackedEntity()"));
        assertTrue(ray.contains("float damage = 9.0F;"));
        assertTrue(ray.contains("damage *= strength.getAmplifier() + 2;"));
        assertTrue(ray.contains("DamageSource.causePlayerDamage((EntityPlayer) owner)"));
        assertFalse(ray.contains("new HashSet"), "Duplicate owner targets remain duplicate in fixed 1.81.4");
    }

    @Test
    void eatingGlowAndBlindnessUpgradeKeepTheirWorldBehaviorBoundaries() throws IOException {
        String source = readUtf8(JAVA_ROOT.resolve("entity").resolve("EntityBeholderFamiliar.java"));
        String eat = section(source, "private static final class EatGoal", "private final class Eye");
        assertTrue(eat.contains("private static final int MAX_COOLDOWN = 20 * 30;"));
        assertTrue(eat.contains("getEntityBoundingBox().grow(3.0D)"));
        assertTrue(eat.contains("EntityShubNiggurathSpawn.class"));
        assertTrue(eat.contains("Entity::isEntityAlive"));
        assertTrue(eat.contains("food.setDead();"));
        assertTrue(eat.contains("MobEffects.STRENGTH"));
        assertTrue(eat.contains("MobEffects.SPEED"));
        assertTrue(source.contains("private static final int EAT_EFFECT_DURATION = 20 * 60 * 10;"));

        String glow = section(source, "private void tickGlow", "private void updateClientAnimation");
        assertTrue(glow.contains("getEntityBoundingBox().grow(OWNER_EFFECT_RANGE)"));
        assertTrue(glow.contains("new PotionEffect(MobEffects.GLOWING, 20 * 60, 0, false, false)"));
        String immunity = section(source, "public static void onPotionApplicable", "private static final class RayGoal");
        assertTrue(immunity.contains("effect.getPotion() != MobEffects.BLINDNESS"));
        assertTrue(immunity.contains("FamiliarUtil.hasFamiliar(owner, EntityBeholderFamiliar.class"));
        assertTrue(immunity.contains("beholder.hasBlacksmithUpgrade()"));
        assertTrue(immunity.contains("event.setResult(Result.DENY);"));
    }

    @Test
    void modelRendererAndTexturesMatchTheFixedSnapshot() throws IOException, NoSuchAlgorithmException {
        String model = readUtf8(JAVA_ROOT.resolve("client").resolve("model")
                .resolve("ModelBeholderFamiliar.java"));
        assertEquals(39, countOccurrences(model, "public final ModelRenderer"));
        assertTrue(model.contains("this.glasses.showModel = beholder.hasBlacksmithUpgrade();"));
        assertTrue(model.contains("beholder.getBigEyePos(partialTicks)"));
        assertTrue(model.contains("beholder.getEyeRot(partialTicks"));

        String renderer = readUtf8(JAVA_ROOT.resolve("client").resolve("render")
                .resolve("RenderBeholderFamiliar.java"));
        assertTrue(renderer.contains("y + entity.getAnimationHeight(partialTicks)"));
        assertTrue(renderer.contains("textures/entity/beholder_familiar_sleep.png"));
        assertTrue(renderer.contains("eatTimer < 5.0F / 6.0F"));

        assertPng("beholder_familiar.png", 64, 64, TEXTURE_SHA256);
        assertPng("beholder_familiar_sleep.png", 64, 64, SLEEP_TEXTURE_SHA256);
        Path icon = ASSET_ROOT.resolve("textures").resolve("gui").resolve("book")
                .resolve("familiar_beholder.png");
        BufferedImage image = ImageIO.read(icon.toFile());
        assertEquals(64, image.getWidth());
        assertEquals(64, image.getHeight());
        assertEquals(BOOK_ICON_SHA256, sha256(icon));
    }

    @Test
    void dictionaryRingAndBilingualTextExposeTheCompleteFeature() throws IOException {
        JsonObject index = readJson(ASSET_ROOT.resolve("dictionary").resolve("modonomicon_index.json"))
                .getAsJsonObject();
        JsonObject category = findObject(index.getAsJsonArray("categories"), "id", "familiar_rituals");
        JsonObject entry = findObject(category.getAsJsonArray("entries"), "id", "familiar_beholder");
        assertNotNull(entry);
        assertEquals("occultism:textures/gui/book/familiar_beholder.png", string(entry, "icon"));
        Set<String> pages = new HashSet<>();
        for (JsonElement element : entry.getAsJsonArray("pages")) {
            pages.add(string(element.getAsJsonObject(), "id"));
        }
        assertEquals(new HashSet<>(Arrays.asList("entity", "ritual", "description")), pages);

        String ring = readUtf8(JAVA_ROOT.resolve("item").resolve("ItemFamiliarRing.java"));
        assertTrue(section(ring, "private static boolean isSupportedStoredFamiliar(@Nullable Entity entity) {",
                "private static boolean canCaptureFamiliar").contains("entity instanceof EntityBeholderFamiliar"),
                "The familiar ring must be able to restore a stored Beholder");
        // Capture is interface-driven, so Beholder inherits the shared IFamiliar ownership gate.
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
        assertTrue(english.contains("entity.occultism.beholder_familiar.name=Beholder Familiar"));
        assertTrue(english.contains("A Blacksmith upgrade grants its owner blindness immunity."));
        assertFalse(english.contains("immunity extends to darkness"));
        assertTrue(chinese.contains("entity.occultism.beholder_familiar.name=眼魔使魔"));
        assertTrue(chinese.contains("铁匠升级会使主人免疫失明"));
    }

    private static void assertPng(String name, int width, int height, String expectedHash)
            throws IOException, NoSuchAlgorithmException {
        Path path = ASSET_ROOT.resolve("textures").resolve("entity").resolve(name);
        BufferedImage image = ImageIO.read(path.toFile());
        assertNotNull(image);
        assertEquals(width, image.getWidth());
        assertEquals(height, image.getHeight());
        assertEquals(expectedHash, sha256(path));
    }

    private static void assertAttribute(EntityBeholderFamiliar entity, IAttribute attribute, double expected) {
        assertNotNull(entity.getEntityAttribute(attribute));
        assertEquals(expected, entity.getEntityAttribute(attribute).getBaseValue(), 0.000001D);
    }

    private static void invoke(EntityBeholderFamiliar entity, String name, Object value) throws Exception {
        Class<?> type = value instanceof Boolean ? boolean.class : float.class;
        Method method = EntityBeholderFamiliar.class.getDeclaredMethod(name, type);
        method.setAccessible(true);
        method.invoke(entity, value);
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
        JsonElement element = object == null ? null : object.get(key);
        return element == null || element.isJsonNull() ? null : element.getAsString();
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

    private static final class TestBeholderFamiliar extends EntityBeholderFamiliar {

        private TestBeholderFamiliar() {
            super(null);
        }

        @Override
        public int getGrowingAge() {
            return 0;
        }
    }
}
