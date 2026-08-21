package com.occultism.occultism.ritual;

import java.util.List;

import net.minecraft.item.ItemStack;

public final class RitualRegistry {

    private RitualRegistry() {
    }

    public static List<RitualDefinition> getDefinitions() {
        return RitualRecipeManager.getDefinitions();
    }

    public static RitualDefinition findMatching(List<PentacleMatch> pentacles, ItemStack stack) {
        return RitualRecipeManager.findMatching(pentacles, stack);
    }

    public static RitualDefinition getFirstForPentacle(String pentacleId) {
        return RitualRecipeManager.getFirstForPentacle(pentacleId);
    }

    public static RitualDefinition getById(String id) {
        return RitualRecipeManager.getById(id);
    }
}
