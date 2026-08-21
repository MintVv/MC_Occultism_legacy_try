package com.occultism.occultism.entity;

import javax.annotation.Nullable;

import com.occultism.occultism.Tags;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

public class EntityPossessedWeakShulker extends EntityShulker {

    private static final ResourceLocation LOOT_TABLE = LootTableList.register(
            new ResourceLocation(Tags.MOD_ID, "entities/possessed_weak_shulker"));

    public EntityPossessedWeakShulker(World worldIn) {
        super(worldIn);
        this.isImmuneToFire = true;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return LOOT_TABLE;
    }
}
