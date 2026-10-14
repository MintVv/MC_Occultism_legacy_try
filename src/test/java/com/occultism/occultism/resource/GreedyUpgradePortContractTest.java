package com.occultism.occultism.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

import com.google.common.base.Optional;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.occultism.occultism.entity.EntityGreedyFamiliar;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;

class GreedyUpgradePortContractTest {

    private static final Path ROOT = Paths.get("src", "main");
    private static final Path JAVA_ROOT = ROOT.resolve("java").resolve("com").resolve("occultism")
            .resolve("occultism");
    private static final Path ASSET_ROOT = ROOT.resolve("resources").resolve("assets").resolve("occultism");
    private static final Path TOOLS_ROOT = Paths.get("tools");
    private static final String GREEDY_TEXTURE_SHA256 =
            "f6ac783462d71b634cb8775133befe9b45c4a0f632405ddede74aedf66a13aab";
    private static final String GREEDY_CHEST_TEXTURE_SHA256 =
            "1aee0d70d33fcba893a375ab7adb85edc0cbc6b2c66e8a44c51256ebb3ae459f";
    private static final String GREEDY_CHRISTMAS_TEXTURE_SHA256 =
            "364aafa87e639672eb491b2c9bcad2070d9add99cd7ce5d93633d5543316b46b";
    private static final String GREEDY_BOOK_ICON_SHA256 =
            "1229dc5e950cdaffc4b91d661c198aa4863576c8cec5f7516bd50052d909e102";

    @Test
    void greedyAcceptsExactlyOneBlacksmithUpgrade() {
        Bootstrap.register();

        EntityGreedyFamiliar greedy = new TestGreedyFamiliar();
        assertEquals(0.5F, greedy.width, 0.000001F);
        assertEquals(0.9F, greedy.height, 0.000001F);
        assertAttribute(greedy, SharedMonsterAttributes.MAX_HEALTH, 30.0D);
        assertAttribute(greedy, SharedMonsterAttributes.ARMOR, 15.0D);
        assertAttribute(greedy, SharedMonsterAttributes.ARMOR_TOUGHNESS, 10.0D);
        assertAttribute(greedy, SharedMonsterAttributes.MOVEMENT_SPEED, 0.3D);
        assertAttribute(greedy, SharedMonsterAttributes.ATTACK_DAMAGE, 6.0D);
        assertFalse(greedy.hasBlacksmithUpgrade());
        assertTrue(greedy.canBlacksmithUpgrade());
        assertSame(greedy, greedy.getFamiliarEntity());
        assertNull(greedy.getFamiliarOwner());

        greedy.blacksmithUpgrade();
        assertTrue(greedy.hasBlacksmithUpgrade());
        assertFalse(greedy.canBlacksmithUpgrade());

        greedy.blacksmithUpgrade();
        assertTrue(greedy.hasBlacksmithUpgrade());
        assertFalse(greedy.canBlacksmithUpgrade());
    }

