package com.occultism.occultism.container;

import com.occultism.occultism.item.ItemBookOfCallingFoliotJob;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

public class ContainerBookOfCalling extends Container {

    private final int bookSlot;

    /** Compatibility constructor for code that only needs a detached container. */
    public ContainerBookOfCalling() {
        this(-1);
    }

    public ContainerBookOfCalling(int bookSlot) {
        this.bookSlot = bookSlot;
    }

    public int getBookSlot() {
        return this.bookSlot;
    }

    public boolean isBookSlot(int slot) {
        return this.bookSlot >= 0 && this.bookSlot == slot;
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        if (this.bookSlot < 0) {
            return true;
        }
        if (playerIn == null || this.bookSlot >= playerIn.inventory.getSizeInventory()) {
            return false;
        }
        ItemStack stack = playerIn.inventory.getStackInSlot(this.bookSlot);
        return stack.getItem() instanceof ItemBookOfCallingFoliotJob;
    }
}
