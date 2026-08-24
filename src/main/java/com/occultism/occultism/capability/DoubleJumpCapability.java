package com.occultism.occultism.capability;

import net.minecraft.nbt.NBTTagCompound;

/** Tracks the number of aerial jumps used since the last landing. */
public class DoubleJumpCapability {

    private int jumps;

    public int getJumps() {
        return this.jumps;
    }

    public void setJumps(int jumps) {
        this.jumps = Math.max(0, jumps);
    }

    public void addJump() {
        this.jumps++;
    }

    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("jumps", this.jumps);
        return compound;
    }

    public void deserializeNBT(NBTTagCompound compound) {
        this.setJumps(compound == null ? 0 : compound.getInteger("jumps"));
    }
}
