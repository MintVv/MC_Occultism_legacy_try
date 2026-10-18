package com.occultism.occultism.block;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Potted otherflower.
 *
 * <p>Upstream subclasses the vanilla flower pot, which in 1.12 means a block entity that holds the
 * contained plant and a model that depends on it. The potted variants are creative-only decoration
 * in both versions, so this port models the one plant directly instead of carrying the block
 * entity: the contents never change, and dropping the block entity keeps the port simple.</p>
 */
public class BlockPottedOtherflower extends Block {

    private static final AxisAlignedBB SHAPE = new AxisAlignedBB(5 / 16D, 0.0D, 5 / 16D, 11 / 16D, 6 / 16D, 11 / 16D);

    public BlockPottedOtherflower() {
        super(Material.CIRCUITS);
        this.setSoundType(SoundType.STONE);
        this.setHardness(0.0F);
        this.setResistance(0.0F);
        this.setLightOpacity(0);
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

    @SideOnly(Side.CLIENT)
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }
}
