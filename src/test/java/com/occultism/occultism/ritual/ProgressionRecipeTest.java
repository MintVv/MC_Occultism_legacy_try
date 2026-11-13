package com.occultism.occultism.ritual;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.occultism.occultism.registry.ModItems;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.JsonContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ProgressionRecipeTest {
    private static final Path RECIPES = Path.of("src/main/resources/assets/occultism/recipes");

    @BeforeAll
    static void bootstrap() throws Exception {
        RitualBundleLoadTest.bootstrap();
    }

    @Test
    void crusherStartsWithTheFourUpstreamIngots() throws Exception {
        Path bundle = Path.of("src/main/resources/assets/occultism/rituals/rituals.json");
        for (JsonElement element : new JsonParser().parse(Files.readString(bundle)).getAsJsonArray()) {
            JsonObject ritual = element.getAsJsonObject();
            if (!"summon_foliot_crusher".equals(ritual.get("id").getAsString())) {
                continue;
            }
            Set<String> costs = new HashSet<>();
            ritual.getAsJsonArray("ingredients").forEach(cost -> costs.add(cost.getAsString()));
            assertEquals(Set.of("#c:ingots/iron", "#c:ingots/gold", "#c:ingots/copper", "#c:ingots/silver"), costs);
            return;
        }
        fail("Missing foliot crusher ritual");
    }

    @Test
    void compatibilityRecipesCraftRealOutputsAndRetainTheirCosts() throws Exception {
        for (String id : List.of("chalk_red_impure", "chalk_orange_impure", "chalk_cyan_impure",
                "chalk_blue_impure", "netherite_scrap_dust", "netherite_dust")) {
            JsonObject json = read(id);
            IRecipe recipe = CraftingHelper.getRecipe(json, new JsonContext("occultism"));
            InventoryCrafting matrix = matrix();
            int slot = 0;
            for (JsonElement entry : json.getAsJsonArray("ingredients")) {
                JsonObject input = entry.getAsJsonObject();
                Item item = Item.REGISTRY.getObject(new ResourceLocation(input.get("item").getAsString()));
                assertNotNull(item);
                assertNotEquals(Items.AIR, item);
                matrix.setInventorySlotContents(slot++, new ItemStack(item, 1,
                        input.has("data") ? input.get("data").getAsInt() : 0));
            }
            assertTrue(recipe.matches(matrix, null), id);
            assertEquals(id, recipe.getCraftingResult(matrix).getItem().getRegistryName().getPath());
            assertEquals(1, recipe.getCraftingResult(matrix).getCount());
            matrix.setInventorySlotContents(1, ItemStack.EMPTY);
            assertFalse(recipe.matches(matrix, null), id + " must not craft without its second cost");
        }
    }

    @Test
    void amethystBlockHasAnExecutableFourDustRecipe() throws Exception {
        IRecipe recipe = CraftingHelper.getRecipe(read("amethyst_block"), new JsonContext("occultism"));
        InventoryCrafting matrix = matrix();
        for (int slot : new int[] {0, 1, 3, 4}) {
            matrix.setInventorySlotContents(slot, new ItemStack(ModItems.AMETHYST_DUST));
        }
        assertTrue(recipe.matches(matrix, null));
        assertEquals("occultism:amethyst_block", recipe.getCraftingResult(matrix).getItem().getRegistryName().toString());
        matrix.setInventorySlotContents(4, ItemStack.EMPTY);
        assertFalse(recipe.matches(matrix, null));
    }

    @Test
    void inkMetadataIsNotInterchangeableWithBoneMeal() throws Exception {
        IRecipe recipe = CraftingHelper.getRecipe(read("chalk_cyan_impure"), new JsonContext("occultism"));
        InventoryCrafting matrix = matrix();
        matrix.setInventorySlotContents(0, new ItemStack(ModItems.CHALK_WHITE_IMPURE));
        matrix.setInventorySlotContents(1, new ItemStack(ModItems.IESNIUM_DUST));
        matrix.setInventorySlotContents(2, new ItemStack(ModItems.ECHO_DUST));
        matrix.setInventorySlotContents(3, new ItemStack(Items.DYE, 1, 15));
        matrix.setInventorySlotContents(4, new ItemStack(Items.GLOWSTONE_DUST));
        assertFalse(recipe.matches(matrix, null));
        matrix.setInventorySlotContents(3, new ItemStack(Items.DYE, 1, 0));
        assertTrue(recipe.matches(matrix, null));
    }

    @Test
    void newSourcesBootstrapWithoutAnyMinerOrMissingColor() throws Exception {
        List<Edge> edges = new ArrayList<>();
        for (String id : List.of("chalk_red_impure", "chalk_orange_impure", "chalk_cyan_impure",
                "chalk_blue_impure", "netherite_scrap_dust", "netherite_dust", "raw_silver_block", "raw_silver_from_block")) {
            JsonObject json = read(id);
            List<Set<String>> costs = new ArrayList<>();
            Iterable<JsonElement> ingredients = json.has("ingredients") ? json.getAsJsonArray("ingredients")
                    : json.getAsJsonObject("key").entrySet().stream().map(java.util.Map.Entry::getValue).toList();
            for (JsonElement input : ingredients) {
                costs.add(Set.of(input.getAsJsonObject().get("item").getAsString()));
            }
            edges.add(new Edge(json.getAsJsonObject("result").get("item").getAsString(), costs));
        }
        Set<String> seeds = new HashSet<>(Set.of("occultism:chalk_white_impure", "occultism:afrit_essence",
                "occultism:marid_essence", "occultism:cursed_honey", "occultism:iesnium_dust",
                "occultism:echo_dust", "occultism:lapis_dust", "occultism:obsidian_dust", "occultism:gold_dust"));
        Set<String> reachable = expand(edges, seeds);
        for (String id : List.of("chalk_red_impure", "chalk_orange_impure", "chalk_cyan_impure",
                "chalk_blue_impure", "netherite_scrap_dust", "netherite_dust")) {
            assertTrue(reachable.contains("occultism:" + id), id);
        }
        assertFalse(reachable.contains("occultism:raw_silver"), "Packing and unpacking cannot create the first raw silver");
        edges.removeIf(edge -> edge.output().equals("occultism:netherite_scrap_dust"));
        assertFalse(expand(edges, seeds).contains("occultism:netherite_dust"), "A removed scrap source must break the chain");
    }

    @Test
    void essenceAndDustGatesArePreserved() throws Exception {
        assertTrue(costs("chalk_red_impure").contains("occultism:afrit_essence"));
        assertTrue(costs("chalk_blue_impure").contains("occultism:marid_essence"));
        assertTrue(costs("chalk_cyan_impure").containsAll(Set.of("occultism:iesnium_dust", "occultism:echo_dust")));
        assertTrue(costs("chalk_orange_impure").contains("occultism:cursed_honey"));
    }

    private static Set<String> costs(String id) throws Exception {
        Set<String> result = new HashSet<>();
        read(id).getAsJsonArray("ingredients").forEach(input -> result.add(input.getAsJsonObject().get("item").getAsString()));
        return result;
    }

    private static Set<String> expand(List<Edge> edges, Set<String> seeds) {
        Set<String> reached = new HashSet<>(seeds);
        boolean changed;
        do {
            changed = false;
            for (Edge edge : edges) {
                if (edge.costs().stream().allMatch(cost -> cost.stream().anyMatch(item ->
                        item.startsWith("minecraft:") || reached.contains(item)))) {
                    changed |= reached.add(edge.output());
                }
            }
        } while (changed);
        return reached;
    }

    private record Edge(String output, List<Set<String>> costs) { }

    private static JsonObject read(String id) throws Exception {
        return new JsonParser().parse(Files.readString(RECIPES.resolve(id + ".json"), StandardCharsets.UTF_8)).getAsJsonObject();
    }

    private static InventoryCrafting matrix() {
        return new InventoryCrafting(new Container() {
            @Override
            public boolean canInteractWith(EntityPlayer player) {
                return true;
            }
        }, 3, 3);
    }
}
