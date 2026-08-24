package com.occultism.occultism.container;

import com.occultism.occultism.entity.EntityFoliotJob;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

/** Spirit inventory with the modern transport/cleaner ghost filter grid. */
public class ContainerSpiritTransporter extends ContainerSpirit {

    private static final int FILTER_SLOT_START = BASE_SLOT_END;
    private static final int FILTER_SLOT_END = FILTER_SLOT_START + EntityFoliotJob.MAX_FILTER_SLOTS;

    public ContainerSpiritTransporter(InventoryPlayer playerInventory, EntityFoliotJob spirit) {
        super(playerInventory, spirit);
        if (!spirit.supportsTransporterFilter()) {
            throw new IllegalArgumentException("Spirit job does not support transporter filters");
        }
        this.setupFilterSlots();
    }

    @Override
    protected void setupPlayerInventorySlots(InventoryPlayer playerInventory) {
        int playerInventoryTop = 138;
        int playerInventoryLeft = 8;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlotToContainer(new Slot(playerInventory, column + row * 9 + 9,
                        playerInventoryLeft + column * 18, playerInventoryTop + row * 18));
            }
        }
    }

    @Override
    protected void setupPlayerHotbar(InventoryPlayer playerInventory) {
        int hotbarTop = 196;
        int hotbarLeft = 8;
        for (int column = 0; column < 9; column++) {
            this.addSlotToContainer(new Slot(playerInventory, column, hotbarLeft + column * 18, hotbarTop));
        }
    }

    private void setupFilterSlots() {
        int left = 8;
        int top = 84;
        IItemHandler filters = this.getSpirit().getFilterItems();
        for (int row = 0; row < 2; row++) {
            for (int column = 0; column < EntityFoliotJob.MAX_FILTER_SLOTS / 2; column++) {
                int slot = column + row * 7;
                this.addSlotToContainer(new FilterSlot(filters, slot, left + column * 18, top + row * 18));
            }
        }
    }

    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player) {
        Slot slot = slotId >= 0 && slotId < this.inventorySlots.size()
                ? this.inventorySlots.get(slotId) : null;
        if (slot instanceof FilterSlot) {
            ItemStack carried = player.inventory.getItemStack();
            if (clickTypeIn != ClickType.PICKUP) {
                return carried.copy();
            }
            if (carried.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else if (slot.isItemValid(carried)) {
                ItemStack ghost = carried.copy();
                ghost.setCount(1);
                slot.putStack(ghost);
            }
            return carried.copy();
        }
        return super.slotClick(slotId, dragType, clickTypeIn, player);
    }

    @Override
    public boolean canDragIntoSlot(Slot slotIn) {
        return !(slotIn instanceof FilterSlot) && super.canDragIntoSlot(slotIn);
    }

    @Override
    public boolean canMergeSlot(ItemStack stack, Slot slotIn) {
        return !(slotIn instanceof FilterSlot) && super.canMergeSlot(stack, slotIn);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        if (index >= FILTER_SLOT_START && index < FILTER_SLOT_END) {
            return ItemStack.EMPTY;
        }
        return super.transferStackInSlot(playerIn, index);
    }

    private static final class FilterSlot extends SlotItemHandler {

        private FilterSlot(IItemHandler handler, int index, int xPosition, int yPosition) {
            super(handler, index, xPosition, yPosition);
        }

        @Override
        public void putStack(ItemStack stack) {
            ItemStack ghost = stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
            if (!ghost.isEmpty()) {
                ghost.setCount(1);
            }
            super.putStack(ghost);
        }

        @Override
        public boolean canTakeStack(EntityPlayer playerIn) {
            return false;
        }

        @Override
        public int getSlotStackLimit() {
            return 1;
        }
    }
}
