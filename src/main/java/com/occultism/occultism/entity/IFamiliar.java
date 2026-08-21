package com.occultism.occultism.entity;

import javax.annotation.Nullable;

import com.occultism.occultism.util.FamiliarUtil;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;

import java.util.Collections;

public interface IFamiliar {

    @Nullable
    EntityLivingBase getFamiliarOwner();

    void setFamiliarOwner(EntityLivingBase owner);

    EntityLivingBase getFamiliarEntity();

    /**
     * Returns fresh potion effects supplied by this familiar. The 1.12 port
     * keeps world-side effect refreshes in the individual entities until an
     * explicit accessory slot is available; this method is the shared
     * contract used by that future ring adapter.
     */
    default Iterable<PotionEffect> getFamiliarEffects() {
        return Collections.emptyList();
    }

    /**
     * Tick hook for a familiar that is held by an equipped ring. A familiar
     * stored in the ordinary inventory must not receive this callback.
     */
    default void curioTick(EntityLivingBase wearer) {
    }

    /**
     * Settings gate used by event handlers and the future 1.12 accessory
     * adapter. Until the settings capability is installed, a live owner keeps
     * the existing world behaviour enabled.
     */
    default boolean isEffectEnabled(@Nullable EntityLivingBase owner) {
        return owner != null && FamiliarUtil.isFamiliarEnabled(owner, this.getFamiliarEntity().getClass());
    }

    default boolean canBlacksmithUpgrade() {
        return false;
    }

    default void blacksmithUpgrade() {
    }
}
