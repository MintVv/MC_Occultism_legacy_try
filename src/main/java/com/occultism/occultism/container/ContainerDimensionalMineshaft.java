package com.occultism.occultism.container;

import com.occultism.occultism.tileentity.TileEntityDimensionalMineshaft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ContainerDimensionalMineshaft extends Container {

    private static final int MINESHAFT_SLOT_COUNT = TileEntityDimensionalMineshaft.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MINESHAFT_SLOT_COUNT;
    private static final int PLAYER_HOTBAR_START = PLAYER_INVENTORY_START + 27;
    private static final int PLAYER_SLOT_END = PLAYER_HOTBAR_START + 9;

    private final TileEntityDimensionalMineshaft mineshaft;
    private int miningTime;
    private int maxMiningTime;

    public ContainerDimensionalMineshaft(InventoryPlayer playerInventory, TileEntityDimensionalMineshaft mineshaft) {
        this.mineshaft = mineshaft;

        this.addSlotToContainer(new MinerSlot(mineshaft, TileEntityDimensionalMineshaft.MINER_SLOT, 26, 35));

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                int slot = TileEntityDimensionalMineshaft.FIRST_OUTPUT_SLOT + column + row * 3;
                this.addSlotToContainer(new OutputSlot(mineshaft, slot, 98 + column * 18, 17 + row * 18));
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlotToContainer(new Slot(playerInventory, column + row * 9 + 9,
                        8 + column * 18, 84 + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            this.addSlotToContainer(new Slot(playerInventory, column, 8 + column * 18, 142));
        }
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        listener.sendAllWindowProperties(this, this.mineshaft);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        for (IContainerListener listener : this.listeners) {
            if (this.miningTime != this.mineshaft.getField(0)) {
                listener.sendWindowProperty(this, 0, this.mineshaft.getField(0));
            }
            if (this.maxMiningTime != this.mineshaft.getField(1)) {
                listener.sendWindowProperty(this, 1, this.mineshaft.getField(1));
            }
        }

        this.miningTime = this.mineshaft.getField(0);
        this.maxMiningTime = this.mineshaft.getField(1);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int data) {
        this.mineshaft.setField(id, data);
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return this.mineshaft.isUsableByPlayer(playerIn);
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

        if (index >= TileEntityDimensionalMineshaft.FIRST_OUTPUT_SLOT && index < MINESHAFT_SLOT_COUNT) {
            if (!this.mergeItemStack(stack, PLAYER_INVENTORY_START, PLAYER_SLOT_END, true)) {
                return ItemStack.EMPTY;
            }
            slot.onSlotChange(stack, original);
        } else if (index >= PLAYER_INVENTORY_START) {
            if (this.mineshaft.isItemValidForSlot(TileEntityDimensionalMineshaft.MINER_SLOT, stack)) {
                if (!this.mergeItemStack(stack, TileEntityDimensionalMineshaft.MINER_SLOT,
                        TileEntityDimensionalMineshaft.MINER_SLOT + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= PLAYER_INVENTORY_START && index < PLAYER_HOTBAR_START) {
                if (!this.mergeItemStack(stack, PLAYER_HOTBAR_START, PLAYER_SLOT_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= PLAYER_HOTBAR_START && index < PLAYER_SLOT_END
                    && !this.mergeItemStack(stack, PLAYER_INVENTORY_START, PLAYER_HOTBAR_START, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.mergeItemStack(stack, PLAYER_INVENTORY_START, PLAYER_SLOT_END, false)) {
            return ItemStack.EMPTY;
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

    public int getProgressScaled(int width) {
        int remaining = this.mineshaft.getField(0);
        int max = this.mineshaft.getField(1);
        if (remaining <= 0 || max <= 0) {
            return 0;
        }
        return (max - remaining) * width / max;
    }

    public int getMiningTime() {
        return this.mineshaft.getField(0);
    }

    public int getMaxMiningTime() {
        return this.mineshaft.getField(1);
    }

    private static class MinerSlot extends Slot {

        MinerSlot(TileEntityDimensionalMineshaft mineshaft, int index, int xPosition, int yPosition) {
            super(mineshaft, index, xPosition, yPosition);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return this.inventory.isItemValidForSlot(TileEntityDimensionalMineshaft.MINER_SLOT, stack);
        }

        @Override
        public int getSlotStackLimit() {
            return 1;
        }
    }

    private static class OutputSlot extends Slot {

        OutputSlot(TileEntityDimensionalMineshaft mineshaft, int index, int xPosition, int yPosition) {
            super(mineshaft, index, xPosition, yPosition);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return false;
        }
    }
}
