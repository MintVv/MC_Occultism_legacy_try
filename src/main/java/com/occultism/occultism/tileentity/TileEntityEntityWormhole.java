package com.occultism.occultism.tileentity;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileEntityEntityWormhole extends TileEntity {

    private static final String TAG_COMPASS = "Compass";

    private ItemStack compass = ItemStack.EMPTY;

    public ItemStack getCompass() {
        return this.compass.copy();
    }

    public boolean hasCompass() {
        return !this.compass.isEmpty();
    }

    public void setCompass(ItemStack compass) {
        this.compass = compass.isEmpty() ? ItemStack.EMPTY : compass.copy();
        this.compass.setCount(Math.min(1, this.compass.getCount()));
        this.markDirty();
    }

    public ItemStack removeCompass() {
        ItemStack removed = this.compass.copy();
        this.compass = ItemStack.EMPTY;
        this.markDirty();
        return removed;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        if (!this.compass.isEmpty()) {
            compound.setTag(TAG_COMPASS, this.compass.writeToNBT(new NBTTagCompound()));
        }
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey(TAG_COMPASS, 10)) {
            this.compass = new ItemStack(compound.getCompoundTag(TAG_COMPASS));
            this.compass.setCount(Math.min(1, this.compass.getCount()));
        } else {
            this.compass = ItemStack.EMPTY;
        }
    }
}
