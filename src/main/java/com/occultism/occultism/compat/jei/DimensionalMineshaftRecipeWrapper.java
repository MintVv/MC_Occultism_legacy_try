package com.occultism.occultism.compat.jei;

import com.occultism.occultism.mineshaft.DimensionalMineshaftRecipe;
import com.occultism.occultism.mineshaft.DimensionalMineshaftRecipe.WeightedOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

public class DimensionalMineshaftRecipeWrapper implements IRecipeWrapper {

    private final DimensionalMineshaftRecipe recipe;
    private final List<ItemStack> outputs;
    private final int totalWeight;

    public DimensionalMineshaftRecipeWrapper(DimensionalMineshaftRecipe recipe) {
        this.recipe = recipe;
        this.outputs = this.createOutputs(recipe);
        this.totalWeight = this.calculateTotalWeight(recipe);
    }

    public List<ItemStack> getOutputs() {
        return this.outputs;
    }

    public WeightedOutput getOutput(int index) {
        return index >= 0 && index < this.recipe.getOutputs().size() ? this.recipe.getOutputs().get(index) : null;
    }

    public int getTotalWeight() {
        return this.totalWeight;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        List<List<ItemStack>> inputs = new ArrayList<>();
        inputs.add(Collections.singletonList(this.recipe.getMiner()));
        ingredients.setInputLists(ItemStack.class, inputs);
        ingredients.setOutputs(ItemStack.class, this.outputs);
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        this.drawArrow(38, 48);
        minecraft.fontRenderer.drawString(I18n.format("jei.occultism.mineshaft.miner"), 8, 18, 0x404040);
        minecraft.fontRenderer.drawString(I18n.format("jei.occultism.mineshaft.outputs"), 64, 7, 0x404040);
    }

    private List<ItemStack> createOutputs(DimensionalMineshaftRecipe recipe) {
        List<ItemStack> stacks = new ArrayList<>();
        for (WeightedOutput output : recipe.getOutputs()) {
            stacks.add(output.getStack());
        }
        return Collections.unmodifiableList(stacks);
    }

    private int calculateTotalWeight(DimensionalMineshaftRecipe recipe) {
        int total = 0;
        for (WeightedOutput output : recipe.getOutputs()) {
            total += output.getWeight();
        }
        return total;
    }

    private void drawArrow(int x, int y) {
        Gui.drawRect(x, y, x + 18, y + 4, 0xFF9B9B9B);
        Gui.drawRect(x + 14, y - 4, x + 18, y + 8, 0xFF9B9B9B);
        Gui.drawRect(x + 18, y - 2, x + 22, y + 6, 0xFF9B9B9B);
        Gui.drawRect(x + 22, y, x + 26, y + 4, 0xFF9B9B9B);
    }
}
