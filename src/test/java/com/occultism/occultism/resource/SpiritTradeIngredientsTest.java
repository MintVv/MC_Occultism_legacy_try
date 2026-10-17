package com.occultism.occultism.resource;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.occultism.occultism.entity.EntityFoliotJob;
import com.occultism.occultism.spirit.SpiritJobType;
import com.occultism.occultism.spirit.SpiritTradeIngredient;
import com.occultism.occultism.spirit.SpiritTradeRecipeRegistry;
import com.occultism.occultism.spirit.SpiritTradeRecipeRegistry.Recipe;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumHand;
import net.minecraftforge.oredict.OreDictionary;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SpiritTradeIngredientsTest {
    private static final String ID = "occultism_test:multi_cost";

    @BeforeAll
    static void bootstrap() {
        Bootstrap.register();
    }

    @AfterEach
    void removeRecipe() {
        SpiritTradeRecipeRegistry.remove(ID);
    }

    @Test
    void repeatedCostsNeedEnoughItemsAndReturnOnlyCompleteTrades() {
        Recipe recipe = recipe(cost(Items.IRON_INGOT), cost(Items.IRON_INGOT));
        ItemStack input = new ItemStack(Items.IRON_INGOT, 9);
        assertFalse(recipe.matches(new ItemStack(Items.IRON_INGOT)));
        assertTrue(recipe.matches(input));
        assertEquals(4, recipe.trade(input, 2).getCount()); // two gold per trade
        assertEquals(5, input.getCount());
        assertEquals(4, recipe.trade(input, 4).getCount());
        assertEquals(1, input.getCount());
        assertTrue(recipe.trade(input).isEmpty());
        assertEquals(1, input.getCount());
    }

    @Test
    void overlappingAlternativesDoNotStealANarrowerIngredientsOnlyInput() {
        Recipe recipe = recipe(cost(Items.IRON_INGOT, Items.GOLD_INGOT), cost(Items.IRON_INGOT));
        List<ItemStack> input = Arrays.asList(new ItemStack(Items.IRON_INGOT), new ItemStack(Items.GOLD_INGOT),
                new ItemStack(Items.DIAMOND, 3));
        assertTrue(recipe.isValid(input));
        List<ItemStack> remaining = recipe.consume(input);
        assertEquals(1, remaining.size());
        assertEquals(Items.DIAMOND, remaining.get(0).getItem());
        assertEquals(3, remaining.get(0).getCount());
        assertEquals(1, input.get(0).getCount());
        assertEquals(1, input.get(1).getCount());
        remaining.get(0).shrink(2);
        assertEquals(3, input.get(2).getCount(), "consume must deep-copy all leftovers");
    }

    @Test
    void oneIngredientConsumesOneUnitAcrossDuplicateStacks() {
        Recipe recipe = recipe(cost(Items.IRON_INGOT));
        List<ItemStack> input = Arrays.asList(new ItemStack(Items.IRON_INGOT, 2),
                new ItemStack(Items.IRON_INGOT, 3), new ItemStack(Items.IRON_INGOT));
        assertTrue(recipe.isValid(input));
        assertEquals(5, total(recipe.consume(input)));
        assertEquals(6, total(input));
    }

    @Test
    void insufficientMixedCostsLeaveAllCountsAndNbtUnchanged() {
        Recipe recipe = recipe(cost(Items.IRON_INGOT), cost(Items.GOLD_INGOT), cost(Items.GOLD_INGOT));
        ItemStack gold = new ItemStack(Items.GOLD_INGOT);
        NBTTagCompound data = new NBTTagCompound();
        data.setString("name", "kept");
        gold.setTagCompound(data);
        List<ItemStack> input = Arrays.asList(new ItemStack(Items.IRON_INGOT, 9), gold);
        assertFalse(recipe.isValid(input));
        List<ItemStack> result = recipe.consume(input);
        assertEquals(10, total(result));
        assertEquals("kept", result.get(1).getTagCompound().getString("name"));
        result.get(1).getTagCompound().setString("name", "changed");
        assertEquals("kept", gold.getTagCompound().getString("name"));
    }

    @Test
    void dynamicallyAddedOreMembersAreMatchedAndDisplayedForTheirOwnCost() {
        String ore = "occultismTradeTestMultiCost";
        OreDictionary.getOres(ore).clear();
        try {
            Recipe recipe = recipe(SpiritTradeIngredient.ore(ore), cost(Items.GOLD_INGOT));
            List<ItemStack> input = Arrays.asList(new ItemStack(Items.IRON_INGOT), new ItemStack(Items.GOLD_INGOT));
            assertFalse(recipe.isValid(input));
            assertTrue(recipe.getIngredients().get(0).getDisplayInputs().isEmpty());
            OreDictionary.registerOre(ore, Items.IRON_INGOT);
            assertTrue(recipe.isValid(input));
            assertTrue(recipe.consume(input).isEmpty());
            assertEquals(Items.IRON_INGOT, recipe.getIngredients().get(0).getDisplayInputs().get(0).getItem());
            assertEquals(Items.GOLD_INGOT, recipe.getIngredients().get(1).getDisplayInputs().get(0).getItem());
        } finally {
            OreDictionary.getOres(ore).clear();
        }
    }

    @Test
    void broadNbtCandidateCanBeReassignedToKeepTheTaggedCostSatisfied() {
        ItemStack tagged = new ItemStack(Items.IRON_INGOT);
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setBoolean("marked", true);
        tagged.setTagCompound(nbt);
        Recipe recipe = recipe(cost(Items.IRON_INGOT),
                SpiritTradeIngredient.items(Collections.singletonList(tagged)));
        assertTrue(recipe.isValid(Arrays.asList(tagged, new ItemStack(Items.IRON_INGOT))));
        assertFalse(recipe.isValid(Collections.singletonList(new ItemStack(Items.IRON_INGOT, 2))));
        assertTrue(recipe.consume(Arrays.asList(tagged, new ItemStack(Items.IRON_INGOT))).isEmpty());
        assertTrue(tagged.getTagCompound().getBoolean("marked"));
    }

    @Test
    void entityPickupBudgetIncludesEveryCostAndRespectsTheHeldStackLimit() throws Exception {
        Recipe recipe = recipe(cost(Items.IRON_INGOT), cost(Items.IRON_INGOT), cost(Items.IRON_INGOT));
        EntityFoliotJob trader = new EntityFoliotJob(null, SpiritJobType.TRADER_OTHERSTONE);
        trader.setTradeRecipeId(ID);
        Method pickup = EntityFoliotJob.class.getDeclaredMethod("getPickupCountForJob", ItemStack.class);
        pickup.setAccessible(true);
        Method lookup = EntityFoliotJob.class.getDeclaredMethod("findTradeResult", ItemStack.class);
        lookup.setAccessible(true);
        assertEquals(12, pickup.invoke(trader, new ItemStack(Items.IRON_INGOT, 64)));
        assertTrue(((ItemStack) lookup.invoke(trader, new ItemStack(Items.IRON_INGOT, 2))).isEmpty());
        assertEquals(2, ((ItemStack) lookup.invoke(trader, new ItemStack(Items.IRON_INGOT, 3))).getCount());
        trader.setMaxTradesPerRound(64);
        assertEquals(64, pickup.invoke(trader, new ItemStack(Items.IRON_INGOT, 64)));
        assertEquals(3, recipe.getIngredients().size());
    }

    @Test
    void mixedIngredientsAreSupportedByTheRecipeButNotPretendedToFitASingleHeldStack() {
        Recipe recipe = recipe(cost(Items.IRON_INGOT), cost(Items.GOLD_INGOT));
        ItemStack held = new ItemStack(Items.IRON_INGOT, 64);
        assertFalse(recipe.matches(held));
        assertTrue(recipe.trade(held).isEmpty());
        assertEquals(64, held.getCount());
        assertTrue(recipe.isValid(Arrays.asList(new ItemStack(Items.GOLD_INGOT), new ItemStack(Items.IRON_INGOT))));
    }

    @Test
    void partialTradeInputIsToppedUpFromInventoryWithoutOverwritingLeftovers() throws Exception {
        Recipe recipe = recipe(cost(Items.IRON_INGOT), cost(Items.IRON_INGOT), cost(Items.IRON_INGOT));
        EntityFoliotJob trader = new EntityFoliotJob(null, SpiritJobType.TRADER_OTHERSTONE);
        trader.setTradeRecipeId(ID);
        trader.setMaxTradesPerRound(1);
        trader.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(Items.IRON_INGOT));
        TileEntityChest chest = new TileEntityChest();
        chest.setInventorySlotContents(0, new ItemStack(Items.GOLD_INGOT, 10));
        chest.setInventorySlotContents(1, new ItemStack(Items.IRON_INGOT, 8));
        Method extract = EntityFoliotJob.class.getDeclaredMethod("extractFirstProcessableStack", TileEntity.class);
        extract.setAccessible(true);
        Method merge = EntityFoliotJob.class.getDeclaredMethod("mergePickedTradeInput", ItemStack.class);
        merge.setAccessible(true);
        ItemStack picked = (ItemStack) extract.invoke(trader, chest);
        assertEquals(2, picked.getCount(), "only top up the remaining one-trade capacity");
        assertEquals(Items.IRON_INGOT, picked.getItem());
        merge.invoke(trader, picked);
        assertEquals(3, trader.getHeldItemMainhand().getCount());
        assertEquals(6, chest.getStackInSlot(1).getCount());
        assertEquals(10, chest.getStackInSlot(0).getCount());
        assertEquals(2, recipe.trade(trader.getHeldItemMainhand(), 1).getCount());
        assertTrue(trader.getHeldItemMainhand().isEmpty());
    }

    @Test
    void recipeCostLimitAndNullsAreValidatedBeforeReplacingAnExistingRecipe() {
        Recipe original = recipe(cost(Items.IRON_INGOT));
        List<SpiritTradeIngredient> tooMany = Collections.nCopies(10, cost(Items.IRON_INGOT));
        assertThrows(IllegalArgumentException.class, () -> SpiritTradeRecipeRegistry.registerIngredients(ID,
                SpiritJobType.TRADER_OTHERSTONE, tooMany, new ItemStack(Items.GOLD_INGOT), 2, 4));
        assertThrows(IllegalArgumentException.class, () -> SpiritTradeRecipeRegistry.registerIngredients(ID,
                SpiritJobType.TRADER_OTHERSTONE, Collections.emptyList(), new ItemStack(Items.GOLD_INGOT), 2, 4));
        assertThrows(IllegalArgumentException.class, () -> SpiritTradeRecipeRegistry.registerIngredients(ID,
                SpiritJobType.TRADER_OTHERSTONE, Arrays.asList(cost(Items.IRON_INGOT), null),
                new ItemStack(Items.GOLD_INGOT), 2, 4));
        assertSame(original, SpiritTradeRecipeRegistry.find(ID));
        assertThrows(IllegalArgumentException.class, () -> original.isValid(null));
        assertThrows(IllegalArgumentException.class, () -> original.consume(Arrays.asList((ItemStack) null)));
        assertFalse(original.isValid(Collections.emptyList()));
    }

    @Test
    void nineCostMatchingUsesCountsWithoutExpandingAnOversizedInventoryStack() {
        Recipe recipe = recipe(Collections.nCopies(9, cost(Items.IRON_INGOT)).toArray(new SpiritTradeIngredient[0]));
        ItemStack huge = new ItemStack(Items.IRON_INGOT, Integer.MAX_VALUE);
        assertTrue(recipe.isValid(Collections.singletonList(huge)));
        assertEquals(Integer.MAX_VALUE - 9, recipe.consume(Collections.singletonList(huge)).get(0).getCount());
        assertEquals(Integer.MAX_VALUE, huge.getCount());
    }

    @Test
    void matcherAgreesWithExhaustiveCountAssignmentForSmallOverlappingRecipes() {
        Item[] types = {Items.IRON_INGOT, Items.GOLD_INGOT};
        // 3 choices per cost: iron, gold, or either; cover all 27 three-cost combinations.
        for (int code = 0; code < 27; code++) {
            int value = code;
            List<SpiritTradeIngredient> costs = new ArrayList<>();
            int[] masks = new int[3];
            for (int index = 0; index < 3; index++) {
                masks[index] = value % 3 + 1;
                value /= 3;
                costs.add(masks[index] == 3 ? cost(types) : cost(types[masks[index] - 1]));
            }
            Recipe recipe = recipe(costs.toArray(new SpiritTradeIngredient[0]));
            for (int iron = 0; iron <= 3; iron++) {
                for (int gold = 0; gold <= 3; gold++) {
                    List<ItemStack> stacks = Arrays.asList(new ItemStack(types[0], iron),
                            new ItemStack(types[1], gold), new ItemStack(Items.DIAMOND, 2));
                    boolean expected = canAssign(masks, 0, iron, gold);
                    assertEquals(expected, recipe.isValid(stacks), "recipe " + code + ", " + iron + "/" + gold);
                    assertEquals(iron + gold + 2 - (expected ? 3 : 0), total(recipe.consume(stacks)));
                }
            }
        }
    }

    private static boolean canAssign(int[] masks, int index, int iron, int gold) {
        if (index == masks.length) {
            return true;
        }
        return iron > 0 && (masks[index] & 1) != 0 && canAssign(masks, index + 1, iron - 1, gold)
                || gold > 0 && (masks[index] & 2) != 0 && canAssign(masks, index + 1, iron, gold - 1);
    }

    private static SpiritTradeIngredient cost(Item... items) {
        List<ItemStack> alternatives = new ArrayList<>();
        for (Item item : items) {
            alternatives.add(new ItemStack(item));
        }
        return SpiritTradeIngredient.items(alternatives);
    }

    private static Recipe recipe(SpiritTradeIngredient... costs) {
        return SpiritTradeRecipeRegistry.registerIngredients(ID, SpiritJobType.TRADER_OTHERSTONE,
                Arrays.asList(costs), new ItemStack(Items.GOLD_INGOT, 2), 5, 4);
    }

    private static int total(List<ItemStack> stacks) {
        return stacks.stream().mapToInt(ItemStack::getCount).sum();
    }
}
