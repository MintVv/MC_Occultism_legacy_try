package com.occultism.occultism.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;

public class BlockModelOnly extends Block {

    private final BlockRenderLayer renderLayer;

    public BlockModelOnly(Material material) {
        this(material, BlockRenderLayer.CUTOUT);
    }

    public BlockModelOnly(Material material, BlockRenderLayer renderLayer) {
        super(material);
        this.renderLayer = renderLayer;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return this.renderLayer;
    }
}
