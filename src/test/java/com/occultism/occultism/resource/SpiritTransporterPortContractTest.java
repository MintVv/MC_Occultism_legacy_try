package com.occultism.occultism.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.occultism.occultism.container.ContainerSpiritTransporter;
import com.occultism.occultism.entity.EntityFoliotJob;
import com.occultism.occultism.network.MessageSetSpiritFilterMode;
import com.occultism.occultism.network.MessageSetSpiritTagFilter;
import com.occultism.occultism.spirit.SpiritJobType;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.oredict.OreDictionary;

/** Contract and behavior coverage for the 1.12 transporter/cleaner filter screen. */
class SpiritTransporterPortContractTest {

    private static final Path ROOT = Paths.get("src", "main");
    private static final Path JAVA_ROOT = ROOT.resolve("java").resolve("com").resolve("occultism")
            .resolve("occultism");
    private static final Path ASSET_ROOT = ROOT.resolve("resources").resolve("assets").resolve("occultism");

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void guiMatchesUpstreamTransporterLayoutAndCloseTiming() throws Exception {
        String gui = readJava("client", "gui", "GuiSpiritTransporter.java");
        String container = readJava("container", "ContainerSpiritTransporter.java");
        String handler = readJava("gui", "ModGuiHandler.java");

        assertTrue(gui.contains("this.xSize = 176"));
        assertTrue(gui.contains("this.ySize = 220"));
        assertTrue(gui.contains("this.guiLeft + 151, this.guiTop + 83"));
        assertTrue(gui.contains("this.guiTop + 123, 124"));
        assertTrue(gui.contains("setMaxStringLength(EntityFoliotJob.MAX_TAG_FILTER_LENGTH)"));
        assertTrue(gui.contains("ModNetworking.sendSpiritTagFilter"));
        assertTrue(gui.indexOf("ModNetworking.sendSpiritTagFilter") < gui.indexOf("super.onGuiClosed()"));

        assertTrue(container.contains("MAX_FILTER_SLOTS / 2"));
        assertTrue(container.contains("int top = 84"));
        assertTrue(container.contains("playerInventoryTop = 138"));
        assertTrue(container.contains("int hotbarTop = 196"));
        assertTrue(container.contains("clickTypeIn != ClickType.PICKUP"));
        assertTrue(handler.contains("new ContainerSpiritTransporter(player.inventory, spirit)"));
        assertTrue(handler.contains("new GuiSpiritTransporter(player.inventory, spirit)"));
    }

    @Test
    void transporterContainerHasTheUpstreamSlotGeometry() {
        EntityFoliotJob spirit = new EntityFoliotJob(null, SpiritJobType.TRANSPORT_ITEMS);
        ContainerSpiritTransporter container =
                new ContainerSpiritTransporter(new InventoryPlayer(null), spirit);

        assertEquals(51, container.inventorySlots.size());
        assertEquals(14, spirit.getFilterItems().getSlots());
        for (int row = 0; row < 2; row++) {
            for (int column = 0; column < 7; column++) {
                int index = 37 + column + row * 7;
                assertEquals(8 + column * 18, container.inventorySlots.get(index).xPos);
                assertEquals(84 + row * 18, container.inventorySlots.get(index).yPos);
                assertEquals(1, container.inventorySlots.get(index).getSlotStackLimit());
            }
        }
    }

    @Test
    void filterSemanticsMatchUpstreamWhitelistAndBlacklistRules() {
        EntityFoliotJob spirit = new EntityFoliotJob(null, SpiritJobType.TRANSPORT_ITEMS);
        ItemStack diamond = new ItemStack(Items.DIAMOND);
        ItemStack iron = new ItemStack(Items.IRON_INGOT);

        assertFalse(spirit.isFilterBlacklist(), "new spirits retain the upstream whitelist default");
        assertFalse(spirit.isAllowedByTransportFilter(diamond), "an empty whitelist matches nothing");

        spirit.setTagFilter("item:minecraft:diamond");
        assertTrue(spirit.isAllowedByTransportFilter(diamond));
        assertFalse(spirit.isAllowedByTransportFilter(iron));

        spirit.setFilterBlacklist(true);
        assertFalse(spirit.isAllowedByTransportFilter(diamond));
        assertTrue(spirit.isAllowedByTransportFilter(iron));
    }

    @Test
    void onePointTwelveOreDictionaryAliasesApproximateCommonTags() {
        OreDictionary.registerOre("oreSpiritTransporterContract", new ItemStack(Items.DIAMOND));
        OreDictionary.registerOre("logSpiritTransporterContract", new ItemStack(Items.STICK));

        EntityFoliotJob spirit = new EntityFoliotJob(null, SpiritJobType.TRANSPORT_ITEMS);
        spirit.setTagFilter("c:ores");
        assertTrue(spirit.isAllowedByTransportFilter(new ItemStack(Items.DIAMOND)));

        spirit.setTagFilter("*logs*");
        assertTrue(spirit.isAllowedByTransportFilter(new ItemStack(Items.STICK)));
    }

