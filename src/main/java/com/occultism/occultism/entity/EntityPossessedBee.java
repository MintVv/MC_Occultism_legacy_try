package com.occultism.occultism.entity;

import javax.annotation.Nullable;

import com.occultism.occultism.registry.ModItems;
import com.occultism.occultism.registry.ModBlocks;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EntityPossessedBee extends EntityBat {

    public static final int ATTACK_COOLDOWN_TICKS = 10;
    public static final int REINFORCEMENT_COOLDOWN_TICKS = 60;
    public static final int MAX_SWARM_SIZE = 8;

    private int attackCooldown;
    private int reinforcementCooldown;
    private int nestSearchCooldown;
    @Nullable
    private BlockPos targetNest;

    public EntityPossessedBee(World worldIn) {
        super(worldIn);
        this.setSize(0.5F, 0.45F);
        this.setIsBatHanging(false);
        this.enablePersistence();
        this.experienceValue = 3;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.setIsBatHanging(false);
        if (this.attackCooldown > 0) {
            this.attackCooldown--;
        }
        if (this.reinforcementCooldown > 0) {
            this.reinforcementCooldown--;
        }
        if (!this.world.isRemote) {
            this.updatePossessedBeeAttack();
        }
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        boolean attacked = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), 3.0F);
        if (attacked && entityIn instanceof EntityLivingBase) {
            ((EntityLivingBase) entityIn).addPotionEffect(new PotionEffect(MobEffects.POISON, 4 * 20, 0));
            this.attackCooldown = ATTACK_COOLDOWN_TICKS;
        }
        return attacked;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        boolean attacked = super.attackEntityFrom(source, amount);
        if (attacked && !this.world.isRemote && this.isEntityAlive() && this.reinforcementCooldown <= 0) {
            this.reinforcementCooldown = REINFORCEMENT_COOLDOWN_TICKS;
            this.spawnPossessedBeeSwarm();
        }
        return attacked;
    }

    @Override
    protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
        int count = 1 + (lootingModifier > 0 ? this.rand.nextInt(lootingModifier + 1) : 0);
        this.entityDropItem(new ItemStack(ModItems.CURSED_HONEY, count), 0.0F);
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return null;
    }

    private void updatePossessedBeeAttack() {
        EntityPlayer target = this.world.getNearestPlayerNotCreative(this, 12.0D);
        if (target != null && target.isEntityAlive()) {
            this.targetNest = null;
            this.moveToward(target.posX, target.posY + target.getEyeHeight() * 0.5D, target.posZ, 0.18D, 0.08D);

            double distanceSq = this.getDistanceSq(target);
            if (distanceSq <= 1.4D && this.attackCooldown <= 0 && this.attackEntityAsMob(target)) {
                this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_BAT_HURT,
                        SoundCategory.HOSTILE, 0.35F, 1.4F);
            }
            return;
        }

        if (this.nestSearchCooldown-- <= 0 || this.targetNest == null) {
            this.targetNest = this.findNearestBeeNest();
            this.nestSearchCooldown = 20;
        }
        if (this.targetNest == null) {
            return;
        }

        double distanceSq = this.getDistanceSq(this.targetNest);
        if (distanceSq <= 2.25D) {
            this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_BAT_TAKEOFF,
                    SoundCategory.HOSTILE, 0.4F, 1.2F);
            this.setDead();
            return;
        }
        this.moveToward(this.targetNest.getX() + 0.5D, this.targetNest.getY() + 0.5D,
                this.targetNest.getZ() + 0.5D, 0.12D, 0.05D);
    }

    private void moveToward(double targetX, double targetY, double targetZ, double farSpeed, double nearSpeed) {
        double dx = targetX - this.posX;
        double dy = targetY - this.posY;
        double dz = targetZ - this.posZ;
        double distanceSq = dx * dx + dy * dy + dz * dz;
        double distance = MathHelper.sqrt(distanceSq);
        if (distance <= 0.0001D) {
            return;
        }
        double speed = distanceSq > 4.0D ? farSpeed : nearSpeed;
        this.motionX += dx / distance * speed;
        this.motionY += dy / distance * speed;
        this.motionZ += dz / distance * speed;
        this.rotationYaw = (float) (MathHelper.atan2(this.motionZ, this.motionX) * (180D / Math.PI)) - 90.0F;
    }

    @Nullable
    private BlockPos findNearestBeeNest() {
        BlockPos nearest = null;
        double nearestDistanceSq = Double.MAX_VALUE;
        int centerX = MathHelper.floor(this.posX);
        int centerY = MathHelper.floor(this.posY);
        int centerZ = MathHelper.floor(this.posZ);
        for (int x = centerX - 8; x <= centerX + 8; x++) {
            for (int y = centerY - 4; y <= centerY + 4; y++) {
                for (int z = centerZ - 8; z <= centerZ + 8; z++) {
                    BlockPos candidate = new BlockPos(x, y, z);
                    if (this.world.getBlockState(candidate).getBlock() != ModBlocks.BEE_NEST) {
                        continue;
                    }
                    double distanceSq = this.getDistanceSq(candidate);
                    if (distanceSq < nearestDistanceSq) {
                        nearest = candidate;
                        nearestDistanceSq = distanceSq;
                    }
                }
            }
        }
        return nearest;
    }

    private void spawnPossessedBeeSwarm() {
        int currentSwarmSize = this.world.getEntitiesWithinAABB(EntityPossessedBee.class,
                this.getEntityBoundingBox().grow(12.0D)).size();
        int available = MAX_SWARM_SIZE - currentSwarmSize;
        if (available <= 0) {
            return;
        }

        int amount = Math.min(available, 2 + this.rand.nextInt(2));
        for (int index = 0; index < amount; index++) {
            EntityPossessedBee bee = new EntityPossessedBee(this.world);
            bee.setLocationAndAngles(this.posX + (this.rand.nextDouble() - 0.5D),
                    this.posY + (this.rand.nextDouble() - 0.5D),
                    this.posZ + (this.rand.nextDouble() - 0.5D), this.rotationYaw, 0.0F);
            this.world.spawnEntity(bee);
        }
    }
}
