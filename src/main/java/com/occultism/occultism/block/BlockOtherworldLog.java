package com.occultism.occultism.block;

import net.minecraft.block.BlockLog;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class BlockOtherworldLog extends BlockLog {

    public BlockOtherworldLog() {
        super();
        this.setDefaultState(this.blockState.getBaseState().withProperty(LOG_AXIS, EnumAxis.Y));
        this.setSoundType(SoundType.WOOD);
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
        return this.getDefaultState().withProperty(LOG_AXIS, axis);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        switch (state.getValue(LOG_AXIS)) {
            case X:
                return 4;
            case Z:
                return 8;
            case NONE:
                return 12;
            case Y:
            default:
                return 0;
        }
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] { LOG_AXIS });
    }

    @Override
    protected ItemStack getSilkTouchDrop(IBlockState state) {
        return new ItemStack(Item.getItemFromBlock(this));
    }

    @Override
    public int damageDropped(IBlockState state) {
        return 0;
    }
}
