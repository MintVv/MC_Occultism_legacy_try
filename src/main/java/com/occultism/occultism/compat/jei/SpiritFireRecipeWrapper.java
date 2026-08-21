package com.occultism.occultism.compat.jei;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.occultism.occultism.registry.ModBlocks;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.item.ItemStack;

public class SpiritFireRecipeWrapper implements IRecipeWrapper {

    private static final int[] ROW_Y = { 53, 119 };
    private static final int INPUT_X = 30;
    private static final int FIRE_X = 82;
    private static final int OUTPUT_X = 133;

    private final List<SpiritFireRecipe> recipes;

    public SpiritFireRecipeWrapper(List<SpiritFireRecipe> recipes) {
        this.recipes = Collections.unmodifiableList(new ArrayList<>(recipes));
    }

    public int getRecipeCount() {
        return this.recipes.size();
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        List<List<ItemStack>> inputs = new ArrayList<>();
        List<ItemStack> outputs = new ArrayList<>();
        for (SpiritFireRecipe recipe : this.recipes) {
            inputs.add(recipe.getInputs());
            outputs.add(recipe.getOutput());
        }
        ingredients.setInputLists(ItemStack.class, inputs);
        ingredients.setOutputs(ItemStack.class, outputs);
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        for (int row = 0; row < this.recipes.size(); row++) {
            int y = ROW_Y[row];
            this.drawArrow(INPUT_X + 26, y + 7);
            this.drawArrow(FIRE_X + 26, y + 7);
            minecraft.getRenderItem().renderItemAndEffectIntoGUI(new ItemStack(ModBlocks.SPIRIT_FIRE), FIRE_X, y);
        }
    }

    private void drawArrow(int x, int y) {
        Gui.drawRect(x, y, x + 24, y + 4, 0xFF9B9B9B);
        Gui.drawRect(x + 20, y - 4, x + 24, y + 8, 0xFF9B9B9B);
        Gui.drawRect(x + 24, y - 2, x + 28, y + 6, 0xFF9B9B9B);
        Gui.drawRect(x + 28, y, x + 32, y + 4, 0xFF9B9B9B);
    }
}
