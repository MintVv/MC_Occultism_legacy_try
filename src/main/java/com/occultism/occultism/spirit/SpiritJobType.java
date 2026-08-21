package com.occultism.occultism.spirit;

import net.minecraft.util.ResourceLocation;

public enum SpiritJobType {
    CRUSHER("crush_tier1", "crusher", "crush_tier2", "crush_tier3", "crush_tier4"),
    CRYSTALLIZER("crystal_tier1", "crystallizer", "crystal_tier2", "crystal_tier3", "crystal_tier4"),
    SMELTER("smelt_tier1", "smelter", "smelt_tier2", "smelt_tier3", "smelt_tier4"),
    CLEANER("cleaner"),
    FARMER("farmer"),
    TRADER_OTHERSTONE("trader_otherstone"),
    TRADER_OTHERROCK("trader_otherrock"),
    TRADER_SAPLING("trader_otherworld_saplings", "trader_sapling"),
    TRANSPORT_ITEMS("transport_items"),
    LUMBERJACK("lumberjack"),
    CLEAR_WEATHER("clear_weather"),
    RAIN_WEATHER("rain_weather"),
    THUNDER_WEATHER("thunder_weather"),
    DAY_TIME("day_time"),
    NIGHT_TIME("night_time"),
    GAMBLER("gambler"),
    /** Djinni route that moves items between a storage controller and a machine. */
    MANAGE_MACHINE("manage_machine");

    private final ResourceLocation id;
    private final ResourceLocation[] aliases;

    SpiritJobType(String path, String... aliases) {
        this.id = toOccultismLocation(path);
        this.aliases = new ResourceLocation[aliases.length];
        for (int i = 0; i < aliases.length; i++) {
            this.aliases[i] = toOccultismLocation(aliases[i]);
        }
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public boolean isFoliotJob() {
        switch (this) {
            case CRUSHER:
            case CRYSTALLIZER:
            case SMELTER:
            case CLEANER:
            case FARMER:
            case TRADER_OTHERSTONE:
            case TRADER_OTHERROCK:
            case TRADER_SAPLING:
            case GAMBLER:
            case TRANSPORT_ITEMS:
            case LUMBERJACK:
            case MANAGE_MACHINE:
                return true;
            default:
                return false;
        }
    }

    public boolean isMachineManagementJob() {
        return this == MANAGE_MACHINE;
    }

    public boolean isWorldEffectJob() {
        switch (this) {
            case CLEAR_WEATHER:
            case RAIN_WEATHER:
            case THUNDER_WEATHER:
            case DAY_TIME:
            case NIGHT_TIME:
                return true;
            default:
                return false;
        }
    }

    public static SpiritJobType fromId(String id) {
        if (id == null || id.trim().isEmpty()) {
            return null;
        }
        ResourceLocation location = toOccultismLocation(id.trim());
        for (SpiritJobType type : values()) {
            if (type.matches(location)) {
                return type;
            }
        }
        return null;
    }

    private boolean matches(ResourceLocation location) {
        if (this.id.equals(location)) {
            return true;
        }
        for (ResourceLocation alias : this.aliases) {
            if (alias.equals(location)) {
                return true;
            }
        }
        return false;
    }

    private static ResourceLocation toOccultismLocation(String id) {
        return id.indexOf(':') >= 0 ? new ResourceLocation(id) : new ResourceLocation("occultism", id);
    }
}
