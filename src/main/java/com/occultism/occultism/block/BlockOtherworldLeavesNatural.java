package com.occultism.occultism.block;

import java.util.Random;

import javax.annotation.Nullable;

import com.occultism.occultism.registry.ModBlocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.SoundType;
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

public class BlockOtherworldLeavesNatural extends BlockLeaves implements IOtherworldBlock {

    public BlockOtherworldLeavesNatural() {
        this.setDefaultState(this.blockState.getBaseState()
                .withProperty(CHECK_DECAY, Boolean.FALSE)
                .withProperty(DECAYABLE, Boolean.TRUE)
                .withProperty(UNCOVERED, Boolean.FALSE));
        this.setHardness(0.2F);
        this.setLightOpacity(1);
        this.setSoundType(SoundType.PLANT);
    }

    @Override
    public Block getUncoveredBlock() {
        return ModBlocks.OTHERWORLD_LEAVES;
    }

    @Override
    public Block getCoveredBlock() {
        return Blocks.LEAVES;
    }

    @Override
    public OtherworldBlockTier getTier() {
        return OtherworldBlockTier.ONE;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return state.getValue(UNCOVERED)
                ? Item.getItemFromBlock(ModBlocks.OTHERWORLD_SAPLING_NATURAL)
                : Item.getItemFromBlock(Blocks.SAPLING);
    }

    @Override
    public int damageDropped(IBlockState state) {
        return 0;
    }

    @Override
    public BlockPlanks.EnumType getWoodType(int meta) {
        return BlockPlanks.EnumType.OAK;
    }

    @Override
    public NonNullList<ItemStack> onSheared(ItemStack item, IBlockAccess world, BlockPos pos, int fortune) {
        NonNullList<ItemStack> drops = NonNullList.create();
        IBlockState state = world.getBlockState(pos);
        drops.add(state.getValue(UNCOVERED)
                ? new ItemStack(Item.getItemFromBlock(ModBlocks.OTHERWORLD_LEAVES))
                : new ItemStack(Item.getItemFromBlock(Blocks.LEAVES), 1, 0));
        return drops;
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
                ? new ItemStack(Item.getItemFromBlock(ModBlocks.OTHERWORLD_LEAVES))
                : new ItemStack(Item.getItemFromBlock(Blocks.LEAVES), 1, 0);
    }

    @Override
    protected ItemStack getSilkTouchDrop(IBlockState state) {
        return state.getValue(UNCOVERED)
                ? new ItemStack(Item.getItemFromBlock(ModBlocks.OTHERWORLD_LEAVES))
                : new ItemStack(Item.getItemFromBlock(Blocks.LEAVES), 1, 0);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState()
                .withProperty(UNCOVERED, (meta & 1) == 1)
                .withProperty(DECAYABLE, (meta & 4) == 0)
                .withProperty(CHECK_DECAY, (meta & 8) > 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = state.getValue(UNCOVERED) ? 1 : 0;
        if (!state.getValue(DECAYABLE)) {
            meta |= 4;
        }
        if (state.getValue(CHECK_DECAY)) {
            meta |= 8;
        }
        return meta;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, CHECK_DECAY, DECAYABLE, UNCOVERED);
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }
}
