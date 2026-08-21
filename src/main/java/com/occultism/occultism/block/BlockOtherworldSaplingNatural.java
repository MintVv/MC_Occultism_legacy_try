package com.occultism.occultism.block;

import java.util.Random;

import javax.annotation.Nullable;

import com.occultism.occultism.registry.ModBlocks;
import com.occultism.occultism.registry.ModCreativeTabs;
import com.occultism.occultism.world.WorldGenOtherworldTree;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.IGrowable;
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

public class BlockOtherworldSaplingNatural extends BlockBush implements IGrowable, IOtherworldBlock {

    private static final WorldGenOtherworldTree TREE_GENERATOR = new WorldGenOtherworldTree(true, true);

    public BlockOtherworldSaplingNatural() {
        super();
        this.setDefaultState(this.blockState.getBaseState().withProperty(UNCOVERED, Boolean.FALSE));
        this.setSoundType(SoundType.PLANT);
        this.setCreativeTab(ModCreativeTabs.OCCULTISM);
        this.setTickRandomly(true);
    }

    @Override
    public Block getUncoveredBlock() {
        return this;
    }

    @Override
    public Block getCoveredBlock() {
        return Blocks.SAPLING;
    }

    @Override
    public OtherworldBlockTier getTier() {
        return OtherworldBlockTier.ONE;
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
        if (!world.isRemote && world.getLightFromNeighbors(pos.up()) >= 9 && random.nextInt(7) == 0) {
            this.generateTree(world, pos, state, random);
        }
    }

    @Override
    public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean isClient) {
        return true;
    }

    @Override
    public boolean canUseBonemeal(World world, Random random, BlockPos pos, IBlockState state) {
        return random.nextFloat() < 0.45D;
    }

    @Override
    public void grow(World world, Random random, BlockPos pos, IBlockState state) {
        this.generateTree(world, pos, state, random);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state,
            int fortune) {
        drops.add(state.getValue(UNCOVERED)
                ? new ItemStack(Item.getItemFromBlock(ModBlocks.OTHERWORLD_SAPLING_NATURAL))
                : new ItemStack(Item.getItemFromBlock(Blocks.SAPLING), 1, 0));
    }

    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state,
            @Nullable net.minecraft.tileentity.TileEntity te, ItemStack stack) {
        super.harvestBlock(worldIn, player, pos, this.getHarvestState(state, player, stack), te, stack);
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, net.minecraft.util.math.RayTraceResult target,
            World world, BlockPos pos, EntityPlayer player) {
        return this.getPickBlockStack(world, pos, state);
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
        return BlockRenderLayer.CUTOUT;
    }

    private void generateTree(World world, BlockPos pos, IBlockState state, Random random) {
        world.setBlockState(pos, Blocks.AIR.getDefaultState(), 4);
        if (!TREE_GENERATOR.generate(world, random, pos)) {
            world.setBlockState(pos, state, 4);
        }
    }
}
