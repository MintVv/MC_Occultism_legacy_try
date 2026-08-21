package com.occultism.occultism.entity;

import javax.annotation.Nullable;

import com.occultism.occultism.Tags;
import net.minecraft.entity.monster.EntityEndermite;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

public class EntityPossessedEndermite extends EntityEndermite {

    private static final ResourceLocation LOOT_TABLE = LootTableList.register(
            new ResourceLocation(Tags.MOD_ID, "entities/possessed_endermite"));

    private final PeacefulRemovalGuard peacefulRemovalGuard = new PeacefulRemovalGuard();

    public EntityPossessedEndermite(World worldIn) {
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
