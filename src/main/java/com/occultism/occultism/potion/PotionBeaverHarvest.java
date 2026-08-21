package com.occultism.occultism.potion;

import net.minecraft.potion.Potion;

public class PotionBeaverHarvest extends Potion {

    public PotionBeaverHarvest() {
        super(false, 0x603613);
        this.setPotionName("effect.occultism.beaver_harvest");
        this.setBeneficial();
    }
}
