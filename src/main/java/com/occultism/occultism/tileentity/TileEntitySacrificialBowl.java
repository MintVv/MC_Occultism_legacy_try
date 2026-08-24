package com.occultism.occultism.tileentity;

import java.util.UUID;

import com.occultism.occultism.ritual.RitualDefinition;
import com.occultism.occultism.ritual.RitualEntityToSummon;
import com.occultism.occultism.ritual.RitualIngredient;
import com.occultism.occultism.entity.EntityBoundSpirit;
import com.occultism.occultism.entity.EntityFoliotJob;
import com.occultism.occultism.entity.IFamiliar;
import com.occultism.occultism.item.ItemBookOfBindingBound;
import com.occultism.occultism.item.ItemBookOfCallingFoliotJob;
import com.occultism.occultism.item.ItemFamiliarRing;
import com.occultism.occultism.item.ItemSoulShard;
import com.occultism.occultism.registry.ModBlocks;
import com.occultism.occultism.registry.ModAdvancements;
import com.occultism.occultism.registry.ModItems;
import com.occultism.occultism.spirit.SpiritJobType;
import com.occultism.occultism.util.ItemNBTUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.SoundEvents;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityParrot;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.world.WorldServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.event.ForgeEventFactory;

public class TileEntitySacrificialBowl extends TileEntity implements ITickable {

    private ItemStack stack = ItemStack.EMPTY;
    private String activeRitualId = "";
    private int ritualProgress;
    private int ritualDuration;
    private int consumedAdditionalIngredients;
    private ItemStack ritualResult = ItemStack.EMPTY;
    private boolean itemUseProvided;
    private UUID castingPlayerId;

    public ItemStack getStack() {
        return this.stack;
    }

    public boolean isEmpty() {
        return this.stack.isEmpty();
    }

    public void setStack(ItemStack stack) {
        this.stack = stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
        this.markDirtyAndSync();
    }

    public ItemStack removeStack() {
        ItemStack removed = this.stack.copy();
        this.stack = ItemStack.EMPTY;
        this.markDirtyAndSync();
        return removed;
    }

    public boolean isRitualActive() {
        return !this.activeRitualId.isEmpty();
    }

    public String getActiveRitualId() {
        return this.activeRitualId;
    }

    public int getRitualProgress() {
        return this.ritualProgress;
    }

    public int getRitualDuration() {
        return this.ritualDuration;
    }

    public boolean canProvideItemUse(ItemStack stack) {
        if (stack.isEmpty() || !this.isRitualActive()) {
            return false;
        }
        RitualDefinition definition = com.occultism.occultism.ritual.RitualRegistry.getById(this.activeRitualId);
        return definition != null && definition.requiresItemUse() && !this.itemUseProvided
                && definition.getItemToUse().matches(stack);
    }

    public boolean provideItemUse(ItemStack stack) {
        if (!this.canProvideItemUse(stack)) {
            return false;
        }
        this.itemUseProvided = true;
        this.playItemUseEffects();
        this.markDirtyAndSync();
        return true;
    }

    public boolean isWaitingForItemUse() {
        if (!this.isRitualActive()) {
            return false;
        }
        RitualDefinition definition = com.occultism.occultism.ritual.RitualRegistry.getById(this.activeRitualId);
        return definition != null && definition.requiresItemUse() && !this.itemUseProvided;
    }

    public boolean canStartRitual(net.minecraft.world.World world, net.minecraft.util.math.BlockPos center,
            RitualDefinition definition, ItemStack activationStack) {
        return definition.matchesAdditionalIngredients(this.getNearbyBowlStacks(world, center))
                && (!definition.requiresSacrifice() || this.hasMatchingSacrifice(world, center, definition))
                && this.canUseActivationItem(definition, activationStack);
    }

    public boolean hasMatchingSacrifice(net.minecraft.world.World world, net.minecraft.util.math.BlockPos center,
            RitualDefinition definition) {
        return this.findSacrifice(world, center, definition) != null;
    }

    public boolean consumeSacrifice(net.minecraft.world.World world, net.minecraft.util.math.BlockPos center,
            RitualDefinition definition) {
        if (!definition.requiresSacrifice()) {
            return true;
        }
        EntityLivingBase sacrifice = this.findSacrifice(world, center, definition);
        if (sacrifice == null) {
            return false;
        }
        this.playSacrificeEffects(sacrifice);
        sacrifice.attackEntityFrom(DamageSource.MAGIC, Float.MAX_VALUE);
        if (!sacrifice.isDead) {
            sacrifice.setDead();
        }
        return true;
    }

    public void startRitual(RitualDefinition definition, ItemStack activationStack) {
        this.startRitual(definition, activationStack, 1);
    }

    public void startRitual(RitualDefinition definition, ItemStack activationStack, int durationDivisor) {
        this.startRitual(definition, activationStack, durationDivisor, null);
    }

