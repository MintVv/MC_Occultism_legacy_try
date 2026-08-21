package com.occultism.occultism.block;

import java.util.Random;

import javax.annotation.Nullable;

import com.occultism.occultism.ritual.SpiritFireRecipeRegistry;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
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

public class BlockSpiritFire extends Block {

    private static final AxisAlignedBB BOUNDING_BOX =
            new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.875D, 1.0D);

    public BlockSpiritFire() {
        super(Material.FIRE);
        this.setSoundType(SoundType.CLOTH);
        this.setLightLevel(1.0F);
        this.setLightOpacity(0);
        this.setTickRandomly(true);
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
    public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {
        return true;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return BOUNDING_BOX;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return NULL_AABB;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return true;
    }

    @Override
    public int quantityDropped(Random random) {
        return 0;
    }

    @Override
    public void onEntityCollision(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
        if (worldIn.isRemote || !(entityIn instanceof EntityItem)) {
            return;
        }
        EntityItem entityItem = (EntityItem) entityIn;
        ItemStack stack = entityItem.getItem();
        ItemStack result = SpiritFireRecipeRegistry.getResult(stack);
        if (result.isEmpty()) {
            return;
        }
        entityItem.setItem(result);
        entityItem.motionX *= 0.2D;
        entityItem.motionY = 0.1D;
        entityItem.motionZ *= 0.2D;
        worldIn.playEvent(2004, pos, 0);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }
}
