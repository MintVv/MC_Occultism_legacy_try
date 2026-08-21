package com.occultism.occultism.network;

import com.occultism.occultism.container.ContainerStorageController;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Transfers a 3x3 recipe template into the open storage terminal.
 *
 * <p>The payload follows the upstream format ({@code s0} ... {@code s8}
 * lists of alternative ItemStack compounds). Counts in the payload are ignored
 * by the server; one item is taken from the player's inventory or controller
 * for every matrix cell.</p>
 */
public class MessageSetRecipe implements IMessage {

    public static final int MAX_NBT_BYTES = 64 * 1024;

    private NBTTagCompound payload = new NBTTagCompound();
    private boolean valid = true;

    public MessageSetRecipe() {
    }

    public MessageSetRecipe(NBTTagCompound payload) {
        this.payload = payload == null ? new NBTTagCompound() : payload.copy();
        this.valid = isBoundedPayload(this.payload);
    }

    public boolean isValid() {
        return this.valid;
    }

    public NBTTagCompound getPayload() {
        return this.payload == null ? new NBTTagCompound() : this.payload.copy();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.valid = false;
        try {
            if (buf.readableBytes() > MAX_NBT_BYTES) {
                return;
            }
            NBTTagCompound decoded = ByteBufUtils.readTag(buf);
            if (decoded != null && isBoundedPayload(decoded)) {
                this.payload = decoded;
                this.valid = true;
            }
        } catch (RuntimeException ignored) {
            this.payload = new NBTTagCompound();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        if (!this.valid || this.payload == null || !isBoundedPayload(this.payload)) {
            throw new IllegalStateException("Invalid storage recipe payload");
        }
        ByteBufUtils.writeTag(buf, this.payload);
    }

    private static boolean isBoundedPayload(NBTTagCompound payload) {
        if (payload == null || payload.getKeySet().size() > 9) {
            return false;
        }
        for (String key : payload.getKeySet()) {
            if (key == null || !key.matches("s[0-8]") || !payload.hasKey(key, 9)) {
                return false;
            }
            if (payload.getTagList(key, 10).tagCount() > 16) {
                return false;
            }
        }
        return true;
    }

    public static class Handler implements IMessageHandler<MessageSetRecipe, IMessage> {

        @Override
        public IMessage onMessage(final MessageSetRecipe message, MessageContext context) {
            if (context.side != Side.SERVER || context.getServerHandler() == null || message == null
                    || !message.valid) {
                return null;
            }
            final EntityPlayerMP player = context.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                if (!(player.openContainer instanceof ContainerStorageController)) {
                    return;
                }
                ContainerStorageController container = (ContainerStorageController) player.openContainer;
                container.setRecipeFromNBT(message.payload, player);
            });
            return null;
        }
    }
}
