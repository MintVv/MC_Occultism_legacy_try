package com.occultism.occultism.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

class CthulhuPortContractTest {

    private static final Path ASSET_ROOT = Paths.get("src", "main", "resources", "assets", "occultism");
    private static final Path JAVA_ROOT = Paths.get("src", "main", "java", "com", "occultism", "occultism");
    private static final String CTHULHU_TEXTURE_SHA256 =
            "aa5d196e58805639dfd2e42804127396eb055aa13a93882a9ba2823bacd16abb";

    @Test
    void cthulhuRitualMatchesThe1814Contract() throws IOException {
        JsonArray rituals = readJsonElement(ASSET_ROOT.resolve("rituals").resolve("rituals.json")).getAsJsonArray();
        JsonObject actual = null;
        int matches = 0;
        for (JsonElement element : rituals) {
            JsonObject ritual = element.getAsJsonObject();
            if ("familiar_cthulhu".equals(getString(ritual, "id"))) {
                actual = ritual;
                matches++;
            }
        }
        assertEquals(1, matches, "Exactly one familiar_cthulhu ritual must be registered");

        JsonObject expected = new JsonObject();
        expected.addProperty("id", "familiar_cthulhu");
        expected.addProperty("display_name", "Cthulhu Familiar");
        expected.addProperty("type", "occultism:ritual");
        expected.addProperty("ritual_type", "occultism:summon_tamed");
        expected.addProperty("activation_item", "occultism:book_of_binding_bound_djinni");
        expected.addProperty("duration_ticks", 1200);

        JsonObject sacrifice = new JsonObject();
        sacrifice.addProperty("display_name", "ritual.occultism.sacrifice.squid");
        sacrifice.addProperty("tag", "c:squids");
        expected.add("entity_to_sacrifice", sacrifice);
        expected.addProperty("entity_to_summon", "occultism:cthulhu_familiar");

        JsonArray ingredients = new JsonArray();
        for (int index = 0; index < 8; index++) {
            ingredients.add("#minecraft:fishes");
        }
        expected.add("ingredients", ingredients);
        expected.addProperty("pentacle_id", "occultism:possess_djinni");

        JsonObject result = new JsonObject();
        result.addProperty("id", "occultism:soul_gem");
        expected.add("result", result);

        JsonObject ritualDummy = new JsonObject();
        ritualDummy.addProperty("id", "occultism:ritual_dummy/familiar_cthulhu");
        expected.add("ritual_dummy", ritualDummy);

        assertEquals(expected, actual, "The 1.12 Cthulhu ritual must retain the selected 1.81.4 contract");

        String ritualManager = readUtf8(JAVA_ROOT.resolve("ritual").resolve("RitualRecipeManager.java"));
        assertTrue(Pattern.compile("if \\(\\\"fishes\\\"\\.equals\\(path\\)\\) \\{\\s*"
                + "return \\\"foodRawFish\\\";", Pattern.DOTALL).matcher(ritualManager).find(),
                "minecraft:fishes must map to the 1.12 raw-fish OreDictionary entry");
        String oreDictionary = readUtf8(JAVA_ROOT.resolve("registry").resolve("ModOreDictionary.java"));
        assertTrue(oreDictionary.contains("OreDictionary.registerOre(\"foodRawFish\", "
                + "new ItemStack(Items.FISH, 1, OreDictionary.WILDCARD_VALUE));"),
                "foodRawFish must accept every 1.12 fish metadata variant");
    }

