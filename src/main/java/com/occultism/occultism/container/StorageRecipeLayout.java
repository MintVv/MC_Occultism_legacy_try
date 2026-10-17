package com.occultism.occultism.container;

import javax.annotation.Nullable;

import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.crafting.IShapedRecipe;

/** Expands registered crafting ingredients into the upstream terminal's top-left 3x3 layout. */
public final class StorageRecipeLayout {
    private StorageRecipeLayout() {
    }

    /** Null means the recipe does not expose a transferable 3x3 ingredient layout. */
    @Nullable
    public static NonNullList<Ingredient> getMatrixIngredients(@Nullable IRecipe recipe) {
        try {
            return expandIngredients(recipe);
        } catch (RuntimeException ignored) {
            // A malformed mod recipe must be rejected before the terminal returns or consumes any items.
            return null;
        }
    }

    @Nullable
    private static NonNullList<Ingredient> expandIngredients(@Nullable IRecipe recipe) {
        if (recipe == null || !recipe.canFit(3, 3)) {
            return null;
        }
        NonNullList<Ingredient> ingredients = recipe.getIngredients();
        if (ingredients == null || ingredients.isEmpty() || ingredients.size() > 9
                || ingredients.stream().allMatch(ingredient -> ingredient == Ingredient.EMPTY)) {
            return null;
        }
        NonNullList<Ingredient> matrix = NonNullList.withSize(9, Ingredient.EMPTY);
        if (recipe instanceof IShapedRecipe) {
            IShapedRecipe shaped = (IShapedRecipe) recipe;
            int width = shaped.getRecipeWidth();
            int height = shaped.getRecipeHeight();
            if (width < 1 || width > 3 || height < 1 || height > 3 || ingredients.size() != width * height) {
                return null;
            }
            for (int row = 0; row < height; row++) {
                for (int column = 0; column < width; column++) {
                    matrix.set(column + row * 3, ingredients.get(column + row * width));
                }
            }
        } else {
            for (int slot = 0; slot < ingredients.size(); slot++) {
                matrix.set(slot, ingredients.get(slot));
            }
        }
        return matrix;
    }
}
