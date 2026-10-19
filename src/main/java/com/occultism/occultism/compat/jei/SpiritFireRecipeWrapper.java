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
            int y = row == 0 ? SpiritFireRecipeCategory.FIRST_ROW_Y : SpiritFireRecipeCategory.SECOND_ROW_Y;
            this.drawArrow(SpiritFireRecipeCategory.ARROW_BEFORE_X, y + 6,
                    SpiritFireRecipeCategory.ARROW_LENGTH);
            this.drawArrow(SpiritFireRecipeCategory.ARROW_AFTER_X, y + 6,
                    SpiritFireRecipeCategory.ARROW_LENGTH);
            // Same 16x16 box as the item slots, so the flame lines up with the rows it sits between.
            minecraft.getRenderItem().renderItemAndEffectIntoGUI(new ItemStack(ModBlocks.SPIRIT_FIRE),
                    SpiritFireRecipeCategory.FIRE_X, y);
        }
    }

    private void drawArrow(int x, int y, int length) {
        Gui.drawRect(x, y, x + length - 4, y + 4, 0xFF9B9B9B);
        Gui.drawRect(x + length - 8, y - 4, x + length - 4, y + 8, 0xFF9B9B9B);
        Gui.drawRect(x + length - 4, y - 2, x + length, y + 6, 0xFF9B9B9B);
    }
}
