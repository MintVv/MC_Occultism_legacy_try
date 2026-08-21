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

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIFollow;
import net.minecraft.entity.ai.EntityAIFollowOwner;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISit;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

public class EntityGoatFamiliar extends EntityFamiliarBase implements IBlacksmithUpgradable {

    private static final float BASE_WIDTH = 0.7F;
    private static final float BASE_HEIGHT = 0.8F;
    private static final byte MAX_SIZE = 100;

    private static final DataParameter<Byte> VARIANTS =
            EntityDataManager.createKey(EntityGoatFamiliar.class, DataSerializers.BYTE);
    private static final DataParameter<Boolean> BLACKSMITH_UPGRADE =
            EntityDataManager.createKey(EntityGoatFamiliar.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Byte> FAMILIAR_SIZE =
            EntityDataManager.createKey(EntityGoatFamiliar.class, DataSerializers.BYTE);

    private int shakeHeadTimer;
    private boolean partying;
    private BlockPos jukeboxPosition;

    public EntityGoatFamiliar(World worldIn) {
        super(worldIn);
        this.setSize(BASE_WIDTH, BASE_HEIGHT);
        this.enablePersistence();
        this.experienceValue = 2;
    }

    public EntityGoatFamiliar(World worldIn, boolean hasRing, boolean hasBeard, byte size,
            EntityLivingBase owner) {
        this(worldIn);
        this.setRing(hasRing);
        this.setBeard(hasBeard);
        this.setSize(size);
        if (owner != null) {
            this.setTamed(true);
            this.setOwnerId(owner.getUniqueID());
        }
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(VARIANTS, Byte.valueOf((byte) 0));
        this.dataManager.register(BLACKSMITH_UPGRADE, Boolean.FALSE);
        this.dataManager.register(FAMILIAR_SIZE, Byte.valueOf((byte) 0));
    }

    @Override
    protected void initEntityAI() {
        this.aiSit = new EntityAISit(this);
        this.tasks.addTask(0, new EntityAIPanic(this, 1.25D));
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, this.aiSit);
        this.tasks.addTask(2, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(3, new EntityAIFollowOwner(this, 1.0D, 3.0F, 1.0F));
        this.tasks.addTask(6, new EntityAIWanderAvoidWater(this, 1.0D));
        this.tasks.addTask(7, new EntityAIFollow(this, 1.0D, 3.0F, 7.0F));
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
        if (this.world.isRemote) {
            this.shakeHeadTimer--;
            if (this.jukeboxPosition == null
                    || this.jukeboxPosition.distanceSq(this.posX, this.posY, this.posZ) > 12.0D
                    || this.world.getBlockState(this.jukeboxPosition).getBlock() != Blocks.JUKEBOX) {
                this.partying = false;
                this.jukeboxPosition = null;
            }
        }
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (!super.attackEntityFrom(source, amount)) {
            return false;
        }
        if (source.getTrueSource() != null) {
            ringBell(this, this.hasBlacksmithUpgrade());
        }
        return true;
    }

    public static void ringBell(EntityTameable familiar, boolean hasBlacksmithUpgrade) {
        EntityLivingBase owner = familiar.getOwner();
        if (owner == null || !hasBlacksmithUpgrade) {
            return;
        }

        // Bells do not exist in 1.12; the note-block harp preserves the audible warning cue.
        familiar.playSound(SoundEvents.BLOCK_NOTE_HARP, 1.0F, 1.0F);
        List<EntityLiving> monsters = familiar.world.getEntitiesWithinAABB(EntityLiving.class,
                familiar.getEntityBoundingBox().grow(30.0D),
                entity -> entity != null && entity.isEntityAlive()
                        && entity.isCreatureType(EnumCreatureType.MONSTER, false));
        for (EntityLiving monster : monsters) {
            monster.setAttackTarget(owner);
        }
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (this.isOwner(player) && this.isTransformItem(stack)) {
            if (this.isInTransformationBiome(player) || this.isInTransformationBiome(this)) {
                if (matchesOre(stack, "dyeBlack")) {
                    this.setBlack(true);
                } else if (stack.getItem() == Items.ENDER_EYE) {
                    this.setRedEyes(true);
                } else if (stack.getItem() == Items.FLINT) {
                    this.setEvilHorns(true);
                }

                if (this.shouldTransform()) {
                    this.transform();
                    if (!this.world.isRemote) {
                        ModAdvancements.FAMILIAR.trigger(player, FamiliarTrigger.Type.SHUB_NIGGURATH_SUMMON);
                    }
                }
                if (!this.world.isRemote) {
                    stack.shrink(1);
                }
                return true;
            }

            this.shakeHeadTimer = 20;
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

    private void transform() {
        if (this.world.isRemote) {
            float scale = this.getScale();
            for (int i = 0; i < 30; i++) {
                double x = this.posX + (this.rand.nextDouble() * 2.0D - 1.0D) * this.width * scale;
                double y = this.posY + this.rand.nextDouble() * this.height;
                double z = this.posZ + (this.rand.nextDouble() * 2.0D - 1.0D) * this.width * scale;
                this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x, y, z, 0.0D, 0.0D, 0.0D);
            }
            return;
        }

        EntityShubNiggurathFamiliar shub = new EntityShubNiggurathFamiliar(this.world, this);
        this.world.spawnEntity(shub);
        this.setDead();
    }

    boolean isTransformItem(ItemStack stack) {
        return matchesOre(stack, "dyeBlack") && !this.isBlack()
                || stack.getItem() == Items.FLINT && !this.hasEvilHorns()
                || stack.getItem() == Items.ENDER_EYE && !this.hasRedEyes();
    }

    boolean shouldTransform() {
        return this.isBlack() && this.hasRedEyes() && this.hasEvilHorns();
    }

    private boolean isInTransformationBiome(Entity entity) {
        return BiomeDictionary.hasType(this.world.getBiome(entity.getPosition()), BiomeDictionary.Type.FOREST);
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

    public boolean isBlack() {
        return this.hasVariant(2);
    }

    private void setBlack(boolean value) {
        this.setVariant(2, value);
    }

    public boolean hasRedEyes() {
        return this.hasVariant(3);
    }

    private void setRedEyes(boolean value) {
        this.setVariant(3, value);
    }

    public boolean hasEvilHorns() {
        return this.hasVariant(4);
    }

    private void setEvilHorns(boolean value) {
        this.setVariant(4, value);
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

    public byte getSize() {
        return this.dataManager.get(FAMILIAR_SIZE).byteValue();
    }

    public void setSize(byte size) {
        this.dataManager.set(FAMILIAR_SIZE,
                Byte.valueOf((byte) MathHelper.clamp((int) size, 0, (int) MAX_SIZE)));
        this.updatePhysicalSize();
    }

    public float getScale() {
        return 1.0F + this.getSize() / (float) MAX_SIZE;
    }

    public float getNeckYRot(float partialTicks) {
        if (this.shakeHeadTimer <= 0) {
            return 0.0F;
        }
        return MathHelper.sin((this.shakeHeadTimer - partialTicks) / 20.0F * (float) Math.PI * 5.0F)
                * (float) Math.toRadians(30.0D);
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> key) {
        super.notifyDataManagerChange(key);
        if (FAMILIAR_SIZE.equals(key)) {
            this.updatePhysicalSize();
        }
    }

    private void updatePhysicalSize() {
        float scale = this.getScale();
        super.setSize(BASE_WIDTH * scale, BASE_HEIGHT * scale);
        this.setScaleForAge(false);
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
        compound.setByte("getSize", this.getSize());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasKey("variants", 1)) {
            this.dataManager.set(VARIANTS, Byte.valueOf(compound.getByte("variants")));
        } else {
            this.setRing(compound.getBoolean("hasRing"));
        }
        this.setBlacksmithUpgrade(compound.getBoolean("hasBlacksmithUpgrade"));
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
}
