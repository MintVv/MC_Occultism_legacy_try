package com.occultism.occultism.spirit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Collection;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.google.common.base.Predicate;
import com.occultism.occultism.registry.ModBlocks;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.RecipeMatcher;

/** Shared 1.12 representation of the upstream spirit_trade recipes. */
public final class SpiritTradeRecipeRegistry {
    public static final int MAX_CONVERSION_STEPS = Integer.MAX_VALUE / 20;
    public static final int MAX_TRADES_PER_ROUND = 64;
    public static final int MAX_INGREDIENTS = 9;
    private static final List<Recipe> RECIPES = createRecipes();

    private SpiritTradeRecipeRegistry() {
    }

    public static synchronized List<Recipe> getRecipes() {
        return Collections.unmodifiableList(new ArrayList<>(RECIPES));
    }

    public static synchronized Recipe find(String id) {
        String canonical = canonicalId(id);
        if (canonical == null) {
            return null;
        }
        for (Recipe recipe : RECIPES) {
            if (recipe.getResourceId().toString().equals(canonical)) {
                return recipe;
            }
        }
        return null;
    }

    public static Recipe findFor(SpiritJobType jobType, ItemStack input) {
        Recipe recipe = getForJob(jobType);
        return recipe != null && recipe.matches(input) ? recipe : null;
    }

    public static Recipe getForJob(SpiritJobType jobType) {
        return find(getDefaultRecipeId(jobType));
    }

    public static boolean isTradeJob(SpiritJobType jobType) {
        return getDefaultRecipeId(jobType) != null;
    }

    public static String getDefaultRecipeId(SpiritJobType jobType) {
        if (jobType == SpiritJobType.TRADER_OTHERSTONE) {
            return "occultism:spirit_trade/stone_to_otherstone";
        }
        if (jobType == SpiritJobType.TRADER_OTHERROCK) {
            return "occultism:spirit_trade/stone_to_otherrock";
        }
        return jobType == SpiritJobType.TRADER_SAPLING ? "occultism:spirit_trade/otherworld_sapling" : null;
    }

    /** Short guide IDs and upstream recipe IDs refer to the same registry entry. */
    public static String canonicalId(String id) {
        if (id == null || id.trim().isEmpty()) {
            return null;
        }
        String value = id.trim();
        if (!value.contains(":")) {
            value = "occultism:" + (value.startsWith("spirit_trade/") ? value : "spirit_trade/" + value);
        } else if (value.startsWith("occultism:") && !value.substring(10).contains("/")) {
            value = "occultism:spirit_trade/" + value.substring(10);
        }
        return value.matches("[a-z0-9_.-]+:[a-z0-9_./-]+") ? value : null;
    }

    /** Replace atomically after validation so a bad integration request cannot erase a working trade. */
    public static synchronized Recipe register(String id, SpiritJobType jobType, Collection<ItemStack> inputs,
            String oreName, ItemStack output, int conversionSteps, int maxTradesPerRound) {
        if (inputs == null) {
            throw new IllegalArgumentException("Missing spirit trade input: " + id);
        }
        List<ItemStack> alternatives = new ArrayList<>();
        for (ItemStack input : inputs) {
            if (input == null || input.isEmpty()) {
                throw new IllegalArgumentException("Empty spirit trade input: " + id);
            }
            ItemStack copy = input.copy();
            copy.setCount(1);
            alternatives.add(copy);
        }
        String ore = oreName == null || oreName.trim().isEmpty() ? null : oreName.trim();
        if (alternatives.isEmpty() && ore == null) {
            throw new IllegalArgumentException("Spirit trades must consume an input: " + id);
        }
        SpiritTradeIngredient ingredient = new SpiritTradeIngredient(() -> alternatives, ore);
        return registerIngredients(id, jobType, Collections.singletonList(ingredient), output,
                conversionSteps, maxTradesPerRound);
    }

