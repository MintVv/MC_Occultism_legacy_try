package com.occultism.occultism.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.occultism.occultism.entity.EntityWonderingTrader;
import com.occultism.occultism.entity.WonderingTrades;
import com.occultism.occultism.registry.ModItems;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Bootstrap;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;

/** Contract coverage for the 1.12 Wondering Trader port. */
class WonderingTraderPortContractTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void entityUsesVillagerLifecycleAndKeepsBothTradeTables() {
        EntityWonderingTrader trader = new EntityWonderingTrader(null);

        assertTrue(trader instanceof EntityVillager);
        MerchantRecipeList common = trader.getCommonOffers();
        assertFalse(common.isEmpty());
        assertEquals(common, trader.getCommonOffers());
        MerchantRecipeList otherworld = trader.getOtherworldOffers();
        assertTrue(otherworld.size() >= 30);
        assertTrue(containsOutput(otherworld, ModItems.OTHERWORLD_GOGGLES));
        assertTrue(containsOutput(otherworld, ModItems.DIMENSIONAL_MATRIX));
        assertTrue(containsOutput(otherworld, ModItems.BOOK_OF_BINDING_BOUND_MARID));
        assertEquals(otherworld, trader.getOtherworldOffers());
    }

    @Test
    void ritualRegistrationAndRendererAreAppendedWithoutChangingPreviousIds() throws Exception {
        String rituals = readSource("src", "main", "resources", "assets", "occultism", "rituals", "rituals.json");
        String entities = readSource("src", "main", "java", "com", "occultism", "occultism", "registry",
                "ModEntities.java");
        String renderers = readSource("src", "main", "java", "com", "occultism", "occultism", "client",
                "render", "ModEntityRenderers.java");
        String trader = readSource("src", "main", "java", "com", "occultism", "occultism", "entity",
                "EntityWonderingTrader.java");

        assertTrue(rituals.contains("\"entity_to_summon\": \"occultism:wondering_trader\""));
        assertTrue(entities.contains("new ResourceLocation(Tags.MOD_ID, \"wondering_trader\")"));
        assertTrue(entities.contains("EntityWonderingTrader.class"));
        assertTrue(renderers.contains("registerEntityRenderingHandler(EntityWonderingTrader.class"));
        assertTrue(renderers.contains("return new RenderVillager(manager);"));
        assertTrue(trader.contains("ModPotions.THIRD_EYE"));
        assertTrue(trader.contains("ItemOtherworldGoggles.isGoggles"));
        assertTrue(trader.contains("IOtherworldBlock.isTrueSightStaff"));
        assertTrue(trader.contains("setRecipes(@Nullable MerchantRecipeList recipeList)"));
        assertTrue(trader.contains("CommonOffers"));
        assertTrue(trader.contains("OtherworldOffers"));
    }

    private static boolean containsOutput(MerchantRecipeList trades, net.minecraft.item.Item item) {
        for (MerchantRecipe trade : trades) {
            ItemStack output = trade.getItemToSell();
            if (!output.isEmpty() && output.getItem() == item) {
                return true;
            }
        }
        return false;
    }

    private static String readSource(String... path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path[0], java.util.Arrays.copyOfRange(path, 1, path.length))),
                StandardCharsets.UTF_8);
    }
}
