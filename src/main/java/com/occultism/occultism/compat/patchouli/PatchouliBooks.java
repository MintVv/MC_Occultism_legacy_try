package com.occultism.occultism.compat.patchouli;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import vazkii.patchouli.api.PatchouliAPI;

/** Loaded only after checking for Patchouli, including on a dedicated server. */
public final class PatchouliBooks {
    public static final ResourceLocation DICTIONARY =
            new ResourceLocation("occultism", "dictionary_of_spirits");

    private PatchouliBooks() {
    }

    public static void open(EntityPlayerMP player) {
        PatchouliAPI.instance.openBookGUI(player, DICTIONARY);
    }
}
