package com.occultism.occultism.entity;

import net.minecraft.entity.Entity;

public interface IWildHuntEntity {

    static boolean isWildHunt(Entity entity) {
        return entity instanceof IWildHuntEntity;
    }
}
