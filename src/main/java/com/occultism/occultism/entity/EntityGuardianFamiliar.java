package com.occultism.occultism.entity;

import javax.annotation.Nullable;

import com.occultism.occultism.advancement.FamiliarTrigger;
import com.occultism.occultism.registry.ModAdvancements;

import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIFollowOwner;
import net.minecraft.entity.ai.EntityAISit;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class EntityGuardianFamiliar extends EntityFamiliarBase implements IBlacksmithUpgradable {

    public static final byte ONE_ARMED = 4;
    public static final byte ONE_LEGGED = 3;
    public static final byte FLOATING = 2;
    public static final byte DEATHS_DOOR = 1;
    public static final byte DEAD = 0;
    public static final byte MAX_LIVES = 5;

    private static final DataParameter<Byte> VARIANTS =
            EntityDataManager.createKey(EntityGuardianFamiliar.class, DataSerializers.BYTE);
    private static final DataParameter<Byte> LIVES =
            EntityDataManager.createKey(EntityGuardianFamiliar.class, DataSerializers.BYTE);
    private static final DataParameter<Boolean> BLACKSMITH_UPGRADE =
            EntityDataManager.createKey(EntityGuardianFamiliar.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Float> RED =
            EntityDataManager.createKey(EntityGuardianFamiliar.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> GREEN =
            EntityDataManager.createKey(EntityGuardianFamiliar.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> BLUE =
            EntityDataManager.createKey(EntityGuardianFamiliar.class, DataSerializers.FLOAT);

    private byte previousLives = -1;
    private int particleTimer;

    public EntityGuardianFamiliar(World worldIn) {
        super(worldIn);
        this.setSize(0.8F, 1.4F);
        this.enablePersistence();
        this.experienceValue = 2;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(VARIANTS, Byte.valueOf((byte) 0));
        this.dataManager.register(LIVES, Byte.valueOf(DEAD));
        this.dataManager.register(BLACKSMITH_UPGRADE, Boolean.FALSE);
        this.dataManager.register(RED, Float.valueOf(1.0F));
        this.dataManager.register(GREEN, Float.valueOf(1.0F));
        this.dataManager.register(BLUE, Float.valueOf(1.0F));
    }

    @Override
    protected void initEntityAI() {
        this.aiSit = new EntityAISit(this);
        this.tasks.addTask(1, this.aiSit);
        this.tasks.addTask(2, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(3, new EntityAIFollowOwner(this, 1.0D, 3.0F, 1.0F));
        this.tasks.addTask(4, new EntityAIWanderAvoidWater(this, 1.0D));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(15.0D);
    }

    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        IEntityLivingData data = super.onInitialSpawn(difficulty, livingdata);
        this.setColor();
        this.setTree(this.rand.nextDouble() < 0.1D);
        this.setBird(this.rand.nextDouble() < 0.5D);
        this.setTools(this.rand.nextDouble() < 0.5D);
        this.setLives((byte) (this.rand.nextInt(MAX_LIVES) + 1));
        return data;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (!this.world.isRemote && this.getLives() <= DEAD && this.isEntityAlive()) {
            this.attackEntityFrom(DamageSource.OUT_OF_WORLD, Float.MAX_VALUE);
            return;
        }
        if (this.previousLives != -1 && this.previousLives > this.getLives()) {
            this.particleTimer = 30;
            this.playSound(SoundEvents.ENTITY_IRONGOLEM_HURT, this.getSoundVolume(), 1.0F);
        }
        this.previousLives = this.getLives();
        if (!this.world.isRemote && this.particleTimer-- > 0 && this.world instanceof WorldServer) {
            ((WorldServer) this.world).spawnParticle(EnumParticleTypes.BLOCK_CRACK, this.posX,
                    this.posY + this.height * 0.5D, this.posZ, 8, 0.35D, 0.35D, 0.35D, 0.02D,
                    1);
        }
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        if (hand == EnumHand.MAIN_HAND && this.isOwner(player) && player.getHeldItem(hand).isEmpty()) {
            this.setSitting(!this.isSitting());
            if (this.aiSit != null) {
                this.aiSit.setSitting(this.isSitting());
            }
            return true;
        }
        return super.processInteract(player, hand);
    }

    @Override
    public void setFamiliarOwner(EntityLivingBase owner) {
        super.setFamiliarOwner(owner);
        if (owner != null && this.world != null && !this.world.isRemote && this.hasTree()) {
            ModAdvancements.FAMILIAR.trigger(owner, FamiliarTrigger.Type.RARE_VARIANT);
        }
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.isEntityInvulnerable(source)) {
            return false;
        }
        this.setSitting(false);
        if (this.aiSit != null) {
            this.aiSit.setSitting(false);
        }
        return super.attackEntityFrom(source, amount);
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return null;
    }

    @Override
    protected boolean storeSoulShard(ItemStack shard) {
        byte lives = this.getLives();
        this.setLives((byte) (this.rand.nextInt(MAX_LIVES) + 1));
        try {
            return super.storeSoulShard(shard);
        } finally {
            this.setLives(lives);
        }
    }

    @Override
    @Nullable
    public EntityAgeable createChild(EntityAgeable ageable) {
        return null;
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.dataManager.set(VARIANTS, Byte.valueOf(compound.getByte("OccultismGuardianVariants")));
        this.dataManager.set(RED, Float.valueOf(compound.hasKey("OccultismGuardianRed")
                ? compound.getFloat("OccultismGuardianRed")
                : 1.0F));
        this.dataManager.set(GREEN, Float.valueOf(compound.hasKey("OccultismGuardianGreen")
                ? compound.getFloat("OccultismGuardianGreen")
                : 1.0F));
        this.dataManager.set(BLUE, Float.valueOf(compound.hasKey("OccultismGuardianBlue")
                ? compound.getFloat("OccultismGuardianBlue")
                : 1.0F));
        this.setLives(compound.hasKey("OccultismGuardianLives")
                ? compound.getByte("OccultismGuardianLives")
                : compound.getByte("lives"));
        this.setBlacksmithUpgrade(compound.getBoolean("hasBlacksmithUpgrade"));
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setByte("OccultismGuardianVariants", this.dataManager.get(VARIANTS).byteValue());
        compound.setByte("OccultismGuardianLives", this.getLives());
        compound.setFloat("OccultismGuardianRed", this.getRed());
        compound.setFloat("OccultismGuardianGreen", this.getGreen());
        compound.setFloat("OccultismGuardianBlue", this.getBlue());
        compound.setBoolean("hasBlacksmithUpgrade", this.hasBlacksmithUpgrade());
    }

    public boolean sacrificeOneLife(EntityPlayer owner) {
        if (!this.isEntityAlive() || this.isSitting() || !this.isTamed() || !this.isOwner(owner)
                || this.getLives() <= DEAD) {
            return false;
        }
        this.setLives((byte) (this.getLives() - 1));
        if (owner != null) {
            owner.setFire(0);
            owner.extinguish();
            owner.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 20 * 10, 1, false, true));
            owner.addPotionEffect(new PotionEffect(MobEffects.ABSORPTION, 20 * 5, 1, false, true));
        }
        return true;
    }

    public byte getLives() {
        return this.dataManager.get(LIVES).byteValue();
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
        return !this.hasBlacksmithUpgrade() && this.getLives() != MAX_LIVES;
    }

    @Override
    public void blacksmithUpgrade() {
        if (!this.canBlacksmithUpgrade()) {
            return;
        }
        this.setBlacksmithUpgrade(true);
        this.setLives((byte) (this.getLives() + 1));
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

    public boolean hasLegs() {
        return this.getLives() > FLOATING;
    }

    public boolean hasTree() {
        return this.hasVariant(0);
    }

    public boolean hasBird() {
        return this.hasVariant(1);
    }

    public boolean hasTools() {
        return this.hasVariant(2);
    }

    public float getAnimationHeight(float partialTicks) {
        return this.hasLegs() ? 0.0F : MathHelper.cos((this.ticksExisted + partialTicks) / 5.0F);
    }

    private void setLives(byte lives) {
        if (lives < DEAD) {
            lives = DEAD;
        }
        if (lives > MAX_LIVES) {
            lives = MAX_LIVES;
        }
        this.dataManager.set(LIVES, Byte.valueOf(lives));
    }

    private void setBlacksmithUpgrade(boolean value) {
        this.dataManager.set(BLACKSMITH_UPGRADE, Boolean.valueOf(value));
    }

    private void setColor() {
        for (int i = 0; i < 20; i++) {
            float red = this.rand.nextFloat();
            float green = this.rand.nextFloat();
            float blue = this.rand.nextFloat();
            if (Math.abs(red - green) > 0.2F || Math.abs(red - blue) > 0.2F || Math.abs(green - blue) > 0.2F) {
                this.dataManager.set(RED, Float.valueOf(red));
                this.dataManager.set(GREEN, Float.valueOf(green));
                this.dataManager.set(BLUE, Float.valueOf(blue));
                return;
            }
        }
    }

    private void setTree(boolean tree) {
        this.setVariant(0, tree);
    }

    private void setBird(boolean bird) {
        this.setVariant(1, bird);
    }

    private void setTools(boolean tools) {
        this.setVariant(2, tools);
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
}
