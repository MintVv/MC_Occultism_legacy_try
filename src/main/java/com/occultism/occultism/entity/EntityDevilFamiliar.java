package com.occultism.occultism.entity;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.occultism.occultism.advancement.FamiliarTrigger;
import com.occultism.occultism.registry.ModAdvancements;

import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIFollowOwner;
import net.minecraft.entity.ai.EntityAISit;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.Items;
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
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemHandlerHelper;

public class EntityDevilFamiliar extends EntityFamiliarBase implements IBlacksmithUpgradable {

    private static final DataParameter<Boolean> BLACKSMITH_UPGRADE =
            EntityDataManager.createKey(EntityDevilFamiliar.class, DataSerializers.BOOLEAN);
    private static final long SIN_INTERVAL = 20L * 60L * 33L;
    private static final double OWNER_EFFECT_RANGE_SQ = 64.0D;
    private final float heightOffset;
    private long sinLastTime;

    public EntityDevilFamiliar(World worldIn) {
        super(worldIn);
        this.setSize(0.8F, 1.8F);
        this.isImmuneToFire = true;
        this.enablePersistence();
        this.experienceValue = 2;
        this.heightOffset = this.rand.nextFloat() * 5.0F;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(BLACKSMITH_UPGRADE, Boolean.FALSE);
    }

    @Override
    protected void initEntityAI() {
        this.aiSit = new EntityAISit(this);
        this.tasks.addTask(1, this.aiSit);
        this.tasks.addTask(2, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(3, new EntityAIFollowOwner(this, 1.0D, 3.0F, 1.0F));
        this.tasks.addTask(4, new AttackGoal(this, 5.0F));
        this.tasks.addTask(5, new EntityAIWanderAvoidWater(this, 1.0D));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(15.0D);
        if (this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) == null) {
            this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);
        } else {
            this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);
        }
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (!this.world.isRemote && this.ticksExisted % 10 == 0 && this.isOwnerNearForEffect()) {
            EntityLivingBase owner = this.getOwner();
            owner.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, 300, 0, false, false));
        }
        if (this.world.isRemote && this.isSwingInProgress) {
            this.spawnAttackParticles();
        }
    }

    @Override
    public Iterable<PotionEffect> getFamiliarEffects() {
        return java.util.Collections.singletonList(
                new PotionEffect(MobEffects.FIRE_RESISTANCE, 300, 0, false, false));
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (this.isOwner(player) && isNormalGoldenApple(stack)) {
            if (!this.world.isRemote) {
                long remaining = this.sinLastTime + SIN_INTERVAL - this.world.getTotalWorldTime();
                if (!this.hasBlacksmithUpgrade()) {
                    player.sendStatusMessage(new TextComponentTranslation("dialog.occultism.devil.no_upgrade"), true);
                } else if (remaining < 0L) {
                    this.sinLastTime = this.world.getTotalWorldTime();
                    if (!player.capabilities.isCreativeMode) {
                        stack.shrink(1);
                    }
                    ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(Items.GOLDEN_APPLE, 1, 1));
                } else {
                    player.sendStatusMessage(new TextComponentTranslation("dialog.occultism.devil.sin_on_cooldown",
                            Long.valueOf(remaining)), true);
                }
            }
            return true;
        }
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

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("hasBlacksmithUpgrade", this.hasBlacksmithUpgrade());
        compound.setLong("sinLastTime", this.sinLastTime);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setBlacksmithUpgrade(compound.getBoolean("hasBlacksmithUpgrade"));
        this.sinLastTime = compound.getLong("sinLastTime");
        this.isImmuneToFire = true;
    }

    private void setBlacksmithUpgrade(boolean value) {
        this.dataManager.set(BLACKSMITH_UPGRADE, Boolean.valueOf(value));
    }

    private static boolean isNormalGoldenApple(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() == Items.GOLDEN_APPLE && stack.getItemDamage() == 0;
    }

    public boolean isOwnerNearForEffect() {
        EntityLivingBase owner = this.getOwner();
        return owner != null && owner.isEntityAlive() && this.getDistanceSq(owner) < OWNER_EFFECT_RANGE_SQ;
    }

    public float getAnimationHeight(float partialTicks) {
        return MathHelper.cos((this.ticksExisted + this.heightOffset + partialTicks) / 3.5F);
    }

    private void spawnAttackParticles() {
        Vec3d direction = this.getLookVec().scale(0.6D);
        for (int i = 0; i < 5; i++) {
            double x = this.posX + direction.x + (this.rand.nextFloat() - 0.5F) * 0.7F;
            double y = this.posY + 1.5D + (this.rand.nextFloat() - 0.5F) * 0.7F;
            double z = this.posZ + direction.z + (this.rand.nextFloat() - 0.5F) * 0.7F;
            this.world.spawnParticle(EnumParticleTypes.FLAME, x, y, z,
                    direction.x * 0.25D, 0.0D, direction.z * 0.25D);
        }
    }

    public static class AttackGoal extends EntityAIBase {

        private static final int MAX_COOLDOWN = 20;

        protected final EntityTameable familiar;
        private final float range;
        private int cooldown = MAX_COOLDOWN;

        public AttackGoal(EntityTameable familiar, float range) {
            this.familiar = familiar;
            this.range = range;
        }

        @Override
        public boolean shouldExecute() {
            return this.cooldown-- < 0 && this.familiar.getOwner() instanceof EntityPlayer
                    && !this.getNearbyEnemies().isEmpty();
        }

        @Override
        public boolean shouldContinueExecuting() {
            return false;
        }

        @Override
        public void startExecuting() {
            List<EntityLivingBase> enemies = this.getNearbyEnemies();
            this.attack(enemies);
            if (this.familiar instanceof EntityDevilFamiliar && !this.familiar.world.isRemote
                    && !enemies.isEmpty()) {
                ModAdvancements.FAMILIAR.trigger(this.familiar.getOwner(), FamiliarTrigger.Type.DEVIL_FIRE);
            }
            this.familiar.swingArm(EnumHand.MAIN_HAND);
            this.cooldown = MAX_COOLDOWN;
        }

        protected void attack(List<EntityLivingBase> enemies) {
            EntityPlayer owner = (EntityPlayer) this.familiar.getOwner();
            for (EntityLivingBase target : enemies) {
                target.attackEntityFrom(DamageSource.causePlayerDamage(owner), 4.0F);
                target.setFire(4);
            }
        }

        @Override
        public void resetTask() {
            this.cooldown = MAX_COOLDOWN;
        }

        private List<EntityLivingBase> getNearbyEnemies() {
            List<EntityLivingBase> targets = new ArrayList<>();
            EntityLivingBase owner = this.familiar.getOwner();
            if (owner != null) {
                this.addIfClose(targets, owner.getRevengeTarget());
                this.addIfClose(targets, owner.getLastAttackedEntity());
            }
            return targets;
        }

        private void addIfClose(List<EntityLivingBase> targets, @Nullable EntityLivingBase target) {
            if (target != null && target != this.familiar && this.familiar.getDistanceSq(target) < this.range) {
                targets.add(target);
            }
        }

    }
}
