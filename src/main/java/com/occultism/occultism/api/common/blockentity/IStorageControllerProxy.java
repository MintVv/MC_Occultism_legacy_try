package com.occultism.occultism.api.common.blockentity;

import javax.annotation.Nullable;

import com.occultism.occultism.api.common.data.GlobalBlockPos;

import net.minecraftforge.common.capabilities.ICapabilityProvider;

/**
 * Capability provider that exposes a loaded storage controller at another
 * location.  A proxy must keep its reference while the target is unloaded,
 * but return {@code null} from {@link #getLinkedStorageController()} until the
 * target dimension and chunk are available.
 */
public interface IStorageControllerProxy extends ICapabilityProvider {

    @Nullable
    IStorageController getLinkedStorageController();

    @Nullable
    GlobalBlockPos getLinkedStorageControllerPosition();

    void setLinkedStorageControllerPosition(@Nullable GlobalBlockPos blockPos);
}
