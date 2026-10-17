package com.occultism.occultism.network;

import static org.junit.jupiter.api.Assertions.*;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import org.junit.jupiter.api.Test;

class StorageRecipeIdNetworkTest {
    @Test
    void recipeIdentityRoundTripsWithoutCountsOrItemTemplates() {
        ResourceLocation id = new ResourceLocation("occultism:storage/crafting_test");
        ByteBuf buffer = Unpooled.buffer();
        try {
            new MessageSetRecipeByID(id).toBytes(buffer);
            MessageSetRecipeByID decoded = new MessageSetRecipeByID();
            decoded.fromBytes(buffer);
            assertTrue(decoded.isValid());
            assertEquals(id, decoded.getRecipeId());
            assertFalse(buffer.isReadable());
        } finally {
            buffer.release();
        }
    }

    @Test
    void malformedTruncatedOversizedAndTrailingBytesAreRejected() {
        assertInvalid(new byte[0]);
        assertInvalid(new byte[] {(byte) 0x80});
        assertInvalid(new byte[] {5, 'a'});
        assertInvalid(new byte[MessageSetRecipeByID.MAX_ID_LENGTH + 6]);
        assertInvalidString("Minecraft:stone", false);
        assertInvalidString("minecraft:STONE", false);
        assertInvalidString("minecraft:has space", false);
        assertInvalidString("minecraft:", false);
        assertInvalidString("minecraft:stone", true);
        assertInvalidString("minecraft:" + String.join("", java.util.Collections.nCopies(257, "a")), false);
    }

    @Test
    void reusedDecoderClearsPreviousValidIdOnFailure() {
        MessageSetRecipeByID message = new MessageSetRecipeByID(new ResourceLocation("minecraft:stone"));
        ByteBuf empty = Unpooled.buffer();
        try {
            message.fromBytes(empty);
            assertFalse(message.isValid());
            assertNull(message.getRecipeId());
            assertThrows(IllegalStateException.class, () -> message.toBytes(empty));
            assertFalse(new MessageSetRecipeByID(null).isValid());
        } finally {
            empty.release();
        }
    }

    private static void assertInvalidString(String value, boolean trailing) {
        ByteBuf buffer = Unpooled.buffer();
        try {
            new PacketBuffer(buffer).writeString(value);
            if (trailing) {
                buffer.writeByte(1);
            }
            MessageSetRecipeByID decoded = new MessageSetRecipeByID();
            decoded.fromBytes(buffer);
            assertFalse(decoded.isValid(), value);
        } finally {
            buffer.release();
        }
    }

    private static void assertInvalid(byte[] bytes) {
        ByteBuf buffer = Unpooled.wrappedBuffer(bytes);
        try {
            MessageSetRecipeByID decoded = new MessageSetRecipeByID();
            decoded.fromBytes(buffer);
            assertFalse(decoded.isValid());
        } finally {
            buffer.release();
        }
    }
}
