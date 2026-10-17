package com.occultism.occultism.compat.jei;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class StorageRecipeIdResolverTest {
    @BeforeAll
    static void bootstrap() {
        Bootstrap.register();
    }

    @Test
    void fullSignatureResolvesSixAlternativesBeforeTheLegacyPayloadIsTruncated() {
        Ingredient cost = Ingredient.fromItems(Items.COAL, Items.REDSTONE, Items.GLOWSTONE_DUST,
                Items.GOLD_INGOT, Items.EMERALD, Items.IRON_INGOT);
        IRecipe recipe = recipe("all_six", cost);
        Map<Integer, List<ItemStack>> slots = slots(new ItemStack(Items.DIAMOND));
        slots.put(1, Arrays.asList(cost.getMatchingStacks()));
        assertEquals(recipe.getRegistryName(), resolve(slots, recipe));
    }

    @Test
    void outputAloneAndPartialAlternativeOverlapAreNeverEnoughToSelectAnId() {
        Ingredient cost = Ingredient.fromItems(Items.IRON_INGOT, Items.GOLD_INGOT);
        IRecipe recipe = recipe("iron_or_gold", cost);
        Map<Integer, List<ItemStack>> slots = slots(new ItemStack(Items.DIAMOND));
        slots.put(1, Collections.singletonList(new ItemStack(Items.IRON_INGOT)));
        assertNull(resolve(slots, recipe));
        slots.put(1, Arrays.asList(new ItemStack(Items.GOLD_INGOT), new ItemStack(Items.IRON_INGOT),
                new ItemStack(Items.IRON_INGOT, 5)));
        assertEquals(recipe.getRegistryName(), resolve(slots, recipe));
        slots.put(0, Collections.singletonList(new ItemStack(Items.DIAMOND, 2)));
        assertNull(resolve(slots, recipe), "result counts are part of the recipe signature");
    }

    @Test
    void ambiguousAndUnregisteredRecipesFallBackRatherThanGuessing() {
        Ingredient iron = Ingredient.fromItem(Items.IRON_INGOT);
        IRecipe first = recipe("first", iron);
        IRecipe second = recipe("second", iron);
        Map<Integer, List<ItemStack>> slots = slots(new ItemStack(Items.DIAMOND));
        slots.put(1, Arrays.asList(iron.getMatchingStacks()));
        assertNull(resolve(slots, first, second));
        IRecipe unnamed = new ShapelessRecipes("", new ItemStack(Items.DIAMOND), ingredients(iron));
        assertNull(resolve(slots, unnamed));
    }

    @Test
    void shapedSignaturesRequireTheActualThreeColumnLayout() {
        Ingredient iron = Ingredient.fromItem(Items.IRON_INGOT);
        Ingredient gold = Ingredient.fromItem(Items.GOLD_INGOT);
        IRecipe shaped = new ShapedRecipes("", 1, 2, ingredients(iron, gold), new ItemStack(Items.DIAMOND))
                .setRegistryName("occultism_test:narrow");
        Map<Integer, List<ItemStack>> slots = slots(new ItemStack(Items.DIAMOND));
        slots.put(1, Arrays.asList(iron.getMatchingStacks()));
        slots.put(4, Arrays.asList(gold.getMatchingStacks()));
        assertEquals(shaped.getRegistryName(), resolve(slots, shaped));
        slots.put(2, slots.remove(4));
        assertNull(resolve(slots, shaped));
    }

    @Test
    void nbtDifferencesAndExtraCellsDoNotResolveToAnotherRegisteredRecipe() {
        ItemStack tagged = new ItemStack(Items.IRON_INGOT);
        tagged.setTagCompound(new NBTTagCompound());
        tagged.getTagCompound().setBoolean("marked", true);
        IRecipe recipe = recipe("tagged", Ingredient.fromStacks(tagged));
        Map<Integer, List<ItemStack>> slots = slots(new ItemStack(Items.DIAMOND));
        slots.put(1, Collections.singletonList(new ItemStack(Items.IRON_INGOT)));
        assertNull(resolve(slots, recipe));
        slots.put(1, Collections.singletonList(tagged));
        assertEquals(recipe.getRegistryName(), resolve(slots, recipe));
        slots.put(2, Collections.singletonList(new ItemStack(Items.STICK)));
        assertNull(resolve(slots, recipe));
        assertTrue(tagged.getTagCompound().getBoolean("marked"));
    }

    private static ResourceLocation resolve(Map<Integer, List<ItemStack>> slots, IRecipe... recipes) {
        return StorageControllerRecipeTransferHandler.findRegisteredRecipeId(slots, Arrays.asList(recipes));
    }

    private static IRecipe recipe(String name, Ingredient... costs) {
        return new ShapelessRecipes("", new ItemStack(Items.DIAMOND), ingredients(costs))
                .setRegistryName("occultism_test:" + name);
    }

    private static NonNullList<Ingredient> ingredients(Ingredient... costs) {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        Collections.addAll(ingredients, costs);
        return ingredients;
    }

    private static Map<Integer, List<ItemStack>> slots(ItemStack output) {
        Map<Integer, List<ItemStack>> slots = new LinkedHashMap<>();
        slots.put(0, Collections.singletonList(output));
        return slots;
    }
}
