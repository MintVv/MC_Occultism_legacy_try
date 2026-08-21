package com.occultism.occultism.potion;

import net.minecraft.potion.Potion;

public class PotionMummyDodge extends Potion {

    public PotionMummyDodge() {
        super(false, 0xe4d8a7);
        this.setPotionName("effect.occultism.mummy_dodge");
        this.setBeneficial();
    }
}
