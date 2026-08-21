package com.occultism.occultism.handler;

import javax.annotation.Nullable;

import com.occultism.occultism.Tags;
import com.occultism.occultism.capability.FamiliarSettings;
import com.occultism.occultism.network.ModNetworking;
import com.occultism.occultism.registry.ModCapabilities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

/** Attaches and clones the persistent Familiar settings capability. */
@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class FamiliarSettingsHandler {

    private static final ResourceLocation KEY = new ResourceLocation(Tags.MOD_ID, "familiar_settings");

    private FamiliarSettingsHandler() {
    }

    @SubscribeEvent
    public static void attach(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityPlayer) {
            event.addCapability(KEY, new ModCapabilities.Provider());
        }
    }

    @SubscribeEvent
    public static void clone(PlayerEvent.Clone event) {
        if (!event.isWasDeath() || ModCapabilities.FAMILIAR_SETTINGS == null) {
            return;
        }
        FamiliarSettings oldSettings = get(event.getOriginal());
        FamiliarSettings newSettings = get(event.getEntityPlayer());
        if (oldSettings != null && newSettings != null) {
            newSettings.copyFrom(oldSettings);
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            ModNetworking.syncFamiliarSettings((EntityPlayerMP) event.player);
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerRespawnEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            ModNetworking.syncFamiliarSettings((EntityPlayerMP) event.player);
        }
    }

    @SubscribeEvent
    public static void onChangedDimension(PlayerChangedDimensionEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            ModNetworking.syncFamiliarSettings((EntityPlayerMP) event.player);
        }
    }

    @Nullable
    private static FamiliarSettings get(EntityPlayer player) {
        if (player == null || ModCapabilities.FAMILIAR_SETTINGS == null
                || !player.hasCapability(ModCapabilities.FAMILIAR_SETTINGS, (EnumFacing) null)) {
            return null;
        }
        return player.getCapability(ModCapabilities.FAMILIAR_SETTINGS, (EnumFacing) null);
    }
}
