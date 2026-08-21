package com.occultism.occultism.entity;

import javax.annotation.Nullable;

import com.occultism.occultism.Tags;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

public class EntityPossessedEnderman extends EntityEnderman {

    private static final ResourceLocation LOOT_TABLE = LootTableList.register(
            new ResourceLocation(Tags.MOD_ID, "entities/possessed_enderman"));

    private final PeacefulRemovalGuard peacefulRemovalGuard = new PeacefulRemovalGuard();

    public EntityPossessedEnderman(World worldIn) {
        super(worldIn);
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
