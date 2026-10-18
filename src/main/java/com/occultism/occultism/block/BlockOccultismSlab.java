package com.occultism.occultism.block;

import java.util.Random;

import com.occultism.occultism.Tags;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;

public abstract class BlockOccultismSlab extends BlockSlab {

    public static final PropertyEnum<Variant> VARIANT = PropertyEnum.create("variant", Variant.class);

    private final String halfSlabName;

    public BlockOccultismSlab(String halfSlabName) {
        super(Material.ROCK);
        this.halfSlabName = halfSlabName;
        IBlockState state = this.blockState.getBaseState().withProperty(VARIANT, Variant.DEFAULT);
        if (!this.isDouble()) {
            state = state.withProperty(HALF, EnumBlockHalf.BOTTOM);
        }
        this.setDefaultState(state);
        this.useNeighborBrightness = !this.isDouble();
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(this.getHalfSlabBlock());
    }

    @Override
    public ItemStack getItem(net.minecraft.world.World worldIn, net.minecraft.util.math.BlockPos pos, IBlockState state) {
        return new ItemStack(this.getHalfSlabBlock());
    }

    @Override
    public String getTranslationKey(int meta) {
        return super.getTranslationKey();
    }

    @Override
    public IProperty<?> getVariantProperty() {
        return VARIANT;
    }

    @Override
    public Comparable<?> getTypeForItem(ItemStack stack) {
        return Variant.DEFAULT;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        IBlockState state = this.getDefaultState().withProperty(VARIANT, Variant.DEFAULT);
        if (!this.isDouble()) {
            state = state.withProperty(HALF, (meta & 8) == 0 ? EnumBlockHalf.BOTTOM : EnumBlockHalf.TOP);
        }
        return state;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        if (!this.isDouble() && state.getValue(HALF) == EnumBlockHalf.TOP) {
            return 8;
        }
        return 0;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return this.isDouble()
                ? new BlockStateContainer(this, VARIANT)
                : new BlockStateContainer(this, HALF, VARIANT);
    }

    @Override
    public int damageDropped(IBlockState state) {
        return 0;
    }

    private Block getHalfSlabBlock() {
        Block block = Block.getBlockFromName(Tags.MOD_ID + ":" + this.halfSlabName);
        return block == null ? this : block;
    }

    public enum Variant implements IStringSerializable {
        DEFAULT("default");

        private final String name;

        Variant(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return this.name;
        }
    }
}
