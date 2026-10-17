package com.occultism.occultism.spirit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

/** One item of cost, with alternatives and an optional dynamic ore-dictionary membership. */
public final class SpiritTradeIngredient {
    private final Supplier<List<ItemStack>> alternatives;
    private final String oreName;

    SpiritTradeIngredient(Supplier<List<ItemStack>> alternatives, String oreName) {
        this.alternatives = alternatives;
        this.oreName = oreName;
    }

    public static SpiritTradeIngredient items(Collection<ItemStack> alternatives) {
        if (alternatives == null || alternatives.isEmpty()) {
            throw new IllegalArgumentException("Trade ingredient needs at least one alternative");
        }
        List<ItemStack> snapshot = new ArrayList<>();
        for (ItemStack input : alternatives) {
            if (input == null || input.isEmpty()) {
                throw new IllegalArgumentException("Trade ingredient contains an empty stack");
            }
            ItemStack copy = input.copy();
            copy.setCount(1);
            snapshot.add(copy);
        }
        return new SpiritTradeIngredient(() -> snapshot, null);
    }

    public static SpiritTradeIngredient ore(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Trade ore name must not be empty");
        }
        return new SpiritTradeIngredient(Collections::emptyList, name.trim());
    }

    public boolean matches(ItemStack input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        for (ItemStack candidate : this.alternatives.get()) {
            if (OreDictionary.itemMatches(candidate, input, false)
                    && (!candidate.hasTagCompound() || ItemStack.areItemStackTagsEqual(candidate, input))) {
                return true;
            }
        }
        if (this.oreName != null) {
            for (ItemStack candidate : OreDictionary.getOres(this.oreName)) {
                if (OreDictionary.itemMatches(candidate, input, false)) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<ItemStack> getDisplayInputs() {
        List<ItemStack> inputs = new ArrayList<>();
        for (ItemStack stack : this.alternatives.get()) {
            addDisplay(inputs, stack);
        }
        if (this.oreName != null) {
            for (ItemStack stack : OreDictionary.getOres(this.oreName)) {
                addDisplay(inputs, stack);
            }
        }
        return Collections.unmodifiableList(inputs);
    }

    private static void addDisplay(List<ItemStack> result, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        ItemStack display = stack.copy();
        display.setCount(1);
        if (display.getMetadata() == OreDictionary.WILDCARD_VALUE) {
            display.setItemDamage(0);
        }
        for (ItemStack existing : result) {
            if (ItemStack.areItemsEqual(existing, display) && ItemStack.areItemStackTagsEqual(existing, display)) {
                return;
            }
        }
        result.add(display);
    }
}
