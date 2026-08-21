package com.occultism.occultism.ritual;

import net.minecraft.util.math.BlockPos;

public class PentacleMatch {

    private final PentacleDefinition definition;
    private final BlockPos center;
    private final int requiredGlyphs;

    public PentacleMatch(PentacleDefinition definition, BlockPos center, int requiredGlyphs) {
        this.definition = definition;
        this.center = center;
        this.requiredGlyphs = requiredGlyphs;
    }

    public PentacleDefinition getDefinition() {
        return this.definition;
    }

    public BlockPos getCenter() {
        return this.center;
    }

    public int getRequiredGlyphs() {
        return this.requiredGlyphs;
    }
}
