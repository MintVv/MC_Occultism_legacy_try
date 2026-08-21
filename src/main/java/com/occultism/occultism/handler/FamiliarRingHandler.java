package com.occultism.occultism.handler;

import javax.annotation.Nullable;

import com.occultism.occultism.Tags;
import com.occultism.occultism.capability.FamiliarRingCapability;
import com.occultism.occultism.registry.ModCapabilities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import com.occultism.occultism.network.MessageUnequipFamiliarRing;
import com.occultism.occultism.network.ModNetworking;

/** Player lifecycle and tick integration for the native 1.12 Familiar Ring slot. */
@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class FamiliarRingHandler {

    private static final ResourceLocation KEY = new ResourceLocation(Tags.MOD_ID, "familiar_ring");

    private FamiliarRingHandler() {
    }

    @SubscribeEvent
    public static void attach(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityPlayer) {
            event.addCapability(KEY, new ModCapabilities.FamiliarRingProvider());
        }
    }

    @SubscribeEvent
    public static void clone(PlayerEvent.Clone event) {
        FamiliarRingCapability oldRing = get(event.getOriginal());
        FamiliarRingCapability newRing = get(event.getEntityPlayer());
        if (oldRing != null && newRing != null) {
            newRing.copyFrom(oldRing);
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerLoggedInEvent event) {
        sync(event.player);
    }

    @SubscribeEvent
    public static void onRespawn(PlayerRespawnEvent event) {
        sync(event.player);
    }

    @SubscribeEvent
    public static void onChangedDimension(PlayerChangedDimensionEvent event) {
        sync(event.player);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player == null) {
            return;
        }
        FamiliarRingCapability ring = get(event.player);
        if (ring != null && ring.isEquipped()) {
            ring.tick(event.player);
        }
    }

    /** Sneaking with an empty main hand is the native 1.12 unequip gesture. */
    @SubscribeEvent
    public static void onRightClickEmpty(PlayerInteractEvent.RightClickEmpty event) {
        EntityPlayer player = event.getEntityPlayer();
        if (player == null || !player.world.isRemote || event.getHand() != net.minecraft.util.EnumHand.MAIN_HAND
                || !player.isSneaking() || !player.getHeldItemMainhand().isEmpty()) {
            return;
        }
        ModNetworking.CHANNEL.sendToServer(new MessageUnequipFamiliarRing());
    }

    public static void sync(@Nullable EntityPlayer player) {
        if (player instanceof EntityPlayerMP) {
            ModNetworking.syncFamiliarRing((EntityPlayerMP) player);
        }
    }

    @Nullable
    public static FamiliarRingCapability get(@Nullable EntityPlayer player) {
        if (player == null || ModCapabilities.FAMILIAR_RING == null
                || !player.hasCapability(ModCapabilities.FAMILIAR_RING, (EnumFacing) null)) {
            return null;
        }
        return player.getCapability(ModCapabilities.FAMILIAR_RING, (EnumFacing) null);
    }
}
