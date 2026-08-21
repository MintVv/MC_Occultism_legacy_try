package com.occultism.occultism.client.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;

/**
 * Client-side copy of a controller's virtual stack list.
 *
 * <p>The cache is rebuilt lazily by item identity.  This keeps recipe/JEI
 * lookups cheap while ensuring a packet update cannot mutate a GUI-owned stack
 * or retain server-side references.</p>
 */
public class ClientStorageCache {

    private final Map<Item, List<ItemStack>> entriesByItem = new IdentityHashMap<>();
    private List<ItemStack> stacks = Collections.emptyList();
    private boolean needsRebuild = true;

    public List<ItemStack> getStacks() {
        return copyStacks(this.stacks);
    }

    /** Upstream-compatible accessor name. */
    public List<ItemStack> stacks() {
        return this.getStacks();
    }

    public void update(List<ItemStack> stacks) {
        this.stacks = copyStacks(stacks);
        this.needsRebuild = true;
    }

    /** Returns all cached entries that match the 1.12 crafting ingredient. */
    public List<ItemStack> getByIngredient(Ingredient ingredient) {
        if (ingredient == null) {
            return Collections.emptyList();
        }
        List<ItemStack> result = new ArrayList<>();
        for (ItemStack candidate : this.stacks) {
            if (ingredient.apply(candidate)) {
                result.add(candidate.copy());
            }
        }
        return result;
    }

    /** Returns entries for an item without exposing mutable cache contents. */
    public List<ItemStack> getByItem(Item item) {
        if (item == null) {
            return Collections.emptyList();
        }
        this.rebuildIfNeeded();
        List<ItemStack> result = this.entriesByItem.get(item);
        return result == null ? Collections.emptyList() : copyStacks(result);
    }

    public boolean isEmpty() {
        return this.stacks.isEmpty();
    }

    private void rebuildIfNeeded() {
        if (!this.needsRebuild) {
            return;
        }
        this.entriesByItem.clear();
        for (ItemStack stack : this.stacks) {
            Item item = stack.getItem();
            if (item == null) {
                continue;
            }
            List<ItemStack> entries = this.entriesByItem.get(item);
            if (entries == null) {
                entries = new ArrayList<>();
                this.entriesByItem.put(item, entries);
            }
            entries.add(stack);
        }
        this.needsRebuild = false;
    }

    private static List<ItemStack> copyStacks(List<ItemStack> source) {
        List<ItemStack> result = new ArrayList<>();
        if (source != null) {
            for (ItemStack stack : source) {
                result.add(stack == null ? ItemStack.EMPTY : stack.copy());
            }
        }
        return result;
    }
}
