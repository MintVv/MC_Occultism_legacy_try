package com.occultism.occultism.network;

import com.occultism.occultism.container.ContainerStorageController;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/** Requests server-owned ingredients for one registered crafting recipe, never client-owned item counts. */
public final class MessageSetRecipeByID implements IMessage {
    public static final int MAX_ID_LENGTH = 256;
    private ResourceLocation recipeId;

    public MessageSetRecipeByID() {
    }

    public MessageSetRecipeByID(ResourceLocation recipeId) {
        this.recipeId = recipeId != null && isValidId(recipeId.toString()) ? recipeId : null;
    }

    public ResourceLocation getRecipeId() {
        return this.recipeId;
    }

    public boolean isValid() {
        return this.recipeId != null;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.recipeId = null;
        try {
            if (buf.readableBytes() > MAX_ID_LENGTH + 5) {
                return;
            }
            String id = new PacketBuffer(buf).readString(MAX_ID_LENGTH);
            if (buf.isReadable() || !isValidId(id)) {
                return;
            }
            this.recipeId = new ResourceLocation(id);
        } catch (RuntimeException ignored) {
            // Malformed, truncated and oversized IDs must not schedule a server mutation.
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        if (!this.isValid()) {
            throw new IllegalStateException("Invalid storage crafting recipe ID");
        }
        new PacketBuffer(buf).writeString(this.recipeId.toString());
    }

    private static boolean isValidId(String id) {
        return id.length() <= MAX_ID_LENGTH && id.matches("[a-z0-9_.-]+:[a-z0-9_./-]+");
    }

    public static final class Handler implements IMessageHandler<MessageSetRecipeByID, IMessage> {
        @Override
        public IMessage onMessage(MessageSetRecipeByID message, MessageContext context) {
            if (message == null || !message.isValid() || context.side != Side.SERVER
                    || context.getServerHandler() == null) {
                return null;
            }
            EntityPlayerMP player = context.getServerHandler().player;
            ResourceLocation recipeId = message.getRecipeId();
            player.getServerWorld().addScheduledTask(() -> {
                if (player.openContainer instanceof ContainerStorageController) {
                    ((ContainerStorageController) player.openContainer).setRecipeById(recipeId, player);
                }
            });
            return null;
        }
    }
}
