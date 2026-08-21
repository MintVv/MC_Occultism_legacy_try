package com.occultism.occultism.entity;

import javax.annotation.Nullable;

import com.occultism.occultism.advancement.FamiliarTrigger;
import com.occultism.occultism.registry.ModAdvancements;
import com.occultism.occultism.registry.ModItems;

import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIEatGrass;
import net.minecraft.entity.ai.EntityAIFollowOwner;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISit;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
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

public class EntityDeerFamiliar extends EntityFamiliarBase implements IBlacksmithUpgradable {

    private static final DataParameter<Byte> VARIANTS =
            EntityDataManager.createKey(EntityDeerFamiliar.class, DataSerializers.BYTE);
    private static final DataParameter<Boolean> BLACKSMITH_UPGRADE =
            EntityDataManager.createKey(EntityDeerFamiliar.class, DataSerializers.BOOLEAN);
    private static final double OWNER_EFFECT_RANGE_SQ = 64.0D;
    private static final double OWNER_SPEED_BOOST_RANGE_SQ = 50.0D;
    private static final double NORMAL_SPEED = 0.3D;
    private static final double BOOSTED_SPEED = 0.45D;
    private static final float STEP_HEIGHT = 1.0F;

    private EntityAIEatGrass eatGrassAI;
    private int eatingAnimationTimer;
    private int neckRotTimer;
    private int prevNeckRotTimer;

    public EntityDeerFamiliar(World worldIn) {
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
        this.eatGrassAI = new EntityAIEatGrass(this);
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(0, new EntityAIPanic(this, 1.25D));
        this.tasks.addTask(1, this.aiSit);
        this.tasks.addTask(2, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(3, new EntityAIFollowOwner(this, 1.0D, 3.0F, 1.0F));
        this.tasks.addTask(4, this.eatGrassAI);
        this.tasks.addTask(5, new EntityDevilFamiliar.AttackGoal(this, 5.0F) {
            @Override
            public boolean shouldExecute() {
                return super.shouldExecute() && EntityDeerFamiliar.this.hasBlacksmithUpgrade();
            }
        });
        this.tasks.addTask(6, new EntityAIWanderAvoidWater(this, 1.0D));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(NORMAL_SPEED);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(15.0D);
    }

    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        IEntityLivingData data = super.onInitialSpawn(difficulty, livingdata);
        this.setRedNose(this.rand.nextDouble() < 0.1D);
        if (this.hasRedNose()) {
            this.setGlowing(true);
        }
        return data;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (this.eatingAnimationTimer > 0) {
            this.eatingAnimationTimer--;
        }
        this.prevNeckRotTimer = this.neckRotTimer;
        if (this.isEating()) {
            this.neckRotTimer = Math.min(this.neckRotTimer + 1, 10);
        } else {
            this.neckRotTimer = Math.max(this.neckRotTimer - 1, 0);
        }
        if (!this.world.isRemote) {
            if (this.hasRedNose() && !this.isGlowing()) {
                this.setGlowing(true);
            }
            this.updateOwnerEffects();
            this.updateSpeedBoost();
        }
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        if (hand == EnumHand.MAIN_HAND && this.isOwner(player) && player.getHeldItem(hand).isEmpty()) {
            this.setSitting(!this.isSitting());
            if (this.aiSit != null) {
                this.aiSit.setSitting(this.isSitting());
            }
            this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_SHEEP_AMBIENT,
                    this.getSoundCategory(), 0.3F, 1.4F);
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
    public void eatGrassBonus() {
        super.eatGrassBonus();
        if (!this.world.isRemote && this.rand.nextDouble() < 0.25D) {
            this.entityDropItem(new ItemStack(ModItems.DATURA_SEEDS), 0.0F);
            ModAdvancements.FAMILIAR.trigger(this.getFamiliarOwner(), FamiliarTrigger.Type.DEER_POOP);
        }
    }

    @Override
    public void handleStatusUpdate(byte id) {
        if (id == 10) {
            this.eatingAnimationTimer = 40;
            this.neckRotTimer = 0;
            this.prevNeckRotTimer = 0;
            return;
        }
        super.handleStatusUpdate(id);
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
        this.dataManager.set(VARIANTS, Byte.valueOf(compound.getByte("OccultismDeerVariants")));
        this.setBlacksmithUpgrade(compound.getBoolean("hasBlacksmithUpgrade"));
        if (this.hasRedNose()) {
            this.setGlowing(true);
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setByte("OccultismDeerVariants", this.dataManager.get(VARIANTS).byteValue());
        compound.setBoolean("hasBlacksmithUpgrade", this.hasBlacksmithUpgrade());
    }

    public float getNeckRot(float partialTicks) {
        float timer = this.prevNeckRotTimer + (this.neckRotTimer - this.prevNeckRotTimer) * partialTicks;
        return 0.4F + timer / 10.0F * 1.5F;
    }

    public boolean hasRedNose() {
        return this.hasVariant(0);
    }

    public boolean isEating() {
        return this.eatingAnimationTimer > 0 || this.eatGrassAI != null && this.eatGrassAI.getEatingGrassTimer() > 0;
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
        if (this.canBlacksmithUpgrade()) {
            this.setBlacksmithUpgrade(true);
        }
    }

    private void updateOwnerEffects() {
        EntityLivingBase owner = this.getOwner();
        if (owner != null && owner.isEntityAlive() && this.ticksExisted % 10 == 0
                && this.getDistanceSq(owner) < OWNER_EFFECT_RANGE_SQ) {
            owner.addPotionEffect(new PotionEffect(MobEffects.JUMP_BOOST, 300, 0, false, true));
            owner.addPotionEffect(new PotionEffect(MobEffects.SPEED, 300, 0, false, true));
            if (owner instanceof EntityPlayer) {
                StepHeightBoostTracker.apply((EntityPlayer) owner, STEP_HEIGHT, this.world.getTotalWorldTime() + 20L);
            }
        }
    }

    @Override
    public Iterable<PotionEffect> getFamiliarEffects() {
        return java.util.Arrays.asList(
                new PotionEffect(MobEffects.JUMP_BOOST, 300, 0, false, true),
                new PotionEffect(MobEffects.SPEED, 300, 0, false, true));
    }

    private void updateSpeedBoost() {
        EntityLivingBase owner = this.getOwner();
        boolean boosted = owner != null && owner.isEntityAlive() && this.getDistanceSq(owner) > OWNER_SPEED_BOOST_RANGE_SQ;
        double targetSpeed = boosted ? BOOSTED_SPEED : NORMAL_SPEED;
        if (this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getBaseValue() != targetSpeed) {
            this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(targetSpeed);
        }
    }

    private void setRedNose(boolean redNose) {
        this.setVariant(0, redNose);
    }

    private void setBlacksmithUpgrade(boolean upgraded) {
        this.dataManager.set(BLACKSMITH_UPGRADE, Boolean.valueOf(upgraded));
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
}
