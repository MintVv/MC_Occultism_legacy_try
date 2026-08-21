package com.occultism.occultism.entity;

import com.occultism.occultism.item.ItemSoulShard;
import com.occultism.occultism.registry.ModItems;
import com.occultism.occultism.spirit.SpiritTier;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class EntityBoundSpirit extends EntityCreature {

    private static final DataParameter<Integer> TIER =
            EntityDataManager.createKey(EntityBoundSpirit.class, DataSerializers.VARINT);

    private boolean droppedSoulShard;

    public EntityBoundSpirit(World worldIn) {
        super(worldIn);
        this.setSize(0.7F, 1.6F);
        this.setCanPickUpLoot(false);
        this.enablePersistence();
    }

    public EntityBoundSpirit(World worldIn, SpiritTier tier) {
        this(worldIn);
        this.setTier(tier);
        this.setHealth(this.getMaxHealth());
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(TIER, Integer.valueOf(SpiritTier.DJINNI.ordinal()));
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(5, new EntityAIWander(this, 0.8D));
        this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(7, new EntityAILookIdle(this));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.applyTierAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(20.0D);
    }

    @Override
    protected boolean canDespawn() {
        return false;
    }

    @Override
    public boolean isEntityInvulnerable(DamageSource source) {
        return super.isEntityInvulnerable(source) || source == DamageSource.IN_WALL
                || source == DamageSource.FLY_INTO_WALL;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.isEntityInvulnerable(source)) {
            return false;
        }
        Entity trueSource = source == null ? null : source.getTrueSource();
        Entity immediateSource = source == null ? null : source.getImmediateSource();
        if (trueSource != null && !(trueSource instanceof EntityPlayer)
                && !(trueSource instanceof EntityArrow) && !(immediateSource instanceof EntityArrow)) {
            amount = (amount + 1.0F) / 2.0F;
        }
        return super.attackEntityFrom(source, amount);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (this.world == null || !this.world.isRemote || this.ticksExisted % 12 != 0) {
            return;
        }
        this.world.spawnParticle(net.minecraft.util.EnumParticleTypes.SPELL_WITCH,
                this.posX + (this.rand.nextDouble() - 0.5D) * 0.45D,
                this.posY + 0.9D + this.rand.nextDouble() * 0.5D,
                this.posZ + (this.rand.nextDouble() - 0.5D) * 0.45D,
                0.0D, 0.01D, 0.0D);
    }

    @Override
    public void onDeath(DamageSource cause) {
        super.onDeath(cause);
        if (!this.world.isRemote && !this.droppedSoulShard) {
            this.dropSoulShard();
        }
    }

    public SpiritTier getTier() {
        int index = this.dataManager.get(TIER).intValue();
        SpiritTier[] values = SpiritTier.values();
        if (index < 0 || index >= values.length) {
            return SpiritTier.DJINNI;
        }
        return values[index];
    }

    public void setTier(SpiritTier tier) {
        this.dataManager.set(TIER, Integer.valueOf((tier == null ? SpiritTier.DJINNI : tier).ordinal()));
        this.applyTierAttributes();
    }

    private void applyTierAttributes() {
        if (this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH) == null) {
            return;
        }
        SpiritTier tier = this.getTier();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(tier.getMaxHealth());
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(tier.getMovementSpeed());
        if (this.getHealth() > this.getMaxHealth()) {
            this.setHealth(this.getMaxHealth());
        }
    }

    private void dropSoulShard() {
        this.droppedSoulShard = true;
        ItemStack shard = new ItemStack(ModItems.SOUL_SHARD);
        if (!ItemSoulShard.storeEntity(shard, this)) {
            return;
        }
        EntityItem shardEntity = new EntityItem(this.world, this.posX, this.posY + 0.5D, this.posZ, shard);
        shardEntity.setPickupDelay(5);
        shardEntity.motionX = 0.0D;
        shardEntity.motionZ = 0.0D;
        this.spawnEntityItem(shardEntity);
    }

    private void spawnEntityItem(EntityItem item) {
        if (this.captureDrops) {
            this.capturedDrops.add(item);
        } else {
            this.world.spawnEntity(item);
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("SpiritTier", this.getTier().ordinal());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        int tier = compound.getInteger("SpiritTier");
        SpiritTier[] values = SpiritTier.values();
        this.setTier(tier >= 0 && tier < values.length ? values[tier] : SpiritTier.DJINNI);
    }
}
