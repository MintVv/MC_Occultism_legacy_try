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

import java.util.UUID;

import javax.annotation.Nullable;

import com.occultism.occultism.advancement.FamiliarTrigger;
import com.occultism.occultism.registry.ModAdvancements;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.IJumpingMount;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIFollowOwner;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAIOwnerHurtByTarget;
import net.minecraft.entity.ai.EntityAIOwnerHurtTarget;
import net.minecraft.entity.ai.EntityAISit;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityChimeraFamiliar extends EntityFamiliarBase implements IFamiliar, IJumpingMount {

    public static final byte NO_ATTACKER = 0;
    public static final byte LION_ATTACKER = 1;
    public static final byte GOAT_ATTACKER = 2;
    public static final byte SNAKE_ATTACKER = 3;
    public static final byte MAX_SIZE = 100;

    private static final float BASE_WIDTH = 0.85F;
    private static final float BASE_HEIGHT = 1.05F;
    private static final UUID SPEED_BONUS = UUID.fromString("f1db15e0-174b-4534-96a3-d941cec44e55");
    private static final UUID DAMAGE_BONUS = UUID.fromString("fdaa6165-abdf-4b85-aed6-199086f6a5ee");
    private static final byte RIDING_SIZE = 80;
    private static final double SHRINK_CHANCE = 0.005D;
    private static final int JUMP_COOLDOWN = 20 * 2;
    private static final int ATTACK_TIME = 10;

    private static final DataParameter<Byte> VARIANTS =
            EntityDataManager.createKey(EntityChimeraFamiliar.class, DataSerializers.BYTE);
    private static final DataParameter<Byte> CHIMERA_SIZE =
            EntityDataManager.createKey(EntityChimeraFamiliar.class, DataSerializers.BYTE);
    private static final DataParameter<Byte> ATTACKER =
            EntityDataManager.createKey(EntityChimeraFamiliar.class, DataSerializers.BYTE);

    private int jumpTimer;
    private int goatNoseTimer;
    private int attackTimer;
    private boolean jumpRequested;
    private boolean partying;
    private BlockPos jukeboxPosition;

    public EntityChimeraFamiliar(World worldIn) {
        super(worldIn);
        this.setSize(BASE_WIDTH, BASE_HEIGHT);
        this.experienceValue = 2;
        this.goatNoseTimer = this.rand.nextInt(100);
        this.enablePersistence();
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(VARIANTS, Byte.valueOf((byte) 0));
        this.dataManager.register(CHIMERA_SIZE, Byte.valueOf((byte) 0));
        this.dataManager.register(ATTACKER, Byte.valueOf(NO_ATTACKER));
    }

    @Override
    protected void initEntityAI() {
        this.aiSit = new EntityAISit(this);
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, this.aiSit);
        this.tasks.addTask(4, new ChimeraMeleeAttackGoal(this, 1.0D, true));
        this.tasks.addTask(5, new EntityAIFollowOwner(this, 1.0D, 3.0F, 1.0F));
        this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(7, new EntityAIWander(this, 1.0D));

        this.targetTasks.addTask(1, new EntityAIOwnerHurtByTarget(this) {
            @Override
            public boolean shouldExecute() {
                return !EntityChimeraFamiliar.this.isSitting() && super.shouldExecute();
            }
        });
        this.targetTasks.addTask(2, new EntityAIOwnerHurtTarget(this) {
            @Override
            public boolean shouldExecute() {
                return !EntityChimeraFamiliar.this.isSitting() && super.shouldExecute();
            }
        });
        this.targetTasks.addTask(3, new EntityAIHurtByTarget(this, true));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(0.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).setBaseValue(0.0D);
        if (this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) == null) {
            this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4.0D);
        } else {
            this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4.0D);
        }
    }

    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        this.setGoat(true);
        this.setSize((byte) 0);
        this.setFlaps(this.rand.nextBoolean());
        this.setRing(this.rand.nextBoolean());
        this.setBeard(this.rand.nextBoolean());
        this.setHat(this.rand.nextDouble() < 0.1D);
        return super.onInitialSpawn(difficulty, livingdata);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();

        if (!this.world.isRemote && this.rand.nextDouble() < SHRINK_CHANCE) {
            this.setSize((byte) (this.getSize() - 1));
        }
        if (this.jumpTimer > 0) {
            this.jumpTimer--;
        }

        this.attackTimer--;
        if (this.attackTimer == 0) {
            this.setAttacker(NO_ATTACKER);
        }

        if (this.world.isRemote) {
            this.goatNoseTimer++;
            this.updatePartyState();
            this.spawnLionAttackParticles();
        }
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (this.hasGoat() && stack.getItem() == Items.GOLDEN_APPLE && player == this.getOwner()) {
            if (!this.world.isRemote) {
                stack.shrink(1);
                this.setGoat(false);
                EntityGoatFamiliar goat = new EntityGoatFamiliar(this.world, this.hasRing(), this.hasBeard(),
                        this.getSize(), this.getOwner());
                goat.setPosition(this.posX, this.posY, this.posZ);
                this.world.spawnEntity(goat);
                ModAdvancements.FAMILIAR.trigger(player, FamiliarTrigger.Type.GOAT_DETACH);
            }
            return true;
        }

        if (this.getSize() < MAX_SIZE && stack.getItem() instanceof ItemFood
                && ((ItemFood) stack.getItem()).isWolfsFavoriteMeat()) {
            if (!this.world.isRemote) {
                ItemFood food = (ItemFood) stack.getItem();
                int nutrition = food.getHealAmount(stack);
                stack.shrink(1);
                this.setSize((byte) (this.getSize() + nutrition));
                this.heal(4.0F);
            }
            return true;
        }

        if (!this.isSitting() && !this.isBeingRidden() && !player.isSneaking()
                && player == this.getOwner() && this.getSize() > RIDING_SIZE) {
            if (!this.world.isRemote) {
                if (player.startRiding(this)) {
                    ModAdvancements.FAMILIAR.trigger(player, FamiliarTrigger.Type.CHIMERA_RIDE);
                }
            }
            return true;
        }

        if (hand == EnumHand.MAIN_HAND && stack.isEmpty() && player == this.getOwner()) {
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

    @Override
    public void travel(float strafe, float vertical, float forward) {
        EntityLivingBase rider = this.getControllingPassenger();
        if (this.isBeingRidden() && rider != null) {
            this.rotationYaw = rider.rotationYaw;
            this.prevRotationYaw = this.rotationYaw;
            this.rotationPitch = rider.rotationPitch * 0.5F;
            this.setRotation(this.rotationYaw, this.rotationPitch);
            this.renderYawOffset = this.rotationYaw;
            this.rotationYawHead = this.rotationYaw;

            strafe = rider.moveStrafing * 0.5F;
            forward = rider.moveForward;
            if (forward < 0.0F) {
                forward *= 0.25F;
            }

            if (this.jumpRequested && this.onGround && this.jumpTimer <= 0) {
                this.jumpRequested = false;
                this.jumpTimer = JUMP_COOLDOWN;
                float yaw = this.rotationYaw * 0.017453292F;
                this.motionX += -MathHelper.sin(yaw) * 0.7D;
                this.motionZ += MathHelper.cos(yaw) * 0.7D;
                this.jump();
            }

            if (this.canPassengerSteer()) {
                this.setAIMoveSpeed((float) this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED)
                        .getAttributeValue() * 0.8F);
                super.travel(strafe, 0.0F, forward);
            } else {
                this.motionX = 0.0D;
                this.motionY = 0.0D;
                this.motionZ = 0.0D;
            }
        } else {
            this.jumpRequested = false;
            super.travel(strafe, vertical, forward);
        }
    }

    @Override
    public boolean canBeSteered() {
        return this.getControllingPassenger() != null;
    }

    @Override
    @Nullable
    public EntityLivingBase getControllingPassenger() {
        Entity passenger = this.getPassengers().isEmpty() ? null : this.getPassengers().get(0);
        return passenger instanceof EntityLivingBase ? (EntityLivingBase) passenger : null;
    }

    @Override
    public void setJumpPower(int jumpPowerIn) {
        this.requestJump();
    }

    @Override
    public boolean canJump() {
        return this.isBeingRidden() && this.getSize() > RIDING_SIZE;
    }

    @Override
    public void handleStartJump(int jumpPowerIn) {
        this.requestJump();
    }

    @Override
    public void handleStopJump() {
    }

    @Override
    protected float getJumpUpwardsMotion() {
        return super.getJumpUpwardsMotion() * 1.35F;
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
        super.fall(distance - 3.0F, damageMultiplier);
    }

    @Override
    public double getMountedYOffset() {
        return super.getMountedYOffset() * 0.6D;
    }

    @Override
    public boolean shouldAttackEntity(EntityLivingBase target, EntityLivingBase owner) {
        if (target == owner || target instanceof EntityCreeper || target instanceof EntityGhast
                || target instanceof EntityArmorStand) {
            return false;
        }
        if (target instanceof EntityWolf) {
            EntityWolf wolf = (EntityWolf) target;
            return !wolf.isTamed() || wolf.getOwner() != owner;
        }
        if (target instanceof EntityPlayer && owner instanceof EntityPlayer
                && !((EntityPlayer) owner).canAttackPlayer((EntityPlayer) target)) {
            return false;
        }
        if (target instanceof AbstractHorse && ((AbstractHorse) target).isTame()) {
            return false;
        }
        return !(target instanceof EntityTameable) || !((EntityTameable) target).isTamed();
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (source == DamageSource.IN_WALL || source == DamageSource.FLY_INTO_WALL) {
            return false;
        }
        return super.attackEntityFrom(source, amount);
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> key) {
        super.notifyDataManagerChange(key);
        if (CHIMERA_SIZE.equals(key)) {
            this.updateSizeAndAttributes();
        }
        if (ATTACKER.equals(key)) {
            this.attackTimer = this.getAttacker() == NO_ATTACKER ? 0 : ATTACK_TIME;
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setByte("variants", this.dataManager.get(VARIANTS).byteValue());
        compound.setByte("getSize", this.getSize());
        compound.setBoolean("hasFlaps", this.hasFlaps());
        compound.setBoolean("hasRing", this.hasRing());
        compound.setBoolean("hasBeard", this.hasBeard());
        compound.setBoolean("hasHat", this.hasHat());
        compound.setBoolean("hasGoat", this.hasGoat());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasKey("variants", 1)) {
            this.dataManager.set(VARIANTS, Byte.valueOf(compound.getByte("variants")));
        } else {
            this.setFlaps(compound.getBoolean("hasFlaps"));
            this.setRing(compound.getBoolean("hasRing"));
            if (compound.hasKey("hasBeard")) {
                this.setBeard(compound.getBoolean("hasBeard"));
            }
            this.setHat(compound.getBoolean("hasHat"));
            this.setGoat(compound.getBoolean("hasGoat"));
        }
        this.setSize(compound.getByte("getSize"));
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
    @Nullable
    public EntityLivingBase getFamiliarOwner() {
        return this.getOwner();
    }

    @Override
    public void setFamiliarOwner(EntityLivingBase owner) {
        super.setFamiliarOwner(owner);
        // Match the upstream binding path: the rare hat variant is awarded
        // when ownership is successfully applied, including ring release.
        if (owner != null && this.world != null && !this.world.isRemote && this.hasHat()) {
            ModAdvancements.FAMILIAR.trigger(owner, FamiliarTrigger.Type.RARE_VARIANT);
        }
    }

    @Override
    public EntityLivingBase getFamiliarEntity() {
        return this;
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

    public byte getSize() {
        return this.dataManager.get(CHIMERA_SIZE).byteValue();
    }

    public void setSize(byte size) {
        byte clamped = (byte) MathHelper.clamp((int) size, 0, MAX_SIZE);
        this.dataManager.set(CHIMERA_SIZE, Byte.valueOf(clamped));
        this.updateSizeAndAttributes();
    }

    public float getScale() {
        return 1.0F + this.getSize() / (float) MAX_SIZE;
    }

    public boolean hasFlaps() {
        return this.hasVariant(0);
    }

    public boolean hasRing() {
        return this.hasVariant(1);
    }

    public boolean hasBeard() {
        return this.hasVariant(2);
    }

    public boolean hasHat() {
        return this.hasVariant(3);
    }

    public boolean hasGoat() {
        return this.hasVariant(4);
    }

    public byte getAttacker() {
        return this.dataManager.get(ATTACKER).byteValue();
    }

    public float getNoseGoatRot(float partialTicks) {
        if (this.goatNoseTimer % 200 >= 40) {
            return 0.0F;
        }
        float progress = (this.goatNoseTimer % 200 + partialTicks) / 40.0F;
        return MathHelper.sin(progress * (float) Math.PI * 4.0F) * 0.1F;
    }

    public float getAttackProgress(float partialTicks) {
        if (this.attackTimer <= 0) {
            return 0.0F;
        }
        return (ATTACK_TIME - this.attackTimer + partialTicks) / ATTACK_TIME;
    }

    public boolean isWithinMeleeAttackRange(EntityLivingBase entity) {
        return entity.getDistanceSq(this) < 9.0D;
    }

    private void requestJump() {
        if (this.onGround && this.jumpTimer <= 0) {
            this.jumpRequested = true;
        }
    }

    private void updateSizeAndAttributes() {
        float scale = this.getScale();
        super.setSize(BASE_WIDTH * scale, BASE_HEIGHT * scale);
        this.setScaleForAge(false);
        if (this.getSize() <= RIDING_SIZE && this.isBeingRidden()) {
            this.removePassengers();
        }

        this.replaceModifier(SharedMonsterAttributes.ATTACK_DAMAGE, DAMAGE_BONUS,
                "Chimera attack bonus", this.getSize() / (double) MAX_SIZE * 3.0D);
        this.replaceModifier(SharedMonsterAttributes.MOVEMENT_SPEED, SPEED_BONUS,
                "Chimera speed bonus", this.getSize() / (double) MAX_SIZE * 0.08D);
    }

    private void replaceModifier(net.minecraft.entity.ai.attributes.IAttribute attribute, UUID id,
            String name, double amount) {
        IAttributeInstance instance = this.getEntityAttribute(attribute);
        if (instance == null) {
            return;
        }
        AttributeModifier existing = instance.getModifier(id);
        if (existing != null) {
            instance.removeModifier(existing);
        }
        instance.applyModifier(new AttributeModifier(id, name, amount, 0).setSaved(false));
    }

    private void setAttacker(byte attacker) {
        this.dataManager.set(ATTACKER, Byte.valueOf(attacker));
    }

    private void setFlaps(boolean value) {
        this.setVariant(0, value);
    }

    private void setRing(boolean value) {
        this.setVariant(1, value);
    }

    private void setBeard(boolean value) {
        this.setVariant(2, value);
    }

    private void setHat(boolean value) {
        this.setVariant(3, value);
    }

    private void setGoat(boolean value) {
        this.setVariant(4, value);
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

    @SideOnly(Side.CLIENT)
    private void updatePartyState() {
        if (this.jukeboxPosition == null
                || this.jukeboxPosition.distanceSq(this.posX, this.posY, this.posZ) > 12.0D
                || this.world.getBlockState(this.jukeboxPosition).getBlock() != Blocks.JUKEBOX) {
            this.partying = false;
            this.jukeboxPosition = null;
        }
    }

    @SideOnly(Side.CLIENT)
    private void spawnLionAttackParticles() {
        if (this.attackTimer <= 0 || this.getAttacker() != LION_ATTACKER) {
            return;
        }
        Vec3d direction = this.getLook(1.0F).scale(this.getScale());
        for (int i = 0; i < 5; i++) {
            double x = this.posX + direction.x + (this.rand.nextFloat() - 0.5F) * 0.7F;
            double y = this.posY + 1.0D + (this.rand.nextFloat() - 0.5F) * 0.7F;
            double z = this.posZ + direction.z + (this.rand.nextFloat() - 0.5F) * 0.7F;
            this.world.spawnParticle(EnumParticleTypes.FLAME, x, y, z,
                    direction.x * 0.1D, 0.0D, direction.z * 0.1D);
        }
    }

    private byte[] possibleAttackers() {
        return this.hasGoat()
                ? new byte[]{LION_ATTACKER, GOAT_ATTACKER, SNAKE_ATTACKER}
                : new byte[]{LION_ATTACKER, SNAKE_ATTACKER};
    }

    private static final class ChimeraMeleeAttackGoal extends EntityAIAttackMelee {

        private final EntityChimeraFamiliar chimera;

        private ChimeraMeleeAttackGoal(EntityChimeraFamiliar chimera, double speedModifier,
                boolean followingTargetEvenIfNotSeen) {
            super(chimera, speedModifier, followingTargetEvenIfNotSeen);
            this.chimera = chimera;
        }

        @Override
        protected void checkAndPerformAttack(EntityLivingBase enemy, double distanceSq) {
            if (distanceSq > this.getAttackReachSqr(enemy) || this.attackTick > 0) {
                return;
            }
            this.attackTick = 20;

            byte attacker = this.randomAttacker();
            this.chimera.setAttacker(attacker);
            enemy.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) this.chimera.getOwner()),
                    (float) this.chimera.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE)
                            .getAttributeValue());

            switch (attacker) {
                case LION_ATTACKER:
                    enemy.setFire(4);
                    break;
                case GOAT_ATTACKER:
                    enemy.knockBack(this.chimera, 2.0F,
                            this.chimera.posX - enemy.posX, this.chimera.posZ - enemy.posZ);
                    break;
                case SNAKE_ATTACKER:
                    enemy.addPotionEffect(new PotionEffect(MobEffects.POISON, 20 * 10));
                    break;
                default:
                    break;
            }
        }

        @Override
        protected double getAttackReachSqr(EntityLivingBase attackTarget) {
            return 9.0D;
        }

        private byte randomAttacker() {
            byte[] attackers = this.chimera.possibleAttackers();
            return attackers[this.chimera.getRNG().nextInt(attackers.length)];
        }
    }
}
