package com.occultism.occultism.network;

import com.occultism.occultism.item.BookOfCallingMode;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/** Immediate mode update sent by the 1.12 Book of Calling screen. */
public class MessageSetBookOfCallingMode implements IMessage {

    private int slot;
    private int mode;

    public MessageSetBookOfCallingMode() {
    }

    public MessageSetBookOfCallingMode(int slot, BookOfCallingMode mode) {
        this.slot = slot;
        this.mode = mode == null ? -1 : mode.getValue();
    }

    public int getSlot() {
        return this.slot;
    }

    public BookOfCallingMode getMode() {
        return BookOfCallingMode.fromValue(this.mode);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.slot = buf.readInt();
        this.mode = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.slot);
        buf.writeInt(this.mode);
    }

    public static class Handler implements IMessageHandler<MessageSetBookOfCallingMode, IMessage> {

        @Override
        public IMessage onMessage(MessageSetBookOfCallingMode message, MessageContext context) {
            if (context.side != Side.SERVER || message == null || message.getMode() == null) {
                return null;
            }
            EntityPlayerMP player = context.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() ->
                    MessageSetBookOfCallingConfig.applyMode(player, message.slot, message.getMode()));
            return null;
        }
    }
}
