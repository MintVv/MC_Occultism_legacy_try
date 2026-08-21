package com.occultism.occultism.potion;

import net.minecraft.potion.Potion;

public class PotionBatLifesteal extends Potion {

    public PotionBatLifesteal() {
        super(false, 0x960201);
        this.setPotionName("effect.occultism.bat_lifesteal");
        this.setBeneficial();
    }
}
