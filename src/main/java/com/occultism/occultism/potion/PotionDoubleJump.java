package com.occultism.occultism.potion;

import net.minecraft.potion.Potion;

/** Marker effect used by Otherworld Bird to authorize aerial jumps. */
public class PotionDoubleJump extends Potion {

    public PotionDoubleJump() {
        super(false, 0xFFF200);
        this.setPotionName("effect.occultism.double_jump");
        this.setBeneficial();
    }
}
