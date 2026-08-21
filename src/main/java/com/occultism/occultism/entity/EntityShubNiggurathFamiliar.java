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

import java.util.List;

import javax.annotation.Nullable;

import com.occultism.occultism.advancement.FamiliarTrigger;
import com.occultism.occultism.registry.ModAdvancements;

import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIFollow;
import net.minecraft.entity.ai.EntityAIFollowOwner;
import net.minecraft.entity.ai.EntityAISit;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityShubNiggurathFamiliar extends EntityFamiliarBase implements IBlacksmithUpgradable {

    private static final int MAX_SPAWN_TIMER = 20 * 10;

    private static final DataParameter<Byte> VARIANTS =
            EntityDataManager.createKey(EntityShubNiggurathFamiliar.class, DataSerializers.BYTE);
    private static final DataParameter<Boolean> BLACKSMITH_UPGRADE =
            EntityDataManager.createKey(EntityShubNiggurathFamiliar.class, DataSerializers.BOOLEAN);

    private int spawnTimer = MAX_SPAWN_TIMER;
    private boolean partying;
    private BlockPos jukeboxPosition;

    public EntityShubNiggurathFamiliar(World worldIn) {
        super(worldIn);
        this.setSize(0.7F, 0.8F);
        this.enablePersistence();
        this.experienceValue = 2;
    }

    public EntityShubNiggurathFamiliar(World worldIn, EntityGoatFamiliar goat) {
        this(worldIn);
        this.setRing(goat.hasRing());
        this.setBeard(goat.hasBeard());
        EntityLivingBase owner = goat.getFamiliarOwner();
        if (owner != null) {
            this.setTamed(true);
            this.setOwnerId(owner.getUniqueID());
        }
        this.setPosition(goat.posX, goat.posY, goat.posZ);
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
        this.tasks.addTask(1, this.aiSit);
        this.tasks.addTask(1, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(2, new EntityAIWatchClosest(this, EntityLivingBase.class, 8.0F));
        this.tasks.addTask(3, new RideCthulhuGoal(this));
        this.tasks.addTask(6, new EntityAIFollowOwner(this, 1.0D, 3.0F, 1.0F));
        this.tasks.addTask(8, new EntityAIWander(this, 1.0D));
        this.tasks.addTask(9, new EntityAIFollow(this, 1.0D, 3.0F, 7.0F));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(15.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).setBaseValue(10.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
        if (this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) == null) {
            this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);
        } else {
            this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);
        }
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (!this.world.isRemote) {
            this.rotateTowardsFriend();
            this.createSpawn();
        } else if (this.jukeboxPosition == null
                || this.jukeboxPosition.distanceSq(this.posX, this.posY, this.posZ) > 12.0D
                || this.world.getBlockState(this.jukeboxPosition).getBlock() != Blocks.JUKEBOX) {
            this.partying = false;
            this.jukeboxPosition = null;
        }
    }

    private void createSpawn() {
        if (this.spawnTimer-- < 0) {
            this.spawnTimer = MAX_SPAWN_TIMER;
            EntityShubNiggurathSpawn spawn = new EntityShubNiggurathSpawn(this.world, this);
            spawn.setPosition(this.getRandomX(2.0D), this.getRandomY(), this.getRandomZ(2.0D));
            this.world.spawnEntity(spawn);
        }
    }

    private double getRandomX(double spread) {
        return this.posX + (this.rand.nextDouble() * 2.0D - 1.0D) * this.width * spread;
    }

    private double getRandomY() {
        return this.posY + this.rand.nextDouble() * this.height;
    }

    private double getRandomZ(double spread) {
        return this.posZ + (this.rand.nextDouble() * 2.0D - 1.0D) * this.width * spread;
    }

    private void rotateTowardsFriend() {
        EntityCthulhuFamiliar friend = this.getCthulhuFriend();
        if (friend == null) {
            return;
        }
        Vec3d direction = friend.getPositionVector().subtract(this.getPositionVector());
        float rotation = (float) Math.toDegrees(MathHelper.atan2(direction.z, direction.x)) - 50.0F;
        // The fixed upstream writes only the previous yaw. Set both frames so the held-hands pose is stable in 1.12.
        this.rotationYaw = rotation;
        this.prevRotationYaw = rotation;
        this.renderYawOffset = rotation;
        this.prevRenderYawOffset = rotation;
    }

    @Nullable
    public EntityCthulhuFamiliar getCthulhuFriend() {
        return this.getRidingEntity() instanceof EntityCthulhuFamiliar
                ? (EntityCthulhuFamiliar) this.getRidingEntity() : null;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.isRiding() && source.isUnblockable()) {
            return false;
        }
        if (!super.attackEntityFrom(source, amount)) {
            return false;
        }
        if (source.getTrueSource() != null) {
            EntityGoatFamiliar.ringBell(this, this.hasBlacksmithUpgrade());
        }
        return true;
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        if (hand == EnumHand.MAIN_HAND && this.isOwner(player) && player.getHeldItem(hand).isEmpty()) {
            if (!this.world.isRemote) {
                this.setSitting(!this.isSitting());
                if (this.aiSit != null) {
                    this.aiSit.setSitting(this.isSitting());
                }
            }
            return true;
        }
        return super.processInteract(player, hand);
    }

    public boolean hasRing() {
        return this.hasVariant(0);
    }

    private void setRing(boolean value) {
        this.setVariant(0, value);
    }

    public boolean hasBeard() {
        return this.hasVariant(1);
    }

    private void setBeard(boolean value) {
        this.setVariant(1, value);
    }

    public boolean hasBlacksmithUpgrade() {
        return this.dataManager.get(BLACKSMITH_UPGRADE).booleanValue();
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
    public boolean isEntityInvulnerable(DamageSource source) {
        return super.isEntityInvulnerable(source) || source == DamageSource.IN_WALL
                || source == DamageSource.FLY_INTO_WALL;
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
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setByte("variants", this.dataManager.get(VARIANTS).byteValue());
        compound.setBoolean("hasBlacksmithUpgrade", this.hasBlacksmithUpgrade());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasKey("variants", 1)) {
            this.dataManager.set(VARIANTS, Byte.valueOf(compound.getByte("variants")));
        }
        this.setBlacksmithUpgrade(compound.getBoolean("hasBlacksmithUpgrade"));
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

    private void setBlacksmithUpgrade(boolean value) {
        this.dataManager.set(BLACKSMITH_UPGRADE, Boolean.valueOf(value));
    }

    private void setVariant(int offset, boolean value) {
        byte variants = this.dataManager.get(VARIANTS).byteValue();
        variants = value ? (byte) (variants | 1 << offset) : (byte) (variants & ~(1 << offset));
        this.dataManager.set(VARIANTS, Byte.valueOf(variants));
    }

    private boolean hasVariant(int offset) {
        return (this.dataManager.get(VARIANTS).byteValue() >> offset & 1) == 1;
    }

    private static final class RideCthulhuGoal extends EntityAIBase {

        private final EntityShubNiggurathFamiliar rider;
        private EntityCthulhuFamiliar mount;

        private RideCthulhuGoal(EntityShubNiggurathFamiliar rider) {
            this.rider = rider;
            this.setMutexBits(5);
        }

        @Override
        public boolean shouldExecute() {
            if (this.rider.getRidingEntity() instanceof EntityCthulhuFamiliar) {
                return true;
            }
            this.mount = this.findMount();
            if (this.mount == null) {
                return false;
            }
            this.rider.getNavigator().tryMoveToEntityLiving(this.mount, 1.0D);
            return true;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return this.rider.getRidingEntity() instanceof EntityCthulhuFamiliar
                    || this.mount != null && !this.rider.getNavigator().noPath();
        }

        @Override
        public void resetTask() {
            this.rider.dismountRidingEntity();
            this.mount = null;
        }

        @Override
        public void updateTask() {
            if (this.mount != null && this.rider.getDistanceSq(this.mount) < 5.0D
                    && !this.mount.isPassenger(this.rider)) {
                if (this.rider.startRiding(this.mount)) {
                    this.mount.getNavigator().clearPath();
                    ModAdvancements.FAMILIAR.trigger(this.rider.getFamiliarOwner(),
                            FamiliarTrigger.Type.SHUB_CTHULHU_FRIENDS);
                }
            }
        }

        @Nullable
        private EntityCthulhuFamiliar findMount() {
            EntityLivingBase owner = this.rider.getOwner();
            if (owner == null) {
                return null;
            }
            List<EntityCthulhuFamiliar> mounts = this.rider.world.getEntitiesWithinAABB(
                    EntityCthulhuFamiliar.class, this.rider.getEntityBoundingBox().grow(5.0D),
                    entity -> entity != null && entity.getOwner() == owner && !entity.isBeingRidden()
                            && !entity.isSitting());
            return mounts.isEmpty() ? null : mounts.get(0);
        }
    }
}
