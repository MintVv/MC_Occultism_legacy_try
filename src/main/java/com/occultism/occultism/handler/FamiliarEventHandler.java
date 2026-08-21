package com.occultism.occultism.handler;

import java.util.List;

import javax.annotation.Nullable;

import com.occultism.occultism.Tags;
import com.occultism.occultism.advancement.FamiliarTrigger;
import com.occultism.occultism.entity.EntityBeaverFamiliar;
import com.occultism.occultism.entity.EntityCthulhuFamiliar;
import com.occultism.occultism.entity.EntityFairyFamiliar;
import com.occultism.occultism.entity.EntityGuardianFamiliar;
import com.occultism.occultism.entity.EntityHeadlessFamiliar;
import com.occultism.occultism.registry.ModAdvancements;
import com.occultism.occultism.registry.ModPotions;
import com.occultism.occultism.util.FamiliarUtil;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.terraingen.SaplingGrowTreeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class FamiliarEventHandler {

    private FamiliarEventHandler() {
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        EntityPlayer player = event.getEntityPlayer();
        if (player == null || !isLog(event.getState(), player.world, event.getPos())
                || !player.isPotionActive(ModPotions.BEAVER_HARVEST)) {
            return;
        }
        PotionEffect effect = player.getActivePotionEffect(ModPotions.BEAVER_HARVEST);
        int amplifier = effect == null ? 0 : effect.getAmplifier();
        event.setNewSpeed(event.getNewSpeed() * (amplifier + 3));
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (entity == null || entity.world.isRemote || !entity.isPotionActive(ModPotions.MUMMY_DODGE)) {
            return;
        }
        DamageSource source = event.getSource();
        Entity sourceEntity = source == null ? null : source.getTrueSource();
        Entity immediateSource = source == null ? null : source.getImmediateSource();
        if (source == null || sourceEntity == null && immediateSource == null || source.isExplosion()
                || source.isUnblockable() || source == DamageSource.OUT_OF_WORLD || source.canHarmInCreative()) {
            return;
        }
        PotionEffect effect = entity.getActivePotionEffect(ModPotions.MUMMY_DODGE);
        int amplifier = effect == null ? 0 : effect.getAmplifier();
        if (entity.getRNG().nextFloat() < (amplifier + 1) * 0.1F) {
            event.setCanceled(true);
            // Match the upstream advancement at the point the dodge actually
            // cancels damage; merely having the potion must not grant it.
            ModAdvancements.FAMILIAR.trigger(entity, FamiliarTrigger.Type.MUMMY_DODGE);
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        EntityLivingBase target = event.getEntityLiving();
        if (target == null || target.world.isRemote) {
            return;
        }
        DamageSource source = event.getSource();
        if (source == null || !(source.getTrueSource() instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer) source.getTrueSource();
        if (hasMatchingHeadless(player, target)) {
            event.setAmount(event.getAmount() * 1.3F);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (entity == null || entity.world.isRemote) {
            return;
        }

        DamageSource source = event.getSource();
        Entity sourceEntity = source == null ? null : source.getTrueSource();
        if (sourceEntity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) sourceEntity;
            boolean stolen = stealHeadFor(player, entity);
            if (entity instanceof EntityCthulhuFamiliar && stolen) {
                ModAdvancements.FAMILIAR.trigger(player, FamiliarTrigger.Type.HEADLESS_CTHULHU_HEAD);
            }
        }

        if (entity instanceof EntityPlayer && source != null && source != DamageSource.OUT_OF_WORLD
                && !source.canHarmInCreative()) {
            EntityPlayer player = (EntityPlayer) entity;
            EntityGuardianFamiliar guardian = findGuardianFor(player);
            boolean ultimateSacrifice = guardian != null && guardian.getLives() == 1;
            if (guardian != null && guardian.sacrificeOneLife(player)) {
                event.setCanceled(true);
                player.setHealth(Math.max(1.0F, player.getMaxHealth() * 0.25F));
                if (ultimateSacrifice) {
                    ModAdvancements.FAMILIAR.trigger(player,
                            FamiliarTrigger.Type.GUARDIAN_ULTIMATE_SACRIFICE);
                }
            }
        }

        applyFairySave(event, entity, source);

        // 1.81.4 also runs lifesteal after another familiar cancels this death event.
        applyBatLifesteal(sourceEntity);
    }

    @SubscribeEvent
    public static void onLivingUpdate(LivingUpdateEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (entity == null || !entity.isPotionActive(ModPotions.FAIRY_SLOW_FALL)) {
            return;
        }
        if (entity.motionY < 0.0D) {
            entity.motionY = Math.max(entity.motionY, -0.125D);
            entity.fallDistance = 0.0F;
        }
    }

    private static void applyFairySave(LivingDeathEvent event, EntityLivingBase entity,
            @Nullable DamageSource source) {
        if (source == DamageSource.OUT_OF_WORLD || source != null && source.canHarmInCreative()
                || entity instanceof EntityGuardianFamiliar || entity instanceof EntityFairyFamiliar) {
            return;
        }
        EntityLivingBase owner = EntityFairyFamiliar.getOwnerOfFamiliar(entity);
        if (owner == null) {
            return;
        }

        EntityFairyFamiliar selected = null;
        double bestDistance = Double.MAX_VALUE;
        List<EntityFairyFamiliar> fairies = FamiliarUtil.getAllFamiliars(owner, EntityFairyFamiliar.class,
                fairy -> fairy != null && fairy.isEntityAlive() && !fairy.isSitting() && fairy.isTamed()
                        && fairy.isOwner(owner));
        for (EntityFairyFamiliar fairy : fairies) {
            double distance = fairy.getDistanceSq(entity);
            if (selected == null || distance < bestDistance) {
                selected = fairy;
                bestDistance = distance;
            }
        }
        if (selected == null || !selected.saveFamiliar(entity)) {
            return;
        }

        event.setCanceled(true);
        entity.setHealth(2.0F);
        entity.addPotionEffect(new PotionEffect(MobEffects.ABSORPTION, 20 * 5, 2));
        selected.spawnSupportParticles(entity);
        ModAdvancements.FAMILIAR.trigger(owner, FamiliarTrigger.Type.FAIRY_SAVE);
    }

    private static void applyBatLifesteal(@Nullable Entity sourceEntity) {
        if (!(sourceEntity instanceof EntityLivingBase)) {
            return;
        }
        EntityLivingBase attacker = (EntityLivingBase) sourceEntity;
        PotionEffect effect = attacker.getActivePotionEffect(ModPotions.BAT_LIFESTEAL);
        if (effect != null) {
            attacker.heal(1.0F + effect.getAmplifier());
        }
    }

    @SubscribeEvent
    public static void onSaplingGrowTree(SaplingGrowTreeEvent event) {
        World world = event.getWorld();
        if (world == null || world.isRemote) {
            return;
        }
        BlockPos pos = event.getPos();
        AxisAlignedBB bounds = new AxisAlignedBB(pos).grow(30.0D);
        List<EntityBeaverFamiliar> beavers = world.getEntitiesWithinAABB(EntityBeaverFamiliar.class, bounds,
                beaver -> beaver != null && beaver.isEntityAlive() && !beaver.isSitting()
                        && beaver.isOwnerNearForEffect());
        if (!beavers.isEmpty()) {
            EntityBeaverFamiliar beaver = beavers.get(world.rand.nextInt(beavers.size()));
            beaver.setTreeTarget(pos);
        }
    }

    public static boolean isLog(IBlockState state, World world, BlockPos pos) {
        return state != null && world != null && pos != null && isLogBlock(state.getBlock(), world, pos);
    }

    public static boolean isLogBlock(Block block, World world, BlockPos pos) {
        return block != null && block.isWood(world, pos);
    }

    @Nullable
    private static EntityGuardianFamiliar findGuardianFor(EntityPlayer player) {
        List<EntityGuardianFamiliar> guardians = FamiliarUtil.getAllFamiliars(player, EntityGuardianFamiliar.class,
                guardian -> guardian != null && guardian.isEntityAlive() && !guardian.isSitting()
                        && guardian.isTamed() && guardian.isOwner(player) && guardian.getLives() > 0);
        EntityGuardianFamiliar best = null;
        double bestDistance = Double.MAX_VALUE;
        for (EntityGuardianFamiliar guardian : guardians) {
            double distance = guardian.getDistanceSq(player);
            if (best == null || guardian.getLives() < best.getLives()
                    || guardian.getLives() == best.getLives() && distance < bestDistance) {
                best = guardian;
                bestDistance = distance;
            }
        }
        return best;
    }

    private static boolean stealHeadFor(EntityPlayer player, EntityLivingBase killed) {
        boolean stolen = false;
        for (EntityHeadlessFamiliar headless : findHeadlessFor(player, killed, false)) {
            stolen |= headless.stealHeadFrom(killed);
        }
        return stolen;
    }

    private static boolean hasMatchingHeadless(EntityPlayer player, EntityLivingBase target) {
        for (EntityHeadlessFamiliar headless : findHeadlessFor(player, target, true)) {
            if (headless.hasMatchingHead(target)) {
                return true;
            }
        }
        return false;
    }

    private static List<EntityHeadlessFamiliar> findHeadlessFor(EntityPlayer player, EntityLivingBase target,
            boolean requireHead) {
        return FamiliarUtil.getAllFamiliars(player, EntityHeadlessFamiliar.class,
                headless -> headless != null && headless.isEntityAlive() && !headless.isHeadlessDead()
                        && headless.isTamed() && headless.isOwner(player) && (!requireHead || headless.hasHead()));
    }
}