    @Test
    void filterItemsAreSingleSlotGhostStacksAndRoundTripThroughNbt() {
        EntityFoliotJob source = new EntityFoliotJob(null, SpiritJobType.TRANSPORT_ITEMS);
        source.setFilterBlacklist(true);
        source.setTagFilter("item:minecraft:diamond");
        source.getFilterItems().setStackInSlot(0, new ItemStack(Items.DIAMOND, 64));
        assertEquals(1, source.getFilterItems().getStackInSlot(0).getCount());

        NBTTagCompound saved = new NBTTagCompound();
        source.writeEntityToNBT(saved);

        EntityFoliotJob decoded = new EntityFoliotJob(null);
        decoded.readEntityFromNBT(saved);
        assertTrue(decoded.isFilterBlacklist());
        assertEquals("item:minecraft:diamond", decoded.getTagFilter());
        assertEquals(1, decoded.getFilterItems().getStackInSlot(0).getCount());
        assertTrue(decoded.isAllowedByTransportFilter(new ItemStack(Items.IRON_INGOT)));
        assertFalse(decoded.isAllowedByTransportFilter(new ItemStack(Items.DIAMOND)));
    }

    @Test
    void oldEntitiesWithoutFilterKeysMigrateToAnEmptyBlacklist() {
        NBTTagCompound legacy = new NBTTagCompound();
        legacy.setInteger("JobType", SpiritJobType.TRANSPORT_ITEMS.ordinal());

        EntityFoliotJob decoded = new EntityFoliotJob(null);
        decoded.readEntityFromNBT(legacy);
        assertTrue(decoded.isFilterBlacklist());
        assertTrue(decoded.isAllowedByTransportFilter(new ItemStack(Items.DIAMOND)));
    }

    @Test
    void filterPacketsRoundTripAndRejectOverlongTagText() {
        ByteBuf buffer = Unpooled.buffer();
        try {
            MessageSetSpiritFilterMode mode = new MessageSetSpiritFilterMode(true, 42);
            mode.toBytes(buffer);
            MessageSetSpiritFilterMode decodedMode = new MessageSetSpiritFilterMode();
            decodedMode.fromBytes(buffer);
            assertTrue(decodedMode.isValid());
            assertTrue(decodedMode.isBlacklist());
            assertEquals(42, decodedMode.getEntityId());

            buffer.clear();
            MessageSetSpiritTagFilter tag = new MessageSetSpiritTagFilter("c:ores;*logs*", 42);
            tag.toBytes(buffer);
            MessageSetSpiritTagFilter decodedTag = new MessageSetSpiritTagFilter();
            decodedTag.fromBytes(buffer);
            assertTrue(decodedTag.isValid());
            assertEquals("c:ores;*logs*", decodedTag.getTagFilter());
            assertEquals(42, decodedTag.getEntityId());

            buffer.clear();
            ByteBufUtils.writeUTF8String(buffer, repeat('x', EntityFoliotJob.MAX_TAG_FILTER_LENGTH + 1));
            buffer.writeInt(42);
            decodedTag.fromBytes(buffer);
            assertFalse(decodedTag.isValid());
        } finally {
            buffer.release();
        }
    }

    @Test
    void filterTexturesAndBilingualKeysArePresent() throws Exception {
        assertTexture("textures/gui/inventory_spirit_transporter.png");
        assertTexture("textures/gui/inventory_spirit_transporter_tagfilter.png");

        String english = readUtf8(ASSET_ROOT.resolve("lang/en_us.lang"));
        String chinese = readUtf8(ASSET_ROOT.resolve("lang/zh_cn.lang"));
        String[] keys = {
                "gui.occultism.spirit.transporter.filter_mode=",
                "gui.occultism.spirit.transporter.filter_mode.blacklist=",
                "gui.occultism.spirit.transporter.filter_mode.whitelist=",
                "gui.occultism.spirit.transporter.tag_filter="
        };
        for (String key : keys) {
            assertTrue(english.contains(key), "missing English key: " + key);
            assertTrue(chinese.contains(key), "missing Chinese key: " + key);
        }
    }

    @Test
    void serverPacketsRequireTheOpenTransporterEntity() throws Exception {
        String mode = readJava("network", "MessageSetSpiritFilterMode.java");
        String tag = readJava("network", "MessageSetSpiritTagFilter.java");
        assertTrue(mode.contains("player.openContainer instanceof ContainerSpiritTransporter"));
        assertTrue(mode.contains("container.canInteractWith(player)"));
        assertTrue(mode.contains("spirit.getEntityId() != entityId"));
        assertTrue(mode.contains("spirit.supportsTransporterFilter()"));
        assertTrue(tag.contains("ByteBufUtils.readUTF8String"));
        assertTrue(tag.contains("MAX_TAG_FILTER_LENGTH"));
        assertTrue(tag.contains("getOpenTransporterSpirit"));
    }

    private static void assertTexture(String relativePath) throws Exception {
        Path path = ASSET_ROOT.resolve(relativePath);
        BufferedImage image = ImageIO.read(path.toFile());
        assertNotNull(image);
        assertEquals(256, image.getWidth());
        assertEquals(256, image.getHeight());
    }

    private static String readJava(String... path) throws Exception {
        Path source = JAVA_ROOT;
        for (String component : path) {
            source = source.resolve(component);
        }
        return readUtf8(source);
    }

    private static String readUtf8(Path path) throws Exception {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    private static String repeat(char value, int length) {
        char[] chars = new char[length];
        java.util.Arrays.fill(chars, value);
        return new String(chars);
    }
}
