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
import com.occultism.occultism.config.ModConfig;
import com.occultism.occultism.registry.ModAdvancements;

import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIFollow;
import net.minecraft.entity.ai.EntityAIFollowOwner;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

public class EntityBlacksmithFamiliar extends EntityFamiliarBase implements IFamiliar {

    private static final DataParameter<Byte> VARIANTS =
            EntityDataManager.createKey(EntityBlacksmithFamiliar.class, DataSerializers.BYTE);
    private static final DataParameter<Byte> BARS =
            EntityDataManager.createKey(EntityBlacksmithFamiliar.class, DataSerializers.BYTE);

    private int ironCount;
    private boolean partying;
    private BlockPos jukeboxPosition;

    public EntityBlacksmithFamiliar(World worldIn) {
        super(worldIn);
        this.setSize(0.65F, 1.0F);
        this.enablePersistence();
        this.experienceValue = 2;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(VARIANTS, Byte.valueOf((byte) 0));
        this.dataManager.register(BARS, Byte.valueOf((byte) 0));
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAIPanic(this, 1.25D));
        this.tasks.addTask(1, new BlacksmithSitGoal(this));
        this.tasks.addTask(2, new UpgradeGoal(this));
        this.tasks.addTask(3, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(4, new EntityAIFollowOwner(this, 1.0D, 3.0F, 1.0F));
        this.tasks.addTask(5, new EntityAIWanderAvoidWater(this, 1.0D));
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
        this.setEarring(this.rand.nextDouble() < 0.1D);
        this.setMarioMoustache(this.rand.nextDouble() < 0.5D);
        this.setSquareHair(this.rand.nextDouble() < 0.5D);
        return super.onInitialSpawn(difficulty, livingdata);
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        boolean isIngot = matchesOre(stack, "ingotIron");
        boolean isBlock = matchesOre(stack, "blockIron");
        if (this.isOwner(player) && this.ironCount < getMaxIron() && (isIngot || isBlock)) {
            if (!this.world.isRemote) {
                stack.shrink(1);
                this.changeIronCount(isIngot ? 1 : 9);
            }
            return true;
        }
        if (hand == EnumHand.MAIN_HAND && this.isOwner(player) && stack.isEmpty()) {
            this.setSitting(!this.isSitting());
            return true;
        }
        return super.processInteract(player, hand);
    }

