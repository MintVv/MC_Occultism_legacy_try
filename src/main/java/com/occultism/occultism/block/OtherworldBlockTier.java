package com.occultism.occultism.block;

public enum OtherworldBlockTier {
    NONE(0),
    ONE(1),
    TWO(2);

    private final int level;

    OtherworldBlockTier(int level) {
        this.level = level;
    }

    public int getLevel() {
        return this.level;
    }

    public boolean canReveal(OtherworldBlockTier required) {
        return this.level >= required.level;
    }

    public static OtherworldBlockTier max(OtherworldBlockTier first, OtherworldBlockTier second) {
        return first.level >= second.level ? first : second;
    }
}
