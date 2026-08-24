package com.occultism.occultism.network;

import com.occultism.occultism.item.WorkAreaSize;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/** Immediate work-area update sent by the 1.12 Book of Calling screen. */
public class MessageSetBookOfCallingWorkAreaSize implements IMessage {

    private int slot;
    private int workAreaSize;

    public MessageSetBookOfCallingWorkAreaSize() {
    }

    public MessageSetBookOfCallingWorkAreaSize(int slot, WorkAreaSize workAreaSize) {
        this.slot = slot;
        this.workAreaSize = workAreaSize == null ? -1 : workAreaSize.getValue();
    }

    public int getSlot() {
        return this.slot;
    }

    public WorkAreaSize getWorkAreaSize() {
        return WorkAreaSize.fromValue(this.workAreaSize);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.slot = buf.readInt();
        this.workAreaSize = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.slot);
        buf.writeInt(this.workAreaSize);
    }

    public static class Handler implements IMessageHandler<MessageSetBookOfCallingWorkAreaSize, IMessage> {

        @Override
        public IMessage onMessage(MessageSetBookOfCallingWorkAreaSize message, MessageContext context) {
            if (context.side != Side.SERVER || message == null || message.getWorkAreaSize() == null) {
                return null;
            }
            EntityPlayerMP player = context.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> MessageSetBookOfCallingConfig.applyWorkAreaSize(player,
                    message.slot, message.getWorkAreaSize()));
            return null;
        }
    }
}
