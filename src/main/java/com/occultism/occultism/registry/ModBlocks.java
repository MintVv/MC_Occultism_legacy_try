package com.occultism.occultism.registry;

import com.occultism.occultism.Tags;
import com.occultism.occultism.block.BlockChalkGlyph;
import com.occultism.occultism.block.BlockDaturaCrop;
import com.occultism.occultism.block.BlockDimensionalBattlefield;
import com.occultism.occultism.block.BlockDimensionalMineshaft;
import com.occultism.occultism.block.BlockEntityWormhole;
import com.occultism.occultism.block.BlockGoldenSacrificialBowl;
import com.occultism.occultism.block.BlockIesniumAnvil;
import com.occultism.occultism.block.BlockLargeCandle;
import com.occultism.occultism.block.BlockLightedAir;
import com.occultism.occultism.block.BlockModelOnly;
import com.occultism.occultism.block.BlockOtherworldLeaves;
import com.occultism.occultism.block.BlockOtherworldLeavesNatural;
import com.occultism.occultism.block.BlockOtherworldLog;
import com.occultism.occultism.block.BlockOtherworldLogNatural;
import com.occultism.occultism.block.BlockOtherworldStrippedLogNatural;
import com.occultism.occultism.block.BlockOtherworldOreNatural;
import com.occultism.occultism.block.BlockOtherflowerNatural;
import com.occultism.occultism.block.BlockOtherglassNatural;
import com.occultism.occultism.block.BlockPentacleRequirement;
import com.occultism.occultism.block.BlockOtherworldSapling;
import com.occultism.occultism.block.BlockOtherworldSaplingNatural;
import com.occultism.occultism.block.BlockOtherworldStoneNatural;
import com.occultism.occultism.block.BlockSacrificialBowl;
import com.occultism.occultism.block.BlockSimplePlant;
import com.occultism.occultism.block.BlockSpiritFire;
import com.occultism.occultism.block.BlockSpiritCampfire;
import com.occultism.occultism.block.BlockSpiritGrindstone;
import com.occultism.occultism.block.BlockSpiritLantern;
import com.occultism.occultism.block.BlockSpiritTorch;
import com.occultism.occultism.block.BlockStableWormhole;
import com.occultism.occultism.block.BlockStorageController;
import com.occultism.occultism.block.BlockStorageStabilizer;
import com.occultism.occultism.block.BlockTranslucentModel;
import com.occultism.occultism.block.OtherworldBlockTier;
import com.occultism.occultism.block.BlockOccultismSlab;
import com.occultism.occultism.block.BlockOccultismHalfSlab;
import com.occultism.occultism.block.BlockOccultismDoubleSlab;
import com.occultism.occultism.block.BlockOccultismStoneButton;
import com.occultism.occultism.block.BlockOccultismStonePressurePlate;
import com.occultism.occultism.block.BlockOtherplanksButton;
import com.occultism.occultism.block.BlockOtherplanksDoor;
import com.occultism.occultism.block.BlockOtherplanksPressurePlate;
import com.occultism.occultism.block.BlockOtherplanksHalfSlab;
import com.occultism.occultism.block.BlockOtherplanksDoubleSlab;
import com.occultism.occultism.block.BlockOtherplanksSlab;
import com.occultism.occultism.block.BlockOtherplanksStairs;
import com.occultism.occultism.block.BlockOtherplanksTrapDoor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockOre;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockWall;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemSlab;
import com.occultism.occultism.item.ItemBlockStableWormhole;
import com.occultism.occultism.item.ItemBlockStorageController;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class ModBlocks {

    public static final Block OTHERSTONE = block("otherstone", new Block(Material.ROCK), 1.5F, 30.0F, 0);
    public static final Block OTHERSTONE_NATURAL = block("otherstone_natural",
            new BlockOtherworldStoneNatural(OTHERSTONE, Blocks.STONE, 5, OtherworldBlockTier.ONE), 1.5F, 30.0F, 0);
    public static final Block OTHERROCK = block("otherrock", new Block(Material.ROCK), 1.5F, 30.0F, 0);
    public static final Block OTHERROCK_NATURAL = block("otherrock_natural",
            new BlockOtherworldStoneNatural(OTHERROCK, Blocks.STONE, 3, OtherworldBlockTier.ONE), 1.5F, 30.0F, 0);
    public static final Block POLISHED_OTHERSTONE = block("polished_otherstone", new Block(Material.ROCK), 1.5F, 30.0F,
            0);
    public static final Block POLISHED_OTHERROCK = block("polished_otherrock", new Block(Material.ROCK), 1.5F, 30.0F,
            0);
    public static final Block OTHERSTONE_BRICKS = block("otherstone_bricks", new Block(Material.ROCK), 1.5F, 30.0F, 0);
    public static final Block OTHERROCK_BRICKS = block("otherrock_bricks", new Block(Material.ROCK), 1.5F, 30.0F, 0);
    public static final Block OTHERWORLD_LOG = block("otherworld_log", new BlockOtherworldLog(), 2.0F, 5.0F, 0);
    public static final Block OTHERWORLD_LOG_NATURAL = block("otherworld_log_natural",
            new BlockOtherworldLogNatural(), 2.0F, 5.0F, 0);
    public static final Block OTHERWORLD_WOOD = block("otherworld_wood", new BlockOtherworldLog(), 2.0F, 5.0F, 0);
    public static final Block STRIPPED_OTHERWORLD_LOG_NATURAL = block("stripped_otherworld_log_natural",
            new BlockOtherworldStrippedLogNatural(), 2.0F, 5.0F, 0);
    public static final Block STRIPPED_OTHERWORLD_LOG = block("stripped_otherworld_log", new BlockOtherworldLog(), 2.0F,
            5.0F, 0);
    public static final Block STRIPPED_OTHERWORLD_WOOD = block("stripped_otherworld_wood", new BlockOtherworldLog(), 2.0F,
            5.0F, 0);
    public static final Block OTHERPLANKS = block("otherplanks", new Block(Material.WOOD), 2.0F, 5.0F, 0);
    public static final Block OTHERWORLD_SAPLING = block("otherworld_sapling", new BlockOtherworldSapling(), 0.0F, 0.0F, -1);
    public static final Block OTHERWORLD_SAPLING_NATURAL = block("otherworld_sapling_natural", new BlockOtherworldSaplingNatural(),
            0.0F, 0.0F, -1);
    public static final Block OTHERWORLD_LEAVES = block("otherworld_leaves", new BlockOtherworldLeaves(), 0.2F, 1.0F, -1);
    public static final Block OTHERWORLD_LEAVES_NATURAL = block("otherworld_leaves_natural",
            new BlockOtherworldLeavesNatural(), 0.2F, 1.0F, -1);
    public static final Block OTHERFLOWER = block("otherflower", new BlockSimplePlant(), 0.0F, 0.0F, -1);
    public static final Block OTHERFLOWER_NATURAL = block("otherflower_natural", new BlockOtherflowerNatural(), 0.0F,
            0.0F, -1);
    public static final Block OTHERGLASS_NATURAL = block("otherglass_natural", new BlockOtherglassNatural(), 5.0F,
            50.0F, 0);
    public static final Block DATURA_CROP = block("datura", new BlockDaturaCrop(), 0.0F, 0.0F, -1);
    public static final Block COPPER_ORE = block("copper_ore", new BlockOre(), 3.0F, 5.0F, 1);
    public static final Block SILVER_ORE = block("silver_ore", new BlockOre(), 3.0F, 5.0F, 1);
    public static final Block IESNIUM_ORE = block("iesnium_ore", new BlockOre(), 4.0F, 10.0F, 2);
    public static final Block IESNIUM_ORE_NATURAL = block("iesnium_ore_natural",
            new BlockOtherworldOreNatural(IESNIUM_ORE, Blocks.NETHERRACK, OtherworldBlockTier.TWO), 4.0F, 10.0F, 2);
    public static final Block COPPER_BLOCK = block("copper_block", new Block(Material.IRON), 5.0F, 10.0F, 1);
    public static final Block SILVER_BLOCK = block("silver_block", new Block(Material.IRON), 5.0F, 10.0F, 1);
    public static final Block RAW_SILVER_BLOCK = block("raw_silver_block", new Block(Material.IRON), 5.0F, 10.0F, 1);
    public static final Block IESNIUM_BLOCK = block("iesnium_block", new Block(Material.IRON), 5.0F, 10.0F, 2);
    public static final Block RAW_IESNIUM_BLOCK = block("raw_iesnium_block", new Block(Material.IRON), 5.0F, 10.0F, 2);
    public static final Block BEE_NEST = block("bee_nest", new Block(Material.WOOD), 1.5F, 5.0F, 0);
    public static final Block BELL = block("bell", new Block(Material.IRON), 5.0F, 10.0F, 0);
    public static final Block AMETHYST_BLOCK = block("amethyst_block", new Block(Material.ROCK), 1.5F, 5.0F, 0);
    public static final Block BUDDING_AMETHYST = block("budding_amethyst", new Block(Material.ROCK), 1.5F, 5.0F, 0);
    public static final Block REINFORCED_DEEPSLATE = block("reinforced_deepslate", new Block(Material.ROCK), 50.0F,
            1200.0F, 2);
    public static final Block SPIRIT_FIRE = block("spirit_fire", new BlockSpiritFire(), 0.2F, 0.2F, -1);
    public static final Block OTHERSTONE_PEDESTAL = block("otherstone_pedestal", new BlockModelOnly(Material.ROCK),
            2.0F, 10.0F, 0);
    public static final Block STABLE_WORMHOLE = block("stable_wormhole", new BlockStableWormhole(), 2.0F,
            10.0F, 0);
    public static final Block STORAGE_CONTROLLER_BASE = block("storage_controller_base", new BlockModelOnly(Material.ROCK),
            2.0F, 10.0F, 0);
    public static final Block OTHERROCK_PEDESTAL = block("otherstone_pedestal_silver", new BlockModelOnly(Material.ROCK), 2.0F,
            10.0F, 0);
    public static final Block STABLE_WORMHOLE_DARK = block("stable_wormhole_dark", new BlockStableWormhole(),
            2.0F, 10.0F, 0);
    public static final Block STORAGE_CONTROLLER_BASE_DARK = block("storage_controller_base_dark",
            new BlockModelOnly(Material.ROCK), 2.0F, 10.0F, 0);
    public static final Block STORAGE_CONTROLLER = block("storage_controller", new BlockStorageController(),
            2.0F, 10.0F, 0);
    public static final Block STORAGE_CONTROLLER_DARK = block("storage_controller_dark", new BlockStorageController(),
            2.0F, 10.0F, 0);
    public static final Block STORAGE_CONTROLLER_STABILIZED = block("storage_controller_stabilized",
            new BlockStorageController(), 2.0F, 10.0F, 0);
    public static final Block STORAGE_CONTROLLER_STABILIZED_DARK = block("storage_controller_stabilized_dark",
            new BlockStorageController(), 2.0F, 10.0F, 0);
    public static final Block STORAGE_STABILIZER_TIER0 = block("storage_stabilizer_tier0", new BlockStorageStabilizer(),
            2.0F, 10.0F, 0);
    public static final Block STORAGE_STABILIZER_TIER1 = block("storage_stabilizer_tier1", new BlockStorageStabilizer(),
            2.0F, 10.0F, 0);
    public static final Block STORAGE_STABILIZER_TIER2 = block("storage_stabilizer_tier2", new BlockStorageStabilizer(),
            2.0F, 10.0F, 0);
    public static final Block STORAGE_STABILIZER_TIER3 = block("storage_stabilizer_tier3", new BlockStorageStabilizer(),
            2.0F, 10.0F, 0);
    public static final Block STORAGE_STABILIZER_TIER4 = block("storage_stabilizer_tier4", new BlockStorageStabilizer(),
            2.0F, 10.0F, 0);
    public static final Block STORAGE_STABILIZER_TIER5 = block("storage_stabilizer_tier5", new BlockStorageStabilizer(),
            2.0F, 10.0F, 0);
    public static final Block STORAGE_STABILIZER_TIER0_DARK = block("storage_stabilizer_tier0_dark",
            new BlockStorageStabilizer(), 2.0F, 10.0F, 0);
    public static final Block STORAGE_STABILIZER_TIER1_DARK = block("storage_stabilizer_tier1_dark",
            new BlockStorageStabilizer(), 2.0F, 10.0F, 0);
    public static final Block STORAGE_STABILIZER_TIER2_DARK = block("storage_stabilizer_tier2_dark",
            new BlockStorageStabilizer(), 2.0F, 10.0F, 0);
    public static final Block STORAGE_STABILIZER_TIER3_DARK = block("storage_stabilizer_tier3_dark",
            new BlockStorageStabilizer(), 2.0F, 10.0F, 0);
    public static final Block STORAGE_STABILIZER_TIER4_DARK = block("storage_stabilizer_tier4_dark",
            new BlockStorageStabilizer(), 2.0F, 10.0F, 0);
    public static final Block STORAGE_STABILIZER_TIER5_DARK = block("storage_stabilizer_tier5_dark",
            new BlockStorageStabilizer(), 2.0F, 10.0F, 0);
    public static final Block SPIRIT_GRINDSTONE = block("spirit_grindstone", new BlockSpiritGrindstone(), 2.0F,
            10.0F, 0);
    public static final Block DIMENSIONAL_EXTRACTOR = block("dimensional_extractor",
            new BlockTranslucentModel(Material.ROCK), 2.0F, 10.0F, 0);
    public static final Block DIMENSIONAL_MINESHAFT = block("dimensional_mineshaft", new BlockDimensionalMineshaft(),
            2.0F, 10.0F, 0);
    public static final Block DIMENSIONAL_BATTLEFIELD = block("dimensional_battlefield",
            new BlockDimensionalBattlefield(), 2.0F, 10.0F, 0);
    public static final Block IESNIUM_ANVIL = block("iesnium_anvil", new BlockIesniumAnvil(), 5.0F, 1200.0F, 2);
    public static final Block ENTITY_WORMHOLE = block("entity_wormhole", new BlockEntityWormhole(), 2.0F, 10.0F, 0);
    public static final Block ENTITY_WORMHOLE_DARK = block("entity_wormhole_dark", new BlockEntityWormhole(), 2.0F,
            10.0F, 0);
    public static final Block SACRIFICIAL_BOWL = block("sacrificial_bowl", new BlockSacrificialBowl(), 2.0F, 10.0F,
            0);
    public static final Block COPPER_SACRIFICIAL_BOWL = block("copper_sacrificial_bowl", new BlockSacrificialBowl(), 2.0F, 10.0F, 0);
    public static final Block SILVER_SACRIFICIAL_BOWL = block("silver_sacrificial_bowl", new BlockSacrificialBowl(), 2.0F, 10.0F, 0);
    public static final Block DARK_SACRIFICIAL_BOWL = block("dark_sacrificial_bowl", new BlockSacrificialBowl(), 2.0F, 10.0F, 0);
    public static final Block DARK_COPPER_SACRIFICIAL_BOWL = block("dark_copper_sacrificial_bowl", new BlockSacrificialBowl(), 2.0F, 10.0F, 0);
    public static final Block DARK_SILVER_SACRIFICIAL_BOWL = block("dark_silver_sacrificial_bowl", new BlockSacrificialBowl(), 2.0F, 10.0F, 0);
    public static final Block GOLDEN_SACRIFICIAL_BOWL = block("golden_sacrificial_bowl", new BlockGoldenSacrificialBowl(),
            2.0F, 10.0F, 0);
    public static final Block DARK_GOLDEN_SACRIFICIAL_BOWL = block("dark_golden_sacrificial_bowl",
            new BlockGoldenSacrificialBowl(), 2.0F, 10.0F, 0);
    public static final Block IESNIUM_SACRIFICIAL_BOWL = block("iesnium_sacrificial_bowl",
            new BlockGoldenSacrificialBowl(4), 2.0F, 10.0F, 0);
    public static final Block DARK_IESNIUM_SACRIFICIAL_BOWL = block("dark_iesnium_sacrificial_bowl",
            new BlockGoldenSacrificialBowl(4), 2.0F, 10.0F, 0);
    public static final Block CELESTIAL_CHALICE = block("celestial_chalice",
            new BlockGoldenSacrificialBowl(Integer.MAX_VALUE), 2.0F, 10.0F, 0);
    public static final Block ELDRITCH_CHALICE = block("eldritch_chalice",
            new BlockGoldenSacrificialBowl(Integer.MAX_VALUE), 2.0F, 10.0F, 0);
    public static final Block LARGE_CANDLE = block("large_candle", new BlockLargeCandle(), 0.2F, 0.2F, -1);
    public static final Block LARGE_CANDLE_WHITE = block("large_candle_white", new BlockLargeCandle(0xF9FFFE), 0.2F, 0.2F, -1);
    public static final Block LARGE_CANDLE_ORANGE = block("large_candle_orange", new BlockLargeCandle(0xF9801D), 0.2F, 0.2F, -1);
    public static final Block LARGE_CANDLE_MAGENTA = block("large_candle_magenta", new BlockLargeCandle(0xC74EBD), 0.2F, 0.2F, -1);
    public static final Block LARGE_CANDLE_LIGHT_BLUE = block("large_candle_light_blue", new BlockLargeCandle(0x3AB3DA), 0.2F, 0.2F, -1);
    public static final Block LARGE_CANDLE_YELLOW = block("large_candle_yellow", new BlockLargeCandle(0xFED83D), 0.2F, 0.2F, -1);
    public static final Block LARGE_CANDLE_LIME = block("large_candle_lime", new BlockLargeCandle(0x80C71F), 0.2F, 0.2F, -1);
    public static final Block LARGE_CANDLE_PINK = block("large_candle_pink", new BlockLargeCandle(0xF38BAA), 0.2F, 0.2F, -1);
    public static final Block LARGE_CANDLE_GRAY = block("large_candle_gray", new BlockLargeCandle(0x474F52), 0.2F, 0.2F, -1);
    public static final Block LARGE_CANDLE_LIGHT_GRAY = block("large_candle_light_gray", new BlockLargeCandle(0x9D9D97), 0.2F, 0.2F, -1);
    public static final Block LARGE_CANDLE_CYAN = block("large_candle_cyan", new BlockLargeCandle(0x169C9C), 0.2F, 0.2F, -1);
    public static final Block LARGE_CANDLE_PURPLE = block("large_candle_purple", new BlockLargeCandle(0x8932B8), 0.2F, 0.2F, -1);
    public static final Block LARGE_CANDLE_BLUE = block("large_candle_blue", new BlockLargeCandle(0x3C44AA), 0.2F, 0.2F, -1);
    public static final Block LARGE_CANDLE_BROWN = block("large_candle_brown", new BlockLargeCandle(0x835432), 0.2F, 0.2F, -1);
    public static final Block LARGE_CANDLE_GREEN = block("large_candle_green", new BlockLargeCandle(0x5E7C16), 0.2F, 0.2F, -1);
    public static final Block LARGE_CANDLE_RED = block("large_candle_red", new BlockLargeCandle(0xB02E26), 0.2F, 0.2F, -1);
    public static final Block LARGE_CANDLE_BLACK = block("large_candle_black", new BlockLargeCandle(0x1D1D21), 0.2F, 0.2F, -1);
    public static final Block SPIRIT_ATTUNED_CRYSTAL = block("spirit_attuned_crystal",
            new BlockPentacleRequirement(Material.GLASS, SoundType.GLASS,
                    new AxisAlignedBB(0.25D, 0.0D, 0.25D, 0.75D, 0.75D, 0.75D), BlockRenderLayer.CUTOUT),
            0.5F, 1.0F, 0);

    public static final BlockChalkGlyph CHALK_GLYPH_WHITE = glyph("chalk_glyph_white", 0xFFFFFF);
    public static final BlockChalkGlyph CHALK_GLYPH_LIGHT_GRAY = glyph("chalk_glyph_light_gray", 0x9D9D97);
    public static final BlockChalkGlyph CHALK_GLYPH_GRAY = glyph("chalk_glyph_gray", 0x474F52);
    public static final BlockChalkGlyph CHALK_GLYPH_BLACK = glyph("chalk_glyph_black", 0x1D1D21);
    public static final BlockChalkGlyph CHALK_GLYPH_BROWN = glyph("chalk_glyph_brown", 0x835432);
    public static final BlockChalkGlyph CHALK_GLYPH_RED = glyph("chalk_glyph_red", 0xB02E26);
    public static final BlockChalkGlyph CHALK_GLYPH_ORANGE = glyph("chalk_glyph_orange", 0xF9801D);
    public static final BlockChalkGlyph CHALK_GLYPH_YELLOW = glyph("chalk_glyph_gold", 0xFED83D);
    public static final BlockChalkGlyph CHALK_GLYPH_LIME = glyph("chalk_glyph_lime", 0x80C71F);
    public static final BlockChalkGlyph CHALK_GLYPH_GREEN = glyph("chalk_glyph_green", 0x5E7C16);
    public static final BlockChalkGlyph CHALK_GLYPH_CYAN = glyph("chalk_glyph_cyan", 0x169C9C);
    public static final BlockChalkGlyph CHALK_GLYPH_LIGHT_BLUE = glyph("chalk_glyph_light_blue", 0x3AB3DA);
    public static final BlockChalkGlyph CHALK_GLYPH_BLUE = glyph("chalk_glyph_blue", 0x3C44AA);
    public static final BlockChalkGlyph CHALK_GLYPH_PURPLE = glyph("chalk_glyph_purple", 0x8932B8);
    public static final BlockChalkGlyph CHALK_GLYPH_MAGENTA = glyph("chalk_glyph_magenta", 0xC74EBD);
    public static final BlockChalkGlyph CHALK_GLYPH_PINK = glyph("chalk_glyph_pink", 0xF38BAA);
    public static final BlockChalkGlyph CHALK_GLYPH_RAINBOW = glyph("chalk_glyph_rainbow", 0xF38BAA);
    public static final BlockChalkGlyph CHALK_GLYPH_VOID = glyph("chalk_glyph_void", 0x474F52);
    public static final Block LIGHTED_AIR = block("lighted_air", new BlockLightedAir(), 0.0F, 0.0F, -1);

    public static final BlockChalkGlyph[] CHALK_GLYPHS = {
            CHALK_GLYPH_WHITE,
            CHALK_GLYPH_LIGHT_GRAY,
            CHALK_GLYPH_GRAY,
            CHALK_GLYPH_BLACK,
            CHALK_GLYPH_BROWN,
            CHALK_GLYPH_RED,
            CHALK_GLYPH_ORANGE,
            CHALK_GLYPH_YELLOW,
            CHALK_GLYPH_LIME,
            CHALK_GLYPH_GREEN,
            CHALK_GLYPH_CYAN,
            CHALK_GLYPH_LIGHT_BLUE,
            CHALK_GLYPH_BLUE,
            CHALK_GLYPH_PURPLE,
            CHALK_GLYPH_MAGENTA,
            CHALK_GLYPH_PINK,
            CHALK_GLYPH_RAINBOW,
            CHALK_GLYPH_VOID
    };

    public static final Block CHISELED_OTHERSTONE_BRICKS = block("chiseled_otherstone_bricks", new Block(Material.ROCK),
            1.5F, 30.0F, 0);
    public static final Block CHISELED_OTHERROCK_BRICKS = block("chiseled_otherrock_bricks", new Block(Material.ROCK),
            1.5F, 30.0F, 0);
    public static final Block CRACKED_OTHERSTONE_BRICKS = block("cracked_otherstone_bricks", new Block(Material.ROCK),
            1.5F, 30.0F, 0);
    public static final Block CRACKED_OTHERROCK_BRICKS = block("cracked_otherrock_bricks", new Block(Material.ROCK),
            1.5F, 30.0F, 0);
    public static final Block OTHERSTONE_STAIRS = block("otherstone_stairs",
            new BlockOtherplanksStairs(OTHERSTONE.getDefaultState()), 1.5F, 30.0F, 0);
    public static final Block OTHERROCK_STAIRS = block("otherrock_stairs",
            new BlockOtherplanksStairs(OTHERROCK.getDefaultState()), 1.5F, 30.0F, 0);
    public static final Block POLISHED_OTHERSTONE_STAIRS = block("polished_otherstone_stairs",
            new BlockOtherplanksStairs(POLISHED_OTHERSTONE.getDefaultState()), 1.5F, 30.0F, 0);
    public static final Block POLISHED_OTHERROCK_STAIRS = block("polished_otherrock_stairs",
            new BlockOtherplanksStairs(POLISHED_OTHERROCK.getDefaultState()), 1.5F, 30.0F, 0);
    public static final Block OTHERSTONE_BRICKS_STAIRS = block("otherstone_bricks_stairs",
            new BlockOtherplanksStairs(OTHERSTONE_BRICKS.getDefaultState()), 1.5F, 30.0F, 0);
    public static final Block OTHERROCK_BRICKS_STAIRS = block("otherrock_bricks_stairs",
            new BlockOtherplanksStairs(OTHERROCK_BRICKS.getDefaultState()), 1.5F, 30.0F, 0);
    public static final BlockOccultismSlab OTHERSTONE_SLAB = (BlockOccultismSlab) block("otherstone_slab",
            new BlockOccultismHalfSlab("otherstone_slab"), 1.5F, 30.0F, 0);
    public static final BlockOccultismSlab OTHERSTONE_DOUBLE_SLAB = (BlockOccultismSlab) block("otherstone_double_slab",
            new BlockOccultismDoubleSlab("otherstone_slab"), 1.5F, 30.0F, 0);
    public static final BlockOccultismSlab OTHERROCK_SLAB = (BlockOccultismSlab) block("otherrock_slab",
            new BlockOccultismHalfSlab("otherrock_slab"), 1.5F, 30.0F, 0);
    public static final BlockOccultismSlab OTHERROCK_DOUBLE_SLAB = (BlockOccultismSlab) block("otherrock_double_slab",
            new BlockOccultismDoubleSlab("otherrock_slab"), 1.5F, 30.0F, 0);
    public static final BlockOccultismSlab POLISHED_OTHERSTONE_SLAB = (BlockOccultismSlab) block(
            "polished_otherstone_slab", new BlockOccultismHalfSlab("polished_otherstone_slab"), 1.5F, 30.0F, 0);
    public static final BlockOccultismSlab POLISHED_OTHERSTONE_DOUBLE_SLAB = (BlockOccultismSlab) block(
            "polished_otherstone_double_slab", new BlockOccultismDoubleSlab("polished_otherstone_slab"), 1.5F, 30.0F,
            0);
    public static final BlockOccultismSlab POLISHED_OTHERROCK_SLAB = (BlockOccultismSlab) block(
            "polished_otherrock_slab", new BlockOccultismHalfSlab("polished_otherrock_slab"), 1.5F, 30.0F, 0);
    public static final BlockOccultismSlab POLISHED_OTHERROCK_DOUBLE_SLAB = (BlockOccultismSlab) block(
            "polished_otherrock_double_slab", new BlockOccultismDoubleSlab("polished_otherrock_slab"), 1.5F, 30.0F,
            0);
    public static final BlockOccultismSlab OTHERSTONE_BRICKS_SLAB = (BlockOccultismSlab) block(
            "otherstone_bricks_slab", new BlockOccultismHalfSlab("otherstone_bricks_slab"), 1.5F, 30.0F, 0);
    public static final BlockOccultismSlab OTHERSTONE_BRICKS_DOUBLE_SLAB = (BlockOccultismSlab) block(
            "otherstone_bricks_double_slab", new BlockOccultismDoubleSlab("otherstone_bricks_slab"), 1.5F, 30.0F, 0);
    public static final BlockOccultismSlab OTHERROCK_BRICKS_SLAB = (BlockOccultismSlab) block(
            "otherrock_bricks_slab", new BlockOccultismHalfSlab("otherrock_bricks_slab"), 1.5F, 30.0F, 0);
    public static final BlockOccultismSlab OTHERROCK_BRICKS_DOUBLE_SLAB = (BlockOccultismSlab) block(
            "otherrock_bricks_double_slab", new BlockOccultismDoubleSlab("otherrock_bricks_slab"), 1.5F, 30.0F, 0);
    public static final Block OTHERSTONE_WALL = block("otherstone_wall", new BlockWall(OTHERSTONE), 1.5F, 30.0F, 0);
    public static final Block OTHERROCK_WALL = block("otherrock_wall", new BlockWall(OTHERROCK), 1.5F, 30.0F, 0);
    public static final Block POLISHED_OTHERSTONE_WALL = block("polished_otherstone_wall",
            new BlockWall(POLISHED_OTHERSTONE), 1.5F, 30.0F, 0);
    public static final Block POLISHED_OTHERROCK_WALL = block("polished_otherrock_wall",
            new BlockWall(POLISHED_OTHERROCK), 1.5F, 30.0F, 0);
    public static final Block OTHERSTONE_BRICKS_WALL = block("otherstone_bricks_wall",
            new BlockWall(OTHERSTONE_BRICKS), 1.5F, 30.0F, 0);
    public static final Block OTHERROCK_BRICKS_WALL = block("otherrock_bricks_wall", new BlockWall(OTHERROCK_BRICKS),
            1.5F, 30.0F, 0);
    public static final Block OTHERSTONE_BUTTON = block("otherstone_button", new BlockOccultismStoneButton(), 0.5F,
            0.5F, -1);
    public static final Block OTHERROCK_BUTTON = block("otherrock_button", new BlockOccultismStoneButton(), 0.5F, 0.5F,
            -1);
    public static final Block OTHERSTONE_PRESSURE_PLATE = block("otherstone_pressure_plate",
            new BlockOccultismStonePressurePlate(), 0.5F, 0.5F, -1);
    public static final Block OTHERROCK_PRESSURE_PLATE = block("otherrock_pressure_plate",
            new BlockOccultismStonePressurePlate(), 0.5F, 0.5F, -1);
    public static final BlockOtherplanksSlab OTHERPLANKS_SLAB = (BlockOtherplanksSlab) block("otherplanks_slab",
            new BlockOtherplanksHalfSlab(), 2.0F, 5.0F, 0);
    public static final BlockOtherplanksSlab OTHERPLANKS_DOUBLE_SLAB = (BlockOtherplanksSlab) block(
            "otherplanks_double_slab", new BlockOtherplanksDoubleSlab(), 2.0F, 5.0F, 0);
    public static final Block OTHERPLANKS_STAIRS = block("otherplanks_stairs",
            new BlockOtherplanksStairs(OTHERPLANKS.getDefaultState()), 2.0F, 5.0F, 0);
    public static final Block OTHERPLANKS_BUTTON = block("otherplanks_button", new BlockOtherplanksButton(), 0.5F,
            0.5F, -1);
    public static final Block OTHERPLANKS_PRESSURE_PLATE = block("otherplanks_pressure_plate",
            new BlockOtherplanksPressurePlate(), 0.5F, 0.5F, -1);
    public static final Block OTHERPLANKS_FENCE = block("otherplanks_fence",
            new BlockFence(Material.WOOD, MapColor.WOOD), 2.0F, 5.0F, 0);
    public static final Block OTHERPLANKS_FENCE_GATE = block("otherplanks_fence_gate",
            new BlockFenceGate(BlockPlanks.EnumType.OAK), 2.0F, 5.0F, 0);
    public static final Block OTHERPLANKS_DOOR = block("otherplanks_door", new BlockOtherplanksDoor(), 3.0F, 5.0F, 0);
    public static final Block OTHERPLANKS_TRAPDOOR = block("otherplanks_trapdoor", new BlockOtherplanksTrapDoor(), 3.0F,
            5.0F, 0);
    public static final ItemDoor OTHERPLANKS_DOOR_ITEM = doorItem(OTHERPLANKS_DOOR);

    public static final Block SPIRIT_TORCH = block("spirit_torch", new BlockSpiritTorch(), 0.0F, 0.0F, -1);

    public static final Block OTHERCOBBLESTONE = block("othercobblestone", new Block(Material.ROCK), 1.5F, 30.0F, 0);
    public static final BlockOccultismSlab OTHERCOBBLESTONE_SLAB = (BlockOccultismSlab) block("othercobblestone_slab",
            new BlockOccultismHalfSlab("othercobblestone_slab"), 1.5F, 30.0F, 0);
    public static final BlockOccultismSlab OTHERCOBBLESTONE_DOUBLE_SLAB = (BlockOccultismSlab) block("othercobblestone_double_slab",
            new BlockOccultismDoubleSlab("othercobblestone_slab"), 1.5F, 30.0F, 0);
    public static final Block OTHERCOBBLESTONE_STAIRS = block("othercobblestone_stairs",
            new BlockOtherplanksStairs(OTHERCOBBLESTONE.getDefaultState()), 1.5F, 30.0F, 0);
    public static final Block OTHERCOBBLESTONE_WALL = block("othercobblestone_wall", new BlockWall(OTHERCOBBLESTONE), 1.5F, 30.0F, 0);
    public static final Block OTHERCOBBLEROCK = block("othercobblerock", new Block(Material.ROCK), 1.5F, 30.0F, 0);
    public static final BlockOccultismSlab OTHERCOBBLEROCK_SLAB = (BlockOccultismSlab) block("othercobblerock_slab",
            new BlockOccultismHalfSlab("othercobblerock_slab"), 1.5F, 30.0F, 0);
    public static final BlockOccultismSlab OTHERCOBBLEROCK_DOUBLE_SLAB = (BlockOccultismSlab) block("othercobblerock_double_slab",
            new BlockOccultismDoubleSlab("othercobblerock_slab"), 1.5F, 30.0F, 0);
    public static final Block OTHERCOBBLEROCK_STAIRS = block("othercobblerock_stairs",
            new BlockOtherplanksStairs(OTHERCOBBLEROCK.getDefaultState()), 1.5F, 30.0F, 0);
    public static final Block OTHERCOBBLEROCK_WALL = block("othercobblerock_wall", new BlockWall(OTHERCOBBLEROCK), 1.5F, 30.0F, 0);

    /** Base candle plus the sixteen coloured variants, used by the colour handlers. */
    public static final Block SPIRIT_CAMPFIRE = block("spirit_campfire", new BlockSpiritCampfire(), 2.0F, 2.0F, 0);
    public static final Block SPIRIT_LANTERN = block("spirit_lantern", new BlockSpiritLantern(), 3.5F, 3.5F, 0);

    public static final Block[] LARGE_CANDLES = {
            LARGE_CANDLE,
            LARGE_CANDLE_WHITE,
            LARGE_CANDLE_ORANGE,
            LARGE_CANDLE_MAGENTA,
            LARGE_CANDLE_LIGHT_BLUE,
            LARGE_CANDLE_YELLOW,
            LARGE_CANDLE_LIME,
            LARGE_CANDLE_PINK,
            LARGE_CANDLE_GRAY,
            LARGE_CANDLE_LIGHT_GRAY,
            LARGE_CANDLE_CYAN,
            LARGE_CANDLE_PURPLE,
            LARGE_CANDLE_BLUE,
            LARGE_CANDLE_BROWN,
            LARGE_CANDLE_GREEN,
            LARGE_CANDLE_RED,
            LARGE_CANDLE_BLACK,
    };

    public static final Block[] BLOCKS = {
            OTHERSTONE,
            OTHERSTONE_NATURAL,
            OTHERROCK,
            OTHERROCK_NATURAL,
            POLISHED_OTHERSTONE,
            POLISHED_OTHERROCK,
            OTHERSTONE_BRICKS,
            OTHERROCK_BRICKS,
            OTHERWORLD_LOG,
            OTHERWORLD_LOG_NATURAL,
            OTHERWORLD_WOOD,
            STRIPPED_OTHERWORLD_LOG_NATURAL,
            STRIPPED_OTHERWORLD_LOG,
            STRIPPED_OTHERWORLD_WOOD,
            OTHERPLANKS,
            OTHERWORLD_SAPLING,
            OTHERWORLD_SAPLING_NATURAL,
            OTHERWORLD_LEAVES,
            OTHERWORLD_LEAVES_NATURAL,
            OTHERFLOWER,
            OTHERFLOWER_NATURAL,
            OTHERGLASS_NATURAL,
            DATURA_CROP,
            COPPER_ORE,
            SILVER_ORE,
            IESNIUM_ORE,
            IESNIUM_ORE_NATURAL,
            COPPER_BLOCK,
            SILVER_BLOCK,
            RAW_SILVER_BLOCK,
            IESNIUM_BLOCK,
            RAW_IESNIUM_BLOCK,
            BEE_NEST,
            BELL,
            AMETHYST_BLOCK,
            BUDDING_AMETHYST,
            REINFORCED_DEEPSLATE,
            SPIRIT_FIRE,
            OTHERSTONE_PEDESTAL,
            STABLE_WORMHOLE,
            STORAGE_CONTROLLER_BASE,
            OTHERROCK_PEDESTAL,
            STABLE_WORMHOLE_DARK,
            STORAGE_CONTROLLER_BASE_DARK,
            STORAGE_CONTROLLER,
            STORAGE_CONTROLLER_DARK,
            STORAGE_CONTROLLER_STABILIZED,
            STORAGE_CONTROLLER_STABILIZED_DARK,
            STORAGE_STABILIZER_TIER0,
            STORAGE_STABILIZER_TIER1,
            STORAGE_STABILIZER_TIER2,
            STORAGE_STABILIZER_TIER3,
            STORAGE_STABILIZER_TIER4,
            STORAGE_STABILIZER_TIER5,
            STORAGE_STABILIZER_TIER0_DARK,
            STORAGE_STABILIZER_TIER1_DARK,
            STORAGE_STABILIZER_TIER2_DARK,
            STORAGE_STABILIZER_TIER3_DARK,
            STORAGE_STABILIZER_TIER4_DARK,
            STORAGE_STABILIZER_TIER5_DARK,
            SPIRIT_GRINDSTONE,
            DIMENSIONAL_EXTRACTOR,
            DIMENSIONAL_MINESHAFT,
            DIMENSIONAL_BATTLEFIELD,
            IESNIUM_ANVIL,
            ENTITY_WORMHOLE,
            ENTITY_WORMHOLE_DARK,
            COPPER_SACRIFICIAL_BOWL,
            SILVER_SACRIFICIAL_BOWL,
            DARK_SACRIFICIAL_BOWL,
            DARK_COPPER_SACRIFICIAL_BOWL,
            DARK_SILVER_SACRIFICIAL_BOWL,
            SACRIFICIAL_BOWL,
            GOLDEN_SACRIFICIAL_BOWL,
            DARK_GOLDEN_SACRIFICIAL_BOWL,
            IESNIUM_SACRIFICIAL_BOWL,
            DARK_IESNIUM_SACRIFICIAL_BOWL,
            CELESTIAL_CHALICE,
            ELDRITCH_CHALICE,
            LARGE_CANDLE,
            SPIRIT_ATTUNED_CRYSTAL,
            CHALK_GLYPH_WHITE,
            CHALK_GLYPH_LIGHT_GRAY,
            CHALK_GLYPH_GRAY,
            CHALK_GLYPH_BLACK,
            CHALK_GLYPH_BROWN,
            CHALK_GLYPH_RED,
            CHALK_GLYPH_ORANGE,
            CHALK_GLYPH_YELLOW,
            CHALK_GLYPH_LIME,
            CHALK_GLYPH_GREEN,
            CHALK_GLYPH_CYAN,
            CHALK_GLYPH_LIGHT_BLUE,
            CHALK_GLYPH_BLUE,
            CHALK_GLYPH_PURPLE,
            CHALK_GLYPH_MAGENTA,
            CHALK_GLYPH_PINK,
            CHALK_GLYPH_RAINBOW,
            CHALK_GLYPH_VOID,
            CHISELED_OTHERSTONE_BRICKS,
            CHISELED_OTHERROCK_BRICKS,
            CRACKED_OTHERSTONE_BRICKS,
            CRACKED_OTHERROCK_BRICKS,
            OTHERSTONE_STAIRS,
            OTHERROCK_STAIRS,
            POLISHED_OTHERSTONE_STAIRS,
            POLISHED_OTHERROCK_STAIRS,
            OTHERSTONE_BRICKS_STAIRS,
            OTHERROCK_BRICKS_STAIRS,
            OTHERSTONE_SLAB,
            OTHERSTONE_DOUBLE_SLAB,
            OTHERROCK_SLAB,
            OTHERROCK_DOUBLE_SLAB,
            POLISHED_OTHERSTONE_SLAB,
            POLISHED_OTHERSTONE_DOUBLE_SLAB,
            POLISHED_OTHERROCK_SLAB,
            POLISHED_OTHERROCK_DOUBLE_SLAB,
            OTHERSTONE_BRICKS_SLAB,
            OTHERSTONE_BRICKS_DOUBLE_SLAB,
            OTHERROCK_BRICKS_SLAB,
            OTHERROCK_BRICKS_DOUBLE_SLAB,
            OTHERSTONE_WALL,
            OTHERROCK_WALL,
            POLISHED_OTHERSTONE_WALL,
            POLISHED_OTHERROCK_WALL,
            OTHERSTONE_BRICKS_WALL,
            OTHERROCK_BRICKS_WALL,
            OTHERSTONE_BUTTON,
            OTHERROCK_BUTTON,
            OTHERSTONE_PRESSURE_PLATE,
            OTHERROCK_PRESSURE_PLATE,
            OTHERPLANKS_SLAB,
            OTHERPLANKS_DOUBLE_SLAB,
            OTHERPLANKS_STAIRS,
            OTHERPLANKS_BUTTON,
            OTHERPLANKS_PRESSURE_PLATE,
            OTHERPLANKS_FENCE,
            OTHERPLANKS_FENCE_GATE,
            OTHERPLANKS_DOOR,
            OTHERPLANKS_TRAPDOOR,
            SPIRIT_TORCH,
            OTHERCOBBLESTONE,
            OTHERCOBBLESTONE_SLAB,
            OTHERCOBBLESTONE_STAIRS,
            OTHERCOBBLESTONE_WALL,
            OTHERCOBBLEROCK,
            OTHERCOBBLEROCK_SLAB,
            OTHERCOBBLEROCK_STAIRS,
            OTHERCOBBLEROCK_WALL,
            LARGE_CANDLE_WHITE,
            LARGE_CANDLE_ORANGE,
            LARGE_CANDLE_MAGENTA,
            LARGE_CANDLE_LIGHT_BLUE,
            LARGE_CANDLE_YELLOW,
            LARGE_CANDLE_LIME,
            LARGE_CANDLE_PINK,
            LARGE_CANDLE_GRAY,
            LARGE_CANDLE_LIGHT_GRAY,
            LARGE_CANDLE_CYAN,
            LARGE_CANDLE_PURPLE,
            LARGE_CANDLE_BLUE,
            LARGE_CANDLE_BROWN,
            LARGE_CANDLE_GREEN,
            LARGE_CANDLE_RED,
            LARGE_CANDLE_BLACK,
            SPIRIT_LANTERN,
            SPIRIT_CAMPFIRE,
            LIGHTED_AIR
    };

    private ModBlocks() {
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(BLOCKS);
    }

    @SubscribeEvent
    public static void registerItemBlocks(RegistryEvent.Register<net.minecraft.item.Item> event) {
        for (Block block : BLOCKS) {
            if (skipItemBlock(block)) {
                continue;
            }
            if (block instanceof BlockStorageController) {
                event.getRegistry().register(new ItemBlockStorageController(block)
                        .setRegistryName(block.getRegistryName())
                        .setTranslationKey(block.getTranslationKey()));
                continue;
            }
            if (block instanceof BlockStableWormhole) {
                event.getRegistry().register(new ItemBlockStableWormhole(block)
                        .setRegistryName(block.getRegistryName())
                        .setTranslationKey(block.getTranslationKey()));
                continue;
            }
            if (block == OTHERPLANKS_DOOR) {
                event.getRegistry().register(OTHERPLANKS_DOOR_ITEM);
                continue;
            }
            if (block == OTHERSTONE_SLAB) {
                event.getRegistry().register(new ItemSlab(block, OTHERSTONE_SLAB, OTHERSTONE_DOUBLE_SLAB)
                        .setRegistryName(block.getRegistryName())
                        .setTranslationKey(block.getTranslationKey()));
                continue;
            }
            if (block == OTHERROCK_SLAB) {
                event.getRegistry().register(new ItemSlab(block, OTHERROCK_SLAB, OTHERROCK_DOUBLE_SLAB)
                        .setRegistryName(block.getRegistryName())
                        .setTranslationKey(block.getTranslationKey()));
                continue;
            }
            if (block == POLISHED_OTHERSTONE_SLAB) {
                event.getRegistry().register(new ItemSlab(block, POLISHED_OTHERSTONE_SLAB, POLISHED_OTHERSTONE_DOUBLE_SLAB)
                        .setRegistryName(block.getRegistryName())
                        .setTranslationKey(block.getTranslationKey()));
                continue;
            }
            if (block == POLISHED_OTHERROCK_SLAB) {
                event.getRegistry().register(new ItemSlab(block, POLISHED_OTHERROCK_SLAB, POLISHED_OTHERROCK_DOUBLE_SLAB)
                        .setRegistryName(block.getRegistryName())
                        .setTranslationKey(block.getTranslationKey()));
                continue;
            }
            if (block == OTHERSTONE_BRICKS_SLAB) {
                event.getRegistry().register(new ItemSlab(block, OTHERSTONE_BRICKS_SLAB, OTHERSTONE_BRICKS_DOUBLE_SLAB)
                        .setRegistryName(block.getRegistryName())
                        .setTranslationKey(block.getTranslationKey()));
                continue;
            }
            if (block == OTHERROCK_BRICKS_SLAB) {
                event.getRegistry().register(new ItemSlab(block, OTHERROCK_BRICKS_SLAB, OTHERROCK_BRICKS_DOUBLE_SLAB)
                        .setRegistryName(block.getRegistryName())
                        .setTranslationKey(block.getTranslationKey()));
                continue;
            }
            if (block == OTHERPLANKS_SLAB) {
                event.getRegistry().register(new ItemSlab(block, OTHERPLANKS_SLAB, OTHERPLANKS_DOUBLE_SLAB)
                        .setRegistryName(block.getRegistryName())
                        .setTranslationKey(block.getTranslationKey()));
                continue;
            }
            if (block == OTHERCOBBLESTONE_SLAB) {
                event.getRegistry().register(new ItemSlab(block, OTHERCOBBLESTONE_SLAB, OTHERCOBBLESTONE_DOUBLE_SLAB)
                        .setRegistryName(block.getRegistryName())
                        .setTranslationKey(block.getTranslationKey()));
                continue;
            }
            if (block == OTHERCOBBLEROCK_SLAB) {
                event.getRegistry().register(new ItemSlab(block, OTHERCOBBLEROCK_SLAB, OTHERCOBBLEROCK_DOUBLE_SLAB)
                        .setRegistryName(block.getRegistryName())
                        .setTranslationKey(block.getTranslationKey()));
                continue;
            }
            ItemBlock itemBlock = new ItemBlock(block);
            if (block == OTHERWORLD_SAPLING || block == OTHERWORLD_SAPLING_NATURAL || block == OTHERFLOWER
                    || block == OTHERFLOWER_NATURAL) {
                itemBlock.setMaxStackSize(64);
            }
            event.getRegistry().register(itemBlock
                    .setRegistryName(block.getRegistryName())
                    .setTranslationKey(block.getTranslationKey()));
        }
    }

    private static ItemDoor doorItem(Block block) {
        ItemDoor item = new ItemDoor(block);
        item.setRegistryName(block.getRegistryName());
        item.setTranslationKey(block.getTranslationKey());
        item.setCreativeTab(com.occultism.occultism.registry.ModCreativeTabs.OCCULTISM);
        return item;
    }

    private static Block block(String name, Block block, float hardness, float resistance, int harvestLevel) {
        block.setRegistryName(Tags.MOD_ID, name);
        block.setTranslationKey(Tags.MOD_ID + "." + name);
        block.setCreativeTab(ModCreativeTabs.OCCULTISM);
        block.setHardness(hardness);
        block.setResistance(resistance);
        if (harvestLevel >= 0) {
            block.setHarvestLevel("pickaxe", harvestLevel);
        }
        if (block == DATURA_CROP) {
            block.setCreativeTab(null);
        }
        if (block == OTHERWORLD_LOG || block == OTHERWORLD_LOG_NATURAL || block == STRIPPED_OTHERWORLD_LOG
                || block == STRIPPED_OTHERWORLD_LOG_NATURAL) {
            Blocks.FIRE.setFireInfo(block, 5, 5);
        }
        if (block == OTHERWORLD_WOOD || block == STRIPPED_OTHERWORLD_WOOD || block == OTHERPLANKS) {
            Blocks.FIRE.setFireInfo(block, 5, 20);
        }
        return block;
    }

    private static boolean skipItemBlock(Block block) {
        return block instanceof BlockChalkGlyph
                || block == OTHERCOBBLESTONE_DOUBLE_SLAB
                || block == OTHERCOBBLEROCK_DOUBLE_SLAB
                || block == OTHERSTONE_DOUBLE_SLAB
                || block == OTHERROCK_DOUBLE_SLAB
                || block == POLISHED_OTHERSTONE_DOUBLE_SLAB
                || block == POLISHED_OTHERROCK_DOUBLE_SLAB
                || block == OTHERSTONE_BRICKS_DOUBLE_SLAB
                || block == OTHERROCK_BRICKS_DOUBLE_SLAB
                || block == OTHERPLANKS_DOUBLE_SLAB
                || block == DATURA_CROP
                || block == LIGHTED_AIR
                || block == OTHERSTONE_NATURAL
                || block == OTHERROCK_NATURAL
                || block == OTHERWORLD_LOG_NATURAL
                || block == STRIPPED_OTHERWORLD_LOG_NATURAL
                || block == OTHERWORLD_LEAVES_NATURAL
                || block == OTHERFLOWER_NATURAL
                || block == IESNIUM_ORE_NATURAL;
    }

    private static BlockChalkGlyph glyph(String name, int color) {
        BlockChalkGlyph block = new BlockChalkGlyph(color);
        block.setRegistryName(Tags.MOD_ID, name);
        block.setTranslationKey(Tags.MOD_ID + "." + name);
        return block;
    }
}
