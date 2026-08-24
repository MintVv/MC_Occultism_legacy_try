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
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.occultism.occultism.entity.EntityBlacksmithFamiliar;
import com.occultism.occultism.entity.EntityCthulhuFamiliar;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.init.Bootstrap;
import org.junit.jupiter.api.Test;

class BlacksmithPortContractTest {

    private static final Path ASSET_ROOT = Paths.get("src", "main", "resources", "assets", "occultism");
    private static final Path JAVA_ROOT = Paths.get("src", "main", "java", "com", "occultism", "occultism");
    private static final Path ENTITY_ROOT = JAVA_ROOT.resolve("entity");
    private static final Path TOOLS_ROOT = Paths.get("tools");
    private static final String BLACKSMITH_TEXTURE_SHA256 =
            "2b6dc08109e9ec91e251297d672b468ec60ae63d43875c75ad081f20499da40d";

    @Test
    void blacksmithRitualMatchesThe1814Contract() throws IOException {
        JsonArray rituals = readJsonElement(ASSET_ROOT.resolve("rituals").resolve("rituals.json")).getAsJsonArray();
        JsonObject actual = null;
        int matches = 0;
        for (JsonElement element : rituals) {
            JsonObject ritual = element.getAsJsonObject();
            if ("familiar_blacksmith".equals(getString(ritual, "id"))) {
                actual = ritual;
                matches++;
            }
        }
        assertEquals(1, matches, "Exactly one familiar_blacksmith ritual must be registered");

        JsonObject expected = new JsonObject();
        expected.addProperty("id", "familiar_blacksmith");
        expected.addProperty("display_name", "Blacksmith Familiar");
        expected.addProperty("type", "occultism:ritual");
        expected.addProperty("ritual_type", "occultism:summon_tamed");
        expected.addProperty("activation_item", "occultism:book_of_binding_bound_foliot");
        expected.addProperty("duration_ticks", 600);

        JsonObject sacrifice = new JsonObject();
        sacrifice.addProperty("display_name", "ritual.occultism.sacrifice.zombies");
        sacrifice.addProperty("tag", "minecraft:zombies");
        expected.add("entity_to_sacrifice", sacrifice);
        expected.addProperty("entity_to_summon", "occultism:blacksmith_familiar");

        JsonArray ingredients = new JsonArray();
        ingredients.add("minecraft:iron_shovel");
        ingredients.add("minecraft:iron_pickaxe");
        ingredients.add("minecraft:iron_axe");
        ingredients.add("minecraft:anvil");
        for (int index = 0; index < 4; index++) {
            ingredients.add("#c:stones");
        }
        expected.add("ingredients", ingredients);
        expected.addProperty("pentacle_id", "occultism:possess_foliot");

        JsonObject result = new JsonObject();
        result.addProperty("id", "occultism:soul_gem");
        expected.add("result", result);

        JsonObject ritualDummy = new JsonObject();
        ritualDummy.addProperty("id", "occultism:ritual_dummy/familiar_blacksmith");
        expected.add("ritual_dummy", ritualDummy);

        assertEquals(expected, actual, "The 1.12 Blacksmith ritual must retain the selected 1.81.4 contract");

        String oreDictionary = readUtf8(JAVA_ROOT.resolve("registry").resolve("ModOreDictionary.java"));
        assertTrue(oreDictionary.contains("OreDictionary.registerOre(\"ingotIron\", Items.IRON_INGOT);"),
                "Blacksmith feeding must have a vanilla iron-ingot OreDictionary entry");
        assertTrue(oreDictionary.contains("OreDictionary.registerOre(\"blockIron\", Blocks.IRON_BLOCK);"),
                "Blacksmith feeding must have a vanilla iron-block OreDictionary entry");
        assertTrue(oreDictionary.contains("OreDictionary.registerOre(\"stones\","),
                "c:stones must resolve to the local stones OreDictionary entry");
    }

