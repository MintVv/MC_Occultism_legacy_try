package com.occultism.occultism.compat.jei;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.occultism.occultism.ritual.SpiritFireRecipeRegistry;

import net.minecraft.item.ItemStack;

public class SpiritFireRecipe {

    private final List<ItemStack> inputs;
    private final ItemStack output;

    public SpiritFireRecipe(ItemStack input, ItemStack output) {
        this(Collections.singletonList(input), output);
    }

    public SpiritFireRecipe(List<ItemStack> inputs, ItemStack output) {
        this.inputs = inputs;
        this.output = output;
    }

    public List<ItemStack> getInputs() {
        return this.inputs;
    }

    public ItemStack getOutput() {
        return this.output;
    }

    public static List<SpiritFireRecipe> createAll() {
        List<SpiritFireRecipe> recipes = new ArrayList<>();
        for (SpiritFireRecipeRegistry.Recipe recipe : SpiritFireRecipeRegistry.getRecipes()) {
            recipes.add(new SpiritFireRecipe(recipe.getInputs(), recipe.getOutput()));
        }
        return recipes;
    }
}
