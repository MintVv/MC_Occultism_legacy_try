package com.occultism.occultism.tileentity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.item.ItemStack;

/**
 * One item in the controller's virtual terminal view.
 *
 * <p>The displayed stack is an aggregate copy.  The physical slot list is
 * retained so extraction can still be performed through the real item
 * handler, rather than mutating a client-only snapshot.</p>
 */
public final class StorageVirtualEntry {

    private final ItemStack stack;
    private final List<Integer> physicalSlots;

    StorageVirtualEntry(ItemStack stack, int physicalSlot) {
        this.stack = stack.copy();
        this.physicalSlots = new ArrayList<>();
        this.physicalSlots.add(Integer.valueOf(physicalSlot));
    }

    void merge(ItemStack additional, int physicalSlot) {
        this.stack.grow(additional.getCount());
        this.physicalSlots.add(Integer.valueOf(physicalSlot));
    }

    ItemStack peekStack() {
        return this.stack;
    }

    /** Returns an independent aggregate stack for rendering or comparison. */
    public ItemStack getStack() {
        return this.stack.copy();
    }

    /** Returns immutable physical handler slots contributing to this entry. */
    public List<Integer> getPhysicalSlots() {
        return Collections.unmodifiableList(this.physicalSlots);
    }

    public int getCount() {
        return this.stack.getCount();
    }
}
