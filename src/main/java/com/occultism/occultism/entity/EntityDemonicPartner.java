package com.occultism.occultism.entity;

import java.util.List;

import javax.annotation.Nullable;

import com.occultism.occultism.registry.ModItems;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIFollowOwner;
import net.minecraft.entity.ai.EntityAIOwnerHurtByTarget;
import net.minecraft.entity.ai.EntityAIOwnerHurtTarget;
import net.minecraft.entity.ai.EntityAISit;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.items.ItemHandlerHelper;

public abstract class EntityDemonicPartner extends EntityTameable {

    private static final DataParameter<Boolean> LYING =
            EntityDataManager.createKey(EntityDemonicPartner.class, DataSerializers.BOOLEAN);
    private static final int HEART_INTERVAL = 20 * 60 * 10;
    private static final String TAG_HEART_LAST_TIME = "OccultismDemonicPartnerHeartLastTime";

    private long heartLastTime;

    protected EntityDemonicPartner(World worldIn) {
        super(worldIn);
        this.setSize(this.getPartnerWidth(), this.getPartnerHeight());
        this.isImmuneToFire = true;
        this.enablePersistence();
        this.experienceValue = 3;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(LYING, Boolean.FALSE);
    }

    @Override
    protected void initEntityAI() {
        this.aiSit = new EntityAISit(this);
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, this.aiSit);
        this.tasks.addTask(2, new EntityAIAttackMelee(this, 1.7D, true));
        this.tasks.addTask(3, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(4, new EntityAIFollowOwner(this, 1.0D, 10.0F, 5.0F));
        this.tasks.addTask(7, new EntityAIWanderAvoidWater(this, 1.0D));
        this.targetTasks.addTask(1, new EntityAIOwnerHurtByTarget(this));
        this.targetTasks.addTask(2, new EntityAIOwnerHurtTarget(this));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(15.0D);
        if (this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) == null) {
            this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(9.0D);
        } else {
            this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(9.0D);
        }
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(30.0D);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (!this.world.isRemote && this.ticksExisted % 12 == 0 && this.isLying()) {
            this.setLying(false);
        }
        if (this.world.isRemote && this.swingProgress > 0.0F) {
            this.spawnClientFlames();
        }
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (this.world.isRemote) {
            return this.isOwner(player) || this.isTamed() || stack.getItem() == Items.DIAMOND;
        }

        if (this.isTamed() && this.isOwner(player)) {
            if (this.tryGiveHoneyHeart(player, stack)) {
                return true;
            }
            if (this.tryApplyPotion(player, stack)) {
                return true;
            }
            if (this.tryCookFood(player, stack)) {
                return true;
            }
            if (this.tryHealWithFood(player, stack)) {
                return true;
            }
            if (hand == EnumHand.MAIN_HAND && stack.isEmpty()) {
                this.setSitting(!this.isSitting());
                if (this.aiSit != null) {
                    this.aiSit.setSitting(this.isSitting());
                }
                this.setAttackTarget(null);
                return true;
            }
        } else if (stack.getItem() == Items.DIAMOND) {
            if (!player.capabilities.isCreativeMode) {
                stack.shrink(1);
            }
            this.setTamed(true);
            this.setOwnerId(player.getUniqueID());
            this.setSitting(true);
            if (this.aiSit != null) {
                this.aiSit.setSitting(true);
            }
            this.navigator.clearPath();
            this.setAttackTarget(null);
            this.world.setEntityState(this, (byte) 7);
            return true;
        }

        return super.processInteract(player, hand);
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        boolean hit = super.attackEntityAsMob(entityIn);
        if (hit) {
            entityIn.setFire(2);
            this.heal(1.0F);
            this.spawnServerFlames();
        }
        return hit;
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
    public boolean isEntityInvulnerable(DamageSource source) {
        return super.isEntityInvulnerable(source) || source == DamageSource.IN_WALL
                || source == DamageSource.FLY_INTO_WALL;
    }

