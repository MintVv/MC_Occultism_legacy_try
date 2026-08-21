package com.occultism.occultism.entity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import com.occultism.occultism.Tags;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class StepHeightBoostTracker {

    private static final Map<UUID, BoostState> BOOSTS = new HashMap<>();

    private StepHeightBoostTracker() {
    }

    public static void apply(EntityPlayer player, float stepHeight, long expiresAt) {
        UUID id = player.getUniqueID();
        BoostState state = BOOSTS.get(id);
        if (state == null) {
            state = new BoostState(player.stepHeight);
            BOOSTS.put(id, state);
        }
        state.expiresAt = Math.max(state.expiresAt, expiresAt);
        state.dimension = player.world.provider.getDimension();
        if (player.stepHeight < stepHeight) {
            player.stepHeight = stepHeight;
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player == null) {
            return;
        }
        BoostState state = BOOSTS.get(event.player.getUniqueID());
        if (state == null) {
            return;
        }
        long time = event.player.world.getTotalWorldTime();
        if (state.dimension != event.player.world.provider.getDimension() || time > state.expiresAt) {
            restore(event.player, state);
            BOOSTS.remove(event.player.getUniqueID());
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerLoggedOutEvent event) {
        if (event.player != null) {
            BoostState state = BOOSTS.remove(event.player.getUniqueID());
            if (state != null) {
                restore(event.player, state);
            }
        }
    }

    public static void clearExpiredForTests(long worldTime) {
        Iterator<Map.Entry<UUID, BoostState>> iterator = BOOSTS.entrySet().iterator();
        while (iterator.hasNext()) {
            if (worldTime > iterator.next().getValue().expiresAt) {
                iterator.remove();
            }
        }
    }

    private static void restore(EntityPlayer player, BoostState state) {
        if (player.stepHeight > state.originalStepHeight) {
            player.stepHeight = state.originalStepHeight;
        }
    }

    private static class BoostState {
        private final float originalStepHeight;
        private long expiresAt;
        private int dimension;

        private BoostState(float originalStepHeight) {
            this.originalStepHeight = originalStepHeight;
        }
    }
}