    @Test
    void cthulhuRegistrationsCloseTheServerAndClientPaths() throws IOException {
        String entities = readUtf8(JAVA_ROOT.resolve("registry").resolve("ModEntities.java"));
        Pattern entityRegistration = Pattern.compile(
                "EntityRegistry\\.registerModEntity\\(new ResourceLocation\\(Tags\\.MOD_ID, \\\"cthulhu_familiar\\\"\\),\\s*"
                        + "EntityCthulhuFamiliar\\.class, \\\"cthulhu_familiar\\\", nextEntityId\\+\\+, "
                        + "Occultism\\.INSTANCE, 64, 3, true\\);",
                Pattern.DOTALL);
        Matcher entityMatcher = entityRegistration.matcher(entities);
        assertTrue(entityMatcher.find(), "cthulhu_familiar must use the normal tracked mod-entity registration");
        String registrationsAfterCthulhu = entities.substring(entityMatcher.end());
        Pattern blacksmithRegistration = Pattern.compile(
                "EntityRegistry\\.registerModEntity\\(new ResourceLocation\\(Tags\\.MOD_ID, \"blacksmith_familiar\"\\),\\s*"
                        + "EntityBlacksmithFamiliar\\.class, \"blacksmith_familiar\", nextEntityId\\+\\+, "
                        + "Occultism\\.INSTANCE, 64, 3, true\\);",
                Pattern.DOTALL);
        Matcher blacksmithMatcher = blacksmithRegistration.matcher(registrationsAfterCthulhu);
        assertTrue(blacksmithMatcher.find(),
                "The new Blacksmith registration must be appended after Cthulhu without moving earlier IDs");
        assertEquals(1, countOccurrences(entities,
                "new ResourceLocation(Tags.MOD_ID, \"cthulhu_familiar\")"),
                "Later append-only entity migrations must not duplicate or move the Cthulhu registration");

        String renderers = readUtf8(JAVA_ROOT.resolve("client").resolve("render").resolve("ModEntityRenderers.java"));
        assertTrue(renderers.contains("RenderingRegistry.registerEntityRenderingHandler(EntityCthulhuFamiliar.class"),
                "The Cthulhu entity must have a client rendering registration");
        assertTrue(renderers.contains("return new RenderCthulhuFamiliar(manager);"),
                "The Cthulhu registration must construct its dedicated renderer");

        String items = readUtf8(JAVA_ROOT.resolve("registry").resolve("ModItems.java"));
        assertTrue(items.contains("public static final Item RITUAL_DUMMY_FAMILIAR_CTHULHU"),
                "The Cthulhu ritual dummy must retain a named registry constant");
        assertTrue(items.contains("item(\"ritual_dummy/familiar_cthulhu\", \"ritual_dummy.familiar_cthulhu\", 1)"),
                "The ritual dummy constant must resolve the ID used by ritual data");
        assertEquals(2, countOccurrences(items, "RITUAL_DUMMY_FAMILIAR_CTHULHU"),
                "The Cthulhu ritual dummy must be declared once and included in ITEMS once");
    }

    @Test
    void cthulhuCoreKeepsTheSelected1814StateTransitions() throws IOException {
        String entity = readUtf8(JAVA_ROOT.resolve("entity").resolve("EntityCthulhuFamiliar.java"));

        assertTrue(entity.contains("new CthulhuSitGoal(this)"),
                "Cthulhu must use the upstream-style sitting rule instead of vanilla tameable combat rules");
        assertFalse(entity.contains("new EntityAISit(this)"),
                "Vanilla EntityAISit adds owner-combat and on-ground conditions absent from 1.81.4");
        String sitGoal = section(entity, "private static class CthulhuSitGoal", "private static class CthulhuMoveHelper");
        assertTrue(Pattern.compile("return !this\\.cthulhu\\.isInWater\\(\\) "
                + "&& this\\.cthulhu\\.getOwner\\(\\) != null\\s*"
                + "&& this\\.cthulhu\\.isSitting\\(\\);", Pattern.DOTALL).matcher(sitGoal).find(),
                "The sitting goal must only require dry ground, a loaded owner, and the sitting flag");

        String livingUpdate = section(entity, "public void onLivingUpdate() {", "public boolean processInteract");
        assertTrue(livingUpdate.indexOf("this.updateNavigationForMedium();")
                        < livingUpdate.indexOf("super.onLivingUpdate();"),
                "Navigation must switch after the current water state is known but before living AI runs");

        String followGoal = section(entity, "private static class FollowOwnerWaterGoal",
                "private static class GiveFlowerGoal");
        assertTrue(followGoal.contains("getBlockState(new BlockPos(this.owner))"),
                "Water-to-land teleport must inspect the block at the owner's feet");
        assertTrue(followGoal.contains(".getMaterial() != Material.WATER"),
                "The owner block must use the 1.12 water material check");
        assertTrue(Pattern.compile("if \\(--this\\.cooldown >= 0\\) \\{\\s*return;\\s*\\}\\s*"
                + "this\\.cooldown = 10;\\s*if \\(this\\.cthulhu\\.getLeashed\\(\\) "
                + "\\|\\| this\\.cthulhu\\.isRiding\\(\\)\\)", Pattern.DOTALL).matcher(followGoal).find(),
                "Follow updates must retain the 1.81.4 eleven-tick cadence and cooldown reset order");

        String updateLight = section(entity, "private void updateLight() {", "private void clearLightBlocks()");
        assertTrue(Pattern.compile("if \\(!this\\.hasBlacksmithUpgrade\\(\\) "
                + "\\|\\| !this\\.isEntityAlive\\(\\)\\) \\{\\s*return;\\s*\\}", Pattern.DOTALL)
                .matcher(updateLight).find(),
                "An unupgraded Cthulhu must not remove lighted_air owned by another familiar");
    }

