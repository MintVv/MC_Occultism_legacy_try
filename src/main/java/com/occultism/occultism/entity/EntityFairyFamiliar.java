/*
 * MIT License
 *
 * Copyright 2021 vemerion
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 * OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package com.occultism.occultism.entity;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.occultism.occultism.advancement.FamiliarTrigger;
import com.occultism.occultism.item.ItemFamiliarRing;
import com.occultism.occultism.registry.ModAdvancements;
import com.occultism.occultism.registry.ModPotions;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIFollow;
import net.minecraft.entity.ai.EntityAIFollowOwnerFlying;
import net.minecraft.entity.ai.EntityAISit;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.entity.ai.EntityAIWanderAvoidWaterFlying;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityFlyHelper;
import net.minecraft.entity.passive.EntityParrot;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateFlying;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityFairyFamiliar extends EntityFamiliarBase implements IFamiliar {

    private static final DataParameter<Byte> VARIANTS =
            EntityDataManager.createKey(EntityFairyFamiliar.class, DataSerializers.BYTE);
    private static final DataParameter<Integer> MAGIC_TARGET =
            EntityDataManager.createKey(EntityFairyFamiliar.class, DataSerializers.VARINT);
    private static final byte SUPPORT_STATUS = 80;
    private static final int SUPPORT_DURATION = 20 * 10;
    private static final int SAVE_COOLDOWN = 20 * 20;
    private static final float ANIMATION_HEIGHT_SPEED = 0.2F;

    private int saveCooldown;
    private int supportAnim;
    private boolean partying;
    private BlockPos jukeboxPosition;

    public EntityFairyFamiliar(World worldIn) {
        super(worldIn);
        this.setSize(0.6F, 1.0F);
        this.moveHelper = new EntityFlyHelper(this);
        this.setNoGravity(true);
        this.isImmuneToFire = true;
        this.ignoreFrustumCheck = true;
        this.enablePersistence();
        this.experienceValue = 2;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(VARIANTS, Byte.valueOf((byte) 0));
        this.dataManager.register(MAGIC_TARGET, Integer.valueOf(-1));
    }

    @Override
    protected void initEntityAI() {
        this.aiSit = new EntityAISit(this);
        this.tasks.addTask(2, this.aiSit);
        this.tasks.addTask(3, new MagicGoal(this));
        this.tasks.addTask(4, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(5, new EntityAIFollowOwnerFlying(this, 1.0D, 4.0F, 1.0F));
        this.tasks.addTask(6, new EntityAIWanderAvoidWaterFlying(this, 1.0D));
        this.tasks.addTask(7, new EntityAIFollow(this, 1.0D, 3.0F, 7.0F));
        this.tasks.addTask(8, new SupportGoal(this));
        this.targetTasks.addTask(0, new SetAttackTargetGoal(this));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(18.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
        if (this.getEntityAttribute(SharedMonsterAttributes.FLYING_SPEED) == null) {
            this.getAttributeMap().registerAttribute(SharedMonsterAttributes.FLYING_SPEED).setBaseValue(0.4D);
        } else {
            this.getEntityAttribute(SharedMonsterAttributes.FLYING_SPEED).setBaseValue(0.4D);
        }
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(15.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).setBaseValue(10.0D);
        if (this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) == null) {
            this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);
        } else {
            this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);
        }
    }

    @Override
    protected PathNavigate createNavigator(World worldIn) {
        PathNavigateFlying navigator = new PathNavigateFlying(this, worldIn);
        navigator.setCanOpenDoors(false);
        navigator.setCanFloat(true);
        navigator.setCanEnterDoors(true);
        return navigator;
    }

    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        IEntityLivingData data = super.onInitialSpawn(difficulty, livingdata);
        this.setTeeth(this.rand.nextBoolean());
        this.setLeftHanded(this.rand.nextBoolean());
        this.setFlower(this.rand.nextDouble() < 0.1D);
        return data;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.setNoGravity(true);
        this.isImmuneToFire = true;

        if (this.isSitting()) {
            this.motionX = 0.0D;
            this.motionY = 0.0D;
            this.motionZ = 0.0D;
        }
        if (this.saveCooldown > 0) {
            this.saveCooldown--;
        }
        if (!this.world.isRemote && this.getAttackTarget() == null) {
            this.setMagicTarget(null);
        }

        if (this.world.isRemote) {
            if (this.jukeboxPosition == null
                    || this.jukeboxPosition.distanceSq(this.posX, this.posY, this.posZ) > 12.0D
                    || this.world.getBlockState(this.jukeboxPosition).getBlock() != Blocks.JUKEBOX) {
                this.partying = false;
                this.jukeboxPosition = null;
            }
            if (this.hasMagicTarget()) {
                this.renderYawOffset = 0.0F;
                this.prevRenderYawOffset = 0.0F;
                this.spawnMagicParticle();
            }
            this.spawnPartyParticle();
            if (this.supportAnim > 0) {
                this.supportAnim--;
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setPartying(BlockPos pos, boolean isPartying) {
        this.jukeboxPosition = pos;
        this.partying = isPartying;
    }

    public boolean isPartying() {
        return this.partying;
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        if (hand == EnumHand.MAIN_HAND && this.isOwner(player) && player.getHeldItem(hand).isEmpty()) {
            if (!this.world.isRemote) {
                this.setSitting(!this.isSitting());
                if (this.aiSit != null) {
                    this.aiSit.setSitting(this.isSitting());
                }
                if (this.isSitting()) {
                    this.setAttackTarget(null);
                    this.setMagicTarget(null);
                }
            }
            return true;
        }
        return super.processInteract(player, hand);
    }

    @Override
    public void setFamiliarOwner(EntityLivingBase owner) {
        super.setFamiliarOwner(owner);
        if (owner != null && this.world != null && !this.world.isRemote && this.hasFlower()) {
            ModAdvancements.FAMILIAR.trigger(owner, FamiliarTrigger.Type.RARE_VARIANT);
        }
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.hasMagicTarget() && source != DamageSource.OUT_OF_WORLD && !source.canHarmInCreative()) {
            return false;
        }
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
    public boolean canBePushed() {
        return !this.hasMagicTarget() && super.canBePushed();
    }

    @Override
    public boolean canBeCollidedWith() {
        return !this.hasMagicTarget() && super.canBeCollidedWith();
    }

    @Override
    protected void collideWithEntity(Entity entityIn) {
        if (!this.hasMagicTarget()) {
            super.collideWithEntity(entityIn);
        }
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
        this.dataManager.set(VARIANTS, Byte.valueOf(compound.getByte("variants")));
        this.dataManager.set(MAGIC_TARGET, Integer.valueOf(-1));
        this.setNoGravity(true);
        this.isImmuneToFire = true;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setByte("variants", this.dataManager.get(VARIANTS).byteValue());
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

    /** Keeps the rescue cooldown advancing while the Fairy is in a ring. */
    @Override
    public void curioTick(@Nullable EntityLivingBase wearer) {
        if (this.saveCooldown > 0) {
            this.saveCooldown--;
        }
    }

    public boolean hasTeeth() {
        return this.hasVariant(0);
    }

    private void setTeeth(boolean value) {
        this.setVariant(0, value);
    }

    public boolean hasFlower() {
        return this.hasVariant(1);
    }

    private void setFlower(boolean value) {
        this.setVariant(1, value);
    }

    public float getAnimationHeight(float partialTicks) {
        return MathHelper.cos((this.ticksExisted + partialTicks) * ANIMATION_HEIGHT_SPEED) * 0.1F + 0.12F;
    }

    public float getWingRot(float partialTicks) {
        return MathHelper.cos((this.ticksExisted + partialTicks) * ANIMATION_HEIGHT_SPEED * 3.0F);
    }

    public float getSupportAnim(float partialTicks) {
        return this.supportAnim == 0 ? 0.0F : (20.0F - this.supportAnim + partialTicks) / 20.0F;
    }

    public float getPartyArmRotX(float partialTicks) {
        float argument = (this.ticksExisted + partialTicks) * 0.2F;
        return MathHelper.cos(argument) * toRads(40.0F) - toRads(90.0F);
    }

    public float getPartyArmRotY(float partialTicks) {
        float argument = (this.ticksExisted + partialTicks) * 0.2F;
        return MathHelper.sin(argument) * toRads(40.0F);
    }

    @Nullable
    public Entity getMagicTarget() {
        int id = this.dataManager.get(MAGIC_TARGET).intValue();
        return id < 0 ? null : this.world.getEntityByID(id);
    }

    private void setMagicTarget(@Nullable Entity entity) {
        this.dataManager.set(MAGIC_TARGET, Integer.valueOf(entity == null ? -1 : entity.getEntityId()));
    }

    public boolean hasMagicTarget() {
        return this.getMagicTarget() != null;
    }

    public Vec2f getMagicRadiusAngle(float partialTicks) {
        Entity target = this.getMagicTarget();
        if (target == null) {
            return new Vec2f(0.0F, 0.0F);
        }
        float radius = target.width * 1.2F;
        float angle = (this.ticksExisted + partialTicks) * 0.1F;
        return new Vec2f(radius, angle);
    }

    @Nullable
    public Vec3d getMagicPosition(float partialTicks) {
        Entity target = this.getMagicTarget();
        if (target == null) {
            return null;
        }
        Vec3d targetPos = interpolate(target, partialTicks).add(0.0D, target.height / 2.0D, 0.0D);
        Vec2f radiusAngle = this.getMagicRadiusAngle(partialTicks);
        return targetPos.add(MathHelper.cos(radiusAngle.y) * radiusAngle.x, 0.0D,
                MathHelper.sin(radiusAngle.y) * radiusAngle.x);
    }

    public boolean saveFamiliar(EntityLivingBase familiar) {
        if (this.saveCooldown > 0 || this.isSitting() || familiar == null) {
            return false;
        }
        this.saveCooldown = SAVE_COOLDOWN;
        if (!this.world.isRemote) {
            this.world.setEntityState(this, SUPPORT_STATUS);
        }
        return true;
    }

    public int getSaveCooldown() {
        return this.saveCooldown;
    }

    public void spawnSupportParticles(EntityLivingBase target) {
        if (!(this.world instanceof WorldServer) || target == null) {
            return;
        }
        EnumParticleTypes particle = isChristmas() ? EnumParticleTypes.SNOW_SHOVEL : EnumParticleTypes.SPELL_MOB;
        ((WorldServer) this.world).spawnParticle(particle, target.posX, target.posY + target.height / 2.0D,
                target.posZ, 30, target.width / 2.0D, target.height / 2.0D, target.width / 2.0D, 0.02D);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void handleStatusUpdate(byte id) {
        if (id == SUPPORT_STATUS) {
            this.supportAnim = 20;
        } else {
            super.handleStatusUpdate(id);
        }
    }

    public static boolean isFamiliarOwnedBy(@Nullable Entity entity, @Nullable EntityLivingBase owner) {
        return owner != null && getOwnerOfFamiliar(entity) == owner;
    }

    public static boolean isFamiliar(@Nullable Entity entity) {
        return entity instanceof IFamiliar
                || entity instanceof EntityParrot
                        && entity.getEntityData().getBoolean(ItemFamiliarRing.TAG_RITUAL_FAMILIAR_PARROT);
    }

    @Nullable
    public static EntityLivingBase getOwnerOfFamiliar(@Nullable Entity entity) {
        if (entity instanceof IFamiliar) {
            return ((IFamiliar) entity).getFamiliarOwner();
        }
        if (entity instanceof EntityParrot
                && entity.getEntityData().getBoolean(ItemFamiliarRing.TAG_RITUAL_FAMILIAR_PARROT)) {
            return ((EntityParrot) entity).getOwner();
        }
        return null;
    }

    private void setVariant(int offset, boolean value) {
        byte variants = this.dataManager.get(VARIANTS).byteValue();
        variants = value ? (byte) (variants | 1 << offset) : (byte) (variants & ~(1 << offset));
        this.dataManager.set(VARIANTS, Byte.valueOf(variants));
    }

    private boolean hasVariant(int offset) {
        return (this.dataManager.get(VARIANTS).byteValue() >> offset & 1) == 1;
    }

    private void spawnMagicParticle() {
        Vec3d pos = this.getMagicPosition(1.0F);
        if (pos != null) {
            this.spawnFairyParticle(pos.x, pos.y + 1.0D, pos.z);
        }
    }

    private void spawnPartyParticle() {
        if (!this.isPartying() || this.ticksExisted % 2 != 0) {
            return;
        }
        double side = this.isLeftHanded() ? -0.2D : 0.2D;
        double yaw = toRads(-this.renderYawOffset);
        double x = this.posX + MathHelper.cos((float) yaw) * side;
        double z = this.posZ + MathHelper.sin((float) yaw) * side;
        this.spawnFairyParticle(x, this.posY + 0.8D + this.getAnimationHeight(0.0F), z);
    }

    private void spawnFairyParticle(double x, double y, double z) {
        if (isChristmas()) {
            this.world.spawnParticle(EnumParticleTypes.SNOW_SHOVEL, x, y, z, 0.0D, -0.01D, 0.0D);
        } else {
            this.world.spawnParticle(EnumParticleTypes.SPELL_MOB, x, y, z, 0.9D, 0.9D, 0.5D);
        }
    }

    private static boolean isChristmas() {
        LocalDate date = LocalDate.now();
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();
        return month == 12 && day >= 15 || month == 1 && day <= 15;
    }

    private static Vec3d interpolate(Entity entity, float partialTicks) {
        return new Vec3d(entity.prevPosX + (entity.posX - entity.prevPosX) * partialTicks,
                entity.prevPosY + (entity.posY - entity.prevPosY) * partialTicks,
                entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTicks);
    }

    private static float toRads(float degrees) {
        return (float) Math.toRadians(degrees);
    }

    private static final class SupportGoal extends net.minecraft.entity.ai.EntityAIBase {

        private final EntityFairyFamiliar fairy;
        private final Map<UUID, Integer> cooldowns = new HashMap<>();

        private SupportGoal(EntityFairyFamiliar fairy) {
            this.fairy = fairy;
        }

        @Override
        public boolean shouldExecute() {
            return !this.fairy.isSitting() && this.fairy.getOwner() != null;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return this.shouldExecute();
        }

        @Override
        public void updateTask() {
            for (Map.Entry<UUID, Integer> entry : this.cooldowns.entrySet()) {
                if (entry.getValue().intValue() > 0) {
                    entry.setValue(Integer.valueOf(entry.getValue().intValue() - 1));
                }
            }
            if (this.fairy.ticksExisted % 5 != 0) {
                return;
            }

            EntityLivingBase owner = this.fairy.getOwner();
            if (owner == null) {
                return;
            }
            AxisAlignedBB bounds = this.fairy.getEntityBoundingBox().grow(10.0D);
            List<EntityLivingBase> familiars = this.fairy.world.getEntitiesWithinAABB(EntityLivingBase.class, bounds,
                    entity -> entity != this.fairy && EntityFairyFamiliar.isFamiliarOwnedBy(entity, owner));
            for (EntityLivingBase familiar : familiars) {
                UUID id = familiar.getUniqueID();
                if (this.cooldowns.getOrDefault(id, Integer.valueOf(0)).intValue() > 0) {
                    continue;
                }
                boolean supported = false;
                if (familiar.isBurning()) {
                    supported |= applyEffect(familiar, MobEffects.FIRE_RESISTANCE);
                }
                if (familiar.isInWater()) {
                    supported |= applyEffect(familiar, MobEffects.WATER_BREATHING);
                }
                if (!familiar.hasNoGravity() && familiar.fallDistance > 3.0F) {
                    supported |= applyEffect(familiar, ModPotions.FAIRY_SLOW_FALL);
                }
                if (familiar instanceof EntityLiving && hasLongPath((EntityLiving) familiar)) {
                    supported |= applyEffect(familiar, MobEffects.SPEED);
                }
                if (familiar.getRevengeTarget() != null) {
                    supported |= applyEffect(familiar, MobEffects.RESISTANCE);
                }
                if (supported) {
                    this.cooldowns.put(id, Integer.valueOf(SUPPORT_DURATION));
                    this.fairy.world.setEntityState(this.fairy, SUPPORT_STATUS);
                    this.fairy.spawnSupportParticles(familiar);
                }
            }
        }

        private static boolean applyEffect(EntityLivingBase entity, Potion potion) {
            PotionEffect current = entity.getActivePotionEffect(potion);
            if (current != null && current.getAmplifier() >= 0 && current.getDuration() >= SUPPORT_DURATION) {
                return false;
            }
            entity.addPotionEffect(new PotionEffect(potion, SUPPORT_DURATION, 0, false, false));
            return true;
        }

        private static boolean hasLongPath(EntityLiving familiar) {
            Path path = familiar.getNavigator().getPath();
            if (path == null || familiar.getNavigator().noPath()) {
                return false;
            }
            PathPoint target = path.getFinalPathPoint();
            return target != null && familiar.getDistanceSq(target.x, target.y, target.z) > 100.0D;
        }
    }

    private static final class MagicGoal extends EntityAIAttackMelee {

        private final EntityFairyFamiliar fairy;

        private MagicGoal(EntityFairyFamiliar fairy) {
            super(fairy, 1.8D, true);
            this.fairy = fairy;
        }

        @Override
        public void startExecuting() {
            super.startExecuting();
            this.attackTick = 10;
        }

        @Override
        public void resetTask() {
            super.resetTask();
            this.fairy.setMagicTarget(null);
        }

        @Override
        protected void checkAndPerformAttack(EntityLivingBase enemy, double distanceSq) {
            double reach = this.getAttackReachSqr(enemy);
            if (distanceSq <= reach) {
                this.fairy.setMagicTarget(enemy);
                if (this.attackTick <= 0) {
                    this.attackTick = 10;
                    EntityLivingBase owner = this.fairy.getOwner();
                    if (owner != null) {
                        DamageSource source = owner instanceof EntityPlayer
                                ? DamageSource.causePlayerDamage((EntityPlayer) owner)
                                : DamageSource.causeMobDamage(owner);
                        enemy.attackEntityFrom(source, 3.0F);
                        enemy.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 40, 1));
                        this.healAllies(owner);
                    }
                }
            } else if (distanceSq > reach * 3.0D) {
                this.fairy.setMagicTarget(null);
            }
        }

        @Override
        protected double getAttackReachSqr(EntityLivingBase target) {
            return super.getAttackReachSqr(target) * 3.0D;
        }

        private void healAllies(EntityLivingBase owner) {
            AxisAlignedBB bounds = this.fairy.getEntityBoundingBox().grow(7.0D);
            List<EntityLivingBase> allies = this.fairy.world.getEntitiesWithinAABB(EntityLivingBase.class, bounds,
                    entity -> entity != this.fairy && EntityFairyFamiliar.isFamiliarOwnedBy(entity, owner));
            allies.add(owner);
            for (EntityLivingBase ally : allies) {
                ally.heal(3.0F);
                if (this.fairy.world instanceof WorldServer) {
                    ((WorldServer) this.fairy.world).spawnParticle(EnumParticleTypes.HEART,
                            ally.posX, ally.posY + ally.height, ally.posZ, 1,
                            0.0D, 0.0D, 0.0D, 1.0D);
                }
            }
        }
    }

    private static final class SetAttackTargetGoal extends EntityAITarget {

        private final EntityFairyFamiliar fairy;
        private EntityLivingBase selectedTarget;
        private int timestamp;

        private SetAttackTargetGoal(EntityFairyFamiliar fairy) {
            super(fairy, false);
            this.fairy = fairy;
            this.setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            EntityLivingBase owner = this.fairy.getOwner();
            if (owner == null || owner.getDistanceSq(this.fairy) >= 400.0D
                    || this.timestamp == owner.getLastAttackedEntityTime()) {
                return false;
            }
            EntityLivingBase target = owner.getLastAttackedEntity();
            if (target == null || target instanceof EntityPlayer || isFamiliar(target)) {
                return false;
            }
            this.selectedTarget = target;
            return true;
        }

        @Override
        public boolean shouldContinueExecuting() {
            EntityLivingBase owner = this.fairy.getOwner();
            return super.shouldContinueExecuting() && owner != null && owner.getDistanceSq(this.fairy) < 400.0D;
        }

        @Override
        public void startExecuting() {
            EntityLivingBase owner = this.fairy.getOwner();
            if (owner != null && this.selectedTarget != null) {
                this.fairy.setAttackTarget(this.selectedTarget);
                this.timestamp = owner.getLastAttackedEntityTime();
            }
            super.startExecuting();
        }
    }
}
