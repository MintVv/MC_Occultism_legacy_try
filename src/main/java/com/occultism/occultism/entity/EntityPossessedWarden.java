package com.occultism.occultism.entity;

import java.util.List;

import javax.annotation.Nullable;

import com.occultism.occultism.Tags;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

/**
 * A 1.12 compatibility implementation for the upstream Warden-based possessed entity.
 *
 * <p>Warden's Brain, vibration system and sonic-boom path do not exist in 1.12. This keeps the
 * dedicated entity contract and approximates the combat pressure with a periodic ranged pulse
 * and blindness field while retaining ordinary 1.12 navigation.</p>
 */
public class EntityPossessedWarden extends EntityMob {

    /**
     * The modern Warden's vibration listener cannot be represented by 1.12. These bounded
     * values define the compatibility pulse instead of pretending that a Brain exists here.
     */
    public static final int SONIC_PULSE_COOLDOWN_TICKS = 60;
    public static final int SONIC_PULSE_ACTIVE_TICKS = 6;
    public static final int BLINDNESS_INTERVAL_TICKS = 40;
    public static final double SONIC_PULSE_MIN_DISTANCE_SQ = 16.0D;
    public static final double SONIC_PULSE_MAX_DISTANCE_SQ = 576.0D;
    public static final double VIBRATION_SCAN_DISTANCE_SQ = 576.0D;

    private static final DataParameter<Boolean> SONIC_BOOM =
            EntityDataManager.createKey(EntityPossessedWarden.class, DataSerializers.BOOLEAN);
    private static final ResourceLocation LOOT_TABLE = LootTableList.register(
            new ResourceLocation(Tags.MOD_ID, "entities/possessed_warden"));

    private final PeacefulRemovalGuard peacefulRemovalGuard = new PeacefulRemovalGuard();
    private int sonicCooldown;
    private int sonicPulseTicks;

    public EntityPossessedWarden(World worldIn) {
        super(worldIn);
        this.setSize(1.8F, 5.8F);
        this.isImmuneToFire = true;
        this.experienceValue = 15;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(SONIC_BOOM, Boolean.FALSE);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(2, new WardenMeleeAttackGoal(this, 0.9D, true));
        this.tasks.addTask(7, new EntityAIWander(this, 0.6D));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 18.0F));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class,
                true));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(250.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(15.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.6D);
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.5D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(48.0D);
    }

    @Override
    public void onUpdate() {
        boolean protect = this.peacefulRemovalGuard.begin(this);
        try {
            super.onUpdate();
        } finally {
            this.peacefulRemovalGuard.finish(this, protect);
        }
    }

    @Override
    public void setDead() {
        this.peacefulRemovalGuard.recordSetDead();
        super.setDead();
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (this.world == null || this.world.isRemote) {
            return;
        }

        if (this.sonicCooldown > 0) {
            this.sonicCooldown--;
        }
        if (this.sonicPulseTicks > 0) {
            this.sonicPulseTicks--;
            if (this.sonicPulseTicks == 0) {
                this.dataManager.set(SONIC_BOOM, Boolean.FALSE);
            }
        }

        // 1.12 has no vibration event bus. Scanning nearby non-creative players at the pulse
        // cadence is the bounded equivalent and also works when normal target AI has not fired.
        EntityLivingBase target = this.findVibrationTarget();
        if (target != null && !target.isEntityInvulnerable(DamageSource.MAGIC)
                && this.getDistanceSq(target) > SONIC_PULSE_MIN_DISTANCE_SQ
                && this.getDistanceSq(target) <= SONIC_PULSE_MAX_DISTANCE_SQ
                && this.sonicCooldown <= 0) {
            this.performSonicPulse(target);
        }

        if (this.ticksExisted % BLINDNESS_INTERVAL_TICKS == 0) {
            AxisAlignedBB area = this.getEntityBoundingBox().grow(16.0D, 8.0D, 16.0D);
            List<EntityPlayer> players = this.world.getEntitiesWithinAABB(EntityPlayer.class, area);
            for (EntityPlayer player : players) {
                if (!player.isCreative() && !player.isSpectator()) {
                    player.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 50, 0, true, false));
                }
            }
        }
    }

    @Nullable
    private EntityLivingBase findVibrationTarget() {
        EntityLivingBase attackTarget = this.getAttackTarget();
        if (isVibrationTarget(attackTarget) && this.getDistanceSq(attackTarget) <= VIBRATION_SCAN_DISTANCE_SQ) {
            return attackTarget;
        }

        AxisAlignedBB area = this.getEntityBoundingBox().grow(24.0D, 12.0D, 24.0D);
        List<EntityPlayer> players = this.world.getEntitiesWithinAABB(EntityPlayer.class, area);
        EntityPlayer nearest = null;
        double nearestDistance = VIBRATION_SCAN_DISTANCE_SQ;
        for (EntityPlayer player : players) {
            if (!isVibrationTarget(player)) {
                continue;
            }
            double distance = this.getDistanceSq(player);
            if (distance <= nearestDistance) {
                nearest = player;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    private static boolean isVibrationTarget(@Nullable EntityLivingBase target) {
        return target != null && target.isEntityAlive() && target instanceof EntityPlayer
                && !target.isDead && !((EntityPlayer) target).isCreative()
                && !((EntityPlayer) target).isSpectator();
    }

    private void performSonicPulse(EntityLivingBase target) {
        this.sonicCooldown = SONIC_PULSE_COOLDOWN_TICKS;
        this.sonicPulseTicks = SONIC_PULSE_ACTIVE_TICKS;
        this.dataManager.set(SONIC_BOOM, Boolean.TRUE);
        if (target.attackEntityFrom(DamageSource.MAGIC, 6.0F)) {
            target.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 40, 0, true, false));
        }
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        boolean attacked = super.attackEntityAsMob(entityIn);
        if (attacked && entityIn instanceof EntityLivingBase) {
            EntityLivingBase target = (EntityLivingBase) entityIn;
            target.knockBack(this, 0.5F, this.posX - target.posX, this.posZ - target.posZ);
        }
        return attacked;
    }

    public boolean isSonicBoom() {
        return this.dataManager.get(SONIC_BOOM).booleanValue();
    }

    public int getSonicCooldown() {
        return this.sonicCooldown;
    }

    public int getSonicPulseTicks() {
        return this.sonicPulseTicks;
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return LOOT_TABLE;
    }

    @Override
    public void readEntityFromNBT(net.minecraft.nbt.NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.sonicCooldown = compound.getInteger("WardenSonicCooldown");
        this.sonicPulseTicks = compound.getInteger("WardenSonicPulseTicks");
        if (this.sonicPulseTicks > 0) {
            this.dataManager.set(SONIC_BOOM, Boolean.TRUE);
        }
    }

    @Override
    public void writeEntityToNBT(net.minecraft.nbt.NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("WardenSonicCooldown", this.sonicCooldown);
        compound.setInteger("WardenSonicPulseTicks", this.sonicPulseTicks);
    }

    private static final class WardenMeleeAttackGoal extends EntityAIAttackMelee {

        private final EntityPossessedWarden warden;

        private WardenMeleeAttackGoal(EntityPossessedWarden warden, double speedTowardsTarget,
                boolean useLongMemory) {
            super(warden, speedTowardsTarget, useLongMemory);
            this.warden = warden;
        }

        @Override
        protected double getAttackReachSqr(EntityLivingBase attackTarget) {
            double width = this.warden.width + attackTarget.width;
            return width * width + 3.0D;
        }
    }
}