    @Test
    void upgradeAndOffhandSamplePersistWhileTargetRemainsRuntimeOnly() throws Exception {
        Bootstrap.register();

        EntityGreedyFamiliar greedy = new TestGreedyFamiliar();
        greedy.blacksmithUpgrade();
        ItemStack sample = new ItemStack(Blocks.STONE, 1, 3);
        greedy.setHeldItem(EnumHand.OFF_HAND, sample);

        Method setter = EntityGreedyFamiliar.class.getDeclaredMethod("setTargetBlock", Optional.class);
        setter.setAccessible(true);
        BlockPos target = new BlockPos(7, 11, -4);
        setter.invoke(greedy, Optional.of(target));
        assertEquals(Optional.of(target), greedy.getTargetBlock());

        NBTTagCompound compound = new NBTTagCompound();
        greedy.writeEntityToNBT(compound);
        assertTrue(compound.getBoolean("hasBlacksmithUpgrade"));
        assertTrue(compound.hasKey("HandItems", 9), "Vanilla equipment NBT must carry the offhand sample");
        assertFalse(compound.hasKey("GreedyTargetBlock"), "A live path target must not survive a reload");

        EntityGreedyFamiliar loaded = new TestGreedyFamiliar();
        loaded.readEntityFromNBT(compound);
        assertTrue(loaded.hasBlacksmithUpgrade());
        assertEquals(sample.getItem(), loaded.getHeldItemOffhand().getItem());
        assertEquals(1, loaded.getHeldItemOffhand().getCount());
        assertEquals(3, loaded.getHeldItemOffhand().getMetadata());
        assertFalse(loaded.getTargetBlock().isPresent());

        String entity = readUtf8(JAVA_ROOT.resolve("entity").resolve("EntityGreedyFamiliar.java"));
        assertTrue(entity.contains("implements IBlacksmithUpgradable"));
        assertTrue(entity.contains("DataSerializers.BOOLEAN"));
        assertTrue(entity.contains("DataSerializers.OPTIONAL_BLOCK_POS"));
        assertTrue(entity.contains("registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE)"),
                "EntityTameable does not provide ATTACK_DAMAGE on 1.12");
        assertTrue(entity.contains("this.tasks.addTask(7, new EntityAIFollow(this, 1.0D, 3.0F, 7.0F));"),
                "The 1.12 EntityAIFollow is the direct FollowMobGoal equivalent");
        assertTrue(entity.contains("this.setBlacksmithUpgrade(compound.getBoolean(\"hasBlacksmithUpgrade\"));"));
        assertTrue(entity.contains("compound.setBoolean(\"hasBlacksmithUpgrade\", this.hasBlacksmithUpgrade());"));
        assertTrue(entity.contains("this.dataManager.register(TARGET_BLOCK, Optional.absent());"));
    }

    @Test
    void blockSampleInteractionPreservesThe1814BranchOrderAndAnyPlayerSemantics() throws IOException {
        String entity = readUtf8(JAVA_ROOT.resolve("entity").resolve("EntityGreedyFamiliar.java"));
        String interaction = section(entity, "public boolean processInteract(EntityPlayer player, EnumHand hand) {",
                "public boolean hasBlacksmithUpgrade()");

        assertTrue(interaction.contains("ItemStack stack = player.getHeldItem(hand);"));
        int returnSample = interaction.indexOf("!this.getOffhandItem().isEmpty()");
        if (returnSample < 0) {
            returnSample = interaction.indexOf("!this.getHeldItemOffhand().isEmpty()");
        }
        int acceptSample = interaction.indexOf("stack.getItem() instanceof ItemBlock");
        int ownerGate = interaction.indexOf("isOwner(player)");
        assertTrue(returnSample >= 0 && acceptSample > returnSample,
                "An existing sample must be returned before accepting a replacement block");
        assertTrue(ownerGate < 0 || ownerGate > acceptSample,
                "The 1.81.4 sample branches intentionally allow any interacting player");

        assertTrue(interaction.contains("ItemHandlerHelper.giveItemToPlayer(player, this.getOffhandItem())")
                        || interaction.contains("ItemHandlerHelper.giveItemToPlayer(player, this.getHeldItemOffhand())"),
                "Returning a sample must drop any inventory remainder instead of deleting it");
        assertTrue(interaction.contains("this.setHeldItem(EnumHand.OFF_HAND, ItemStack.EMPTY);"));
        assertTrue(interaction.contains("this.setHeldItem(EnumHand.OFF_HAND, new ItemStack(stack.getItem()));"),
                "The sample intentionally discards count, metadata and NBT");
        assertTrue(interaction.contains("stack.shrink(1);"));
        assertTrue(interaction.indexOf("stack.shrink(1);") < ownerGate || ownerGate < 0,
                "The sample interaction must run before the port-specific mainhand return branch");
    }