    public static synchronized Recipe registerIngredients(String id, SpiritJobType jobType,
            List<SpiritTradeIngredient> ingredients, ItemStack output, int conversionSteps, int maxTradesPerRound) {
        String canonical = canonicalId(id);
        if (canonical == null || !isTradeJob(jobType) || ingredients == null || ingredients.isEmpty()
                || ingredients.size() > MAX_INGREDIENTS || ingredients.contains(null)
                || output == null || output.isEmpty() || output.getCount() > output.getMaxStackSize()
                || conversionSteps < 1 || conversionSteps > MAX_CONVERSION_STEPS
                || maxTradesPerRound < 1 || maxTradesPerRound > MAX_TRADES_PER_ROUND) {
            throw new IllegalArgumentException("Invalid spirit trade definition: " + id);
        }
        ItemStack snapshot = output.copy();
        Recipe recipe = new Recipe(canonical, jobType, ingredients, () -> snapshot,
                conversionSteps, maxTradesPerRound);
        Recipe previous = find(canonical);
        if (previous != null) {
            RECIPES.set(RECIPES.indexOf(previous), recipe);
        } else {
            RECIPES.add(recipe);
        }
        return recipe;
    }

    public static synchronized Recipe remove(String id) {
        Recipe previous = find(id);
        if (previous != null) {
            RECIPES.remove(previous);
        }
        return previous;
    }

    public static synchronized void restore(Recipe recipe) {
        if (recipe == null) {
            throw new IllegalArgumentException("Cannot restore an empty recipe");
        }
        Recipe previous = find(recipe.getResourceId().toString());
        if (previous != null) {
            RECIPES.set(RECIPES.indexOf(previous), recipe);
        } else {
            RECIPES.add(recipe);
        }
    }

