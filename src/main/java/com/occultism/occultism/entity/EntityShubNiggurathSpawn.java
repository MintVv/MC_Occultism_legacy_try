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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.occultism.occultism.advancement.FamiliarTrigger;
import com.occultism.occultism.registry.ModAdvancements;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityShubNiggurathSpawn extends EntityCreature {

    private static final int EYE_COUNT = 4;
    private static final int LIFE_TICKS_MAX = 20 * 20;
    private static final byte DEATH_PARTICLE_STATUS = 80;

    private final int[] eyeBlinkTimers = new int[EYE_COUNT];
    private int lifeTicks = LIFE_TICKS_MAX;
    private UUID creatorId;

    public EntityShubNiggurathSpawn(World worldIn) {
        super(worldIn);
        this.setSize(0.6F, 0.6F);
    }

    public EntityShubNiggurathSpawn(World worldIn, EntityLivingBase creator) {
        this(worldIn);
        this.creatorId = creator.getUniqueID();
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new MoveToTargetGoal(this));
        this.targetTasks.addTask(0, new FindEnemiesGoal(this));
        this.targetTasks.addTask(1, new FindCreatorGoal(this));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(6.0D);
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
            if (this.lifeTicks-- < 0) {
                this.explode();
            }
            if (this.lifeTicks % 10 == 0 && !getOwnerEnemies(this.getCreatorOwner(), this, 10.0F).isEmpty()) {
                this.explode();
            }
        } else {
            for (int i = 0; i < EYE_COUNT; i++) {
                this.eyeBlinkTimers[i]--;
                if (this.eyeBlinkTimers[i] < 0) {
                    this.eyeBlinkTimers[i] = this.rand.nextInt(100) + 50;
                }
            }
        }
    }

    public boolean isBlinking(int eye) {
        return eye >= 0 && eye < EYE_COUNT && this.eyeBlinkTimers[eye] < 3;
    }

    private void explode() {
        if (!this.isEntityAlive()) {
            return;
        }
        float damage = (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
        EntityLivingBase owner = this.getCreatorOwner();
        List<EntityLivingBase> enemies = getOwnerEnemies(owner, this, 10.0F);
        boolean damagedEnemy = false;
        for (EntityLivingBase enemy : enemies) {
            DamageSource source = owner == null ? DamageSource.GENERIC : DamageSource.causeMobDamage(owner);
            damagedEnemy |= enemy.attackEntityFrom(source, damage);
        }
        if (damagedEnemy) {
            ModAdvancements.FAMILIAR.trigger(owner, FamiliarTrigger.Type.SHUB_NIGGURATH_SPAWN);
        }

        this.world.setEntityState(this, DEATH_PARTICLE_STATUS);
        this.setDead();
    }

    static List<EntityLivingBase> getOwnerEnemies(@Nullable EntityLivingBase owner,
            EntityLivingBase familiar, float range) {
        List<EntityLivingBase> enemies = new ArrayList<>();
        if (owner == null) {
            return enemies;
        }
        addIfClose(enemies, owner.getRevengeTarget(), familiar, range);
        // Deliberately do not de-duplicate: this is the fixed upstream 1.81.4 damage behavior.
        addIfClose(enemies, owner.getLastAttackedEntity(), familiar, range);
        return enemies;
    }

    private static void addIfClose(List<EntityLivingBase> targets, @Nullable EntityLivingBase target,
            EntityLivingBase familiar, float range) {
        // Fixed upstream compares squared distance directly to range rather than range squared.
        if (target != null && target != familiar && target.getDistanceSq(familiar) < range) {
            targets.add(target);
        }
    }

    @Nullable
    private EntityLivingBase getCreator() {
        if (this.creatorId == null || !(this.world instanceof WorldServer)) {
            return null;
        }
        Entity creator = ((WorldServer) this.world).getEntityFromUuid(this.creatorId);
        return creator instanceof EntityLivingBase ? (EntityLivingBase) creator : null;
    }

    @Nullable
    private EntityLivingBase getCreatorOwner() {
        EntityLivingBase creator = this.getCreator();
        return creator instanceof IFamiliar ? ((IFamiliar) creator).getFamiliarOwner() : creator;
    }

    @Override
    public void onDeath(DamageSource cause) {
        if (!this.world.isRemote) {
            this.world.setEntityState(this, DEATH_PARTICLE_STATUS);
        }
        super.onDeath(cause);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void handleStatusUpdate(byte id) {
        if (id == DEATH_PARTICLE_STATUS) {
            for (int i = 0; i < 30; i++) {
                double x = this.posX + (this.rand.nextDouble() * 2.0D - 1.0D) * this.width;
                double y = this.posY + this.rand.nextDouble() * this.height;
                double z = this.posZ + (this.rand.nextDouble() * 2.0D - 1.0D) * this.width;
                this.world.spawnParticle(EnumParticleTypes.REDSTONE, x, y, z, 0.5D, 0.0D, 0.0D);
            }
        } else {
            super.handleStatusUpdate(id);
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("lifeTicks", this.lifeTicks);
        if (this.creatorId != null) {
            compound.setUniqueId("creatorId", this.creatorId);
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasKey("lifeTicks", 3)) {
            this.lifeTicks = compound.getInteger("lifeTicks");
        }
        if (compound.hasUniqueId("creatorId")) {
            this.creatorId = compound.getUniqueId("creatorId");
        }
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return null;
    }

    private abstract static class FindTargetGoal extends EntityAITarget {

        protected final EntityShubNiggurathSpawn entity;

        private FindTargetGoal(EntityShubNiggurathSpawn entity) {
            super(entity, false);
            this.entity = entity;
            this.setMutexBits(1);
        }

        @Nullable
        protected abstract EntityLivingBase findTarget();

        @Override
        public boolean shouldExecute() {
            return this.findTarget() != null;
        }

        @Override
        public void startExecuting() {
            EntityLivingBase target = this.findTarget();
            if (target != null) {
                this.entity.setAttackTarget(target);
            }
            super.startExecuting();
        }
    }

    private static final class FindCreatorGoal extends FindTargetGoal {

        private FindCreatorGoal(EntityShubNiggurathSpawn entity) {
            super(entity);
        }

        @Override
        @Nullable
        protected EntityLivingBase findTarget() {
            return this.entity.getCreator();
        }
    }

    private static final class FindEnemiesGoal extends FindTargetGoal {

        private FindEnemiesGoal(EntityShubNiggurathSpawn entity) {
            super(entity);
        }

        @Override
        @Nullable
        protected EntityLivingBase findTarget() {
            List<EntityLivingBase> enemies = getOwnerEnemies(this.entity.getCreatorOwner(), this.entity, 30.0F);
            return enemies.isEmpty() ? null : enemies.get(0);
        }
    }

    private static final class MoveToTargetGoal extends EntityAIBase {

        private final EntityShubNiggurathSpawn entity;
        private int timer;

        private MoveToTargetGoal(EntityShubNiggurathSpawn entity) {
            this.entity = entity;
            this.setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            EntityLivingBase target = this.entity.getAttackTarget();
            return target != null && target.isEntityAlive();
        }

        @Override
        public void resetTask() {
            this.entity.getNavigator().clearPath();
        }

        @Override
        public void updateTask() {
            if (this.timer-- < 0) {
                this.timer = 20;
                EntityLivingBase target = this.entity.getAttackTarget();
                if (target != null) {
                    this.entity.getNavigator().tryMoveToEntityLiving(target, 1.0D);
                }
            }
        }
    }
}
