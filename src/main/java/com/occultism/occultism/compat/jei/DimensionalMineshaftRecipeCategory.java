package com.occultism.occultism.compat.jei;

import com.occultism.occultism.Tags;
import com.occultism.occultism.mineshaft.DimensionalMineshaftRecipe.WeightedOutput;
import com.occultism.occultism.registry.ModBlocks;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

public class DimensionalMineshaftRecipeCategory implements IRecipeCategory<DimensionalMineshaftRecipeWrapper> {

    private static final int WIDTH = 168;
    private static final int HEIGHT = 92;
    private static final int OUTPUT_SLOT_START = 1;

    private final IDrawable background;
    private final IDrawable icon;

    public DimensionalMineshaftRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableIngredient(new ItemStack(ModBlocks.DIMENSIONAL_MINESHAFT));
    }

    @Override
    public String getUid() {
        return RitualJeiPlugin.DIMENSIONAL_MINESHAFT_UID;
    }

    @Override
    public String getTitle() {
        return I18n.format("jei.occultism.category.dimensional_mineshaft");
    }

    @Override
    public String getModName() {
        return Tags.MOD_NAME;
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(mezz.jei.api.gui.IRecipeLayout recipeLayout,
            DimensionalMineshaftRecipeWrapper recipeWrapper, IIngredients ingredients) {
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
        itemStacks.init(0, true, 12, 42);
        for (int i = 0; i < recipeWrapper.getOutputs().size(); i++) {
            int x = 64 + i % 4 * 22;
            int y = 20 + i / 4 * 22;
            itemStacks.init(OUTPUT_SLOT_START + i, false, x, y);
        }
        itemStacks.addTooltipCallback((slotIndex, input, stack, tooltip) -> {
            if (input) {
                tooltip.add(I18n.format("jei.occultism.mineshaft.miner_tooltip"));
                return;
            }
            int outputIndex = slotIndex - OUTPUT_SLOT_START;
            WeightedOutput output = recipeWrapper.getOutput(outputIndex);
            if (output == null || recipeWrapper.getTotalWeight() <= 0) {
                return;
            }
            float chance = output.getWeight() * 100.0F / recipeWrapper.getTotalWeight();
            tooltip.add(I18n.format("jei.occultism.mineshaft.weight", output.getWeight()));
            tooltip.add(I18n.format("jei.occultism.mineshaft.chance", String.format("%.1f", chance)));
        });
        itemStacks.set(ingredients);
    }
}
