package com.occultism.occultism.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import com.occultism.occultism.Tags;
import com.occultism.occultism.util.ItemNBTUtil;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityWitherSkeleton;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.event.ForgeEventFactory;

public class EntityWildHuntWitherSkeleton extends EntityWitherSkeleton implements IWildHuntEntity {

    private static final ResourceLocation LOOT_TABLE = LootTableList.register(
            new ResourceLocation(Tags.MOD_ID, "entities/wild_hunt_wither_skeleton"));

    private final List<EntityWildHuntSkeleton> minions = new ArrayList<>();
    private final Set<UUID> minionIds = new HashSet<>();
    private boolean trackingPeacefulRemoval;
    private int trackedSetDeadCalls;

    public EntityWildHuntWitherSkeleton(World worldIn) {
        super(worldIn);
        this.setSize(0.6F, 2.9F);
        this.isImmuneToFire = false;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(60.0D);
    }

    @Override
    @Nullable
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        if (!this.world.isRemote) {
            int skeletonCount = 3 + this.world.rand.nextInt(6);
            for (int i = 0; i < skeletonCount; i++) {
                EntityWildHuntSkeleton minion = new EntityWildHuntSkeleton(this.world);
                if (!ForgeEventFactory.doSpecialSpawn(minion, this.world, (float) this.posX, (float) this.posY,
                        (float) this.posZ)) {
                    minion.onInitialSpawn(difficulty, livingdata);
                }

                double offsetX = this.world.rand.nextGaussian() * (1 + this.world.rand.nextInt(4));
                double offsetZ = this.world.rand.nextGaussian() * (1 + this.world.rand.nextInt(4));
                minion.setLocationAndAngles(MathHelper.floor(this.posX) + offsetX, MathHelper.floor(this.posY) + 1.5D,
                        MathHelper.floor(this.posZ) + offsetZ, this.world.rand.nextInt(360), 0.0F);
                minion.setCustomNameTag(ItemNBTUtil.generateName());
                minion.enablePersistence();
                if (this.world.spawnEntity(minion)) {
                    minion.setMaster(this);
                    this.minions.add(minion);
                    this.minionIds.add(minion.getUniqueID());
                }
            }
        }

        IEntityLivingData result = super.onInitialSpawn(difficulty, livingdata);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);
        return result;
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
            this.resolveMinions();
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
    public boolean isEntityInvulnerable(DamageSource source) {
        if (IWildHuntEntity.isWildHunt(source.getTrueSource())
                || IWildHuntEntity.isWildHunt(source.getImmediateSource())) {
            return true;
        }
        if (!this.minions.isEmpty() && source != DamageSource.OUT_OF_WORLD && !source.isCreativePlayer()) {
            return true;
        }
        return super.isEntityInvulnerable(source);
    }

    public void notifyMinionDeath(EntityWildHuntSkeleton minion) {
        this.minions.remove(minion);
        this.minionIds.remove(minion.getUniqueID());
    }

    private void resolveMinions() {
        if (this.world == null || this.world.isRemote || !(this.world instanceof WorldServer)
                || this.minionIds.isEmpty()) {
            return;
        }
        for (UUID id : new HashSet<>(this.minionIds)) {
            net.minecraft.entity.Entity entity = ((WorldServer) this.world).getEntityFromUuid(id);
            if (entity instanceof EntityWildHuntSkeleton && !this.minions.contains(entity)) {
                EntityWildHuntSkeleton minion = (EntityWildHuntSkeleton) entity;
                minion.setMaster(this);
                this.minions.add(minion);
            }
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        NBTTagList ids = new NBTTagList();
        for (UUID id : this.minionIds) {
            NBTTagCompound entry = new NBTTagCompound();
            entry.setUniqueId("UUID", id);
            ids.appendTag(entry);
        }
        compound.setTag("WildHuntMinions", ids);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.minions.clear();
        this.minionIds.clear();
        if (!compound.hasKey("WildHuntMinions", 9)) {
            return;
        }
        NBTTagList ids = compound.getTagList("WildHuntMinions", 10);
        for (int index = 0; index < ids.tagCount(); index++) {
            NBTTagCompound entry = ids.getCompoundTagAt(index);
            if (entry.hasUniqueId("UUID")) {
                this.minionIds.add(entry.getUniqueId("UUID"));
            }
        }
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return LOOT_TABLE;
    }
}
