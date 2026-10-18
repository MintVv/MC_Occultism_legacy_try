package com.occultism.occultism.block;

import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;

public class BlockOtherplanksStairs extends BlockStairs {

    public BlockOtherplanksStairs(IBlockState modelState) {
        super(modelState);
        this.useNeighborBrightness = true;
    }
}
