package com.occultism.occultism.registry;

import com.occultism.occultism.Tags;
import com.occultism.occultism.block.BlockChalkGlyph;
import com.occultism.occultism.client.render.TileEntitySacrificialBowlRenderer;
import com.occultism.occultism.tileentity.TileEntitySacrificialBowl;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID, value = Side.CLIENT)
public final class ModModels {

    private ModModels() {
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySacrificialBowl.class,
                new TileEntitySacrificialBowlRenderer());
        for (Item item : ModItems.ITEMS) {
            registerItemModel(item);
        }
        registerDivinationRodModels();
        registerVitalityCompassModels();
        registerMagicLampModels();
        registerStableWormholeModels();
        for (Block block : ModBlocks.BLOCKS) {
            if (block instanceof BlockChalkGlyph) {
                continue;
            }
            registerItemModel(Item.getItemFromBlock(block));
        }
    }

    @SubscribeEvent
    public static void registerBlockColors(ColorHandlerEvent.Block event) {
        event.getBlockColors().registerBlockColorHandler((state, worldIn, pos, tintIndex) -> {
            if (tintIndex == 0 && state.getBlock() instanceof BlockChalkGlyph) {
                return ((BlockChalkGlyph) state.getBlock()).getColor();
            }
            return 0xFFFFFF;
        }, ModBlocks.CHALK_GLYPHS);
        event.getBlockColors().registerBlockColorHandler((state, worldIn, pos, tintIndex) -> 0xFFFFFF,
                ModBlocks.LARGE_CANDLE);
    }

    @SubscribeEvent
    public static void registerItemColors(ColorHandlerEvent.Item event) {
        registerImpureChalkColor(event, ModItems.CHALK_WHITE_IMPURE, 0xFFFFFF);
        registerImpureChalkColor(event, ModItems.CHALK_LIGHT_GRAY_IMPURE, 0x9D9D97);
        registerImpureChalkColor(event, ModItems.CHALK_GRAY_IMPURE, 0x474F52);
        registerImpureChalkColor(event, ModItems.CHALK_BLACK_IMPURE, 0x1D1D21);
        registerImpureChalkColor(event, ModItems.CHALK_BROWN_IMPURE, 0x835432);
        registerImpureChalkColor(event, ModItems.CHALK_RED_IMPURE, 0xB02E26);
        registerImpureChalkColor(event, ModItems.CHALK_ORANGE_IMPURE, 0xF9801D);
        registerImpureChalkColor(event, ModItems.CHALK_YELLOW_IMPURE, 0xFED83D);
        registerImpureChalkColor(event, ModItems.CHALK_LIME_IMPURE, 0x80C71F);
        registerImpureChalkColor(event, ModItems.CHALK_GREEN_IMPURE, 0x5E7C16);
        registerImpureChalkColor(event, ModItems.CHALK_CYAN_IMPURE, 0x169C9C);
        registerImpureChalkColor(event, ModItems.CHALK_LIGHT_BLUE_IMPURE, 0x3AB3DA);
        registerImpureChalkColor(event, ModItems.CHALK_BLUE_IMPURE, 0x3C44AA);
        registerImpureChalkColor(event, ModItems.CHALK_PURPLE_IMPURE, 0x8932B8);
        registerImpureChalkColor(event, ModItems.CHALK_MAGENTA_IMPURE, 0xC74EBD);
        registerImpureChalkColor(event, ModItems.CHALK_PINK_IMPURE, 0xF38BAA);
        event.getItemColors().registerItemColorHandler((stack, tintIndex) -> 0xFFFFFF,
                Item.getItemFromBlock(ModBlocks.LARGE_CANDLE));
    }

    private static void registerItemModel(Item item) {
        if (item == null || item == Items.AIR) {
            return;
        }
        ModelLoader.setCustomModelResourceLocation(item, 0,
                new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }

    private static void registerVitalityCompassModels() {
        ResourceLocation[] variants = new ResourceLocation[32];
        for (int i = 0; i < variants.length; i++) {
            variants[i] = new ResourceLocation(Tags.MOD_ID,
                    String.format(java.util.Locale.ROOT, "vitality_compass/compass_%02d", i));
        }
        ModelLoader.registerItemVariants(ModItems.VITALITY_COMPASS, variants);
    }

    private static void registerDivinationRodModels() {
        ModelLoader.registerItemVariants(ModItems.DIVINATION_ROD, getDivinationVariants("divination_rod"));
        ModelLoader.registerItemVariants(ModItems.TRUE_SIGHT_STAFF, getDivinationVariants("true_sight_staff"));
    }

    private static ResourceLocation[] getDivinationVariants(String prefix) {
        ResourceLocation[] variants = new ResourceLocation[9];
        for (int i = 0; i <= 7; i++) {
            variants[i] = new ResourceLocation(Tags.MOD_ID, prefix + "_" + i);
        }
        variants[8] = new ResourceLocation(Tags.MOD_ID, prefix + "_searching");
        return variants;
    }

    private static void registerMagicLampModels() {
        ModelLoader.registerItemVariants(ModItems.MAGIC_LAMP_EMPTY,
                new ResourceLocation(Tags.MOD_ID, "magic_lamp"));
    }

    private static void registerStableWormholeModels() {
        ModelLoader.registerItemVariants(Item.getItemFromBlock(ModBlocks.STABLE_WORMHOLE),
                new ResourceLocation(Tags.MOD_ID, "stable_wormhole_unlinked"),
                new ResourceLocation(Tags.MOD_ID, "stable_wormhole_linked"));
        ModelLoader.registerItemVariants(Item.getItemFromBlock(ModBlocks.STABLE_WORMHOLE_DARK),
                new ResourceLocation(Tags.MOD_ID, "stable_wormhole_dark_unlinked"),
                new ResourceLocation(Tags.MOD_ID, "stable_wormhole_dark_linked"));
    }

    private static void registerImpureChalkColor(ColorHandlerEvent.Item event, Item item, int color) {
        event.getItemColors().registerItemColorHandler((stack, tintIndex) -> tintIndex == 0 ? color : 0xFFFFFF, item);
    }
}
