package com.occultism.occultism.block;

import com.occultism.occultism.registry.ModCreativeTabs;
import net.minecraft.block.BlockBush;
import net.minecraft.block.SoundType;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockSimplePlant extends BlockBush {

    public BlockSimplePlant() {
        super();
        this.setSoundType(SoundType.PLANT);
        this.setCreativeTab(ModCreativeTabs.OCCULTISM);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }
}
