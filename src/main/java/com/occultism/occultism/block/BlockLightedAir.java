package com.occultism.occultism.block;

import java.util.List;
import java.util.Random;

import com.occultism.occultism.entity.EntityCthulhuFamiliar;

import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockLightedAir extends BlockAir {

    public BlockLightedAir() {
        this.setLightLevel(1.0F);
        this.setTickRandomly(true);
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random random) {
        if (worldIn.isRemote) {
            return;
        }
        List<EntityCthulhuFamiliar> familiars = worldIn.getEntitiesWithinAABB(
                EntityCthulhuFamiliar.class, new AxisAlignedBB(pos));
        for (EntityCthulhuFamiliar familiar : familiars) {
            if (!familiar.isDead && familiar.hasBlacksmithUpgrade()) {
                return;
            }
        }
        worldIn.setBlockToAir(pos);
    }
}
