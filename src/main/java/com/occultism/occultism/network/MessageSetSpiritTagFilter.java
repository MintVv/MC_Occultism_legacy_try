package com.occultism.occultism.network;

import java.nio.charset.StandardCharsets;

import com.occultism.occultism.entity.EntityFoliotJob;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/** Updates the semicolon-separated tag filter when the transporter GUI closes. */
public class MessageSetSpiritTagFilter implements IMessage {

    public static final int MAX_UTF8_BYTES = EntityFoliotJob.MAX_TAG_FILTER_LENGTH * 4;

    private String tagFilter;
    private int entityId;
    private boolean valid;

    public MessageSetSpiritTagFilter() {
        this.tagFilter = "";
        this.entityId = -1;
        this.valid = false;
    }

    public MessageSetSpiritTagFilter(String tagFilter, int entityId) {
        this.tagFilter = EntityFoliotJob.normalizeTagFilter(tagFilter);
        this.entityId = entityId;
        this.valid = entityId >= 0 && isBounded(this.tagFilter);
    }

    public String getTagFilter() {
        return this.tagFilter;
    }

    public int getEntityId() {
        return this.entityId;
    }

    public boolean isValid() {
        return this.valid && this.entityId >= 0 && isBounded(this.tagFilter);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.tagFilter = "";
        this.entityId = -1;
        this.valid = false;
        try {
            if (buf == null || buf.readableBytes() < 5 || buf.readableBytes() > MAX_UTF8_BYTES + 8) {
                return;
            }
            String decoded = ByteBufUtils.readUTF8String(buf);
            if (decoded == null || !isBounded(decoded) || buf.readableBytes() < 4) {
                return;
            }
            this.entityId = buf.readInt();
            this.tagFilter = EntityFoliotJob.normalizeTagFilter(decoded);
            this.valid = this.entityId >= 0 && isBounded(this.tagFilter);
        } catch (RuntimeException ignored) {
            this.tagFilter = "";
            this.entityId = -1;
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        if (!this.isValid()) {
            throw new IllegalStateException("Invalid spirit tag-filter payload");
        }
        ByteBufUtils.writeUTF8String(buf, this.tagFilter);
        buf.writeInt(this.entityId);
    }

    private static boolean isBounded(String value) {
        return value != null && value.length() <= EntityFoliotJob.MAX_TAG_FILTER_LENGTH
                && value.getBytes(StandardCharsets.UTF_8).length <= MAX_UTF8_BYTES;
    }

    public static class Handler implements IMessageHandler<MessageSetSpiritTagFilter, IMessage> {

        @Override
        public IMessage onMessage(final MessageSetSpiritTagFilter message, MessageContext context) {
            if (context.side != Side.SERVER || context.getServerHandler() == null
                    || message == null || !message.isValid()) {
                return null;
            }
            final EntityPlayerMP player = context.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> applyOnServer(message, player));
            return null;
        }

        private static void applyOnServer(MessageSetSpiritTagFilter message, EntityPlayerMP player) {
            EntityFoliotJob spirit = MessageSetSpiritFilterMode.getOpenTransporterSpirit(player,
                    message.getEntityId());
            if (spirit == null) {
                return;
            }
            spirit.setTagFilter(message.getTagFilter());
            player.openContainer.detectAndSendChanges();
        }
    }
}
