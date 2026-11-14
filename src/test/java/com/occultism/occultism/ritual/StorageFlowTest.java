package com.occultism.occultism.ritual;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.occultism.occultism.block.BlockStorageStabilizer;
import com.occultism.occultism.config.ModConfig;
import com.occultism.occultism.container.ContainerStorageController;
import com.occultism.occultism.item.ItemBlockStorageController;
import com.occultism.occultism.registry.ModBlocks;
import com.occultism.occultism.registry.ModItems;
import com.occultism.occultism.registry.ModOreDictionary;
import com.occultism.occultism.tileentity.StorageSortDirection;
import com.occultism.occultism.tileentity.StorageSortType;
import com.occultism.occultism.tileentity.TileEntityStorageController;
import com.occultism.occultism.util.ItemNBTUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.JsonContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StorageFlowTest {
    private static final BlockPos CONTROLLER = new BlockPos(24, 0, 0);
    private static final BlockPos STABILIZER = CONTROLLER.up().east(3);
    private StorageWorld world;
    private TileEntityStorageController controller;
    private TestPlayer player;
    private int oldLimit;
    private boolean oldOverride;
    private Field definitions;
    private Object originalDefinitions;

    @BeforeAll
    static void bootstrap() throws Exception {
        RitualBundleLoadTest.bootstrap();
        ModOreDictionary.register();
        if (TileEntity.getKey(TileEntityStorageController.class) == null) {
            TileEntity.register("occultism:storage_controller", TileEntityStorageController.class);
        }
        if (TileEntity.getKey(com.occultism.occultism.tileentity.TileEntitySacrificialBowl.class) == null) {
            TileEntity.register("occultism:sacrificial_bowl",
                    com.occultism.occultism.tileentity.TileEntitySacrificialBowl.class);
        }
    }

    @BeforeEach
    void setup() throws Exception {
        oldLimit = ModConfig.STORAGE.controllerStackSize;
        oldOverride = ModConfig.STORAGE.overrideItemStackSizes;
        ModConfig.STORAGE.controllerStackSize = 1024;
        ModConfig.STORAGE.overrideItemStackSizes = true;
        world = new StorageWorld();
        player = new TestPlayer(world);
        player.setPosition(24.5, 0.5, 0.5);
        world.playerEntities.add(player);
        controller = install(CONTROLLER);
        definitions = RitualRecipeManager.class.getDeclaredField("definitions");
        definitions.setAccessible(true);
        originalDefinitions = definitions.get(null);
        RitualRecipeManager.load();
    }

    @AfterEach
    void restore() throws Exception {
        ModConfig.STORAGE.controllerStackSize = oldLimit;
        ModConfig.STORAGE.overrideItemStackSizes = oldOverride;
        definitions.set(null, originalDefinitions);
    }

    @Test
    void realBaseAndMatrixRitualsCraftControllerThatAcceptsAndReturnsItems() throws Exception {
        ItemStack base = craftRitual("craft_storage_controller_base");
        ItemStack matrix = craftRitual("craft_dimensional_matrix");
        JsonObject json = new JsonParser().parse(Files.readString(Path.of(
                "src/main/resources/assets/occultism/recipes/storage_controller.json"))).getAsJsonObject();
        var recipe = CraftingHelper.getRecipe(json, new JsonContext("occultism"));
        InventoryCrafting grid = new InventoryCrafting(new Container() {
            @Override
            public boolean canInteractWith(EntityPlayer p) { return true; }
        }, 3, 3);
        grid.setInventorySlotContents(0, matrix);
        grid.setInventorySlotContents(3, base);
        assertTrue(recipe.matches(grid, world));
        ItemStack crafted = recipe.getCraftingResult(grid);
        assertEquals(Item.getItemFromBlock(ModBlocks.STORAGE_CONTROLLER), crafted.getItem());
        assertTrue(place(crafted, CONTROLLER));
        controller = (TileEntityStorageController) world.getTileEntity(CONTROLLER);
        assertTrue(controller.insertIntoStorage(new ItemStack(Items.DIAMOND, 1500), false).isEmpty());
        var entry = controller.getVirtualStorageEntries().getFirst();
        assertEquals(1500, entry.getCount());
        ItemStack taken = controller.extractVirtualStack(entry, 1400, false);
        assertEquals(1400, taken.getCount());
        assertEquals(100, controller.getAvailableAmount(stack -> stack.getItem() == Items.DIAMOND));
    }

    @Test
    void simulationIsNonMutatingAndNbtVariantsRemainSeparateAcrossReload() {
        ItemStack red = namedDiamonds("red", 900);
        ItemStack blue = namedDiamonds("blue", 300);
        var before = controller.getMessageUpdateStacks();
        assertTrue(controller.insertIntoStorage(red, true).isEmpty());
        assertSame(before, controller.getMessageUpdateStacks());
        assertTrue(controller.isEmpty());
        controller.insertIntoStorage(red, false);
        controller.insertIntoStorage(blue, false);
        assertEquals(900, red.getCount());
        assertEquals(300, blue.getCount());
        assertEquals(2, controller.getVirtualStorageEntryCount());
        var entry = controller.getVirtualStorageEntries().stream()
                .filter(e -> "red".equals(e.getStack().getTagCompound().getString("variant"))).findFirst().orElseThrow();
        assertEquals(250, controller.extractVirtualStack(entry, 250, true).getCount());
        assertEquals(1200, controller.getAvailableAmount(stack -> true));
        NBTTagCompound saved = controller.writeToNBT(new NBTTagCompound());
        controller = install(CONTROLLER);
        controller.readFromNBT(saved);
        assertEquals(2, controller.getVirtualStorageEntryCount());
        assertEquals(1200, controller.getAvailableAmount(stack -> true));
        assertEquals(900, controller.getAvailableAmount(stack -> stack.hasTagCompound()
                && "red".equals(stack.getTagCompound().getString("variant"))));
    }

    @Test
    void stabilizerFacingAndObstructionsDetermineActualCapacity() {
        stabilizer(EnumFacing.WEST);
        controller.updateStabilizers();
        assertEquals(384, controller.getStorageSlotCount());
        stabilizer(EnumFacing.EAST);
        controller.updateStabilizers();
        assertEquals(128, controller.getStorageSlotCount());
        stabilizer(EnumFacing.WEST);
        world.states.put(CONTROLLER.up().east(), Blocks.STONE.getDefaultState());
        controller.updateStabilizers();
        assertEquals(128, controller.getStorageSlotCount());
        world.states.remove(CONTROLLER.up().east());
        controller.updateStabilizers();
        assertEquals(384, controller.getStorageSlotCount());
    }

    @Test
    void hiddenSlotsRemainHiddenAcrossSaveAndReturnWhenStabilizerIsRestored() {
        stabilizer(EnumFacing.WEST);
        controller.updateStabilizers();
        controller.getStorageItemHandler().insertItem(300, namedDiamonds("hidden", 700), false);
        world.states.remove(STABILIZER);
        controller.updateStabilizers();
        assertEquals(128, controller.getStorageSlotCount());
        assertTrue(controller.getStackInSlot(300).isEmpty());
        assertEquals(0, controller.getVirtualStorageEntryCount());
        NBTTagCompound saved = controller.writeToNBT(new NBTTagCompound());
        controller = install(CONTROLLER);
        controller.readFromNBT(saved);
        assertEquals(128, controller.getStorageSlotCount(), "Reload must not activate hidden slots without a stabilizer");
        assertTrue(controller.getStackInSlot(300).isEmpty());
        stabilizer(EnumFacing.WEST);
        controller.updateStabilizers();
        assertEquals(700, controller.getStackInSlot(300).getCount());
        assertEquals("hidden", controller.getStackInSlot(300).getTagCompound().getString("variant"));
    }

    @Test
    void portableControllerPreservesHiddenStorageMatrixOrderAndSettings() {
        stabilizer(EnumFacing.WEST);
        controller.updateStabilizers();
        controller.getStorageItemHandler().insertItem(300, namedDiamonds("hidden", 700), false);
        controller.setCraftingMatrixSlot(0, new ItemStack(Items.IRON_INGOT, 350));
        controller.setOrderStack(new ItemStack(Items.ENDER_PEARL, 400));
        controller.setSearchQuery("Diamond");
        controller.setSortType(StorageSortType.MOD);
        controller.setSortDirection(StorageSortDirection.UP);
        world.states.remove(STABILIZER);
        controller.updateStabilizers();
        NonNullList<ItemStack> drops = NonNullList.create();
        ModBlocks.STORAGE_CONTROLLER.getDrops(drops, world, CONTROLLER, world.getBlockState(CONTROLLER), 0);
        assertEquals(1, drops.size());
        assertEquals(700, TileEntityStorageController.getStoredItemCount(drops.getFirst()));
        NBTTagCompound itemNbt = drops.getFirst().writeToNBT(new NBTTagCompound());
        ItemStack portable = new ItemStack(itemNbt);
        BlockPos replacement = CONTROLLER.south(20);
        assertTrue(place(portable, replacement));
        TileEntityStorageController restored = (TileEntityStorageController) world.getTileEntity(replacement);
        assertEquals(128, restored.getStorageSlotCount());
        assertEquals(350, restored.getCraftingMatrixSlot(0).getCount());
        assertEquals(400, restored.getOrderStack().getCount());
        assertEquals("diamond", restored.getSearchQuery());
        assertEquals(StorageSortType.MOD, restored.getSortType());
        assertEquals(StorageSortDirection.UP, restored.getSortDirection());
        restored.setStorageSlotCount(384);
        assertEquals(700, restored.getStackInSlot(300).getCount());
        assertEquals(700, TileEntityStorageController.getStoredItemCount(portable));
    }

    @Test
    void lowerStackLimitDoesNotDeleteExistingOverLimitItemsDuringExtraction() {
        controller.insertIntoStorage(new ItemStack(Items.DIAMOND, 900), false);
        ModConfig.STORAGE.controllerStackSize = 64;
        ItemStack extracted = controller.getStorageItemHandler().extractItem(0, 1, false);
        assertEquals(1, extracted.getCount());
        assertEquals(899, controller.getStackInSlot(0).getCount());
        assertEquals(900, extracted.getCount() + controller.getAvailableAmount(stack -> true));
    }

    @Test
    void disablingStackOverrideDoesNotDeleteExistingPearlsDuringExtraction() {
        controller.insertIntoStorage(new ItemStack(Items.ENDER_PEARL, 300), false);
        ModConfig.STORAGE.overrideItemStackSizes = false;
        ItemStack extracted = controller.getStorageItemHandler().extractItem(0, 10, false);
        assertEquals(10, extracted.getCount());
        assertEquals(290, controller.getStackInSlot(0).getCount());
    }

    @Test
    void retainedVirtualEntryCannotExtractAnUnrelatedReplacementStack() {
        controller.setStorageSlotCount(4);
        controller.getStorageItemHandler().insertItem(0, new ItemStack(Items.DIAMOND, 50), false);
        controller.getStorageItemHandler().insertItem(2, new ItemStack(Items.DIAMOND, 70), false);
        var oldEntry = controller.getVirtualStorageEntries().getFirst();
        controller.removeStackFromSlot(0);
        controller.getStorageItemHandler().insertItem(0, new ItemStack(Items.EMERALD, 90), false);
        ItemStack taken = controller.extractVirtualStack(oldEntry, 120, false);
        assertEquals(Items.DIAMOND, taken.getItem());
        assertEquals(70, taken.getCount());
        assertEquals(90, controller.getStackInSlot(0).getCount());
    }

    @Test
    void unloadedStabilizerRayStopsWithoutReadingUnloadedBlocks() {
        stabilizer(EnumFacing.WEST);
        world.unloaded.add(CONTROLLER.up().east());
        controller.updateStabilizers();
        assertEquals(128, controller.getStorageSlotCount());
        world.unloaded.clear();
        controller.updateStabilizers();
        assertEquals(384, controller.getStorageSlotCount());
    }

    @Test
    void stabilizerNotificationSkipsUnloadedTilePositions() {
        stabilizer(EnumFacing.WEST);
        world.unloaded.add(STABILIZER.down().west());
        ((BlockStorageStabilizer) ModBlocks.STORAGE_STABILIZER_TIER1).notifyStorageControllers(
                world, STABILIZER, world.getBlockState(STABILIZER));
        assertEquals(384, controller.getStorageSlotCount());
    }

    @Test
    void playerShiftInsertAndContainerCloseReturnMatrixWithoutDuplication() {
        ContainerStorageController terminal = new ContainerStorageController(player.inventory, controller);
        player.inventory.setInventorySlotContents(9, new ItemStack(Items.DIAMOND, 64));
        terminal.transferStackInSlot(player, 11);
        assertTrue(player.inventory.getStackInSlot(9).isEmpty());
        assertEquals(64, controller.getAvailableAmount(stack -> stack.getItem() == Items.DIAMOND));
        terminal.getCraftMatrix().setInventorySlotContents(0, new ItemStack(Items.IRON_INGOT, 3));
        terminal.onContainerClosed(player);
        assertTrue(terminal.getCraftMatrix().getStackInSlot(0).isEmpty());
        assertTrue(controller.getCraftingMatrixSlot(0).isEmpty());
        int totalIron = controller.getAvailableAmount(stack -> stack.getItem() == Items.IRON_INGOT);
        for (ItemStack stack : player.inventory.mainInventory) {
            if (stack.getItem() == Items.IRON_INGOT) {
                totalIron += stack.getCount();
            }
        }
        assertEquals(3, totalIron);
        terminal.onContainerClosed(player);
        assertEquals(3, controller.getAvailableAmount(stack -> stack.getItem() == Items.IRON_INGOT));
    }

    @Test
    void movingExpandedControllerRecalculatesCapacityAtTheNewLocation() {
        stabilizer(EnumFacing.WEST);
        controller.updateStabilizers();
        controller.getStorageItemHandler().insertItem(300, new ItemStack(Items.DIAMOND, 700), false);
        NonNullList<ItemStack> drops = NonNullList.create();
        ModBlocks.STORAGE_CONTROLLER.getDrops(drops, world, CONTROLLER, world.getBlockState(CONTROLLER), 0);
        BlockPos target = CONTROLLER.south(20);
        assertTrue(place(drops.getFirst(), target));
        TileEntityStorageController moved = (TileEntityStorageController) world.getTileEntity(target);
        assertEquals(128, moved.getStorageSlotCount());
        assertTrue(moved.getStackInSlot(300).isEmpty());
        world.states.put(target.up().east(3), ModBlocks.STORAGE_STABILIZER_TIER1.getDefaultState()
                .withProperty(BlockStorageStabilizer.FACING, EnumFacing.WEST));
        moved.updateStabilizers();
        assertEquals(700, moved.getStackInSlot(300).getCount());
    }

    @Test
    void remoteContainerClosesWithMatrixOnTheItemAndKeepsTheRemoteLocked() {
        ItemStack remote = new ItemStack(ModItems.STORAGE_REMOTE);
        com.occultism.occultism.item.ItemStorageRemote.setLinkedController(remote,
                new com.occultism.occultism.api.common.data.GlobalBlockPos(CONTROLLER, 0));
        player.inventory.setInventorySlotContents(0, remote);
        ContainerStorageController terminal = new ContainerStorageController(player.inventory, controller, 0);
        terminal.getCraftMatrix().setInventorySlotContents(0, new ItemStack(Items.IRON_INGOT, 3));
        assertTrue(terminal.getSlot(38).getStack().isEmpty());
        terminal.transferStackInSlot(player, 38);
        terminal.onContainerClosed(player);
        assertSame(remote, player.inventory.getStackInSlot(0));
        assertEquals(3, com.occultism.occultism.item.ItemStorageRemote.getCraftingMatrix(remote).getFirst().getCount());
        assertEquals(0, controller.getAvailableAmount(stack -> stack.getItem() == Items.IRON_INGOT));
        ContainerStorageController reopened = new ContainerStorageController(player.inventory, controller, 0);
        assertEquals(3, reopened.getCraftMatrix().getStackInSlot(0).getCount());
        reopened.onContainerClosed(player);
        assertEquals(3, com.occultism.occultism.item.ItemStorageRemote.getCraftingMatrix(remote).getFirst().getCount());
    }

    private ItemStack craftRitual(String id) {
        world.states.keySet().removeIf(pos -> Math.abs(pos.getX()) <= 10 && Math.abs(pos.getZ()) <= 10);
        world.tiles.keySet().removeIf(pos -> Math.abs(pos.getX()) <= 10 && Math.abs(pos.getZ()) <= 10);
        var ritual = RitualRecipeManager.getById(id);
        var pattern = PentacleDetector.getDefinitionById(ritual.getPentacleId());
        for (var symbol : PentacleDetector.getRequiredSymbols(pattern, BlockPos.ORIGIN)) {
            List<IBlockState> states = new ArrayList<>();
            for (var glyph : ModBlocks.CHALK_GLYPHS) {
                states.add(glyph.getDefaultState());
            }
            states.add(ModBlocks.LARGE_CANDLE.getDefaultState());
            states.add(ModBlocks.SPIRIT_ATTUNED_CRYSTAL.getDefaultState());
            states.add(Blocks.SKULL.getDefaultState());
            IBlockState state = states.stream().filter(candidate -> PentacleDetector.matchesPreviewSymbol(
                    pattern, symbol.getSymbol(), candidate)).findFirst().orElseThrow();
            world.states.put(symbol.getPos(), state);
            if (state.getBlock() == Blocks.SKULL) {
                TileEntitySkull skull = new TileEntitySkull();
                skull.setWorld(world);
                skull.setPos(symbol.getPos());
                skull.setType(0);
                world.tiles.put(symbol.getPos(), skull);
            }
        }
        var bowl = world.installBowl(BlockPos.ORIGIN, true);
        List<com.occultism.occultism.tileentity.TileEntitySacrificialBowl> offerings = new ArrayList<>();
        int cost = 0;
        for (int x = -8; x <= 8 && cost < ritual.getAdditionalIngredients().size(); x++) {
            for (int z = -8; z <= 8 && cost < ritual.getAdditionalIngredients().size(); z++) {
                BlockPos pos = new BlockPos(x, 0, z);
                if (world.getBlockState(pos).getBlock() == Blocks.AIR) {
                    var offering = world.installBowl(pos, false);
                    offering.setStack(ritual.getAdditionalIngredients().get(cost++).createDisplayStack());
                    offerings.add(offering);
                }
            }
        }
        ItemStack book = ritual.getActivationIngredient().createDisplayStack();
        ItemNBTUtil.setBoundSpiritName(book, "Astaroth");
        player.setHeldItem(EnumHand.MAIN_HAND, book);
        ModBlocks.GOLDEN_SACRIFICIAL_BOWL.onBlockActivated(world, BlockPos.ORIGIN,
                world.getBlockState(BlockPos.ORIGIN), player, EnumHand.MAIN_HAND, EnumFacing.UP, 0.5F, 0.5F, 0.5F);
        assertEquals(id, bowl.getActiveRitualId());
        for (int i = 0; i < ritual.getDurationTicks(); i++) {
            bowl.update();
        }
        assertFalse(bowl.isRitualActive());
        assertTrue(offerings.stream().allMatch(offering -> offering.isEmpty()));
        assertEquals(1, world.drops.size());
        ItemStack result = world.drops.getFirst().getItem().copy();
        world.drops.clear();
        return result;
    }

    private void stabilizer(EnumFacing facing) {
        world.states.put(STABILIZER, ModBlocks.STORAGE_STABILIZER_TIER1.getDefaultState()
                .withProperty(BlockStorageStabilizer.FACING, facing));
    }

    private TileEntityStorageController install(BlockPos pos) {
        var result = new TileEntityStorageController();
        result.setWorld(world);
        result.setPos(pos);
        world.states.put(pos, ModBlocks.STORAGE_CONTROLLER.getDefaultState());
        world.tiles.put(pos, result);
        return result;
    }

    private boolean place(ItemStack stack, BlockPos pos) {
        return new ItemBlockStorageController(ModBlocks.STORAGE_CONTROLLER).placeBlockAt(stack, player, world,
                pos, EnumFacing.UP, 0.5F, 0.5F, 0.5F, ModBlocks.STORAGE_CONTROLLER.getDefaultState());
    }

    private static ItemStack namedDiamonds(String variant, int count) {
        ItemStack stack = new ItemStack(Items.DIAMOND, count);
        stack.setTagCompound(new NBTTagCompound());
        stack.getTagCompound().setString("variant", variant);
        return stack;
    }

    private static final class StorageWorld extends RitualTestWorld {
        private StorageWorld() { super(false); }

        @Override
        public boolean setBlockState(BlockPos pos, IBlockState state, int flags) {
            states.put(pos, state);
            if (state.getBlock().hasTileEntity(state)) {
                TileEntity tile = state.getBlock().createTileEntity(this, state);
                tile.setWorld(this);
                tile.setPos(pos);
                tiles.put(pos, tile);
            }
            return true;
        }

        @Override
        public boolean isAirBlock(BlockPos pos) {
            return getBlockState(pos).getBlock() == Blocks.AIR;
        }

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
            super(world, new GameProfile(UUID.randomUUID(), "storage_flow_test"));
        }

        @Override
        public boolean isSpectator() { return false; }

        @Override
        public boolean isCreative() { return false; }
    }
}
