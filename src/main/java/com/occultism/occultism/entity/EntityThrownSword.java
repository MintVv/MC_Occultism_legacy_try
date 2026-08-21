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

import com.occultism.occultism.item.ItemFamiliarRing;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityParrot;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IThrowableEntity;

public class EntityThrownSword extends EntityThrowable implements IThrowableEntity {

    private static final int MAX_DURATION = 100;

    private int duration;

    public EntityThrownSword(World worldIn) {
        super(worldIn);
        this.setSize(0.5F, 0.5F);
        this.setNoGravity(true);
    }

    public EntityThrownSword(World worldIn, EntityLivingBase owner) {
        super(worldIn, owner);
        this.setSize(0.5F, 0.5F);
        this.setNoGravity(true);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        this.duration++;
        if (!this.world.isRemote && this.duration > MAX_DURATION) {
            this.setDead();
        }
    }

    @Override
    protected float getGravityVelocity() {
        return 0.0F;
    }

    @Override
    public void setThrower(Entity entity) {
        this.thrower = entity instanceof EntityLivingBase ? (EntityLivingBase) entity : null;
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        if (result.typeOfHit == RayTraceResult.Type.ENTITY && result.entityHit != null
                && this.isFriendly(result.entityHit)) {
            return;
        }
        if (result.typeOfHit == RayTraceResult.Type.ENTITY && result.entityHit != null
                && !this.world.isRemote) {
            result.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), 6.0F);
        }
        this.setDead();
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("duration", this.duration);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.duration = compound.getInteger("duration");
        this.setNoGravity(true);
    }

    private boolean isFriendly(Entity target) {
        EntityLivingBase owner = this.getThrower();
        if (owner == null) {
            return false;
        }
        if (target == owner) {
            return true;
        }
        if (target instanceof IFamiliar && ((IFamiliar) target).getFamiliarOwner() == owner) {
            return true;
        }
        if (target instanceof IBlacksmithUpgradable
                && ((IBlacksmithUpgradable) target).getFamiliarOwner() == owner) {
            return true;
        }
        if (target instanceof EntityDevilFamiliar && ((EntityDevilFamiliar) target).getOwner() == owner) {
            return true;
        }
        if (target instanceof EntityBlacksmithFamiliar && ((EntityBlacksmithFamiliar) target).getOwner() == owner) {
            return true;
        }
        return target instanceof EntityParrot
                && target.getEntityData().getBoolean(ItemFamiliarRing.TAG_RITUAL_FAMILIAR_PARROT)
                && ((EntityParrot) target).getOwner() == owner;
    }
}
