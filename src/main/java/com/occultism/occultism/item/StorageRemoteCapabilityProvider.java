package com.occultism.occultism.item;

import javax.annotation.Nullable;

import com.occultism.occultism.api.common.blockentity.IStorageController;
import com.occultism.occultism.api.common.blockentity.IStorageControllerProxy;
import com.occultism.occultism.api.common.data.GlobalBlockPos;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;

/**
 * Loaded-only Forge capability proxy for a linked Storage Remote ItemStack.
 *
 * <p>The item keeps its link in ordinary NBT. This provider resolves that link
 * only when an automation consumer asks for the item-handler capability, so a
 * remote item in a chest never forces a dimension or chunk to load.</p>
 */
public final class StorageRemoteCapabilityProvider implements IStorageControllerProxy {

    private final ItemStack remote;

    public StorageRemoteCapabilityProvider(ItemStack remote) {
        this.remote = remote;
    }

    @Nullable
    private IStorageController getController() {
        return ItemStorageRemote.getStorageController(this.remote, null);
    }

    @Override
    @Nullable
    public IStorageController getLinkedStorageController() {
        return this.getController();
    }

    @Override
    @Nullable
    public GlobalBlockPos getLinkedStorageControllerPosition() {
        return ItemStorageRemote.getLinkedControllerPosition(this.remote);
    }

    @Override
    public void setLinkedStorageControllerPosition(@Nullable GlobalBlockPos blockPos) {
        if (blockPos == null) {
            ItemStorageRemote.clearLinkedController(this.remote);
        } else {
            ItemStorageRemote.setLinkedController(this.remote, blockPos);
        }
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && this.getController() != null;
    }

    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability != CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return null;
        }
        IStorageController controller = this.getController();
        return controller == null ? null : (T) controller.getStorageItemHandler();
    }
}
