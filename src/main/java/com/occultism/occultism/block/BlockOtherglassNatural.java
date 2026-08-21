package com.occultism.occultism.block;

import javax.annotation.Nullable;

import com.occultism.occultism.registry.ModItems;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockOtherglassNatural extends Block implements IOtherworldBlock {

    public BlockOtherglassNatural() {
        super(Material.GLASS);
        this.setSoundType(SoundType.GLASS);
        this.setDefaultState(this.blockState.getBaseState().withProperty(UNCOVERED, Boolean.FALSE));
    }

    @Override
    public Block getUncoveredBlock() {
        return Blocks.END_STONE;
    }

    @Override
    public Block getCoveredBlock() {
        return this;
    }

    @Override
    public OtherworldBlockTier getTier() {
        return OtherworldBlockTier.TWO;
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state,
            int fortune) {
        drops.add(state.getValue(UNCOVERED)
                ? new ItemStack(Item.getItemFromBlock(this))
                : new ItemStack(ModItems.CRUSHED_END_STONE));
    }

    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state,
            @Nullable net.minecraft.tileentity.TileEntity te, ItemStack stack) {
        super.harvestBlock(worldIn, player, pos, this.getHarvestState(state, player, stack), te, stack);
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, net.minecraft.util.math.RayTraceResult target,
            World world, BlockPos pos, EntityPlayer player) {
        return state.getValue(UNCOVERED)
                ? new ItemStack(Item.getItemFromBlock(Blocks.END_STONE))
                : new ItemStack(Item.getItemFromBlock(this));
    }

    @Override
    protected ItemStack getSilkTouchDrop(IBlockState state) {
        return state.getValue(UNCOVERED)
                ? new ItemStack(Item.getItemFromBlock(this))
                : new ItemStack(ModItems.CRUSHED_END_STONE);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(UNCOVERED, (meta & 1) == 1);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(UNCOVERED) ? 1 : 0;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, UNCOVERED);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.TRANSLUCENT;
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
