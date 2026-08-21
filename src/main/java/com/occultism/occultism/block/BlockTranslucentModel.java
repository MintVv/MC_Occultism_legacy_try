package com.occultism.occultism.block;

import net.minecraft.block.material.Material;
import net.minecraft.util.BlockRenderLayer;

public class BlockTranslucentModel extends BlockModelOnly {

    public BlockTranslucentModel(Material material) {
        super(material, BlockRenderLayer.TRANSLUCENT);
    }
}
