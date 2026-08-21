package com.occultism.occultism.container;

import com.occultism.occultism.tileentity.TileEntityDimensionalBattlefield;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ContainerDimensionalBattlefield extends Container {

    private static final int BATTLEFIELD_SLOT_COUNT = TileEntityDimensionalBattlefield.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = BATTLEFIELD_SLOT_COUNT;
    private static final int PLAYER_HOTBAR_START = PLAYER_INVENTORY_START + 27;
    private static final int PLAYER_SLOT_END = PLAYER_HOTBAR_START + 9;

    private final TileEntityDimensionalBattlefield battlefield;
    private int battleTime;
    private int maxBattleTime;
    private int fuelCost;

    public ContainerDimensionalBattlefield(InventoryPlayer playerInventory,
            TileEntityDimensionalBattlefield battlefield) {
        this.battlefield = battlefield;

        this.addSlotToContainer(new InputSlot(battlefield, TileEntityDimensionalBattlefield.SOUL_SLOT, 26, 35));
        this.addSlotToContainer(new InputSlot(battlefield, TileEntityDimensionalBattlefield.WEAPON_SLOT, 17, 61));
        this.addSlotToContainer(new InputSlot(battlefield, TileEntityDimensionalBattlefield.FUEL_SLOT, 35, 61));

        for (int row = 0; row < 5; row++) {
            for (int column = 0; column < 5; column++) {
                int slot = TileEntityDimensionalBattlefield.FIRST_OUTPUT_SLOT + column + row * 5;
                this.addSlotToContainer(new OutputSlot(battlefield, slot, 76 + column * 18, 17 + row * 18));
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlotToContainer(new Slot(playerInventory, column + row * 9 + 9,
                        8 + column * 18, 130 + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            this.addSlotToContainer(new Slot(playerInventory, column, 8 + column * 18, 188));
        }
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        listener.sendAllWindowProperties(this, this.battlefield);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        for (IContainerListener listener : this.listeners) {
            if (this.battleTime != this.battlefield.getField(0)) {
                listener.sendWindowProperty(this, 0, this.battlefield.getField(0));
            }
            if (this.maxBattleTime != this.battlefield.getField(1)) {
                listener.sendWindowProperty(this, 1, this.battlefield.getField(1));
            }
            if (this.fuelCost != this.battlefield.getField(2)) {
                listener.sendWindowProperty(this, 2, this.battlefield.getField(2));
            }
        }

        this.battleTime = this.battlefield.getField(0);
        this.maxBattleTime = this.battlefield.getField(1);
        this.fuelCost = this.battlefield.getField(2);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int data) {
        this.battlefield.setField(id, data);
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return this.battlefield.isUsableByPlayer(playerIn);
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

        if (index >= TileEntityDimensionalBattlefield.FIRST_OUTPUT_SLOT && index < BATTLEFIELD_SLOT_COUNT) {
            if (!this.mergeItemStack(stack, PLAYER_INVENTORY_START, PLAYER_SLOT_END, true)) {
                return ItemStack.EMPTY;
            }
            slot.onSlotChange(stack, original);
        } else if (index < BATTLEFIELD_SLOT_COUNT) {
            if (!this.mergeItemStack(stack, PLAYER_INVENTORY_START, PLAYER_SLOT_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.mergeIntoBattlefieldInputs(stack)) {
            if (index >= PLAYER_INVENTORY_START && index < PLAYER_HOTBAR_START) {
                if (!this.mergeItemStack(stack, PLAYER_HOTBAR_START, PLAYER_SLOT_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= PLAYER_HOTBAR_START && index < PLAYER_SLOT_END
                    && !this.mergeItemStack(stack, PLAYER_INVENTORY_START, PLAYER_HOTBAR_START, false)) {
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

    private boolean mergeIntoBattlefieldInputs(ItemStack stack) {
        if (this.battlefield.isItemValidForSlot(TileEntityDimensionalBattlefield.SOUL_SLOT, stack)
                && this.mergeItemStack(stack, TileEntityDimensionalBattlefield.SOUL_SLOT,
                        TileEntityDimensionalBattlefield.SOUL_SLOT + 1, false)) {
            return true;
        }
        if (this.battlefield.isItemValidForSlot(TileEntityDimensionalBattlefield.FUEL_SLOT, stack)
                && this.mergeItemStack(stack, TileEntityDimensionalBattlefield.FUEL_SLOT,
                        TileEntityDimensionalBattlefield.FUEL_SLOT + 1, false)) {
            return true;
        }
        return this.battlefield.isItemValidForSlot(TileEntityDimensionalBattlefield.WEAPON_SLOT, stack)
                && this.mergeItemStack(stack, TileEntityDimensionalBattlefield.WEAPON_SLOT,
                        TileEntityDimensionalBattlefield.WEAPON_SLOT + 1, false);
    }

    public int getProgressScaled(int width) {
        int remaining = this.battlefield.getField(0);
        int max = this.battlefield.getField(1);
        if (remaining <= 0 || max <= 0) {
            return 0;
        }
        return (max - remaining) * width / max;
    }

    public int getBattleTime() {
        return this.battlefield.getField(0);
    }

    public int getMaxBattleTime() {
        return this.battlefield.getField(1);
    }

    public int getFuelCost() {
        return this.battlefield.getField(2);
    }

    private static class InputSlot extends Slot {

        InputSlot(TileEntityDimensionalBattlefield battlefield, int index, int xPosition, int yPosition) {
            super(battlefield, index, xPosition, yPosition);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return this.inventory.isItemValidForSlot(this.getSlotIndex(), stack);
        }

        @Override
        public int getSlotStackLimit() {
            return this.getSlotIndex() == TileEntityDimensionalBattlefield.FUEL_SLOT ? 64 : 1;
        }
    }

    private static class OutputSlot extends Slot {

        OutputSlot(TileEntityDimensionalBattlefield battlefield, int index, int xPosition, int yPosition) {
            super(battlefield, index, xPosition, yPosition);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return false;
        }
    }
}
