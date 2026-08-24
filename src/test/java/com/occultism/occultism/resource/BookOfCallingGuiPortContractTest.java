package com.occultism.occultism.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import com.occultism.occultism.item.BookOfCallingMode;
import com.occultism.occultism.item.WorkAreaSize;
import com.occultism.occultism.network.MessageSetBookOfCallingMode;
import com.occultism.occultism.network.MessageSetBookOfCallingWorkAreaSize;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

class BookOfCallingGuiPortContractTest {

    @Test
    void modeValuesRemainCompatibleWithTheUpstreamCallingItem() {
        assertEquals(0, BookOfCallingMode.SET_DEPOSIT.getValue());
        assertEquals(1, BookOfCallingMode.SET_EXTRACT.getValue());
        assertEquals(2, BookOfCallingMode.SET_BASE.getValue());
        assertEquals(3, BookOfCallingMode.SET_STORAGE_CONTROLLER.getValue());
        assertEquals(4, BookOfCallingMode.SET_MANAGED_MACHINE.getValue());

        for (BookOfCallingMode mode : BookOfCallingMode.values()) {
            assertSame(mode, BookOfCallingMode.fromValue(mode.getValue()));
        }
        assertNull(BookOfCallingMode.fromValue(-1));
        assertNull(BookOfCallingMode.fromValue(5));
    }

    @Test
    void workAreaValuesReadBothStableAndLegacyOnePointTwelveData() {
        assertEquals(16, WorkAreaSize.SMALL.getValue());
        assertEquals(32, WorkAreaSize.MEDIUM.getValue());
        assertEquals(64, WorkAreaSize.LARGE.getValue());

        assertSame(WorkAreaSize.SMALL, WorkAreaSize.fromValue(16));
        assertSame(WorkAreaSize.MEDIUM, WorkAreaSize.fromValue(32));
        assertSame(WorkAreaSize.LARGE, WorkAreaSize.fromValue(64));
        assertSame(WorkAreaSize.SMALL, WorkAreaSize.fromPersistedValue(0));
        assertSame(WorkAreaSize.MEDIUM, WorkAreaSize.fromPersistedValue(1));
        assertSame(WorkAreaSize.LARGE, WorkAreaSize.fromPersistedValue(2));
        assertSame(WorkAreaSize.SMALL, WorkAreaSize.fromPersistedValue(99));
    }

    @Test
    void immediateModePacketUsesStableValueAndRoundTrips() {
        MessageSetBookOfCallingMode encoded =
                new MessageSetBookOfCallingMode(17, BookOfCallingMode.SET_BASE);
        ByteBuf buffer = Unpooled.buffer();
        try {
            encoded.toBytes(buffer);
            assertEquals(17, buffer.readInt());
            assertEquals(2, buffer.readInt());

            MessageSetBookOfCallingMode decoded = new MessageSetBookOfCallingMode();
            buffer.clear();
            encoded.toBytes(buffer);
            decoded.fromBytes(buffer);
            assertEquals(17, decoded.getSlot());
            assertSame(BookOfCallingMode.SET_BASE, decoded.getMode());
        } finally {
            buffer.release();
        }
    }

    @Test
    void immediateWorkAreaPacketUsesStableValueAndRejectsOrdinalPayloads() {
        MessageSetBookOfCallingWorkAreaSize encoded =
                new MessageSetBookOfCallingWorkAreaSize(4, WorkAreaSize.LARGE);
        ByteBuf buffer = Unpooled.buffer();
        try {
            encoded.toBytes(buffer);
            assertEquals(4, buffer.readInt());
            assertEquals(64, buffer.readInt());

            buffer.clear();
            buffer.writeInt(4);
            buffer.writeInt(32);
            MessageSetBookOfCallingWorkAreaSize decoded = new MessageSetBookOfCallingWorkAreaSize();
            decoded.fromBytes(buffer);
            assertEquals(4, decoded.getSlot());
            assertSame(WorkAreaSize.MEDIUM, decoded.getWorkAreaSize());

            buffer.clear();
            buffer.writeInt(4);
            buffer.writeInt(1);
            decoded.fromBytes(buffer);
            assertNull(decoded.getWorkAreaSize(), "Network payloads must use 16/32/64, not local ordinals");
        } finally {
            buffer.release();
        }
    }

    @Test
    void guiMatchesTheUpstreamOnePointFifteenInteractionContract() throws Exception {
        String source = readSource("src", "main", "java", "com", "occultism", "occultism", "client", "gui",
                "GuiBookOfCalling.java");
        assertTrue(source.contains("GUI_HEIGHT = 166"));
        assertTrue(source.contains("BUTTON_WIDTH = 150"));
        assertTrue(source.contains("BUTTON_HEIGHT = 20"));
        assertTrue(source.contains("top + 60"));
        assertTrue(source.contains("top + 85"));
        assertTrue(source.contains("top + (showSize ? 110 : 85)"));
        assertTrue(source.contains("selectedMode.hasSize()"));
        assertTrue(source.contains("new MessageSetBookOfCallingMode"));
        assertTrue(source.contains("new MessageSetBookOfCallingWorkAreaSize"));
        assertTrue(source.contains("extends GuiContainer"));
        assertTrue(source.contains("super(new ContainerBookOfCalling(slot))"));
        assertTrue(source.contains("this.mc.player.closeScreen()"));
        assertTrue(source.contains("drawGuiContainerBackgroundLayer"));
        assertTrue(source.contains("BUTTON_EXIT"));
        assertTrue(!source.contains("BUTTON_CONFIRM"));
        assertTrue(!source.contains("MessageSetBookOfCallingConfig"));
    }