    @Test
    void findBlockGoalUsesTheFullSearchVolumeExactBlockTypeAndTargetLifecycle() throws IOException {
        String entity = readUtf8(JAVA_ROOT.resolve("entity").resolve("EntityGreedyFamiliar.java"));
        assertTrue(entity.contains("this.tasks.addTask(2, new FindBlockGoal(this));"));
        assertFalse(entity.contains("extends EntityAIMoveToBlock"),
                "The 1.12 helper only searches two Y levels and always navigates above its destination");

        String goal = entity.substring(entity.indexOf("class FindBlockGoal"));
        assertTrue(goal.contains("extends EntityAIBase"));
        assertTrue(Pattern.compile("HORIZONTAL_RANGE\\s*=\\s*10").matcher(goal).find());
        assertTrue(Pattern.compile("VERTICAL_RANGE\\s*=\\s*5").matcher(goal).find());
        assertTrue(Pattern.compile("MIN_SEARCH_COOLDOWN\\s*=\\s*200").matcher(goal).find());
        assertTrue(Pattern.compile("SEARCH_COOLDOWN_VARIANCE\\s*=\\s*200").matcher(goal).find());
        assertTrue(Pattern.compile("MAX_TARGET_TICKS\\s*=\\s*1200").matcher(goal).find());
        assertTrue(Pattern.compile("MIN_STAY_TICKS\\s*=\\s*1200").matcher(goal).find());
        assertTrue(Pattern.compile("STAY_TICK_VARIANCE\\s*=\\s*1200").matcher(goal).find());
        assertTrue(Pattern.compile("PATH_RECALCULATION_INTERVAL\\s*=\\s*40").matcher(goal).find());
        assertTrue(Pattern.compile("ARRIVAL_DISTANCE_SQ\\s*=\\s*1\\.0D").matcher(goal).find());
        assertTrue(countOccurrences(goal, "HORIZONTAL_RANGE") >= 3,
                "The horizontal radius must bound both X and Z search axes");
        assertTrue(countOccurrences(goal, "VERTICAL_RANGE") >= 2,
                "The search must cover five blocks above and below the familiar");
        assertTrue(goal.contains("this.setMutexBits(5)"), "Block locating must own movement and jumping");
        assertTrue(goal.contains("getOffhandItem()") || goal.contains("getHeldItemOffhand()"));
        assertFalse(goal.contains("hasBlacksmithUpgrade()"),
                "Upstream gates the goal by the offhand sample, not by a second upgrade check");

        assertTrue(goal.contains("Block.getBlockFromItem("));
        assertTrue(goal.contains("world.getBlockState(pos).getBlock()"));
        assertFalse(goal.contains("getMetadata()"), "Matching is by Block identity, not item metadata");
        assertFalse(goal.contains("getBlockState(pos).equals"), "Matching ignores the placed block state");
        assertTrue(Pattern.compile("setTargetBlock\\(Optional\\.of\\(").matcher(goal).find());
        assertTrue(goal.contains("setTargetBlock(Optional.absent())"));
        assertTrue(goal.contains("tryMoveToXYZ"));
        assertTrue(goal.contains("world.isBlockLoaded(this.targetBlock)"),
                "An unloaded target chunk must end the goal before reading its block state");
        assertTrue(goal.contains("getDistanceSqToTarget() <= ARRIVAL_DISTANCE_SQ"));
        assertTrue(goal.contains("this.targetTicks--;"),
                "Arrival must count down a stay budget instead of consuming the 1200-tick pathfinding budget");
        assertTrue(goal.contains("this.targetTicks >= -this.maxStayTicks"));
        assertTrue(goal.contains("sampleChanged"),
                "Changing the sample must bypass the normal 200-399 tick search delay");
        assertTrue(goal.contains("scheduleNextSearch()"),
                "A failed or expired full-volume search must be throttled before retrying");
        assertFalse(goal.contains(".up()"), "The upstream goal navigates to the matched block itself");
        assertFalse(goal.contains("setBlockState"));
        assertFalse(goal.contains("destroyBlock"));
    }

