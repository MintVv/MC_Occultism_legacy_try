package com.occultism.occultism.network;

import com.occultism.occultism.item.BookOfCallingMode;
import com.occultism.occultism.item.ItemBookOfCallingFoliotJob;
import com.occultism.occultism.item.WorkAreaSize;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageSetBookOfCallingConfig implements IMessage {

    private int slot;
    private int mode;
    private int workAreaSize;

    public MessageSetBookOfCallingConfig() {
    }

    public MessageSetBookOfCallingConfig(int slot, BookOfCallingMode mode, WorkAreaSize workAreaSize) {
        this.slot = slot;
        this.mode = mode.ordinal();
        this.workAreaSize = workAreaSize.ordinal();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.slot = buf.readInt();
        this.mode = buf.readInt();
        this.workAreaSize = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.slot);
        buf.writeInt(this.mode);
        buf.writeInt(this.workAreaSize);
    }

    public static class Handler implements IMessageHandler<MessageSetBookOfCallingConfig, IMessage> {

        @Override
        public IMessage onMessage(MessageSetBookOfCallingConfig message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                if (message.slot < 0 || message.slot >= player.inventory.getSizeInventory()) {
                    return;
                }
                ItemStack stack = player.inventory.getStackInSlot(message.slot);
                if (!(stack.getItem() instanceof ItemBookOfCallingFoliotJob)) {
                    return;
                }
                ItemBookOfCallingFoliotJob book = (ItemBookOfCallingFoliotJob) stack.getItem();
                book.setBookConfiguration(stack, BookOfCallingMode.byOrdinal(message.mode),
                        WorkAreaSize.byOrdinal(message.workAreaSize), player.getEntityWorld());
                player.inventory.setInventorySlotContents(message.slot, stack);
                player.inventoryContainer.detectAndSendChanges();
            });
            return null;
        }
    }
}
