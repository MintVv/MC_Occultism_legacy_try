package com.occultism.occultism.compat.jei;

import com.occultism.occultism.Tags;
import com.occultism.occultism.registry.ModBlocks;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class RitualRecipeCategory implements IRecipeCategory<RitualRecipeWrapper> {

    static final int WIDTH = 168;
    static final int HEIGHT = 132;
    private static final int SLOT_ACTIVATION = 0;
    private static final ResourceLocation JEI_VANILLA_GUI =
            new ResourceLocation("jei", "textures/gui/gui_vanilla.png");
    static final int RITUAL_CENTER_X = 97;
    static final int RITUAL_CENTER_Y = 70;
    static final int SLOT_OFFSET_Y = -5;
    static final int ACTIVATION_X = RITUAL_CENTER_X;
    static final int ACTIVATION_Y = RITUAL_CENTER_Y + SLOT_OFFSET_Y;
    static final int OUTPUT_BOWL_X = 145;
    static final int OUTPUT_BOWL_Y = RITUAL_CENTER_Y;
    static final int OUTPUT_X = OUTPUT_BOWL_X;
    static final int OUTPUT_Y = OUTPUT_BOWL_Y + SLOT_OFFSET_Y;
    static final int ARROW_X = 121;
    static final int ARROW_Y = RITUAL_CENTER_Y - 1;
    static final int ITEM_TO_USE_X = RITUAL_CENTER_X;
    static final int ITEM_TO_USE_Y = 43;
    static final int ITEM_TO_USE_LABEL_Y = 36;

    private final IDrawable background;
    private final IDrawable icon;
    private static IDrawable arrow;
    private static IDrawable goldenSacrificialBowl;
    private static IDrawable sacrificialBowl;

    public RitualRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableIngredient(new ItemStack(ModBlocks.GOLDEN_SACRIFICIAL_BOWL));
        arrow = guiHelper.createDrawable(JEI_VANILLA_GUI, 82, 128, 24, 17);
        goldenSacrificialBowl = guiHelper.createDrawableIngredient(new ItemStack(ModBlocks.GOLDEN_SACRIFICIAL_BOWL));
        sacrificialBowl = guiHelper.createDrawableIngredient(new ItemStack(ModBlocks.SACRIFICIAL_BOWL));
    }

    @Override
    public String getUid() {
        return RitualJeiPlugin.RITUAL_UID;
    }

    @Override
    public String getTitle() {
        return I18n.format("jei.occultism.category.ritual");
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
    public void setRecipe(mezz.jei.api.gui.IRecipeLayout recipeLayout, RitualRecipeWrapper recipeWrapper,
            IIngredients ingredients) {
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
        int inputCount = recipeWrapper.getInputs().size();
        int bowlSlotStart = recipeWrapper.hasItemToUse() ? 2 : 1;
        itemStacks.init(SLOT_ACTIVATION, true, ACTIVATION_X, ACTIVATION_Y);
        if (recipeWrapper.hasItemToUse()) {
            itemStacks.init(1, true, ITEM_TO_USE_X, ITEM_TO_USE_Y);
        }
        for (int i = 0; i < recipeWrapper.getBowlInputs().size(); i++) {
            int[] pos = getBowlPosition(i);
            itemStacks.init(bowlSlotStart + i, true, pos[0], pos[1] + SLOT_OFFSET_Y);
        }
        itemStacks.init(inputCount, false, OUTPUT_X, OUTPUT_Y);
        itemStacks.addTooltipCallback((slotIndex, input, stack, tooltip) -> {
            if (input && slotIndex == SLOT_ACTIVATION) {
                tooltip.add(I18n.format("jei.occultism.ritual.activation"));
            } else if (input && recipeWrapper.hasItemToUse() && slotIndex == 1) {
                tooltip.add(I18n.format("jei.occultism.item_to_use"));
            } else if (input) {
                tooltip.add(I18n.format("jei.occultism.ritual.bowl_ingredient"));
            } else {
                tooltip.add(I18n.format("jei.occultism.ritual.result"));
            }
        });
        itemStacks.set(ingredients);
    }

    static int[] getBowlPosition(int index) {
        int startX = 6;
        int startY = 58;
        int column = index % 4;
        int row = index / 4;
        int[][] positions = new int[][] {
                { startX + column * 20, startY + row * 22 }
        };
        return positions[0];
    }

    static void drawArrow(Minecraft minecraft) {
        if (arrow != null) {
            arrow.draw(minecraft, ARROW_X, ARROW_Y);
        }
    }

    static void drawGoldenSacrificialBowl(Minecraft minecraft, int x, int y) {
        if (goldenSacrificialBowl != null) {
            goldenSacrificialBowl.draw(minecraft, x, y);
        }
    }

    static void drawSacrificialBowl(Minecraft minecraft, int x, int y) {
        if (sacrificialBowl != null) {
            sacrificialBowl.draw(minecraft, x, y);
        }
    }
}