    @Test
    void findItemGoalUsesInventorySimulationDemagnetizationAndTheSameMoveMutex() throws IOException {
        String entity = readUtf8(JAVA_ROOT.resolve("entity").resolve("EntityGreedyFamiliar.java"));
        assertTrue(entity.contains("this.tasks.addTask(4, new FindItemGoal(this));"));
        String goal = section(entity, "class FindItemGoal", "class FindBlockGoal");
        assertTrue(goal.contains("extends EntityAIBase"));
        assertTrue(goal.contains("private static final double RANGE = 12.0D;"));
        assertTrue(goal.contains("this.setMutexBits(1)"),
                "FindItem and priority-2 FindBlock must compete for the MOVE mutex");
        assertTrue(goal.contains("getFamiliarOwner()") || goal.contains("getOwner()"));
        assertTrue(Pattern.compile("new PlayerMainInvWrapper\\((owner|player)\\.inventory\\)")
                .matcher(goal).find());
        assertTrue(goal.contains("ItemHandlerHelper.insertItemStacked(inventory, stack, true)"),
                "The familiar must only path to an item if at least part of it fits in the owner inventory");
        assertTrue(goal.contains("stack.getTagCompound().getBoolean(\"PreventRemoteMovement\")"));
        assertTrue(goal.contains("item.getEntityData().getBoolean(\"PreventRemoteMovement\")"));
        assertTrue(goal.contains("item.onCollideWithPlayer("),
                "Pickup must use the vanilla player collision path instead of a one-item mainhand buffer");

        assertFalse(entity.contains("PICKUP_RANGE_SQ"));
        assertFalse(entity.contains("RETURN_RANGE_SQ"));
        assertFalse(entity.contains("pickupCooldown"));
        assertFalse(entity.contains("updateGreedyBehavior"));
        assertFalse(entity.contains("returnHeldItemToOwner"));
        assertFalse(entity.contains("pickupOne"));
        assertFalse(entity.contains("getHeldItemMainhand()"));
        assertFalse(entity.contains("setHeldItem(EnumHand.MAIN_HAND"));
    }

    @Test
    void dedicatedModelAndThreeTexturesMatchThe1814Geometry() throws IOException, NoSuchAlgorithmException {
        String model = readUtf8(JAVA_ROOT.resolve("client").resolve("model")
                .resolve("ModelGreedyFamiliar.java"));
        assertTrue(model.contains("this.textureWidth = 32;"));
        assertTrue(model.contains("this.textureHeight = 32;"));
        assertEquals(16, countOccurrences(model, "public final ModelRenderer"));
        assertTrue(model.contains("this.body = new ModelRenderer(this, 0, 10);"));
        assertTrue(model.contains("this.rightArm = new ModelRenderer(this, 16, 21);"));
        assertTrue(model.contains("this.leftArm = new ModelRenderer(this, 16, 21);"));
        assertTrue(model.contains("this.chest1 = new ModelRenderer(this, 12, 14);"));
        assertEquals(2, countOccurrences(model, "new ModelRenderer(this, 8, 24)"));
        assertTrue(model.contains("this.head = new ModelRenderer(this, 0, 0);"));
        assertTrue(model.contains("this.chest2 = new ModelRenderer(this, 0, 18);"));
        assertTrue(model.contains("this.monster = new ModelRenderer(this, 15, 0);"));
        assertEquals(2, countOccurrences(model, "new ModelRenderer(this, 21, 0)"));
        assertEquals(2, countOccurrences(model, "new ModelRenderer(this, 24, 0)"));
        assertTrue(model.contains("this.nose = new ModelRenderer(this, 18, 8);"));

        assertTrue(model.contains("greedy.getEarRotZ("));
        assertTrue(model.contains("greedy.getEarRotX("));
        assertTrue(model.contains("greedy.getTargetBlock().isPresent()"));
        assertTrue(model.contains("this.rightArm.rotateAngleX = -toRad(100.0F)"));
        assertTrue(model.contains("greedy.getLidRot("));
        assertTrue(model.contains("greedy.getMonsterRot("));

        Path textures = ASSET_ROOT.resolve("textures").resolve("entity");
        assertEquals(GREEDY_TEXTURE_SHA256, sha256(textures.resolve("greedy_familiar.png")));
        assertEquals(GREEDY_CHEST_TEXTURE_SHA256, sha256(textures.resolve("greedy_familiar_chest.png")));
        assertEquals(GREEDY_CHRISTMAS_TEXTURE_SHA256,
                sha256(textures.resolve("greedy_familiar_christmas.png")));
    }

