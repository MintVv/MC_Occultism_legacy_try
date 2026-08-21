package com.occultism.occultism.entity;

import javax.annotation.Nullable;

import com.occultism.occultism.registry.ModItems;

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
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EntityPossessedBee extends EntityBat {

    private int attackCooldown;

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
        if (target == null || !target.isEntityAlive()) {
            return;
        }

        double dx = target.posX - this.posX;
        double dy = target.posY + target.getEyeHeight() * 0.5D - this.posY;
        double dz = target.posZ - this.posZ;
        double distanceSq = dx * dx + dy * dy + dz * dz;
        double distance = MathHelper.sqrt(distanceSq);
        if (distance > 0.0001D) {
            double speed = distanceSq > 4.0D ? 0.18D : 0.08D;
            this.motionX += dx / distance * speed;
            this.motionY += dy / distance * speed;
            this.motionZ += dz / distance * speed;
            this.rotationYaw = (float) (MathHelper.atan2(this.motionZ, this.motionX) * (180D / Math.PI)) - 90.0F;
        }

        if (distanceSq <= 1.4D && this.attackCooldown <= 0 && this.attackEntityAsMob(target)) {
            this.attackCooldown = 20;
            this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_BAT_HURT,
                    SoundCategory.HOSTILE, 0.35F, 1.4F);
        }
    }
}
