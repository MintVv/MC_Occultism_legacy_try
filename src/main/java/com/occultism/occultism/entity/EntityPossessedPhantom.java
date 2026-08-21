package com.occultism.occultism.entity;

import javax.annotation.Nullable;

import com.occultism.occultism.Tags;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

/**
 * A 1.12 flying compatibility implementation for the upstream Phantom-based possessed entity.
 * The modern Phantom brain does not exist in 1.12, so this keeps the same circle/swoop combat loop
 * without depending on a ground navigator.
 */
public class EntityPossessedPhantom extends EntityMob {

    private static final DataParameter<Boolean> SWOOPING =
            EntityDataManager.createKey(EntityPossessedPhantom.class, DataSerializers.BOOLEAN);
    private static final ResourceLocation LOOT_TABLE = LootTableList.register(
            new ResourceLocation(Tags.MOD_ID, "entities/possessed_phantom"));

    private final PeacefulRemovalGuard peacefulRemovalGuard = new PeacefulRemovalGuard();
    private double anchorX;
    private double anchorY;
    private double anchorZ;
    private boolean hasAnchor;
    private int swoopTicks;
    private int attackCooldown;
    private int circleDirection = 1;

    public EntityPossessedPhantom(World worldIn) {
        super(worldIn);
        this.setSize(0.9F, 0.5F);
        this.setNoGravity(true);
        this.isImmuneToFire = true;
        this.experienceValue = 5;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(SWOOPING, Boolean.FALSE);
    }