    @Test
    void blacksmithEntityAndDummyAreAppendOnlyRegistrations() throws IOException {
        String entities = readUtf8(JAVA_ROOT.resolve("registry").resolve("ModEntities.java"));
        Pattern cthulhuRegistration = Pattern.compile(
                "EntityRegistry\\.registerModEntity\\(new ResourceLocation\\(Tags\\.MOD_ID, \\\"cthulhu_familiar\\\"\\),\\s*"
                        + "EntityCthulhuFamiliar\\.class, \\\"cthulhu_familiar\\\", nextEntityId\\+\\+, "
                        + "Occultism\\.INSTANCE, 64, 3, true\\);",
                Pattern.DOTALL);
        Pattern blacksmithRegistration = Pattern.compile(
                "EntityRegistry\\.registerModEntity\\(new ResourceLocation\\(Tags\\.MOD_ID, \\\"blacksmith_familiar\\\"\\),\\s*"
                        + "EntityBlacksmithFamiliar\\.class, \\\"blacksmith_familiar\\\", nextEntityId\\+\\+, "
                        + "Occultism\\.INSTANCE, 64, 3, true\\);",
                Pattern.DOTALL);
        Matcher cthulhuMatcher = cthulhuRegistration.matcher(entities);
        Matcher blacksmithMatcher = blacksmithRegistration.matcher(entities);
        assertTrue(cthulhuMatcher.find(), "The existing Cthulhu registration is missing");
        assertTrue(blacksmithMatcher.find(), "blacksmith_familiar must use the normal tracked mod-entity registration");
        assertTrue(blacksmithMatcher.start() > cthulhuMatcher.end(),
                "Blacksmith must be appended after Cthulhu so existing numeric entity IDs do not move");
        assertEquals(1, countOccurrences(entities,
                "new ResourceLocation(Tags.MOD_ID, \"blacksmith_familiar\")"),
                "Later append-only entity migrations must not duplicate or move the Blacksmith registration");

        String items = readUtf8(JAVA_ROOT.resolve("registry").resolve("ModItems.java"));
        assertTrue(items.contains("public static final Item RITUAL_DUMMY_FAMILIAR_BLACKSMITH"),
                "The Blacksmith ritual dummy must retain a named registry constant");
        assertTrue(items.contains("item(\"ritual_dummy/familiar_blacksmith\", "
                + "\"ritual_dummy.familiar_blacksmith\", 1)"),
                "The ritual dummy constant must resolve the ID used by ritual data");
        assertEquals(2, countOccurrences(items, "RITUAL_DUMMY_FAMILIAR_BLACKSMITH"),
                "The Blacksmith ritual dummy must be declared once and included in ITEMS once");
        String itemArray = section(items, "public static final Item[] ITEMS = {", "};");
        assertTrue(itemArray.indexOf("BEAVER_NUGGET") > itemArray.indexOf("RITUAL_DUMMY_FAMILIAR_BLACKSMITH"));
    }

    @Test
    void blacksmithClientResourcesMatchTheSelectedUpstreamSnapshot()
            throws IOException, NoSuchAlgorithmException {
        Path modelPath = JAVA_ROOT.resolve("client").resolve("model").resolve("ModelBlacksmithFamiliar.java");
        Path rendererPath = JAVA_ROOT.resolve("client").resolve("render").resolve("RenderBlacksmithFamiliar.java");
        assertTrue(Files.isRegularFile(modelPath), "Missing dedicated Blacksmith model");
        assertTrue(Files.isRegularFile(rendererPath), "Missing dedicated Blacksmith renderer");

        String renderer = readUtf8(rendererPath);
        assertTrue(renderer.contains("new ModelBlacksmithFamiliar()"),
                "The renderer must use the dedicated Blacksmith model");
        assertTrue(renderer.contains("textures/entity/blacksmith_familiar.png"),
                "The renderer must resolve the migrated upstream texture");
        assertTrue(Pattern.compile("for \\(int i = 0; i < entity\\.getBars\\(\\); i\\+\\+\\)")
                .matcher(renderer).find(), "The ingot layer must render the synchronized number of iron bars");

        String renderers = readUtf8(JAVA_ROOT.resolve("client").resolve("render")
                .resolve("ModEntityRenderers.java"));
        assertTrue(renderers.contains("RenderingRegistry.registerEntityRenderingHandler(EntityBlacksmithFamiliar.class"),
                "The Blacksmith entity must have a client rendering registration");
        assertTrue(renderers.contains("return new RenderBlacksmithFamiliar(manager);"),
                "The registration must construct the dedicated Blacksmith renderer");

        Path texture = ASSET_ROOT.resolve("textures").resolve("entity").resolve("blacksmith_familiar.png");
        BufferedImage image = ImageIO.read(texture.toFile());
        assertNotNull(image, "blacksmith_familiar.png must be a readable PNG");
        assertEquals(64, image.getWidth(), "Unexpected Blacksmith texture width");
        assertEquals(64, image.getHeight(), "Unexpected Blacksmith texture height");
        assertEquals(BLACKSMITH_TEXTURE_SHA256, sha256(texture),
                "The Blacksmith texture must match Occultism 1.81.4 commit 84b8ad4");

        JsonObject dummyModel = readJsonObject(ASSET_ROOT.resolve("models").resolve("item")
                .resolve("ritual_dummy").resolve("familiar_blacksmith.json"));
        assertEquals("occultism:item/ritual_dummy", getString(dummyModel, "parent"));

        JsonObject baseDummyModel = readJsonObject(ASSET_ROOT.resolve("models").resolve("item")
                .resolve("ritual_dummy.json"));
        assertEquals("item/generated", getString(baseDummyModel, "parent"));
        assertEquals("occultism:item/ritual_dummy",
                getString(baseDummyModel.getAsJsonObject("textures"), "layer0"));
    }

