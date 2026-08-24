package com.occultism.occultism.entity;

import javax.annotation.Nullable;

import com.occultism.occultism.registry.ModItems;

import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.entity.IEntityLivingData;

public class EntityMercyGoat extends EntitySheep {

    public static final int RAM_COOLDOWN_TICKS = 60;
    public static final double RAM_RANGE_SQ = 2.25D;
    public static final float RAM_DAMAGE = 4.0F;
    public static final float RAM_KNOCKBACK = 0.7F;

    private boolean droppedCrueltyEssence;
    private int ramCooldown;

    public EntityMercyGoat(World worldIn) {
        super(worldIn);
        this.setFleeceColor(EnumDyeColor.WHITE);
        this.setSheared(false);
        this.enablePersistence();
        this.experienceValue = 3;
    }

    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData livingdata) {
        IEntityLivingData data = super.onInitialSpawn(difficulty, livingdata);
        this.setFleeceColor(EnumDyeColor.WHITE);
        this.setSheared(false);
        return data;
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        return false;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (this.world.isRemote) {
            return;
        }
        if (this.ramCooldown > 0) {
            this.ramCooldown--;
        }
        this.updateMercyGoatRam();
    }

    @Override
    public EntityMercyGoat createChild(EntityAgeable ageable) {
        return new EntityMercyGoat(this.world);
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return null;
    }

    @Override
    protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
    }

    @Override
    public void onDeath(DamageSource cause) {
        super.onDeath(cause);
        if (!this.world.isRemote && !this.droppedCrueltyEssence) {
            this.droppedCrueltyEssence = true;
            this.entityDropItem(new ItemStack(ModItems.CRUELTY_ESSENCE), 0.0F);
        }
    }

    private void updateMercyGoatRam() {
        EntityPlayer target = this.world.getNearestPlayerNotCreative(this, 12.0D);
        if (target == null || !target.isEntityAlive()) {
            return;
        }

        double distanceSq = this.getDistanceSq(target);
        if (distanceSq > RAM_RANGE_SQ) {
            this.getNavigator().tryMoveToEntityLiving(target, 1.35D);
            return;
        }
        if (this.ramCooldown > 0) {
            return;
        }

        boolean hit = target.attackEntityFrom(DamageSource.causeMobDamage(this), RAM_DAMAGE);
        if (hit) {
            target.knockBack(this, RAM_KNOCKBACK, target.posX - this.posX, target.posZ - this.posZ);
            this.world.playSound(null, this.posX, this.posY, this.posZ, net.minecraft.init.SoundEvents.ENTITY_SHEEP_HURT,
                    net.minecraft.util.SoundCategory.HOSTILE, 0.7F, 0.9F);
        }
        this.ramCooldown = RAM_COOLDOWN_TICKS;
    }
}