    @Override
    protected void initEntityAI() {
        // Phantom navigation/Brain goals are not present in 1.12. Movement is updated below.
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(35.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.23D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(3.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(2.0D);
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
        if (!this.world.isRemote) {
            if (this.attackCooldown > 0) {
                this.attackCooldown--;
            }
            this.updateTarget();
            this.updateFlight();
        }
        super.onLivingUpdate();
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (this.isInWater()) {
            this.moveRelative(strafe, vertical, forward, 0.02F);
            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.8D;
            this.motionY *= 0.8D;
            this.motionZ *= 0.8D;
            return;
        }
        if (this.isInLava()) {
            this.moveRelative(strafe, vertical, forward, 0.02F);
            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.5D;
            this.motionY *= 0.5D;
            this.motionZ *= 0.5D;
            return;
        }

        this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
        this.motionX *= 0.91D;
        this.motionY *= 0.91D;
        this.motionZ *= 0.91D;
        this.prevLimbSwingAmount = this.limbSwingAmount;
        double horizontal = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
        float movement = MathHelper.sqrt(horizontal * horizontal) * 4.0F;
        if (movement > 1.0F) {
            movement = 1.0F;
        }
        this.limbSwingAmount += (movement - this.limbSwingAmount) * 0.4F;
        this.limbSwing += this.limbSwingAmount;
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
        // Flying entities do not take fall damage.
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    protected void collideWithEntity(Entity entityIn) {
        // Phantom flight should not be redirected by crowded entity collision boxes.
    }

    @Override
    protected void collideWithNearbyEntities() {
        // Phantom flight should not be redirected by crowded entity collision boxes.
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.isEntityInvulnerable(source)) {
            return false;
        }
        this.setSwooping(false);
        return super.attackEntityFrom(source, amount);
    }

    public boolean isSwooping() {
        return this.dataManager.get(SWOOPING).booleanValue();
    }

    public void setSwooping(boolean value) {
        this.dataManager.set(SWOOPING, Boolean.valueOf(value));
    }

    public float getWingAngle(float partialTicks) {
        float age = this.ticksExisted + partialTicks;
        return MathHelper.cos(age * 0.45F) * (this.isSwooping() ? 0.25F : 0.65F);
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return LOOT_TABLE;
    }

    @Override
    public void readEntityFromNBT(net.minecraft.nbt.NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.anchorX = compound.getDouble("PhantomAnchorX");
        this.anchorY = compound.getDouble("PhantomAnchorY");
        this.anchorZ = compound.getDouble("PhantomAnchorZ");
        this.hasAnchor = compound.getBoolean("PhantomHasAnchor");
        this.swoopTicks = compound.getInteger("PhantomSwoopTicks");
        this.attackCooldown = compound.getInteger("PhantomAttackCooldown");
        this.setSwooping(compound.getBoolean("PhantomSwooping"));
    }

    @Override
    public void writeEntityToNBT(net.minecraft.nbt.NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setDouble("PhantomAnchorX", this.anchorX);
        compound.setDouble("PhantomAnchorY", this.anchorY);
        compound.setDouble("PhantomAnchorZ", this.anchorZ);
        compound.setBoolean("PhantomHasAnchor", this.hasAnchor);
        compound.setInteger("PhantomSwoopTicks", this.swoopTicks);
        compound.setInteger("PhantomAttackCooldown", this.attackCooldown);
        compound.setBoolean("PhantomSwooping", this.isSwooping());
    }

    private void updateTarget() {
        EntityLivingBase current = this.getAttackTarget();
        if (current != null && (!current.isEntityAlive()
                || current instanceof EntityPlayer && (((EntityPlayer) current).isCreative()
                || ((EntityPlayer) current).isSpectator())
                || this.getDistanceSq(current) > 2304.0D)) {
            this.setAttackTarget(null);
            current = null;
            this.setSwooping(false);
        }
        if (current == null && this.world != null) {
            EntityPlayer target = this.world.getNearestPlayerNotCreative(this, 48.0D);
            if (target != null && !target.isSpectator()) {
                this.setAttackTarget(target);
            }
        }
    }

    private void updateFlight() {
        EntityLivingBase target = this.getAttackTarget();
        if (target != null && target.isEntityAlive()) {
            if (!this.hasAnchor || this.getAnchorDistanceSq(target) > 1024.0D || this.rand.nextInt(200) == 0) {
                this.anchorX = target.posX;
                this.anchorY = target.posY + 10.0D + this.rand.nextInt(7);
                this.anchorZ = target.posZ;
                this.hasAnchor = true;
            }
        } else if (!this.hasAnchor) {
            this.anchorX = this.posX;
            this.anchorY = this.posY + 8.0D;
            this.anchorZ = this.posZ;
            this.hasAnchor = true;
        }

        if (this.isSwooping()) {
            this.swoopTicks++;
            this.moveToward(target == null ? this.anchorX : target.posX,
                    target == null ? this.anchorY : target.posY + target.getEyeHeight() * 0.5D,
                    target == null ? this.anchorZ : target.posZ, 0.32D);
            if (target != null && this.getDistanceSq(target) < 2.25D && this.attackCooldown <= 0) {
                this.attackCooldown = 20;
                this.swingArm(net.minecraft.util.EnumHand.MAIN_HAND);
                this.attackEntityAsMob(target);
                this.setSwooping(false);
                this.swoopTicks = 0;
            } else if (this.swoopTicks > 100) {
                this.setSwooping(false);
                this.swoopTicks = 0;
            }
            return;
        }

        if (target != null && this.attackCooldown <= 0 && this.ticksExisted % 80 == 0
                && this.getDistanceSq(target) < 576.0D) {
            this.setSwooping(true);
            this.swoopTicks = 0;
            return;
        }

        if (this.ticksExisted % 120 == 0 && this.rand.nextBoolean()) {
            this.circleDirection = -this.circleDirection;
        }
        double angle = (this.ticksExisted + this.getEntityId() * 3) * 0.08D * this.circleDirection;
        double radius = 8.0D;
        this.moveToward(this.anchorX + Math.cos(angle) * radius,
                this.anchorY + Math.sin(angle * 0.5D) * 3.0D,
                this.anchorZ + Math.sin(angle) * radius, 0.12D);
    }

    private double getAnchorDistanceSq(Entity entity) {
        double dx = this.anchorX - entity.posX;
        double dy = this.anchorY - entity.posY;
        double dz = this.anchorZ - entity.posZ;
        return dx * dx + dy * dy + dz * dz;
    }

    private void moveToward(double x, double y, double z, double speed) {
        double dx = x - this.posX;
        double dy = y - this.posY;
        double dz = z - this.posZ;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (distance < 0.001D) {
            return;
        }
        this.motionX += dx / distance * speed;
        this.motionY += dy / distance * speed;
        this.motionZ += dz / distance * speed;
        double max = 0.55D;
        double horizontal = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
        if (horizontal > max) {
            double scale = max / horizontal;
            this.motionX *= scale;
            this.motionZ *= scale;
        }
        if (this.motionY > max) {
            this.motionY = max;
        } else if (this.motionY < -max) {
            this.motionY = -max;
        }
        this.rotationYaw = (float) (MathHelper.atan2(this.motionZ, this.motionX) * (180.0D / Math.PI)) - 90.0F;
        this.rotationPitch = (float) (-(MathHelper.atan2(this.motionY, Math.sqrt(this.motionX * this.motionX
                + this.motionZ * this.motionZ)) * (180.0D / Math.PI)));
        this.rotationYawHead = this.rotationYaw;
    }
}
