package com.occultism.occultism.entity;

import java.util.UUID;

import javax.annotation.Nullable;

import com.occultism.occultism.advancement.FamiliarTrigger;
import com.occultism.occultism.registry.ModAdvancements;
import com.occultism.occultism.registry.ModPotions;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIFollowOwner;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISit;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

public class EntityMummyFamiliar extends EntityFamiliarBase implements IBlacksmithUpgradable {

    private static final DataParameter<Byte> VARIANTS =
            EntityDataManager.createKey(EntityMummyFamiliar.class, DataSerializers.BYTE);
    private static final DataParameter<Boolean> BLACKSMITH_UPGRADE =
            EntityDataManager.createKey(EntityMummyFamiliar.class, DataSerializers.BOOLEAN);
    private static final UUID BLACKSMITH_UPGRADE_MODIFIER_ID =
            UUID.fromString("6aa62086-7009-402b-9c13-c2de74bf077d");
    private static final AttributeModifier BLACKSMITH_UPGRADE_MODIFIER =
            new AttributeModifier(BLACKSMITH_UPGRADE_MODIFIER_ID,
                    "Occultism mummy familiar blacksmith upgrade", 3.0D, 0).setSaved(true);
    private static final double OWNER_EFFECT_RANGE_SQ = 64.0D;

    public EntityMummyFamiliar(World worldIn) {
        super(worldIn);
        this.setSize(0.6F, 1.0F);
        this.enablePersistence();
        this.experienceValue = 2;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(VARIANTS, Byte.valueOf((byte) 0));
        this.dataManager.register(BLACKSMITH_UPGRADE, Boolean.FALSE);
    }

