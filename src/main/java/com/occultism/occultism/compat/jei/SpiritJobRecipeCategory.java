package com.occultism.occultism.compat.jei;

import com.occultism.occultism.Tags;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

public class SpiritJobRecipeCategory implements IRecipeCategory<SpiritJobRecipeWrapper> {

    private static final int WIDTH = 140;
    private static final int HEIGHT = 66;

    private final String uid;
    private final String titleKey;
    private final IDrawable background;
    private final IDrawable icon;

    public SpiritJobRecipeCategory(IGuiHelper guiHelper, String uid, String titleKey, ItemStack iconStack) {
        this.uid = uid;
        this.titleKey = titleKey;
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableIngredient(iconStack);
    }

    @Override
    public String getUid() {
        return this.uid;
    }

    @Override
    public String getTitle() {
        return I18n.format(this.titleKey);
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
    public void setRecipe(mezz.jei.api.gui.IRecipeLayout recipeLayout, SpiritJobRecipeWrapper recipeWrapper,
            IIngredients ingredients) {
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
        itemStacks.init(0, true, 42, 23);
        itemStacks.init(1, false, 108, 23);
        itemStacks.set(ingredients);
    }
}
