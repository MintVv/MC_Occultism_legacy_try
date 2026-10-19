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

    /**
     * Row layout shared with {@link SpiritFireRecipeWrapper}, which draws the arrows and the flame.
     * Keeping the numbers in one place stops the icon and the slots from drifting apart - they used
     * to overlap, with the first arrow running under the flame and the second under the output slot.
     */
    static final int INPUT_X = 24;
    static final int FIRE_X = 66;
    static final int OUTPUT_X = 112;
    static final int ARROW_LENGTH = 16;
    static final int ARROW_BEFORE_X = INPUT_X + 20;
    static final int ARROW_AFTER_X = FIRE_X + 22;
    static final int FIRST_ROW_Y = 53;
    static final int SECOND_ROW_Y = 119;

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
            int y = row == 0 ? FIRST_ROW_Y : SECOND_ROW_Y;
            itemStacks.init(slot++, true, INPUT_X, y);
            itemStacks.init(slot++, false, OUTPUT_X, y);
        }
        itemStacks.set(ingredients);
    }
}
