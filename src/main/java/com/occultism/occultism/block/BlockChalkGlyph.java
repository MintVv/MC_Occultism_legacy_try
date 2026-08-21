package com.occultism.occultism.block;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockChalkGlyph extends Block {

    public static final int MAX_SIGN = 14;
    public static final PropertyInteger SIGN = PropertyInteger.create("sign", 0, MAX_SIGN);

    private static final AxisAlignedBB BOUNDING_BOX =
            new AxisAlignedBB(0.09375D, 0.0D, 0.09375D, 0.90625D, 0.03125D, 0.90625D);

    private final int color;
    @Nullable
    private Item chalkItem;

    public BlockChalkGlyph(int color) {
        super(Material.CIRCUITS);
        this.color = color;
        this.setDefaultState(this.blockState.getBaseState().withProperty(SIGN, 1));
        this.setSoundType(SoundType.CLOTH);
        this.setHardness(0.2F);
        this.setResistance(0.2F);
        this.setLightOpacity(0);
    }

    public int getColor() {
        return this.color;
    }

    public void setChalkItem(Item chalkItem) {
        this.chalkItem = chalkItem;
    }

    public int getRandomSign(Random random) {
        return Math.max(random.nextInt(MAX_SIGN + 1), 1);
    }

    public int getNextSign(IBlockState state) {
        int sign = state.getValue(SIGN) + 1;
        return sign > MAX_SIGN ? 1 : sign;
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
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos,
            EntityPlayer player) {
        return this.chalkItem == null ? ItemStack.EMPTY : new ItemStack(this.chalkItem);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(SIGN, Math.min(meta, MAX_SIGN));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(SIGN);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, SIGN);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }
}