    @Override
    public void onLivingUpdate() {
        if (this.world.isRemote && (this.jukeboxPosition == null
                || this.jukeboxPosition.distanceSq(this.posX, this.posY, this.posZ) > 12.0D
                || this.world.getBlockState(this.jukeboxPosition).getBlock() != Blocks.JUKEBOX)) {
            this.partying = false;
            this.jukeboxPosition = null;
        }
        super.onLivingUpdate();
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
    protected void dropEquipment(boolean wasRecentlyHit, int lootingModifier) {
        super.dropEquipment(wasRecentlyHit, lootingModifier);
        int blockCount = this.ironCount / 9;
        int ingotCount = this.ironCount % 9;
        if (ingotCount > 0) {
            this.entityDropItem(new ItemStack(Items.IRON_INGOT, ingotCount), 0.0F);
        }
        if (blockCount > 0) {
            this.entityDropItem(new ItemStack(Blocks.IRON_BLOCK, blockCount), 0.0F);
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setByte("variants", this.dataManager.get(VARIANTS).byteValue());
        compound.setInteger("ironCount", this.ironCount);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasKey("variants", 1)) {
            this.dataManager.set(VARIANTS, Byte.valueOf(compound.getByte("variants")));
        } else {
            this.setEarring(compound.getBoolean("hasEarring"));
            this.setMarioMoustache(compound.getBoolean("hasMarioMoustache"));
            this.setSquareHair(compound.getBoolean("hasSquareHair"));
        }
        this.setIronCount(compound.getInteger("ironCount"));
    }

    public boolean hasEarring() {
        return this.hasVariant(0);
    }

    public boolean hasMarioMoustache() {
        return this.hasVariant(1);
    }

    public boolean hasSquareHair() {
        return this.hasVariant(2);
    }

    public byte getBars() {
        return this.dataManager.get(BARS).byteValue();
    }

    public int getIronCount() {
        return this.ironCount;
    }

    private static int getMaxIron() {
        return ModConfig.SPIRIT_JOBS.blacksmithFamiliarUpgradeCost * 10;
    }

    private void setEarring(boolean value) {
        this.setVariant(0, value);
    }

    private void setMarioMoustache(boolean value) {
        this.setVariant(1, value);
    }

    private void setSquareHair(boolean value) {
        this.setVariant(2, value);
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

    private void setIronCount(int count) {
        this.ironCount = count;
        int cost = ModConfig.SPIRIT_JOBS.blacksmithFamiliarUpgradeCost;
        this.dataManager.set(BARS, Byte.valueOf((byte) Math.min(10, this.ironCount / cost)));
    }

    private void changeIronCount(int delta) {
        this.setIronCount(this.ironCount + delta);
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

    private static final class BlacksmithSitGoal extends EntityAIBase {

        private final EntityBlacksmithFamiliar blacksmith;

        private BlacksmithSitGoal(EntityBlacksmithFamiliar blacksmith) {
            this.blacksmith = blacksmith;
            this.setMutexBits(5);
        }

        @Override
        public boolean shouldExecute() {
            return !this.blacksmith.isInWater() && this.blacksmith.getOwner() != null
                    && this.blacksmith.isSitting();
        }

        @Override
        public void startExecuting() {
            this.blacksmith.getNavigator().clearPath();
        }

        @Override
        public void resetTask() {
            this.blacksmith.setSitting(false);
        }
    }

    private static final class UpgradeGoal extends EntityAIBase {

        private final EntityBlacksmithFamiliar blacksmith;
        private IBlacksmithUpgradable target;
        private int cooldown = ModConfig.SPIRIT_JOBS.blacksmithFamiliarUpgradeCooldown;

        private UpgradeGoal(EntityBlacksmithFamiliar blacksmith) {
            this.blacksmith = blacksmith;
            this.setMutexBits(5);
        }

        @Override
        public boolean shouldExecute() {
            this.target = this.findTarget();
            return this.blacksmith.ironCount >= ModConfig.SPIRIT_JOBS.blacksmithFamiliarUpgradeCost
                    && this.target != null && this.cooldown-- < 0;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return this.target != null;
        }

        @Override
        public void startExecuting() {
            this.moveToTarget();
        }

        @Override
        public void resetTask() {
            this.blacksmith.getNavigator().clearPath();
            this.cooldown = ModConfig.SPIRIT_JOBS.blacksmithFamiliarUpgradeCooldown;
            this.target = null;
        }

        @Override
        public void updateTask() {
            if (this.target == null) {
                return;
            }
            EntityLivingBase targetEntity = this.target.getFamiliarEntity();
            if (this.blacksmith.getNavigator().noPath()) {
                this.moveToTarget();
            }
            if (this.blacksmith.getDistanceSq(targetEntity) < 3.0D) {
                if (this.target.canBlacksmithUpgrade()) {
                    this.target.blacksmithUpgrade();
                    // Spend iron and grant the advancement only when the target
                    // actually transitions out of the upgradeable state.
                    if (!this.target.canBlacksmithUpgrade()) {
                        this.blacksmith.changeIronCount(-ModConfig.SPIRIT_JOBS.blacksmithFamiliarUpgradeCost);
                        ModAdvancements.FAMILIAR.trigger(this.blacksmith.getFamiliarOwner(),
                                FamiliarTrigger.Type.BLACKSMITH_UPGRADE);
                    }
                }
                this.target = null;
            }
        }

        @Nullable
        private IBlacksmithUpgradable findTarget() {
            List<EntityLivingBase> entities = this.blacksmith.world.getEntitiesWithinAABB(EntityLivingBase.class,
                    this.blacksmith.getEntityBoundingBox().grow(4.0D), this::isValidTarget);
            return entities.isEmpty() ? null : (IBlacksmithUpgradable) entities.get(0);
        }

        private boolean isValidTarget(@Nullable EntityLivingBase entity) {
            if (!(entity instanceof IBlacksmithUpgradable)) {
                return false;
            }
            IBlacksmithUpgradable familiar = (IBlacksmithUpgradable) entity;
            EntityLivingBase owner = familiar.getFamiliarOwner();
            return familiar.canBlacksmithUpgrade() && owner != null && owner == this.blacksmith.getOwner();
        }

        private void moveToTarget() {
            if (this.target != null) {
                this.blacksmith.getNavigator().tryMoveToEntityLiving(this.target.getFamiliarEntity(), 0.7D);
            }
        }
    }
}
