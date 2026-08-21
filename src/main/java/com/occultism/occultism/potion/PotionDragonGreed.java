package com.occultism.occultism.potion;

import net.minecraft.potion.Potion;

public class PotionDragonGreed extends Potion {

    public PotionDragonGreed() {
        super(false, 0xFFD700);
        this.setPotionName("effect.occultism.dragon_greed");
        this.setBeneficial();
    }
}
