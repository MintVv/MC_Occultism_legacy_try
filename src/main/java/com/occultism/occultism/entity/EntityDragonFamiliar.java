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
import com.occultism.occultism.registry.ModPotions;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIFollow;
import net.minecraft.entity.ai.EntityAIFollowOwner;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISit;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.oredict.OreDictionary;

public class EntityDragonFamiliar extends EntityFamiliarBase implements IBlacksmithUpgradable {

    public static final int MAX_PET_TIMER = 40;

    private static final int GREEDY_INCREMENT = 6000;
    private static final double OWNER_EFFECT_RANGE_SQ = 64.0D;
    private static final DataParameter<Byte> VARIANTS =
            EntityDataManager.createKey(EntityDragonFamiliar.class, DataSerializers.BYTE);
    private static final DataParameter<Boolean> BLACKSMITH_UPGRADE =
            EntityDataManager.createKey(EntityDragonFamiliar.class, DataSerializers.BOOLEAN);

    private final float colorOffset;
    private int greedyTimer;
    private int flyingTimer;
    private int flyingTimer0;
    private int wingspan;
    private int wingspan0;
    private int petTimer;
    private boolean partying;
    private BlockPos jukeboxPosition;

    public EntityDragonFamiliar(World worldIn) {
        super(worldIn);
        this.setSize(1.0F, 0.8F);
        this.isImmuneToFire = true;
        this.enablePersistence();
        this.experienceValue = 2;
        this.colorOffset = this.rand.nextFloat() * 2.0F;
        this.petTimer = MAX_PET_TIMER;
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
        this.tasks.addTask(0, new EntityAIPanic(this, 1.25D));
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, this.aiSit);
        this.tasks.addTask(2, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(3, new EntityGreedyFamiliar.FindItemGoal(this) {
            @Override
            public boolean shouldExecute() {
                return super.shouldExecute() && EntityDragonFamiliar.this.getPassengers().stream()
                        .anyMatch(entity -> entity instanceof EntityGreedyFamiliar);
            }
        });
        this.tasks.addTask(4, new FetchGoal(this));
        this.tasks.addTask(5, new EntityAIFollowOwner(this, 1.0D, 3.0F, 1.0F));
        this.tasks.addTask(6, new EntityDevilFamiliar.AttackGoal(this, 5.0F) {
            @Override
            public boolean shouldExecute() {
                return super.shouldExecute() && !EntityDragonFamiliar.this.hasBlacksmithUpgrade();
            }
        });
        this.tasks.addTask(6, new ThrowSwordGoal(this, 100.0F));
        this.tasks.addTask(7, new EntityAIWanderAvoidWater(this, 1.0D));
        this.tasks.addTask(8, new EntityAIFollow(this, 1.0D, 3.0F, 7.0F));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(25.0D);
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
        this.setFez(this.rand.nextDouble() < 0.1D);
        this.setEars(this.rand.nextDouble() < 0.5D);
        this.setArms(this.rand.nextDouble() < 0.5D);
        return super.onInitialSpawn(difficulty, livingdata);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();

        if (this.greedyTimer > 0) {
            this.greedyTimer--;
        }

        if (!this.onGround && this.motionY < 0.0D) {
            this.motionY *= 0.5D;
        }

        if (!this.world.isRemote && this.ticksExisted % 10 == 0 && this.isOwnerNearForEffect()) {
            EntityLivingBase owner = this.getOwner();
            owner.addPotionEffect(new PotionEffect(ModPotions.DRAGON_GREED, 300,
                    this.greedyTimer > 0 ? 1 : 0, false, false));
        }

        if (this.world.isRemote) {
            if (this.jukeboxPosition == null
                    || this.jukeboxPosition.distanceSq(this.posX, this.posY, this.posZ) > 12.0D
                    || this.world.getBlockState(this.jukeboxPosition).getBlock() != Blocks.JUKEBOX) {
                this.partying = false;
                this.jukeboxPosition = null;
            }
            this.updateClientAnimations();
        }
    }

