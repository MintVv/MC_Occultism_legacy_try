package com.occultism.occultism.compat.crafttweaker;

import java.util.ArrayList;
import java.util.List;

import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import net.minecraft.item.ItemStack;

final class CraftTweakerUtil {

    private CraftTweakerUtil() {
    }

    static List<ItemStack> toItemStacks(IIngredient ingredient) {
        List<ItemStack> stacks = new ArrayList<>();
        if (ingredient == null) {
            return stacks;
        }
        for (IItemStack stack : ingredient.getItems()) {
            ItemStack mcStack = CraftTweakerMC.getItemStack(stack);
            if (!mcStack.isEmpty()) {
                stacks.add(mcStack.copy());
            }
        }
        return stacks;
    }

    static List<ItemStack> toItemStacks(IItemStack... stacks) {
        List<ItemStack> result = new ArrayList<>();
        if (stacks == null) {
            return result;
        }
        for (IItemStack stack : stacks) {
            if (stack == null) {
                continue;
            }
            ItemStack mcStack = CraftTweakerMC.getItemStack(stack);
            if (!mcStack.isEmpty()) {
                result.add(mcStack.copy());
            }
        }
        return result;
    }

    static ItemStack toItemStack(IItemStack stack) {
        if (stack == null) {
            return ItemStack.EMPTY;
        }
        ItemStack mcStack = CraftTweakerMC.getItemStack(stack);
        return mcStack.isEmpty() ? ItemStack.EMPTY : mcStack.copy();
    }

    static String normalizeId(String id) {
        if (id == null) {
            return "";
        }
        String trimmed = id.trim();
        if (trimmed.startsWith("occultism:")) {
            return trimmed.substring("occultism:".length());
        }
        return trimmed;
    }
}