    @Override
    protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
        this.dropSoulShard();
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
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.heartLastTime = compound.getLong(TAG_HEART_LAST_TIME);
        this.setLying(compound.getBoolean("OccultismDemonicPartnerLying"));
        this.isImmuneToFire = true;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setLong(TAG_HEART_LAST_TIME, this.heartLastTime);
        compound.setBoolean("OccultismDemonicPartnerLying", this.isLying());
    }

    public boolean isLying() {
        return this.dataManager.get(LYING).booleanValue();
    }

    public void setLying(boolean lying) {
        this.dataManager.set(LYING, Boolean.valueOf(lying));
    }

    public abstract float getPartnerWidth();

    public abstract float getPartnerHeight();

    private boolean tryGiveHoneyHeart(EntityPlayer player, ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() != ModItems.CURSED_HONEY) {
            return false;
        }
        long remaining = this.heartLastTime + HEART_INTERVAL - this.world.getTotalWorldTime();
        if (remaining > 0) {
            player.sendStatusMessage(new TextComponentTranslation("dialog.occultism.partner.heart_on_cooldown",
                    Long.valueOf(remaining)), true);
            return true;
        }

        this.heartLastTime = this.world.getTotalWorldTime();
        if (!player.capabilities.isCreativeMode) {
            stack.shrink(1);
        }
        this.giveToPlayerOrDrop(player, new ItemStack(ModItems.SWEET_HONEY_HEART));
        this.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, this.getSoundVolume(), 1.6F);
        return true;
    }

    private boolean tryApplyPotion(EntityPlayer player, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        List<PotionEffect> effects = PotionUtils.getEffectsFromStack(stack);
        if (effects.isEmpty()) {
            return false;
        }

        for (PotionEffect effect : effects) {
            Potion potion = effect.getPotion();
            if (potion.isInstant()) {
                potion.affectEntity(this, this, player, effect.getAmplifier() + 2, 1.0D);
            } else {
                player.addPotionEffect(new PotionEffect(potion, effect.getDuration() * 5,
                        effect.getAmplifier(), effect.getIsAmbient(), effect.doesShowParticles()));
            }
        }

        if (!player.capabilities.isCreativeMode) {
            stack.shrink(1);
            this.giveToPlayerOrDrop(player, new ItemStack(Items.GLASS_BOTTLE));
        }
        this.spawnServerFlames();
        return true;
    }

    private boolean tryCookFood(EntityPlayer player, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        ItemStack result = FurnaceRecipes.instance().getSmeltingResult(stack);
        if (result.isEmpty()) {
            return false;
        }

        ItemStack output = result.copy();
        int count = player.isSneaking() ? stack.getCount() : 1;
        output.setCount(output.getCount() * count);
        if (!player.capabilities.isCreativeMode) {
            stack.shrink(count);
        }
        this.giveToPlayerOrDrop(player, output);
        this.spawnServerFlames();
        return true;
    }

    private boolean tryHealWithFood(EntityPlayer player, ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemFood) || this.getHealth() >= this.getMaxHealth()) {
            return false;
        }

        this.heal(((ItemFood) stack.getItem()).getHealAmount(stack));
        if (!player.capabilities.isCreativeMode) {
            stack.shrink(1);
        }
        this.playSound(SoundEvents.ENTITY_GENERIC_EAT, this.getSoundVolume(), 1.0F);
        return true;
    }

    private void giveToPlayerOrDrop(EntityPlayer player, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        if (!player.inventory.addItemStackToInventory(stack.copy())) {
            this.entityDropItem(stack.copy(), 0.2F);
        }
        player.inventoryContainer.detectAndSendChanges();
    }

    private void dropSoulShard() {
        ItemStack shard = new ItemStack(ModItems.SOUL_SHARD);
        if (!com.occultism.occultism.item.ItemSoulShard.storeEntity(shard, this)) {
            return;
        }

        EntityLivingBase owner = this.getOwner();
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

    private void spawnClientFlames() {
        double lookX = -Math.sin(this.rotationYaw * 0.017453292F) * 0.6D;
        double lookZ = Math.cos(this.rotationYaw * 0.017453292F) * 0.6D;
        for (int i = 0; i < 2; i++) {
            this.world.spawnParticle(EnumParticleTypes.FLAME, this.posX + lookX + (this.rand.nextDouble() - 0.5D) * 0.7D,
                    this.posY + 1.2D + (this.rand.nextDouble() - 0.5D) * 0.7D,
                    this.posZ + lookZ + (this.rand.nextDouble() - 0.5D) * 0.7D,
                    lookX * 0.1D, 0.0D, lookZ * 0.1D);
        }
    }

    private void spawnServerFlames() {
        if (this.world instanceof WorldServer) {
            ((WorldServer) this.world).spawnParticle(EnumParticleTypes.FLAME, this.posX, this.posY + 1.2D,
                    this.posZ, 5, 0.35D, 0.35D, 0.35D, 0.02D);
        }
    }
}
