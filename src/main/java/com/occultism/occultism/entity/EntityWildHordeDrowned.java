package com.occultism.occultism.entity;

import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.world.World;

/** 1.12 Zombie-based entity for the wild drowned horde ritual. */
public class EntityWildHordeDrowned extends EntityZombie {

    public EntityWildHordeDrowned(World worldIn) {
        super(worldIn);
    }

    @Override
    protected boolean shouldBurnInDay() {
        return false;
    }
}
