package com.occultism.occultism.ritual;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class RitualIngredientMatcherTest {
    @BeforeAll
    static void bootstrap() { Bootstrap.register(); }

    @Test
    void broadAlternativesAreAssignedWithoutStealingAnExactCostsOnlyItem() {
        List<RitualIngredient> costs = Arrays.asList(either(), iron());
        List<ItemStack> input = Arrays.asList(new ItemStack(Items.IRON_INGOT), new ItemStack(Items.GOLD_INGOT));
        assertArrayEquals(new int[] {1, 0}, RitualIngredientMatcher.assign(costs, input));
        assertEquals(1, input.get(0).getCount());
        assertEquals(1, input.get(1).getCount());
        assertNull(RitualIngredientMatcher.assign(costs, Collections.singletonList(new ItemStack(Items.IRON_INGOT))));
    }

    @Test
    void repeatedCostsCanShareOneBowlButNeverDoubleSpendItsLastUnit() {
        List<RitualIngredient> costs = Arrays.asList(iron(), iron(), iron());
        ItemStack input = new ItemStack(Items.IRON_INGOT, 3);
        assertArrayEquals(new int[] {0, 0, 0}, RitualIngredientMatcher.assign(costs, Collections.singletonList(input)));
        assertEquals(3, input.getCount());
        input.setCount(2);
        assertNull(RitualIngredientMatcher.assign(costs, Collections.singletonList(input)));
        input.setCount(Integer.MAX_VALUE);
        assertNotNull(RitualIngredientMatcher.assign(costs, Collections.singletonList(input)),
                "large input counts must not cause full-stack expansion");
    }

    @Test
    void metadataWildcardsAndDynamicOreMembersRemainEligible() {
        RitualIngredient dye = new RitualIngredient(Items.DYE, 1, OreDictionary.WILDCARD_VALUE);
        RitualIngredient lapis = new RitualIngredient(Items.DYE, 1, 4);
        assertArrayEquals(new int[] {1, 0}, RitualIngredientMatcher.assign(Arrays.asList(dye, lapis),
                Arrays.asList(new ItemStack(Items.DYE, 1, 4), new ItemStack(Items.DYE, 1, 0))));
        String ore = "occultismRitualMatcherTest";
        OreDictionary.getOres(ore).clear();
        try {
            RitualIngredient member = new RitualIngredient(ore, 1);
            List<ItemStack> input = Arrays.asList(new ItemStack(Items.IRON_INGOT), new ItemStack(Items.GOLD_INGOT));
            assertNull(RitualIngredientMatcher.assign(Arrays.asList(member, iron()), input));
            OreDictionary.registerOre(ore, Items.GOLD_INGOT);
            assertArrayEquals(new int[] {1, 0}, RitualIngredientMatcher.assign(Arrays.asList(member, iron()), input));
        } finally {
            OreDictionary.getOres(ore).clear();
        }
    }

    @Test
    void countedScriptCostsKeepTheirExistingWholeBowlConsumptionAndDoNotMutateInputs() {
        RitualIngredient twoIron = new RitualIngredient(Items.IRON_INGOT, 2);
        ItemStack input = new ItemStack(Items.IRON_INGOT, 3);
        assertArrayEquals(new int[] {0, 0}, RitualIngredientMatcher.assign(Arrays.asList(twoIron, iron()),
                Collections.singletonList(input)));
        assertEquals(3, input.getCount());
        input.setCount(2);
        assertNull(RitualIngredientMatcher.assign(Arrays.asList(twoIron, iron()), Collections.singletonList(input)));
        assertNotNull(RitualIngredientMatcher.assign(Collections.nCopies(33, iron()),
                Collections.singletonList(new ItemStack(Items.IRON_INGOT, 33))),
                "larger scripted recipes retain their previous fallback rather than becoming unsupported");
    }

    @Test
    void emptyAndInvalidCostsFailSafely() {
        assertArrayEquals(new int[0], RitualIngredientMatcher.assign(Collections.emptyList(), Collections.emptyList()));
        assertNull(RitualIngredientMatcher.assign(Collections.singletonList(iron()), Collections.emptyList()));
        assertNull(RitualIngredientMatcher.assign(Collections.singletonList(iron()), null));
        assertNull(RitualIngredientMatcher.assign(Collections.singletonList(new RitualIngredient(Items.IRON_INGOT, 0)),
                Collections.singletonList(new ItemStack(Items.IRON_INGOT))));
        assertNull(RitualIngredientMatcher.assign(Collections.singletonList(new RitualIngredient(Items.IRON_INGOT, -1)),
                Collections.singletonList(new ItemStack(Items.IRON_INGOT))));
        assertNull(RitualIngredientMatcher.assign(Collections.singletonList(new RitualIngredient(Collections.emptyList())),
                Collections.singletonList(new ItemStack(Items.IRON_INGOT))));
    }

    @Test
    void allBundledRitualsUseTheOptimizedUnitCostPath() throws Exception {
        String json = new String(Files.readAllBytes(Paths.get(
                "src/main/resources/assets/occultism/rituals/rituals.json")), StandardCharsets.UTF_8);
        int count = 0;
        for (JsonElement element : new JsonParser().parse(json).getAsJsonArray()) {
            assertTrue(element.getAsJsonObject().getAsJsonArray("ingredients").size() <= 32);
            for (JsonElement ingredient : element.getAsJsonObject().getAsJsonArray("ingredients")) {
                assertUnitIngredient(ingredient);
            }
            count++;
        }
        assertEquals(170, count);
    }

    @Test
    void matchesAndSelectedConsumptionAgreeForExhaustiveOverlappingUnitCosts() {
        for (int code = 0; code < 27; code++) {
            int value = code;
            int[] masks = new int[3];
            List<RitualIngredient> costs = new ArrayList<>();
            for (int index = 0; index < masks.length; index++) {
                masks[index] = value % 3 + 1;
                value /= 3;
                costs.add(masks[index] == 3 ? either() : new RitualIngredient(
                        masks[index] == 1 ? Items.IRON_INGOT : Items.GOLD_INGOT, 1));
            }
            for (int iron = 0; iron <= 3; iron++) {
                for (int gold = 0; gold <= 3; gold++) {
                    List<ItemStack> input = Arrays.asList(new ItemStack(Items.IRON_INGOT, iron),
                            new ItemStack(Items.GOLD_INGOT, gold));
                    int[] plan = RitualIngredientMatcher.assign(costs, input);
                    assertEquals(canAssign(masks, 0, iron, gold), plan != null,
                            "recipe " + code + ", input " + iron + "/" + gold);
                    if (plan != null) {
                        List<ItemStack> copies = Arrays.asList(input.get(0).copy(), input.get(1).copy());
                        for (int cost = 0; cost < plan.length; cost++) {
                            assertTrue(costs.get(cost).matches(copies.get(plan[cost])));
                            costs.get(cost).consume(copies.get(plan[cost]));
                        }
                        assertEquals(iron + gold - 3, copies.get(0).getCount() + copies.get(1).getCount());
                    }
                    assertEquals(iron, input.get(0).getCount());
                    assertEquals(gold, input.get(1).getCount());
                }
            }
        }
    }

    private static RitualIngredient iron() { return new RitualIngredient(Items.IRON_INGOT, 1); }

    private static RitualIngredient either() {
        return new RitualIngredient(Arrays.asList(iron(), new RitualIngredient(Items.GOLD_INGOT, 1)));
    }

    private static void assertUnitIngredient(JsonElement ingredient) {
        if (ingredient.isJsonArray()) {
            for (JsonElement alternative : ingredient.getAsJsonArray()) {
                assertUnitIngredient(alternative);
            }
        } else if (ingredient.isJsonObject() && ingredient.getAsJsonObject().has("count")) {
            assertEquals(1, ingredient.getAsJsonObject().get("count").getAsInt());
        }
    }

    private static boolean canAssign(int[] masks, int index, int iron, int gold) {
        if (index == masks.length) {
            return true;
        }
        return iron > 0 && (masks[index] & 1) != 0 && canAssign(masks, index + 1, iron - 1, gold)
                || gold > 0 && (masks[index] & 2) != 0 && canAssign(masks, index + 1, iron, gold - 1);
    }
}
