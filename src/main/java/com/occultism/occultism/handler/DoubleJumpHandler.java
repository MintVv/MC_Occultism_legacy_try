package com.occultism.occultism.handler;

import javax.annotation.Nullable;

import com.occultism.occultism.Tags;
import com.occultism.occultism.capability.DoubleJumpCapability;
import com.occultism.occultism.registry.ModCapabilities;
import com.occultism.occultism.util.MovementUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/** Player capability lifecycle for the Otherworld Bird aerial-jump effect. */
@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class DoubleJumpHandler {

    private static final ResourceLocation KEY = new ResourceLocation(Tags.MOD_ID, "double_jump");

    private DoubleJumpHandler() {
    }

    @SubscribeEvent
    public static void attach(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityPlayer && ModCapabilities.DOUBLE_JUMP != null
                && !event.getObject().hasCapability(ModCapabilities.DOUBLE_JUMP, (EnumFacing) null)) {
            event.addCapability(KEY, new ModCapabilities.DoubleJumpProvider());
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player == null || ModCapabilities.DOUBLE_JUMP == null) {
            return;
        }
        DoubleJumpCapability capability = get(event.player);
        if (capability == null || !event.player.onGround || capability.getJumps() <= 0) {
            return;
        }
        capability.setJumps(0);
        if (!event.player.world.isRemote) {
            MovementUtil.syncJumps(event.player);
        }
    }

    @Nullable
    public static DoubleJumpCapability get(EntityPlayer player) {
        if (player == null || ModCapabilities.DOUBLE_JUMP == null
                || !player.hasCapability(ModCapabilities.DOUBLE_JUMP, (EnumFacing) null)) {
            return null;
        }
        return player.getCapability(ModCapabilities.DOUBLE_JUMP, (EnumFacing) null);
    }
}
