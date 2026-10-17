package com.occultism.occultism.handler;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.occultism.occultism.Tags;
import com.occultism.occultism.tileentity.TileEntitySacrificialBowl;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/** Loaded-only ritual listeners, with death confirmation after Guardian/Fairy and other cancellation hooks. */
@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class RitualSacrificeHandler {
    private static final Map<World, List<PendingSacrifice>> PENDING = new IdentityHashMap<>();

    private RitualSacrificeHandler() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDeath(LivingDeathEvent event) {
        EntityLivingBase victim = event.getEntityLiving();
        if (event.isCanceled() || victim == null || victim.world == null || victim.world.isRemote) {
            return;
        }
        // 1.12 Entity.getPosition adds 0.5 to Y; upstream blockPosition floors the actual foot position.
        BlockPos deathPosition = new BlockPos(victim.posX, victim.posY, victim.posZ);
        for (TileEntity tile : new ArrayList<>(victim.world.loadedTileEntityList)) {
            if (!(tile instanceof TileEntitySacrificialBowl)) {
                continue;
            }
            TileEntitySacrificialBowl bowl = (TileEntitySacrificialBowl) tile;
            if (bowl.canProvideSacrifice(victim, event.getSource(), deathPosition)) {
                PENDING.computeIfAbsent(victim.world, world -> new ArrayList<>())
                        .add(new PendingSacrifice(event, bowl, deathPosition));
            }
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.world == null || event.world.isRemote) {
            return;
        }
        List<PendingSacrifice> deaths = PENDING.remove(event.world);
        if (deaths == null) {
            return;
        }
        for (PendingSacrifice pending : deaths) {
            // Re-check the same event: even a later LOWEST listener may cancel or revive the victim.
            if (pending.event.isCanceled() || pending.bowl.getWorld() != event.world
                    || !event.world.isBlockLoaded(pending.bowl.getPos())
                    || event.world.getTileEntity(pending.bowl.getPos()) != pending.bowl) {
                continue;
            }
            pending.bowl.provideSacrifice(pending.event.getEntityLiving(), pending.event.getSource(),
                    pending.deathPosition, pending.generation);
        }
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        if (!event.getWorld().isRemote) {
            PENDING.remove(event.getWorld());
        }
    }

    private static final class PendingSacrifice {
        private final LivingDeathEvent event;
        private final TileEntitySacrificialBowl bowl;
        private final BlockPos deathPosition;
        private final long generation;

        private PendingSacrifice(LivingDeathEvent event, TileEntitySacrificialBowl bowl, BlockPos deathPosition) {
            this.event = event;
            this.bowl = bowl;
            this.deathPosition = deathPosition;
            this.generation = bowl.getRitualGeneration();
        }
    }
}
