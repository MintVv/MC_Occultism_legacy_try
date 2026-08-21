package com.occultism.occultism.container;

import com.occultism.occultism.registry.ModItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerEnderSatchel extends Container {

    private static final int ENDER_CHEST_SIZE = 27;
    private static final int PLAYER_INVENTORY_START = ENDER_CHEST_SIZE;
    private static final int PLAYER_HOTBAR_START = PLAYER_INVENTORY_START + 27;
    private static final int PLAYER_SLOT_END = PLAYER_HOTBAR_START + 9;

    private final IInventory enderInventory;
    private final int selectedSlot;

    public ContainerEnderSatchel(IInventory playerInventory, IInventory enderInventory, EntityPlayer player,
            int selectedSlot) {
        this.enderInventory = enderInventory;
        this.selectedSlot = selectedSlot;
        enderInventory.openInventory(player);

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlotToContainer(new Slot(enderInventory, column + row * 9, 8 + column * 18, 18 + row * 18));
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlotToContainer(new LockedPlayerSlot(playerInventory, column + row * 9 + 9,
                        8 + column * 18, 103 + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            this.addSlotToContainer(new LockedPlayerSlot(playerInventory, column, 8 + column * 18, 161));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        ItemStack stack = playerIn.inventory.getStackInSlot(this.selectedSlot);
        return !stack.isEmpty() && stack.getItem() == ModItems.ENDER_SATCHEL
                && this.enderInventory.isUsableByPlayer(playerIn);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack original = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot == null || !slot.getHasStack()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getStack();
        original = stack.copy();

        if (index < ENDER_CHEST_SIZE) {
            if (!this.mergeItemStack(stack, PLAYER_INVENTORY_START, PLAYER_SLOT_END, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (stack == playerIn.inventory.getStackInSlot(this.selectedSlot)) {
                return ItemStack.EMPTY;
            }
            if (!this.mergeItemStack(stack, 0, ENDER_CHEST_SIZE, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.putStack(ItemStack.EMPTY);
        } else {
            slot.onSlotChanged();
        }

        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(playerIn, stack);
        return original;
    }

    @Override
    public void onContainerClosed(EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);
        this.enderInventory.closeInventory(playerIn);
    }

    public IInventory getEnderInventory() {
        return this.enderInventory;
    }

    private class LockedPlayerSlot extends Slot {

        LockedPlayerSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
            super(inventoryIn, index, xPosition, yPosition);
        }

        @Override
        public boolean canTakeStack(EntityPlayer playerIn) {
            return this.getSlotIndex() != ContainerEnderSatchel.this.selectedSlot && super.canTakeStack(playerIn);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return this.getSlotIndex() != ContainerEnderSatchel.this.selectedSlot && super.isItemValid(stack);
        }
    }
}
