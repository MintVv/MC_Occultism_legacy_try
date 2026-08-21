package com.occultism.occultism.compat.jei;

import com.occultism.occultism.Tags;
import com.occultism.occultism.registry.ModBlocks;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

public class SpiritFireRecipeCategory implements IRecipeCategory<SpiritFireRecipeWrapper> {

    private final IDrawable background;
    private final IDrawable icon;

    public SpiritFireRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(168, 150);
        this.icon = guiHelper.createDrawableIngredient(new ItemStack(ModBlocks.SPIRIT_FIRE));
    }

    @Override
    public String getUid() {
        return RitualJeiPlugin.SPIRIT_FIRE_UID;
    }

    @Override
    public String getTitle() {
        return I18n.format("jei.occultism.category.spirit_fire");
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
    public void setRecipe(mezz.jei.api.gui.IRecipeLayout recipeLayout, SpiritFireRecipeWrapper recipeWrapper,
            IIngredients ingredients) {
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
        int slot = 0;
        for (int row = 0; row < recipeWrapper.getRecipeCount(); row++) {
            int y = row == 0 ? 53 : 119;
            itemStacks.init(slot++, true, 30, y);
            itemStacks.init(slot++, false, 133, y);
        }
        itemStacks.set(ingredients);
    }
}
