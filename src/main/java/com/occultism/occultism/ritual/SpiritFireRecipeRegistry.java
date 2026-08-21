package com.occultism.occultism.ritual;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Collection;
import java.util.function.Predicate;

import com.occultism.occultism.registry.ModBlocks;
import com.occultism.occultism.registry.ModItems;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public final class SpiritFireRecipeRegistry {

    private static final List<Recipe> RECIPES = new ArrayList<>();

    static {
        RECIPES.addAll(createRecipes());
    }

    private SpiritFireRecipeRegistry() {
    }

    public static List<Recipe> getRecipes() {
        return Collections.unmodifiableList(RECIPES);
    }

    public static ItemStack getResult(ItemStack input) {
        if (input.isEmpty()) {
            return ItemStack.EMPTY;
        }
        for (Recipe recipe : RECIPES) {
            if (recipe.matches(input)) {
                ItemStack output = recipe.getOutput();
                output.setCount(input.getCount());
                return output;
            }
        }
        return ItemStack.EMPTY;
    }

    public static synchronized void addRecipe(Collection<ItemStack> inputs, ItemStack output) {
        addRecipe(null, inputs, output);
    }

    public static synchronized void addRecipe(String oreName, Collection<ItemStack> inputs, ItemStack output) {
        RECIPES.add(new Recipe(oreName, inputs, output));
    }

    public static synchronized int removeRecipes(Predicate<Recipe> predicate) {
        int originalSize = RECIPES.size();
        RECIPES.removeIf(predicate);
        return originalSize - RECIPES.size();
    }

    public static synchronized int removeByInputs(Collection<ItemStack> inputs) {
        return removeRecipes(recipe -> recipe.matchesAny(inputs));
    }

    public static synchronized int removeByOutputs(Collection<ItemStack> outputs) {
        return removeRecipes(recipe -> matchesAnyOutput(recipe.getOutput(), outputs));
    }

    private static List<Recipe> createRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        recipes.add(oreRecipe("gemDiamond", stack(Items.DIAMOND), new ItemStack(ModItems.SPIRIT_ATTUNED_GEM)));
        recipes.add(recipe(stoneVariants(5, 6), new ItemStack(ModBlocks.OTHERSTONE)));
        recipes.add(recipe(stoneVariants(3, 4), new ItemStack(ModBlocks.OTHERROCK)));
        recipes.add(recipe(new ItemStack(Blocks.SAPLING, 1, 0), new ItemStack(ModBlocks.OTHERWORLD_SAPLING_NATURAL)));
        recipes.add(oreRecipe("logOtherworld", overworldLogs(), new ItemStack(ModItems.OTHERWORLD_ASHES)));
        recipes.add(recipe(vanillaFlowers(), new ItemStack(ModBlocks.OTHERFLOWER)));
        recipes.add(oreRecipe("feather", stack(Items.FEATHER), new ItemStack(ModItems.AWAKENED_FEATHER)));
        recipes.add(oreRecipe("dyeBlack", new ItemStack(Items.DYE, 1, 0), new ItemStack(ModItems.PURIFIED_INK)));
        recipes.add(recipe(new ItemStack(Items.BOOK), new ItemStack(ModItems.TABOO_BOOK)));
        recipes.add(recipe(booksForEmpty(), new ItemStack(ModItems.BOOK_OF_BINDING_EMPTY)));
        recipes.add(recipe(new ItemStack(ModItems.DEMONS_DREAM_ESSENCE),
                new ItemStack(ModItems.OTHERWORLD_ESSENCE)));
        recipes.addAll(chalkRecipes());
        return Collections.unmodifiableList(recipes);
    }

    private static Recipe recipe(ItemStack input, ItemStack output) {
        return recipe(Collections.singletonList(input), output);
    }

    private static Recipe recipe(List<ItemStack> inputs, ItemStack output) {
        return new Recipe(null, inputs, output);
    }

    private static Recipe oreRecipe(String oreName, ItemStack fallbackInput, ItemStack output) {
        return oreRecipe(oreName, Collections.singletonList(fallbackInput), output);
    }

    private static Recipe oreRecipe(String oreName, List<ItemStack> fallbackInputs, ItemStack output) {
        return new Recipe(oreName, fallbackInputs, output);
    }

    private static ItemStack stack(Item item) {
        return new ItemStack(item);
    }

    private static List<ItemStack> stoneVariants(int... metas) {
        List<ItemStack> stacks = new ArrayList<>();
        for (int meta : metas) {
            stacks.add(new ItemStack(Blocks.STONE, 1, meta));
        }
        return stacks;
    }

    private static List<ItemStack> overworldLogs() {
        List<ItemStack> stacks = new ArrayList<>();
        for (int meta = 0; meta < 4; meta++) {
            stacks.add(new ItemStack(Blocks.LOG, 1, meta));
        }
        for (int meta = 0; meta < 2; meta++) {
            stacks.add(new ItemStack(Blocks.LOG2, 1, meta));
        }
        stacks.add(new ItemStack(ModBlocks.OTHERWORLD_LOG));
        stacks.add(new ItemStack(ModBlocks.OTHERWORLD_WOOD));
        stacks.add(new ItemStack(ModBlocks.STRIPPED_OTHERWORLD_LOG));
        stacks.add(new ItemStack(ModBlocks.STRIPPED_OTHERWORLD_WOOD));
        return stacks;
    }

    private static List<ItemStack> vanillaFlowers() {
        List<ItemStack> stacks = new ArrayList<>();
        stacks.add(new ItemStack(Blocks.YELLOW_FLOWER));
        for (int meta = 0; meta <= 8; meta++) {
            stacks.add(new ItemStack(Blocks.RED_FLOWER, 1, meta));
        }
        return stacks;
    }

    private static List<ItemStack> booksForEmpty() {
        List<ItemStack> stacks = new ArrayList<>();
        stacks.add(new ItemStack(Items.WRITABLE_BOOK));
        stacks.add(new ItemStack(Items.WRITTEN_BOOK));
        stacks.add(new ItemStack(Items.ENCHANTED_BOOK));
        stacks.add(new ItemStack(ModItems.BOOK_OF_BINDING_BOUND_FOLIOT));
        stacks.add(new ItemStack(ModItems.BOOK_OF_BINDING_BOUND_DJINNI));
        stacks.add(new ItemStack(ModItems.BOOK_OF_BINDING_BOUND_AFRIT));
        stacks.add(new ItemStack(ModItems.BOOK_OF_BINDING_BOUND_MARID));
        stacks.add(new ItemStack(ModItems.BOOK_OF_BINDING_FOLIOT));
        stacks.add(new ItemStack(ModItems.BOOK_OF_BINDING_DJINNI));
        stacks.add(new ItemStack(ModItems.BOOK_OF_BINDING_AFRIT));
        stacks.add(new ItemStack(ModItems.BOOK_OF_BINDING_MARID));
        stacks.add(new ItemStack(ModItems.BOOK_OF_CALLING_FOLIOT_LUMBERJACK));
        stacks.add(new ItemStack(ModItems.BOOK_OF_CALLING_FOLIOT_FARMER));
        stacks.add(new ItemStack(ModItems.BOOK_OF_CALLING_FOLIOT_CLEANER));
        stacks.add(new ItemStack(ModItems.BOOK_OF_CALLING_FOLIOT_TRANSPORT_ITEMS));
        stacks.add(new ItemStack(ModItems.BOOK_OF_CALLING_FOLIOT_SMELTER));
        stacks.add(new ItemStack(ModItems.BOOK_OF_CALLING_FOLIOT_CRUSHER));
        stacks.add(new ItemStack(ModItems.BOOK_OF_CALLING_FOLIOT_CRYSTALLIZER));
        stacks.add(new ItemStack(ModItems.BOOK_OF_CALLING_FOLIOT_OTHERSTONE_TRADER));
        stacks.add(new ItemStack(ModItems.BOOK_OF_CALLING_FOLIOT_OTHERROCK_TRADER));
        stacks.add(new ItemStack(ModItems.BOOK_OF_CALLING_FOLIOT_SAPLING_TRADER));
        return stacks;
    }

    private static List<Recipe> chalkRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        recipes.add(recipe(new ItemStack(ModItems.CHALK_WHITE_IMPURE), new ItemStack(ModItems.CHALK_WHITE)));
        recipes.add(recipe(new ItemStack(ModItems.CHALK_LIGHT_GRAY_IMPURE), new ItemStack(ModItems.CHALK_LIGHT_GRAY)));
        recipes.add(recipe(new ItemStack(ModItems.CHALK_GRAY_IMPURE), new ItemStack(ModItems.CHALK_GRAY)));
        recipes.add(recipe(new ItemStack(ModItems.CHALK_BLACK_IMPURE), new ItemStack(ModItems.CHALK_BLACK)));
        recipes.add(recipe(new ItemStack(ModItems.CHALK_BROWN_IMPURE), new ItemStack(ModItems.CHALK_BROWN)));
        recipes.add(recipe(new ItemStack(ModItems.CHALK_RED_IMPURE), new ItemStack(ModItems.CHALK_RED)));
        recipes.add(recipe(new ItemStack(ModItems.CHALK_ORANGE_IMPURE), new ItemStack(ModItems.CHALK_ORANGE)));
        recipes.add(recipe(new ItemStack(ModItems.CHALK_YELLOW_IMPURE), new ItemStack(ModItems.CHALK_YELLOW)));
        recipes.add(recipe(new ItemStack(ModItems.CHALK_LIME_IMPURE), new ItemStack(ModItems.CHALK_LIME)));
        recipes.add(recipe(new ItemStack(ModItems.CHALK_GREEN_IMPURE), new ItemStack(ModItems.CHALK_GREEN)));
        recipes.add(recipe(new ItemStack(ModItems.CHALK_CYAN_IMPURE), new ItemStack(ModItems.CHALK_CYAN)));
        recipes.add(recipe(new ItemStack(ModItems.CHALK_LIGHT_BLUE_IMPURE), new ItemStack(ModItems.CHALK_LIGHT_BLUE)));
        recipes.add(recipe(new ItemStack(ModItems.CHALK_BLUE_IMPURE), new ItemStack(ModItems.CHALK_BLUE)));
        recipes.add(recipe(new ItemStack(ModItems.CHALK_PURPLE_IMPURE), new ItemStack(ModItems.CHALK_PURPLE)));
        recipes.add(recipe(new ItemStack(ModItems.CHALK_MAGENTA_IMPURE), new ItemStack(ModItems.CHALK_MAGENTA)));
        recipes.add(recipe(new ItemStack(ModItems.CHALK_PINK_IMPURE), new ItemStack(ModItems.CHALK_PINK)));
        return recipes;
    }

    public static final class Recipe {

        private final String oreName;
        private final List<ItemStack> fallbackInputs;
        private final ItemStack output;

        private Recipe(String oreName, Collection<ItemStack> fallbackInputs, ItemStack output) {
            this.oreName = oreName;
            this.fallbackInputs = copyStacks(fallbackInputs);
            this.output = output.copy();
        }

        public List<ItemStack> getInputs() {
            List<ItemStack> inputs = new ArrayList<>();
            for (ItemStack fallback : this.fallbackInputs) {
                addUnique(inputs, fallback);
            }
            if (this.oreName != null) {
                for (ItemStack oreStack : OreDictionary.getOres(this.oreName)) {
                    addUnique(inputs, oreStack);
                }
            }
            return Collections.unmodifiableList(inputs);
        }

        public ItemStack getOutput() {
            return this.output.copy();
        }

        private boolean matchesAny(Collection<ItemStack> inputs) {
            for (ItemStack input : inputs) {
                if (matches(input)) {
                    return true;
                }
            }
            return false;
        }

        private boolean matches(ItemStack stack) {
            for (ItemStack input : this.fallbackInputs) {
                if (matchesStack(input, stack)) {
                    return true;
                }
            }
            if (this.oreName != null) {
                for (ItemStack oreStack : OreDictionary.getOres(this.oreName)) {
                    if (matchesStack(oreStack, stack)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    private static boolean matchesAnyOutput(ItemStack output, Collection<ItemStack> candidates) {
        for (ItemStack candidate : candidates) {
            if (sameItemAndTagsIgnoreCount(output, candidate)) {
                return true;
            }
        }
        return false;
    }

    private static List<ItemStack> copyStacks(Collection<ItemStack> stacks) {
        List<ItemStack> copies = new ArrayList<>();
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                copies.add(stack.copy());
            }
        }
        return Collections.unmodifiableList(copies);
    }

    private static void addUnique(List<ItemStack> stacks, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        for (ItemStack existing : stacks) {
            if (sameItemAndMetadata(existing, stack)) {
                return;
            }
        }
        stacks.add(stack.copy());
    }

    private static boolean matchesStack(ItemStack target, ItemStack input) {
        return OreDictionary.itemMatches(target, input, false);
    }

    private static boolean sameItemAndTagsIgnoreCount(ItemStack first, ItemStack second) {
        if (first.isEmpty() || second.isEmpty()) {
            return false;
        }
        return first.getItem() == second.getItem() && first.getMetadata() == second.getMetadata()
                && ItemStack.areItemStackTagsEqual(first, second);
    }

    private static boolean sameItemAndMetadata(ItemStack first, ItemStack second) {
        return first.getItem() == second.getItem() && first.getMetadata() == second.getMetadata();
    }
}