    @Test
    void rendererUsesExclusivePickaxeAndOffhandLayersWithoutTheBipedDuplicate() throws IOException {
        String renderer = readUtf8(JAVA_ROOT.resolve("client").resolve("render")
                .resolve("RenderGreedyFamiliar.java"));
        assertTrue(renderer.contains("extends RenderLiving<EntityGreedyFamiliar>"));
        assertFalse(renderer.contains("extends RenderBiped"),
                "RenderBiped would add a second vanilla LayerHeldItem for the same offhand sample");
        assertTrue(renderer.contains("new ModelGreedyFamiliar()"));
        assertTrue(renderer.contains("textures/entity/greedy_familiar.png"));
        assertTrue(renderer.contains("textures/entity/greedy_familiar_chest.png"));
        assertTrue(renderer.contains("textures/entity/greedy_familiar_christmas.png"));

        String items = renderer.substring(renderer.indexOf("private static class ItemLayer"));
        assertTrue(items.contains("entity.hasBlacksmithUpgrade()"));
        assertTrue(items.contains("entity.getOffhandItem()") || items.contains("entity.getHeldItemOffhand()"));
        assertTrue(items.contains("new ItemStack(Items.IRON_PICKAXE)"));
        assertTrue(items.contains("ItemCameraTransforms.TransformType.GROUND"));
        assertTrue(items.contains("GlStateManager.translate(-0.06F, 0.2F, -0.1F)"));
        assertTrue(items.contains("GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F)"));
        assertTrue(items.contains("GlStateManager.rotate(-45.0F, 0.0F, 0.0F, 1.0F)"));
        assertTrue(items.contains("GlStateManager.translate(0.06F, 0.2F, -0.17F)"));
        assertTrue(items.contains("GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F)"));
        assertTrue(items.contains("GlStateManager.scale(0.75F, 0.75F, 0.75F)"));
        assertTrue(items.contains("model.body.postRender(scale)"));
        assertTrue(items.contains("model.rightArm.postRender(scale)"));
        assertTrue(items.contains("model.leftArm.postRender(scale)"));
    }

    @Test
    void dictionaryExposesThreeGreedyPagesAndRitualUsesThirtySeconds() throws IOException,
            NoSuchAlgorithmException {
        JsonObject root = readJsonObject(ASSET_ROOT.resolve("dictionary").resolve("modonomicon_index.json"));
        JsonObject category = findObject(root.getAsJsonArray("categories"), "id", "familiar_rituals");
        assertNotNull(category, "Missing familiar_rituals dictionary category");
        JsonObject entry = findObject(category.getAsJsonArray("entries"), "id", "familiar_greedy");
        assertNotNull(entry, "Missing familiar_greedy dictionary entry");
        assertEquals("occultism:textures/gui/book/familiar_greedy.png", getString(entry, "icon"));

        JsonArray pages = entry.getAsJsonArray("pages");
        assertEquals(3, pages.size(), "Greedy must expose entity, ritual, and description pages");
        JsonObject entityPage = findObject(pages, "id", "entity");
        assertNotNull(entityPage);
        assertEquals("modonomicon:entity", getString(entityPage, "type"));
        assertEquals("occultism:greedy_familiar", getString(entityPage, "entity_id"));
        JsonObject ritualPage = findObject(pages, "id", "ritual");
        assertNotNull(ritualPage);
        assertEquals("occultism:ritual_recipe", getString(ritualPage, "type"));
        assertEquals("occultism:ritual/familiar_greedy", getString(ritualPage, "recipe_id_1"));
        assertTextPage(pages, "description",
                "book.occultism.dictionary_of_spirits.familiar_rituals.familiar_greedy.description.text");

        Path icon = ASSET_ROOT.resolve("textures").resolve("gui").resolve("book")
                .resolve("familiar_greedy.png");
        assertEquals(GREEDY_BOOK_ICON_SHA256, sha256(icon));

        String generator = readUtf8(TOOLS_ROOT.resolve("generate_modonomicon_index.py"));
        assertTrue(Pattern.compile("\\\"familiar_greedy\\\"\\s*:\\s*\\(\\\"entity\\\",\\s*"
                + "\\\"ritual\\\",\\s*\\\"description\\\"\\)").matcher(generator).find(),
                "Regeneration must preserve all three Greedy dictionary pages");
        String gui = readUtf8(JAVA_ROOT.resolve("client").resolve("gui")
                .resolve("GuiDictionaryOfSpirits.java"));
        assertTrue(gui.contains("entry(\"familiar_greedy\")"),
                "The no-resource fallback dictionary must also expose Greedy");

        JsonArray rituals = new JsonParser().parse(readUtf8(ASSET_ROOT.resolve("rituals")
                .resolve("rituals.json"))).getAsJsonArray();
        JsonObject ritual = findObject(rituals, "id", "familiar_greedy");
        assertNotNull(ritual);
        assertEquals(600, ritual.get("duration_ticks").getAsInt(),
                "The 1.81.4 ritual duration is thirty seconds");
    }

