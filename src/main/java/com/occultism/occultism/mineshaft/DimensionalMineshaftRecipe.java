package com.occultism.occultism.mineshaft;

import com.occultism.occultism.registry.ModBlocks;
import com.occultism.occultism.registry.ModItems;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class DimensionalMineshaftRecipe {

    private static final List<DimensionalMineshaftRecipe> RECIPES = new ArrayList<>(createRecipes());

    private final ItemStack miner;
    private final List<WeightedOutput> outputs;

    private DimensionalMineshaftRecipe(ItemStack miner, Collection<WeightedOutput> outputs) {
        this.miner = miner.copy();
        this.outputs = Collections.unmodifiableList(copyOutputs(outputs));
    }

    public ItemStack getMiner() {
        return this.miner.copy();
    }

    public List<WeightedOutput> getOutputs() {
        return this.outputs;
    }

    public ItemStack rollOutput(Random random) {
        if (this.outputs.isEmpty()) {
            return ItemStack.EMPTY;
        }
        int totalWeight = 0;
        for (WeightedOutput output : this.outputs) {
            totalWeight += output.getWeight();
        }
        int roll = random.nextInt(totalWeight);
        for (WeightedOutput output : this.outputs) {
            roll -= output.getWeight();
            if (roll < 0) {
                return output.getStack();
            }
        }
        return this.outputs.get(this.outputs.size() - 1).getStack();
    }

    public static List<DimensionalMineshaftRecipe> getRecipes() {
        return Collections.unmodifiableList(RECIPES);
    }

    public static DimensionalMineshaftRecipe find(ItemStack miner) {
        if (miner.isEmpty()) {
            return null;
        }
        Item item = miner.getItem();
        for (DimensionalMineshaftRecipe recipe : RECIPES) {
            if (recipe.miner.getItem() == item) {
                return recipe;
            }
        }
        return null;
    }

    public static synchronized void addRecipe(ItemStack miner, Collection<WeightedOutput> outputs) {
        validateMiner(miner);
        validateOutputs(outputs);
        removeByMiner(miner);
        RECIPES.add(new DimensionalMineshaftRecipe(miner, outputs));
    }

    public static synchronized void addOutput(ItemStack miner, WeightedOutput output) {
        validateMiner(miner);
        if (output == null) {
            throw new IllegalArgumentException("Occultism dimensional mineshaft output must not be null.");
        }
        for (int i = 0; i < RECIPES.size(); i++) {
            DimensionalMineshaftRecipe recipe = RECIPES.get(i);
            if (!recipe.matchesMiner(miner)) {
                continue;
            }
            List<WeightedOutput> outputs = new ArrayList<>(recipe.outputs);
            outputs.add(output);
            RECIPES.set(i, new DimensionalMineshaftRecipe(recipe.miner, outputs));
            return;
        }
        RECIPES.add(new DimensionalMineshaftRecipe(miner, Collections.singletonList(output)));
    }

    public static synchronized int removeByMiner(ItemStack miner) {
        return removeByMiner(Collections.singletonList(miner));
    }

    public static synchronized int removeByMiner(Collection<ItemStack> miners) {
        return removeRecipes(recipe -> recipe.matchesAnyMiner(miners));
    }

    public static synchronized int removeOutputs(Collection<ItemStack> outputs) {
        int removed = 0;
        for (int i = RECIPES.size() - 1; i >= 0; i--) {
            DimensionalMineshaftRecipe recipe = RECIPES.get(i);
            List<WeightedOutput> remaining = new ArrayList<>();
            for (WeightedOutput output : recipe.outputs) {
                if (matchesAnyOutput(output.stack, outputs)) {
                    removed++;
                } else {
                    remaining.add(output);
                }
            }
            if (remaining.size() == recipe.outputs.size()) {
                continue;
            }
            if (remaining.isEmpty()) {
                RECIPES.remove(i);
            } else {
                RECIPES.set(i, new DimensionalMineshaftRecipe(recipe.miner, remaining));
            }
        }
        return removed;
    }

    public static synchronized int removeRecipes(Predicate<DimensionalMineshaftRecipe> predicate) {
        int originalSize = RECIPES.size();
        RECIPES.removeIf(predicate);
        return originalSize - RECIPES.size();
    }

    private static List<DimensionalMineshaftRecipe> createRecipes() {
        List<DimensionalMineshaftRecipe> recipes = new ArrayList<>();

        Builder foliot = builder(ModItems.MINER_FOLIOT_UNSPECIALIZED);
        foliot.add(Blocks.STONE, 45)
                .add(Blocks.COBBLESTONE, 35)
                .add(Blocks.COAL_ORE, 20)
                .add(Blocks.IRON_ORE, 14)
                .add(ModBlocks.COPPER_ORE, 12)
                .add(ModBlocks.SILVER_ORE, 6)
                .add(Blocks.GOLD_ORE, 4)
                .add(Blocks.REDSTONE_ORE, 3)
                .add(Blocks.LAPIS_ORE, 3)
                .add(Blocks.DIAMOND_ORE, 1);
        recipes.add(foliot.build());

        Builder djinni = builder(ModItems.MINER_DJINNI_ORES);
        djinni.add(Blocks.COAL_ORE, 28)
                .add(Blocks.IRON_ORE, 24)
                .add(ModBlocks.COPPER_ORE, 22)
                .add(ModBlocks.SILVER_ORE, 18)
                .add(Blocks.GOLD_ORE, 12)
                .add(Blocks.REDSTONE_ORE, 10)
                .add(Blocks.LAPIS_ORE, 8)
                .add(Blocks.DIAMOND_ORE, 3)
                .add(Blocks.EMERALD_ORE, 1);
        recipes.add(djinni.build());

        Builder afrit = builder(ModItems.MINER_AFRIT_DEEPS);
        afrit.add(Blocks.IRON_ORE, 18)
                .add(Blocks.GOLD_ORE, 16)
                .add(ModBlocks.COPPER_ORE, 12)
                .add(ModBlocks.SILVER_ORE, 12)
                .add(Blocks.REDSTONE_ORE, 12)
                .add(Blocks.LAPIS_ORE, 8)
                .add(Blocks.QUARTZ_ORE, 8)
                .add(Blocks.GLOWSTONE, 6)
                .add(Blocks.DIAMOND_ORE, 6)
                .add(Blocks.EMERALD_ORE, 3)
                .add(ModBlocks.IESNIUM_ORE, 2);
        recipes.add(afrit.build());

        Builder marid = builder(ModItems.MINER_MARID_MASTER);
        marid.add(Blocks.GOLD_ORE, 18)
                .add(ModBlocks.SILVER_ORE, 14)
                .add(Blocks.REDSTONE_ORE, 12)
                .add(Blocks.LAPIS_ORE, 10)
                .add(Blocks.QUARTZ_ORE, 8)
                .add(Blocks.DIAMOND_ORE, 8)
                .add(Blocks.EMERALD_ORE, 5)
                .add(ModBlocks.IESNIUM_ORE, 5)
                .add(Blocks.GLOWSTONE, 5)
                .add(new ItemStack(ModItems.MINING_DIM_CORE), 1);
        recipes.add(marid.build());

        Builder ancient = builder(ModItems.MINER_ANCIENT_ELDRITCH);
        ancient.add(Blocks.DIAMOND_ORE, 16)
                .add(Blocks.EMERALD_ORE, 12)
                .add(ModBlocks.IESNIUM_ORE, 12)
                .add(new ItemStack(ModItems.NETHERITE_DUST), 8)
                .add(new ItemStack(ModItems.MINING_DIM_CORE), 4);
        recipes.add(ancient.build());

        return Collections.unmodifiableList(recipes);
    }

    private static Builder builder(Item miner) {
        return new Builder(miner);
    }

    private boolean matchesMiner(ItemStack miner) {
        return miner != null && !miner.isEmpty() && this.miner.getItem() == miner.getItem();
    }

    private boolean matchesAnyMiner(Collection<ItemStack> miners) {
        if (miners == null) {
            return false;
        }
        for (ItemStack miner : miners) {
            if (this.matchesMiner(miner)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesAnyOutput(ItemStack output, Collection<ItemStack> candidates) {
        if (candidates == null) {
            return false;
        }
        for (ItemStack candidate : candidates) {
            if (sameItemAndTagsIgnoreCount(output, candidate)) {
                return true;
            }
        }
        return false;
    }

    private static List<WeightedOutput> copyOutputs(Collection<WeightedOutput> outputs) {
        List<WeightedOutput> copies = new ArrayList<>();
        if (outputs == null) {
            return copies;
        }
        for (WeightedOutput output : outputs) {
            if (output != null) {
                copies.add(new WeightedOutput(output.stack, output.weight));
            }
        }
        return copies;
    }

    private static void validateMiner(ItemStack miner) {
        if (miner == null || miner.isEmpty()) {
            throw new IllegalArgumentException("Occultism dimensional mineshaft miner must not be empty.");
        }
    }

    private static void validateOutputs(Collection<WeightedOutput> outputs) {
        if (outputs == null || outputs.isEmpty()) {
            throw new IllegalArgumentException("Occultism dimensional mineshaft recipe must have outputs.");
        }
        for (WeightedOutput output : outputs) {
            if (output == null) {
                throw new IllegalArgumentException("Occultism dimensional mineshaft output must not be null.");
            }
        }
    }

    private static boolean sameItemAndTagsIgnoreCount(ItemStack first, ItemStack second) {
        if (first == null || first.isEmpty() || second == null || second.isEmpty()) {
            return false;
        }
        return first.getItem() == second.getItem() && first.getMetadata() == second.getMetadata()
                && ItemStack.areItemStackTagsEqual(first, second);
    }

    public static class WeightedOutput {
        private final ItemStack stack;
        private final int weight;

        public WeightedOutput(ItemStack stack, int weight) {
            if (stack == null || stack.isEmpty()) {
                throw new IllegalArgumentException("Occultism dimensional mineshaft output stack must not be empty.");
            }
            if (weight <= 0) {
                throw new IllegalArgumentException("Occultism dimensional mineshaft output weight must be positive.");
            }
            this.stack = stack.copy();
            this.weight = weight;
        }

        public ItemStack getStack() {
            return this.stack.copy();
        }

        public int getWeight() {
            return this.weight;
        }
    }

    private static class Builder {
        private final ItemStack miner;
        private final List<WeightedOutput> outputs = new ArrayList<>();

        private Builder(Item miner) {
            this.miner = new ItemStack(miner);
        }

        private Builder add(Block block, int weight) {
            return this.add(new ItemStack(block), weight);
        }

        private Builder add(ItemStack stack, int weight) {
            this.outputs.add(new WeightedOutput(stack, weight));
            return this;
        }

        private DimensionalMineshaftRecipe build() {
            return new DimensionalMineshaftRecipe(this.miner, this.outputs);
        }
    }
}
