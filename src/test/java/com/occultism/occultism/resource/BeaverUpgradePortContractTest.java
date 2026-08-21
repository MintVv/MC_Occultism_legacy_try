package com.occultism.occultism.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.occultism.occultism.entity.EntityBeaverFamiliar;
import com.occultism.occultism.registry.ModItems;

import net.minecraft.init.Bootstrap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.junit.jupiter.api.Test;

class BeaverUpgradePortContractTest {

    private static final Path ROOT = Paths.get("src", "main");
    private static final Path JAVA_ROOT = ROOT.resolve("java").resolve("com").resolve("occultism")
            .resolve("occultism");
    private static final Path ASSET_ROOT = ROOT.resolve("resources").resolve("assets").resolve("occultism");
    private static final String BEAVER_NUGGET_TEXTURE_SHA256 =
            "c126e7824e4c600665d6b7102e49c07a09a44e5182158c331140f81a5815f1da";

    @Test
    void beaverAcceptsExactlyOneBlacksmithUpgrade() {
        Bootstrap.register();

        EntityBeaverFamiliar beaver = new EntityBeaverFamiliar(null);
        assertFalse(beaver.hasBlacksmithUpgrade());
        assertTrue(beaver.canBlacksmithUpgrade());
        assertSame(beaver, beaver.getFamiliarEntity());
        assertNull(beaver.getFamiliarOwner());

        beaver.blacksmithUpgrade();
        assertTrue(beaver.hasBlacksmithUpgrade());
        assertFalse(beaver.canBlacksmithUpgrade(),
                "The port intentionally fixes the 1.81.4 Beaver's repeated iron-consumption bug");

        beaver.blacksmithUpgrade();
        assertTrue(beaver.hasBlacksmithUpgrade());
        assertFalse(beaver.canBlacksmithUpgrade());
    }

    @Test
    void upgradeStateAndSnackInteractionUseTheSelectedPortContract() throws IOException {
        String entity = readUtf8(JAVA_ROOT.resolve("entity").resolve("EntityBeaverFamiliar.java"));
        assertTrue(entity.contains("implements IBlacksmithUpgradable"));
        assertTrue(entity.contains("EntityDataManager.createKey(EntityBeaverFamiliar.class, DataSerializers.BOOLEAN)"));
        assertTrue(entity.contains("this.setBlacksmithUpgrade(compound.getBoolean(\"hasBlacksmithUpgrade\"));"));
        assertTrue(entity.contains("compound.setBoolean(\"hasBlacksmithUpgrade\", this.hasBlacksmithUpgrade());"));
        assertTrue(entity.contains("return !this.hasBlacksmithUpgrade();"),
                "An upgraded Beaver must stop qualifying for another paid Blacksmith pass");

        String interaction = section(entity, "public boolean processInteract(EntityPlayer player, EnumHand hand) {",
                "public boolean attackEntityFrom");
        assertTrue(interaction.contains("!player.isSneaking()") && interaction.contains("stack.isEmpty()"));
        assertTrue(interaction.contains("getTotalWorldTime() > this.lastSnackTime + SNACK_INTERVAL"),
                "The upstream boundary is strict: T + 20 is still on cooldown and T + 21 succeeds");
        assertTrue(interaction.contains("ItemHandlerHelper.giveItemToPlayer(player,"
                + " new ItemStack(ModItems.BEAVER_NUGGET))"));
        assertTrue(interaction.contains("dialog.occultism.beaver.no_upgrade"));
        assertTrue(interaction.contains("dialog.occultism.beaver.snack_on_cooldown"));
        int snackGate = interaction.indexOf("!player.isSneaking()");
        int mainHandSitGate = interaction.indexOf("hand == EnumHand.MAIN_HAND");
        assertTrue(snackGate >= 0 && (mainHandSitGate < 0 || snackGate < mainHandSitGate),
                "The snack branch must also support an empty interacted offhand before the main-hand sitting rule");
    }

    @Test
    void beaverNuggetUsesUpstreamFoodValuesAndAppendOnlyRegistration() throws IOException {
        Bootstrap.register();

        Item item = ModItems.BEAVER_NUGGET;
        assertEquals(new ResourceLocation("occultism", "beaver_nugget"), item.getRegistryName());
        assertEquals("item.occultism.beaver_nugget", item.getTranslationKey());
        assertEquals(64, item.getItemStackLimit());
        assertTrue(item instanceof ItemFood);

        ItemFood food = (ItemFood) item;
        ItemStack stack = new ItemStack(item);
        assertEquals(8, food.getHealAmount(stack));
        assertEquals(0.8F, food.getSaturationModifier(stack), 0.000001F);
        assertFalse(food.isWolfsFavoriteMeat());

        String modItems = readUtf8(JAVA_ROOT.resolve("registry").resolve("ModItems.java"));
        String registrations = section(modItems, "public static final Item[] ITEMS = {", "};")
                .replace("\r\n", "\n");
        assertEquals(1, countOccurrences(registrations, "BEAVER_NUGGET"));
        assertTrue(registrations.indexOf("BEAVER_NUGGET")
                        > registrations.indexOf("RITUAL_DUMMY_FAMILIAR_BLACKSMITH"),
                "The new item must be appended after every existing item registration");
    }

