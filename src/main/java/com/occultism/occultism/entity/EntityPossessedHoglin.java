package com.occultism.occultism.entity;

import javax.annotation.Nullable;

import com.occultism.occultism.Tags;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

/**
 * A 1.12 compatibility implementation for the upstream Hoglin-based possessed entity.
 *
 * <p>1.12 has no Hoglin, Brain, or Nether biome AI. This keeps the upstream contract that
 * matters to the ritual (dedicated entity, attributes, fire immunity and hostile melee) while
 * using the closest stable 1.12 navigation and target-sharing primitives. Classic red/brown
 * mushrooms are an explicit warped-fungus avoidance surrogate, and leaving the Nether after
 * the modern conversion delay replaces this entity with a vanilla Zombie Pigman.</p>
 */
public class EntityPossessedHoglin extends EntityMob {

    public static final int NETHER_DIMENSION = -1;
    public static final int ZOMBIFICATION_DELAY_TICKS = 300;
    public static final int FUNGUS_AVOIDANCE_RADIUS = 8;
    public static final int FUNGUS_AVOIDANCE_VERTICAL = 3;
    public static final double ZOMBIFIED_ATTACK_DAMAGE = 12.0D;

    private static final ResourceLocation LOOT_TABLE = LootTableList.register(
            new ResourceLocation(Tags.MOD_ID, "entities/possessed_hoglin"));

    private final PeacefulRemovalGuard peacefulRemovalGuard = new PeacefulRemovalGuard();
    private int outsideNetherTicks;

