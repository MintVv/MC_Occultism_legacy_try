package com.occultism.occultism.entity;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import com.occultism.occultism.Tags;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootTableList;

public class EntityWildHuntSkeleton extends EntitySkeleton implements IWildHuntEntity {

    private static final ResourceLocation LOOT_TABLE = LootTableList.register(
            new ResourceLocation(Tags.MOD_ID, "entities/wild_hunt_skeleton"));

    private Optional<EntityWildHuntWitherSkeleton> master = Optional.empty();
    private UUID masterId;
    private boolean trackingPeacefulRemoval;
    private int trackedSetDeadCalls;

    public EntityWildHuntSkeleton(World worldIn) {
        super(worldIn);
        this.setSize(0.6F, 2.9F);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0D);
    }

    public void setMaster(@Nullable EntityWildHuntWitherSkeleton master) {
        this.master = Optional.ofNullable(master);
        this.masterId = master == null ? null : master.getUniqueID();
    }

    private void resolveMaster() {
        if (this.world == null || this.world.isRemote || this.master.isPresent() || this.masterId == null
                || !(this.world instanceof WorldServer)) {
            return;
        }
        net.minecraft.entity.Entity entity = ((WorldServer) this.world).getEntityFromUuid(this.masterId);
        if (entity instanceof EntityWildHuntWitherSkeleton) {
            this.setMaster((EntityWildHuntWitherSkeleton) entity);
        }
    }

    @Override
    protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty) {
        super.setEquipmentBasedOnDifficulty(difficulty);
        if (this.rand.nextFloat() <= 0.7F) {
            this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
        }
    }

    @Override
    public void onLivingUpdate() {
        ItemStack originalHelmet = this.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        ItemStack sunlightGuard = ItemStack.EMPTY;
        boolean suppressSunlight = !this.world.isRemote && this.world.isDaytime();
        if (suppressSunlight) {
            sunlightGuard = new ItemStack(Items.PAPER);
            this.setItemStackToSlot(EntityEquipmentSlot.HEAD, sunlightGuard);
        }

        try {
            super.onLivingUpdate();
        } finally {
            if (suppressSunlight && this.getItemStackFromSlot(EntityEquipmentSlot.HEAD) == sunlightGuard) {
                this.setItemStackToSlot(EntityEquipmentSlot.HEAD, originalHelmet);
            }
        }
    }

    @Override
    public void onUpdate() {
        if (this.ticksExisted % 20 == 0) {
            this.resolveMaster();
        }
        boolean protectFromPeaceful = !this.world.isRemote && this.world.getDifficulty() == EnumDifficulty.PEACEFUL
                && !this.isDead && this.getHealth() > 0.0F;
        if (protectFromPeaceful) {
            this.trackingPeacefulRemoval = true;
            this.trackedSetDeadCalls = 0;
        }

        try {
            super.onUpdate();
        } finally {
            this.trackingPeacefulRemoval = false;
            // EntityMob performs the peaceful removal after all other update-time removals.
            if (protectFromPeaceful && this.trackedSetDeadCalls == 1 && this.getHealth() > 0.0F) {
                this.isDead = false;
            }
        }
    }

    @Override
    public void setDead() {
        if (this.trackingPeacefulRemoval) {
            this.trackedSetDeadCalls++;
        }
        super.setDead();
    }

    @Override
    public void onRemovedFromWorld() {
        if (!this.world.isRemote) {
            this.master.ifPresent(boss -> boss.notifyMinionDeath(this));
        }
        super.onRemovedFromWorld();
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        if (this.masterId != null) {
            compound.setUniqueId("WildHuntMaster", this.masterId);
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.master = Optional.empty();
        this.masterId = compound.hasUniqueId("WildHuntMaster") ? compound.getUniqueId("WildHuntMaster") : null;
    }

    @Override
    public boolean isEntityInvulnerable(DamageSource source) {
        if (IWildHuntEntity.isWildHunt(source.getTrueSource())
                || IWildHuntEntity.isWildHunt(source.getImmediateSource())) {
            return true;
        }
        return super.isEntityInvulnerable(source);
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return LOOT_TABLE;
    }
}
