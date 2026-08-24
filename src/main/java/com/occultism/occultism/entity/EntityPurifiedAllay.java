package com.occultism.occultism.entity;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityVex;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

public class EntityPurifiedAllay extends EntityVex implements IFamiliar {

    private static final double FOLLOW_RANGE = 10.0D;
    private static final double PICKUP_RANGE_SQ = 1.4D;
    private static final double RETURN_RANGE_SQ = 3.0D;
    private int pickupCooldown;
    @Nullable
    private UUID familiarOwnerId;

    public EntityPurifiedAllay(World worldIn) {
        super(worldIn);
        this.enablePersistence();
        this.experienceValue = 2;
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(16.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(0.0D);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.setAttackTarget(null);
        this.setCharging(false);
        if (this.pickupCooldown > 0) {
            this.pickupCooldown--;
        }
        if (!this.world.isRemote) {
            this.updatePurifiedAllayBehavior();
        }
    }

    @Override
    protected boolean processInteract(EntityPlayer player, EnumHand hand) {
        if (this.familiarOwnerId != null && this.getFamiliarOwner() != player) {
            return false;
        }
        ItemStack held = this.getHeldItemMainhand();
        if (held.isEmpty()) {
            return false;
        }
        ItemStack remaining = held.copy();
        player.inventory.addItemStackToInventory(remaining);
        this.setHeldItem(EnumHand.MAIN_HAND, remaining.isEmpty() ? ItemStack.EMPTY : remaining);
        this.setDropChance(EntityEquipmentSlot.MAINHAND, 0.0F);
        this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ITEM_PICKUP,
                SoundCategory.NEUTRAL, 0.4F, 1.6F);
        return true;
    }

    @Override
    public boolean attackEntityAsMob(net.minecraft.entity.Entity entityIn) {
        return false;
    }

