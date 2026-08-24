package com.occultism.occultism.network;

import com.occultism.occultism.container.ContainerSpiritTransporter;
import com.occultism.occultism.entity.EntityFoliotJob;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/** Updates the transporter filter mode for the spirit in the open container. */
public class MessageSetSpiritFilterMode implements IMessage {

    private boolean blacklist;
    private int entityId;
    private boolean valid;

    public MessageSetSpiritFilterMode() {
        this.valid = false;
    }

    public MessageSetSpiritFilterMode(boolean blacklist, int entityId) {
        this.blacklist = blacklist;
        this.entityId = entityId;
        this.valid = entityId >= 0;
    }

    public boolean isBlacklist() {
        return this.blacklist;
    }

    public int getEntityId() {
        return this.entityId;
    }

    public boolean isValid() {
        return this.valid && this.entityId >= 0;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.valid = false;
        try {
            if (buf == null || buf.readableBytes() < 5) {
                return;
            }
            this.blacklist = buf.readBoolean();
            this.entityId = buf.readInt();
            this.valid = this.entityId >= 0;
        } catch (RuntimeException ignored) {
            this.entityId = -1;
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        if (!this.isValid()) {
            throw new IllegalStateException("Invalid spirit filter-mode payload");
        }
        buf.writeBoolean(this.blacklist);
        buf.writeInt(this.entityId);
    }

    public static class Handler implements IMessageHandler<MessageSetSpiritFilterMode, IMessage> {

        @Override
        public IMessage onMessage(final MessageSetSpiritFilterMode message, MessageContext context) {
            if (context.side != Side.SERVER || context.getServerHandler() == null
                    || message == null || !message.isValid()) {
                return null;
            }
            final EntityPlayerMP player = context.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> applyOnServer(message, player));
            return null;
        }

        private static void applyOnServer(MessageSetSpiritFilterMode message, EntityPlayerMP player) {
            EntityFoliotJob spirit = getOpenTransporterSpirit(player, message.getEntityId());
            if (spirit == null) {
                return;
            }
            spirit.setFilterBlacklist(message.isBlacklist());
            player.openContainer.detectAndSendChanges();
        }
    }

    /** Shared validation for packets that mutate the open spirit's filter state. */
    static EntityFoliotJob getOpenTransporterSpirit(EntityPlayerMP player, int entityId) {
        if (player == null || !(player.openContainer instanceof ContainerSpiritTransporter)) {
            return null;
        }
        ContainerSpiritTransporter container = (ContainerSpiritTransporter) player.openContainer;
        if (!container.canInteractWith(player)) {
            return null;
        }
        EntityFoliotJob spirit = container.getSpirit();
        if (spirit == null || !spirit.isEntityAlive() || !spirit.supportsTransporterFilter()
                || spirit.getEntityId() != entityId || player.world.getEntityByID(entityId) != spirit) {
            return null;
        }
        return spirit;
    }
}
