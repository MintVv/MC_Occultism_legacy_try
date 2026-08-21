package com.occultism.occultism.entity;

import javax.annotation.Nullable;

import com.occultism.occultism.Tags;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityElderGuardian;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

public class EntityPossessedElderGuardian extends EntityElderGuardian {

    private static final ResourceLocation LOOT_TABLE = LootTableList.register(
            new ResourceLocation(Tags.MOD_ID, "entities/possessed_elder_guardian"));

    private final PeacefulRemovalGuard peacefulRemovalGuard = new PeacefulRemovalGuard();

    public EntityPossessedElderGuardian(World worldIn) {
        super(worldIn);
        this.setSize(2.0F, 2.0F);
        this.isImmuneToFire = true;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(160.0D);
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