    @Test
    void dictionaryEntryResolvesAllFourBlacksmithPages() throws IOException {
        JsonObject root = readJsonObject(ASSET_ROOT.resolve("dictionary").resolve("modonomicon_index.json"));
        JsonObject category = findObject(root.getAsJsonArray("categories"), "id", "familiar_rituals");
        assertNotNull(category, "Missing familiar_rituals dictionary category");
        JsonObject entry = findObject(category.getAsJsonArray("entries"), "id", "familiar_blacksmith");
        assertNotNull(entry, "Missing familiar_blacksmith dictionary entry");
        assertEquals("occultism:textures/gui/book/familiar_blacksmith.png", getString(entry, "icon"));
        assertTrue(Files.isRegularFile(ASSET_ROOT.resolve("textures").resolve("gui").resolve("book")
                .resolve("familiar_blacksmith.png")), "The Blacksmith dictionary icon is missing");

        JsonArray pages = entry.getAsJsonArray("pages");
        assertEquals(4, pages.size(), "Blacksmith must expose entity, ritual, description, and description2 pages");
        Set<String> pageIds = new HashSet<>();
        for (JsonElement pageElement : pages) {
            pageIds.add(getString(pageElement.getAsJsonObject(), "id"));
        }
        assertEquals(new HashSet<>(Arrays.asList("entity", "ritual", "description", "description2")), pageIds);

        JsonObject entityPage = findObject(pages, "id", "entity");
        assertEquals("modonomicon:entity", getString(entityPage, "type"));
        assertEquals("occultism:blacksmith_familiar", getString(entityPage, "entity_id"));
        JsonObject ritualPage = findObject(pages, "id", "ritual");
        assertEquals("occultism:ritual_recipe", getString(ritualPage, "type"));
        assertEquals("occultism:ritual/familiar_blacksmith", getString(ritualPage, "recipe_id_1"));
        assertTextPage(pages, "description",
                "book.occultism.dictionary_of_spirits.familiar_rituals.familiar_blacksmith.description.text");
        assertTextPage(pages, "description2",
                "book.occultism.dictionary_of_spirits.familiar_rituals.familiar_blacksmith.description2.text");

        String generator = readUtf8(TOOLS_ROOT.resolve("generate_modonomicon_index.py"));
        assertTrue(Pattern.compile("\\\"familiar_blacksmith\\\"\\s*:\\s*\\(\\\"entity\\\",\\s*"
                + "\\\"ritual\\\",\\s*\\\"description\\\",\\s*\\\"description2\\\"\\)")
                .matcher(generator).find(), "Regeneration must preserve all four Blacksmith dictionary pages");
        String gui = readUtf8(JAVA_ROOT.resolve("client").resolve("gui").resolve("GuiDictionaryOfSpirits.java"));
        assertTrue(gui.contains("entry(\"familiar_blacksmith\")"),
                "The no-resource fallback dictionary must also expose Blacksmith");
    }

