package com.occultism.occultism.compat.jei;

import java.util.ArrayList;
import java.util.List;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import net.minecraft.item.ItemStack;
import com.occultism.occultism.mineshaft.DimensionalMineshaftRecipe;
import com.occultism.occultism.registry.ModBlocks;
import com.occultism.occultism.registry.ModItems;
import com.occultism.occultism.spirit.SpiritJobRecipe;
import com.occultism.occultism.spirit.SpiritJobType;

@JEIPlugin
public class RitualJeiPlugin implements IModPlugin {

    public static final String RITUAL_UID = "occultism.ritual";
    public static final String SPIRIT_FIRE_UID = "occultism.spirit_fire";
    public static final String CRUSHING_UID = "occultism.crushing";
    public static final String CRYSTALLIZE_UID = "occultism.crystallize";
    public static final String DIMENSIONAL_MINESHAFT_UID = "occultism.dimensional_mineshaft";

    @Override
    public void registerIngredients(IModIngredientRegistration registry) {
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        registry.addRecipeCategories(
                new RitualRecipeCategory(registry.getJeiHelpers().getGuiHelper()),
                new SpiritFireRecipeCategory(registry.getJeiHelpers().getGuiHelper()),
                new SpiritJobRecipeCategory(registry.getJeiHelpers().getGuiHelper(), CRUSHING_UID,
                        "jei.occultism.category.crushing", new ItemStack(ModItems.FOLIOT_CRUSHER_TOKEN)),
                new SpiritJobRecipeCategory(registry.getJeiHelpers().getGuiHelper(), CRYSTALLIZE_UID,
                        "jei.occultism.category.crystallize", new ItemStack(ModItems.FOLIOT_CRYSTALLIZER_TOKEN)),
                new DimensionalMineshaftRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void register(IModRegistry registry) {
        registry.addRecipes(RitualRecipeWrapper.createAll(), RITUAL_UID);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.GOLDEN_SACRIFICIAL_BOWL), RITUAL_UID);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.SACRIFICIAL_BOWL), RITUAL_UID);
        registry.addRecipes(wrapSpiritFireRecipes(), SPIRIT_FIRE_UID);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.SPIRIT_FIRE), SPIRIT_FIRE_UID);
        registry.addRecipes(wrapSpiritJobRecipes(SpiritJobType.CRUSHER), CRUSHING_UID);
        registry.addRecipeCatalyst(new ItemStack(ModItems.FOLIOT_CRUSHER_TOKEN), CRUSHING_UID);
        registry.addRecipes(wrapSpiritJobRecipes(SpiritJobType.CRYSTALLIZER), CRYSTALLIZE_UID);
        registry.addRecipeCatalyst(new ItemStack(ModItems.FOLIOT_CRYSTALLIZER_TOKEN), CRYSTALLIZE_UID);
        registry.addRecipes(wrapDimensionalMineshaftRecipes(), DIMENSIONAL_MINESHAFT_UID);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.DIMENSIONAL_MINESHAFT), DIMENSIONAL_MINESHAFT_UID);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.DIMENSIONAL_EXTRACTOR), DIMENSIONAL_MINESHAFT_UID);
        registry.addRecipeCatalyst(new ItemStack(ModItems.MINER_FOLIOT_UNSPECIALIZED), DIMENSIONAL_MINESHAFT_UID);
        registry.addRecipeCatalyst(new ItemStack(ModItems.MINER_DJINNI_ORES), DIMENSIONAL_MINESHAFT_UID);
        registry.addRecipeCatalyst(new ItemStack(ModItems.MINER_AFRIT_DEEPS), DIMENSIONAL_MINESHAFT_UID);
        registry.addRecipeCatalyst(new ItemStack(ModItems.MINER_MARID_MASTER), DIMENSIONAL_MINESHAFT_UID);
        registry.addRecipeCatalyst(new ItemStack(ModItems.MINER_ANCIENT_ELDRITCH), DIMENSIONAL_MINESHAFT_UID);
        registry.getRecipeTransferRegistry().addRecipeTransferHandler(
                new StorageControllerRecipeTransferHandler(registry.getJeiHelpers().recipeTransferHandlerHelper()),
                VanillaRecipeCategoryUid.CRAFTING);
    }

    private static List<IRecipeWrapper> wrapSpiritFireRecipes() {
        List<IRecipeWrapper> wrappers = new ArrayList<>();
        List<SpiritFireRecipe> recipes = SpiritFireRecipe.createAll();
        for (int i = 0; i < recipes.size(); i += 2) {
            List<SpiritFireRecipe> page = new ArrayList<>();
            page.add(recipes.get(i));
            if (i + 1 < recipes.size()) {
                page.add(recipes.get(i + 1));
            }
            wrappers.add(new SpiritFireRecipeWrapper(page));
        }
        return wrappers;
    }

    private static List<IRecipeWrapper> wrapSpiritJobRecipes(SpiritJobType type) {
        List<IRecipeWrapper> wrappers = new ArrayList<>();
        for (SpiritJobRecipe recipe : SpiritJobRecipe.getRecipes(type)) {
            wrappers.add(new SpiritJobRecipeWrapper(recipe));
        }
        return wrappers;
    }

    private static List<IRecipeWrapper> wrapDimensionalMineshaftRecipes() {
        List<IRecipeWrapper> wrappers = new ArrayList<>();
        for (DimensionalMineshaftRecipe recipe : DimensionalMineshaftRecipe.getRecipes()) {
            wrappers.add(new DimensionalMineshaftRecipeWrapper(recipe));
        }
        return wrappers;
    }
}