    public void startRitual(RitualDefinition definition, ItemStack activationStack, int durationDivisor,
            EntityPlayer castingPlayer) {
        if (this.isRitualActive()) {
            return;
        }
        this.stack = activationStack.copy();
        this.activeRitualId = definition.getId();
        this.ritualProgress = 0;
        this.ritualDuration = this.getAdjustedRitualDuration(definition, durationDivisor);
        this.consumedAdditionalIngredients = 0;
        this.ritualResult = definition.getResult().createStack();
        this.itemUseProvided = false;
        this.castingPlayerId = castingPlayer == null ? null : castingPlayer.getUniqueID();
        this.applyActivationItemData(definition, this.stack, this.ritualResult);
        this.applyRitualSpecificResultData(definition, this.ritualResult);
        this.markDirtyAndSync();
    }

    private int getAdjustedRitualDuration(RitualDefinition definition, int durationDivisor) {
        int divisor = Math.max(1, durationDivisor);
        int duration = Math.max(1, definition.getDurationTicks());
        long adjustedDuration = ((long) duration + divisor - 1L) / divisor;
        return (int) Math.max(1L, adjustedDuration);
    }

    @Override
    public void update() {
        if (!this.isRitualActive() || this.world == null) {
            return;
        }

        if (this.world.isRemote) {
            this.spawnClientParticles();
            return;
        }

        RitualDefinition definition = com.occultism.occultism.ritual.RitualRegistry.getById(this.activeRitualId);
        if (definition == null) {
            this.stopRitual(false);
            return;
        }
        if (definition.requiresItemUse() && !this.itemUseProvided) {
            this.spawnItemUseWaitingParticles();
            if (this.world.getTotalWorldTime() % 20 == 0) {
                this.markDirtyAndSync();
            }
            return;
        }

        this.ritualProgress++;
        this.spawnRitualMarkerParticles(definition);
        if (!this.consumePendingIngredients(definition)) {
            this.stopRitual(true);
            return;
        }
        if (this.ritualProgress >= this.ritualDuration) {
            this.completeRitual();
        } else if (this.ritualProgress % 20 == 0) {
            this.markDirtyAndSync();
        }
    }

    private void completeRitual() {
        RitualDefinition definition = com.occultism.occultism.ritual.RitualRegistry.getById(this.activeRitualId);
        this.triggerRitualAdvancement(definition);
        ItemStack repairResult = this.createRepairResult(definition);
        if (definition != null && this.isResurrectRitual(definition)) {
            if (!this.tryResurrectStoredEntity() && !this.stack.isEmpty()) {
                net.minecraft.inventory.InventoryHelper.spawnItemStack(this.world, this.pos.getX() + 0.5D,
                        this.pos.getY() + 0.7D, this.pos.getZ() + 0.5D, this.stack.copy());
            }
        } else if (definition != null && "repair".equals(definition.getRitualType())) {
            if (!repairResult.isEmpty()) {
                net.minecraft.inventory.InventoryHelper.spawnItemStack(this.world, this.pos.getX() + 0.5D,
                        this.pos.getY() + 0.7D, this.pos.getZ() + 0.5D, repairResult);
            }
        } else if (this.hasNonItemRitualResult(definition)) {
            // Non-item results are display metadata and must never become cancellation fallback drops.
            this.trySpawnRitualEntity();
        } else if (!this.ritualResult.isEmpty()) {
            net.minecraft.inventory.InventoryHelper.spawnItemStack(this.world, this.pos.getX() + 0.5D,
                    this.pos.getY() + 0.7D, this.pos.getZ() + 0.5D, this.ritualResult.copy());
        }
        this.world.playSound(null, this.pos, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 0.8F,
                1.2F);
        this.stack = ItemStack.EMPTY;
        this.clearRitualState();
        this.markDirtyAndSync();
    }

