package com.occultism.occultism.entity;

import com.occultism.occultism.registry.ModBlocks;
import com.occultism.occultism.registry.ModItems;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;

/** Fixed 1.12 trade tables for {@link EntityWonderingTrader}. */
public final class WonderingTrades {

    private WonderingTrades() {
    }

    /** Mundane fallback offers shown without otherworld sight. */
    public static MerchantRecipeList createCommonTrades() {
        MerchantRecipeList trades = new MerchantRecipeList();
        trades.add(trade(new ItemStack(Items.WHEAT_SEEDS, 20), new ItemStack(Items.EMERALD), 16));
        trades.add(trade(new ItemStack(Items.ROTTEN_FLESH, 24), new ItemStack(Items.EMERALD), 16));
        trades.add(trade(new ItemStack(Items.COAL, 16), new ItemStack(Items.EMERALD), 16));
        trades.add(trade(new ItemStack(Items.PAPER, 24), new ItemStack(Items.EMERALD), 16));
        trades.add(trade(new ItemStack(Items.EMERALD), new ItemStack(Items.BREAD, 6), 12));
        trades.add(trade(new ItemStack(Items.EMERALD), new ItemStack(Items.APPLE, 3), 12));
        return trades;
    }

    /**
     * Occultism offers available when the player can see the otherworld.
     * Modern-only items are represented by existing 1.12 items or omitted
     * where no honest local equivalent exists.
     */
    public static MerchantRecipeList createOtherworldTrades() {
        MerchantRecipeList trades = new MerchantRecipeList();

        // Books and binding materials.
        trades.add(trade(new ItemStack(ModItems.OTHERWORLD_ESSENCE), new ItemStack(Items.BOOK), 12));
        trades.add(trade(new ItemStack(ModItems.OTHERWORLD_ESSENCE), new ItemStack(ModItems.TABOO_BOOK), 8));
        trades.add(trade(new ItemStack(ModItems.OTHERWORLD_ESSENCE, 2),
                new ItemStack(ModItems.BOOK_OF_BINDING_EMPTY), 8));
        trades.add(trade(new ItemStack(ModItems.OTHERWORLD_ESSENCE),
                new ItemStack(ModItems.BOOK_OF_BINDING_BOUND_FOLIOT), 4));
        trades.add(trade(new ItemStack(ModItems.OTHERWORLD_ESSENCE, 2),
                new ItemStack(ModItems.BOOK_OF_BINDING_BOUND_DJINNI), 4));
        trades.add(trade(new ItemStack(ModItems.OTHERWORLD_ESSENCE, 3),
                new ItemStack(ModItems.BOOK_OF_BINDING_BOUND_AFRIT), 3));
        trades.add(trade(new ItemStack(ModItems.OTHERWORLD_ESSENCE, 4),
                new ItemStack(ModItems.BOOK_OF_BINDING_BOUND_MARID), 2));

        // Paraphernalia and local materials.
        trades.add(trade(new ItemStack(Items.ROTTEN_FLESH, 10), block(ModBlocks.LARGE_CANDLE), 8));
        trades.add(trade(new ItemStack(Blocks.IRON_BLOCK), block(ModBlocks.SPIRIT_ATTUNED_CRYSTAL), 8));
        trades.add(trade(new ItemStack(Blocks.BONE_BLOCK, 4), new ItemStack(Items.SKULL, 4, 0), 8));
        trades.add(trade(new ItemStack(Items.COAL, 64), new ItemStack(Items.SKULL, 2, 1), 8));
        trades.add(trade(new ItemStack(Items.NETHER_WART, 7), new ItemStack(Items.BLAZE_POWDER, 4), 12));
        trades.add(trade(new ItemStack(ModItems.DATURA, 12), new ItemStack(ModItems.NATURE_PASTE), 24));
        trades.add(trade(new ItemStack(Blocks.GRAVEL, 4), new ItemStack(ModItems.GRAY_PASTE), 24));
        trades.add(trade(new ItemStack(Blocks.EMERALD_BLOCK), new ItemStack(ModItems.RESEARCH_FRAGMENT_DUST), 32));
        trades.add(trade(new ItemStack(Items.NETHER_STAR), new ItemStack(ModItems.WITHERITE_DUST), 16));
        trades.add(trade(new ItemStack(Items.SKULL, 1, 5), new ItemStack(ModItems.DRAGONYST_DUST), 16));
        trades.add(trade(new ItemStack(ModItems.DEMONIC_MEAT, 2),
                new ItemStack(ModItems.IESNIUM_INGOT, 3), 16));

        // Inventory and storage.
        trades.add(trade(new ItemStack(Items.CHEST_MINECART), new ItemStack(ModItems.SATCHEL), 16));
        trades.add(trade(new ItemStack(Items.ENDER_PEARL, 4), new ItemStack(ModItems.ENDER_SATCHEL), 16));
        trades.add(trade(new ItemStack(Items.GLOWSTONE_DUST, 2), new ItemStack(ModItems.RITUAL_SATCHEL_T1), 16));
        trades.add(trade(new ItemStack(ModItems.AFRIT_ESSENCE, 2), new ItemStack(ModItems.RITUAL_SATCHEL_T2), 8));
        trades.add(trade(new ItemStack(Items.SKULL, 1, 1), new ItemStack(ModItems.FAMILIAR_RING), 8));
        trades.add(trade(new ItemStack(ModItems.DEMONIC_MEAT, 2),
                new ItemStack(ModItems.STORAGE_REMOTE), 16));
        trades.add(trade(new ItemStack(Items.QUARTZ, 27), new ItemStack(ModItems.DIMENSIONAL_MATRIX), 16));
        trades.add(trade(new ItemStack(Items.GOLD_INGOT, 3), block(ModBlocks.STORAGE_CONTROLLER_BASE), 16));
        trades.add(trade(new ItemStack(Items.ENDER_EYE, 2), block(ModBlocks.STABLE_WORMHOLE), 8));
        trades.add(trade(new ItemStack(ModItems.COPPER_INGOT, 4), block(ModBlocks.STORAGE_STABILIZER_TIER1), 8));
        trades.add(trade(new ItemStack(ModItems.SPIRIT_ATTUNED_GEM, 8),
                block(ModBlocks.STORAGE_STABILIZER_TIER2), 8));
        trades.add(trade(new ItemStack(ModItems.AFRIT_ESSENCE, 4),
                block(ModBlocks.STORAGE_STABILIZER_TIER3), 4));
        trades.add(trade(new ItemStack(ModItems.MARID_ESSENCE, 5),
                block(ModBlocks.STORAGE_STABILIZER_TIER4), 2));

        // Utility and dimensional access.
        // Spyglass does not exist in 1.12; Ender Eye is the nearest sight item.
        trades.add(trade(new ItemStack(Items.ENDER_EYE), new ItemStack(ModItems.OTHERWORLD_GOGGLES), 8));
        trades.add(trade(new ItemStack(Items.END_CRYSTAL), new ItemStack(ModItems.VITALITY_COMPASS), 8));
        trades.add(trade(new ItemStack(Blocks.DIAMOND_BLOCK, 2), new ItemStack(ModItems.INFUSED_PICKAXE), 4));
        trades.add(trade(new ItemStack(ModItems.DEMONIC_MEAT, 5),
                new ItemStack(ModItems.IESNIUM_PICKAXE), 4));
        trades.add(trade(new ItemStack(ModItems.CURSED_HONEY, 8),
                block(ModBlocks.DIMENSIONAL_MINESHAFT), 4));
        trades.add(trade(new ItemStack(ModItems.CRUELTY_ESSENCE, 4),
                block(ModBlocks.DIMENSIONAL_BATTLEFIELD), 4));
        trades.add(trade(new ItemStack(ModItems.CRUELTY_ESSENCE, 2),
                block(ModBlocks.DIMENSIONAL_EXTRACTOR), 4));
        trades.add(trade(new ItemStack(ModItems.CURSED_HONEY, 8), new ItemStack(ModItems.MINER_FOLIOT_UNSPECIALIZED), 4));
        trades.add(trade(new ItemStack(ModItems.DEMONIC_MEAT, 9),
                new ItemStack(ModItems.MINER_DJINNI_ORES), 4));
        trades.add(trade(new ItemStack(ModItems.DEMONIC_MEAT, 64),
                new ItemStack(ModItems.MINER_AFRIT_DEEPS), 1));
        trades.add(trade(new ItemStack(ModItems.DRAGONYST_DUST), new ItemStack(Items.ELYTRA), 1));
        trades.add(trade(new ItemStack(Items.GOLDEN_APPLE, 1, 1), new ItemStack(Items.TOTEM_OF_UNDYING), 1));
        return trades;
    }

    private static MerchantRecipe trade(ItemStack input, ItemStack output, int maxUses) {
        return new MerchantRecipe(input, ItemStack.EMPTY, output, 0, maxUses);
    }

    private static ItemStack block(Block block) {
        return new ItemStack(Item.getItemFromBlock(block));
    }
}
