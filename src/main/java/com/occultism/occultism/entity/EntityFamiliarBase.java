package com.occultism.occultism.entity;

import javax.annotation.Nullable;

import com.occultism.occultism.item.ItemSoulShard;
import com.occultism.occultism.registry.ModItems;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemHandlerHelper;

public abstract class EntityFamiliarBase extends EntityTameable implements IFamiliar {

    protected EntityFamiliarBase(World worldIn) {
        super(worldIn);
        this.enablePersistence();
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(15.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).setBaseValue(10.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
        IAttributeInstance attack = this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        if (attack == null) {
            attack = this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        }
        attack.setBaseValue(6.0D);
    }

    @Override
    protected boolean canDespawn() {
        return false;
    }

    /** The upstream FamiliarEntity ignores the two movement collision damage sources. */
    @Override
    public boolean isEntityInvulnerable(DamageSource source) {
        return super.isEntityInvulnerable(source) || source == DamageSource.IN_WALL
                || source == DamageSource.FLY_INTO_WALL;
    }

    @Override
    protected void dropLoot(boolean wasRecentlyHit, int lootingModifier, DamageSource source) {
        super.dropLoot(wasRecentlyHit, lootingModifier, source);
        this.dropSoulShard();
    }

    protected boolean storeSoulShard(ItemStack shard) {
        return ItemSoulShard.storeEntity(shard, this);
    }

    private void dropSoulShard() {
        ItemStack shard = new ItemStack(ModItems.SOUL_SHARD);
        if (!this.storeSoulShard(shard)) {
            return;
        }

        EntityLivingBase owner = this.getFamiliarOwner();
        if (owner instanceof EntityPlayer) {
            ItemHandlerHelper.giveItemToPlayer((EntityPlayer) owner, shard);
            return;
        }

        EntityItem item = new EntityItem(this.world, this.posX, this.posY + 0.5D, this.posZ, shard);
        item.setPickupDelay(5);
        item.motionX = 0.0D;
        item.motionZ = 0.0D;
        this.world.spawnEntity(item);
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
}
