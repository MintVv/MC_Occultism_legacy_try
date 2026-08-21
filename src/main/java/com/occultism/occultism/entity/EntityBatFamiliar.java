package com.occultism.occultism.entity;

import javax.annotation.Nullable;

import com.occultism.occultism.advancement.FamiliarTrigger;
import com.occultism.occultism.registry.ModAdvancements;
import com.occultism.occultism.registry.ModPotions;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

public class EntityBatFamiliar extends EntityFamiliarBase implements IBlacksmithUpgradable {

    private static final DataParameter<Byte> VARIANTS =
            EntityDataManager.createKey(EntityBatFamiliar.class, DataSerializers.BYTE);
    private static final DataParameter<Boolean> BLACKSMITH_UPGRADE =
            EntityDataManager.createKey(EntityBatFamiliar.class, DataSerializers.BOOLEAN);
    private static final double OWNER_EFFECT_RANGE_SQ = 64.0D;
    private static final double OWNER_TELEPORT_RANGE_SQ = 256.0D;
    private int cannibalismCooldown;

    public EntityBatFamiliar(World worldIn) {
        super(worldIn);
        this.setSize(0.7F, 0.7F);
        this.setNoGravity(true);
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
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(15.0D);
    }

    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        IEntityLivingData data = super.onInitialSpawn(difficulty, livingdata);
        this.setHair(this.rand.nextBoolean());
        this.setRibbon(this.rand.nextDouble() < 0.1D);
        this.setTail(this.rand.nextBoolean());
        return data;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.setNoGravity(true);
        if (this.cannibalismCooldown > 0) {
            this.cannibalismCooldown--;
        }
        if (!this.world.isRemote) {
            this.updateOwnerEffect();
            if (this.isSitting()) {
                this.motionX = 0.0D;
                this.motionY = 0.0D;
                this.motionZ = 0.0D;
                return;
            }
            this.updateCannibalism();
            this.updateFlight();
        }
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        if (hand == EnumHand.MAIN_HAND && this.isOwner(player) && player.getHeldItem(hand).isEmpty()) {
            this.setSitting(!this.isSitting());
            this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_BAT_AMBIENT,
                    this.getSoundCategory(), 0.25F, 1.2F);
            return true;
        }
        return super.processInteract(player, hand);
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    protected void collideWithEntity(Entity entityIn) {
    }

    @Override
    protected void collideWithNearbyEntities() {
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }

    @Override
    protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {
    }

    @Override
    public boolean doesEntityNotTriggerPressurePlate() {
        return true;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.isEntityInvulnerable(source)) {
            return false;
        }
        this.setSitting(false);
        return super.attackEntityFrom(source, amount);
    }

    @Override
    @Nullable
    protected SoundEvent getAmbientSound() {
        return this.rand.nextInt(4) == 0 ? SoundEvents.ENTITY_BAT_AMBIENT : null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.ENTITY_BAT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_BAT_DEATH;
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
        this.dataManager.set(VARIANTS, Byte.valueOf(compound.getByte("OccultismBatVariants")));
        this.cannibalismCooldown = compound.getInteger("OccultismCannibalismCooldown");
        this.setBlacksmithUpgrade(compound.getBoolean("hasBlacksmithUpgrade"));
        this.setNoGravity(true);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setByte("OccultismBatVariants", this.dataManager.get(VARIANTS).byteValue());
        compound.setInteger("OccultismCannibalismCooldown", this.cannibalismCooldown);
        compound.setBoolean("hasBlacksmithUpgrade", this.hasBlacksmithUpgrade());
    }

    public float getAnimationHeight(float partialTicks) {
        return MathHelper.cos((this.ticksExisted + partialTicks) / 5.0F);
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

    public boolean hasHair() {
        return this.hasVariant(0);
    }

    public boolean hasRibbon() {
        return this.hasVariant(1);
    }

    public boolean hasTail() {
        return this.hasVariant(2);
    }

    private void updateOwnerEffect() {
        EntityLivingBase owner = this.getOwner();
        if (owner != null && owner.isEntityAlive() && this.ticksExisted % 10 == 0
                && this.getDistanceSq(owner) < OWNER_EFFECT_RANGE_SQ) {
            owner.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, 300, 1, false, false));
            if (this.hasBlacksmithUpgrade()) {
                owner.addPotionEffect(new PotionEffect(ModPotions.BAT_LIFESTEAL, 300, 0, false, false));
            }
        }
    }

    @Override
    public Iterable<PotionEffect> getFamiliarEffects() {
        if (this.hasBlacksmithUpgrade()) {
            return java.util.Arrays.asList(
                    new PotionEffect(MobEffects.NIGHT_VISION, 300, 1, false, false),
                    new PotionEffect(ModPotions.BAT_LIFESTEAL, 300, 0, false, false));
        }
        return java.util.Collections.singletonList(
                new PotionEffect(MobEffects.NIGHT_VISION, 300, 1, false, false));
    }

    private void updateFlight() {
        EntityLivingBase owner = this.getOwner();
        if (owner != null && owner.isEntityAlive()) {
            double distanceSq = this.getDistanceSq(owner);
            if (distanceSq > OWNER_TELEPORT_RANGE_SQ) {
                this.setPosition(owner.posX + this.rand.nextInt(5) - 2, owner.posY + 1.0D,
                        owner.posZ + this.rand.nextInt(5) - 2);
                this.motionX = 0.0D;
                this.motionY = 0.0D;
                this.motionZ = 0.0D;
                return;
            }
            if (distanceSq > 9.0D) {
                this.moveToward(owner.posX, owner.posY + 1.2D, owner.posZ, distanceSq > 36.0D ? 0.18D : 0.11D);
                return;
            }
        }

        if (this.rand.nextInt(20) == 0) {
            double x = this.posX + this.rand.nextInt(7) - 3;
            double y = this.posY + this.rand.nextInt(5) - 2;
            double z = this.posZ + this.rand.nextInt(7) - 3;
            this.moveToward(x, Math.max(1.0D, y), z, 0.06D);
        }
        this.motionY *= 0.6D;
    }

    private void updateCannibalism() {
        if (this.cannibalismCooldown > 0) {
            return;
        }
        AxisAlignedBB bounds = this.getEntityBoundingBox().grow(2.0D);
        for (EntityBat bat : this.world.getEntitiesWithinAABB(EntityBat.class, bounds)) {
            if (bat.isEntityAlive()) {
                bat.attackEntityFrom(DamageSource.causeMobDamage(this), 10.0F);
                ModAdvancements.FAMILIAR.trigger(this.getFamiliarOwner(), FamiliarTrigger.Type.BAT_EAT);
                this.cannibalismCooldown = 80;
                break;
            }
        }
    }

    private void moveToward(double x, double y, double z, double speed) {
        double dx = x - this.posX;
        double dy = y - this.posY;
        double dz = z - this.posZ;
        double distance = MathHelper.sqrt(dx * dx + dy * dy + dz * dz);
        if (distance < 0.0001D) {
            return;
        }
        this.motionX += dx / distance * speed;
        this.motionY += dy / distance * speed;
        this.motionZ += dz / distance * speed;
        float yaw = (float) (MathHelper.atan2(this.motionZ, this.motionX) * (180D / Math.PI)) - 90.0F;
        this.rotationYaw = yaw;
        this.renderYawOffset = yaw;
    }

    private void setHair(boolean hair) {
        this.setVariant(0, hair);
    }

    private void setRibbon(boolean ribbon) {
        this.setVariant(1, ribbon);
    }

    private void setTail(boolean tail) {
        this.setVariant(2, tail);
    }

    private void setBlacksmithUpgrade(boolean value) {
        this.dataManager.set(BLACKSMITH_UPGRADE, Boolean.valueOf(value));
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
