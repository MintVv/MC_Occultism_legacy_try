package com.occultism.occultism.potion;

import net.minecraft.potion.Potion;

public class PotionFairySlowFall extends Potion {

    public PotionFairySlowFall() {
        super(false, 0xEEE680);
        this.setPotionName("effect.occultism.fairy_slow_fall");
        this.setBeneficial();
    }
}
