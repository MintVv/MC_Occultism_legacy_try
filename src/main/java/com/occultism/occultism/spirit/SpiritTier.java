package com.occultism.occultism.spirit;

import net.minecraft.util.ResourceLocation;

public enum SpiritTier {
    DJINNI("djinni", 12.0D, 0.32D, 0.95F),
    AFRIT("afrit", 18.0D, 0.34D, 1.12F),
    MARID("marid", 24.0D, 0.36D, 1.28F),
    ANCIENT("ancient", 32.0D, 0.38D, 1.45F),
    FOLIOT("foliot", 8.0D, 0.35D, 1.0F);

    private final ResourceLocation id;
    private final double maxHealth;
    private final double movementSpeed;
    private final float renderScale;

    SpiritTier(String path, double maxHealth, double movementSpeed, float renderScale) {
        this.id = new ResourceLocation("occultism", path);
        this.maxHealth = maxHealth;
        this.movementSpeed = movementSpeed;
        this.renderScale = renderScale;
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public double getMaxHealth() {
        return this.maxHealth;
    }

    public double getMovementSpeed() {
        return this.movementSpeed;
    }

    public float getRenderScale() {
        return this.renderScale;
    }

    public static SpiritTier fromId(String id) {
        if (id == null || id.trim().isEmpty()) {
            return null;
        }
        ResourceLocation location = toOccultismLocation(id.trim());
        for (SpiritTier tier : values()) {
            if (tier.id.equals(location)) {
                return tier;
            }
        }
        return null;
    }

    private static ResourceLocation toOccultismLocation(String id) {
        return id.indexOf(':') >= 0 ? new ResourceLocation(id) : new ResourceLocation("occultism", id);
    }
}
