package com.occultism.occultism.entity;

import net.minecraft.world.World;

public class EntityDemonicWife extends EntityDemonicPartner {

    public EntityDemonicWife(World worldIn) {
        super(worldIn);
    }

    @Override
    public float getPartnerWidth() {
        return 0.6F;
    }

    @Override
    public float getPartnerHeight() {
        return 2.0F;
    }
}