    public EntityPossessedHoglin(World worldIn) {
        super(worldIn);
        this.setSize(1.88F, 1.88F);
        this.isImmuneToFire = true;
        this.experienceValue = 10;
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new FungusAvoidanceGoal(this, 1.25D, 1.05D));
        this.tasks.addTask(2, new HoglinMeleeAttackGoal(this, 1.1D, true));
        this.tasks.addTask(7, new EntityAIWander(this, 1.0D));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 12.0F));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class,
                true));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(66.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.4D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(20.0D);
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.6D);
        if (this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) == null) {
            this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(12.0D);
        } else {
            this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(12.0D);
        }
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
        if (this.world == null || this.world.isRemote || this.isDead) {
            return;
        }
        if (isNetherDimension(this.world.provider.getDimension())) {
            this.outsideNetherTicks = 0;
            return;
        }
        if (this.outsideNetherTicks < ZOMBIFICATION_DELAY_TICKS) {
            this.outsideNetherTicks++;
        }
        if (this.outsideNetherTicks >= ZOMBIFICATION_DELAY_TICKS && this.transformToZombiePigman()) {
            this.outsideNetherTicks = 0;
        }
    }

    public static boolean isNetherDimension(int dimension) {
        return dimension == NETHER_DIMENSION;
    }

    /**
     * 1.12 has no warped fungus. Both classic mushroom blocks are intentionally treated as a
     * visible, placeable avoidance marker instead of inventing a new registry ID.
     */
    public static boolean isFungusSubstitute(net.minecraft.block.state.IBlockState state) {
        if (state == null) {
            return false;
        }
        Block block = state.getBlock();
        return block == Blocks.RED_MUSHROOM || block == Blocks.BROWN_MUSHROOM;
    }

    public int getOutsideNetherTicks() {
        return this.outsideNetherTicks;
    }

    private boolean transformToZombiePigman() {
        if (this.world == null || this.world.isRemote || this.isDead) {
            return false;
        }
        EntityPigZombie converted = new EntityPigZombie(this.world);
        converted.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
        converted.motionX = this.motionX;
        converted.motionY = this.motionY;
        converted.motionZ = this.motionZ;
        converted.setNoAI(this.isAIDisabled());
        converted.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(ZOMBIFIED_ATTACK_DAMAGE);
        if (this.getAttackTarget() != null) {
            converted.setAttackTarget(this.getAttackTarget());
            converted.setRevengeTarget(this.getAttackTarget());
        }
        if (this.hasCustomName()) {
            converted.setCustomNameTag(this.getCustomNameTag());
            converted.setAlwaysRenderNameTag(this.getAlwaysRenderNameTag());
        }
        float healthRatio = this.getMaxHealth() <= 0.0F ? 1.0F : this.getHealth() / this.getMaxHealth();
        converted.setHealth(Math.max(1.0F, converted.getMaxHealth() * Math.min(1.0F, healthRatio)));
        if (!this.world.spawnEntity(converted)) {
            return false;
        }
        this.setDead();
        return true;
    }

    @Override
    public void readEntityFromNBT(net.minecraft.nbt.NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.outsideNetherTicks = Math.max(0,
                Math.min(ZOMBIFICATION_DELAY_TICKS, compound.getInteger("HoglinOutsideNetherTicks")));
    }

    @Override
    public void writeEntityToNBT(net.minecraft.nbt.NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("HoglinOutsideNetherTicks", this.outsideNetherTicks);
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        boolean attacked = super.attackEntityAsMob(entityIn);
        if (attacked && entityIn instanceof EntityLivingBase) {
            EntityLivingBase target = (EntityLivingBase) entityIn;
            target.knockBack(this, 1.0F, this.posX - target.posX, this.posZ - target.posZ);
        }
        return attacked;
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return LOOT_TABLE;
    }

    private static final class FungusAvoidanceGoal extends EntityAIBase {

        private final EntityPossessedHoglin hoglin;
        private final double farSpeed;
        private final double nearSpeed;
        private BlockPos fungus;
        private net.minecraft.pathfinding.Path path;

        private FungusAvoidanceGoal(EntityPossessedHoglin hoglin, double farSpeed, double nearSpeed) {
            this.hoglin = hoglin;
            this.farSpeed = farSpeed;
            this.nearSpeed = nearSpeed;
            this.setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            if (this.hoglin.world == null || this.hoglin.world.isRemote) {
                return false;
            }
            this.fungus = this.findNearestFungus();
            if (this.fungus == null) {
                return false;
            }
            Vec3d away = RandomPositionGenerator.findRandomTargetBlockAwayFrom(this.hoglin, 12, 5,
                    new Vec3d(this.fungus.getX() + 0.5D, this.fungus.getY(), this.fungus.getZ() + 0.5D));
            if (away == null || this.fungus.distanceSq(away.x, away.y, away.z) <= this.fungus.distanceSq(
                    this.hoglin.posX, this.hoglin.posY, this.hoglin.posZ)) {
                return false;
            }
            this.path = this.hoglin.getNavigator().getPathToXYZ(away.x, away.y, away.z);
            return this.path != null;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return this.path != null && !this.hoglin.getNavigator().noPath();
        }

        @Override
        public void startExecuting() {
            this.hoglin.getNavigator().setPath(this.path, this.farSpeed);
        }

        @Override
        public void resetTask() {
            this.fungus = null;
            this.path = null;
        }

        @Override
        public void updateTask() {
            if (this.fungus != null && this.hoglin.getDistanceSq(this.fungus) < 49.0D) {
                this.hoglin.getNavigator().setSpeed(this.nearSpeed);
            } else {
                this.hoglin.getNavigator().setSpeed(this.farSpeed);
            }
        }

        @Nullable
        private BlockPos findNearestFungus() {
            BlockPos origin = new BlockPos(this.hoglin.posX, this.hoglin.posY, this.hoglin.posZ);
            BlockPos nearest = null;
            double nearestDistance = (double) (FUNGUS_AVOIDANCE_RADIUS * FUNGUS_AVOIDANCE_RADIUS);
            for (int x = -FUNGUS_AVOIDANCE_RADIUS; x <= FUNGUS_AVOIDANCE_RADIUS; x++) {
                for (int y = -FUNGUS_AVOIDANCE_VERTICAL; y <= FUNGUS_AVOIDANCE_VERTICAL; y++) {
                    for (int z = -FUNGUS_AVOIDANCE_RADIUS; z <= FUNGUS_AVOIDANCE_RADIUS; z++) {
                        BlockPos candidate = origin.add(x, y, z);
                        if (!this.hoglin.world.isBlockLoaded(candidate)
                                || !isFungusSubstitute(this.hoglin.world.getBlockState(candidate))) {
                            continue;
                        }
                        double distance = candidate.distanceSq(this.hoglin.posX, this.hoglin.posY, this.hoglin.posZ);
                        if (distance < nearestDistance) {
                            nearest = candidate;
                            nearestDistance = distance;
                        }
                    }
                }
            }
            return nearest;
        }
    }

    private static final class HoglinMeleeAttackGoal extends EntityAIAttackMelee {

        private final EntityPossessedHoglin hoglin;

        private HoglinMeleeAttackGoal(EntityPossessedHoglin hoglin, double speedTowardsTarget,
                boolean useLongMemory) {
            super(hoglin, speedTowardsTarget, useLongMemory);
            this.hoglin = hoglin;
        }

        @Override
        protected double getAttackReachSqr(EntityLivingBase attackTarget) {
            double width = this.hoglin.width + attackTarget.width;
            return width * width + 2.0D;
        }
    }
}
