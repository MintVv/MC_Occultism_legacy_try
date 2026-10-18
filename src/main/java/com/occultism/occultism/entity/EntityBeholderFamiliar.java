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
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.occultism.occultism.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.occultism.occultism.Tags;
import com.occultism.occultism.advancement.FamiliarTrigger;
import com.occultism.occultism.registry.ModAdvancements;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIFollow;
import net.minecraft.entity.ai.EntityAIFollowOwner;
import net.minecraft.entity.ai.EntityAISit;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import com.occultism.occultism.util.FamiliarUtil;
import net.minecraftforge.event.entity.living.PotionEvent.PotionApplicableEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public class EntityBeholderFamiliar extends EntityFamiliarBase implements IBlacksmithUpgradable {

    private static final int EAT_EFFECT_DURATION = 20 * 60 * 10;
    private static final double OWNER_EFFECT_RANGE = 10.0D;
    private static final double OWNER_ENEMY_RANGE_SQ = 100.0D;
    private static final byte EAT_STATUS = 81;
    private static final DataParameter<Byte> VARIANTS =
            EntityDataManager.createKey(EntityBeholderFamiliar.class, DataSerializers.BYTE);
    private static final DataParameter<Boolean> BLACKSMITH_UPGRADE =
            EntityDataManager.createKey(EntityBeholderFamiliar.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Float> RED =
            EntityDataManager.createKey(EntityBeholderFamiliar.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> GREEN =
            EntityDataManager.createKey(EntityBeholderFamiliar.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> BLUE =
            EntityDataManager.createKey(EntityBeholderFamiliar.class, DataSerializers.FLOAT);
    private static final DataParameter<Integer> RAY_TARGET_0 =
            EntityDataManager.createKey(EntityBeholderFamiliar.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> RAY_TARGET_1 =
            EntityDataManager.createKey(EntityBeholderFamiliar.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> RAY_TARGET_2 =
            EntityDataManager.createKey(EntityBeholderFamiliar.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> RAY_TARGET_3 =
            EntityDataManager.createKey(EntityBeholderFamiliar.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> RAY_VOLLEY =
            EntityDataManager.createKey(EntityBeholderFamiliar.class, DataSerializers.VARINT);

    private final float heightOffset;
    private final Eye[] eyes = new Eye[] {
            new Eye(-0.2D + 0.07D, 1.3D, -0.2D + 0.07D),
            new Eye(0.24D - 0.1D, 1.3D, -0.23D + 0.1D),
            new Eye(0.28D - 0.1D, 1.3D, 0.23D - 0.07D),
            new Eye(-0.15D + 0.06D, 1.3D, 0.2D - 0.09D)
    };

    // Plain floats on purpose: these used to be Vec2f, a client-only class. A field initialiser
    // runs in the constructor on both sides, so summoning this familiar on a dedicated server
    // threw NoClassDefFoundError before the entity existed. Render code builds a Vec2f on demand.
    private float bigEyePosX;
    private float bigEyePosY;
    private float bigEyePos0X;
    private float bigEyePos0Y;
    private float bigEyeTargetX;
    private float bigEyeTargetY;
    private int eatTimer = -1;
    private float mouthRot;
    private float actualMouthRot;
    private float actualMouthRot0;
    private int clientRayVolley;
    private boolean partying;
    private BlockPos jukeboxPosition;

    public EntityBeholderFamiliar(World worldIn) {
        super(worldIn);
        this.setSize(1.6F, 1.5F);
        this.enablePersistence();
        this.experienceValue = 2;
        this.heightOffset = this.rand.nextFloat() * 5.0F;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(VARIANTS, Byte.valueOf((byte) 0));
        this.dataManager.register(BLACKSMITH_UPGRADE, Boolean.FALSE);
        this.dataManager.register(RED, Float.valueOf(0.0F));
        this.dataManager.register(GREEN, Float.valueOf(0.0F));
        this.dataManager.register(BLUE, Float.valueOf(0.0F));
        this.dataManager.register(RAY_TARGET_0, Integer.valueOf(-1));
        this.dataManager.register(RAY_TARGET_1, Integer.valueOf(-1));
        this.dataManager.register(RAY_TARGET_2, Integer.valueOf(-1));
        this.dataManager.register(RAY_TARGET_3, Integer.valueOf(-1));
        this.dataManager.register(RAY_VOLLEY, Integer.valueOf(0));
    }

    @Override
    protected void initEntityAI() {
        this.aiSit = new EntityAISit(this);
        this.tasks.addTask(1, this.aiSit);
        this.tasks.addTask(2, new RayGoal(this));
        this.tasks.addTask(3, new EatGoal(this));
        this.tasks.addTask(4, new EntityAIFollowOwner(this, 1.0D, 3.0F, 1.0F));
        this.tasks.addTask(5, new EntityAIWander(this, 1.0D));
        this.tasks.addTask(6, new EntityAIFollow(this, 1.0D, 3.0F, 7.0F));
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
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        this.setColor();
        this.setBeard(this.rand.nextBoolean());
        this.setSpikes(this.rand.nextBoolean());
        this.setTongue(this.rand.nextDouble() < 0.1D);
        return super.onInitialSpawn(difficulty, livingdata);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();

        if (this.world.isRemote) {
            this.updateClientAnimation();
        } else if (this.rand.nextDouble() >= 0.98D) {
            this.tickGlow(this.getOwner());
        }
        this.renderYawOffset = this.prevRotationYaw;
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

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (source == DamageSource.IN_WALL || source == DamageSource.FLY_INTO_WALL) {
            return false;
        }
        return super.attackEntityFrom(source, amount);
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }

    @Override
    protected void playStepSound(BlockPos pos, Block blockIn) {
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
    public void swingArm(EnumHand hand) {
        super.swingArm(hand);
        this.eatTimer = 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void handleStatusUpdate(byte id) {
        if (id == EAT_STATUS) {
            this.eatTimer = 0;
            return;
        }
        super.handleStatusUpdate(id);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.dataManager.set(VARIANTS, Byte.valueOf(compound.getByte("variants")));
        this.setBlacksmithUpgrade(compound.getBoolean("hasBlacksmithUpgrade"));
        this.setRed(compound.getFloat("red"));
        this.setGreen(compound.getFloat("green"));
        this.setBlue(compound.getFloat("blue"));
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setByte("variants", this.dataManager.get(VARIANTS).byteValue());
        compound.setBoolean("hasBlacksmithUpgrade", this.hasBlacksmithUpgrade());
        compound.setFloat("red", this.getRed());
        compound.setFloat("green", this.getGreen());
        compound.setFloat("blue", this.getBlue());
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

    public boolean hasBlacksmithUpgrade() {
        return this.dataManager.get(BLACKSMITH_UPGRADE).booleanValue();
    }

    public boolean hasBeard() {
        return this.hasVariant(0);
    }

    public boolean hasSpikes() {
        return this.hasVariant(1);
    }

    public boolean hasTongue() {
        return this.hasVariant(2);
    }

    public float getRed() {
        return this.dataManager.get(RED).floatValue();
    }

    public float getGreen() {
        return this.dataManager.get(GREEN).floatValue();
    }

    public float getBlue() {
        return this.dataManager.get(BLUE).floatValue();
    }

    public float getAnimationHeight(float partialTicks) {
        if (this.isSitting()) {
            return -0.44F;
        }
        return 1.0F + MathHelper.cos((this.ticksExisted + this.heightOffset + partialTicks) / 5.0F) * 0.1F;
    }

    public float getMouthRot(float partialTicks) {
        return lerp(partialTicks, this.actualMouthRot0, this.actualMouthRot);
    }

    public float getEatTimer(float partialTicks) {
        return (this.eatTimer + partialTicks) / 60.0F;
    }

    public boolean isEating() {
        return this.eatTimer != -1;
    }

    @SideOnly(Side.CLIENT)
    public Vec2f getBigEyePos(float partialTicks) {
        return new Vec2f(lerp(partialTicks, this.bigEyePos0X, this.bigEyePosX),
                lerp(partialTicks, this.bigEyePos0Y, this.bigEyePosY));
    }

    @SideOnly(Side.CLIENT)
    public Vec2f getEyeRot(float partialTicks, int index) {
        return this.eyes[index].getEyeRot(partialTicks);
    }

    private void setBlacksmithUpgrade(boolean value) {
        this.dataManager.set(BLACKSMITH_UPGRADE, Boolean.valueOf(value));
    }

    private void setBeard(boolean value) {
        this.setVariant(0, value);
    }

    private void setSpikes(boolean value) {
        this.setVariant(1, value);
    }

    private void setTongue(boolean value) {
        this.setVariant(2, value);
    }

    private void setVariant(int offset, boolean value) {
        byte variants = this.dataManager.get(VARIANTS).byteValue();
        variants = value ? (byte) (variants | 1 << offset) : (byte) (variants & ~(1 << offset));
        this.dataManager.set(VARIANTS, Byte.valueOf(variants));
    }

    private boolean hasVariant(int offset) {
        return (this.dataManager.get(VARIANTS).byteValue() >> offset & 1) == 1;
    }

    private void setRed(float value) {
        this.dataManager.set(RED, Float.valueOf(value));
    }

    private void setGreen(float value) {
        this.dataManager.set(GREEN, Float.valueOf(value));
    }

    private void setBlue(float value) {
        this.dataManager.set(BLUE, Float.valueOf(value));
    }

    private void setColor() {
        for (int i = 0; i < 20; i++) {
            float red = this.rand.nextFloat();
            float green = this.rand.nextFloat();
            float blue = this.rand.nextFloat();
            if (MathHelper.abs(red - green) > 0.2F || MathHelper.abs(red - blue) > 0.2F
                    || MathHelper.abs(green - blue) > 0.2F) {
                this.setRed(red);
                this.setGreen(green);
                this.setBlue(blue);
                return;
            }
        }
    }

    @Override
    public boolean isEffectEnabled(@Nullable EntityLivingBase owner) {
        return super.isEffectEnabled(owner);
    }

    /** Native 1.12 ring hook for the owner's occasional random glow effect. */
    @Override
    public void curioTick(@Nullable EntityLivingBase wearer) {
        if (wearer != null && !this.world.isRemote && this.rand.nextDouble() < 0.02D) {
            this.tickGlow(wearer);
        }
    }

    private void tickGlow(@Nullable EntityLivingBase owner) {
        if (!this.isEffectEnabled(owner)) {
            return;
        }
        List<EntityLivingBase> nearby = owner.world.getEntitiesWithinAABB(EntityLivingBase.class,
                owner.getEntityBoundingBox().grow(OWNER_EFFECT_RANGE),
                entity -> !(entity instanceof EntityPlayer) && entity != owner && entity != this
                        && !entity.isPotionActive(MobEffects.GLOWING));
        if (!nearby.isEmpty()) {
            nearby.get(this.rand.nextInt(nearby.size()))
                    .addPotionEffect(new PotionEffect(MobEffects.GLOWING, 20 * 60, 0, false, false));
        }
    }

    private void updateClientAnimation() {
        if (this.jukeboxPosition == null
                || this.jukeboxPosition.distanceSq(this.posX, this.posY, this.posZ) > 12.0D
                || this.world.getBlockState(this.jukeboxPosition).getBlock() != Blocks.JUKEBOX) {
            this.partying = false;
            this.jukeboxPosition = null;
        }

        for (Eye eye : this.eyes) {
            eye.tick();
        }

        this.bigEyePos0X = this.bigEyePosX;
        this.bigEyePos0Y = this.bigEyePosY;
        if (Math.abs(this.bigEyePosX - this.bigEyeTargetX)
                + Math.abs(this.bigEyePosY - this.bigEyeTargetY) < 0.05F) {
            this.bigEyeTargetX = (this.rand.nextFloat() - 0.5F) * 2.9F;
            this.bigEyeTargetY = (this.rand.nextFloat() - 0.5F) * 1.9F;
        }
        this.bigEyePosX = lerp(0.1F, this.bigEyePosX, this.bigEyeTargetX);
        this.bigEyePosY = lerp(0.1F, this.bigEyePosY, this.bigEyeTargetY);

        this.actualMouthRot0 = this.actualMouthRot;
        this.mouthRot = MathHelper.cos(this.ticksExisted * 0.1F) * toRads(10.0F) + toRads(27.0F);
        if (this.eatTimer != -1) {
            this.eatTimer++;
            if (this.eatTimer == 60) {
                this.eatTimer = -1;
            }
            if (this.eatTimer < 50) {
                if (this.eatTimer % 5 == 0) {
                    this.world.playSound(this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EAT,
                            SoundCategory.HOSTILE, this.getSoundVolume(), this.getSoundPitch(), false);
                }
                this.mouthRot = MathHelper.sin(this.ticksExisted) * toRads(50.0F) + toRads(20.0F);
            }
            if (this.eatTimer == 51) {
                this.world.playSound(this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE,
                        SoundCategory.HOSTILE, this.getSoundVolume(), 0.2F, false);
            }
        }
        this.actualMouthRot = lerp(0.2F, this.actualMouthRot, this.mouthRot);

        int volley = this.dataManager.get(RAY_VOLLEY).intValue();
        if (volley != this.clientRayVolley) {
            this.clientRayVolley = volley;
            this.shootRay(this.getSyncedRayTargets());
        }
    }

    @SideOnly(Side.CLIENT)
    private void shootRay(List<Integer> targetIds) {
        List<Eye> availableEyes = new ArrayList<>();
        Collections.addAll(availableEyes, this.eyes);
        for (Integer targetId : targetIds) {
            if (availableEyes.isEmpty()) {
                break;
            }
            int index = this.rand.nextInt(availableEyes.size());
            Eye eye = availableEyes.remove(index);
            eye.prepareShot(targetId.intValue());
        }
    }

    private List<Integer> getSyncedRayTargets() {
        List<Integer> result = new ArrayList<>();
        this.addSyncedTarget(result, RAY_TARGET_0);
        this.addSyncedTarget(result, RAY_TARGET_1);
        this.addSyncedTarget(result, RAY_TARGET_2);
        this.addSyncedTarget(result, RAY_TARGET_3);
        return result;
    }

    private void addSyncedTarget(List<Integer> targets, DataParameter<Integer> parameter) {
        int id = this.dataManager.get(parameter).intValue();
        if (id >= 0) {
            targets.add(Integer.valueOf(id));
        }
    }

    private void syncRayTargets(List<Integer> targets) {
        this.dataManager.set(RAY_TARGET_0, Integer.valueOf(targetId(targets, 0)));
        this.dataManager.set(RAY_TARGET_1, Integer.valueOf(targetId(targets, 1)));
        this.dataManager.set(RAY_TARGET_2, Integer.valueOf(targetId(targets, 2)));
        this.dataManager.set(RAY_TARGET_3, Integer.valueOf(targetId(targets, 3)));
        this.dataManager.set(RAY_VOLLEY, Integer.valueOf(this.dataManager.get(RAY_VOLLEY).intValue() + 1));
    }

    private static int targetId(List<Integer> targets, int index) {
        return index < targets.size() ? targets.get(index).intValue() : -1;
    }

    private static float lerp(float amount, float start, float end) {
        return start + amount * (end - start);
    }

    private static Vec3d lerpVec(float amount, Vec3d start, Vec3d end) {
        return new Vec3d(lerp(amount, (float) start.x, (float) end.x),
                lerp(amount, (float) start.y, (float) end.y),
                lerp(amount, (float) start.z, (float) end.z));
    }

    private static float rotLerp(float amount, float start, float end) {
        return start + amount * MathHelper.wrapDegrees(end - start);
    }

    private static float toRads(float degrees) {
        return (float) Math.toRadians(degrees);
    }

    @SubscribeEvent
    public static void onPotionApplicable(PotionApplicableEvent event) {
        PotionEffect effect = event.getPotionEffect();
        EntityLivingBase owner = event.getEntityLiving();
        if (effect.getPotion() != MobEffects.BLINDNESS || owner == null || owner.world == null) {
            return;
        }
        // FamiliarUtil includes both nearby world familiars and the native 1.12
        // equipped ring slot. This keeps the upstream Curios semantics for a
        // blacksmith-upgraded Beholder even while its entity is stored in NBT.
        if (FamiliarUtil.hasFamiliar(owner, EntityBeholderFamiliar.class,
                beholder -> beholder.isEntityAlive() && beholder.hasBlacksmithUpgrade())) {
            event.setResult(Result.DENY);
        }
    }

    private static final class RayGoal extends EntityAIBase {

        private static final int MAX_COOLDOWN = 20;

        private final EntityBeholderFamiliar beholder;
        private int cooldown = MAX_COOLDOWN;
        private int attackTimer;
        private List<Integer> targetIds;

        private RayGoal(EntityBeholderFamiliar beholder) {
            this.beholder = beholder;
        }

        @Override
        public boolean shouldExecute() {
            return this.beholder.getOwner() instanceof EntityPlayer;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return this.shouldExecute();
        }

        @Override
        public void resetTask() {
            this.cooldown = MAX_COOLDOWN;
            this.targetIds = null;
            this.attackTimer = 0;
        }

        @Override
        public void updateTask() {
            List<Integer> enemies = this.getNearbyEnemies();
            boolean cooldownReady = this.cooldown-- < 0;
            if (this.targetIds == null && cooldownReady && !enemies.isEmpty()) {
                this.targetIds = enemies;
                this.cooldown = MAX_COOLDOWN;
                this.attackTimer = 23;
                this.beholder.syncRayTargets(this.targetIds);
            }

            if (this.targetIds != null && this.attackTimer-- < 0) {
                this.attack();
                this.targetIds = null;
            }
        }

        private List<Integer> getNearbyEnemies() {
            List<Integer> result = new ArrayList<>();
            EntityLivingBase owner = this.beholder.getOwner();
            if (owner != null) {
                this.addIfClose(result, owner.getRevengeTarget());
                this.addIfClose(result, owner.getLastAttackedEntity());
            }
            return result;
        }

        private void addIfClose(List<Integer> targets, @Nullable EntityLivingBase target) {
            if (target != null && target != this.beholder
                    && this.beholder.getDistanceSq(target) < OWNER_ENEMY_RANGE_SQ) {
                targets.add(Integer.valueOf(target.getEntityId()));
            }
        }

        private void attack() {
            EntityLivingBase owner = this.beholder.getOwner();
            boolean hit = false;
            for (Integer targetId : this.targetIds) {
                Entity target = this.beholder.world.getEntityByID(targetId.intValue());
                if (target == null) {
                    continue;
                }

                float damage = 9.0F;
                PotionEffect strength = this.beholder.getActivePotionEffect(MobEffects.STRENGTH);
                if (strength != null) {
                    damage *= strength.getAmplifier() + 2;
                }
                DamageSource source = owner instanceof EntityPlayer
                        ? DamageSource.causePlayerDamage((EntityPlayer) owner)
                        : DamageSource.causeMobDamage(this.beholder);
                hit |= target.attackEntityFrom(source, damage);
            }
            if (hit) {
                ModAdvancements.FAMILIAR.trigger(owner, FamiliarTrigger.Type.BEHOLDER_RAY);
            }
        }
    }

    private static final class EatGoal extends EntityAIBase {

        private static final int MAX_COOLDOWN = 20 * 30;

        private final EntityBeholderFamiliar beholder;
        private int cooldown;

        private EatGoal(EntityBeholderFamiliar beholder) {
            this.beholder = beholder;
        }

        @Override
        public boolean shouldExecute() {
            return !this.beholder.isSitting() && this.cooldown-- < 0;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return false;
        }

        @Override
        public void startExecuting() {
            this.cooldown = MAX_COOLDOWN;
            List<EntityShubNiggurathSpawn> foods = this.beholder.world.getEntitiesWithinAABB(
                    EntityShubNiggurathSpawn.class,
                    this.beholder.getEntityBoundingBox().grow(3.0D),
                    Entity::isEntityAlive);
            EntityLivingBase owner = this.beholder.getOwner();
            if (foods.isEmpty() || !this.beholder.isEffectEnabled(owner)) {
                return;
            }

            EntityShubNiggurathSpawn food = foods.get(this.beholder.rand.nextInt(foods.size()));
            food.setDead();
            this.beholder.swingArm(EnumHand.MAIN_HAND);
            this.beholder.world.setEntityState(this.beholder, EAT_STATUS);
            this.beholder.addPotionEffect(new PotionEffect(MobEffects.STRENGTH,
                    EAT_EFFECT_DURATION, 0, false, false));
            this.beholder.addPotionEffect(new PotionEffect(MobEffects.SPEED,
                    EAT_EFFECT_DURATION, 0, false, false));
            ModAdvancements.FAMILIAR.trigger(owner, FamiliarTrigger.Type.BEHOLDER_EAT);
        }

        @Override
        public void resetTask() {
            this.cooldown = MAX_COOLDOWN;
        }
    }

    private final class Eye {

        private final Vec3d pos;
        private Vec3d lookPos0;
        private Vec3d lookPos;
        private EyeTarget eyeTarget;
        private int shotTimer;
        private boolean initialized;

        private Eye(double x, double y, double z) {
            this.pos = new Vec3d(x, y, z);
            this.eyeTarget = new PositionEyeTarget(new Vec3d(0.0D, 0.0D, 0.0D));
            this.lookPos = new Vec3d(0.0D, 0.0D, 0.0D);
            this.lookPos0 = this.lookPos;
        }

        private void prepareShot(int targetId) {
            this.shotTimer = 20;
            this.eyeTarget = new EntityEyeTarget(targetId);
        }

        private void selectEyeTarget() {
            List<EntityLivingBase> nearby = EntityBeholderFamiliar.this.world.getEntitiesWithinAABB(
                    EntityLivingBase.class, EntityBeholderFamiliar.this.getEntityBoundingBox().grow(7.0D),
                    entity -> entity != EntityBeholderFamiliar.this);
            if (nearby.isEmpty()) {
                Vec3d position = new Vec3d(5.0D, 0.0D, 0.0D)
                        .rotateYaw(EntityBeholderFamiliar.this.rand.nextFloat() * 360.0F)
                        .rotatePitch(EntityBeholderFamiliar.this.rand.nextFloat() * 360.0F)
                        .add(EntityBeholderFamiliar.this.getPositionVector());
                this.eyeTarget = new PositionEyeTarget(position);
            } else {
                Entity target = nearby.get(EntityBeholderFamiliar.this.rand.nextInt(nearby.size()));
                this.eyeTarget = new EntityEyeTarget(target.getEntityId());
            }
        }

        private boolean needsNewEyeTarget() {
            Vec3d target = this.eyeTarget.getEyeTarget();
            return target == null || target.squareDistanceTo(EntityBeholderFamiliar.this.getPositionVector()) > 225.0D
                    || EntityBeholderFamiliar.this.rand.nextDouble() < 0.01D;
        }

        private void tick() {
            if (!this.initialized) {
                this.initialized = true;
                this.selectEyeTarget();
                Vec3d initialTarget = this.eyeTarget.getEyeTarget();
                this.lookPos = initialTarget == null ? new Vec3d(0.0D, 0.0D, 0.0D) : initialTarget;
                this.lookPos0 = this.lookPos;
            }
            this.lookPos0 = this.lookPos;
            if (this.needsNewEyeTarget() && this.shotTimer == 0) {
                this.selectEyeTarget();
            }

            Vec3d target = this.eyeTarget.getEyeTarget();
            if (target != null) {
                this.lookPos = lerpVec(0.2F, this.lookPos, target);
            }
            if (this.shotTimer > 0) {
                this.shotTimer--;
                if (this.shotTimer < 5) {
                    this.shoot();
                }
            }
        }

        private Vec2f getEyeRot(float partialTicks) {
            float bodyRot = toRads(rotLerp(partialTicks,
                    EntityBeholderFamiliar.this.prevRenderYawOffset,
                    EntityBeholderFamiliar.this.renderYawOffset));
            Vec3d eyePosition = interpolate(EntityBeholderFamiliar.this, partialTicks)
                    .add(0.0D, EntityBeholderFamiliar.this.getAnimationHeight(partialTicks), 0.0D)
                    .add(this.pos.rotateYaw(-bodyRot));
            Vec3d direction = lerpVec(partialTicks, this.lookPos0, this.lookPos).subtract(eyePosition);
            double yaw = MathHelper.atan2(direction.z, direction.x) + toRads(-90.0F) - bodyRot;
            double vertical = direction.normalize().y;
            float thirty = toRads(30.0F);
            return new Vec2f((float) (thirty - thirty * vertical), (float) yaw);
        }

        private void shoot() {
            float bodyRot = toRads(rotLerp(1.0F,
                    EntityBeholderFamiliar.this.prevRenderYawOffset,
                    EntityBeholderFamiliar.this.renderYawOffset));
            Vec2f rotation = this.getEyeRot(1.0F);
            Vec3d increment = new Vec3d(0.0D, 0.15D, 0.0D);
            Vec3d end = this.eyeTarget.getEyeTarget();
            if (end == null) {
                return;
            }

            Vec3d start = EntityBeholderFamiliar.this.getPositionVector()
                    .add(new Vec3d(this.pos.x, 0.9D + EntityBeholderFamiliar.this.getAnimationHeight(1.0F),
                            this.pos.z).rotateYaw(-bodyRot))
                    .add(increment);
            for (int i = 0; i < 3; i++) {
                increment = increment.rotatePitch(-rotation.x);
                start = start.add(increment.rotateYaw(-rotation.y - bodyRot));
            }

            Vec3d direction = end.subtract(start).normalize();
            AxisAlignedBB endBox = new AxisAlignedBB(end, end).grow(0.25D);
            for (int i = 0; i < 150; i++) {
                Vec3d particle = start.add(direction.scale(i * 0.1D));
                EntityBeholderFamiliar.this.world.spawnParticle(EnumParticleTypes.REDSTONE,
                        particle.x, particle.y, particle.z,
                        EntityBeholderFamiliar.this.getRed(), EntityBeholderFamiliar.this.getBlue(),
                        EntityBeholderFamiliar.this.getGreen());
                if (endBox.intersects(new AxisAlignedBB(particle, particle).grow(0.25D))) {
                    break;
                }
            }
        }

        private abstract class EyeTarget {
            @Nullable
            abstract Vec3d getEyeTarget();
        }

        private final class EntityEyeTarget extends EyeTarget {

            private final int entityId;

            private EntityEyeTarget(int entityId) {
                this.entityId = entityId;
            }

            @Override
            @Nullable
            Vec3d getEyeTarget() {
                Entity entity = EntityBeholderFamiliar.this.world.getEntityByID(this.entityId);
                return entity == null ? null : entity.getPositionEyes(0.0F);
            }
        }

        private final class PositionEyeTarget extends EyeTarget {

            private final Vec3d position;

            private PositionEyeTarget(Vec3d position) {
                this.position = position;
            }

            @Override
            Vec3d getEyeTarget() {
                return this.position;
            }
        }
    }

    private static Vec3d interpolate(Entity entity, float partialTicks) {
        return new Vec3d(entity.prevPosX + (entity.posX - entity.prevPosX) * partialTicks,
                entity.prevPosY + (entity.posY - entity.prevPosY) * partialTicks,
                entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTicks);
    }
}
