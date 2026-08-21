package com.occultism.occultism.entity;

import javax.annotation.Nullable;

import com.occultism.occultism.registry.ModItems;

import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.entity.IEntityLivingData;

public class EntityMercyGoat extends EntitySheep {

    private boolean droppedCrueltyEssence;

    public EntityMercyGoat(World worldIn) {
        super(worldIn);
        this.setFleeceColor(EnumDyeColor.WHITE);
        this.setSheared(false);
        this.enablePersistence();
    }

    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData livingdata) {
        IEntityLivingData data = super.onInitialSpawn(difficulty, livingdata);
        this.setFleeceColor(EnumDyeColor.WHITE);
        this.setSheared(false);
        return data;
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        return false;
    }

    @Override
    public EntityMercyGoat createChild(EntityAgeable ageable) {
        return new EntityMercyGoat(this.world);
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return null;
    }

    @Override
    protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
    }

    @Override
    public void onDeath(DamageSource cause) {
        super.onDeath(cause);
        if (!this.world.isRemote && !this.droppedCrueltyEssence) {
            this.droppedCrueltyEssence = true;
            this.entityDropItem(new ItemStack(ModItems.CRUELTY_ESSENCE), 0.0F);
        }
    }
}