    @Test
    void beaverNuggetModelAndTextureMatch1814() throws IOException, NoSuchAlgorithmException {
        Path modelPath = ASSET_ROOT.resolve("models").resolve("item").resolve("beaver_nugget.json");
        JsonObject model = new JsonParser().parse(readUtf8(modelPath)).getAsJsonObject();
        assertTrue(model.get("parent").getAsString().endsWith("item/generated"));
        assertEquals("occultism:item/beaver_nugget",
                model.getAsJsonObject("textures").get("layer0").getAsString());

        Path texture = ASSET_ROOT.resolve("textures").resolve("item").resolve("beaver_nugget.png");
        assertEquals(BEAVER_NUGGET_TEXTURE_SHA256, sha256(texture));
    }

    @Test
    void bilingualDialogsAndTooltipDescribeTheOneTimeSnackUpgrade() throws IOException {
        String english = readUtf8(ASSET_ROOT.resolve("lang").resolve("en_us.lang"));
        String chinese = readUtf8(ASSET_ROOT.resolve("lang").resolve("zh_cn.lang"));

        assertEquals("A Blacksmith Familiar needs to upgrade the Beaver before he dispenses snacks!",
                langValue(english, "dialog.occultism.beaver.no_upgrade"));
        assertEquals("Hey now, don't be greedy!",
                langValue(english, "dialog.occultism.beaver.snack_on_cooldown"));
        assertEquals("\u9700\u8981\u5148\u4f7f\u7528\u94c1\u5320\u4f7f\u9b54\u5347\u7ea7\u6cb3\u72f8\uff0c"
                        + "\u7136\u540e\u5b83\u624d\u80fd\u5206\u53d1\u96f6\u98df\uff01",
                langValue(chinese, "dialog.occultism.beaver.no_upgrade"));
        assertEquals("\u563f\uff0c\u522b\u592a\u8d2a\u5fc3\uff01",
                langValue(chinese, "dialog.occultism.beaver.snack_on_cooldown"));
        assertEquals("Beaver Nugget", langValue(english, "item.occultism.beaver_nugget.name"));
        assertEquals("\u6cb3\u72f8\u8089\u5757", langValue(chinese, "item.occultism.beaver_nugget.name"));

        String englishTooltip = langValue(english, "item.occultism.ritual_dummy.familiar_beaver.tooltip");
        String chineseTooltip = langValue(chinese, "item.occultism.ritual_dummy.familiar_beaver.tooltip");
        assertTrue(englishTooltip.contains("A Blacksmith can upgrade it once"));
        assertTrue(englishTooltip.contains("free Beaver Nugget"));
        assertTrue(chineseTooltip.contains("\u94c1\u5320\u4f7f\u9b54\u53ef\u5c06\u5b83\u5347\u7ea7\u4e00\u6b21"));
        assertTrue(chineseTooltip.contains("\u514d\u8d39\u7684\u6cb3\u72f8\u8089\u5757"));
    }

    @Test
    void dictionaryDescribesTheActualSnackInsteadOfUnrelatedEffects() throws IOException {
        String english = readUtf8(ASSET_ROOT.resolve("lang").resolve("en_us.lang"));
        String chinese = readUtf8(ASSET_ROOT.resolve("lang").resolve("zh_cn.lang"));
        String key = "book.occultism.dictionary_of_spirits.getting_started.mod_food."
                + "spotlight_beaver_nugget.text";
        String englishText = langValue(english, key);
        String chineseText = langValue(chinese, key);

        assertTrue(englishText.contains("upgraded Beaver Familiar"));
        assertFalse(englishText.contains("dropped by Beaver Familiars"));
        assertFalse(englishText.toLowerCase().contains("mining fatigue"));
        assertFalse(englishText.toLowerCase().contains("mining speed"));

        assertTrue(chineseText.contains("\u5347\u7ea7\u540e\u7684\u6cb3\u72f8\u4f7f\u9b54"));
        assertFalse(chineseText.contains("\u7531\u6cb3\u72f8\u4f7f\u9b54\u6389\u843d"));
        assertFalse(chineseText.contains("\u6316\u6398\u75b2\u52b3"));
        assertFalse(chineseText.contains("\u91c7\u77ff\u901f\u5ea6"));
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
}
