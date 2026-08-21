package com.occultism.occultism.ritual;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.inventory.InventoryHelper;

public class RitualResult {

    private final ItemStack stack;

    public RitualResult(ItemStack stack) {
        this.stack = stack.copy();
    }

    public ItemStack createStack() {
        return this.stack.copy();
    }

    public void spawn(World world, BlockPos pos) {
        InventoryHelper.spawnItemStack(world, pos.getX() + 0.5D, pos.getY() + 0.7D, pos.getZ() + 0.5D,
                this.createStack());
    }
}
