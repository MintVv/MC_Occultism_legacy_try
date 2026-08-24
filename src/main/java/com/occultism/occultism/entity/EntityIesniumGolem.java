package com.occultism.occultism.entity;

import javax.annotation.Nullable;

import com.occultism.occultism.item.ItemSoulGem;
import com.occultism.occultism.registry.ModItems;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

/**
 * The 1.12 counterpart of the upstream Iesnium Golem. It keeps the vanilla
 * iron-golem AI and shape, but can only be dismissed by a sneaking player
 * making a direct melee hit.
 */
public class EntityIesniumGolem extends EntityIronGolem {

    public static final double MOVEMENT_SPEED = 0.5D;
    public static final double ATTACK_DAMAGE = 100.0D;
    public static final double FOLLOW_RANGE = 64.0D;
    private static final int MAX_VOID_RETURN_RISE = 256;

    public EntityIesniumGolem(World worldIn) {
        super(worldIn);
        this.enablePersistence();
        this.setPlayerCreated(true);
        this.experienceValue = 0;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(1.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(MOVEMENT_SPEED);
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(FOLLOW_RANGE);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(ATTACK_DAMAGE);
    }

    @Override
    public boolean canAttackClass(Class<? extends EntityLivingBase> cls) {
        return !EntityPlayer.class.isAssignableFrom(cls) && super.canAttackClass(cls);
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        this.world.setEntityState(this, (byte) 4);
        boolean attacked = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this),
                (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());
        if (attacked && entityIn instanceof EntityLivingBase) {
            entityIn.motionY += 0.4D;
            this.applyEnchantments(this, entityIn);
        }
        this.playSound(SoundEvents.ENTITY_IRONGOLEM_ATTACK, 1.0F, 1.0F);
        return attacked;
    }

    @Override
    public boolean isEntityInvulnerable(DamageSource source) {
        if (source == null) {
            return true;
        }
        return !isValidDisbandAttack(source) || super.isEntityInvulnerable(source);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (!isValidDisbandAttack(source)) {
            return false;
        }
        EntityPlayer player = (EntityPlayer) source.getTrueSource();
        if (this.world.isRemote) {
            return true;
        }
        return this.disband(player);
    }

    /** Returns true only for a direct melee damage source from a sneaking player. */
    public static boolean isValidDisbandAttack(@Nullable DamageSource source) {
        if (!(source instanceof EntityDamageSource)
                || source instanceof net.minecraft.util.EntityDamageSourceIndirect) {
            return false;
        }
        Entity attacker = source.getTrueSource();
        return "player".equals(source.getDamageType()) && source.getImmediateSource() == attacker
                && attacker instanceof EntityPlayer && ((EntityPlayer) attacker).isSneaking();
    }

    /**
     * Serializes a healthy copy of this golem into a filled fragile soul gem,
     * then removes the golem without invoking vanilla iron-golem loot.
     */
    public boolean disband(EntityPlayer player) {
        if (player == null || this.isDead || this.world.isRemote) {
            return false;
        }

        ItemStack gem = new ItemStack(ModItems.FRAGILE_SOUL_GEM);
        if (!ItemSoulGem.storeEntity(gem, this)) {
            return false;
        }

        this.setDead();
        if (!player.addItemStackToInventory(gem)) {
            net.minecraft.entity.item.EntityItem dropped = new net.minecraft.entity.item.EntityItem(this.world, this.posX,
                    this.posY + 0.5D, this.posZ, gem);
            dropped.setPickupDelay(5);
            this.world.spawnEntity(dropped);
        }
        player.swingArm(EnumHand.MAIN_HAND);
        this.world.playSound(null, this.getPosition(), SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.HOSTILE, 1.0F,
                1.0F);
        player.sendStatusMessage(new TextComponentTranslation("entity.occultism.iesnium_golem.disbanded"), true);
        return true;
    }

    /** Keep the immortal guardian from being deleted by 1.12's void callback. */
    @Override
    protected void outOfWorld() {
        if (this.world == null || this.world.isRemote) {
            return;
        }

        BlockPos spawn = this.world.getTopSolidOrLiquidBlock(this.world.getSpawnPoint());
        this.setPositionAndUpdate(spawn.getX() + 0.5D, spawn.getY(), spawn.getZ() + 0.5D);
        this.motionX = 0.0D;
        this.motionY = 0.0D;
        this.motionZ = 0.0D;
        this.fallDistance = 0.0F;

        // A tall golem can overlap the spawn terrain; rise only as far as a bounded search permits.
        int rises = 0;
        while (!this.world.getCollisionBoxes(this, this.getEntityBoundingBox()).isEmpty()
                && rises++ < MAX_VOID_RETURN_RISE) {
            this.setPositionAndUpdate(this.posX, this.posY + 1.0D, this.posZ);
        }
    }

    @Override
    protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
        // Dismissal creates the only intended item.
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return null;
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.enablePersistence();
        this.setPlayerCreated(true);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("PlayerCreated", true);
        compound.setBoolean("PersistenceRequired", true);
    }
}