    private ItemStack createRepairResult(RitualDefinition definition) {
        if (definition == null || !"repair".equals(definition.getRitualType()) || this.stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack repaired = this.stack.copy();
        repaired.setCount(1);
        if (!repaired.isItemStackDamageable()) {
            return ItemStack.EMPTY;
        }
        repaired.setItemDamage(0);
        return repaired;
    }

    private boolean canUseActivationItem(RitualDefinition definition, ItemStack activationStack) {
        if (!this.isResurrectRitual(definition)) {
            return true;
        }
        return ItemSoulShard.hasStoredEntity(activationStack);
    }

    private boolean isResurrectRitual(RitualDefinition definition) {
        return definition != null && ("resurrect_familiar".equals(definition.getRitualType())
                || "resurrect".equals(definition.getRitualType()));
    }

    private boolean tryResurrectStoredEntity() {
        if (this.stack.isEmpty() || !ItemSoulShard.hasStoredEntity(this.stack)) {
            return false;
        }
        Entity entity = ItemSoulShard.createStoredEntity(this.stack, this.world);
        if (entity == null) {
            return false;
        }
        entity.dimension = this.world.provider.getDimension();
        entity.setLocationAndAngles(this.pos.getX() + 0.5D, this.pos.getY() + 1.0D, this.pos.getZ() + 0.5D,
                0.0F, 0.0F);
        if (entity instanceof EntityLivingBase) {
            EntityLivingBase living = (EntityLivingBase) entity;
            living.setRenderYawOffset(0.0F);
            living.setRotationYawHead(0.0F);
        }
        if (!this.world.spawnEntity(entity)) {
            return false;
        }
        EntityPlayer castingPlayer = this.findCastingPlayer();
        if (entity instanceof IFamiliar && castingPlayer != null) {
            ((IFamiliar) entity).setFamiliarOwner(castingPlayer);
        } else if (entity instanceof EntityBoundSpirit && castingPlayer != null) {
            ((EntityBoundSpirit) entity).setSpiritOwner(castingPlayer);
        }
        this.spawnEntitySummonParticles(entity);
        return true;
    }

    private EntityPlayer findCastingPlayer() {
        if (this.castingPlayerId == null || this.world == null) {
            return null;
        }
        EntityPlayer player = this.world.getPlayerEntityByUUID(this.castingPlayerId);
        if (player != null || this.world.getMinecraftServer() == null) {
            return player;
        }
        return this.world.getMinecraftServer().getPlayerList().getPlayerByUUID(this.castingPlayerId);
    }

    private boolean trySpawnRitualEntity() {
        RitualDefinition definition = com.occultism.occultism.ritual.RitualRegistry.getById(this.activeRitualId);
        if (definition != null && definition.hasSpiritJobType()
                && definition.getSpiritJobType().isWorldEffectJob()) {
            return this.applyWorldEffectJob(definition.getSpiritJobType());
        }

        SpiritJobType jobType = this.getFoliotJobRitualType(definition);
        if (jobType != null) {
            return this.spawnFoliotJobEntity(jobType, definition);
        }
        if (this.isWildHuntRitual(definition)) {
            return this.spawnWildHunt(definition);
        }
        // An explicit entity_to_summon is authoritative. This is required by
        // the unbound Afrit/Marid rituals, whose spirit_tier is only metadata.
        if (definition != null && definition.summonsEntity()) {
            return this.spawnConfiguredEntities(definition);
        }
        return definition != null && definition.hasSpiritTier() && this.spawnBoundSpiritEntity(definition);
    }

    private boolean hasNonItemRitualResult(RitualDefinition definition) {
        return definition != null
                && ((definition.hasSpiritJobType() && definition.getSpiritJobType().isWorldEffectJob())
                        || this.getFoliotJobRitualType(definition) != null
                        || definition.hasSpiritTier()
                        || this.isWildHuntRitual(definition)
                        || definition.summonsEntity());
    }

    private boolean isWildHuntRitual(RitualDefinition definition) {
        return definition != null && "summon_wild_hunt".equals(definition.getRitualType());
    }

    private boolean spawnWildHunt(RitualDefinition definition) {
        if (!definition.summonsEntity()) {
            return true;
        }
        for (int i = 0; i < 3; i++) {
            net.minecraft.util.ResourceLocation entityId = definition.getEntityToSummon().pickEntityId(this.world.rand);
            Entity entity = entityId == null ? null : this.createRitualEntity(definition, entityId);
            if (!(entity instanceof EntityLiving)) {
                continue;
            }
            EntityLiving boss = (EntityLiving) entity;
            double offsetX = this.world.rand.nextGaussian() * (1 + this.world.rand.nextInt(4));
            double offsetZ = this.world.rand.nextGaussian() * (1 + this.world.rand.nextInt(4));
            boss.setLocationAndAngles(this.pos.getX() + offsetX, this.pos.getY() + 1.5D,
                    this.pos.getZ() + offsetZ, this.world.rand.nextInt(360), 0.0F);
            boss.setCustomNameTag(ItemNBTUtil.generateName());
            boss.enablePersistence();
            this.prepareRitualLivingEntity(boss);
            this.triggerSummonedEntityAdvancement(boss);
            if (!this.world.spawnEntity(boss)) {
                continue;
            }
            this.spawnEntitySummonParticles(boss);
        }
        // The recipe result is display-only. Never drop a free skull when entity spawning is cancelled.
        return true;
    }

    private void prepareRitualLivingEntity(EntityLiving entity) {
        if (!ForgeEventFactory.doSpecialSpawn(entity, this.world, (float) entity.posX, (float) entity.posY,
                (float) entity.posZ)) {
            entity.onInitialSpawn(
                    this.world.getDifficultyForLocation(new net.minecraft.util.math.BlockPos(entity)), null);
        }
    }

    private void triggerSummonedEntityAdvancement(Entity entity) {
        for (EntityPlayerMP player : this.world.getEntitiesWithinAABB(EntityPlayerMP.class,
                entity.getEntityBoundingBox().grow(50.0D))) {
            CriteriaTriggers.SUMMONED_ENTITY.trigger(player, entity);
        }
    }

    private void triggerRitualAdvancement(RitualDefinition definition) {
        if (definition == null) {
            return;
        }
        EntityPlayer castingPlayer = this.findCastingPlayer();
        if (castingPlayer instanceof EntityPlayerMP) {
            ModAdvancements.RITUAL.trigger((EntityPlayerMP) castingPlayer, definition);
        }
    }

    private boolean applyWorldEffectJob(SpiritJobType jobType) {
        switch (jobType) {
            case CLEAR_WEATHER:
                this.world.getWorldInfo().setRaining(false);
                this.world.getWorldInfo().setThundering(false);
                this.world.getWorldInfo().setRainTime(12000);
                this.world.getWorldInfo().setThunderTime(12000);
                break;
            case RAIN_WEATHER:
                this.world.getWorldInfo().setRaining(true);
                this.world.getWorldInfo().setThundering(false);
                this.world.getWorldInfo().setRainTime(12000);
                this.world.getWorldInfo().setThunderTime(12000);
                break;
            case THUNDER_WEATHER:
                this.world.getWorldInfo().setRaining(true);
                this.world.getWorldInfo().setThundering(true);
                this.world.getWorldInfo().setRainTime(12000);
                this.world.getWorldInfo().setThunderTime(12000);
                break;
            case DAY_TIME:
                this.setTimeOfDay(1000L);
                break;
            case NIGHT_TIME:
                this.setTimeOfDay(13000L);
                break;
            default:
                return false;
        }

        if (this.world instanceof WorldServer) {
            ((WorldServer) this.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, this.pos.getX() + 0.5D,
                    this.pos.getY() + 1.0D, this.pos.getZ() + 0.5D, 32, 0.8D, 0.5D, 0.8D, 0.03D);
        }
        return true;
    }

    private boolean spawnBoundSpiritEntity(RitualDefinition definition) {
        EntityBoundSpirit spirit = EntityBoundSpirit.createForTier(this.world, definition.getSpiritTier());
        EntityPlayer castingPlayer = this.findCastingPlayer();
        if (castingPlayer != null) {
            spirit.setSpiritOwner(castingPlayer);
        }
        this.applyBoundSpiritName(spirit);
        spirit.setLocationAndAngles(this.pos.getX() + 0.5D, this.pos.getY() + 1.0D, this.pos.getZ() + 0.5D,
                this.world.rand.nextFloat() * 360.0F, 0.0F);
        this.prepareRitualLivingEntity(spirit);
        this.triggerSummonedEntityAdvancement(spirit);
        if (!this.world.spawnEntity(spirit)) {
            return false;
        }
        if (this.world instanceof WorldServer) {
            ((WorldServer) this.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, spirit.posX, spirit.posY + 0.8D,
                    spirit.posZ, 28, 0.5D, 0.5D, 0.5D, 0.02D);
            ((WorldServer) this.world).spawnParticle(EnumParticleTypes.PORTAL, spirit.posX, spirit.posY + 0.8D,
                    spirit.posZ, 20, 0.5D, 0.5D, 0.5D, 0.02D);
        }
        return true;
    }

    private void setTimeOfDay(long timeOfDay) {
        long current = this.world.getWorldTime();
        long dayStart = current - current % 24000L;
        long target = dayStart + timeOfDay;
        if (target <= current) {
            target += 24000L;
        }
        this.world.setWorldTime(target);
    }

    private boolean spawnFoliotJobEntity(SpiritJobType jobType, RitualDefinition definition) {
        com.occultism.occultism.spirit.SpiritTier tier = definition != null && definition.hasSpiritTier()
                ? definition.getSpiritTier() : com.occultism.occultism.spirit.SpiritTier.FOLIOT;
        EntityFoliotJob spirit = EntityFoliotJob.createForTier(this.world, jobType, tier);
        if (definition != null && definition.hasSpiritMaxAge()) {
            spirit.setSpiritMaxAge(definition.getSpiritMaxAge());
        }
        this.applyBoundSpiritName(spirit);
        spirit.setLocationAndAngles(this.pos.getX() + 0.5D, this.pos.getY() + 1.0D, this.pos.getZ() + 0.5D,
                this.world.rand.nextFloat() * 360.0F, 0.0F);
        this.prepareRitualLivingEntity(spirit);
        this.triggerSummonedEntityAdvancement(spirit);
        if (!this.world.spawnEntity(spirit)) {
            return false;
        }
        this.dropLinkedCallingBook(spirit);
        if (this.world instanceof WorldServer) {
            ((WorldServer) this.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, spirit.posX, spirit.posY + 0.6D,
                    spirit.posZ, 24, 0.4D, 0.4D, 0.4D, 0.02D);
            ((WorldServer) this.world).spawnParticle(EnumParticleTypes.PORTAL, spirit.posX, spirit.posY + 0.6D,
                    spirit.posZ, 16, 0.4D, 0.4D, 0.4D, 0.02D);
        }
        return true;
    }

    private boolean spawnConfiguredEntities(RitualDefinition definition) {
        RitualEntityToSummon entityToSummon = definition.getEntityToSummon();
        boolean spawnedAny = false;
        for (int i = 0; i < entityToSummon.getSummonNumber(); i++) {
            net.minecraft.util.ResourceLocation entityId = entityToSummon.pickEntityId(this.world.rand);
            if (entityId == null) {
                continue;
            }
            Entity entity = this.createRitualEntity(definition, entityId);
            if (entity == null) {
                continue;
            }
            double offsetX = entityToSummon.getSummonNumber() == 1 ? 0.0D : (this.world.rand.nextDouble() - 0.5D) * 1.5D;
            double offsetZ = entityToSummon.getSummonNumber() == 1 ? 0.0D : (this.world.rand.nextDouble() - 0.5D) * 1.5D;
            entity.setLocationAndAngles(this.pos.getX() + 0.5D + offsetX, this.pos.getY() + 1.0D,
                    this.pos.getZ() + 0.5D + offsetZ, this.world.rand.nextFloat() * 360.0F, 0.0F);
            this.applyTamedOwner(definition, entity);
            if (entity instanceof EntityLiving) {
                this.prepareRitualLivingEntity((EntityLiving) entity);
            }
            this.triggerSummonedEntityAdvancement(entity);
            if (!this.world.spawnEntity(entity)) {
                continue;
            }
            spawnedAny = true;
            this.spawnEntitySummonParticles(entity);
        }
        return spawnedAny;
    }

    private Entity createRitualEntity(RitualDefinition definition, net.minecraft.util.ResourceLocation entityId) {
        if (definition != null && "summon_with_chance_of_chicken_tamed".equals(definition.getRitualType())
                && this.world.rand.nextInt(3) != 0) {
            return EntityList.createEntityByIDFromName(new net.minecraft.util.ResourceLocation("minecraft:chicken"),
                    this.world);
        }
        return EntityList.createEntityByIDFromName(entityId, this.world);
    }

    private void applyTamedOwner(RitualDefinition definition, Entity entity) {
        if (definition != null && "resurrect_allay".equals(definition.getId())
                && entity instanceof IFamiliar && this.castingPlayerId != null) {
            EntityPlayer castingPlayer = this.findCastingPlayer();
            if (castingPlayer != null) {
                ((IFamiliar) entity).setFamiliarOwner(castingPlayer);
            }
            return;
        }
        if (definition == null || this.castingPlayerId == null || !(entity instanceof EntityTameable)
                || !this.isTamedSummonRitual(definition)) {
            return;
        }
        EntityTameable tameable = (EntityTameable) entity;
        tameable.setTamed(true);
        tameable.setOwnerId(this.castingPlayerId);
        if (this.isRitualFamiliarParrot(definition) && entity instanceof EntityParrot) {
            entity.getEntityData().setBoolean(ItemFamiliarRing.TAG_RITUAL_FAMILIAR_PARROT, true);
        }
    }

    private boolean isRitualFamiliarParrot(RitualDefinition definition) {
        String id = definition.getId();
        return "familiar_parrot".equals(id) || "familiar_otherworld_bird".equals(id);
    }

    private boolean isTamedSummonRitual(RitualDefinition definition) {
        String ritualType = definition.getRitualType();
        return "summon_tamed".equals(ritualType) || "summon_with_chance_of_chicken_tamed".equals(ritualType);
    }

    private void spawnEntitySummonParticles(Entity entity) {
        if (!(this.world instanceof WorldServer)) {
            return;
        }
        ((WorldServer) this.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, entity.posX, entity.posY + 0.6D,
                entity.posZ, 18, 0.4D, 0.4D, 0.4D, 0.02D);
        ((WorldServer) this.world).spawnParticle(EnumParticleTypes.PORTAL, entity.posX, entity.posY + 0.6D,
                entity.posZ, 12, 0.4D, 0.4D, 0.4D, 0.02D);
    }

    private void dropLinkedCallingBook(EntityFoliotJob spirit) {
        if (this.ritualResult.isEmpty() || !(this.ritualResult.getItem() instanceof ItemBookOfCallingFoliotJob)) {
            return;
        }

        ItemBookOfCallingFoliotJob bookItem = (ItemBookOfCallingFoliotJob) this.ritualResult.getItem();
        if (bookItem.getJobType() != spirit.getJobType()) {
            return;
        }
        if (bookItem.getSpiritTier() != spirit.getTier()) {
            return;
        }

        ItemStack book = this.ritualResult.copy();
        if (!this.stack.isEmpty()) {
            ItemNBTUtil.copyBoundSpiritName(this.stack, book);
        }
        bookItem.bindToSpirit(book, spirit);
        net.minecraft.inventory.InventoryHelper.spawnItemStack(this.world, this.pos.getX() + 0.5D,
                this.pos.getY() + 0.7D, this.pos.getZ() + 0.5D, book);
    }

    private void applyActivationItemData(RitualDefinition definition, ItemStack activationStack, ItemStack result) {
        if (result.isEmpty() || !this.shouldCopyBoundSpiritName(definition, activationStack)) {
            return;
        }
        ItemNBTUtil.copyBoundSpiritName(activationStack, result);
    }

    private void applyRitualSpecificResultData(RitualDefinition definition, ItemStack result) {
        if (result.isEmpty()) {
            return;
        }

        this.applySatchelUpgradeResultData(definition, result);
        this.applyStorageControllerUpgradeResultData(definition, result);
    }

    private void applySatchelUpgradeResultData(RitualDefinition definition, ItemStack result) {
        if (!"craft_upgrade_ritual_satchel".equals(definition.getId())
                || result.getItem() != ModItems.RITUAL_SATCHEL_T2) {
            return;
        }
        ItemStack sourceSatchel = this.findNearbyStack(ModItems.RITUAL_SATCHEL_T1);
        if (sourceSatchel.isEmpty() || sourceSatchel.getTagCompound() == null) {
            return;
        }
        result.setTagCompound(sourceSatchel.getTagCompound().copy());
    }

    private void applyStorageControllerUpgradeResultData(RitualDefinition definition, ItemStack result) {
        net.minecraft.item.Item sourceItem = ItemStack.EMPTY.getItem();
        if ("misc_stabilized_storage".equals(definition.getId())
                && result.getItem() == net.minecraft.item.Item.getItemFromBlock(ModBlocks.STORAGE_CONTROLLER_STABILIZED)) {
            sourceItem = net.minecraft.item.Item.getItemFromBlock(ModBlocks.STORAGE_CONTROLLER);
        } else if ("misc_stabilized_storage_dark".equals(definition.getId())
                && result.getItem() == net.minecraft.item.Item.getItemFromBlock(ModBlocks.STORAGE_CONTROLLER_STABILIZED_DARK)) {
            sourceItem = net.minecraft.item.Item.getItemFromBlock(ModBlocks.STORAGE_CONTROLLER_DARK);
        }
        if (sourceItem == ItemStack.EMPTY.getItem()) {
            return;
        }

        ItemStack sourceController = this.findNearbyStack(sourceItem);
        if (sourceController.isEmpty() || sourceController.getTagCompound() == null) {
            return;
        }
        result.setTagCompound(sourceController.getTagCompound().copy());
    }

    private ItemStack findNearbyStack(net.minecraft.item.Item item) {
        for (TileEntitySacrificialBowl bowl : this.getNearbySacrificialBowls(this.world, this.pos)) {
            ItemStack stack = bowl.getStack();
            if (!stack.isEmpty() && stack.getItem() == item) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private boolean shouldCopyBoundSpiritName(RitualDefinition definition, ItemStack activationStack) {
        String ritualType = definition.getRitualType();
        boolean copiesSpiritName = "craft_with_spirit_name".equals(ritualType)
                || "craft_miner_spirit".equals(ritualType);
        if (!copiesSpiritName) {
            return false;
        }
        return activationStack.getItem() instanceof ItemBookOfBindingBound
                || ItemNBTUtil.hasBoundSpiritName(activationStack);
    }

    private void applyBoundSpiritName(EntityLivingBase spirit) {
        if (this.stack.isEmpty()) {
            return;
        }
        String name = this.stack.getItem() instanceof ItemBookOfBindingBound
                ? ItemNBTUtil.getOrCreateBoundSpiritName(this.stack)
                : ItemNBTUtil.getBoundSpiritName(this.stack);
        if (!name.isEmpty()) {
            spirit.setCustomNameTag(name);
            spirit.setAlwaysRenderNameTag(true);
        }
    }

    private SpiritJobType getFoliotJobRitualType(RitualDefinition definition) {
        if (definition != null && definition.hasSpiritJobType()) {
            return definition.getSpiritJobType().isFoliotJob() ? definition.getSpiritJobType() : null;
        }
        if ("summon_foliot_cleaner".equals(this.activeRitualId)) {
            return SpiritJobType.CLEANER;
        }
        if ("summon_foliot_farmer".equals(this.activeRitualId)) {
            return SpiritJobType.FARMER;
        }
        if ("summon_foliot_transport_items".equals(this.activeRitualId)) {
            return SpiritJobType.TRANSPORT_ITEMS;
        }
        if ("summon_foliot_lumberjack".equals(this.activeRitualId)) {
            return SpiritJobType.LUMBERJACK;
        }
        if ("summon_foliot_smelter".equals(this.activeRitualId)) {
            return SpiritJobType.SMELTER;
        }
        if ("summon_foliot_otherstone_trader".equals(this.activeRitualId)) {
            return SpiritJobType.TRADER_OTHERSTONE;
        }
        if ("summon_foliot_otherrock_trader".equals(this.activeRitualId)) {
            return SpiritJobType.TRADER_OTHERROCK;
        }
        if ("summon_foliot_sapling_trader".equals(this.activeRitualId)) {
            return SpiritJobType.TRADER_SAPLING;
        }
        if ("summon_foliot_crusher".equals(this.activeRitualId)
                || (!this.ritualResult.isEmpty() && this.ritualResult.getItem() == ModItems.FOLIOT_CRUSHER_TOKEN)) {
            return SpiritJobType.CRUSHER;
        }
        if ("summon_foliot_crystallizer".equals(this.activeRitualId)
                || (!this.ritualResult.isEmpty()
                        && this.ritualResult.getItem() == ModItems.FOLIOT_CRYSTALLIZER_TOKEN)) {
            return SpiritJobType.CRYSTALLIZER;
        }
        return null;
    }

    private void stopRitual(boolean dropActivationItem) {
        if (dropActivationItem && !this.stack.isEmpty()) {
            net.minecraft.inventory.InventoryHelper.spawnItemStack(this.world, this.pos.getX() + 0.5D,
                    this.pos.getY() + 0.7D, this.pos.getZ() + 0.5D, this.stack.copy());
        }
        this.stack = ItemStack.EMPTY;
        this.clearRitualState();
        this.markDirtyAndSync();
    }

    private void clearRitualState() {
        this.activeRitualId = "";
        this.ritualProgress = 0;
        this.ritualDuration = 0;
        this.consumedAdditionalIngredients = 0;
        this.ritualResult = ItemStack.EMPTY;
        this.itemUseProvided = false;
        this.castingPlayerId = null;
    }

    private java.util.List<ItemStack> getNearbyBowlStacks(net.minecraft.world.World world,
            net.minecraft.util.math.BlockPos center) {
        java.util.List<ItemStack> stacks = new java.util.ArrayList<>();
        for (TileEntitySacrificialBowl bowl : this.getNearbySacrificialBowls(world, center)) {
            if (!bowl.getStack().isEmpty()) {
                stacks.add(bowl.getStack());
            }
        }
        return stacks;
    }

    private EntityLivingBase findSacrifice(net.minecraft.world.World world, net.minecraft.util.math.BlockPos center,
            RitualDefinition definition) {
        if (world == null || !definition.requiresSacrifice()) {
            return null;
        }
        net.minecraft.util.math.AxisAlignedBB box = new net.minecraft.util.math.AxisAlignedBB(center).grow(8.0D, 4.0D,
                8.0D);
        java.util.List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, box,
                entity -> definition.getSacrifice().matches(entity));
        return entities.isEmpty() ? null : entities.get(0);
    }

    private void playSacrificeEffects(EntityLivingBase sacrifice) {
        if (this.world instanceof WorldServer) {
            ((WorldServer) this.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, sacrifice.posX,
                    sacrifice.posY + sacrifice.height * 0.5D, sacrifice.posZ, 24, 0.5D, 0.5D, 0.5D, 0.02D);
            ((WorldServer) this.world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, sacrifice.posX,
                    sacrifice.posY + sacrifice.height * 0.5D, sacrifice.posZ, 8, 0.3D, 0.3D, 0.3D, 0.01D);
        }
        this.world.playSound(null, sacrifice.getPosition(), SoundEvents.ENTITY_WITHER_HURT, SoundCategory.HOSTILE,
                0.6F, 1.4F);
    }

    private boolean consumePendingIngredients(RitualDefinition definition) {
        int requiredConsumed = this.getRequiredConsumedIngredientCount(definition);
        while (this.consumedAdditionalIngredients < requiredConsumed) {
            RitualIngredient ingredient = definition.getAdditionalIngredients().get(this.consumedAdditionalIngredients);
            if (!this.consumeIngredient(ingredient)) {
                return false;
            }
            this.consumedAdditionalIngredients++;
        }
        return true;
    }

    private int getRequiredConsumedIngredientCount(RitualDefinition definition) {
        int ingredientCount = definition.getAdditionalIngredients().size();
        if (ingredientCount == 0) {
            return 0;
        }
        int interval = Math.max(1, this.ritualDuration / (ingredientCount + 1));
        return Math.min(ingredientCount, this.ritualProgress / interval);
    }

    private boolean consumeIngredient(RitualIngredient ingredient) {
        TileEntitySacrificialBowl bowl = this.findBowlWithIngredient(ingredient);
        if (bowl == null) {
            return false;
        }

        ItemStack bowlStack = bowl.getStack();
        ingredient.consume(bowlStack);
        if (bowlStack.isEmpty()) {
            bowl.setStack(ItemStack.EMPTY);
        } else {
            bowl.setStack(bowlStack);
        }
        this.playIngredientConsumeEffects(bowl.getPos());
        return true;
    }

    private TileEntitySacrificialBowl findBowlWithIngredient(RitualIngredient ingredient) {
        java.util.List<TileEntitySacrificialBowl> bowls = this.getNearbySacrificialBowls(this.world, this.pos);
        for (TileEntitySacrificialBowl bowl : bowls) {
            if (ingredient.matches(bowl.getStack())) {
                return bowl;
            }
        }
        return null;
    }

    private void playIngredientConsumeEffects(net.minecraft.util.math.BlockPos bowlPos) {
        if (this.world instanceof WorldServer) {
            ((WorldServer) this.world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, bowlPos.getX() + 0.5D,
                    bowlPos.getY() + 1.5D, bowlPos.getZ() + 0.5D, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        this.world.playSound(null, bowlPos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.7F, 0.7F);
    }

    private void playItemUseEffects() {
        if (this.world instanceof WorldServer) {
            ((WorldServer) this.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, this.pos.getX() + 0.5D,
                    this.pos.getY() + 0.9D, this.pos.getZ() + 0.5D, 24, 0.5D, 0.3D, 0.5D, 0.02D);
            ((WorldServer) this.world).spawnParticle(EnumParticleTypes.PORTAL, this.pos.getX() + 0.5D,
                    this.pos.getY() + 0.9D, this.pos.getZ() + 0.5D, 16, 0.5D, 0.3D, 0.5D, 0.02D);
        }
        this.world.playSound(null, this.pos, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 0.6F,
                1.4F);
    }

    private void spawnItemUseWaitingParticles() {
        if (!(this.world instanceof WorldServer) || this.world.rand.nextInt(12) != 0) {
            return;
        }
        ((WorldServer) this.world).spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE, this.pos.getX() + 0.5D,
                this.pos.getY() + 0.8D, this.pos.getZ() + 0.5D, 4, 0.5D, 0.2D, 0.5D, 0.02D);
    }

    private void spawnRitualMarkerParticles(RitualDefinition definition) {
        if (!(this.world instanceof WorldServer) || this.ritualProgress % 5 != 0) {
            return;
        }

        net.minecraft.util.math.BlockPos markerPos = this.pos;
        if (this.consumedAdditionalIngredients < definition.getAdditionalIngredients().size()) {
            RitualIngredient nextIngredient = definition.getAdditionalIngredients().get(this.consumedAdditionalIngredients);
            TileEntitySacrificialBowl targetBowl = this.findBowlWithIngredient(nextIngredient);
            if (targetBowl == null) {
                return;
            }
            markerPos = targetBowl.getPos();
        }

        this.spawnSpiritFireMarker((WorldServer) this.world, markerPos);
    }

    private void spawnSpiritFireMarker(WorldServer worldServer, net.minecraft.util.math.BlockPos markerPos) {
        double gameTime = this.world.getTotalWorldTime() * 0.05D;
        double sin = Math.sin(gameTime) * 0.3D;
        double cos = Math.cos(gameTime) * 0.3D;
        double x = markerPos.getX() + 0.5D;
        double y = markerPos.getY() + 0.7D;
        double z = markerPos.getZ() + 0.5D;

        worldServer.spawnParticle(EnumParticleTypes.SPELL_WITCH, x + cos, y + cos, z + sin, 1, 0.0D, 0.0D, 0.0D,
                0.003D);
        worldServer.spawnParticle(EnumParticleTypes.PORTAL, x + cos * 0.7D, y, z + sin * 0.7D, 1, 0.0D, 0.0D,
                0.0D, 0.003D);
    }

    private java.util.List<TileEntitySacrificialBowl> getNearbySacrificialBowls(net.minecraft.world.World world,
            net.minecraft.util.math.BlockPos center) {
        java.util.List<TileEntitySacrificialBowl> bowls = new java.util.ArrayList<>();
        if (world == null) {
            return bowls;
        }
        for (int x = -8; x <= 8; x++) {
            for (int z = -8; z <= 8; z++) {
                if (x == 0 && z == 0) {
                    continue;
                }
                net.minecraft.util.math.BlockPos checkPos = center.add(x, 0, z);
                if (world.getBlockState(checkPos).getBlock() == com.occultism.occultism.registry.ModBlocks.SACRIFICIAL_BOWL
                        && world.getTileEntity(checkPos) instanceof TileEntitySacrificialBowl) {
                    bowls.add((TileEntitySacrificialBowl) world.getTileEntity(checkPos));
                }
            }
        }
        return bowls;
    }

    private void spawnClientParticles() {
        if (this.world.rand.nextInt(3) == 0) {
            double x = this.pos.getX() + 0.5D + (this.world.rand.nextDouble() - 0.5D) * 0.6D;
            double y = this.pos.getY() + 0.6D + this.world.rand.nextDouble() * 0.25D;
            double z = this.pos.getZ() + 0.5D + (this.world.rand.nextDouble() - 0.5D) * 0.6D;
            this.world.spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE, x, y, z, 0.0D, 0.04D, 0.0D);
        }
    }

    private void markDirtyAndSync() {
        this.markDirty();
        if (this.world != null && !this.world.isRemote) {
            this.world.notifyBlockUpdate(this.pos, this.world.getBlockState(this.pos), this.world.getBlockState(this.pos),
                    3);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        if (!this.stack.isEmpty()) {
            compound.setTag("Item", this.stack.writeToNBT(new NBTTagCompound()));
        }
        compound.setString("ActiveRitual", this.activeRitualId);
        compound.setInteger("RitualProgress", this.ritualProgress);
        compound.setInteger("RitualDuration", this.ritualDuration);
        compound.setInteger("ConsumedAdditionalIngredients", this.consumedAdditionalIngredients);
        if (!this.ritualResult.isEmpty()) {
            compound.setTag("RitualResult", this.ritualResult.writeToNBT(new NBTTagCompound()));
        }
        compound.setBoolean("ItemUseProvided", this.itemUseProvided);
        if (this.castingPlayerId != null) {
            compound.setString("CastingPlayerUUID", this.castingPlayerId.toString());
        }
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("Item")) {
            this.stack = new ItemStack(compound.getCompoundTag("Item"));
        } else {
            this.stack = ItemStack.EMPTY;
        }
        this.activeRitualId = compound.getString("ActiveRitual");
        if ("wild_hunt".equals(this.activeRitualId)) {
            this.activeRitualId = "summon_wild_hunt";
        }
        this.ritualProgress = compound.getInteger("RitualProgress");
        this.ritualDuration = compound.getInteger("RitualDuration");
        this.consumedAdditionalIngredients = compound.getInteger("ConsumedAdditionalIngredients");
        if (compound.hasKey("RitualResult")) {
            this.ritualResult = new ItemStack(compound.getCompoundTag("RitualResult"));
        } else {
            this.ritualResult = ItemStack.EMPTY;
        }
        this.itemUseProvided = compound.getBoolean("ItemUseProvided");
        if (compound.hasKey("CastingPlayerUUID")) {
            try {
                this.castingPlayerId = UUID.fromString(compound.getString("CastingPlayerUUID"));
            } catch (IllegalArgumentException e) {
                this.castingPlayerId = null;
            }
        } else {
            this.castingPlayerId = null;
        }
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return this.writeToNBT(new NBTTagCompound());
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.pos, 0, this.getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.getNbtCompound());
    }
}
