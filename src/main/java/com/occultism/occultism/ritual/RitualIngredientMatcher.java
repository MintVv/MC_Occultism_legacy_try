package com.occultism.occultism.ritual;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Predicate;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.RecipeMatcher;

/** Maps remaining costs to physical bowls, shared by validation and incremental consumption. */
final class RitualIngredientMatcher {
    private static final int MAX_UNIT_COSTS = 32;

    private RitualIngredientMatcher() {
    }

    static int[] assign(List<RitualIngredient> costs, List<ItemStack> stacks) {
        if (stacks == null || costs.stream().anyMatch(cost -> !hasPositiveCost(cost))) {
            return null;
        }
        if (costs.isEmpty()) {
            return new int[0];
        }
        if (costs.size() > MAX_UNIT_COSTS || !costs.stream().allMatch(RitualIngredientMatcher::isUnitCost)) {
            return assignCountedCosts(costs, stacks);
        }
        int size = costs.size();
        Map<Long, Unit> selected = new LinkedHashMap<>();
        // N candidates per cost suffice for an N-cost matching; never expand a large stack's full count.
        for (RitualIngredient cost : costs) {
            int candidates = 0;
            for (int slot = 0; slot < stacks.size() && candidates < size; slot++) {
                ItemStack stack = stacks.get(slot);
                if (stack == null || !cost.matches(stack)) {
                    continue;
                }
                for (int unit = 0; unit < stack.getCount() && candidates < size; unit++, candidates++) {
                    selected.putIfAbsent((long) slot * size + unit, new Unit(slot, stack));
                }
            }
        }
        if (selected.size() < size) {
            return null;
        }
        List<Unit> units = new ArrayList<>(selected.values());
        List<Predicate<Unit>> tests = new ArrayList<>();
        for (RitualIngredient cost : costs) {
            tests.add(unit -> cost.matches(unit.stack));
        }
        while (tests.size() < units.size()) {
            tests.add(unit -> true);
        }
        int[] matches = RecipeMatcher.findMatches(units, tests);
        if (matches == null) {
            return null;
        }
        int[] assignment = new int[size];
        for (int index = 0; index < matches.length; index++) {
            if (matches[index] < size) {
                assignment[matches[index]] = units.get(index).slot;
            }
        }
        return assignment;
    }

    private static boolean isUnitCost(RitualIngredient ingredient) {
        if (ingredient.getAlternatives() != null) {
            return !ingredient.getAlternatives().isEmpty()
                    && ingredient.getAlternatives().stream().allMatch(RitualIngredientMatcher::isUnitCost);
        }
        return ingredient.getCount() == 1;
    }

    private static boolean hasPositiveCost(RitualIngredient ingredient) {
        if (ingredient == null) {
            return false;
        }
        if (ingredient.getAlternatives() != null) {
            return !ingredient.getAlternatives().isEmpty()
                    && ingredient.getAlternatives().stream().allMatch(RitualIngredientMatcher::hasPositiveCost);
        }
        return ingredient.getCount() > 0;
    }

    /** Counted/large scripted definitions retain their existing first-match semantics, but now return a plan. */
    private static int[] assignCountedCosts(List<RitualIngredient> costs, List<ItemStack> stacks) {
        List<ItemStack> remaining = new ArrayList<>();
        for (ItemStack stack : stacks) {
            remaining.add(stack == null ? ItemStack.EMPTY : stack.copy());
        }
        int[] assignment = new int[costs.size()];
        for (int index = 0; index < costs.size(); index++) {
            RitualIngredient cost = costs.get(index);
            boolean found = false;
            for (int slot = 0; slot < remaining.size(); slot++) {
                ItemStack stack = remaining.get(slot);
                if (cost.matches(stack)) {
                    assignment[index] = slot;
                    cost.consume(stack);
                    found = true;
                    break;
                }
            }
            if (!found) {
                return null;
            }
        }
        return assignment;
    }

    private static final class Unit {
        private final int slot;
        private final ItemStack stack;

        private Unit(int slot, ItemStack stack) {
            this.slot = slot;
            this.stack = stack;
        }
    }
}
