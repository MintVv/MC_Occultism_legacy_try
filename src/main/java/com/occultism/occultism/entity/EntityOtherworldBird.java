package com.occultism.occultism.entity;

import java.util.Arrays;

import javax.annotation.Nullable;

import com.occultism.occultism.registry.ModPotions;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIFollow;
import net.minecraft.entity.ai.EntityAIFollowOwnerFlying;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISit;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWanderAvoidWaterFlying;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.passive.EntityParrot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

/**
 * 1.12-compatible Otherworld Bird (Drikwing) port.
 *
 * <p>The upstream class is a dedicated Parrot subclass. This implementation
 * keeps the vanilla Parrot's tame/NBT behavior, but removes the shoulder
 * landing goal and adds the familiar owner/effect contract available locally
 * in 1.12.</p>
 */
public class EntityOtherworldBird extends EntityParrot implements IFamiliar {

    public static final float MAX_BOOST_DISTANCE = 8.0F;

    public EntityOtherworldBird(World worldIn) {
        super(worldIn);
        this.enablePersistence();
    }

    @Override
    protected void initEntityAI() {
        // Same movement profile as a parrot, but intentionally no shoulder
        // landing goal (the upstream bird never lands).
        this.aiSit = new EntityAISit(this);
        this.tasks.addTask(0, new EntityAIPanic(this, 1.25D));
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(2, this.aiSit);
        this.tasks.addTask(2, new EntityAIFollowOwnerFlying(this, 1.0D, 5.0F, 1.0F));
        this.tasks.addTask(2, new EntityAIWanderAvoidWaterFlying(this, 1.0D));
        this.tasks.addTask(3, new EntityAIFollow(this, 1.0D, 3.0F, 7.0F));
    }

    @Override
    public void onLivingUpdate() {
        if (!this.world.isRemote && this.ticksExisted % 10 == 0 && this.isTamed()) {
            EntityLivingBase owner = this.getOwner();
            if (owner != null && owner.isEntityAlive() && this.getDistanceSq(owner)
                    < MAX_BOOST_DISTANCE * MAX_BOOST_DISTANCE) {
                for (PotionEffect effect : this.getFamiliarEffects()) {
                    owner.addPotionEffect(new PotionEffect(effect));
                }
            }
        }
        super.onLivingUpdate();
    }

    /** Otherworld Birds remain airborne instead of entering a player's shoulder NBT. */
    @Override
    public boolean canSitOnShoulder() {
        return false;
    }

    @Override
    @Nullable
    public EntityLivingBase getFamiliarOwner() {
        return this.getOwner();
    }

    @Override
    public void setFamiliarOwner(EntityLivingBase owner) {
        if (owner == null) {
            return;
        }
        this.setTamed(true);
        this.setOwnerId(owner.getUniqueID());
    }

    @Override
    public EntityLivingBase getFamiliarEntity() {
        return this;
    }

    @Override
    public Iterable<PotionEffect> getFamiliarEffects() {
        // 1.12 has no local Double Jump effect. Keep the two available
        // equivalents and document the missing upstream capability.
        return Arrays.asList(
                new PotionEffect(MobEffects.JUMP_BOOST, 60, 5, false, false),
                new PotionEffect(ModPotions.FAIRY_SLOW_FALL, 300, 0, false, false));
    }
}
