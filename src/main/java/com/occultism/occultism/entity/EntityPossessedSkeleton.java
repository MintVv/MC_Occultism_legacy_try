package com.occultism.occultism.entity;

import javax.annotation.Nullable;

import com.occultism.occultism.Tags;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

public class EntityPossessedSkeleton extends EntitySkeleton {

    private static final ResourceLocation LOOT_TABLE = LootTableList.register(
            new ResourceLocation(Tags.MOD_ID, "entities/possessed_skeleton"));

    private final PeacefulRemovalGuard peacefulRemovalGuard = new PeacefulRemovalGuard();

    public EntityPossessedSkeleton(World worldIn) {
        super(worldIn);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30.0D);
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
        boolean protect = this.peacefulRemovalGuard.begin(this);
        try {
            super.onUpdate();
        } finally {
            this.peacefulRemovalGuard.finish(this, protect);
        }
    }

    @Override
    public void setDead() {
        this.peacefulRemovalGuard.recordSetDead();
        super.setDead();
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return LOOT_TABLE;
    }
}
