package com.occultism.occultism.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Collections;

import com.occultism.occultism.entity.EntityFoliotJob;
import com.occultism.occultism.registry.ModBlocks;
import com.occultism.occultism.spirit.SpiritJobType;
import com.occultism.occultism.spirit.SpiritTradeRecipeRegistry;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SpiritTradePortContractTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
        for (Block block : new Block[] {ModBlocks.OTHERSTONE, ModBlocks.OTHERROCK,
                ModBlocks.OTHERWORLD_SAPLING_NATURAL, ModBlocks.OTHERWORLD_SAPLING}) {
            if (!ForgeRegistries.ITEMS.containsKey(block.getRegistryName())) {
                ForgeRegistries.ITEMS.register(new ItemBlock(block).setRegistryName(block.getRegistryName()));
            }
        }
    }

    @Test
    void tradeDefinitionsMatchTheUpstreamTimersAndRounds() {
        SpiritTradeRecipeRegistry.Recipe stone =
                SpiritTradeRecipeRegistry.getForJob(SpiritJobType.TRADER_OTHERSTONE);
        SpiritTradeRecipeRegistry.Recipe rock =
                SpiritTradeRecipeRegistry.getForJob(SpiritJobType.TRADER_OTHERROCK);
        SpiritTradeRecipeRegistry.Recipe sapling =
                SpiritTradeRecipeRegistry.getForJob(SpiritJobType.TRADER_SAPLING);

        assertNotNull(stone);
        assertNotNull(rock);
        assertNotNull(sapling);
        assertEquals(15, stone.getConversionSteps());
        assertEquals(4, stone.getMaxTradesPerRound());
        assertEquals(15, rock.getConversionSteps());
        assertEquals(4, rock.getMaxTradesPerRound());
        assertEquals(20, sapling.getConversionSteps());
        assertEquals(1, sapling.getMaxTradesPerRound());
    }

    @Test
    void tradeConsumesOnlyCompleteInputsAndProducesTheConfiguredCount() {
        SpiritTradeRecipeRegistry.Recipe recipe =
                SpiritTradeRecipeRegistry.getForJob(SpiritJobType.TRADER_OTHERSTONE);
        ItemStack input = new ItemStack(Blocks.STONE, 5, 0);
        ItemStack output = recipe.trade(input);
        assertEquals(1, input.getCount());
        assertEquals(8, output.getCount());
        assertEquals(Item.getItemFromBlock(ModBlocks.OTHERSTONE), output.getItem());
        ItemStack last = recipe.trade(input);
        assertTrue(input.isEmpty());
        assertEquals(2, last.getCount());
        assertTrue(recipe.trade(input).isEmpty());
    }

    @Test
    void saplingTradeDoesNotAcceptTheConvertedSaplingOrConsumeMoreThanOne() {
        SpiritTradeRecipeRegistry.Recipe recipe = SpiritTradeRecipeRegistry.getForJob(SpiritJobType.TRADER_SAPLING);
        ItemStack input = new ItemStack(ModBlocks.OTHERWORLD_SAPLING_NATURAL, 3);
        ItemStack output = recipe.trade(input);
        assertEquals(2, input.getCount());
        assertEquals(1, output.getCount());
        assertEquals(Item.getItemFromBlock(ModBlocks.OTHERWORLD_SAPLING), output.getItem());
        assertTrue(recipe.trade(output).isEmpty());
        assertEquals(1, output.getCount());
        ItemStack unrelated = new ItemStack(Items.DIAMOND, 3);
        assertTrue(recipe.trade(unrelated).isEmpty());
        assertEquals(3, unrelated.getCount());
    }

    @Test
    void displayedInputsMatchAcceptedStoneAndReturnedStacksCannotMutateTheRecipe() {
        SpiritTradeRecipeRegistry.Recipe recipe = SpiritTradeRecipeRegistry.getForJob(SpiritJobType.TRADER_OTHERROCK);
        for (int metadata = 0; metadata <= 6; metadata++) {
            assertTrue(recipe.matches(new ItemStack(Blocks.STONE, 1, metadata)));
        }
        assertFalse(recipe.matches(new ItemStack(Blocks.COBBLESTONE)));
        // A newly contributed ore member must appear in both processing and the guide.
        Item customStone = new Item().setRegistryName("occultism_test", "trade_stone");
        ForgeRegistries.ITEMS.register(customStone);
        OreDictionary.registerOre("stone", new ItemStack(customStone));
        assertTrue(recipe.matches(new ItemStack(customStone)));
        List<ItemStack> displayed = recipe.getDisplayInputs();
        assertTrue(displayed.stream().anyMatch(s -> s.getItem() == customStone));
        displayed.get(0).setCount(59);
        recipe.getOutput().setCount(59);
        assertEquals(1, recipe.getDisplayInputs().get(0).getCount());
        assertEquals(2, recipe.getOutput().getCount());
        assertNull(SpiritTradeRecipeRegistry.getForJob(SpiritJobType.GAMBLER));
        assertNull(SpiritTradeRecipeRegistry.find("missing"));
    }

    @Test
    void entityUsesUpstreamDurationsAndCanPickUpAFourItemBatchWithAnEmptyHand() throws Exception {
        EntityFoliotJob trader = new EntityFoliotJob(null, SpiritJobType.TRADER_OTHERSTONE);
        assertTrue(trader.getHeldItemMainhand().isEmpty());
        assertEquals(4, invoke(trader, "getTraderOperationCount"));
        assertEquals(300, invoke(trader, "getProcessTime"));
        Method lookup = EntityFoliotJob.class.getDeclaredMethod("findTradeResult", ItemStack.class);
        lookup.setAccessible(true);
        ItemStack input = new ItemStack(Blocks.STONE, 7);
        assertEquals(2, ((ItemStack) lookup.invoke(trader, input)).getCount());
        assertEquals(7, input.getCount());
        trader.setJobType(SpiritJobType.TRADER_SAPLING);
        assertEquals(400, invoke(trader, "getProcessTime"));
        assertEquals(1, invoke(trader, "getTraderOperationCount"));
    }

    @Test
    void savedTradeProgressSurvivesReloadAndSwitchingJobsResetsIt() throws Exception {
        EntityFoliotJob trader = new EntityFoliotJob(null, SpiritJobType.TRADER_OTHERSTONE);
        trader.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(Blocks.STONE, 4));
        Field timer = EntityFoliotJob.class.getDeclaredField("workTimer");
        timer.setAccessible(true);
        timer.setInt(trader, 160);
        NBTTagCompound saved = new NBTTagCompound();
        trader.writeEntityToNBT(saved);
        EntityFoliotJob restored = new EntityFoliotJob(null);
        restored.readEntityFromNBT(saved);
        assertEquals(160, timer.getInt(restored));
        restored.setJobType(SpiritJobType.TRADER_SAPLING);
        assertEquals(0, timer.getInt(restored));
        // Exercise legacy saves separately: the new upstream field has precedence.
        saved.removeTag("conversionTimer");
        saved.setInteger("WorkTimer", -100);
        restored.readEntityFromNBT(saved);
        assertEquals(0, timer.getInt(restored));
        saved.setInteger("WorkTimer", Integer.MAX_VALUE);
        restored.readEntityFromNBT(saved);
        assertEquals(280, timer.getInt(restored));
    }

    @Test
    void shortNamesAndUpstreamRecipeIdsResolveWithoutNamespaceCollisions() {
        SpiritTradeRecipeRegistry.Recipe recipe = SpiritTradeRecipeRegistry.find("stone_to_otherstone");
        assertSame(recipe, SpiritTradeRecipeRegistry.find("spirit_trade/stone_to_otherstone"));
        assertSame(recipe, SpiritTradeRecipeRegistry.find("occultism:stone_to_otherstone"));
        assertSame(recipe, SpiritTradeRecipeRegistry.find("occultism:spirit_trade/stone_to_otherstone"));
        assertNull(SpiritTradeRecipeRegistry.find("anothermod:spirit_trade/stone_to_otherstone"));
        assertNull(SpiritTradeRecipeRegistry.find("Bad ID"));
        assertEquals(SpiritJobType.TRADER_OTHERSTONE, SpiritJobType.fromId("occultism:trade_otherstone_t1"));
        assertEquals(SpiritJobType.TRADER_SAPLING, SpiritJobType.fromId("occultism:trade_otherworld_saplings_t1"));
    }

    @Test
    void runtimeReplacementAndRemovalReachExistingEntitiesAndInvalidEditsAreAtomic() throws Exception {
        SpiritTradeRecipeRegistry.Recipe original = SpiritTradeRecipeRegistry.find("stone_to_otherstone");
        EntityFoliotJob trader = new EntityFoliotJob(null, SpiritJobType.TRADER_OTHERSTONE);
        Method lookup = EntityFoliotJob.class.getDeclaredMethod("findTradeResult", ItemStack.class);
        lookup.setAccessible(true);
        try {
            SpiritTradeRecipeRegistry.Recipe replacement = SpiritTradeRecipeRegistry.register(
                    "occultism:spirit_trade/stone_to_otherstone", SpiritJobType.TRADER_OTHERSTONE,
                    Collections.singletonList(new ItemStack(Items.IRON_INGOT)), null,
                    new ItemStack(Items.GOLD_INGOT, 3), 7, 2);
            assertEquals(140, invoke(trader, "getProcessTime"));
            assertEquals(2, invoke(trader, "getTraderOperationCount"));
            assertEquals(3, ((ItemStack) lookup.invoke(trader, new ItemStack(Items.IRON_INGOT))).getCount());
            assertTrue(((ItemStack) lookup.invoke(trader, new ItemStack(Blocks.STONE))).isEmpty());
            assertThrows(IllegalArgumentException.class, () -> SpiritTradeRecipeRegistry.register(
                    "stone_to_otherstone", SpiritJobType.TRADER_OTHERSTONE, Collections.emptyList(),
                    null, new ItemStack(Items.DIAMOND), 10, 1));
            assertSame(replacement, SpiritTradeRecipeRegistry.find("stone_to_otherstone"));
            SpiritTradeRecipeRegistry.remove("stone_to_otherstone");
            assertTrue(((ItemStack) lookup.invoke(trader, new ItemStack(Items.IRON_INGOT))).isEmpty());
            trader.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(Items.IRON_INGOT, 4));
            invoke(trader, "updateJob");
            assertEquals(4, trader.getHeldItemMainhand().getCount(), "removed recipes must retain held items");
        } finally {
            SpiritTradeRecipeRegistry.restore(original);
        }
    }

    @Test
    void selectedRecipeAndOverridesRoundTripUsingUpstreamNbtKeys() throws Exception {
        String id = "occultism_test:spirit_trade/iron_for_gold";
        try {
            SpiritTradeRecipeRegistry.register(id, SpiritJobType.TRADER_OTHERSTONE,
                    Collections.singletonList(new ItemStack(Items.IRON_INGOT)), null,
                    new ItemStack(Items.GOLD_INGOT), 9, 2);
            EntityFoliotJob trader = new EntityFoliotJob(null, SpiritJobType.TRADER_OTHERSTONE);
            trader.setTradeRecipeId(id);
            trader.setTimeToConvert(11);
            trader.setMaxTradesPerRound(3);
            Field timer = EntityFoliotJob.class.getDeclaredField("workTimer");
            timer.setAccessible(true);
            timer.setInt(trader, 80);
            NBTTagCompound saved = new NBTTagCompound();
            trader.writeEntityToNBT(saved);
            assertEquals(id, saved.getString("spiritTradeId"));
            assertEquals(11, saved.getInteger("timeToConvert"));
            assertEquals(3, saved.getInteger("maxTradesPerRound"));
            assertEquals(4, saved.getInteger("conversionTimer"));
            saved.setInteger("WorkTimer", Integer.MAX_VALUE);
            EntityFoliotJob restored = new EntityFoliotJob(null);
            restored.readEntityFromNBT(saved);
            assertEquals(id, restored.getSpiritTradeId());
            assertEquals(220, invoke(restored, "getProcessTime"));
            assertEquals(3, invoke(restored, "getTraderOperationCount"));
            assertEquals(80, timer.getInt(restored), "canonical seconds take precedence over legacy ticks");
            restored.setJobType(SpiritJobType.TRADER_SAPLING);
            assertEquals("occultism:spirit_trade/otherworld_sapling", restored.getSpiritTradeId());
            assertEquals(400, invoke(restored, "getProcessTime"));
            assertEquals(1, invoke(restored, "getTraderOperationCount"));
        } finally {
            SpiritTradeRecipeRegistry.remove(id);
        }
    }

    @Test
    void missingAndMalformedSelectionsNeverFallBackAndMissingSelectionsCanRecover() throws Exception {
        EntityFoliotJob trader = new EntityFoliotJob(null, SpiritJobType.TRADER_OTHERSTONE);
        String id = "occultism_test:missing_trade";
        trader.setTradeRecipeId(id);
        trader.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(Blocks.STONE, 4));
        Field timer = EntityFoliotJob.class.getDeclaredField("workTimer");
        timer.setAccessible(true);
        timer.setInt(trader, 100);
        invoke(trader, "updateJob");
        assertEquals(4, trader.getHeldItemMainhand().getCount());
        assertEquals(0, timer.getInt(trader));
        Method lookup = EntityFoliotJob.class.getDeclaredMethod("findTradeResult", ItemStack.class);
        lookup.setAccessible(true);
        try {
            SpiritTradeRecipeRegistry.register(id, SpiritJobType.TRADER_OTHERSTONE,
                    Collections.singletonList(new ItemStack(Blocks.STONE)), null,
                    new ItemStack(Items.GOLD_INGOT), 3, 2);
            assertEquals(Items.GOLD_INGOT, ((ItemStack) lookup.invoke(trader, new ItemStack(Blocks.STONE))).getItem());
        } finally {
            SpiritTradeRecipeRegistry.remove(id);
        }
        NBTTagCompound saved = new NBTTagCompound();
        trader.writeEntityToNBT(saved);
        saved.setString("spiritTradeId", "invalid ID");
        saved.setInteger("timeToConvert", Integer.MAX_VALUE);
        saved.setInteger("maxTradesPerRound", Integer.MAX_VALUE);
        saved.setInteger("conversionTimer", Integer.MAX_VALUE);
        trader.readEntityFromNBT(saved);
        assertTrue(((ItemStack) lookup.invoke(trader, new ItemStack(Blocks.STONE))).isEmpty());
        assertEquals(SpiritTradeRecipeRegistry.MAX_CONVERSION_STEPS * 20, invoke(trader, "getProcessTime"));
        assertEquals(64, invoke(trader, "getTraderOperationCount"));
        assertTrue(timer.getInt(trader) >= 0);
    }

    @Test
    void wildcardMetadataAndTaggedInputAreMatchedWithoutMutatingCallerData() {
        String id = "occultism_test:tagged_trade";
        ItemStack template = new ItemStack(Items.DYE, 32, OreDictionary.WILDCARD_VALUE);
        NBTTagCompound marker = new NBTTagCompound();
        marker.setString("tradeMarker", "blue");
        template.setTagCompound(marker);
        try {
            SpiritTradeRecipeRegistry.Recipe recipe = SpiritTradeRecipeRegistry.register(id,
                    SpiritJobType.TRADER_OTHERSTONE, Collections.singletonList(template), null,
                    new ItemStack(Items.GOLD_INGOT, 2), 2, 4);
            template.getTagCompound().setString("tradeMarker", "changed");
            ItemStack input = new ItemStack(Items.DYE, 5, 4);
            assertFalse(recipe.matches(input));
            NBTTagCompound correct = new NBTTagCompound();
            correct.setString("tradeMarker", "blue");
            input.setTagCompound(correct);
            assertTrue(recipe.matches(input));
            assertEquals(4, recipe.trade(input, 2).getCount());
            assertEquals(3, input.getCount());
            assertEquals("blue", input.getTagCompound().getString("tradeMarker"));
            assertTrue(recipe.trade(input, 0).isEmpty());
            assertEquals(3, input.getCount());
        } finally {
            SpiritTradeRecipeRegistry.remove(id);
        }
    }

    @Test
    void generatedBookUsesTheSharedTradePage() throws Exception {
        String book = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(
                "src/main/resources/assets/occultism/patchouli_books/dictionary_of_spirits/en_us/entries/"
                        + "summoning_rituals/summon_otherstone_trader.json")),
                java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(book.contains("\"type\": \"occultism:spirit_trade\""));
        assertTrue(book.contains("\"recipe\": \"stone_to_otherstone\""));
    }

    private static Object invoke(EntityFoliotJob entity, String methodName) throws Exception {
        Method method = EntityFoliotJob.class.getDeclaredMethod(methodName);
        method.setAccessible(true);
        return method.invoke(entity);
    }
}
