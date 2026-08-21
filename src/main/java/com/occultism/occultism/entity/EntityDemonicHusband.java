package com.occultism.occultism.entity;

import net.minecraft.world.World;

public class EntityDemonicHusband extends EntityDemonicPartner {

    public EntityDemonicHusband(World worldIn) {
        super(worldIn);
    }

    @Override
    public float getPartnerWidth() {
        return 0.6F;
    }

    @Override
    public float getPartnerHeight() {
        return 1.8F;
    }
}
