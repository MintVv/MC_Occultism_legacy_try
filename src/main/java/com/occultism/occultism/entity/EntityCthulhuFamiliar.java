/*
 * MIT License
 *
 * Copyright 2021 vemerion
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.occultism.occultism.entity;

import java.util.List;

import javax.annotation.Nullable;

import com.occultism.occultism.advancement.FamiliarTrigger;
import com.occultism.occultism.registry.ModBlocks;
import com.occultism.occultism.registry.ModAdvancements;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathNavigateSwimmer;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class EntityCthulhuFamiliar extends EntityFamiliarBase implements IBlacksmithUpgradable {

    private static final DataParameter<Byte> VARIANTS =
            EntityDataManager.createKey(EntityCthulhuFamiliar.class, DataSerializers.BYTE);
    private static final DataParameter<Boolean> BLACKSMITH_UPGRADE =
            EntityDataManager.createKey(EntityCthulhuFamiliar.class, DataSerializers.BOOLEAN);

    private static final double OWNER_EFFECT_RANGE_SQ = 64.0D;
    private static final int LIGHT_UPDATE_INTERVAL = 10;

    private final PathNavigateGround groundNavigator;
    private final PathNavigateSwimmer waterNavigator;
    private BlockPos lightPos;
    private BlockPos previousLightPos;
    private int lightTimer;
    private float riderRotation;
    private float previousRiderRotation;

    public EntityCthulhuFamiliar(World worldIn) {
        super(worldIn);
        this.setSize(0.6F, 1.0F);
        this.setPathPriority(PathNodeType.WATER, 0.0F);
        this.groundNavigator = new PathNavigateGround(this, worldIn);
        this.waterNavigator = new PathNavigateSwimmer(this, worldIn);
        this.navigator = this.groundNavigator;
        this.moveHelper = new CthulhuMoveHelper(this);
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
        this.tasks.addTask(1, new CthulhuSitGoal(this));
        this.tasks.addTask(2, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(3, new FollowOwnerWaterGoal(this, 1.0D, 3.0F, 1.0F));
        this.tasks.addTask(4, new GiveFlowerGoal(this));
        this.tasks.addTask(5, new EntityAIWanderAvoidWater(this, 1.0D));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(15.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).setBaseValue(10.0D);
        if (this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) == null) {
            this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);
        } else {
            this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);
        }
    }

    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        IEntityLivingData data = super.onInitialSpawn(difficulty, livingdata);
        this.setHat(this.rand.nextDouble() < 0.1D);
        this.setTrunk(this.rand.nextDouble() < 0.5D);
        return data;
    }

    @Override
    public void onLivingUpdate() {
        if (!this.world.isRemote) {
            this.updateNavigationForMedium();
        }
        super.onLivingUpdate();

        this.previousRiderRotation = this.riderRotation;
        float rotationDelta = MathHelper.wrapDegrees(this.rotationYaw - this.riderRotation);
        this.riderRotation += MathHelper.clamp(rotationDelta, -10.0F, 10.0F);

        if (this.world.isRemote) {
            return;
        }

        if (this.isAngry() && this.rand.nextDouble() < 0.0007D) {
            this.setAngry(false);
        }
        if (this.ticksExisted % 10 == 0) {
            this.updateOwnerEffect();
        }
        if (--this.lightTimer < 0) {
            this.lightTimer = LIGHT_UPDATE_INTERVAL;
            this.updateLight();
        }
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        if (hand == EnumHand.MAIN_HAND && this.isOwner(player) && player.getHeldItem(hand).isEmpty()) {
            this.setSitting(!this.isSitting());
            return true;
        }
        return super.processInteract(player, hand);
    }

    @Override
    public void setFamiliarOwner(EntityLivingBase owner) {
        super.setFamiliarOwner(owner);
        // The upstream rare-variant advancement is granted when the familiar
        // is successfully bound, rather than merely when it spawns.
        if (owner != null && this.world != null && !this.world.isRemote && this.hasHat()) {
            ModAdvancements.FAMILIAR.trigger(owner, FamiliarTrigger.Type.RARE_VARIANT);
        }
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.isEntityInvulnerable(source) || !super.attackEntityFrom(source, amount)) {
            return false;
        }

        Entity attacker = source.getTrueSource();
        if (attacker != null && attacker == this.getOwner()) {
            this.setAngry(true);
            this.setSitting(true);
            if (this.world != null && !this.world.isRemote) {
                ModAdvancements.FAMILIAR.trigger(this.getFamiliarOwner(), FamiliarTrigger.Type.CTHULHU_SAD);
            }
        } else if (attacker != null) {
            Vec3d target = RandomPositionGenerator.findRandomTarget(this, 8, 4);
            if (target != null) {
                this.setPositionAndUpdate(target.x + 0.5D, target.y, target.z + 0.5D);
            }
            this.getNavigator().clearPath();
        }
        return true;
    }

    @Override
    public void onDeath(DamageSource cause) {
        this.clearLightBlocks();
        super.onDeath(cause);
    }

    @Override
    public void setDead() {
        this.clearLightBlocks();
        super.setDead();
    }

    @Override
    public void onRemovedFromWorld() {
        this.clearLightBlocks();
        super.onRemovedFromWorld();
    }

    @Override
    public Entity changeDimension(int dimensionIn) {
        this.clearLightBlocks();
        return super.changeDimension(dimensionIn);
    }

    @Override
    protected void playStepSound(BlockPos pos, Block blockIn) {
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }

    @Override
    public boolean shouldRiderSit() {
        return false;
    }

    @Override
    public void updatePassenger(Entity passenger) {
        if (!this.isPassenger(passenger)) {
            return;
        }
        double angle = Math.toRadians(this.riderRotation + 230.0F);
        passenger.setPosition(this.posX - Math.sin(angle) * 0.68D, this.posY,
                this.posZ + Math.cos(angle) * 0.68D);
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
        if (compound.hasKey("variants", 1)) {
            this.dataManager.set(VARIANTS, Byte.valueOf(compound.getByte("variants")));
        } else {
            this.setHat(compound.getBoolean("hasHat"));
            this.setTrunk(compound.getBoolean("hasTrunk"));
            this.setAngry(compound.getBoolean("isAngry"));
        }
        this.setBlacksmithUpgrade(compound.getBoolean("hasBlacksmithUpgrade"));

        int savedLightDimension = compound.hasKey("lightDimension", 3)
                ? compound.getInteger("lightDimension") : this.world.provider.getDimension();
        if (savedLightDimension == this.world.provider.getDimension()) {
            this.lightPos = readBlockPos(compound, "lightPos");
            this.previousLightPos = readBlockPos(compound, "lightPos0");
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setByte("variants", this.dataManager.get(VARIANTS).byteValue());
        compound.setBoolean("hasBlacksmithUpgrade", this.hasBlacksmithUpgrade());
        compound.setInteger("lightDimension", this.world.provider.getDimension());
        writeBlockPos(compound, "lightPos", this.lightPos);
        writeBlockPos(compound, "lightPos0", this.previousLightPos);
    }

    public boolean hasHat() {
        return this.hasVariant(0);
    }

    public boolean hasTrunk() {
        return this.hasVariant(1);
    }

    public boolean isAngry() {
        return this.hasVariant(2);
    }

    public boolean isGiving() {
        return this.hasVariant(3);
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
        this.setBlacksmithUpgrade(true);
    }

    public float getAnimationHeight(float partialTicks) {
        return MathHelper.cos((this.ticksExisted + partialTicks) / 5.0F);
    }

    public float getRiderRotation(float partialTicks) {
        return this.previousRiderRotation
                + MathHelper.wrapDegrees(this.riderRotation - this.previousRiderRotation) * partialTicks;
    }

    private void setHat(boolean value) {
        this.setVariant(0, value);
    }

    private void setTrunk(boolean value) {
        this.setVariant(1, value);
    }

    private void setAngry(boolean value) {
        this.setVariant(2, value);
    }

    private void setGiving(boolean value) {
        this.setVariant(3, value);
    }

    private void setBlacksmithUpgrade(boolean value) {
        this.dataManager.set(BLACKSMITH_UPGRADE, Boolean.valueOf(value));
        if (!value) {
            this.clearLightBlocks();
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

    private void updateNavigationForMedium() {
        PathNavigate required = this.isInWater() ? this.waterNavigator : this.groundNavigator;
        if (this.navigator != required) {
            this.navigator.clearPath();
            this.navigator = required;
        }
    }

    private void updateOwnerEffect() {
        EntityLivingBase owner = this.getOwner();
        if (owner != null && owner.isEntityAlive() && this.getDistanceSq(owner) < OWNER_EFFECT_RANGE_SQ) {
            owner.addPotionEffect(new PotionEffect(MobEffects.WATER_BREATHING, 300, 0, false, false));
        }
    }

    @Override
    public Iterable<PotionEffect> getFamiliarEffects() {
        return java.util.Collections.singletonList(
                new PotionEffect(MobEffects.WATER_BREATHING, 300, 0, false, false));
    }

    private void updateLight() {
        BlockPos current = new BlockPos(this);
        if (this.lightPos != null && !this.lightPos.equals(current)) {
            this.previousLightPos = this.lightPos;
        }
        this.lightPos = current;

        this.removeLight(this.previousLightPos);
        this.previousLightPos = null;

        if (!this.hasBlacksmithUpgrade() || !this.isEntityAlive()) {
            return;
        }
        if (this.world.isAirBlock(this.lightPos)) {
            this.world.setBlockState(this.lightPos, ModBlocks.LIGHTED_AIR.getDefaultState(), 3);
        }
    }

    private void clearLightBlocks() {
        this.removeLight(this.lightPos);
        this.removeLight(this.previousLightPos);
        this.lightPos = null;
        this.previousLightPos = null;
    }

    private void removeLight(@Nullable BlockPos pos) {
        if (this.world == null || this.world.isRemote || pos == null || !this.world.isBlockLoaded(pos)) {
            return;
        }
        if (this.world.getBlockState(pos).getBlock() == ModBlocks.LIGHTED_AIR) {
            this.world.setBlockToAir(pos);
        }
    }

    private static void writeBlockPos(NBTTagCompound compound, String key, @Nullable BlockPos pos) {
        if (pos == null) {
            return;
        }
        NBTTagCompound position = new NBTTagCompound();
        position.setInteger("X", pos.getX());
        position.setInteger("Y", pos.getY());
        position.setInteger("Z", pos.getZ());
        compound.setTag(key, position);
    }

    @Nullable
    private static BlockPos readBlockPos(NBTTagCompound compound, String key) {
        if (!compound.hasKey(key, 10)) {
            return null;
        }
        NBTTagCompound position = compound.getCompoundTag(key);
        return new BlockPos(position.getInteger("X"), position.getInteger("Y"), position.getInteger("Z"));
    }

    private static class CthulhuSitGoal extends EntityAIBase {

        private final EntityCthulhuFamiliar cthulhu;

        private CthulhuSitGoal(EntityCthulhuFamiliar cthulhu) {
            this.cthulhu = cthulhu;
            this.setMutexBits(5);
        }

        @Override
        public boolean shouldExecute() {
            return !this.cthulhu.isInWater() && this.cthulhu.getOwner() != null
                    && this.cthulhu.isSitting();
        }

        @Override
        public void startExecuting() {
            this.cthulhu.getNavigator().clearPath();
        }

        @Override
        public void resetTask() {
            this.cthulhu.setSitting(false);
        }
    }

    private static class CthulhuMoveHelper extends EntityMoveHelper {

        private final EntityCthulhuFamiliar cthulhu;

        private CthulhuMoveHelper(EntityCthulhuFamiliar cthulhu) {
            super(cthulhu);
            this.cthulhu = cthulhu;
        }

        @Override
        public void onUpdateMoveHelper() {
            if (!this.cthulhu.isInWater()) {
                super.onUpdateMoveHelper();
                return;
            }

            this.cthulhu.motionY += 0.005D;
            if (this.action != Action.MOVE_TO) {
                this.cthulhu.setAIMoveSpeed(0.0F);
                return;
            }

            double dx = this.posX - this.cthulhu.posX;
            double dy = this.posY - this.cthulhu.posY;
            double dz = this.posZ - this.cthulhu.posZ;
            double distance = MathHelper.sqrt(dx * dx + dy * dy + dz * dz);
            if (distance < 0.1D) {
                this.cthulhu.setMoveForward(0.0F);
                return;
            }

            float targetSpeed = (float) (this.speed
                    * this.cthulhu.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue()) * 3.0F;
            float currentSpeed = this.cthulhu.getAIMoveSpeed();
            this.cthulhu.setAIMoveSpeed(currentSpeed + (targetSpeed - currentSpeed) * 0.125F);
            if (Math.abs(dy) > 0.0001D) {
                this.cthulhu.motionY += this.cthulhu.getAIMoveSpeed() * (dy / distance) * 0.1D;
            }
            if (Math.abs(dx) > 0.0001D || Math.abs(dz) > 0.0001D) {
                float targetYaw = (float) (MathHelper.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;
                this.cthulhu.rotationYaw = this.limitAngle(this.cthulhu.rotationYaw, targetYaw, 8.0F);
                this.cthulhu.renderYawOffset = this.cthulhu.rotationYaw;
            }
        }
    }

    private static class FollowOwnerWaterGoal extends EntityAIBase {

        private static final int TELEPORT_ATTEMPTS = 10;

        private final EntityCthulhuFamiliar cthulhu;
        private final double speed;
        private final double minDistanceSq;
        private final double maxDistanceSq;
        private EntityLivingBase owner;
        private int cooldown;

        private FollowOwnerWaterGoal(EntityCthulhuFamiliar cthulhu, double speed,
                float minDistance, float maxDistance) {
            this.cthulhu = cthulhu;
            this.speed = speed;
            this.minDistanceSq = minDistance * minDistance;
            this.maxDistanceSq = maxDistance * maxDistance;
            this.setMutexBits(3);
        }

        @Override
        public boolean shouldExecute() {
            this.owner = this.cthulhu.getOwner();
            return this.shouldFollow(this.minDistanceSq);
        }

        @Override
        public boolean shouldContinueExecuting() {
            return this.shouldFollow(this.maxDistanceSq);
        }

        @Override
        public void startExecuting() {
            this.cooldown = 0;
        }

        @Override
        public void resetTask() {
            this.owner = null;
            this.cthulhu.getNavigator().clearPath();
        }

        @Override
        public void updateTask() {
            this.cthulhu.getLookHelper().setLookPositionWithEntity(this.owner, 10.0F,
                    this.cthulhu.getVerticalFaceSpeed());
            if (--this.cooldown >= 0) {
                return;
            }
            this.cooldown = 10;
            if (this.cthulhu.getLeashed() || this.cthulhu.isRiding()) {
                return;
            }

            if (this.cthulhu.getDistanceSq(this.owner) >= 150.0D
                    || this.cthulhu.isInWater() && this.cthulhu.world.getBlockState(new BlockPos(this.owner))
                            .getMaterial() != Material.WATER) {
                this.tryTeleport();
            } else {
                this.cthulhu.getNavigator().tryMoveToEntityLiving(this.owner, this.speed);
            }
        }

        private boolean shouldFollow(double distanceSq) {
            return this.owner != null && (!(this.owner instanceof EntityPlayer)
                    || !((EntityPlayer) this.owner).isSpectator())
                    && this.cthulhu.getDistanceSq(this.owner) > distanceSq;
        }

        private void tryTeleport() {
            BlockPos ownerPos = new BlockPos(this.owner);
            for (int attempt = 0; attempt < TELEPORT_ATTEMPTS; attempt++) {
                BlockPos target = ownerPos.add(this.cthulhu.rand.nextInt(7) - 3,
                        this.cthulhu.rand.nextInt(3) - 1, this.cthulhu.rand.nextInt(7) - 3);
                if (this.isTeleportFriendly(target)) {
                    this.cthulhu.setLocationAndAngles(target.getX() + 0.5D, target.getY(),
                            target.getZ() + 0.5D, this.cthulhu.rotationYaw, this.cthulhu.rotationPitch);
                    this.cthulhu.getNavigator().clearPath();
                    return;
                }
            }
        }

        private boolean isTeleportFriendly(BlockPos target) {
            BlockPos below = target.down();
            if (!this.cthulhu.world.getBlockState(below).isSideSolid(this.cthulhu.world, below,
                    net.minecraft.util.EnumFacing.UP)) {
                return false;
            }
            AxisAlignedBB movedBounds = this.cthulhu.getEntityBoundingBox().offset(
                    target.getX() + 0.5D - this.cthulhu.posX,
                    target.getY() - this.cthulhu.posY,
                    target.getZ() + 0.5D - this.cthulhu.posZ);
            return this.cthulhu.world.isAirBlock(target) && this.cthulhu.world.isAirBlock(target.up())
                    && this.cthulhu.world.getCollisionBoxes(this.cthulhu, movedBounds).isEmpty();
        }
    }

    private static class GiveFlowerGoal extends EntityAIBase {

        private static final int MAX_COOLDOWN = 20 * 60 * 5;

        private final EntityCthulhuFamiliar cthulhu;
        private EntityDevilFamiliar devil;
        private int cooldown = MAX_COOLDOWN;

        private GiveFlowerGoal(EntityCthulhuFamiliar cthulhu) {
            this.cthulhu = cthulhu;
            this.setMutexBits(3);
        }

        @Override
        public boolean shouldExecute() {
            this.devil = this.findDevil();
            return this.devil != null && this.cooldown-- < 0
                    && this.cthulhu.getDistanceSq(this.devil) > 3.0D && !this.cthulhu.isBeingRidden();
        }

        @Override
        public boolean shouldContinueExecuting() {
            return this.devil != null && !this.cthulhu.getNavigator().noPath()
                    && !this.cthulhu.isBeingRidden();
        }

        @Override
        public void startExecuting() {
            this.cthulhu.getNavigator().tryMoveToEntityLiving(this.devil, 0.3D);
            this.cthulhu.setGiving(true);
        }

        @Override
        public void resetTask() {
            this.cthulhu.setGiving(false);
            this.cthulhu.getNavigator().clearPath();
            this.cooldown = MAX_COOLDOWN;
            this.devil = null;
        }

        @Override
        public void updateTask() {
            if (this.devil != null && this.cthulhu.getDistanceSq(this.devil) < 2.0D) {
                if (this.cthulhu.world instanceof WorldServer) {
                    ((WorldServer) this.cthulhu.world).spawnParticle(EnumParticleTypes.HEART,
                            this.devil.posX, this.devil.posY + 1.0D, this.devil.posZ,
                            1, 0.0D, 0.0D, 0.0D, 0.0D);
                }
                this.devil = null;
            }
        }

        @Nullable
        private EntityDevilFamiliar findDevil() {
            List<EntityDevilFamiliar> devils = this.cthulhu.world.getEntitiesWithinAABB(
                    EntityDevilFamiliar.class, this.cthulhu.getEntityBoundingBox().grow(4.0D));
            return devils.isEmpty() ? null : devils.get(0);
        }
    }
}