    @Test
    void serverConfigurationRequiresTheBookContainerAndSupportedMode() throws Exception {
        String container = readSource("src", "main", "java", "com", "occultism", "occultism", "container",
                "ContainerBookOfCalling.java");
        String handler = readSource("src", "main", "java", "com", "occultism", "occultism", "network",
                "MessageSetBookOfCallingConfig.java");
        String guiHandler = readSource("src", "main", "java", "com", "occultism", "occultism", "gui",
                "ModGuiHandler.java");
        assertTrue(container.contains("private final int bookSlot"));
        assertTrue(container.contains("isBookSlot"));
        assertTrue(handler.contains("player.openContainer instanceof ContainerBookOfCalling"));
        assertTrue(handler.contains("container.canInteractWith(player)"));
        assertTrue(handler.contains("book.supportsMode(mode)"));
        assertTrue(handler.contains("player.inventoryContainer.detectAndSendChanges()"));
        assertTrue(guiHandler.contains("new ContainerBookOfCalling(x)"));
    }

    @Test
    void managedMachineGuiMatchesUpstreamLayoutAndSendTiming() throws Exception {
        String gui = readSource("src", "main", "java", "com", "occultism", "occultism", "client", "gui",
                "GuiBookOfCallingManagedMachine.java");
        String book = readSource("src", "main", "java", "com", "occultism", "occultism", "item",
                "ItemBookOfCallingFoliotJob.java");
        String message = readSource("src", "main", "java", "com", "occultism", "occultism", "network",
                "MessageSetManagedMachine.java");
        String english = readSource("src", "main", "resources", "assets", "occultism", "lang", "en_us.lang");
        String chinese = readSource("src", "main", "resources", "assets", "occultism", "lang", "zh_cn.lang");

        assertTrue(gui.contains("GUI_HEIGHT = 166"));
        assertTrue(gui.contains("BUTTON_WIDTH = 150"));
        assertTrue(gui.contains("BUTTON_HEIGHT = 20"));
        assertTrue(gui.contains("BUTTON_MARGIN = 5"));
        assertTrue(gui.contains("BUTTON_TOP = 60"));
        assertTrue(gui.contains("setMaxStringLength(30)"));
        assertTrue(gui.contains("BUTTON_INSERT_FACING"));
        assertTrue(gui.contains("BUTTON_EXTRACT_FACING"));
        assertTrue(gui.contains("ModNetworking.sendManagedMachine(this.makeMachineReference())"));
        assertTrue(gui.contains("!StringUtils.isBlank(this.customName)"));
        assertTrue(gui.contains("!this.customName.equals(this.originalCustomName)"));
        assertTrue(gui.contains("doesGuiPauseGame()"));
        assertTrue(gui.contains("return false"));

        assertTrue(book.contains("mode == BookOfCallingMode.SET_MANAGED_MACHINE"));
        assertTrue(book.contains("hasItemHandlerOnAnySide(tile)"));
        assertTrue(book.contains("openManagedMachineGui(stack)"));
        assertTrue(book.contains("ClientBookOfCallingGuiHelper.openManagedMachine"));
        assertTrue(book.contains("ItemNBTUtil.getManagedMachine(stack)"));
        assertTrue(book.contains("boolean sameMachine"));
        assertTrue(book.contains("reference.getInsertFacing()"));
        assertTrue(book.contains("reference.getExtractFacing()"));
        assertTrue(message.contains("trusted.getInsertPosition(), trusted.getInsertDimension()"));
        assertTrue(message.contains("requested.getInsertFacing()"));
        assertTrue(message.contains("trusted.getExtractPosition(), trusted.getExtractDimension()"));
        assertTrue(message.contains("requested.getExtractFacing()"));
        assertTrue(!message.contains("sameEndpoints"));

        assertTrue(english.contains("enum.occultism.facing.up=Up"));
        assertTrue(english.contains("gui.occultism.book_of_calling.manage_machine.custom_name=Custom Name"));
        assertTrue(chinese.contains("enum.occultism.facing.up=上"));
        assertTrue(chinese.contains("gui.occultism.book_of_calling.manage_machine.custom_name=自定义名称"));
    }

    private static String readSource(String... path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path[0], java.util.Arrays.copyOfRange(path, 1, path.length))),
                StandardCharsets.UTF_8);
    }
}
