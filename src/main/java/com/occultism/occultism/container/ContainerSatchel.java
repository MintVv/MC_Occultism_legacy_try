package com.occultism.occultism.container;

import com.occultism.occultism.item.ItemSatchel;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerSatchel extends Container {

    private static final int SATCHEL_SIZE = SatchelInventory.SIZE;
    private static final int PLAYER_INVENTORY_START = SATCHEL_SIZE;
    private static final int PLAYER_HOTBAR_START = PLAYER_INVENTORY_START + 27;
    private static final int PLAYER_SLOT_END = PLAYER_HOTBAR_START + 9;
    private static final int SATCHEL_ROW_LENGTH = 13;
    private static final int SATCHEL_LEFT = 8;
    private static final int SATCHEL_TOP = 8;
    private static final int PLAYER_INVENTORY_LEFT = 44;
    private static final int PLAYER_INVENTORY_TOP = 174;
    private static final int HOTBAR_LEFT = 44;
    private static final int HOTBAR_TOP = 232;

    private final InventoryPlayer playerInventory;
    private final SatchelInventory satchelInventory;
    private final int selectedSlot;

    public ContainerSatchel(InventoryPlayer playerInventory, SatchelInventory satchelInventory, int selectedSlot) {
        this.playerInventory = playerInventory;
        this.satchelInventory = satchelInventory;
        this.selectedSlot = selectedSlot;

        for (int slotIndex = 0; slotIndex < SATCHEL_SIZE; slotIndex++) {
            int row = slotIndex / SATCHEL_ROW_LENGTH;
            int column = slotIndex % SATCHEL_ROW_LENGTH;
            this.addSlotToContainer(new SatchelSlot(satchelInventory, slotIndex,
                    SATCHEL_LEFT + column * 18, SATCHEL_TOP + row * 18));
        }

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlotToContainer(new PlayerSlot(playerInventory, column + row * 9 + 9,
                        PLAYER_INVENTORY_LEFT + column * 18, PLAYER_INVENTORY_TOP + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            this.addSlotToContainer(new PlayerSlot(playerInventory, column, HOTBAR_LEFT + column * 18, HOTBAR_TOP));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        ItemStack stack = this.playerInventory.getStackInSlot(this.selectedSlot);
        return ItemSatchel.opensSatchelInventory(stack) && this.satchelInventory.isUsableByPlayer(playerIn);
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

        if (index < SATCHEL_SIZE) {
            if (!this.mergeItemStack(stack, PLAYER_INVENTORY_START, PLAYER_SLOT_END, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (isSatchelItem(stack)) {
                return ItemStack.EMPTY;
            }
            if (!this.mergeItemStack(stack, 0, SATCHEL_SIZE, false)) {
                if (index >= PLAYER_INVENTORY_START && index < PLAYER_HOTBAR_START) {
                    if (!this.mergeItemStack(stack, PLAYER_HOTBAR_START, PLAYER_SLOT_END, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= PLAYER_HOTBAR_START && index < PLAYER_SLOT_END
                        && !this.mergeItemStack(stack, PLAYER_INVENTORY_START, PLAYER_HOTBAR_START, false)) {
                    return ItemStack.EMPTY;
                }
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
        this.satchelInventory.writeToStack();
    }

    private static boolean isSatchelItem(ItemStack stack) {
        return ItemSatchel.isSatchelLike(stack);
    }

    private static class SatchelSlot extends Slot {

        SatchelSlot(SatchelInventory inventory, int index, int xPosition, int yPosition) {
            super(inventory, index, xPosition, yPosition);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return !isSatchelItem(stack);
        }
    }

    private class PlayerSlot extends Slot {

        PlayerSlot(InventoryPlayer inventory, int index, int xPosition, int yPosition) {
            super(inventory, index, xPosition, yPosition);
        }

        @Override
        public boolean canTakeStack(EntityPlayer playerIn) {
            return this.getSlotIndex() != ContainerSatchel.this.selectedSlot && super.canTakeStack(playerIn);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return this.getSlotIndex() != ContainerSatchel.this.selectedSlot && super.isItemValid(stack);
        }
    }
}
