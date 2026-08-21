package com.occultism.occultism.network;

import com.occultism.occultism.container.ContainerStorageController;
import com.occultism.occultism.tileentity.TileEntityStorageController;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/** Client request for a fresh snapshot of the currently opened controller. */
public class MessageRequestStacks implements IMessage {

    @Override
    public void fromBytes(ByteBuf buf) {
        // The request intentionally has no block position or client supplied data.
    }

    @Override
    public void toBytes(ByteBuf buf) {
        // The request intentionally has no block position or client supplied data.
    }

    public static class Handler implements IMessageHandler<MessageRequestStacks, IMessage> {
        @Override
        public IMessage onMessage(final MessageRequestStacks message, MessageContext context) {
            if (context.side != Side.SERVER || context.getServerHandler() == null) {
                return null;
            }
            final EntityPlayerMP player = context.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> sendSnapshot(player));
            return null;
        }

        private static void sendSnapshot(EntityPlayerMP player) {
            if (player == null || !(player.openContainer instanceof ContainerStorageController)) {
                return;
            }
            ContainerStorageController container = (ContainerStorageController) player.openContainer;
            if (!container.canInteractWith(player)) {
                return;
            }
            TileEntityStorageController controller = container.getStorageController();
            if (controller == null || controller.isInvalid()) {
                return;
            }

            // Use the same controller-owned cache as all mutation responses.
            // This also keeps a remote/proxy container from observing a
            // separately-built snapshot between validation and send.
            ModNetworking.CHANNEL.sendTo(controller.getMessageUpdateStacks(), player);
        }
    }
}
