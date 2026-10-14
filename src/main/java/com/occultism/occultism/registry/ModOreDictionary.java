package com.occultism.occultism.registry;

import net.minecraft.item.ItemStack;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraftforge.oredict.OreDictionary;

public final class ModOreDictionary {

    private ModOreDictionary() {
    }

    public static void register() {
        OreDictionary.registerOre("oreSilver", ModBlocks.SILVER_ORE);
        OreDictionary.registerOre("oreCopper", ModBlocks.COPPER_ORE);
        OreDictionary.registerOre("oreIesnium", ModBlocks.IESNIUM_ORE);
        OreDictionary.registerOre("blockCopper", ModBlocks.COPPER_BLOCK);
        OreDictionary.registerOre("blockCoal", Blocks.COAL_BLOCK);
        OreDictionary.registerOre("blockDiamond", Blocks.DIAMOND_BLOCK);
        OreDictionary.registerOre("blockGold", Blocks.GOLD_BLOCK);
        OreDictionary.registerOre("blockIron", Blocks.IRON_BLOCK);
        OreDictionary.registerOre("blockQuartz", Blocks.QUARTZ_BLOCK);
        OreDictionary.registerOre("blockAmethyst", ModBlocks.AMETHYST_BLOCK);
        OreDictionary.registerOre("blockSilver", ModBlocks.SILVER_BLOCK);
        OreDictionary.registerOre("blockIesnium", ModBlocks.IESNIUM_BLOCK);
        OreDictionary.registerOre("blockNetherite", ModBlocks.IESNIUM_BLOCK);
        OreDictionary.registerOre("blockRawSilver", ModBlocks.RAW_SILVER_BLOCK);
        OreDictionary.registerOre("blockRawIesnium", ModBlocks.RAW_IESNIUM_BLOCK);

        OreDictionary.registerOre("ingotCopper", ModItems.COPPER_INGOT);
        OreDictionary.registerOre("ingotIron", Items.IRON_INGOT);
        OreDictionary.registerOre("ingotGold", Items.GOLD_INGOT);
        OreDictionary.registerOre("ingotSilver", ModItems.SILVER_INGOT);
        OreDictionary.registerOre("nuggetCopper", ModItems.COPPER_NUGGET);
        OreDictionary.registerOre("nuggetGold", Items.GOLD_NUGGET);
        OreDictionary.registerOre("ingotIesnium", ModItems.IESNIUM_INGOT);
        OreDictionary.registerOre("nuggetSilver", ModItems.SILVER_NUGGET);
        OreDictionary.registerOre("nuggetIesnium", ModItems.IESNIUM_NUGGET);
        OreDictionary.registerOre("dustCopper", ModItems.COPPER_DUST);
        OreDictionary.registerOre("dustSilver", ModItems.SILVER_DUST);
        OreDictionary.registerOre("dustIesnium", ModItems.IESNIUM_DUST);
        OreDictionary.registerOre("dustNetheriteScrap", ModItems.NETHERITE_SCRAP_DUST);
        OreDictionary.registerOre("dustNetherite", ModItems.NETHERITE_DUST);
        OreDictionary.registerOre("dustObsidian", ModItems.OBSIDIAN_DUST);
        OreDictionary.registerOre("dustLapis", ModItems.LAPIS_DUST);
        OreDictionary.registerOre("dustAmethyst", ModItems.AMETHYST_DUST);
        OreDictionary.registerOre("dustEmerald", ModItems.EMERALD_DUST);
        OreDictionary.registerOre("dustEcho", ModItems.ECHO_DUST);
        OreDictionary.registerOre("dustIron", ModItems.IRON_DUST);
        OreDictionary.registerOre("dustGold", ModItems.GOLD_DUST);
        OreDictionary.registerOre("dustOtherstone", ModItems.BURNT_OTHERSTONE);
        OreDictionary.registerOre("dustOtherrock", ModItems.BURNT_OTHERROCK);
        OreDictionary.registerOre("dustCalcite", ModItems.CRUSHED_CALCITE);
        OreDictionary.registerOre("dustBlackstone", ModItems.CRUSHED_BLACKSTONE);
        OreDictionary.registerOre("dustIce", ModItems.CRUSHED_ICE);
        OreDictionary.registerOre("dustPackedIce", ModItems.CRUSHED_PACKED_ICE);
        OreDictionary.registerOre("dustBlueIce", ModItems.CRUSHED_BLUE_ICE);
        OreDictionary.registerOre("dustGlowstone", Items.GLOWSTONE_DUST);

        OreDictionary.registerOre("stoneOtherworld", ModBlocks.OTHERSTONE);
        OreDictionary.registerOre("stoneOtherworld", ModBlocks.OTHERROCK);
        OreDictionary.registerOre("stoneOtherworld", ModBlocks.POLISHED_OTHERSTONE);
        OreDictionary.registerOre("stoneOtherworld", ModBlocks.POLISHED_OTHERROCK);
        OreDictionary.registerOre("stoneOtherworld", ModBlocks.OTHERSTONE_BRICKS);
        OreDictionary.registerOre("stoneOtherworld", ModBlocks.OTHERROCK_BRICKS);
        OreDictionary.registerOre("cropDatura", ModItems.DATURA);
        OreDictionary.registerOre("cropBeetroot", Items.BEETROOT);
        OreDictionary.registerOre("cropCarrot", Items.CARROT);
        OreDictionary.registerOre("cropPotato", Items.POTATO);
        OreDictionary.registerOre("cropWheat", Items.WHEAT);
        OreDictionary.registerOre("seedDatura", ModItems.DATURA_SEEDS);
        OreDictionary.registerOre("logOtherworld", ModBlocks.OTHERWORLD_LOG);
        OreDictionary.registerOre("logOtherworld", ModBlocks.OTHERWORLD_WOOD);
        OreDictionary.registerOre("logOtherworld", ModBlocks.STRIPPED_OTHERWORLD_LOG);
        OreDictionary.registerOre("logOtherworld", ModBlocks.STRIPPED_OTHERWORLD_WOOD);
        OreDictionary.registerOre("plankWood", ModBlocks.OTHERPLANKS);
        OreDictionary.registerOre("treeLeaves", new ItemStack(Blocks.LEAVES, 1, OreDictionary.WILDCARD_VALUE));
        OreDictionary.registerOre("treeLeaves", new ItemStack(Blocks.LEAVES2, 1, OreDictionary.WILDCARD_VALUE));
        OreDictionary.registerOre("treeLeaves", ModBlocks.OTHERWORLD_LEAVES);
        OreDictionary.registerOre("treeSapling", ModBlocks.OTHERWORLD_SAPLING_NATURAL);
        OreDictionary.registerOre("treeSapling", ModBlocks.OTHERWORLD_SAPLING);
        OreDictionary.registerOre("treeSapling", new ItemStack(Blocks.SAPLING, 1, OreDictionary.WILDCARD_VALUE));
        OreDictionary.registerOre("saplings", new ItemStack(Blocks.SAPLING, 1, OreDictionary.WILDCARD_VALUE));
        OreDictionary.registerOre("flowerOtherworld", ModBlocks.OTHERFLOWER);
        OreDictionary.registerOre("blockGlass", ModBlocks.OTHERGLASS_NATURAL);
        OreDictionary.registerOre("gemSoul", new ItemStack(ModItems.SOUL_GEM, 1, OreDictionary.WILDCARD_VALUE));
        OreDictionary.registerOre("gemQuartz", Items.QUARTZ);
        OreDictionary.registerOre("gemDiamond", Items.DIAMOND);
        OreDictionary.registerOre("gems", Items.QUARTZ);
        OreDictionary.registerOre("gems", Items.DIAMOND);
        OreDictionary.registerOre("enderpearl", Items.ENDER_PEARL);
        OreDictionary.registerOre("rodWooden", Items.STICK);
        OreDictionary.registerOre("leather", Items.LEATHER);
        OreDictionary.registerOre("leathers", Items.LEATHER);
        OreDictionary.registerOre("feather", Items.FEATHER);
        OreDictionary.registerOre("feathers", Items.FEATHER);
        OreDictionary.registerOre("string", Items.STRING);
        OreDictionary.registerOre("egg", Items.EGG);
        OreDictionary.registerOre("wool", new ItemStack(Blocks.WOOL, 1, OreDictionary.WILDCARD_VALUE));
        OreDictionary.registerOre("blockGlass", Blocks.GLASS);
        OreDictionary.registerOre("gemPrismarine", new ItemStack(Items.DYE, 1, 6));
        OreDictionary.registerOre("gemLapis", new ItemStack(Items.DYE, 1, 4));
        OreDictionary.registerOre("gemAmethyst", ModItems.SPIRIT_ATTUNED_GEM);
        OreDictionary.registerOre("gemEmerald", Items.EMERALD);
        OreDictionary.registerOre("gems", new ItemStack(Items.DYE, 1, 6));
        OreDictionary.registerOre("gems", new ItemStack(Items.DYE, 1, 4));
        OreDictionary.registerOre("gems", ModItems.SPIRIT_ATTUNED_GEM);
        OreDictionary.registerOre("gems", Items.EMERALD);
        OreDictionary.registerOre("dyeGreen", new ItemStack(Items.DYE, 1, 2));
        OreDictionary.registerOre("dyeBlue", new ItemStack(Items.DYE, 1, 4));
        OreDictionary.registerOre("dyeRed", new ItemStack(Items.DYE, 1, 1));
        OreDictionary.registerOre("dyeGray", new ItemStack(Items.DYE, 1, 8));
        OreDictionary.registerOre("dyeYellow", new ItemStack(Items.DYE, 1, 11));
        OreDictionary.registerOre("gunpowder", Items.GUNPOWDER);
        OreDictionary.registerOre("slimeball", Items.SLIME_BALL);
        OreDictionary.registerOre("slimeballs", Items.SLIME_BALL);
        OreDictionary.registerOre("slime_balls", Items.SLIME_BALL);
        OreDictionary.registerOre("chests", Blocks.CHEST);
        OreDictionary.registerOre("chestEnder", Blocks.ENDER_CHEST);
        OreDictionary.registerOre("soulFireBaseBlock", Blocks.SOUL_SAND);
        OreDictionary.registerOre("magma", Blocks.MAGMA);
        OreDictionary.registerOre("netherrack", Blocks.NETHERRACK);
        OreDictionary.registerOre("obsidianCrying", Blocks.OBSIDIAN);
        OreDictionary.registerOre("obsidian", Blocks.OBSIDIAN);
        OreDictionary.registerOre("obsidians", Blocks.OBSIDIAN);
        OreDictionary.registerOre("stone", new ItemStack(Blocks.STONE, 1, OreDictionary.WILDCARD_VALUE));
        OreDictionary.registerOre("stones", new ItemStack(Blocks.STONE, 1, OreDictionary.WILDCARD_VALUE));
        OreDictionary.registerOre("cobblestone", Blocks.COBBLESTONE);
        OreDictionary.registerOre("cobblestones", Blocks.COBBLESTONE);
        OreDictionary.registerOre("sand", new ItemStack(Blocks.SAND, 1, OreDictionary.WILDCARD_VALUE));
        OreDictionary.registerOre("sands", new ItemStack(Blocks.SAND, 1, OreDictionary.WILDCARD_VALUE));
        OreDictionary.registerOre("bone", Items.BONE);
        OreDictionary.registerOre("bones", Items.BONE);
        OreDictionary.registerOre("logWood", new ItemStack(Blocks.LOG, 1, OreDictionary.WILDCARD_VALUE));
        OreDictionary.registerOre("logWood", new ItemStack(Blocks.LOG2, 1, OreDictionary.WILDCARD_VALUE));
        OreDictionary.registerOre("logs", new ItemStack(Blocks.LOG, 1, OreDictionary.WILDCARD_VALUE));
        OreDictionary.registerOre("logs", new ItemStack(Blocks.LOG2, 1, OreDictionary.WILDCARD_VALUE));
        OreDictionary.registerOre("dustEndStone", ModItems.CRUSHED_END_STONE);
        OreDictionary.registerOre("ingotNetherite", ModItems.IESNIUM_INGOT);
        OreDictionary.registerOre("dustWitherite", ModItems.WITHERITE_DUST);
        OreDictionary.registerOre("dustDragonyst", ModItems.DRAGONYST_DUST);
        OreDictionary.registerOre("foodRawFish", new ItemStack(Items.FISH, 1, OreDictionary.WILDCARD_VALUE));
        OreDictionary.registerOre("gemPrismarine", Items.PRISMARINE_SHARD);
        OreDictionary.registerOre("crystalPrismarine", Items.PRISMARINE_CRYSTALS);
        OreDictionary.registerOre("dyeBlack", new ItemStack(Items.DYE, 1, 0));
        OreDictionary.registerOre("shellNautilus", ModItems.NAUTILUS_SHELL);

        registerRepairTags();
    }

