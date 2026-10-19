package com.occultism.occultism.block;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * The bell, brought back for 1.12.
 *
 * <p>The 1.14 bell is a thin object, not a full cube, so a plain {@link Block} with the default shape
 * looked like a misplaced solid block. This gives it the outline of the real thing while the model
 * carries the bell geometry. Only the floor-standing form is modelled: the wall and ceiling
 * attachments need two more block states, which this ritual-only decorative block does not warrant.</p>
 */
public class BlockBell extends Block {

    private static final AxisAlignedBB SHAPE = new AxisAlignedBB(5 / 16D, 0.0D, 5 / 16D, 11 / 16D, 13 / 16D, 11 / 16D);

    public BlockBell() {
        super(Material.IRON);
        this.setSoundType(SoundType.METAL);
        this.setLightOpacity(0);
        this.setHardness(5.0F);
        this.setResistance(10.0F);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return SHAPE;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return SHAPE;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }
}
