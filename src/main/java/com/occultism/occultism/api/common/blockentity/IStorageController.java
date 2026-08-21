package com.occultism.occultism.api.common.blockentity;

import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.occultism.occultism.network.MessageUpdateStacks;
import com.occultism.occultism.spirit.MachineReference;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

/**
 * 1.12 storage-controller API used by storage proxies and machine integrations.
 *
 * <p>The modern API's item-stack comparator type is not present in Forge 1.12;
 * this port uses a {@link Predicate} instead.  Implementations must keep
 * simulation side-effect free and must never resolve another dimension merely
 * to answer an API call.</p>
 */
public interface IStorageController {

    /** Returns defensive copies of the currently stored physical stacks. */
    List<ItemStack> getStacks();

    /** Returns the active physical slot capacity. */
    int getMaxSlots();

    /** Updates active capacity without truncating hidden backing slots. */
    void setMaxSlots(int slots);

    /** Returns the number of non-empty active physical slots. */
    int getUsedSlots();

    /** Returns a fresh bounded stack-update message for network consumers. */
    MessageUpdateStacks getMessageUpdateStacks();

    /** Returns defensive copies of linked machine references. */
    List<MachineReference> getLinkedMachines();

    /** Replaces linked machine references after server-side validation. */
    void setLinkedMachines(List<MachineReference> machines);

    /** Registers one strict machine reference. */
    boolean linkMachine(@Nullable MachineReference machine);

    /** Returns the real Forge handler used for item movement. */
    IItemHandler getStorageItemHandler();

    /** Returns true when the stack is not allowed into this network. */
    boolean isBlacklisted(@Nullable ItemStack stack);

    /** Inserts through the network and returns the number of items left over. */
    int insertStack(@Nullable ItemStack stack, boolean simulate);

    /** Extracts up to {@code requestedSize} matching items. */
    ItemStack getItemStack(@Nullable Predicate<ItemStack> comparator, int requestedSize, boolean simulate);

    /** Returns one item from the most abundant matching stack. */
    ItemStack getOneOfMostCommonItem(@Nullable Predicate<ItemStack> comparator, boolean simulate);

    /** Returns the total amount matching the supplied comparator. */
    int getAvailableAmount(@Nullable Predicate<ItemStack> comparator);

    /** Called by integrations after mutating contents outside the handler. */
    void onContentsChanged();
}
