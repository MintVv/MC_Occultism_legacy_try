package com.occultism.occultism.network;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import com.occultism.occultism.client.gui.GuiStorageController;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Server-to-client snapshot of the current virtual storage list.
 *
 * <p>The payload is deliberately bounded before allocation and decompression.
 * ItemStack counts are sent as integers because 1.12's NBT Count field is a
 * signed byte while storage controllers support configured super stacks.</p>
 */
public class MessageUpdateStacks implements IMessage {

    public static final int MAX_COMPRESSED_PAYLOAD = 1024 * 1024;
    public static final int MAX_UNCOMPRESSED_PAYLOAD = 8 * 1024 * 1024;
    public static final int MAX_STACK_ENTRIES = 32768;
    public static final int MAX_STACK_COUNT = 16 * 1024 * 1024;

    private static final int DEFAULT_BUFFER_SIZE = 2048;

    private List<ItemStack> stacks = Collections.emptyList();
    private int usedSlots;
    private int maxSlots;
    private byte[] payload = new byte[0];
    private boolean valid = true;

    public MessageUpdateStacks() {
    }

    public MessageUpdateStacks(List<ItemStack> stacks, int usedSlots, int maxSlots) {
        this.stacks = copyStacks(stacks);
        this.usedSlots = clampMetadata(usedSlots);
        this.maxSlots = clampMetadata(maxSlots);
        this.compress();
    }

    public List<ItemStack> getStacks() {
        if (this.valid && this.stacks.isEmpty() && this.payload.length > 0) {
            List<ItemStack> decoded = this.uncompressPayload();
            if (decoded == null) {
                this.valid = false;
                return Collections.emptyList();
            }
            this.stacks = decoded;
        }
        return copyStacks(this.stacks);
    }

    public int getUsedSlots() {
        return this.usedSlots;
    }

    public int getMaxSlots() {
        return this.maxSlots;
    }

