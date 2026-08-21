package com.occultism.occultism.block;

import javax.annotation.Nullable;

import com.occultism.occultism.registry.ModBlocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLog;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.IProperty;
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

public class BlockOtherworldLogNatural extends BlockLog implements IOtherworldBlock {

    public BlockOtherworldLogNatural() {
        super();
        this.setDefaultState(this.blockState.getBaseState()
                .withProperty(LOG_AXIS, EnumAxis.Y)
                .withProperty(UNCOVERED, Boolean.FALSE));
        this.setSoundType(SoundType.WOOD);
    }

    @Override
    public Block getUncoveredBlock() {
        return ModBlocks.OTHERWORLD_LOG;
    }

    @Override
    public Block getCoveredBlock() {
        return Blocks.LOG;
    }

    @Override
    public OtherworldBlockTier getTier() {
        return OtherworldBlockTier.ONE;
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state,
            int fortune) {
        drops.add(state.getValue(UNCOVERED)
                ? new ItemStack(Item.getItemFromBlock(ModBlocks.OTHERWORLD_LOG))
                : new ItemStack(Item.getItemFromBlock(Blocks.LOG), 1, 0));
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
        EnumAxis axis = EnumAxis.Y;
        int axisBits = meta & 12;
        if (axisBits == 4) {
            axis = EnumAxis.X;
        } else if (axisBits == 8) {
            axis = EnumAxis.Z;
        } else if (axisBits == 12) {
            axis = EnumAxis.NONE;
        }
        return this.getDefaultState()
                .withProperty(LOG_AXIS, axis)
                .withProperty(UNCOVERED, (meta & 1) == 1);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = state.getValue(UNCOVERED) ? 1 : 0;
        switch (state.getValue(LOG_AXIS)) {
            case X:
                return meta | 4;
            case Z:
                return meta | 8;
            case NONE:
                return meta | 12;
            case Y:
            default:
                return meta;
        }
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] { LOG_AXIS, UNCOVERED });
    }

    @Override
    protected ItemStack getSilkTouchDrop(IBlockState state) {
        return new ItemStack(Item.getItemFromBlock(state.getValue(UNCOVERED) ? ModBlocks.OTHERWORLD_LOG : Blocks.LOG),
                1, 0);
    }

    @Override
    public int damageDropped(IBlockState state) {
        return state.getValue(UNCOVERED) ? 0 : 0;
    }
}
