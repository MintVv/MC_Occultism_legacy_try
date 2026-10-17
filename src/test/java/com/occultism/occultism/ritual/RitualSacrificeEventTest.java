package com.occultism.occultism.ritual;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import com.occultism.occultism.handler.RitualSacrificeHandler;
import com.occultism.occultism.tileentity.TileEntitySacrificialBowl;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RitualSacrificeEventTest {
    private RitualTestWorld world;
    private TestPlayer killer;
    private TileEntitySacrificialBowl bowl;
    private RitualDefinition definition;
    private Field recipes;
    private Object previousRecipes;

    @BeforeAll
    static void bootstrap() {
        Bootstrap.register();
        if (TileEntity.getKey(TileEntitySacrificialBowl.class) == null) {
            TileEntity.register("occultism:sacrificial_bowl", TileEntitySacrificialBowl.class);
        }
    }

    @BeforeEach
    void setup() throws Exception {
        world = new RitualTestWorld(false);
        killer = new TestPlayer(world);
        bowl = install(world, BlockPos.ORIGIN);
        recipes = RitualRecipeManager.class.getDeclaredField("definitions");
        recipes.setAccessible(true);
        previousRecipes = recipes.get(null);
        definition = definition("c:cows");
        recipes.set(null, Collections.singletonList(definition));
    }

    @AfterEach
    void cleanup() throws Exception {
        RitualSacrificeHandler.onWorldUnload(new WorldEvent.Unload(world));
        recipes.set(null, previousRecipes);
    }

    @Test
    void liveNearbyEntitiesAreNeverKilledAndTheRitualWaits() {
        EntityCow cow = cow(2, 0, 0, false);
        start(bowl);
        for (int tick = 0; tick < 25; tick++) {
            bowl.update();
        }
        assertTrue(bowl.isWaitingForSacrifice());
        assertEquals(0, bowl.getRitualProgress());
        assertTrue(cow.isEntityAlive());
        assertEquals(cow.getMaxHealth(), cow.getHealth());
        assertFalse(bowl.provideSacrifice(cow, playerDamage(), cow.getPosition(), bowl.getRitualGeneration()));
    }

    @Test
    void playerKillUnlocksTheRitualOnlyAfterWorldTickEndAndOnlyOnce() {
        start(bowl);
        EntityCow cow = cow(2, 0, 0, true);
        LivingDeathEvent event = new LivingDeathEvent(cow, playerDamage());
        RitualSacrificeHandler.onLivingDeath(event);
        RitualSacrificeHandler.onLivingDeath(event);
        assertTrue(bowl.isWaitingForSacrifice());
        RitualSacrificeHandler.onWorldTick(new TickEvent.WorldTickEvent(Side.SERVER, TickEvent.Phase.START, world));
        assertTrue(bowl.isWaitingForSacrifice());
        int updates = world.updates;
        flush(world);
        assertFalse(bowl.isWaitingForSacrifice());
        assertEquals(updates + 1, world.updates, "duplicate death notifications must synchronize only once");
        bowl.update();
        assertEquals(1, bowl.getRitualProgress());
        flush(world);
        assertEquals(1, bowl.getRitualProgress());
    }

    @Test
    void cancellationAtCaptureOrByALaterListenerDoesNotCount() {
        start(bowl);
        EntityCow cow = cow(1, 0, 0, true);
        LivingDeathEvent canceled = new CancelableDeathEvent(cow, playerDamage());
        canceled.setCanceled(true);
        RitualSacrificeHandler.onLivingDeath(canceled);
        flush(world);
        assertTrue(bowl.isWaitingForSacrifice());
        LivingDeathEvent lateCanceled = new CancelableDeathEvent(cow, playerDamage());
        RitualSacrificeHandler.onLivingDeath(lateCanceled);
        lateCanceled.setCanceled(true);
        flush(world);
        assertTrue(bowl.isWaitingForSacrifice());
    }

    @Test
    void revivedVictimDoesNotSupplyEnergyEvenIfTheEventRemainsUncanceled() {
        start(bowl);
        EntityCow cow = cow(1, 0, 0, true);
        RitualSacrificeHandler.onLivingDeath(new LivingDeathEvent(cow, playerDamage()));
        cow.setHealth(2.0F);
        flush(world);
        assertTrue(bowl.isWaitingForSacrifice());
        assertEquals(2.0F, cow.getHealth());
    }

    @Test
    void environmentAndNonplayerKillsAreRejectedButPlayerArrowsCount() {
        start(bowl);
        EntityCow cow = cow(2, 0, 0, true);
        RitualSacrificeHandler.onLivingDeath(new LivingDeathEvent(cow, DamageSource.FALL));
        RitualSacrificeHandler.onLivingDeath(new LivingDeathEvent(cow,
                DamageSource.causeMobDamage(new EntityZombie(world))));
        flush(world);
        assertTrue(bowl.isWaitingForSacrifice());
        DamageSource arrow = DamageSource.causeArrowDamage(new EntityTippedArrow(world), killer);
        RitualSacrificeHandler.onLivingDeath(new LivingDeathEvent(cow, arrow));
        flush(world);
        assertFalse(bowl.isWaitingForSacrifice());
    }

    @Test
    void rangeUsesUpstreamFootBlockPositionAndAThreeDimensionalEightBlockRadius() {
        start(bowl);
        assertTrue(bowl.canProvideSacrifice(cow(8.99D, 0, 0, true), playerDamage(), new BlockPos(8, 0, 0)));
        assertFalse(bowl.canProvideSacrifice(cow(9, 0, 0, true), playerDamage(), new BlockPos(9, 0, 0)));
        assertFalse(bowl.canProvideSacrifice(cow(6, 0, 6, true), playerDamage(), new BlockPos(6, 0, 6)));
        // The old automatic AABB stopped at four blocks vertically.
        RitualSacrificeHandler.onLivingDeath(new LivingDeathEvent(cow(0, 8.49D, 0, true), playerDamage()));
        flush(world);
        assertFalse(bowl.isWaitingForSacrifice());
    }

    @Test
    void oneDeathCountsForAllMatchingActiveBowlsRegardlessOfWhoStartedThem() {
        start(bowl);
        TileEntitySacrificialBowl second = install(world, new BlockPos(1, 0, 0));
        start(second);
        TestPlayer otherPlayer = new TestPlayer(world);
        EntityCow cow = cow(2, 0, 0, true);
        RitualSacrificeHandler.onLivingDeath(new LivingDeathEvent(cow, DamageSource.causePlayerDamage(otherPlayer)));
        flush(world);
        assertFalse(bowl.isWaitingForSacrifice());
        assertFalse(second.isWaitingForSacrifice());
    }

    @Test
    void humansMatchTheUpstreamVillagerAndPlayerTagButNotWitches() throws Exception {
        RitualSacrifice humans = new RitualSacrifice("occultism:humans", "Human");
        assertTrue(humans.matches(new EntityVillager(world)));
        TestPlayer victim = new TestPlayer(world);
        victim.setHealth(0);
        assertTrue(humans.matches(victim));
        assertFalse(humans.matches(new EntityWitch(world)));
        assertFalse(humans.matches(cow(1, 0, 0, false)));
        definition = definition("occultism:humans");
        recipes.set(null, Collections.singletonList(definition));
        start(bowl);
        victim.setPosition(1, 0, 0);
        RitualSacrificeHandler.onLivingDeath(new LivingDeathEvent(victim, playerDamage()));
        flush(world);
        assertFalse(bowl.isWaitingForSacrifice(), "players are allowed only through confirmed death events");
    }

    @Test
    void oldDeathsNeverUnlockRitualsStartedAfterTheEventOrReloadedExecutions() {
        EntityCow cow = cow(1, 0, 0, true);
        RitualSacrificeHandler.onLivingDeath(new LivingDeathEvent(cow, playerDamage()));
        start(bowl);
        flush(world);
        assertTrue(bowl.isWaitingForSacrifice());
        RitualSacrificeHandler.onLivingDeath(new LivingDeathEvent(cow, playerDamage()));
        NBTTagCompound saved = bowl.writeToNBT(new NBTTagCompound());
        bowl.readFromNBT(saved);
        flush(world);
        assertTrue(bowl.isWaitingForSacrifice(), "a queued death must not apply to a reloaded execution");
    }

    @Test
    void replacedOrUnloadedBowlsCannotReceivePendingDeaths() {
        start(bowl);
        RitualSacrificeHandler.onLivingDeath(new LivingDeathEvent(cow(1, 0, 0, true), playerDamage()));
        TileEntitySacrificialBowl replacement = install(world, BlockPos.ORIGIN);
        start(replacement);
        flush(world);
        assertTrue(bowl.isWaitingForSacrifice());
        assertTrue(replacement.isWaitingForSacrifice());
        RitualSacrificeHandler.onLivingDeath(new LivingDeathEvent(cow(1, 0, 0, true), playerDamage()));
        world.loaded = false;
        flush(world);
        assertTrue(replacement.isWaitingForSacrifice());
        world.loaded = true;
        flush(world);
        assertTrue(replacement.isWaitingForSacrifice(), "unavailable chunks must not retain a death for later");
    }

    @Test
    void worldUnloadClearsPendingEventsAndClientOrOtherWorldDeathsAreIgnored() {
        start(bowl);
        EntityCow cow = cow(1, 0, 0, true);
        RitualSacrificeHandler.onLivingDeath(new LivingDeathEvent(cow, playerDamage()));
        RitualSacrificeHandler.onWorldUnload(new WorldEvent.Unload(world));
        flush(world);
        assertTrue(bowl.isWaitingForSacrifice());
        RitualTestWorld other = new RitualTestWorld(false);
        EntityCow otherCow = new EntityCow(other);
        otherCow.setHealth(0);
        assertFalse(bowl.canProvideSacrifice(otherCow, playerDamage(), BlockPos.ORIGIN));
        RitualTestWorld client = new RitualTestWorld(true);
        TileEntitySacrificialBowl clientBowl = install(client, BlockPos.ORIGIN);
        start(clientBowl);
        EntityCow clientCow = new EntityCow(client);
        clientCow.setHealth(0);
        RitualSacrificeHandler.onLivingDeath(new LivingDeathEvent(clientCow, playerDamage()));
        flush(client);
        assertTrue(clientBowl.isWaitingForSacrifice());
    }

    @Test
    void receivedSacrificePersistsAndCorpseMovementDoesNotChangeTheDeathLocation() {
        start(bowl);
        EntityCow victim = cow(2, 0, 0, true);
        RitualSacrificeHandler.onLivingDeath(new LivingDeathEvent(victim, playerDamage()));
        victim.setPosition(100, 0, 0);
        flush(world);
        assertFalse(bowl.isWaitingForSacrifice());
        NBTTagCompound saved = bowl.writeToNBT(new NBTTagCompound());
        assertTrue(saved.getBoolean("SacrificeProvided"));
        TileEntitySacrificialBowl loaded = install(world, BlockPos.ORIGIN);
        loaded.readFromNBT(saved);
        assertFalse(loaded.isWaitingForSacrifice());
        loaded.update();
        assertEquals(1, loaded.getRitualProgress());
        assertTrue(victim.getHealth() <= 0);
    }

    @Test
    void sacrificeNamesRemainClientTranslatableOrUseTheCustomLiteral() {
        RitualSacrifice keyed = new RitualSacrifice("c:cows", "ritual.occultism.sacrifice.cows");
        assertTrue(keyed.createNameComponent() instanceof TextComponentTranslation);
        assertEquals("ritual.occultism.sacrifice.cows",
                ((TextComponentTranslation) keyed.createNameComponent()).getKey());
        assertEquals("Custom offering", new RitualSacrifice("custom:id", "Custom offering")
                .createNameComponent().getUnformattedText());
        assertEquals("custom:id", new RitualSacrifice("custom:id", "").createNameComponent().getUnformattedText());
    }

    private RitualDefinition definition(String tag) {
        RitualResult result = new RitualResult(new ItemStack(Items.DIAMOND));
        return new RitualDefinition("test_sacrifice", "Test sacrifice", "summon_foliot", "craft",
                new RitualIngredient(Items.BOOK, 1), result, result, 100, new RitualSacrifice(tag, "Offering"));
    }

    private void start(TileEntitySacrificialBowl target) {
        target.startRitual(definition, new ItemStack(Items.BOOK), 1, killer);
    }

    private DamageSource playerDamage() {
        return DamageSource.causePlayerDamage(killer);
    }

    private EntityCow cow(double x, double y, double z, boolean dying) {
        EntityCow cow = new EntityCow(world);
        cow.setPosition(x, y, z);
        if (dying) {
            cow.setHealth(0);
        }
        return cow;
    }

    private static TileEntitySacrificialBowl install(RitualTestWorld world, BlockPos pos) {
        world.placeFoliotPentacle(pos);
        return world.installBowl(pos, true);
    }

    private static void flush(World world) {
        RitualSacrificeHandler.onWorldTick(new TickEvent.WorldTickEvent(Side.SERVER, TickEvent.Phase.END, world));
    }

    /** Forge supplies this override through event transformation in game, but not under plain JUnit. */
    private static final class CancelableDeathEvent extends LivingDeathEvent {
        private CancelableDeathEvent(EntityLivingBase victim, DamageSource source) {
            super(victim, source);
        }

        @Override
        public boolean isCancelable() { return true; }
    }

    private static final class TestPlayer extends EntityPlayer {
        private TestPlayer(World world) {
            super(world, new GameProfile(UUID.randomUUID(), "sacrifice_test"));
        }

        @Override
        public boolean isSpectator() { return false; }

        @Override
        public boolean isCreative() { return false; }
    }

}