    @Override
    protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty) {
        this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStack.EMPTY);
        this.setDropChance(EntityEquipmentSlot.MAINHAND, 0.0F);
    }

    @Override
    protected void dropEquipment(boolean wasRecentlyHit, int lootingModifier) {
        ItemStack held = this.getHeldItemMainhand();
        if (!held.isEmpty()) {
            this.entityDropItem(held.copy(), 0.0F);
            this.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
        }
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return null;
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.pickupCooldown = compound.getInteger("OccultismPickupCooldown");
        if (compound.hasKey("OccultismFamiliarOwner", 8)) {
            try {
                this.familiarOwnerId = UUID.fromString(compound.getString("OccultismFamiliarOwner"));
            } catch (IllegalArgumentException ignored) {
                this.familiarOwnerId = null;
            }
        } else {
            this.familiarOwnerId = null;
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("OccultismPickupCooldown", this.pickupCooldown);
        if (this.familiarOwnerId != null) {
            compound.setString("OccultismFamiliarOwner", this.familiarOwnerId.toString());
        }
    }

    @Override
    @Nullable
    public EntityLivingBase getFamiliarOwner() {
        if (this.familiarOwnerId == null || this.world == null) {
            return null;
        }
        return this.world.getPlayerEntityByUUID(this.familiarOwnerId);
    }

    @Override
    public void setFamiliarOwner(EntityLivingBase owner) {
        if (owner != null) {
            this.familiarOwnerId = owner.getUniqueID();
        }
    }

    @Override
    public EntityLivingBase getFamiliarEntity() {
        return this;
    }

    private void updatePurifiedAllayBehavior() {
        EntityPlayer followTarget = this.getFollowTarget();
        ItemStack held = this.getHeldItemMainhand();
        if (!held.isEmpty()) {
            this.returnHeldItemToPlayer(followTarget);
            return;
        }
        if (this.pickupCooldown > 0) {
            this.followPlayer(followTarget);
            return;
        }
        EntityItem target = this.findNearestPickup();
        if (target != null) {
            this.moveToward(target.posX, target.posY + 0.25D, target.posZ, 0.22D);
            this.getLookHelper().setLookPositionWithEntity(target, 20.0F, 20.0F);
            if (this.getDistanceSq(target) <= PICKUP_RANGE_SQ) {
                this.pickupOne(target);
            }
            return;
        }
        this.followPlayer(followTarget);
    }

    @Nullable
    private EntityPlayer getFollowTarget() {
        if (this.familiarOwnerId != null) {
            EntityLivingBase owner = this.getFamiliarOwner();
            return owner instanceof EntityPlayer && owner.isEntityAlive() ? (EntityPlayer) owner : null;
        }
        return this.world.getNearestPlayerNotCreative(this, FOLLOW_RANGE);
    }

    private void returnHeldItemToPlayer(@Nullable EntityPlayer player) {
        if (player == null || !player.isEntityAlive()) {
            return;
        }
        this.moveToward(player.posX, player.posY + 1.0D, player.posZ, 0.2D);
        this.getLookHelper().setLookPositionWithEntity(player, 20.0F, 20.0F);
        if (this.getDistanceSq(player) <= RETURN_RANGE_SQ) {
            ItemStack held = this.getHeldItemMainhand();
            ItemStack remaining = held.copy();
            if (player.inventory.addItemStackToInventory(remaining)) {
                this.setHeldItem(EnumHand.MAIN_HAND, remaining.isEmpty() ? ItemStack.EMPTY : remaining);
                this.pickupCooldown = 40;
                this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ITEM_PICKUP,
                        SoundCategory.NEUTRAL, 0.4F, 1.6F);
            }
        }
    }

    private void followPlayer(@Nullable EntityPlayer player) {
        if (player != null && player.isEntityAlive() && this.getDistanceSq(player) > 9.0D) {
            this.moveToward(player.posX, player.posY + 1.0D, player.posZ, 0.16D);
            return;
        }
        if (!this.getMoveHelper().isUpdating() && this.rand.nextInt(20) == 0) {
            double x = this.posX + this.rand.nextInt(7) - 3;
            double y = this.posY + this.rand.nextInt(5) - 2;
            double z = this.posZ + this.rand.nextInt(7) - 3;
            this.moveToward(x, y, z, 0.08D);
        }
    }

    @Nullable
    private EntityItem findNearestPickup() {
        AxisAlignedBB bounds = this.getEntityBoundingBox().grow(6.0D, 3.0D, 6.0D);
        List<EntityItem> items = this.world.getEntitiesWithinAABB(EntityItem.class, bounds);
        EntityItem best = null;
        double bestDistance = Double.MAX_VALUE;
        for (EntityItem item : items) {
            if (item.isDead || item.getItem().isEmpty() || item.cannotPickup()) {
                continue;
            }
            double distance = this.getDistanceSq(item);
            if (distance < bestDistance) {
                best = item;
                bestDistance = distance;
            }
        }
        return best;
    }

    private void pickupOne(EntityItem entityItem) {
        ItemStack stack = entityItem.getItem();
        ItemStack picked = stack.copy();
        picked.setCount(1);
        stack.shrink(1);
        if (stack.isEmpty()) {
            entityItem.setDead();
        } else {
            entityItem.setItem(stack);
        }
        this.setHeldItem(EnumHand.MAIN_HAND, picked);
        this.setDropChance(EntityEquipmentSlot.MAINHAND, 0.0F);
        this.pickupCooldown = 20;
        this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ITEM_PICKUP,
                SoundCategory.NEUTRAL, 0.4F, 1.4F);
    }

    private void moveToward(double x, double y, double z, double speed) {
        double dx = x - this.posX;
        double dy = y - this.posY;
        double dz = z - this.posZ;
        double distance = MathHelper.sqrt(dx * dx + dy * dy + dz * dz);
        if (distance < 0.0001D) {
            return;
        }
        this.motionX += dx / distance * speed;
        this.motionY += dy / distance * speed;
        this.motionZ += dz / distance * speed;
        this.rotationYaw = (float) (MathHelper.atan2(this.motionZ, this.motionX) * (180D / Math.PI)) - 90.0F;
        this.renderYawOffset = this.rotationYaw;
    }
}
