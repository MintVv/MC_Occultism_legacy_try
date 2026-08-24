package com.occultism.occultism.network;

import com.occultism.occultism.item.BookOfCallingMode;
import com.occultism.occultism.container.ContainerBookOfCalling;
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
        // Retain the original 1.12 packet format. The replacement packets use
        // the upstream stable enum values.
        this.mode = mode == null ? -1 : mode.ordinal();
        this.workAreaSize = workAreaSize == null ? -1 : workAreaSize.ordinal();
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
            if (ctx.side != net.minecraftforge.fml.relauncher.Side.SERVER || message == null) {
                return null;
            }
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                if (message.mode < 0 || message.mode >= BookOfCallingMode.values().length
                        || message.workAreaSize < 0 || message.workAreaSize >= WorkAreaSize.values().length) {
                    return;
                }
                applyConfiguration(player, message.slot, BookOfCallingMode.byOrdinal(message.mode),
                        WorkAreaSize.byOrdinal(message.workAreaSize));
            });
            return null;
        }
    }

    static boolean applyMode(EntityPlayerMP player, int slot, BookOfCallingMode mode) {
        ItemBookOfCallingFoliotJob book = getValidatedBook(player, slot);
        if (book == null || mode == null || !book.supportsMode(mode)) {
            return false;
        }
        ItemStack stack = player.inventory.getStackInSlot(slot);
        book.setBookConfiguration(stack, mode, book.getCurrentWorkAreaSize(stack), player.getEntityWorld());
        player.inventory.setInventorySlotContents(slot, stack);
        syncContainerState(player);
        return true;
    }

    static boolean applyWorkAreaSize(EntityPlayerMP player, int slot, WorkAreaSize workAreaSize) {
        ItemBookOfCallingFoliotJob book = getValidatedBook(player, slot);
        if (book == null || workAreaSize == null) {
            return false;
        }
        ItemStack stack = player.inventory.getStackInSlot(slot);
        BookOfCallingMode mode = book.getCurrentMode(stack);
        if (!mode.hasSize()) {
            return false;
        }
        book.setBookConfiguration(stack, mode, workAreaSize, player.getEntityWorld());
        player.inventory.setInventorySlotContents(slot, stack);
        syncContainerState(player);
        return true;
    }

    private static boolean applyConfiguration(EntityPlayerMP player, int slot, BookOfCallingMode mode,
            WorkAreaSize workAreaSize) {
        ItemBookOfCallingFoliotJob book = getValidatedBook(player, slot);
        if (book == null || mode == null || workAreaSize == null || !book.supportsMode(mode)) {
            return false;
        }
        ItemStack stack = player.inventory.getStackInSlot(slot);
        book.setBookConfiguration(stack, mode, workAreaSize, player.getEntityWorld());
        player.inventory.setInventorySlotContents(slot, stack);
        syncContainerState(player);
        return true;
    }

    private static ItemBookOfCallingFoliotJob getValidatedBook(EntityPlayerMP player, int slot) {
        if (player == null || !(player.openContainer instanceof ContainerBookOfCalling)) {
            return null;
        }
        ContainerBookOfCalling container = (ContainerBookOfCalling) player.openContainer;
        if (!container.isBookSlot(slot) || !container.canInteractWith(player)
                || slot < 0 || slot >= player.inventory.getSizeInventory()) {
            return null;
        }
        ItemStack stack = player.inventory.getStackInSlot(slot);
        return stack.getItem() instanceof ItemBookOfCallingFoliotJob
                ? (ItemBookOfCallingFoliotJob) stack.getItem() : null;
    }

    private static void syncContainerState(EntityPlayerMP player) {
        player.inventoryContainer.detectAndSendChanges();
        if (player.openContainer != player.inventoryContainer) {
            player.openContainer.detectAndSendChanges();
        }
    }
}
