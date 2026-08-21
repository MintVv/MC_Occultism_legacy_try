package com.occultism.occultism.block;

import com.occultism.occultism.registry.ModItems;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.SoundType;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockDaturaCrop extends BlockCrops {

    public BlockDaturaCrop() {
        this.setSoundType(SoundType.PLANT);
    }

    @Override
    protected Item getSeed() {
        return ModItems.DATURA_SEEDS;
    }

    @Override
    protected Item getCrop() {
        return ModItems.DATURA;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }
}
