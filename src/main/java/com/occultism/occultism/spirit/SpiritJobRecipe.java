package com.occultism.occultism.spirit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import com.occultism.occultism.registry.ModBlocks;
import com.occultism.occultism.registry.ModItems;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class SpiritJobRecipe {

    private static final List<SpiritJobRecipe> CRUSHING_RECIPES = new ArrayList<>();
    private static final List<SpiritJobRecipe> CRYSTALLIZE_RECIPES = new ArrayList<>();

    static {
        CRUSHING_RECIPES.addAll(createCrushingRecipes());
        CRYSTALLIZE_RECIPES.addAll(createCrystallizeRecipes());
    }

    private final SpiritJobType type;
    private final List<ItemStack> inputs;
    private final List<String> oreInputs;
    private final ItemStack output;
    private final int minimumTier;

    private SpiritJobRecipe(SpiritJobType type, List<ItemStack> inputs, List<String> oreInputs, ItemStack output) {
        this(type, inputs, oreInputs, output, -1);
    }

    private SpiritJobRecipe(SpiritJobType type, List<ItemStack> inputs, List<String> oreInputs, ItemStack output,
            int minimumTier) {
        this.type = type;
        this.inputs = Collections.unmodifiableList(inputs);
        this.oreInputs = Collections.unmodifiableList(oreInputs);
        this.output = output.copy();
        this.minimumTier = minimumTier;
    }

    public SpiritJobType getType() {
        return this.type;
    }

    public List<ItemStack> getInputs() {
        List<ItemStack> displayInputs = new ArrayList<>(this.inputs);
        for (String oreName : this.oreInputs) {
            for (ItemStack oreStack : OreDictionary.getOres(oreName)) {
                if (!containsMatchingInput(displayInputs, oreStack)) {
                    displayInputs.add(oreStack.copy());
                }
            }
        }
        return Collections.unmodifiableList(displayInputs);
    }

    public ItemStack getOutput() {
        return this.output.copy();
    }

    public int getMinimumTier() {
        return this.minimumTier;
    }

    public boolean hasOutput(ItemStack stack) {
        return matchesExact(this.output, stack);
    }

    public boolean matches(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        for (ItemStack input : this.inputs) {
            if (matches(input, stack)) {
                return true;
            }
        }
        for (String oreName : this.oreInputs) {
            if (matchesOreName(oreName, stack)) {
                return true;
            }
        }
        return false;
    }

    public ItemStack createOutput(int inputCount) {
        ItemStack result = this.output.copy();
        result.setCount(result.getCount() * inputCount);
        return result;
    }

    public static List<SpiritJobRecipe> getRecipes(SpiritJobType type) {
        if (type == SpiritJobType.CRUSHER) {
            return Collections.unmodifiableList(CRUSHING_RECIPES);
        }
        if (type == SpiritJobType.CRYSTALLIZER) {
            return Collections.unmodifiableList(CRYSTALLIZE_RECIPES);
        }
        return Collections.emptyList();
    }

    public static SpiritJobRecipe findFirst(SpiritJobType type, ItemStack stack) {
        return findFirst(type, stack, null);
    }

    public static SpiritJobRecipe findFirst(SpiritJobType type, ItemStack stack, SpiritTier tier) {
        for (SpiritJobRecipe recipe : getRecipes(type)) {
            if (recipe.isAvailableTo(tier) && recipe.matches(stack)) {
                return recipe;
            }
        }
        return null;
    }

    public static synchronized void addRecipe(SpiritJobType type, Collection<ItemStack> inputs, ItemStack output) {
        getMutableRecipes(type).add(new SpiritJobRecipe(type, copyStacks(inputs), Collections.emptyList(), output));
    }

    public static synchronized void addRecipe(SpiritJobType type, String oreName, ItemStack displayInput,
            ItemStack output) {
        getMutableRecipes(type).add(new SpiritJobRecipe(type, Collections.singletonList(displayInput.copy()),
                Collections.singletonList(oreName), output));
    }

    public static synchronized int removeRecipes(SpiritJobType type, Predicate<SpiritJobRecipe> predicate) {
        List<SpiritJobRecipe> recipes = getMutableRecipes(type);
        int originalSize = recipes.size();
        recipes.removeIf(predicate);
        return originalSize - recipes.size();
    }

    public static synchronized int removeByInputs(SpiritJobType type, Collection<ItemStack> inputs) {
        return removeRecipes(type, recipe -> recipe.matchesAny(inputs));
    }

    public static synchronized int removeByOutputs(SpiritJobType type, Collection<ItemStack> outputs) {
        return removeRecipes(type, recipe -> recipe.hasAnyOutput(outputs));
    }

    private static List<SpiritJobRecipe> createCrushingRecipes() {
        List<SpiritJobRecipe> recipes = new ArrayList<>();
        recipes.add(crushing("oreIron", new ItemStack(Blocks.IRON_ORE), new ItemStack(ModItems.IRON_DUST, 2)));
        recipes.add(crushing("ingotIron", new ItemStack(Items.IRON_INGOT), new ItemStack(ModItems.IRON_DUST)));
        recipes.add(crushing("oreGold", new ItemStack(Blocks.GOLD_ORE), new ItemStack(ModItems.GOLD_DUST, 2)));
        recipes.add(crushing("ingotGold", new ItemStack(Items.GOLD_INGOT), new ItemStack(ModItems.GOLD_DUST)));
        recipes.add(crushing("oreCopper", new ItemStack(ModBlocks.COPPER_ORE), new ItemStack(ModItems.COPPER_DUST, 2)));
        recipes.add(crushing("ingotCopper", new ItemStack(ModItems.COPPER_INGOT), new ItemStack(ModItems.COPPER_DUST)));
        recipes.add(crushing("oreSilver", new ItemStack(ModBlocks.SILVER_ORE), new ItemStack(ModItems.SILVER_DUST, 2)));
        recipes.add(crushing("ingotSilver", new ItemStack(ModItems.SILVER_INGOT), new ItemStack(ModItems.SILVER_DUST)));
        recipes.add(crushing("rawSilver", new ItemStack(ModItems.RAW_SILVER), new ItemStack(ModItems.SILVER_DUST, 2)));
        recipes.add(crushing("blockRawSilver", new ItemStack(ModBlocks.RAW_SILVER_BLOCK),
                new ItemStack(ModItems.SILVER_DUST, 18)));
        recipes.add(crushing("oreIesnium", new ItemStack(ModBlocks.IESNIUM_ORE),
                new ItemStack(ModItems.IESNIUM_DUST, 2)));
        recipes.add(crushing("ingotIesnium", new ItemStack(ModItems.IESNIUM_INGOT),
                new ItemStack(ModItems.IESNIUM_DUST)));
        recipes.add(crushing("rawIesnium", new ItemStack(ModItems.RAW_IESNIUM),
                new ItemStack(ModItems.IESNIUM_DUST, 2)));
        recipes.add(crushing("blockRawIesnium", new ItemStack(ModBlocks.RAW_IESNIUM_BLOCK),
                new ItemStack(ModItems.IESNIUM_DUST, 18)));
        recipes.add(crushing("cropDatura", new ItemStack(ModItems.DATURA), new ItemStack(ModItems.DATURA_SEEDS, 2)));
        recipes.add(crushing("rodBlaze", new ItemStack(Items.BLAZE_ROD), new ItemStack(Items.BLAZE_POWDER, 4)));
        recipes.add(crushing("bones", new ItemStack(Items.BONE), new ItemStack(Items.DYE, 4, 15)));
        recipes.add(crushing("oreRedstone", new ItemStack(Blocks.REDSTONE_ORE), new ItemStack(Items.REDSTONE, 4)));
        recipes.add(crushing("oreLapis", new ItemStack(Blocks.LAPIS_ORE), new ItemStack(ModItems.LAPIS_DUST, 9)));
        recipes.add(crushing("oreEmerald", new ItemStack(Blocks.EMERALD_ORE), new ItemStack(ModItems.EMERALD_DUST, 3)));
        recipes.add(crushing("obsidian", new ItemStack(Blocks.OBSIDIAN), new ItemStack(ModItems.OBSIDIAN_DUST)));
        recipes.add(crushing(new ItemStack(Blocks.END_STONE), new ItemStack(ModItems.CRUSHED_END_STONE)));
        recipes.add(crushing(new ItemStack(ModBlocks.OTHERROCK), new ItemStack(ModItems.CRUSHED_BLACKSTONE)));
        recipes.add(crushing(new ItemStack(Blocks.ICE), new ItemStack(ModItems.CRUSHED_ICE)));
        recipes.add(crushing(new ItemStack(Blocks.PACKED_ICE), new ItemStack(ModItems.CRUSHED_PACKED_ICE)));
        recipes.add(crushing("gemLapis", new ItemStack(Items.DYE, 1, 4), new ItemStack(ModItems.LAPIS_DUST)));
        recipes.add(crushing("gemEmerald", new ItemStack(Items.EMERALD), new ItemStack(ModItems.EMERALD_DUST)));
        recipes.add(crushing(amethystInputs(), new ItemStack(ModItems.AMETHYST_DUST)));
        recipes.add(crushing("enderpearl", new ItemStack(Items.ENDER_PEARL), new ItemStack(ModItems.ECHO_DUST)));
        // Upstream crushes otherstone/otherrock into their cobbled forms.
        recipes.add(crushing(new ItemStack(ModBlocks.OTHERSTONE), new ItemStack(ModBlocks.OTHERCOBBLESTONE)));
        recipes.add(crushing(new ItemStack(ModBlocks.OTHERROCK), new ItemStack(ModBlocks.OTHERCOBBLEROCK)));
        return Collections.unmodifiableList(recipes);
    }

    private static List<SpiritJobRecipe> createCrystallizeRecipes() {
        List<SpiritJobRecipe> recipes = new ArrayList<>();
        recipes.add(crystallize("dustObsidian", new ItemStack(ModItems.OBSIDIAN_DUST), new ItemStack(Blocks.OBSIDIAN)));
        recipes.add(crystallize("dustEndStone", new ItemStack(ModItems.CRUSHED_END_STONE),
                new ItemStack(Blocks.END_STONE)));
        recipes.add(crystallize("dustBlackstone", new ItemStack(ModItems.CRUSHED_BLACKSTONE),
                new ItemStack(ModBlocks.OTHERROCK)));
        recipes.add(crystallize("dustIce", new ItemStack(ModItems.CRUSHED_ICE), new ItemStack(Blocks.ICE)));
        recipes.add(crystallize("dustPackedIce", new ItemStack(ModItems.CRUSHED_PACKED_ICE),
                new ItemStack(Blocks.PACKED_ICE)));
        recipes.add(crystallize("dustLapis", new ItemStack(ModItems.LAPIS_DUST), new ItemStack(Items.DYE, 1, 4)));
        recipes.add(crystallize("dustEmerald", new ItemStack(ModItems.EMERALD_DUST), new ItemStack(Items.EMERALD)));
        recipes.add(crystallize("dustAmethyst", new ItemStack(ModItems.AMETHYST_DUST),
                new ItemStack(ModItems.SPIRIT_ATTUNED_GEM)));
        recipes.add(crystallize("dustEcho", new ItemStack(ModItems.ECHO_DUST), new ItemStack(Items.ENDER_PEARL)));
        recipes.add(crystallize("oreLapis", new ItemStack(Blocks.LAPIS_ORE), new ItemStack(Items.DYE, 6, 4)));
        recipes.add(crystallize("oreEmerald", new ItemStack(Blocks.EMERALD_ORE), new ItemStack(Items.EMERALD, 2)));
        recipes.add(crystallize("oreDiamond", new ItemStack(Blocks.DIAMOND_ORE), new ItemStack(Items.DIAMOND, 2)));
        recipes.add(crystallize("oreQuartz", new ItemStack(Blocks.QUARTZ_ORE), new ItemStack(Items.QUARTZ, 2)));
        recipes.add(crystallize(SpiritTier.MARID.getJobTier(), new ItemStack(ModBlocks.AMETHYST_BLOCK),
                new ItemStack(ModBlocks.BUDDING_AMETHYST)));
        return Collections.unmodifiableList(recipes);
    }

    private static List<ItemStack> amethystInputs() {
        return Arrays.asList(
                new ItemStack(ModItems.SPIRIT_ATTUNED_GEM),
                new ItemStack(ModBlocks.SPIRIT_ATTUNED_CRYSTAL),
                new ItemStack(ModBlocks.AMETHYST_BLOCK));
    }

    private static SpiritJobRecipe crushing(ItemStack input, ItemStack output) {
        return crushing(Collections.singletonList(input), output);
    }

    private static SpiritJobRecipe crushing(String oreName, ItemStack displayInput, ItemStack output) {
        return new SpiritJobRecipe(SpiritJobType.CRUSHER, Collections.singletonList(displayInput),
                Collections.singletonList(oreName), output);
    }

    private static SpiritJobRecipe crushing(List<ItemStack> inputs, ItemStack output) {
        return new SpiritJobRecipe(SpiritJobType.CRUSHER, inputs, Collections.emptyList(), output);
    }

    private static SpiritJobRecipe crystallize(ItemStack input, ItemStack output) {
        return new SpiritJobRecipe(SpiritJobType.CRYSTALLIZER, Collections.singletonList(input),
                Collections.emptyList(), output);
    }

    private static SpiritJobRecipe crystallize(int minimumTier, ItemStack input, ItemStack output) {
        return new SpiritJobRecipe(SpiritJobType.CRYSTALLIZER, Collections.singletonList(input),
                Collections.emptyList(), output, minimumTier);
    }

    private static SpiritJobRecipe crystallize(String oreName, ItemStack displayInput, ItemStack output) {
        return new SpiritJobRecipe(SpiritJobType.CRYSTALLIZER, Collections.singletonList(displayInput),
                Collections.singletonList(oreName), output);
    }

    private static List<SpiritJobRecipe> getMutableRecipes(SpiritJobType type) {
        if (type == SpiritJobType.CRUSHER) {
            return CRUSHING_RECIPES;
        }
        if (type == SpiritJobType.CRYSTALLIZER) {
            return CRYSTALLIZE_RECIPES;
        }
        throw new IllegalArgumentException("Unsupported spirit job recipe type: " + type);
    }

    private boolean matchesAny(Collection<ItemStack> stacks) {
        for (ItemStack stack : stacks) {
            if (this.matches(stack)) {
                return true;
            }
        }
        return false;
    }

    public boolean isAvailableTo(SpiritTier tier) {
        return this.minimumTier < 0 || (tier != null && tier.getJobTier() >= this.minimumTier);
    }

    private boolean hasAnyOutput(Collection<ItemStack> stacks) {
        for (ItemStack stack : stacks) {
            if (this.hasOutput(stack)) {
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
        return copies;
    }

    private static boolean matches(ItemStack expected, ItemStack actual) {
        if (expected.getItem() != actual.getItem()) {
            return false;
        }
        int metadata = expected.getMetadata();
        return metadata == OreDictionary.WILDCARD_VALUE || metadata == actual.getMetadata();
    }

    private static boolean matchesExact(ItemStack expected, ItemStack actual) {
        if (expected.isEmpty() || actual.isEmpty()) {
            return false;
        }
        return expected.getItem() == actual.getItem() && expected.getMetadata() == actual.getMetadata()
                && ItemStack.areItemStackTagsEqual(expected, actual);
    }

    private static boolean matchesOreName(String expectedOreName, ItemStack actual) {
        for (int id : OreDictionary.getOreIDs(actual)) {
            if (expectedOreName.equals(OreDictionary.getOreName(id))) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsMatchingInput(List<ItemStack> inputs, ItemStack stack) {
        for (ItemStack input : inputs) {
            if (matches(input, stack)) {
                return true;
            }
        }
        return false;
    }
}
