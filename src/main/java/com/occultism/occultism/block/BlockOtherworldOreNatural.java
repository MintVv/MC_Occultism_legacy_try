package com.occultism.occultism.block;

import java.util.Random;

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
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockOtherworldOreNatural extends Block implements IOtherworldBlock {

    private final Block uncoveredBlock;
    private final Block coveredBlock;
    private final OtherworldBlockTier tier;

    public BlockOtherworldOreNatural(Block uncoveredBlock, Block coveredBlock, OtherworldBlockTier tier) {
        super(Material.ROCK);
        this.uncoveredBlock = uncoveredBlock;
        this.coveredBlock = coveredBlock;
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
        if (!state.getValue(UNCOVERED)) {
            drops.add(new ItemStack(Item.getItemFromBlock(this.coveredBlock)));
            return;
        }
        drops.add(new ItemStack(ModItems.RAW_IESNIUM, this.quantityDroppedWithBonus(fortune, world instanceof World
                ? ((World) world).rand : new Random())));
    }

    @Override
    public int quantityDropped(Random random) {
        return 1;
    }

    @Override
    public int quantityDroppedWithBonus(int fortune, Random random) {
        if (fortune <= 0) {
            return this.quantityDropped(random);
        }
        return this.quantityDropped(random) * (1 + random.nextInt(fortune + 1));
    }

    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state,
            @Nullable net.minecraft.tileentity.TileEntity te, ItemStack stack) {
        super.harvestBlock(worldIn, player, pos, this.getHarvestState(state, player, stack), te, stack);
    }

    @Override
    public int getExpDrop(IBlockState state, IBlockAccess world, BlockPos pos, int fortune) {
        return state.getValue(UNCOVERED) ? 3 + (world instanceof World ? ((World) world).rand.nextInt(5) : 0) : 0;
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, net.minecraft.util.math.RayTraceResult target,
            World world, BlockPos pos, EntityPlayer player) {
        return this.getPickBlockStack(world, pos, state);
    }

    @Override
    protected ItemStack getSilkTouchDrop(IBlockState state) {
        return new ItemStack(Item.getItemFromBlock(state.getValue(UNCOVERED) ? this.uncoveredBlock : this.coveredBlock));
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
}