    private static void registerRepairTags() {
        registerTool(Items.WOODEN_SWORD);
        registerWildcard("swords", Items.WOODEN_SWORD);
        registerTool(Items.WOODEN_SHOVEL);
        registerPickaxe(Items.WOODEN_PICKAXE);
        registerTool(Items.WOODEN_AXE);
        registerTool(Items.WOODEN_HOE);
        registerTool(Items.STONE_SWORD);
        registerWildcard("swords", Items.STONE_SWORD);
        registerTool(Items.STONE_SHOVEL);
        registerPickaxe(Items.STONE_PICKAXE);
        registerTool(Items.STONE_AXE);
        registerTool(Items.STONE_HOE);
        registerTool(Items.IRON_SWORD);
        registerWildcard("swords", Items.IRON_SWORD);
        registerTool(Items.IRON_SHOVEL);
        registerPickaxe(Items.IRON_PICKAXE);
        registerTool(Items.IRON_AXE);
        registerTool(Items.IRON_HOE);
        registerTool(Items.GOLDEN_SWORD);
        registerWildcard("swords", Items.GOLDEN_SWORD);
        registerTool(Items.GOLDEN_SHOVEL);
        registerPickaxe(Items.GOLDEN_PICKAXE);
        registerTool(Items.GOLDEN_AXE);
        registerTool(Items.GOLDEN_HOE);
        registerTool(Items.DIAMOND_SWORD);
        registerWildcard("swords", Items.DIAMOND_SWORD);
        registerTool(Items.DIAMOND_SHOVEL);
        registerPickaxe(Items.DIAMOND_PICKAXE);
        registerTool(Items.DIAMOND_AXE);
        registerTool(Items.DIAMOND_HOE);
        registerTool(Items.BOW);
        registerTool(Items.FISHING_ROD);
        registerTool(Items.SHEARS);
        registerTool(Items.FLINT_AND_STEEL);
        registerPickaxe(ModItems.INFUSED_PICKAXE);
        registerPickaxe(ModItems.IESNIUM_PICKAXE);
        registerTool(ModItems.BUTCHER_KNIFE);
        registerTool(ModItems.IESNIUM_BUTCHER_KNIFE);
        registerWildcard("toolKnife", ModItems.BUTCHER_KNIFE);
        registerWildcard("toolKnife", ModItems.IESNIUM_BUTCHER_KNIFE);
        registerWildcard("toolKnifeIesnium", ModItems.IESNIUM_BUTCHER_KNIFE);

        registerArmor(Items.LEATHER_HELMET);
        registerArmor(Items.LEATHER_CHESTPLATE);
        registerArmor(Items.LEATHER_LEGGINGS);
        registerArmor(Items.LEATHER_BOOTS);
        registerArmor(Items.CHAINMAIL_HELMET);
        registerArmor(Items.CHAINMAIL_CHESTPLATE);
        registerArmor(Items.CHAINMAIL_LEGGINGS);
        registerArmor(Items.CHAINMAIL_BOOTS);
        registerArmor(Items.IRON_HELMET);
        registerArmor(Items.IRON_CHESTPLATE);
        registerArmor(Items.IRON_LEGGINGS);
        registerArmor(Items.IRON_BOOTS);
        registerArmor(Items.GOLDEN_HELMET);
        registerArmor(Items.GOLDEN_CHESTPLATE);
        registerArmor(Items.GOLDEN_LEGGINGS);
        registerArmor(Items.GOLDEN_BOOTS);
        registerArmor(Items.DIAMOND_HELMET);
        registerArmor(Items.DIAMOND_CHESTPLATE);
        registerArmor(Items.DIAMOND_LEGGINGS);
        registerArmor(Items.DIAMOND_BOOTS);

        registerChalk(ModItems.CHALK_WHITE);
        registerChalk(ModItems.CHALK_LIGHT_GRAY);
        registerChalk(ModItems.CHALK_GRAY);
        registerChalk(ModItems.CHALK_BLACK);
        registerChalk(ModItems.CHALK_BROWN);
        registerChalk(ModItems.CHALK_RED);
        registerChalk(ModItems.CHALK_ORANGE);
        registerChalk(ModItems.CHALK_YELLOW);
        registerChalk(ModItems.CHALK_LIME);
        registerChalk(ModItems.CHALK_GREEN);
        registerChalk(ModItems.CHALK_CYAN);
        registerChalk(ModItems.CHALK_LIGHT_BLUE);
        registerChalk(ModItems.CHALK_BLUE);
        registerChalk(ModItems.CHALK_PURPLE);
        registerChalk(ModItems.CHALK_MAGENTA);
        registerChalk(ModItems.CHALK_PINK);
        registerChalk(ModItems.CHALK_RAINBOW);
        registerChalk(ModItems.CHALK_VOID);

        registerMiner(ModItems.MINER_FOLIOT_UNSPECIALIZED);
        registerMiner(ModItems.MINER_DJINNI_ORES);
        registerMiner(ModItems.MINER_AFRIT_DEEPS);
        registerMiner(ModItems.MINER_MARID_MASTER);
        registerMiner(ModItems.MINER_ANCIENT_ELDRITCH);
    }

    private static void registerTool(Item item) {
        registerWildcard("tools", item);
    }

    private static void registerPickaxe(Item item) {
        registerTool(item);
        registerWildcard("toolPickaxe", item);
        registerWildcard("pickaxes", item);
        registerWildcard("tools/pickaxes", item);
    }

    private static void registerArmor(Item item) {
        registerWildcard("armors", item);
    }

    private static void registerChalk(Item item) {
        registerWildcard("toolChalk", item);
    }

    private static void registerMiner(Item item) {
        registerWildcard("miners", item);
    }

    private static void registerWildcard(String oreName, Item item) {
        OreDictionary.registerOre(oreName, new ItemStack(item, 1, OreDictionary.WILDCARD_VALUE));
    }
}
