package com.occultism.occultism.block;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockOtherworldStoneNatural extends Block implements IOtherworldBlock {

    private final Block uncoveredBlock;
    private final Block coveredBlock;
    private final int coveredMeta;
    private final OtherworldBlockTier tier;

    public BlockOtherworldStoneNatural(Block uncoveredBlock, Block coveredBlock, int coveredMeta,
            OtherworldBlockTier tier) {
        super(Material.ROCK);
        this.uncoveredBlock = uncoveredBlock;
        this.coveredBlock = coveredBlock;
        this.coveredMeta = coveredMeta;
        this.tier = tier;
        this.setSoundType(SoundType.STONE);
        this.setDefaultState(this.blockState.getBaseState().withProperty(UNCOVERED, Boolean.FALSE));
    }

    @Override
    public Block getUncoveredBlock() {
        return this.uncoveredBlock;
    }

    @Override
    public Block getCoveredBlock() {
        return this.coveredBlock;
    }

    @Override
    public OtherworldBlockTier getTier() {
        return this.tier;
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state,
            int fortune) {
        Block dropBlock = state.getValue(UNCOVERED) ? this.uncoveredBlock : this.coveredBlock;
        int meta = state.getValue(UNCOVERED) ? 0 : this.coveredMeta;
        Item item = Item.getItemFromBlock(dropBlock);
        if (item != Item.getItemFromBlock(Blocks.AIR)) {
            drops.add(new ItemStack(item, 1, meta));
        }
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
                ? new ItemStack(Item.getItemFromBlock(this.uncoveredBlock))
                : new ItemStack(Item.getItemFromBlock(this.coveredBlock), 1, this.coveredMeta);
    }

    @Override
    protected ItemStack getSilkTouchDrop(IBlockState state) {
        return state.getValue(UNCOVERED)
                ? new ItemStack(Item.getItemFromBlock(this.uncoveredBlock))
                : new ItemStack(Item.getItemFromBlock(this.coveredBlock), 1, this.coveredMeta);
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

    @Override
    public void getSubBlocks(net.minecraft.creativetab.CreativeTabs itemIn, NonNullList<ItemStack> items) {
        items.add(new ItemStack(this));
    }
}
