package com.occultism.occultism.block;

public class BlockOccultismHalfSlab extends BlockOccultismSlab {

    public BlockOccultismHalfSlab(String halfSlabName) {
        super(halfSlabName);
    }

    @Override
    public boolean isDouble() {
        return false;
    }
}
