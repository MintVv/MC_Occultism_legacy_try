package com.occultism.occultism.registry;

import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.potion.PotionHelper;
import net.minecraftforge.fml.common.registry.GameRegistry;

public final class ModRecipes {

    private ModRecipes() {
    }

    public static void register() {
        GameRegistry.addSmelting(ModBlocks.COPPER_ORE, new ItemStack(ModItems.COPPER_INGOT), 0.7F);
        GameRegistry.addSmelting(ModItems.COPPER_DUST, new ItemStack(ModItems.COPPER_INGOT), 0.5F);
        GameRegistry.addSmelting(ModItems.IRON_DUST, new ItemStack(net.minecraft.init.Items.IRON_INGOT), 0.5F);
        GameRegistry.addSmelting(ModItems.GOLD_DUST, new ItemStack(net.minecraft.init.Items.GOLD_INGOT), 0.5F);

        GameRegistry.addSmelting(ModBlocks.SILVER_ORE, new ItemStack(ModItems.SILVER_INGOT), 0.7F);
        GameRegistry.addSmelting(ModItems.RAW_SILVER, new ItemStack(ModItems.SILVER_INGOT), 0.7F);
        GameRegistry.addSmelting(ModItems.SILVER_DUST, new ItemStack(ModItems.SILVER_INGOT), 0.5F);

        GameRegistry.addSmelting(ModBlocks.IESNIUM_ORE, new ItemStack(ModItems.IESNIUM_INGOT), 1.0F);
        GameRegistry.addSmelting(ModItems.RAW_IESNIUM, new ItemStack(ModItems.IESNIUM_INGOT), 1.0F);
        GameRegistry.addSmelting(ModItems.IESNIUM_DUST, new ItemStack(ModItems.IESNIUM_INGOT), 0.8F);
        // No netherite_dust -> iesnium_ingot line: upstream crushes netherite INTO dust, and
        // iesnium is this mod's endgame metal - turning a 1.16 leftover into an iesnium source
        // would bypass that progression. netherite_scrap_dust has no source in 1.12 anyway.

        GameRegistry.addSmelting(ModBlocks.OTHERSTONE, new ItemStack(ModBlocks.POLISHED_OTHERSTONE), 0.5F);
        GameRegistry.addSmelting(ModBlocks.OTHERROCK, new ItemStack(ModBlocks.POLISHED_OTHERROCK), 0.5F);
        GameRegistry.addSmelting(ModBlocks.POLISHED_OTHERSTONE, new ItemStack(ModItems.BURNT_OTHERSTONE), 0.15F);
        GameRegistry.addSmelting(ModBlocks.POLISHED_OTHERROCK, new ItemStack(ModItems.BURNT_OTHERROCK), 0.15F);

        PotionHelper.addMix(PotionTypes.AWKWARD, ModItems.OTHERWORLD_ESSENCE, ModPotions.THIRD_EYE_POTION);
        PotionHelper.addMix(ModPotions.THIRD_EYE_POTION, Items.REDSTONE, ModPotions.LONG_THIRD_EYE_POTION);
        PotionHelper.addMix(PotionTypes.NIGHT_VISION, ModItems.DATURA, ModPotions.THIRD_EYE_POTION);
        PotionHelper.addMix(PotionTypes.LONG_NIGHT_VISION, ModItems.DATURA, ModPotions.LONG_THIRD_EYE_POTION);
    }
}
