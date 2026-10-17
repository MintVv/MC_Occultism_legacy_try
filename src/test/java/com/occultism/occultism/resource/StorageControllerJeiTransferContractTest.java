package com.occultism.occultism.resource;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

/** Contract coverage for the 1.12 JEI storage-terminal recipe transfer. */
class StorageControllerJeiTransferContractTest {

    @Test
    void handlerUsesThe1_12TransferApiAndBoundsIngredientAlternatives() throws Exception {
        String handler = readSource("src", "main", "java", "com", "occultism", "occultism", "compat", "jei",
                "StorageControllerRecipeTransferHandler.java");
        assertTrue(handler.contains("IRecipeTransferHandler<ContainerStorageController>"));
        assertTrue(handler.contains("getGuiIngredients()"));
        assertTrue(handler.contains("MAX_ALTERNATIVES_PER_SLOT = 5"));
        assertTrue(handler.contains("ModNetworking.sendStorageRecipe(payload)"));
        assertTrue(handler.contains("ModNetworking.sendStorageRecipeById(recipeId)"));
        assertTrue(handler.contains("findRegisteredRecipeId(recipeLayout, CraftingManager.REGISTRY)"));
    }

    @Test
    void pluginRegistersStorageControllerForVanillaCrafting() throws Exception {
        String plugin = readSource("src", "main", "java", "com", "occultism", "occultism", "compat", "jei",
                "RitualJeiPlugin.java");
        assertTrue(plugin.contains("recipeTransferHandlerHelper()"));
        assertTrue(plugin.contains("getRecipeTransferRegistry()"));
        assertTrue(plugin.contains("new StorageControllerRecipeTransferHandler"));
        assertTrue(plugin.contains("VanillaRecipeCategoryUid.CRAFTING"));
    }

    @Test
    void transferErrorHasEnglishAndChineseText() throws Exception {
        String english = readSource("src", "main", "resources", "assets", "occultism", "lang", "en_us.lang");
        String chinese = readSource("src", "main", "resources", "assets", "occultism", "lang", "zh_cn.lang");
        assertTrue(english.contains("jei.occultism.error.recipe_no_items="));
        assertTrue(chinese.contains("jei.occultism.error.recipe_no_items="));
        assertTrue(english.contains("jei.occultism.error.recipe_too_large="));
        assertTrue(chinese.contains("jei.occultism.error.recipe_too_large="));
    }

    @Test
    void newRecipeIdPacketIsServerScheduledAndAppendedAfterExistingMessageIds() throws Exception {
        String networking = readSource("src", "main", "java", "com", "occultism", "occultism", "network",
                "ModNetworking.java");
        String message = readSource("src", "main", "java", "com", "occultism", "occultism", "network",
                "MessageSetRecipeByID.java");
        assertTrue(networking.indexOf("MessageSetRecipeByID.Handler.class")
                > networking.indexOf("MessageSetJumps.Handler.class"));
        assertTrue(message.contains("context.side != Side.SERVER"));
        assertTrue(message.contains("getServerWorld().addScheduledTask"));
        assertTrue(message.contains("player.openContainer instanceof ContainerStorageController"));
        assertTrue(message.contains("setRecipeById(recipeId, player)"));
    }

    private static String readSource(String... path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path[0], java.util.Arrays.copyOfRange(path, 1, path.length))),
                StandardCharsets.UTF_8);
    }
}