    @Test
    void familiarRingCapturesUnboundOrOwnedBlacksmith() throws IOException {
        String ring = readUtf8(JAVA_ROOT.resolve("item").resolve("ItemFamiliarRing.java"));
        String supported = section(ring,
                "private static boolean isSupportedStoredFamiliar(@Nullable Entity entity) {",
                "private static boolean canCaptureFamiliar");
        assertTrue(supported.contains("entity instanceof EntityBlacksmithFamiliar"),
                "The familiar ring must be able to restore a stored Blacksmith");

        // Capture is interface-driven, so Blacksmith inherits the shared IFamiliar ownership gate.
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

    @Test
    void blacksmithConfigurationKeepsTheUpstreamDefaults() throws IOException {
        String config = readUtf8(JAVA_ROOT.resolve("config").resolve("ModConfig.java"));
        assertTrue(config.contains("@Config(modid = Tags.MOD_ID"), "Blacksmith settings must use Forge 1.12 config");
        assertTrue(Pattern.compile("blacksmithFamiliarRepairChance\\s*=\\s*0\\.05D?;")
                .matcher(config).find(), "Repair chance must default to 0.05");
        assertTrue(Pattern.compile("blacksmithFamiliarUpgradeCost\\s*=\\s*18;")
                .matcher(config).find(), "Upgrade cost must default to 18 iron units");
        assertTrue(Pattern.compile("blacksmithFamiliarUpgradeCooldown\\s*=\\s*(?:400|20\\s*\\*\\s*20);")
                .matcher(config).find(), "Upgrade cooldown must default to 400 ticks");
        assertTrue(Pattern.compile("@Config\\.RangeInt\\(min = 1\\)\\s*"
                + "public int blacksmithFamiliarUpgradeCost", Pattern.DOTALL).matcher(config).find(),
                "Upgrade cost must not allow zero because the bar calculation divides by it");
    }

    @Test
    void familiarSourcesRegisterTheRequiredCombatAttributes() throws IOException {
        String blacksmith = readUtf8(ENTITY_ROOT.resolve("EntityBlacksmithFamiliar.java"));
        assertTrue(blacksmith.contains("this.setSize(0.65F, 1.0F);"));
        assertTrue(blacksmith.contains("SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30.0D);"));
        assertTrue(blacksmith.contains("SharedMonsterAttributes.ARMOR).setBaseValue(15.0D);"));
        assertTrue(blacksmith.contains("SharedMonsterAttributes.ARMOR_TOUGHNESS).setBaseValue(10.0D);"));
        assertTrue(blacksmith.contains("SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);"));
        assertTrue(Pattern.compile("SharedMonsterAttributes\\.ATTACK_DAMAGE\\)"
                + "(?:\\.setBaseValue\\(6\\.0D\\)|\\.setBaseValue\\(6\\.0D\\);)")
                .matcher(blacksmith).find(), "Blacksmith must expose a six-damage attack attribute");

        String cthulhu = readUtf8(ENTITY_ROOT.resolve("EntityCthulhuFamiliar.java"));
        assertTrue(cthulhu.contains("SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);"),
                "Cthulhu must register the attack attribute used by its migrated contract");
    }

    @Test
    void bootstrappedFamiliarConstructorsExposeTheRequiredAttributes() {
        Bootstrap.register();

        EntityBlacksmithFamiliar blacksmith = new EntityBlacksmithFamiliar(null);
        assertEquals(0.65F, blacksmith.width, 0.0001F);
        assertEquals(1.0F, blacksmith.height, 0.0001F);
        assertAttribute(blacksmith, SharedMonsterAttributes.MAX_HEALTH, 30.0D);
        assertAttribute(blacksmith, SharedMonsterAttributes.ARMOR, 15.0D);
        assertAttribute(blacksmith, SharedMonsterAttributes.ARMOR_TOUGHNESS, 10.0D);
        assertAttribute(blacksmith, SharedMonsterAttributes.MOVEMENT_SPEED, 0.3D);
        assertAttribute(blacksmith, SharedMonsterAttributes.ATTACK_DAMAGE, 6.0D);

        EntityCthulhuFamiliar cthulhu = new EntityCthulhuFamiliar(null);
        assertAttribute(cthulhu, SharedMonsterAttributes.ATTACK_DAMAGE, 6.0D);
    }

    @Test
    void ironFeedingCapacityBarsPersistenceAndRefundRemainCoherent() throws IOException {
        String entity = readUtf8(ENTITY_ROOT.resolve("EntityBlacksmithFamiliar.java"));
        String interaction = section(entity, "public boolean processInteract(EntityPlayer player, EnumHand hand) {",
                "public void setPartying");
        int ingotClassification = interaction.indexOf("boolean isIngot = matchesOre(stack, \"ingotIron\");");
        int blockClassification = interaction.indexOf("boolean isBlock = matchesOre(stack, \"blockIron\");");
        int shrink = interaction.indexOf("stack.shrink(1);");
        assertTrue(ingotClassification >= 0 && blockClassification >= 0 && shrink >= 0,
                "Iron feeding must classify ingots and blocks and then consume one item");
        assertTrue(ingotClassification < shrink && blockClassification < shrink,
                "The iron value must be captured before shrinking a one-count stack");
        assertTrue(interaction.contains("this.ironCount < getMaxIron()"),
                "Feeding must stop once the configured ten-upgrade capacity is reached");
        assertTrue(interaction.contains("this.changeIronCount(isIngot ? 1 : 9);"),
                "Iron ingots and blocks must contribute one and nine units respectively");

        String maxIron = section(entity, "private static int getMaxIron() {", "private void setEarring");
        assertTrue(Pattern.compile("blacksmithFamiliarUpgradeCost\\s*\\*\\s*10")
                .matcher(maxIron).find(), "Capacity must equal ten configured upgrades");
        String setIron = section(entity, "private void setIronCount(int count) {", "private void changeIronCount");
        assertTrue(Pattern.compile("Math\\.min\\(10, this\\.ironCount / cost\\)")
                .matcher(setIron).find(), "The synchronized bar count must be capped at ten completed upgrades");

        assertTrue(entity.contains("compound.setInteger(\"ironCount\", this.ironCount);"),
                "Raw stored iron must be saved to NBT");
        assertTrue(entity.contains("this.setIronCount(compound.getInteger(\"ironCount\"));"),
                "Loading NBT must also rebuild the synchronized bar count");
        assertTrue(entity.contains("int blockCount = this.ironCount / 9;"),
                "Death refunds must convert stored iron into blocks first");
        assertTrue(entity.contains("int ingotCount = this.ironCount % 9;"),
                "Death refunds must preserve the remaining individual ingots");
        assertTrue(entity.contains("new ItemStack(Items.IRON_INGOT, ingotCount)"));
        assertTrue(entity.contains("new ItemStack(Blocks.IRON_BLOCK, blockCount)"));
    }

    @Test
    void upgradeGoalRequiresSameOwnerRangeCostAndCooldown() throws IOException {
        String entity = readUtf8(ENTITY_ROOT.resolve("EntityBlacksmithFamiliar.java"));
        int start = entity.indexOf("private static final class UpgradeGoal");
        assertTrue(start >= 0, "Missing Blacksmith upgrade goal");
        String upgrade = entity.substring(start);

        assertTrue(upgrade.contains("private IBlacksmithUpgradable target;"),
                "Upgrade targeting must be capability-based rather than a hardcoded familiar list");
        assertTrue(upgrade.contains("this.blacksmith.ironCount >= ModConfig.SPIRIT_JOBS.blacksmithFamiliarUpgradeCost"),
                "The goal must not start without the configured iron cost");
        assertTrue(upgrade.contains("this.cooldown-- < 0"),
                "The selected 1.81.4 cooldown countdown semantics must be retained");
        assertTrue(upgrade.contains("this.cooldown = ModConfig.SPIRIT_JOBS.blacksmithFamiliarUpgradeCooldown;"),
                "Finishing an attempt must reset the configured cooldown");
        assertTrue(upgrade.contains("getEntityBoundingBox().grow(4.0D)"),
                "Upgrade candidates must be searched within the upstream four-block AABB");
        assertTrue(Pattern.compile("owner != null && owner == this\\.blacksmith\\.getOwner\\(\\)")
                .matcher(upgrade).find(), "Only a familiar belonging to the same loaded owner may be upgraded");
        assertTrue(upgrade.contains("this.blacksmith.getDistanceSq(targetEntity) < 3.0D"),
                "Iron may only be spent when the Blacksmith reaches the upstream distance threshold");
        assertTrue(upgrade.contains("tryMoveToEntityLiving(this.target.getFamiliarEntity(), 0.7D)"),
                "The Blacksmith must approach its upgrade target at the upstream speed");
        int mutation = upgrade.indexOf("this.target.blacksmithUpgrade();");
        int payment = upgrade.indexOf("this.blacksmith.changeIronCount("
                + "-ModConfig.SPIRIT_JOBS.blacksmithFamiliarUpgradeCost);");
        assertTrue(mutation >= 0 && payment > mutation,
                "Iron must only be deducted after the target still accepts the upgrade");
    }

    @Test
    void stonePickupConsumesTheStackAndRepairsOnlyOneEquipmentItem() throws IOException {
        String handler = readUtf8(JAVA_ROOT.resolve("handler").resolve("LootEventHandler.java"));
        assertTrue(handler.contains("EntityItemPickupEvent"),
                "The repair mechanic must run at item pickup rather than an unrelated mining event");
        assertTrue(handler.contains("!matchesOre(stone, \"stone\") && !matchesOre(stone, \"cobblestone\")"),
                "Both stone and cobblestone inputs must be accepted");
        assertTrue(handler.contains("blacksmithFamiliarRepairChance * stone.getCount()"),
                "The repair chance must scale linearly with the consumed stack count");
        assertTrue(handler.contains("FamiliarUtil.getAllFamiliars"),
                "Stone repair must query nearby and equipped Blacksmith familiars through the shared utility");
        assertTrue(handler.contains("familiar.isTamed()") && handler.contains("familiar.isOwner(player)"),
                "A foreign or untamed Blacksmith must not consume the player's stone");
        assertTrue(handler.contains("event.setCanceled(true);") && handler.contains("entity.setDead();"),
                "Handled stone pickup must cancel vanilla insertion and consume the entire entity stack");
        assertTrue(handler.indexOf("event.setCanceled(true);") < handler.indexOf("entity.setDead();"));
        assertTrue(handler.contains("EntityBlacksmithFamiliar.class"),
                "Stone repair must keep the Blacksmith type boundary");

        String repair = section(handler, "private static void repairEquipment(EntityPlayer player) {",
                "private static boolean matchesOre");
        assertTrue(repair.contains("player.getEquipmentAndArmor()"),
                "Repair scanning must be limited to held equipment and armor");
        int repairMutation = repair.indexOf("stack.setItemDamage(Math.max(0, stack.getItemDamage() - 2));");
        int earlyReturn = repair.indexOf("return;", repairMutation);
        assertTrue(repairMutation >= 0 && earlyReturn > repairMutation,
                "Exactly the first damaged equipment stack must be repaired by two durability");
        assertFalse(repair.contains("mainInventory"), "The Blacksmith must not repair the whole player inventory");
    }

    @Test
    void onlyFamiliarsWithMigratedEffectsOptIntoTheUpgradeCapability() throws IOException {
        Set<String> implementors = new HashSet<>();
        try (Stream<Path> files = Files.walk(ENTITY_ROOT)) {
            files.filter(path -> path.getFileName().toString().endsWith(".java"))
                    .forEach(path -> {
                        try {
                            if (readUtf8(path).contains("implements IBlacksmithUpgradable")) {
                                implementors.add(path.getFileName().toString());
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
        assertEquals(new HashSet<>(Arrays.asList("EntityBatFamiliar.java", "EntityBeaverFamiliar.java",
                "EntityBeholderFamiliar.java",
                "EntityCthulhuFamiliar.java", "EntityDeerFamiliar.java", "EntityDevilFamiliar.java",
                "EntityDragonFamiliar.java",
                "EntityGoatFamiliar.java", "EntityGuardianFamiliar.java", "EntityGreedyFamiliar.java",
                "EntityHeadlessFamiliar.java", "EntityMummyFamiliar.java",
                "EntityShubNiggurathFamiliar.java")), implementors,
                "Only familiars with a complete migrated upgrade behavior may opt into Blacksmith upgrades");

        String interfaceSource = readUtf8(ENTITY_ROOT.resolve("IBlacksmithUpgradable.java"));
        assertTrue(interfaceSource.contains("boolean canBlacksmithUpgrade();"));
        assertTrue(interfaceSource.contains("void blacksmithUpgrade();"));
        String blacksmith = readUtf8(ENTITY_ROOT.resolve("EntityBlacksmithFamiliar.java"));
        assertFalse(blacksmith.contains("EntityCthulhuFamiliar"),
                "The Blacksmith AI must discover opt-in targets through the interface, not a concrete class list");
        assertFalse(blacksmith.contains("EntityGuardianFamiliar"));
        assertFalse(blacksmith.contains("EntityMummyFamiliar"));
        assertFalse(blacksmith.contains("EntityBatFamiliar"));
        assertFalse(blacksmith.contains("EntityGreedyFamiliar"));
    }

    private static void assertTextPage(JsonArray pages, String id, String textKey) {
        JsonObject page = findObject(pages, "id", id);
        assertNotNull(page, "Missing dictionary page: " + id);
        assertEquals("modonomicon:text", getString(page, "type"));
        assertEquals(textKey, getString(page, "text"));
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
