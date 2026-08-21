package com.occultism.occultism.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.EnumDifficulty;

final class PeacefulRemovalGuard {

    private boolean tracking;
    private int setDeadCalls;

    boolean begin(EntityLivingBase entity) {
        boolean protect = entity.world != null && !entity.world.isRemote
                && entity.world.getDifficulty() == EnumDifficulty.PEACEFUL
                && !entity.isDead && entity.getHealth() > 0.0F;
        if (protect) {
            this.tracking = true;
            this.setDeadCalls = 0;
        }
        return protect;
    }

    void finish(EntityLivingBase entity, boolean protect) {
        this.tracking = false;
        if (protect && this.setDeadCalls == 1 && entity.getHealth() > 0.0F) {
            entity.isDead = false;
        }
    }

    void recordSetDead() {
        if (this.tracking) {
            this.setDeadCalls++;
        }
    }
}
