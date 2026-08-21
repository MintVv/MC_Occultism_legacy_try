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
import java.util.UUID;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.occultism.occultism.entity.EntityGoatFamiliar;
import com.occultism.occultism.entity.EntityShubNiggurathFamiliar;
import com.occultism.occultism.entity.EntityShubNiggurathSpawn;
import com.occultism.occultism.entity.IBlacksmithUpgradable;
import com.occultism.occultism.entity.IFamiliar;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.init.Bootstrap;
import net.minecraft.nbt.NBTTagCompound;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class GoatShubSpawnPortContractTest {

    private static final Path ROOT = Paths.get("src", "main");
    private static final Path JAVA_ROOT = ROOT.resolve("java").resolve("com").resolve("occultism")
            .resolve("occultism");
    private static final Path ASSET_ROOT = ROOT.resolve("resources").resolve("assets").resolve("occultism");
    private static final String GOAT_TEXTURE =
            "321f13b1df0caa34db5e014413868c044629488a69a7e66b7c95d9490db1cb57";
    private static final String SHUB_TEXTURE =
            "0c446e96dd2fbb8adabf5446292f4c1ad99da88ec661d2fce6632e481ec96c21";
    private static final String SPAWN_TEXTURE =
            "71a89685e0a17b977813c246f07f7560882bc3218f77e43f1962cb4a26756699";
    private static final String SPAWN_BLINK_TEXTURE =
            "35e3b4908701f4919218480aa0ed0f404404d68dd524ad0dc2aa29937b830bc9";
    private static final String SHUB_ICON =
            "169027b64fe6bfd20a22a82efc36cb16aa73ba16f3c9466651f2d8b1a6f53470";

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void registrationsAppendGoatShubAndSpawnWithoutDirectRituals() throws IOException {
        String entities = readUtf8(JAVA_ROOT.resolve("registry").resolve("ModEntities.java"));
        int fairy = entities.indexOf("\"fairy_familiar\"");
        int goat = entities.indexOf("\"goat_familiar\"");
        int shub = entities.indexOf("\"shub_niggurath_familiar\"");
        int spawn = entities.indexOf("\"shub_niggurath_spawn\"");
        int beholder = entities.indexOf("\"beholder_familiar\"");
        assertTrue(fairy >= 0 && goat > fairy && shub > goat && spawn > shub && beholder > spawn);
        assertEquals(1, countOccurrences(entities, "new ResourceLocation(Tags.MOD_ID, \"goat_familiar\")"));
        assertEquals(1, countOccurrences(entities,
                "new ResourceLocation(Tags.MOD_ID, \"shub_niggurath_familiar\")"));
        assertEquals(1, countOccurrences(entities,
                "new ResourceLocation(Tags.MOD_ID, \"shub_niggurath_spawn\")"));

        JsonArray rituals = readJson(ASSET_ROOT.resolve("rituals").resolve("rituals.json")).getAsJsonArray();
        assertNull(findObject(rituals, "id", "familiar_goat"));
        assertNull(findObject(rituals, "id", "familiar_shub_niggurath"));
    }

    @Test
    void goatConstructsScalesAndRoundTripsFiveVariantsAndUpgrade() throws Exception {
        EntityGoatFamiliar goat = new TestGoatFamiliar();
        assertEquals(0.7F, goat.width, 0.000001F);
        assertEquals(0.8F, goat.height, 0.000001F);
        assertFamiliarAttributes(goat);
        assertTrue(goat instanceof IBlacksmithUpgradable);
        assertSame(goat, goat.getFamiliarEntity());
        assertNull(goat.getFamiliarOwner());

        invoke(goat, "setRing", true);
        invoke(goat, "setBeard", true);
        invoke(goat, "setBlack", true);
        invoke(goat, "setRedEyes", true);
        invoke(goat, "setEvilHorns", true);
        goat.setSize((byte) 100);
        goat.blacksmithUpgrade();
        assertEquals(2.0F, goat.getScale(), 0.000001F);
        assertEquals(1.4F, goat.width, 0.000001F);
        assertEquals(1.6F, goat.height, 0.000001F);
        assertTrue(goat.hasBlacksmithUpgrade());

        NBTTagCompound saved = new NBTTagCompound();
        goat.writeEntityToNBT(saved);
        assertEquals(31, saved.getByte("variants") & 0xff);
        assertEquals(100, saved.getByte("getSize"));
        assertTrue(saved.getBoolean("hasBlacksmithUpgrade"));

        EntityGoatFamiliar loaded = new TestGoatFamiliar();
        loaded.readEntityFromNBT(saved);
        assertTrue(loaded.hasRing() && loaded.hasBeard() && loaded.isBlack());
        assertTrue(loaded.hasRedEyes() && loaded.hasEvilHorns());
        assertEquals(100, loaded.getSize());
        assertEquals(1.4F, loaded.width, 0.000001F);
        assertTrue(loaded.hasBlacksmithUpgrade());
        assertFalse(loaded.canBlacksmithUpgrade());
    }

    @Test
    void goatTransformationAllowsAnyOrderAndCopiesOnlyTheFixedStateSubset() throws IOException {
        String goat = readUtf8(JAVA_ROOT.resolve("entity").resolve("EntityGoatFamiliar.java"));
        String interaction = section(goat, "public boolean processInteract", "private void transform");
        assertTrue(interaction.contains("this.isInTransformationBiome(player)"
                + " || this.isInTransformationBiome(this)"));
        assertTrue(interaction.contains("matchesOre(stack, \"dyeBlack\")"));
        assertTrue(interaction.contains("stack.getItem() == Items.ENDER_EYE"));
        assertTrue(interaction.contains("stack.getItem() == Items.FLINT"));
        assertTrue(interaction.contains("if (this.shouldTransform())"));
        assertFalse(interaction.contains("else if (this.isBlack())"),
                "The fixed code does not impose a material sequence");
        assertTrue(goat.contains("BiomeDictionary.Type.FOREST"));
        assertTrue(goat.contains("new EntityShubNiggurathFamiliar(this.world, this)"));

        String shub = readUtf8(JAVA_ROOT.resolve("entity").resolve("EntityShubNiggurathFamiliar.java"));
        String constructor = section(shub,
                "public EntityShubNiggurathFamiliar(World worldIn, EntityGoatFamiliar goat)",
                "protected void entityInit");
        assertTrue(constructor.contains("this.setRing(goat.hasRing());"));
        assertTrue(constructor.contains("this.setBeard(goat.hasBeard());"));
        assertTrue(constructor.contains("this.setOwnerId(owner.getUniqueID());"));
        assertFalse(constructor.contains("hasBlacksmithUpgrade"));
        assertFalse(constructor.contains("goat.getSize"));
        assertFalse(constructor.contains("getCustomName"));
    }

    @Test
    void shubConstructsUpgradesAndKeepsItsSpawnTimerTransient() throws Exception {
        EntityShubNiggurathFamiliar shub = new TestShubFamiliar();
        assertEquals(0.7F, shub.width, 0.000001F);
        assertEquals(0.8F, shub.height, 0.000001F);
        assertFamiliarAttributes(shub);
        assertTrue(shub instanceof IBlacksmithUpgradable);
        invoke(shub, "setRing", true);
        invoke(shub, "setBeard", true);
        shub.blacksmithUpgrade();

        NBTTagCompound saved = new NBTTagCompound();
        shub.writeEntityToNBT(saved);
        assertEquals(3, saved.getByte("variants") & 0xff);
        assertTrue(saved.getBoolean("hasBlacksmithUpgrade"));
        assertFalse(saved.hasKey("spawnTimer"));

        EntityShubNiggurathFamiliar loaded = new TestShubFamiliar();
        loaded.readEntityFromNBT(saved);
        assertTrue(loaded.hasRing() && loaded.hasBeard());
        assertTrue(loaded.hasBlacksmithUpgrade());
        assertFalse(loaded.canBlacksmithUpgrade());
    }

    @Test
    void spawnConstructsAsANonFamiliarAndPersistsCreatorAndLifetime() {
        EntityShubNiggurathSpawn spawn = new EntityShubNiggurathSpawn(null);
        assertEquals(0.6F, spawn.width, 0.000001F);
        assertEquals(0.6F, spawn.height, 0.000001F);
        assertAttribute(spawn, SharedMonsterAttributes.MAX_HEALTH, 6.0D);
        assertAttribute(spawn, SharedMonsterAttributes.MOVEMENT_SPEED, 0.3D);
        assertAttribute(spawn, SharedMonsterAttributes.ATTACK_DAMAGE, 6.0D);
        assertFalse(spawn instanceof IFamiliar);
        assertFalse(spawn instanceof IBlacksmithUpgradable);

        UUID creator = UUID.fromString("12345678-1234-5678-9abc-def012345678");
        NBTTagCompound input = new NBTTagCompound();
        input.setInteger("lifeTicks", 123);
        input.setUniqueId("creatorId", creator);
        spawn.readEntityFromNBT(input);
        NBTTagCompound output = new NBTTagCompound();
        spawn.writeEntityToNBT(output);
        assertEquals(123, output.getInteger("lifeTicks"));
        assertEquals(creator, output.getUniqueId("creatorId"));
    }

    @Test
    void timingDistanceDuplicateDamageAndOrdinaryDeathFollowFixedSource() throws IOException {
        String shub = readUtf8(JAVA_ROOT.resolve("entity").resolve("EntityShubNiggurathFamiliar.java"));
        assertTrue(shub.contains("private static final int MAX_SPAWN_TIMER = 20 * 10;"));
        assertTrue(shub.contains("if (this.spawnTimer-- < 0)"),
                "Post-decrement makes the fixed interval 202 ticks");
        assertFalse(section(shub, "public void writeEntityToNBT", "public void readEntityFromNBT")
                .contains("spawnTimer"));
        assertTrue(shub.contains("this.isRiding() && source.isUnblockable()"));
        assertTrue(shub.contains("getEntityBoundingBox().grow(5.0D)"));
        assertTrue(shub.contains("this.rider.getDistanceSq(this.mount) < 5.0D"));

        String spawn = readUtf8(JAVA_ROOT.resolve("entity").resolve("EntityShubNiggurathSpawn.java"));
        assertTrue(spawn.contains("private static final int LIFE_TICKS_MAX = 20 * 20;"));
        assertTrue(spawn.contains("if (this.lifeTicks-- < 0)"),
                "Post-decrement makes the fixed lifetime 402 ticks");
        assertTrue(spawn.contains("this.lifeTicks % 10 == 0"));
        assertTrue(spawn.contains("target.getDistanceSq(familiar) < range"),
                "The fixed snapshot compares squared distance to the raw range");
        assertEquals(2, countOccurrences(spawn, "addIfClose(enemies,"),
                "Revenge and last-attacked references intentionally remain duplicate-capable");
        String death = section(spawn, "public void onDeath", "public void handleStatusUpdate");
        assertFalse(death.contains("this.explode()"), "Ordinary damage death must not cause area damage");
        String movement = spawn.substring(spawn.indexOf("private static final class MoveToTargetGoal"));
        assertTrue(movement.contains("if (this.timer-- < 0)"));
        assertTrue(movement.contains("this.timer = 20;"),
                "The fixed post-decrement navigation interval is 22 ticks");
    }

    @Test
    void modelsRenderersTexturesDictionaryAndRingCloseTheClientAndStorageChain()
            throws IOException, NoSuchAlgorithmException {
        assertModelParts("ModelGoatFamiliar.java", 36);
        assertModelParts("ModelShubNiggurathFamiliar.java", 51);
        assertModelParts("ModelShubNiggurathSpawn.java", 25);
        String renderers = readUtf8(JAVA_ROOT.resolve("client").resolve("render")
                .resolve("ModEntityRenderers.java"));
        assertTrue(renderers.contains("return new RenderGoatFamiliar(manager);"));
        assertTrue(renderers.contains("return new RenderShubNiggurathFamiliar(manager);"));
        assertTrue(renderers.contains("return new RenderShubNiggurathSpawn(manager);"));

        assertPng("goat_familiar.png", 64, 32, GOAT_TEXTURE);
        assertPng("shub_niggurath_familiar.png", 64, 32, SHUB_TEXTURE);
        assertPng("shub_niggurath_spawn.png", 32, 32, SPAWN_TEXTURE);
        assertPng("shub_niggurath_spawn_blinking.png", 32, 32, SPAWN_BLINK_TEXTURE);
        Path icon = ASSET_ROOT.resolve("textures").resolve("gui").resolve("book")
                .resolve("familiar_shub_niggurath.png");
        assertEquals(SHUB_ICON, sha256(icon));

        JsonObject index = readJson(ASSET_ROOT.resolve("dictionary").resolve("modonomicon_index.json"))
                .getAsJsonObject();
        JsonObject category = findObject(index.getAsJsonArray("categories"), "id", "familiar_rituals");
        JsonObject entry = findObject(category.getAsJsonArray("entries"), "id", "familiar_shub_niggurath");
        assertNotNull(entry);
        Set<String> pages = new HashSet<>();
        for (JsonElement element : entry.getAsJsonArray("pages")) {
            pages.add(string(element.getAsJsonObject(), "id"));
        }
        assertEquals(new HashSet<>(Arrays.asList("entity", "ritual", "description")), pages);

        String ring = readUtf8(JAVA_ROOT.resolve("item").resolve("ItemFamiliarRing.java"));
        String supported = section(ring, "private static boolean isSupportedStoredFamiliar",
                "private static boolean canCaptureFamiliar");
        assertTrue(supported.contains("entity instanceof EntityGoatFamiliar"));
        assertTrue(supported.contains("entity instanceof EntityShubNiggurathFamiliar"));
        assertFalse(supported.contains("EntityShubNiggurathSpawn"));
        // Capture is interface-driven, so Goat and Shub-Niggurath inherit the shared IFamiliar
        // ownership gate. Contract source: upstream FamiliarRingItem.interactLivingEntity, which
        // captures when getFamiliarOwner() == player || getFamiliarOwner() == null (unbound
        // familiars may be claimed).
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
    }

    private static void assertFamiliarAttributes(EntityLivingBase entity) {
        assertAttribute(entity, SharedMonsterAttributes.MAX_HEALTH, 30.0D);
        assertAttribute(entity, SharedMonsterAttributes.ARMOR, 15.0D);
        assertAttribute(entity, SharedMonsterAttributes.ARMOR_TOUGHNESS, 10.0D);
        assertAttribute(entity, SharedMonsterAttributes.MOVEMENT_SPEED, 0.3D);
        assertAttribute(entity, SharedMonsterAttributes.ATTACK_DAMAGE, 6.0D);
    }

    private static void assertAttribute(EntityLivingBase entity, IAttribute attribute, double expected) {
        assertNotNull(entity.getEntityAttribute(attribute));
        assertEquals(expected, entity.getEntityAttribute(attribute).getBaseValue(), 0.000001D);
    }

    private static void invoke(Object target, String name, boolean value) throws Exception {
        Class<?> type = target.getClass();
        Method method = null;
        while (type != null && method == null) {
            try {
                method = type.getDeclaredMethod(name, boolean.class);
            } catch (NoSuchMethodException exception) {
                type = type.getSuperclass();
            }
        }
        assertNotNull(method, "Missing method: " + name);
        method.setAccessible(true);
        method.invoke(target, Boolean.valueOf(value));
    }

    private static void assertModelParts(String file, int expected) throws IOException {
        String source = readUtf8(JAVA_ROOT.resolve("client").resolve("model").resolve(file));
        assertEquals(expected, countOccurrences(source, "public final ModelRenderer"));
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

    private static final class TestGoatFamiliar extends EntityGoatFamiliar {

        private TestGoatFamiliar() {
            super(null);
        }

        @Override
        public int getGrowingAge() {
            return 0;
        }
    }

    private static final class TestShubFamiliar extends EntityShubNiggurathFamiliar {

        private TestShubFamiliar() {
            super(null);
        }

        @Override
        public int getGrowingAge() {
            return 0;
        }
    }
}