    private static List<Recipe> createRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        recipes.add(new Recipe("stone_to_otherstone", SpiritJobType.TRADER_OTHERSTONE,
                SpiritTradeRecipeRegistry::stoneInputs, "stone", () -> new ItemStack(ModBlocks.OTHERSTONE, 2), 15, 4));
        recipes.add(new Recipe("stone_to_otherrock", SpiritJobType.TRADER_OTHERROCK,
                SpiritTradeRecipeRegistry::stoneInputs, "stone", () -> new ItemStack(ModBlocks.OTHERROCK, 2), 15, 4));
        recipes.add(new Recipe("otherworld_sapling", SpiritJobType.TRADER_SAPLING,
                () -> Collections.singletonList(new ItemStack(ModBlocks.OTHERWORLD_SAPLING_NATURAL)), null,
                () -> new ItemStack(ModBlocks.OTHERWORLD_SAPLING), 20, 1));
        return recipes;
    }

    private static List<ItemStack> stoneInputs() {
        List<ItemStack> inputs = new ArrayList<>();
        // Preserve the existing port's seven stone variants; accept modded ore-dictionary stone too.
        for (int metadata = 0; metadata <= 6; metadata++) {
            inputs.add(new ItemStack(Blocks.STONE, 1, metadata));
        }
        return inputs;
    }

    public static final class Recipe {
        private final String id;
        private final SpiritJobType jobType;
        private final List<SpiritTradeIngredient> ingredients;
        private final Supplier<ItemStack> output;
        private final int conversionSteps;
        private final int maxTradesPerRound;

        // Defaults must not resolve block items during an early class load before Forge registration.
        private Recipe(String id, SpiritJobType jobType, Supplier<List<ItemStack>> displayInputs,
                String oreName, Supplier<ItemStack> output, int conversionSteps, int maxTradesPerRound) {
            this(id, jobType, Collections.singletonList(new SpiritTradeIngredient(displayInputs, oreName)),
                    output, conversionSteps, maxTradesPerRound);
        }

        private Recipe(String id, SpiritJobType jobType, List<SpiritTradeIngredient> ingredients,
                Supplier<ItemStack> output, int conversionSteps, int maxTradesPerRound) {
            this.id = id;
            this.jobType = jobType;
            this.ingredients = Collections.unmodifiableList(new ArrayList<>(ingredients));
            this.output = output;
            this.conversionSteps = conversionSteps;
            this.maxTradesPerRound = maxTradesPerRound;
        }

        public String getId() {
            return this.id;
        }

        public ResourceLocation getResourceId() {
            return new ResourceLocation(canonicalId(this.id));
        }

        public SpiritJobType getJobType() {
            return this.jobType;
        }

        public List<ItemStack> getDisplayInputs() {
            return this.ingredients.get(0).getDisplayInputs();
        }

        public List<SpiritTradeIngredient> getIngredients() {
            return this.ingredients;
        }

        public ItemStack getOutput() {
            return this.output.get().copy();
        }

        public int getConversionSteps() {
            return this.conversionSteps;
        }

        public int getMaxTradesPerRound() {
            return this.maxTradesPerRound;
        }

        public boolean matches(ItemStack input) {
            return input != null && input.getCount() >= this.ingredients.size() && this.acceptsStackType(input);
        }

        public boolean acceptsStackType(ItemStack input) {
            if (input == null || input.isEmpty()) {
                return false;
            }
            for (SpiritTradeIngredient ingredient : this.ingredients) {
                if (!ingredient.matches(input)) {
                    return false;
                }
            }
            return true;
        }

        /** TraderJob, like upstream, supplies its single held stack. */
        public ItemStack trade(ItemStack input) {
            return this.trade(input, this.maxTradesPerRound);
        }

        public ItemStack trade(ItemStack input, int limit) {
            if (limit < 1 || limit > MAX_TRADES_PER_ROUND || !this.matches(input)) {
                return ItemStack.EMPTY;
            }
            int trades = Math.min(input.getCount() / this.ingredients.size(), limit);
            ItemStack result = this.getOutput();
            if (result.isEmpty()) {
                return ItemStack.EMPTY;
            }
            result.setCount(result.getCount() * trades);
            input.shrink(trades * this.ingredients.size());
            return result;
        }

        public boolean isValid(List<ItemStack> input) {
            return this.planConsumption(input) != null;
        }

        /** Upstream-style copy-on-consume API. Insufficient inputs return unchanged copies. */
        public List<ItemStack> consume(List<ItemStack> input) {
            int[] counts = this.planConsumption(input);
            List<ItemStack> remaining = copy(input);
            if (counts == null) {
                return remaining;
            }
            for (int index = 0; index < counts.length; index++) {
                remaining.get(index).shrink(counts[index]);
            }
            remaining.removeIf(ItemStack::isEmpty);
            return remaining;
        }

        private int[] planConsumption(List<ItemStack> input) {
            if (input == null || input.stream().anyMatch(stack -> stack == null)) {
                throw new IllegalArgumentException("Missing trade input list or stack");
            }
            int costCount = this.ingredients.size();
            Map<Long, InputUnit> selected = new LinkedHashMap<>();
            // Each of N costs needs at most N candidate units: at most N-1 can be taken by other costs.
            // This retains a possible matching while bounding Forge's matcher to at most N*N units.
            for (SpiritTradeIngredient ingredient : this.ingredients) {
                int candidates = 0;
                for (int slot = 0; slot < input.size() && candidates < costCount; slot++) {
                    ItemStack stack = input.get(slot);
                    if (!ingredient.matches(stack)) {
                        continue;
                    }
                    for (int unit = 0; unit < stack.getCount() && candidates < costCount; unit++, candidates++) {
                        selected.putIfAbsent((long) slot * costCount + unit, new InputUnit(slot, stack));
                    }
                }
            }
            if (selected.size() < costCount) {
                return null;
            }
            List<InputUnit> units = new ArrayList<>(selected.values());
            List<Predicate<InputUnit>> tests = new ArrayList<>();
            for (SpiritTradeIngredient ingredient : this.ingredients) {
                tests.add(unit -> ingredient.matches(unit.stack));
            }
            while (tests.size() < units.size()) {
                tests.add(unit -> true); // Unused inventory units remain in the returned stacks.
            }
            int[] matches = RecipeMatcher.findMatches(units, tests);
            if (matches == null) {
                return null;
            }
            int[] consumed = new int[input.size()];
            for (int index = 0; index < matches.length; index++) {
                if (matches[index] < costCount) {
                    consumed[units.get(index).slot]++;
                }
            }
            return consumed;
        }

        private static final class InputUnit {
            private final int slot;
            private final ItemStack stack;

            private InputUnit(int slot, ItemStack stack) {
                this.slot = slot;
                this.stack = stack;
            }
        }

        private static List<ItemStack> copy(List<ItemStack> stacks) {
            List<ItemStack> result = new ArrayList<>();
            for (ItemStack stack : stacks) {
                result.add(stack.copy());
            }
            return result;
        }

    }
}