    @Override
    protected void initEntityAI() {
        this.aiSit = new EntityAISit(this);
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(0, new EntityAIPanic(this, 1.25D));
        this.tasks.addTask(1, this.aiSit);
        this.tasks.addTask(2, new EntityAIAttackMelee(this, 1.25D, true));
        this.tasks.addTask(3, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(4, new EntityAIFollowOwner(this, 1.0D, 4.0F, 1.0F));
        this.tasks.addTask(7, new EntityAIWanderAvoidWater(this, 1.0D));
        this.targetTasks.addTask(0, new OwnerAttackTargetGoal(this));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(15.0D);
        if (this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) == null) {
            this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(9.0D);
        } else {
            this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(9.0D);
        }
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(30.0D);
    }

    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        IEntityLivingData data = super.onInitialSpawn(difficulty, livingdata);
        this.setCrown(this.rand.nextDouble() < 0.1D);
        this.setTooth(this.rand.nextBoolean());
        this.setHeka(this.rand.nextBoolean());
        return data;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (!this.world.isRemote && this.ticksExisted % 10 == 0 && this.isOwnerNearForEffect()) {
            EntityLivingBase owner = this.getOwner();
            owner.addPotionEffect(new PotionEffect(ModPotions.MUMMY_DODGE, 300, 0, false, true));
        }
    }

    @Override
    public Iterable<PotionEffect> getFamiliarEffects() {
        return java.util.Collections.singletonList(
                new PotionEffect(ModPotions.MUMMY_DODGE, 300, 0, false, true));
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        if (hand == EnumHand.MAIN_HAND && this.isOwner(player) && player.getHeldItem(hand).isEmpty()) {
            this.setSitting(!this.isSitting());
            if (this.aiSit != null) {
                this.aiSit.setSitting(this.isSitting());
            }
            return true;
        }
        return super.processInteract(player, hand);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.isEntityInvulnerable(source)) {
            return false;
        }
        this.setSitting(false);
        if (this.aiSit != null) {
            this.aiSit.setSitting(false);
        }
        return super.attackEntityFrom(source, amount);
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return null;
    }

    @Override
    @Nullable
    public EntityAgeable createChild(EntityAgeable ageable) {
        return null;
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.dataManager.set(VARIANTS, Byte.valueOf(compound.getByte("OccultismMummyVariants")));
        this.setBlacksmithUpgrade(compound.getBoolean("hasBlacksmithUpgrade"));
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setByte("OccultismMummyVariants", this.dataManager.get(VARIANTS).byteValue());
        compound.setBoolean("hasBlacksmithUpgrade", this.hasBlacksmithUpgrade());
    }

    public boolean isOwnerNearForEffect() {
        EntityLivingBase owner = this.getOwner();
        return owner != null && owner.isEntityAlive() && this.getDistanceSq(owner) < OWNER_EFFECT_RANGE_SQ;
    }

    @Override
    public void setFamiliarOwner(EntityLivingBase owner) {
        super.setFamiliarOwner(owner);
        if (owner != null && this.world != null && !this.world.isRemote && this.hasCrown()) {
            ModAdvancements.FAMILIAR.trigger(owner, FamiliarTrigger.Type.RARE_VARIANT);
        }
    }

    public boolean hasBlacksmithUpgrade() {
        return this.dataManager.get(BLACKSMITH_UPGRADE).booleanValue();
    }

    @Override
    @Nullable
    public EntityLivingBase getFamiliarOwner() {
        return this.getOwner();
    }

    @Override
    public EntityLivingBase getFamiliarEntity() {
        return this;
    }

    @Override
    public boolean canBlacksmithUpgrade() {
        return !this.hasBlacksmithUpgrade();
    }

    @Override
    public void blacksmithUpgrade() {
        if (!this.canBlacksmithUpgrade()) {
            return;
        }
        this.setBlacksmithUpgrade(true);
    }

    public boolean hasCrown() {
        return this.hasVariant(0);
    }

    public boolean hasHeka() {
        return this.hasVariant(1);
    }

    public boolean hasTooth() {
        return this.hasVariant(2);
    }

    private void setCrown(boolean crown) {
        this.setVariant(0, crown);
    }

    private void setHeka(boolean heka) {
        this.setVariant(1, heka);
    }

    private void setTooth(boolean tooth) {
        this.setVariant(2, tooth);
    }

    private void setBlacksmithUpgrade(boolean value) {
        this.dataManager.set(BLACKSMITH_UPGRADE, Boolean.valueOf(value));
        IAttributeInstance attackDamage = this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        AttributeModifier existing = attackDamage.getModifier(BLACKSMITH_UPGRADE_MODIFIER_ID);
        if (value && existing == null) {
            attackDamage.applyModifier(BLACKSMITH_UPGRADE_MODIFIER);
        } else if (!value && existing != null) {
            attackDamage.removeModifier(existing);
        }
    }

    private void setVariant(int offset, boolean value) {
        byte variants = this.dataManager.get(VARIANTS).byteValue();
        if (value) {
            variants = (byte) (variants | (1 << offset));
        } else {
            variants = (byte) (variants & ~(1 << offset));
        }
        this.dataManager.set(VARIANTS, Byte.valueOf(variants));
    }

    private boolean hasVariant(int offset) {
        return ((this.dataManager.get(VARIANTS).byteValue() >> offset) & 1) == 1;
    }

    private static class OwnerAttackTargetGoal extends EntityAITarget {

        private final EntityMummyFamiliar mummy;
        private EntityLivingBase target;
        private int timestamp;

        private OwnerAttackTargetGoal(EntityMummyFamiliar mummy) {
            super(mummy, false);
            this.mummy = mummy;
            this.setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            EntityLivingBase owner = this.mummy.getOwner();
            if (owner == null || !owner.isEntityAlive() || owner.getDistanceSq(this.mummy) >= 400.0D) {
                return false;
            }
            if (this.timestamp == owner.getLastAttackedEntityTime()) {
                return false;
            }
            EntityLivingBase attacked = owner.getLastAttackedEntity();
            if (attacked == null || attacked instanceof EntityPlayer || attacked instanceof EntityTameable) {
                return false;
            }
            this.target = attacked;
            return this.isSuitableTarget(this.target, false);
        }

        @Override
        public boolean shouldContinueExecuting() {
            EntityLivingBase owner = this.mummy.getOwner();
            return owner != null && owner.isEntityAlive() && owner.getDistanceSq(this.mummy) < 400.0D
                    && super.shouldContinueExecuting();
        }

        @Override
        public void startExecuting() {
            this.mummy.setAttackTarget(this.target);
            EntityLivingBase owner = this.mummy.getOwner();
            if (owner != null) {
                this.timestamp = owner.getLastAttackedEntityTime();
            }
            super.startExecuting();
        }
    }
}
