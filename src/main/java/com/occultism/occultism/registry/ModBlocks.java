package com.occultism.occultism.registry;

import com.occultism.occultism.Tags;
import com.occultism.occultism.block.BlockChalkGlyph;
import com.occultism.occultism.block.BlockDaturaCrop;
import com.occultism.occultism.block.BlockDimensionalBattlefield;
import com.occultism.occultism.block.BlockDimensionalMineshaft;
import com.occultism.occultism.block.BlockEntityWormhole;
import com.occultism.occultism.block.BlockGoldenSacrificialBowl;
import com.occultism.occultism.block.BlockIesniumAnvil;
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
import com.occultism.occultism.block.BlockSpiritGrindstone;
import com.occultism.occultism.block.BlockStableWormhole;
import com.occultism.occultism.block.BlockStorageController;
import com.occultism.occultism.block.BlockStorageStabilizer;
import com.occultism.occultism.block.BlockTranslucentModel;
import com.occultism.occultism.block.OtherworldBlockTier;
import net.minecraft.block.Block;
import net.minecraft.block.BlockOre;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
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
    public static final Block OTHERROCK_PEDESTAL = block("otherrock_pedestal", new BlockModelOnly(Material.ROCK), 2.0F,
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
    public static final Block LARGE_CANDLE = block("large_candle",
            new BlockPentacleRequirement(Material.CIRCUITS, SoundType.CLOTH,
                    new AxisAlignedBB(0.25D, 0.0D, 0.25D, 0.75D, 0.625D, 0.75D), BlockRenderLayer.CUTOUT),
            0.2F, 0.2F, -1);
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
    public static final BlockChalkGlyph CHALK_GLYPH_YELLOW = glyph("chalk_glyph_yellow", 0xFED83D);
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
