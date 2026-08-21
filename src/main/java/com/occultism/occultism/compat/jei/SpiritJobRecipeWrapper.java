package com.occultism.occultism.compat.jei;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.occultism.occultism.spirit.SpiritJobRecipe;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.item.ItemStack;

public class SpiritJobRecipeWrapper implements IRecipeWrapper {

    private final SpiritJobRecipe recipe;

    public SpiritJobRecipeWrapper(SpiritJobRecipe recipe) {
        this.recipe = recipe;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        List<List<ItemStack>> inputs = new ArrayList<>();
        inputs.add(Collections.unmodifiableList(this.recipe.getInputs()));
        ingredients.setInputLists(ItemStack.class, inputs);
        ingredients.setOutput(ItemStack.class, this.recipe.getOutput());
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        this.drawArrow(72, 31);
    }

    private void drawArrow(int x, int y) {
        Gui.drawRect(x, y, x + 30, y + 4, 0xFF9B9B9B);
        Gui.drawRect(x + 26, y - 4, x + 30, y + 8, 0xFF9B9B9B);
        Gui.drawRect(x + 30, y - 2, x + 34, y + 6, 0xFF9B9B9B);
        Gui.drawRect(x + 34, y, x + 38, y + 4, 0xFF9B9B9B);
    }
}
