package com.occultism.occultism.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Decorative otherglass, the plain counterpart of {@link BlockOtherglassNatural}.
 *
 * <p>Upstream registers it as a plain block with the glass sound; 1.12 additionally needs the
 * render layer and the face culling hints that make glass behave like glass, which is what this
 * class adds.</p>
 */
public class BlockOtherglass extends Block {

    public BlockOtherglass() {
        super(Material.GLASS);
        this.setSoundType(SoundType.GLASS);
        this.setHardness(2.5F);
        this.setResistance(50.0F);
        this.setLightOpacity(0);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        IBlockState neighbour = world.getBlockState(pos.offset(side));
        // Same as vanilla glass: hide the faces between two identical blocks.
        return neighbour.getBlock() == this ? false : super.shouldSideBeRendered(state, world, pos, side);
    }
}
