package com.occultism.occultism.block;

import java.util.Random;

import com.occultism.occultism.registry.ModBlocks;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.SoundType;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockOtherworldLeaves extends BlockLeaves {

    public BlockOtherworldLeaves() {
        this.setDefaultState(this.blockState.getBaseState()
                .withProperty(CHECK_DECAY, Boolean.FALSE)
                .withProperty(DECAYABLE, Boolean.TRUE));
        this.setHardness(0.2F);
        this.setLightOpacity(1);
        this.setSoundType(SoundType.PLANT);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(ModBlocks.OTHERWORLD_SAPLING_NATURAL);
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
        drops.add(new ItemStack(this));
        return drops;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState()
                .withProperty(DECAYABLE, (meta & 4) == 0)
                .withProperty(CHECK_DECAY, (meta & 8) > 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = 0;
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
        return new BlockStateContainer(this, CHECK_DECAY, DECAYABLE);
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
