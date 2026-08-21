package com.occultism.occultism.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockPentacleRequirement extends Block {

    private final AxisAlignedBB boundingBox;
    private final BlockRenderLayer renderLayer;

    public BlockPentacleRequirement(Material material, SoundType soundType, AxisAlignedBB boundingBox,
            BlockRenderLayer renderLayer) {
        super(material);
        this.boundingBox = boundingBox;
        this.renderLayer = renderLayer;
        this.setSoundType(soundType);
        this.setLightOpacity(0);
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return this.canSurviveAt(worldIn, pos);
    }

    @Override
    public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side) {
        return side == EnumFacing.UP && this.canPlaceBlockAt(worldIn, pos);
    }

    private boolean canSurviveAt(IBlockAccess worldIn, BlockPos pos) {
        BlockPos down = pos.down();
        IBlockState downState = worldIn.getBlockState(down);
        return downState.isSideSolid(worldIn, down, EnumFacing.UP);
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (!this.canSurviveAt(worldIn, pos)) {
            worldIn.setBlockToAir(pos);
        }
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return this.boundingBox;
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
        return this.renderLayer;
    }
}