    @Test
    void bilingualTooltipDescribesBlockFindingAndKeepsCuriosOutOfScope() throws IOException {
        String english = readUtf8(ASSET_ROOT.resolve("lang").resolve("en_us.lang"));
        String chinese = readUtf8(ASSET_ROOT.resolve("lang").resolve("zh_cn.lang"));
        String englishTooltip = langValue(english, "item.occultism.ritual_dummy.familiar_greedy.tooltip");
        String chineseTooltip = langValue(chinese, "item.occultism.ritual_dummy.familiar_greedy.tooltip");

        assertTrue(englishTooltip.contains("A Blacksmith can upgrade it once"));
        assertTrue(englishTooltip.toLowerCase().contains("matching blocks"));
        assertTrue(englishTooltip.contains("native one-slot ring effects"));
        assertFalse(englishTooltip.contains("blacksmith upgrades are not implemented"));

        assertTrue(chineseTooltip.contains("\u94c1\u5320\u4f7f\u9b54\u53ef\u5c06\u5b83\u5347\u7ea7\u4e00\u6b21"));
        assertTrue(chineseTooltip.contains("\u5339\u914d") && chineseTooltip.contains("\u65b9\u5757"));
        assertTrue(chineseTooltip.contains("\u539f\u751f\u5355\u69fd\u6212\u6307\u6548\u679c"));
        assertFalse(chineseTooltip.contains("不实现 Curios 拾取范围效果或铁匠升级"));
    }

    private static String section(String source, String start, String end) {
        int startIndex = source.indexOf(start);
        assertTrue(startIndex >= 0, "Missing source marker: " + start);
        int endIndex = source.indexOf(end, startIndex + start.length());
        assertTrue(endIndex >= 0, "Missing source marker after " + start + ": " + end);
        return source.substring(startIndex, endIndex);
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

    private static void assertAttribute(EntityLivingBase entity, IAttribute attribute, double expected) {
        assertNotNull(entity.getEntityAttribute(attribute), "Missing entity attribute: " + attribute.getName());
        assertEquals(expected, entity.getEntityAttribute(attribute).getBaseValue(), 0.000001D,
                "Unexpected base value for " + attribute.getName());
    }

    private static void assertTextPage(JsonArray pages, String id, String textKey) {
        JsonObject page = findObject(pages, "id", id);
        assertNotNull(page, "Missing dictionary page: " + id);
        assertEquals("modonomicon:text", getString(page, "type"));
        assertEquals(textKey, getString(page, "text"));
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
        JsonElement value = object == null ? null : object.get(key);
        if (value == null || value.isJsonNull()) {
            return null;
        }
        if (value.isJsonObject() && value.getAsJsonObject().has("texture")) {
            return value.getAsJsonObject().get("texture").getAsString();
        }
        return value.getAsString();
    }

    private static JsonObject readJsonObject(Path path) throws IOException {
        return new JsonParser().parse(readUtf8(path)).getAsJsonObject();
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

    private static String readUtf8(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    private static final class TestGreedyFamiliar extends EntityGreedyFamiliar {

        private TestGreedyFamiliar() {
            super(null);
        }

        @Override
        public int getGrowingAge() {
            return 0;
        }
    }
}
