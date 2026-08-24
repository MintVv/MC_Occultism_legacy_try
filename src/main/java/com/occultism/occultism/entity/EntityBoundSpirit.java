package com.occultism.occultism.entity;

import javax.annotation.Nullable;

import com.occultism.occultism.item.ItemSoulShard;
import com.occultism.occultism.Occultism;
import com.occultism.occultism.gui.ModGuiHandler;
import com.occultism.occultism.registry.ModItems;
import com.occultism.occultism.spirit.SpiritTier;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIFollowOwner;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class EntityBoundSpirit extends EntityTameable {

    private static final DataParameter<Integer> TIER =
            EntityDataManager.createKey(EntityBoundSpirit.class, DataSerializers.VARINT);
    private static final DataParameter<NBTTagCompound> INVENTORY =
            EntityDataManager.createKey(EntityBoundSpirit.class, DataSerializers.COMPOUND_TAG);

    private final ItemStackHandler spiritInventory = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            EntityBoundSpirit.this.syncSpiritInventory();
        }
    };
    private boolean loadingSpiritInventory;
    private boolean droppedSoulShard;

    public EntityBoundSpirit(World worldIn) {
        super(worldIn);
        this.setSize(0.7F, 1.6F);
        this.setCanPickUpLoot(false);
        this.enablePersistence();
    }

    public EntityBoundSpirit(World worldIn, SpiritTier tier) {
        this(worldIn);
        this.setTier(tier);
        this.setHealth(this.getMaxHealth());
    }

    /** Creates the dedicated high-tier entity while preserving the legacy generic fallback. */
    public static EntityBoundSpirit createForTier(World worldIn, SpiritTier tier) {
        if (tier == SpiritTier.DJINNI) {
            return new EntityDjinni(worldIn);
        }
        if (tier == SpiritTier.AFRIT) {
            return new EntityAfrit(worldIn);
        }
        if (tier == SpiritTier.MARID) {
            return new EntityMarid(worldIn);
        }
        return new EntityBoundSpirit(worldIn, tier);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(TIER, Integer.valueOf(SpiritTier.DJINNI.ordinal()));
        this.dataManager.register(INVENTORY, new NBTTagCompound());
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(4, new EntityAIFollowOwner(this, 1.0D, 10.0F, 3.0F));
        this.tasks.addTask(5, new EntityAIWander(this, 0.8D));
        this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(7, new EntityAILookIdle(this));
    }

    @Override
    @Nullable
    public EntityAgeable createChild(EntityAgeable ageable) {
        return null;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        if (this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) == null) {
            this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        }
        if (this.getEntityAttribute(SharedMonsterAttributes.ARMOR) == null) {
            this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ARMOR);
        }
        if (this.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS) == null) {
            this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS);
        }
        this.applyTierAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(20.0D);
    }

    @Override
    protected boolean canDespawn() {
        return false;
    }

    @Override
    public boolean isEntityInvulnerable(DamageSource source) {
        return super.isEntityInvulnerable(source) || source == DamageSource.IN_WALL
                || source == DamageSource.FLY_INTO_WALL;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.isEntityInvulnerable(source)) {
            return false;
        }
        Entity trueSource = source == null ? null : source.getTrueSource();
        Entity immediateSource = source == null ? null : source.getImmediateSource();
        if (trueSource != null && !(trueSource instanceof EntityPlayer)
                && !(trueSource instanceof EntityArrow) && !(immediateSource instanceof EntityArrow)) {
            amount = (amount + 1.0F) / 2.0F;
        }
        return super.attackEntityFrom(source, amount);
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        if (player != null && player.isSneaking() && player.getHeldItem(hand).isEmpty() && this.isEntityAlive()
                && this.canPlayerAccess(player)) {
            if (!this.world.isRemote) {
                player.openGui(Occultism.INSTANCE, ModGuiHandler.BOUND_SPIRIT, this.world, this.getEntityId(), 0, 0);
            }
            return true;
        }
        return super.processInteract(player, hand);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (this.world == null || !this.world.isRemote || this.ticksExisted % 12 != 0) {
            return;
        }
        this.world.spawnParticle(net.minecraft.util.EnumParticleTypes.SPELL_WITCH,
                this.posX + (this.rand.nextDouble() - 0.5D) * 0.45D,
                this.posY + 0.9D + this.rand.nextDouble() * 0.5D,
                this.posZ + (this.rand.nextDouble() - 0.5D) * 0.45D,
                0.0D, 0.01D, 0.0D);
    }

    @Override
    public void onDeath(DamageSource cause) {
        super.onDeath(cause);
        if (!this.world.isRemote) {
            this.dropSpiritInventory();
            if (!this.droppedSoulShard) {
                this.dropSoulShard();
            }
        }
    }

    public ItemStackHandler getSpiritInventory() {
        return this.spiritInventory;
    }

    /** Binds this spirit to a player while preserving the vanilla owner UUID/NBT contract. */
    public void setSpiritOwner(@Nullable EntityLivingBase owner) {
        if (owner == null) {
            return;
        }
        this.setTamed(true);
        this.setOwnerId(owner.getUniqueID());
    }

    public boolean canPlayerAccess(@Nullable EntityPlayer player) {
        return player != null && (!this.isTamed() || this.isOwner(player));
    }

    @Override
    public boolean hasCapability(Capability<?> capability, net.minecraft.util.EnumFacing facing) {
        return this.isEntityAlive() && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
                || super.hasCapability(capability, facing);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(Capability<T> capability, net.minecraft.util.EnumFacing facing) {
        if (this.isEntityAlive() && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return (T) this.spiritInventory;
        }
        return super.getCapability(capability, facing);
    }

    public SpiritTier getTier() {
        int index = this.dataManager.get(TIER).intValue();
        SpiritTier[] values = SpiritTier.values();
        if (index < 0 || index >= values.length) {
            return SpiritTier.DJINNI;
        }
        return values[index];
    }

    public void setTier(SpiritTier tier) {
        this.dataManager.set(TIER, Integer.valueOf((tier == null ? SpiritTier.DJINNI : tier).ordinal()));
        this.applyTierAttributes();
    }

    protected void applyTierAttributes() {
        if (this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH) == null) {
            return;
        }
        SpiritTier tier = this.getTier();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(tier.getMaxHealth());
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(tier.getMovementSpeed());
        if (this.getHealth() > this.getMaxHealth()) {
            this.setHealth(this.getMaxHealth());
        }
    }

    private void dropSoulShard() {
        this.droppedSoulShard = true;
        ItemStack shard = new ItemStack(ModItems.SOUL_SHARD);
        if (!ItemSoulShard.storeEntity(shard, this)) {
            return;
        }
        EntityItem shardEntity = new EntityItem(this.world, this.posX, this.posY + 0.5D, this.posZ, shard);
        shardEntity.setPickupDelay(5);
        shardEntity.motionX = 0.0D;
        shardEntity.motionZ = 0.0D;
        this.spawnEntityItem(shardEntity);
    }

    private void dropSpiritInventory() {
        for (int slot = 0; slot < this.spiritInventory.getSlots(); slot++) {
            ItemStack stack = this.spiritInventory.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                EntityItem item = new EntityItem(this.world, this.posX, this.posY + 0.5D, this.posZ, stack.copy());
                item.setPickupDelay(5);
                this.spawnEntityItem(item);
                this.spiritInventory.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
    }

    private void spawnEntityItem(EntityItem item) {
        if (this.captureDrops) {
            this.capturedDrops.add(item);
        } else {
            this.world.spawnEntity(item);
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("SpiritTier", this.getTier().ordinal());
        compound.setTag("SpiritInventory", this.spiritInventory.serializeNBT());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        int tier = compound.getInteger("SpiritTier");
        SpiritTier[] values = SpiritTier.values();
        this.setTier(tier >= 0 && tier < values.length ? values[tier] : SpiritTier.DJINNI);
        if (compound.hasKey("SpiritInventory", 10)) {
            NBTTagCompound inventory = compound.getCompoundTag("SpiritInventory");
            inventory.setInteger("Size", this.spiritInventory.getSlots());
            this.loadingSpiritInventory = true;
            try {
                this.spiritInventory.deserializeNBT(inventory);
            } finally {
                this.loadingSpiritInventory = false;
            }
        }
        this.syncSpiritInventory();
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> key) {
        super.notifyDataManagerChange(key);
        if (INVENTORY.equals(key) && this.world != null && this.world.isRemote) {
            NBTTagCompound inventory = this.dataManager.get(INVENTORY);
            if (inventory != null && !inventory.isEmpty()) {
                this.loadingSpiritInventory = true;
                try {
                    this.spiritInventory.deserializeNBT(inventory);
                } finally {
                    this.loadingSpiritInventory = false;
                }
            }
        }
    }

    private void syncSpiritInventory() {
        if (this.loadingSpiritInventory || this.dataManager == null
                || this.world != null && this.world.isRemote) {
            return;
        }
        this.dataManager.set(INVENTORY, this.spiritInventory.serializeNBT());
    }
}