    @Override
    public Iterable<PotionEffect> getFamiliarEffects() {
        return java.util.Collections.singletonList(new PotionEffect(ModPotions.DRAGON_GREED, 300,
                this.greedyTimer > 0 ? 1 : 0, false, false));
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (this.hasStick()) {
            if (!this.world.isRemote) {
                ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(Items.STICK));
                this.setStick(false);
            }
            return true;
        } else if (matchesOre(stack, "nuggetGold")) {
            if (!this.world.isRemote) {
                ModAdvancements.FAMILIAR.trigger(this.getFamiliarOwner(), FamiliarTrigger.Type.DRAGON_NUGGET);
            }
            this.greedyTimer += GREEDY_INCREMENT;
            if (!this.world.isRemote) {
                stack.shrink(1);
            } else {
                this.world.spawnParticle(EnumParticleTypes.HEART, this.posX, this.posY + 1.0D, this.posZ,
                        0.0D, 0.0D, 0.0D);
            }
            return true;
        } else if (stack.isEmpty() && player.isSneaking()) {
            if (!this.world.isRemote) {
                ModAdvancements.FAMILIAR.trigger(player, FamiliarTrigger.Type.DRAGON_PET);
            }
            this.petTimer = 0;
            return true;
        }

        if (hand == EnumHand.MAIN_HAND && this.isOwner(player) && stack.isEmpty()) {
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
    public void swingArm(EnumHand hand) {
        super.swingArm(hand);
        this.swingProgressInt = -14;
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }

    @Override
    public double getMountedYOffset() {
        return super.getMountedYOffset() * 0.4D;
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

    public boolean isOwnerNearForEffect() {
        EntityLivingBase owner = this.getOwner();
        return owner != null && owner.isEntityAlive() && this.getDistanceSq(owner) < OWNER_EFFECT_RANGE_SQ;
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
        if (this.canBlacksmithUpgrade()) {
            this.setBlacksmithUpgrade(true);
        }
    }

    public boolean hasSword() {
        return this.hasBlacksmithUpgrade() && !this.isSwingInProgress;
    }

    public float getAttackProgress(float partialTicks) {
        return (this.swingProgressInt + 14.0F + partialTicks) / 20.0F;
    }

    public int getPetTimer() {
        return this.petTimer;
    }

    public float getFlyingTimer(float partialTicks) {
        return lerp(partialTicks, this.flyingTimer0, this.flyingTimer);
    }

    public float getWingspan(float partialTicks) {
        return lerp(partialTicks, this.wingspan0, this.wingspan);
    }

    public boolean hasFez() {
        return this.hasVariant(0);
    }

    public boolean hasEars() {
        return this.hasVariant(1);
    }

    public boolean hasArms() {
        return this.hasVariant(2);
    }

    public boolean hasStick() {
        return this.hasVariant(3);
    }

    public float getEyeColorR(float partialTicks) {
        return Math.abs(MathHelper.sin((this.ticksExisted + partialTicks + 5.0F) / 20.0F + this.colorOffset))
                * 0.8F + 0.2F;
    }

    public float getEyeColorG(float partialTicks) {
        return Math.abs(MathHelper.sin((this.ticksExisted + partialTicks + 10.0F) / 30.0F + this.colorOffset))
                * 0.8F + 0.2F;
    }

    public float getEyeColorB(float partialTicks) {
        return Math.abs(MathHelper.sin((this.ticksExisted + partialTicks) / 40.0F + this.colorOffset))
                * 0.8F + 0.2F;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setByte("variants", this.dataManager.get(VARIANTS).byteValue());
        compound.setBoolean("hasBlacksmithUpgrade", this.hasBlacksmithUpgrade());
        compound.setInteger("greedyTimer", this.greedyTimer);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasKey("variants", 1)) {
            this.dataManager.set(VARIANTS, Byte.valueOf(compound.getByte("variants")));
        } else {
            this.setFez(compound.getBoolean("hasFez"));
            this.setEars(compound.getBoolean("hasEars"));
            this.setArms(compound.getBoolean("hasArms"));
            this.setStick(compound.getBoolean("hasStick"));
        }
        this.setBlacksmithUpgrade(compound.getBoolean("hasBlacksmithUpgrade"));
        this.greedyTimer = compound.getInteger("greedyTimer");
        this.isImmuneToFire = true;
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

    private void setFez(boolean value) {
        this.setVariant(0, value);
    }

    private void setEars(boolean value) {
        this.setVariant(1, value);
    }

    private void setArms(boolean value) {
        this.setVariant(2, value);
    }

    private void setStick(boolean value) {
        this.setVariant(3, value);
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

    private void updateClientAnimations() {
        this.flyingTimer0 = this.flyingTimer;
        this.wingspan0 = this.wingspan;
        if (this.onGround) {
            this.wingspan = Math.max(0, this.wingspan - 5);
        } else {
            this.flyingTimer++;
            this.wingspan = Math.min(30, this.wingspan + 5);
        }
        if (this.petTimer < MAX_PET_TIMER) {
            this.petTimer++;
        }
    }

    private static boolean matchesOre(ItemStack stack, String oreName) {
        if (stack.isEmpty()) {
            return false;
        }
        for (int id : OreDictionary.getOreIDs(stack)) {
            if (oreName.equals(OreDictionary.getOreName(id))) {
                return true;
            }
        }
        return false;
    }

    private static float lerp(float amount, float start, float end) {
        return start + amount * (end - start);
    }

    private static final class ThrowSwordGoal extends EntityDevilFamiliar.AttackGoal {

        private final EntityDragonFamiliar dragon;

        private ThrowSwordGoal(EntityDragonFamiliar dragon, float range) {
            super(dragon, range);
            this.dragon = dragon;
        }

        @Override
        public boolean shouldExecute() {
            return super.shouldExecute() && this.dragon.hasBlacksmithUpgrade();
        }

        @Override
        protected void attack(List<EntityLivingBase> enemies) {
            if (enemies.isEmpty()) {
                return;
            }

            Entity target = enemies.get(this.dragon.getRNG().nextInt(enemies.size()));
            EntityThrownSword sword = new EntityThrownSword(this.dragon.world, this.dragon.getOwner());
            double x = this.dragon.posX;
            double y = this.dragon.posY + this.dragon.getEyeHeight();
            double z = this.dragon.posZ;
            double xDirection = target.posX - x;
            double yDirection = target.posY + target.height - y;
            double zDirection = target.posZ - z;
            sword.setPosition(x, y, z);
            sword.shoot(xDirection, yDirection, zDirection, 0.5F, 3.0F);
            this.dragon.world.spawnEntity(sword);
        }
    }

    private static final class FetchGoal extends EntityAIBase {

        private final EntityDragonFamiliar dragon;
        private EntityItem stick;

        private FetchGoal(EntityDragonFamiliar dragon) {
            this.dragon = dragon;
            this.setMutexBits(5);
        }

        @Override
        public boolean shouldExecute() {
            this.stick = this.findStick();
            return this.stick != null && !this.dragon.hasStick();
        }

        @Override
        public boolean shouldContinueExecuting() {
            return this.stick != null && !this.dragon.hasStick();
        }

        @Override
        public void startExecuting() {
            this.dragon.getNavigator().tryMoveToEntityLiving(this.stick, 1.2D);
        }

        @Override
        public void resetTask() {
            this.dragon.getNavigator().clearPath();
            this.stick = null;
        }

        @Override
        public void updateTask() {
            if (this.stick == null || this.stick.isDead) {
                this.stick = this.findStick();
                if (this.stick == null) {
                    return;
                }
            }

            this.dragon.getNavigator().tryMoveToEntityLiving(this.stick, 1.2D);
            if (this.dragon.getDistanceSq(this.stick) < 3.0D) {
                ItemStack stack = this.stick.getItem();
                if (stack.isEmpty() || stack.getItem() != Items.STICK) {
                    this.stick = null;
                    return;
                }
                this.dragon.setStick(true);
                ModAdvancements.FAMILIAR.trigger(this.dragon.getFamiliarOwner(), FamiliarTrigger.Type.DRAGON_FETCH);
                stack.shrink(1);
                if (stack.isEmpty()) {
                    this.stick.setDead();
                } else {
                    this.stick.setItem(stack);
                }
                this.stick = null;
            }
        }

        @Nullable
        private EntityItem findStick() {
            List<EntityItem> sticks = this.dragon.world.getEntitiesWithinAABB(EntityItem.class,
                    this.dragon.getEntityBoundingBox().grow(8.0D),
                    item -> item != null && !item.isDead && item.getItem().getItem() == Items.STICK);
            return sticks.isEmpty() ? null : sticks.get(0);
        }
    }
}
