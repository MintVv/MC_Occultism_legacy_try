package com.occultism.occultism.block;

public class BlockOccultismDoubleSlab extends BlockOccultismSlab {

    public BlockOccultismDoubleSlab(String halfSlabName) {
        super(halfSlabName);
    }

    @Override
    public boolean isDouble() {
        return true;
    }
}
