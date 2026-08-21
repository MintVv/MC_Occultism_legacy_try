package com.occultism.occultism.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Contract tests for the bounded compressed storage snapshot protocol. */
class StorageStacksNetworkContractTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void compressedPayloadRoundTripsSuperStackCountsAndMetadata() {
        List<ItemStack> stacks = Arrays.asList(new ItemStack(Items.DIAMOND, 300),
                new ItemStack(Items.ENDER_PEARL, 16));
        MessageUpdateStacks encoded = new MessageUpdateStacks(stacks, 2, 256);
        ByteBuf buffer = Unpooled.buffer();
        try {
            encoded.toBytes(buffer);
            MessageUpdateStacks decoded = new MessageUpdateStacks();
            decoded.fromBytes(buffer);
            assertTrue(decoded.isValid());
            assertEquals(2, decoded.getUsedSlots());
            assertEquals(256, decoded.getMaxSlots());
            assertEquals(2, decoded.getStacks().size());
            assertEquals(300, decoded.getStacks().get(0).getCount());
        } finally {
            buffer.release();
        }
    }

    @Test
    void malformedCompressedLengthIsRejectedBeforeAllocation() {
        ByteBuf buffer = Unpooled.buffer();
        try {
            buffer.writeByte(1); // used slots
            buffer.writeByte(1); // max slots
            writeVarInt(buffer, MessageUpdateStacks.MAX_COMPRESSED_PAYLOAD + 1);
            MessageUpdateStacks decoded = new MessageUpdateStacks();
            decoded.fromBytes(buffer);
            assertFalse(decoded.isValid());
        } finally {
            buffer.release();
        }
    }

    @Test
    void registrationAndClientCacheBoundariesArePresent() throws Exception {
        String networking = readSource("src", "main", "java", "com", "occultism", "occultism", "network",
                "ModNetworking.java");
        String request = readSource("src", "main", "java", "com", "occultism", "occultism", "network",
                "MessageRequestStacks.java");
        String update = readSource("src", "main", "java", "com", "occultism", "occultism", "network",
                "MessageUpdateStacks.java");
        String gui = readSource("src", "main", "java", "com", "occultism", "occultism", "client", "gui",
                "GuiStorageController.java");

        assertTrue(networking.contains("MessageRequestStacks.Handler.class"));
        assertTrue(networking.contains("MessageUpdateStacks.Handler.class"));
        assertTrue(networking.contains("controller.getMessageUpdateStacks()"),
                "server sends must use the controller-owned snapshot cache");
        assertTrue(request.contains("container.canInteractWith(player)"));
        assertTrue(request.contains("controller.getMessageUpdateStacks()"),
                "request responses must share the controller cache");
        assertTrue(update.contains("MAX_UNCOMPRESSED_PAYLOAD"));
        assertTrue(update.contains("MAX_COMPRESSED_PAYLOAD"));
        assertTrue(gui.contains("requestStorageStacks()"));
        assertTrue(gui.contains("applyStorageSnapshot"));
    }

    private static String readSource(String... path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path[0], java.util.Arrays.copyOfRange(path, 1, path.length))),
                StandardCharsets.UTF_8);
    }

    private static void writeVarInt(ByteBuf buffer, int value) {
        int remaining = value;
        while ((remaining & ~0x7F) != 0) {
            buffer.writeByte((remaining & 0x7F) | 0x80);
            remaining >>>= 7;
        }
        buffer.writeByte(remaining);
    }
}