    /** Returns false when packet framing or decompression validation failed. */
    public boolean isValid() {
        return this.valid;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.valid = false;
        try {
            this.usedSlots = readBoundedVarInt(buf, MAX_STACK_ENTRIES);
            this.maxSlots = readBoundedVarInt(buf, MAX_STACK_ENTRIES);
            int compressedSize = readBoundedVarInt(buf, MAX_COMPRESSED_PAYLOAD);
            if (compressedSize > buf.readableBytes()) {
                return;
            }
            this.payload = new byte[compressedSize];
            buf.readBytes(this.payload);
            this.valid = true;
        } catch (RuntimeException ex) {
            this.payload = new byte[0];
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        if (!this.valid || this.payload == null || this.payload.length > MAX_COMPRESSED_PAYLOAD) {
            throw new IllegalStateException("Invalid storage stack payload");
        }
        writeVarInt(buf, this.usedSlots);
        writeVarInt(buf, this.maxSlots);
        writeVarInt(buf, this.payload.length);
        buf.writeBytes(this.payload);
    }

    /** Called on the client network thread; GUI mutation is scheduled on the main thread. */
    public static class Handler implements IMessageHandler<MessageUpdateStacks, IMessage> {
        @Override
        public IMessage onMessage(final MessageUpdateStacks message, MessageContext context) {
            if (context.side != Side.CLIENT || message == null || !message.valid) {
                return null;
            }
            Minecraft.getMinecraft().addScheduledTask(() -> {
                List<ItemStack> decoded = message.uncompressPayload();
                if (decoded == null) {
                    return;
                }
                if (Minecraft.getMinecraft().currentScreen instanceof GuiStorageController) {
                    GuiStorageController gui = (GuiStorageController) Minecraft.getMinecraft().currentScreen;
                    gui.applyStorageSnapshot(decoded, message.usedSlots, message.maxSlots);
                }
            });
            return null;
        }
    }

    /** Decodes the compressed payload into the stack list for API-compatible callers. */
    public void uncompress() {
        List<ItemStack> decoded = this.uncompressPayload();
        if (decoded == null) {
            this.valid = false;
        } else {
            this.stacks = decoded;
        }
    }

    private List<ItemStack> uncompressPayload() {
        if (this.payload == null || this.payload.length == 0 || this.payload.length > MAX_COMPRESSED_PAYLOAD) {
            return null;
        }
        Inflater inflater = new Inflater();
        ByteArrayOutputStream output = new ByteArrayOutputStream(Math.min(MAX_UNCOMPRESSED_PAYLOAD,
                Math.max(DEFAULT_BUFFER_SIZE, this.payload.length * 4)));
        try {
            inflater.setInput(this.payload);
            byte[] buffer = new byte[1024];
            int total = 0;
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                if (count > 0) {
                    total += count;
                    if (total > MAX_UNCOMPRESSED_PAYLOAD) {
                        return null;
                    }
                    output.write(buffer, 0, count);
                } else if (inflater.needsDictionary() || inflater.needsInput()) {
                    return null;
                } else {
                    return null;
                }
            }
            byte[] raw = output.toByteArray();
            if (raw.length < 4) {
                return null;
            }
            ByteBuf data = Unpooled.wrappedBuffer(raw);
            int size = data.readInt();
            if (size < 0 || size > MAX_STACK_ENTRIES) {
                return null;
            }
            List<ItemStack> decoded = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                ItemStack stack = ByteBufUtils.readItemStack(data);
                if (stack == null) {
                    stack = ItemStack.EMPTY;
                }
                if (data.readableBytes() < 4) {
                    return null;
                }
                int count = data.readInt();
                if (count < 0 || count > MAX_STACK_COUNT) {
                    return null;
                }
                stack.setCount(count);
                decoded.add(stack);
            }
            if (data.isReadable()) {
                // Reject trailing bytes so a packet cannot smuggle an unbounded second payload.
                return null;
            }
            return decoded;
        } catch (DataFormatException | RuntimeException ex) {
            return null;
        } finally {
            inflater.end();
        }
    }

    /** Rebuilds the bounded compressed payload from the current stack list. */
    public void compress() {
        if (this.stacks.size() > MAX_STACK_ENTRIES) {
            throw new IllegalArgumentException("Too many storage stack entries");
        }
        ByteBuf data = Unpooled.buffer(Math.max(DEFAULT_BUFFER_SIZE, DEFAULT_BUFFER_SIZE * this.stacks.size()));
        try {
            data.writeInt(this.stacks.size());
            for (ItemStack stack : this.stacks) {
                ByteBufUtils.writeItemStack(data, stack == null ? ItemStack.EMPTY : stack);
                data.writeInt(stack == null ? 0 : stack.getCount());
            }
            byte[] raw = new byte[data.readableBytes()];
            data.getBytes(data.readerIndex(), raw);

            Deflater deflater = new Deflater(Deflater.BEST_SPEED);
            ByteArrayOutputStream output = new ByteArrayOutputStream(DEFAULT_BUFFER_SIZE);
            try {
                deflater.setInput(raw);
                deflater.finish();
                byte[] buffer = new byte[1024];
                while (!deflater.finished()) {
                    int count = deflater.deflate(buffer);
                    if (count <= 0) {
                        throw new IllegalStateException("Unable to compress storage stack payload");
                    }
                    output.write(buffer, 0, count);
                    if (output.size() > MAX_COMPRESSED_PAYLOAD) {
                        throw new IllegalArgumentException("Storage stack payload is too large");
                    }
                }
            } finally {
                deflater.end();
            }
            this.payload = output.toByteArray();
            this.valid = true;
        } finally {
            data.release();
        }
    }

    private static List<ItemStack> copyStacks(List<ItemStack> source) {
        List<ItemStack> result = new ArrayList<>();
        if (source != null) {
            for (ItemStack stack : source) {
                result.add(stack == null ? ItemStack.EMPTY : stack.copy());
            }
        }
        return result;
    }

    private static int clampMetadata(int value) {
        return Math.max(0, Math.min(MAX_STACK_ENTRIES, value));
    }

    private static void writeVarInt(ByteBuf buf, int value) {
        int remaining = Math.max(0, value);
        do {
            int next = remaining & 0x7F;
            remaining >>>= 7;
            if (remaining != 0) {
                next |= 0x80;
            }
            buf.writeByte(next);
        } while (remaining != 0);
    }

    private static int readBoundedVarInt(ByteBuf buf, int max) {
        int result = 0;
        int shift = 0;
        for (int i = 0; i < 5; i++) {
            if (!buf.isReadable()) {
                throw new IllegalArgumentException("Truncated storage packet");
            }
            int value = buf.readUnsignedByte();
            result |= (value & 0x7F) << shift;
            if ((value & 0x80) == 0) {
                if (result < 0 || result > max) {
                    throw new IllegalArgumentException("Storage packet integer out of bounds");
                }
                return result;
            }
            shift += 7;
        }
        throw new IllegalArgumentException("Storage packet VarInt is too long");
    }
}
