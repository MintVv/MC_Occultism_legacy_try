package com.occultism.occultism.ritual;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import com.occultism.occultism.item.ItemMinerSpirit;
import com.occultism.occultism.mineshaft.DimensionalMineshaftRecipe;
import com.occultism.occultism.registry.ModBlocks;
import com.occultism.occultism.registry.ModItems;
import com.occultism.occultism.registry.ModOreDictionary;
import com.occultism.occultism.tileentity.TileEntityDimensionalMineshaft;
import com.occultism.occultism.tileentity.TileEntitySacrificialBowl;
import com.occultism.occultism.util.ItemNBTUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MineshaftFlowTest {
    private static final BlockPos SHAFT_POS = new BlockPos(24, 2, 0);
    private MiningWorld world;
    private TileEntityDimensionalMineshaft shaft;
    private List<DimensionalMineshaftRecipe> originalMiningRecipes;
    private Object originalRituals;
    private Field definitions;

    @BeforeAll
    static void bootstrap() throws Exception {
        RitualBundleLoadTest.bootstrap();
        ModOreDictionary.register();
        if (TileEntity.getKey(TileEntityDimensionalMineshaft.class) == null) {
            TileEntity.register("occultism:dimensional_mineshaft", TileEntityDimensionalMineshaft.class);
        }
        if (TileEntity.getKey(TileEntitySacrificialBowl.class) == null) {
            TileEntity.register("occultism:sacrificial_bowl", TileEntitySacrificialBowl.class);
        }
    }

    @BeforeEach
    void setup() throws Exception {
        world = new MiningWorld(false);
        shaft = installShaft(world);
        originalMiningRecipes = new ArrayList<>(DimensionalMineshaftRecipe.getRecipes());
        definitions = RitualRecipeManager.class.getDeclaredField("definitions");
        definitions.setAccessible(true);
        originalRituals = definitions.get(null);
        RitualRecipeManager.load();
    }

    @AfterEach
    void restore() throws Exception {
        DimensionalMineshaftRecipe.removeRecipes(recipe -> true);
        for (DimensionalMineshaftRecipe recipe : originalMiningRecipes) {
            DimensionalMineshaftRecipe.addRecipe(recipe.getMiner(), recipe.getOutputs());
        }
        definitions.set(null, originalRituals);
    }

    @Test
    void realMinerRitualProducesNamedToolThatWorksInTheRealMineshaft() {
        ItemStack miner = craftRitual("craft_miner_foliot_unspecialized");
        assertEquals(ModItems.MINER_FOLIOT_UNSPECIALIZED, miner.getItem());
        assertEquals("Astaroth", ItemNBTUtil.getBoundSpiritName(miner));
        assertEquals(0, miner.getItemDamage());
        TestPlayer player = new TestPlayer(world);
        player.setHeldItem(EnumHand.MAIN_HAND, miner.copy());
        assertTrue(ModBlocks.DIMENSIONAL_MINESHAFT.onBlockActivated(world, SHAFT_POS,
                world.getBlockState(SHAFT_POS), player, EnumHand.MAIN_HAND, EnumFacing.UP, 0.5F, 0.5F, 0.5F));
        assertTrue(player.getHeldItemMainhand().isEmpty());
        tick(1);
        assertEquals(400, shaft.getField(0));
        tick(399);
        assertFalse(shaft.hasOutputs());
        tick(1);
        assertEquals(1, shaft.getStackInSlot(0).getItemDamage());
        assertEquals(1, outputCount());
        ItemStack output = shaft.takeFirstOutput();
        assertTrue(DimensionalMineshaftRecipe.find(miner).getOutputs().stream().anyMatch(candidate ->
                ItemStack.areItemsEqual(candidate.getStack(), output)));
        assertEquals("Astaroth", ItemNBTUtil.getBoundSpiritName(shaft.getStackInSlot(0)));
    }

    @Test
    void realMineshaftRitualConsumesItsSevenCostsAndCreatesOneBlock() {
        ItemStack result = craftRitual("craft_dimensional_mineshaft");
        assertEquals(Item.getItemFromBlock(ModBlocks.DIMENSIONAL_MINESHAFT), result.getItem());
        assertEquals(1, result.getCount());
        assertEquals("", ItemNBTUtil.getBoundSpiritName(result));
    }

    @Test
    void saveRestoresCountdownInventoryAndMinerDataWithoutExtraProduction() {
        deterministic(ModItems.MINER_FOLIOT_UNSPECIALIZED, new ItemStack(Items.DIAMOND));
        ItemStack miner = new ItemStack(ModItems.MINER_FOLIOT_UNSPECIALIZED);
        ItemNBTUtil.setBoundSpiritName(miner, "Astaroth");
        shaft.setInventorySlotContents(0, miner);
        tick(124);
        assertEquals(277, shaft.getField(0));
        NBTTagCompound saved = shaft.writeToNBT(new NBTTagCompound());
        shaft = installShaft(world);
        shaft.readFromNBT(saved);
        assertEquals(277, shaft.getField(0));
        assertEquals("Astaroth", ItemNBTUtil.getBoundSpiritName(shaft.getStackInSlot(0)));
        tick(276);
        assertEquals(0, outputCount());
        tick(1);
        assertEquals(1, outputCount());
        assertEquals(1, shaft.getStackInSlot(0).getItemDamage());
        tick(400);
        assertEquals(2, outputCount());
        assertEquals(2, shaft.getStackInSlot(0).getItemDamage());
    }

    @Test
    void fullOutputDoesNotConsumeDurabilityAndCanResumeAfterSpaceIsFreed() {
        deterministic(ModItems.MINER_FOLIOT_UNSPECIALIZED, new ItemStack(Items.DIAMOND));
        shaft.setInventorySlotContents(0, new ItemStack(ModItems.MINER_FOLIOT_UNSPECIALIZED));
        fillOutputs();
        tick(801);
        assertEquals(0, shaft.getStackInSlot(0).getItemDamage());
        assertTrue(world.drops.isEmpty());
        shaft.setInventorySlotContents(1, ItemStack.EMPTY);
        tick(400);
        assertEquals(Items.DIAMOND, shaft.getStackInSlot(1).getItem());
        assertEquals(1, shaft.getStackInSlot(1).getCount());
        assertEquals(1, shaft.getStackInSlot(0).getItemDamage());
    }

    @Test
    void redstoneStopsProductionAndRestartsWithAFullCountdown() {
        deterministic(ModItems.MINER_FOLIOT_UNSPECIALIZED, new ItemStack(Items.DIAMOND));
        shaft.setInventorySlotContents(0, new ItemStack(ModItems.MINER_FOLIOT_UNSPECIALIZED));
        tick(200);
        world.powered = true;
        tick(500);
        assertEquals(0, outputCount());
        assertEquals(0, shaft.getStackInSlot(0).getItemDamage());
        assertEquals(0, shaft.getField(0));
        world.powered = false;
        tick(400);
        assertEquals(0, outputCount());
        tick(1);
        assertEquals(1, outputCount());
    }

    @Test
    void removedMinerCannotFinishAnOldOperationAndReplacementStartsFresh() {
        deterministic(ModItems.MINER_FOLIOT_UNSPECIALIZED, new ItemStack(Items.DIAMOND));
        shaft.setInventorySlotContents(0, new ItemStack(ModItems.MINER_FOLIOT_UNSPECIALIZED));
        tick(400);
        ItemStack miner = shaft.takeMiner();
        tick(500);
        assertEquals(0, outputCount());
        assertEquals(0, miner.getItemDamage());
        shaft.setInventorySlotContents(0, miner);
        tick(400);
        assertEquals(0, outputCount());
        tick(1);
        assertEquals(1, outputCount());
    }

    @Test
    void noRegisteredOutputRecipeNeverDamagesTheMiner() {
        DimensionalMineshaftRecipe.removeByMiner(new ItemStack(ModItems.MINER_FOLIOT_UNSPECIALIZED));
        shaft.setInventorySlotContents(0, new ItemStack(ModItems.MINER_FOLIOT_UNSPECIALIZED));
        tick(801);
        assertEquals(0, outputCount());
        assertEquals(0, shaft.getStackInSlot(0).getItemDamage());
        deterministic(ModItems.MINER_FOLIOT_UNSPECIALIZED, new ItemStack(Items.DIAMOND));
        tick(400);
        assertEquals(1, outputCount());
        assertEquals(1, shaft.getStackInSlot(0).getItemDamage());
    }

    @Test
    void nearlyBrokenMinerMovesToOutputWithNbtAndEnchantmentIntact() {
        deterministic(ModItems.MINER_FOLIOT_UNSPECIALIZED, new ItemStack(Items.DIAMOND));
        ItemStack miner = new ItemStack(ModItems.MINER_FOLIOT_UNSPECIALIZED);
        miner.setItemDamage(miner.getMaxDamage() - 2);
        ItemNBTUtil.setBoundSpiritName(miner, "Astaroth");
        miner.addEnchantment(Enchantments.EFFICIENCY, 1);
        shaft.setInventorySlotContents(0, miner);
        tick(371);
        assertTrue(shaft.getStackInSlot(0).isEmpty());
        List<ItemStack> saved = outputs().stream().filter(stack -> stack.getItem() == miner.getItem()).toList();
        assertEquals(1, saved.size());
        assertEquals(miner.getMaxDamage() - 1, saved.getFirst().getItemDamage());
        assertEquals("Astaroth", ItemNBTUtil.getBoundSpiritName(saved.getFirst()));
        assertEquals(1, net.minecraft.enchantment.EnchantmentHelper.getEnchantmentLevel(
                Enchantments.EFFICIENCY, saved.getFirst()));
        tick(500);
        assertEquals(2, outputCount());
    }

    @Test
    void savingMinerAndDropsMustBothFitBeforeAnythingIsConsumed() {
        deterministic(ModItems.MINER_FOLIOT_UNSPECIALIZED, new ItemStack(Items.DIAMOND, 64));
        ItemStack miner = new ItemStack(ModItems.MINER_FOLIOT_UNSPECIALIZED);
        miner.setItemDamage(miner.getMaxDamage() - 2);
        shaft.setInventorySlotContents(0, miner);
        fillOutputs();
        shaft.setInventorySlotContents(1, ItemStack.EMPTY);
        tick(401);
        assertEquals(miner.getMaxDamage() - 2, shaft.getStackInSlot(0).getItemDamage());
        assertTrue(shaft.getStackInSlot(1).isEmpty());
        shaft.setInventorySlotContents(2, ItemStack.EMPTY);
        tick(400);
        assertTrue(shaft.getStackInSlot(0).isEmpty());
        assertEquals(Items.DIAMOND, shaft.getStackInSlot(1).getItem());
        assertEquals(64, shaft.getStackInSlot(1).getCount());
        assertEquals(miner.getItem(), shaft.getStackInSlot(2).getItem());
        assertTrue(world.drops.isEmpty());
    }

    @Test
    void extractorReceivesOutputAndFullExtractorFallsBackToInternalSlots() {
        deterministic(ModItems.MINER_FOLIOT_UNSPECIALIZED, new ItemStack(Items.DIAMOND));
        TileEntityChest chest = new TileEntityChest();
        chest.setWorld(world);
        chest.setPos(SHAFT_POS.down(2));
        world.tiles.put(chest.getPos(), chest);
        world.states.put(chest.getPos(), Blocks.CHEST.getDefaultState());
        world.states.put(SHAFT_POS.down(), ModBlocks.DIMENSIONAL_EXTRACTOR.getDefaultState());
        shaft.setInventorySlotContents(0, new ItemStack(ModItems.MINER_FOLIOT_UNSPECIALIZED));
        tick(401);
        assertEquals(1, chest.getStackInSlot(0).getCount());
        assertEquals(0, outputCount());
        for (int i = 0; i < chest.getSizeInventory(); i++) {
            chest.setInventorySlotContents(i, new ItemStack(Items.STICK, 64));
        }
        tick(400);
        assertEquals(1, outputCount());
        assertEquals(2, shaft.getStackInSlot(0).getItemDamage());
        assertTrue(world.drops.isEmpty());
    }

    @Test
    void localEnchantmentsChangeTimingAndOutputButDamageOnlyOncePerOperation() {
        deterministic(ModItems.MINER_FOLIOT_UNSPECIALIZED, new ItemStack(Items.DIAMOND));
        ItemStack miner = new ItemStack(ModItems.MINER_FOLIOT_UNSPECIALIZED);
        miner.addEnchantment(Enchantments.EFFICIENCY, 2);
        miner.addEnchantment(Enchantments.FORTUNE, 3);
        miner.addEnchantment(Enchantments.SILK_TOUCH, 1);
        shaft.setInventorySlotContents(0, miner);
        tick(340);
        assertEquals(0, outputCount());
        tick(1);
        assertEquals(8, outputCount());
        assertEquals(1, shaft.getStackInSlot(0).getItemDamage());
    }

    @Test
    void sidedInventoryAcceptsMinerFromTopAndExtractsOutputsFromBottom() {
        SidedInvWrapper top = new SidedInvWrapper(shaft, EnumFacing.UP);
        SidedInvWrapper bottom = new SidedInvWrapper(shaft, EnumFacing.DOWN);
        assertEquals(1, top.getSlots());
        assertEquals(9, bottom.getSlots());
        assertEquals(2, top.insertItem(0, new ItemStack(Items.STICK, 2), false).getCount());
        assertTrue(top.insertItem(0, new ItemStack(ModItems.MINER_FOLIOT_UNSPECIALIZED), false).isEmpty());
        assertTrue(top.extractItem(0, 1, false).isEmpty());
        shaft.setInventorySlotContents(1, new ItemStack(Items.DIAMOND, 3));
        assertEquals(3, bottom.extractItem(0, 3, false).getCount());
        assertTrue(shaft.getStackInSlot(1).isEmpty());
        assertEquals(1, bottom.insertItem(0, new ItemStack(Items.DIAMOND), false).getCount());
    }

    @Test
    void clientTicksCannotProduceOrDamageAnything() {
        MiningWorld client = new MiningWorld(true);
        TileEntityDimensionalMineshaft clientShaft = installShaft(client);
        ItemStack miner = new ItemStack(ModItems.MINER_FOLIOT_UNSPECIALIZED);
        clientShaft.setInventorySlotContents(0, miner);
        for (int i = 0; i < 500; i++) {
            clientShaft.update();
        }
        assertFalse(clientShaft.hasOutputs());
        assertEquals(0, miner.getItemDamage());
        assertTrue(client.drops.isEmpty());
    }

    @Test
    void replacingMinerWithAnotherOfTheSameTypeStartsANewCountdown() {
        deterministic(ModItems.MINER_FOLIOT_UNSPECIALIZED, new ItemStack(Items.DIAMOND));
        shaft.setInventorySlotContents(0, new ItemStack(ModItems.MINER_FOLIOT_UNSPECIALIZED));
        tick(400);
        ItemStack replacement = new ItemStack(ModItems.MINER_FOLIOT_UNSPECIALIZED);
        ItemNBTUtil.setBoundSpiritName(replacement, "Replacement");
        shaft.setInventorySlotContents(0, replacement);
        tick(1);
        assertEquals(400, shaft.getField(0));
        assertEquals(0, outputCount());
        assertEquals(0, replacement.getItemDamage());
        tick(400);
        assertEquals(1, outputCount());
        assertEquals(1, replacement.getItemDamage());
    }

    @Test
    void unloadedExtractorDoesNotReadOrLoadItsTargetAndUsesInternalOutput() {
        deterministic(ModItems.MINER_FOLIOT_UNSPECIALIZED, new ItemStack(Items.DIAMOND));
        world.states.put(SHAFT_POS.down(), ModBlocks.DIMENSIONAL_EXTRACTOR.getDefaultState());
        world.unloaded.add(SHAFT_POS.down(2));
        shaft.setInventorySlotContents(0, new ItemStack(ModItems.MINER_FOLIOT_UNSPECIALIZED));
        tick(401);
        assertEquals(1, outputCount());
        assertEquals(1, shaft.getStackInSlot(0).getItemDamage());
    }

    @Test
    void multiRollCapacityCheckAccountsForTheWholeBatchWithoutPartialOutput() {
        deterministic(ModItems.MINER_FOLIOT_UNSPECIALIZED, new ItemStack(Items.DIAMOND, 32));
        ItemStack miner = new ItemStack(ModItems.MINER_FOLIOT_UNSPECIALIZED);
        miner.addEnchantment(Enchantments.FORTUNE, 2);
        shaft.setInventorySlotContents(0, miner);
        fillOutputs();
        shaft.setInventorySlotContents(1, ItemStack.EMPTY);
        tick(401);
        assertTrue(shaft.getStackInSlot(1).isEmpty());
        assertEquals(0, miner.getItemDamage());
        shaft.setInventorySlotContents(2, ItemStack.EMPTY);
        tick(400);
        assertEquals(64, shaft.getStackInSlot(1).getCount());
        assertEquals(32, shaft.getStackInSlot(2).getCount());
        assertEquals(1, miner.getItemDamage());
        assertTrue(world.drops.isEmpty());
    }

    @Test
    void eachRegisteredMinerTierUsesItsOwnShippedOutputTable() {
        for (Item item : List.of(ModItems.MINER_FOLIOT_UNSPECIALIZED, ModItems.MINER_DJINNI_ORES,
                ModItems.MINER_AFRIT_DEEPS, ModItems.MINER_MARID_MASTER, ModItems.MINER_ANCIENT_ELDRITCH)) {
            shaft.clear();
            ItemStack miner = new ItemStack(item);
            var recipe = DimensionalMineshaftRecipe.find(miner);
            assertNotNull(recipe);
            assertFalse(recipe.getOutputs().isEmpty());
            shaft.setInventorySlotContents(0, miner);
            tick(((ItemMinerSpirit) item).getMaxMiningTime() + 1);
            assertEquals(1, miner.getItemDamage());
            assertTrue(shaft.hasOutputs());
            for (ItemStack output : outputs()) {
                assertTrue(recipe.getOutputs().stream().anyMatch(candidate ->
                        ItemStack.areItemsEqual(candidate.getStack(), output)));
                assertTrue(output.getCount() <= ((ItemMinerSpirit) item).getOutputMultiplier());
            }
        }
    }

    private ItemStack craftRitual(String id) {
        RitualDefinition ritual = RitualRecipeManager.getById(id);
        assertNotNull(ritual);
        PentacleDefinition pattern = PentacleDetector.getDefinitionById(ritual.getPentacleId());
        for (var symbol : PentacleDetector.getRequiredSymbols(pattern, BlockPos.ORIGIN)) {
            List<IBlockState> candidates = new ArrayList<>();
            for (var glyph : ModBlocks.CHALK_GLYPHS) {
                candidates.add(glyph.getDefaultState());
            }
            candidates.add(ModBlocks.LARGE_CANDLE.getDefaultState());
            candidates.add(ModBlocks.SPIRIT_ATTUNED_CRYSTAL.getDefaultState());
            candidates.add(Blocks.SKULL.getDefaultState());
            IBlockState state = candidates.stream().filter(candidate -> PentacleDetector.matchesPreviewSymbol(
                    pattern, symbol.getSymbol(), candidate)).findFirst().orElseThrow();
            world.states.put(symbol.getPos(), state);
            if (state.getBlock() == Blocks.SKULL) {
                TileEntitySkull skull = new TileEntitySkull();
                skull.setWorld(world);
                skull.setPos(symbol.getPos());
                skull.setType(symbol.getSymbol() == '4' ? 1 : 0);
                world.tiles.put(symbol.getPos(), skull);
            }
        }
        TileEntitySacrificialBowl bowl = world.installBowl(BlockPos.ORIGIN, true);
        List<TileEntitySacrificialBowl> offerings = new ArrayList<>();
        int cost = 0;
        for (int x = -8; x <= 8 && cost < ritual.getAdditionalIngredients().size(); x++) {
            for (int z = -8; z <= 8 && cost < ritual.getAdditionalIngredients().size(); z++) {
                BlockPos pos = new BlockPos(x, 0, z);
                if (world.getBlockState(pos).getBlock() == Blocks.AIR) {
                    var offering = world.installBowl(pos, false);
                    ItemStack input = ritual.getAdditionalIngredients().get(cost++).createDisplayStack();
                    assertFalse(input.isEmpty());
                    offering.setStack(input);
                    offerings.add(offering);
                }
            }
        }
        TestPlayer player = new TestPlayer(world);
        world.playerEntities.add(player);
        ItemStack book = ritual.getActivationIngredient().createDisplayStack();
        ItemNBTUtil.setBoundSpiritName(book, "Astaroth");
        player.setHeldItem(EnumHand.MAIN_HAND, book);
        ModBlocks.GOLDEN_SACRIFICIAL_BOWL.onBlockActivated(world, BlockPos.ORIGIN,
                world.getBlockState(BlockPos.ORIGIN), player, EnumHand.MAIN_HAND, EnumFacing.UP, 0.5F, 0.5F, 0.5F);
        assertEquals(id, bowl.getActiveRitualId());
        assertTrue(player.getHeldItemMainhand().isEmpty());
        for (int i = 0; i < ritual.getDurationTicks() - 1; i++) {
            bowl.update();
        }
        assertTrue(world.drops.isEmpty());
        bowl.update();
        assertFalse(bowl.isRitualActive());
        assertTrue(offerings.stream().allMatch(TileEntitySacrificialBowl::isEmpty));
        assertTrue(bowl.isEmpty());
        assertEquals(1, world.drops.size());
        ItemStack result = world.drops.getFirst().getItem().copy();
        world.drops.clear();
        return result;
    }

    private void deterministic(Item miner, ItemStack output) {
        DimensionalMineshaftRecipe.addRecipe(new ItemStack(miner), List.of(
                new DimensionalMineshaftRecipe.WeightedOutput(output, 1)));
    }

    private TileEntityDimensionalMineshaft installShaft(MiningWorld target) {
        var tile = (TileEntityDimensionalMineshaft) ModBlocks.DIMENSIONAL_MINESHAFT.createTileEntity(
                target, ModBlocks.DIMENSIONAL_MINESHAFT.getDefaultState());
        tile.setWorld(target);
        tile.setPos(SHAFT_POS);
        target.tiles.put(SHAFT_POS, tile);
        target.states.put(SHAFT_POS, ModBlocks.DIMENSIONAL_MINESHAFT.getDefaultState());
        return tile;
    }

    private void fillOutputs() {
        for (int i = 1; i < shaft.getSizeInventory(); i++) {
            shaft.setInventorySlotContents(i, new ItemStack(Items.STICK, 64));
        }
    }

    private List<ItemStack> outputs() {
        List<ItemStack> result = new ArrayList<>();
        for (int i = 1; i < shaft.getSizeInventory(); i++) {
            if (!shaft.getStackInSlot(i).isEmpty()) {
                result.add(shaft.getStackInSlot(i));
            }
        }
        return result;
    }

    private int outputCount() {
        return outputs().stream().mapToInt(ItemStack::getCount).sum();
    }

    private void tick(int count) {
        for (int i = 0; i < count; i++) {
            world.setTotalWorldTime(world.getTotalWorldTime() + 1);
            shaft.update();
        }
    }

    private static final class MiningWorld extends RitualTestWorld {
        private boolean powered;

        private MiningWorld(boolean client) {
            super(client);
        }

        @Override
        public boolean isBlockPowered(BlockPos pos) { return powered; }

        @Override
        public TileEntity getTileEntity(BlockPos pos) {
            if (!isBlockLoaded(pos)) {
                throw new AssertionError("Read unloaded tile: " + pos);
            }
            return super.getTileEntity(pos);
        }
    }

    private static final class TestPlayer extends EntityPlayer {
        private TestPlayer(World world) {
            super(world, new GameProfile(UUID.randomUUID(), "mineshaft_test"));
        }

        @Override
        public boolean isSpectator() { return false; }

        @Override
        public boolean isCreative() { return false; }
    }
}