    @Test
    void lightedAirIsAHiddenRegistered12Block() throws IOException {
        String blocks = readUtf8(JAVA_ROOT.resolve("registry").resolve("ModBlocks.java"));
        assertTrue(Pattern.compile("public static final Block LIGHTED_AIR\\s*=\\s*"
                + "block\\(\\\"lighted_air\\\", new BlockLightedAir\\(\\),").matcher(blocks).find(),
                "lighted_air must be created through the normal block registry helper");

        String blockArray = section(blocks, "public static final Block[] BLOCKS = {", "};");
        assertTrue(blockArray.trim().endsWith("LIGHTED_AIR"),
                "lighted_air must be appended so existing numeric block IDs do not move");
        String skipItemBlock = section(blocks, "private static boolean skipItemBlock(Block block) {", "}");
        assertTrue(skipItemBlock.contains("block == LIGHTED_AIR"),
                "lighted_air must not receive an ItemBlock");

        JsonObject blockstate = readJsonObject(ASSET_ROOT.resolve("blockstates").resolve("lighted_air.json"));
        JsonObject variants = blockstate.getAsJsonObject("variants");
        assertNotNull(variants, "lighted_air blockstate must define variants");
        assertEquals(1, variants.size(), "lighted_air only needs the 1.12 normal variant");
        JsonObject normal = variants.getAsJsonObject("normal");
        assertNotNull(normal, "The 1.12 blockstate variant must be named normal");
        assertEquals("occultism:lighted_air", normal.get("model").getAsString());

        JsonObject model = readJsonObject(ASSET_ROOT.resolve("models").resolve("block").resolve("lighted_air.json"));
        assertEquals(0, model.size(), "The invisible lighted_air block model must remain empty");
        assertFalse(Files.exists(ASSET_ROOT.resolve("models").resolve("item").resolve("lighted_air.json")),
                "A hidden lighted_air block must not expose an item model");
    }

    @Test
    void cthulhuModelAndTextureMatchTheSelectedUpstreamSnapshot()
            throws IOException, NoSuchAlgorithmException {
        assertTrue(Files.isRegularFile(JAVA_ROOT.resolve("client").resolve("model")
                .resolve("ModelCthulhuFamiliar.java")), "Missing dedicated Cthulhu model");

        Path rendererPath = JAVA_ROOT.resolve("client").resolve("render").resolve("RenderCthulhuFamiliar.java");
        String renderer = readUtf8(rendererPath);
        assertTrue(renderer.contains("new ModelCthulhuFamiliar()"),
                "The renderer must use the dedicated Cthulhu model");
        assertTrue(renderer.contains("textures/entity/cthulhu_familiar.png"),
                "The renderer must resolve the migrated upstream texture");

        Path texture = ASSET_ROOT.resolve("textures").resolve("entity").resolve("cthulhu_familiar.png");
        BufferedImage image = ImageIO.read(texture.toFile());
        assertNotNull(image, "cthulhu_familiar.png must be a readable PNG");
        assertEquals(64, image.getWidth(), "Unexpected Cthulhu texture width");
        assertEquals(32, image.getHeight(), "Unexpected Cthulhu texture height");
        assertEquals(CTHULHU_TEXTURE_SHA256, sha256(texture),
                "The Cthulhu texture must match Occultism 1.81.4 commit 84b8ad4");

        JsonObject dummyModel = readJsonObject(ASSET_ROOT.resolve("models").resolve("item")
                .resolve("ritual_dummy").resolve("familiar_cthulhu.json"));
        assertEquals("item/generated", dummyModel.get("parent").getAsString());
        assertEquals("occultism:item/ritual_dummy_possess",
                dummyModel.getAsJsonObject("textures").get("layer0").getAsString());
    }

