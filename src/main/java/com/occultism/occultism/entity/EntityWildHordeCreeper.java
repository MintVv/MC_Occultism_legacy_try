package com.occultism.occultism.entity;

import javax.annotation.Nullable;

import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

/** Charged Creeper entity used by the wild creeper horde ritual. */
public class EntityWildHordeCreeper extends EntityCreeper {

    public EntityWildHordeCreeper(World worldIn) {
        super(worldIn);
    }

    @Override
    @Nullable
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        IEntityLivingData data = super.onInitialSpawn(difficulty, livingdata);
        if (this.world != null) {
            // 1.12 exposes the powered flag through the lightning-hit hook.
            this.onStruckByLightning(new EntityLightningBolt(this.world, this.posX, this.posY, this.posZ, true));
        }
        return data;
    }
}