    @Test
    void dictionaryEntryResolvesTheCthulhuEntityRitualAndDescription() throws IOException {
        JsonObject root = readJsonObject(ASSET_ROOT.resolve("dictionary").resolve("modonomicon_index.json"));
        JsonObject category = findObject(root.getAsJsonArray("categories"), "id", "familiar_rituals");
        assertNotNull(category, "Missing familiar_rituals dictionary category");
        JsonObject entry = findObject(category.getAsJsonArray("entries"), "id", "familiar_cthulhu");
        assertNotNull(entry, "Missing familiar_cthulhu dictionary entry");

        assertEquals("occultism:textures/gui/book/familiar_cthulhu.png", entry.get("icon").getAsString(),
                "The dictionary icon must resolve the migrated Cthulhu book image");
        assertTrue(Files.isRegularFile(ASSET_ROOT.resolve("textures").resolve("gui").resolve("book")
                .resolve("familiar_cthulhu.png")), "The dictionary Cthulhu icon is missing");

        JsonArray pages = entry.getAsJsonArray("pages");
        assertEquals(3, pages.size(), "The Cthulhu entry must expose entity, ritual, and description pages");
        Set<String> pageIds = new HashSet<>();
        for (JsonElement pageElement : pages) {
            pageIds.add(getString(pageElement.getAsJsonObject(), "id"));
        }
        assertEquals(new HashSet<>(Arrays.asList("entity", "ritual", "description")), pageIds);

        JsonObject entityPage = findObject(pages, "id", "entity");
        assertEquals("modonomicon:entity", getString(entityPage, "type"));
        assertEquals("occultism:cthulhu_familiar", getString(entityPage, "entity_id"));

        JsonObject ritualPage = findObject(pages, "id", "ritual");
        assertEquals("occultism:ritual_recipe", getString(ritualPage, "type"));
        assertEquals("occultism:ritual/familiar_cthulhu", getString(ritualPage, "recipe_id_1"));

        JsonObject descriptionPage = findObject(pages, "id", "description");
        assertEquals("modonomicon:text", getString(descriptionPage, "type"));
        assertEquals("book.occultism.dictionary_of_spirits.familiar_rituals.familiar_cthulhu.description.text",
                getString(descriptionPage, "text"));

        String gui = readUtf8(JAVA_ROOT.resolve("client").resolve("gui").resolve("GuiDictionaryOfSpirits.java"));
        assertTrue(gui.contains("entry(\"familiar_cthulhu\")"),
                "The no-resource fallback dictionary must also expose Cthulhu");
    }

    @Test
    void familiarRingCapturesUnboundOrOwnedCthulhu() throws IOException {
        String ring = readUtf8(JAVA_ROOT.resolve("item").resolve("ItemFamiliarRing.java"));
        String supported = section(ring,
                "private static boolean isSupportedStoredFamiliar(@Nullable Entity entity) {",
                "private static boolean canCaptureFamiliar");
        assertTrue(supported.contains("entity instanceof EntityCthulhuFamiliar"),
                "The familiar ring must be able to restore a stored Cthulhu");

        // Capture is interface-driven, so Cthulhu inherits the shared IFamiliar ownership gate.
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

    private static String getString(JsonObject object, String key) {
        return object != null && object.has(key) ? object.get(key).getAsString() : "";
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
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return new JsonParser().parse(reader);
        }
    }

    private static String readUtf8(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
