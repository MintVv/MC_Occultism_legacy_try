package com.occultism.occultism.tileentity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import com.occultism.occultism.registry.ModBlocks;
import com.occultism.occultism.registry.ModItems;
import com.occultism.occultism.config.ModConfig;
import com.occultism.occultism.container.ContainerStorageController;
import com.occultism.occultism.network.MessageUpdateStacks;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Contract tests for the first 1.12 storage-controller capacity slice.
 *
 * <p>The full upstream controller has a virtual storage network.  These tests
 * deliberately focus on the local inventory contract needed before that work:
 * dynamic slot counts, stabilizer contributions, and lossless NBT resizing.</p>
 */
class StorageControllerContractTest {

    private static final int LEGACY_SLOT_COUNT = 54;
    private static final int BASE_SLOT_COUNT = 128;
    private static final String STORAGE_SLOT_COUNT = "StorageSlotCount";

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void freshControllerUsesUpstreamBaseCapacity() {
        TileEntityStorageController controller = new TileEntityStorageController();

        assertEquals(BASE_SLOT_COUNT, TileEntityStorageController.BASE_SLOT_COUNT);
        assertEquals(BASE_SLOT_COUNT, controller.getStorageSlotCount());
        assertEquals(BASE_SLOT_COUNT, controller.getMaxStorageSlots());
        assertEquals(controller.getStorageSlotCount(), controller.getSizeInventory());
    }

    @Test
    void stabilizerTiersMapToUpstreamAdditionalCapacity() throws Exception {
        assertEquals(0, slotsFor(ModBlocks.STORAGE_STABILIZER_TIER0));
        assertEquals(256, slotsFor(ModBlocks.STORAGE_STABILIZER_TIER1));
        assertEquals(512, slotsFor(ModBlocks.STORAGE_STABILIZER_TIER2));
        assertEquals(1024, slotsFor(ModBlocks.STORAGE_STABILIZER_TIER3));
        assertEquals(2048, slotsFor(ModBlocks.STORAGE_STABILIZER_TIER4));
        assertEquals(0, slotsFor(ModBlocks.STORAGE_STABILIZER_TIER5));
        assertEquals(0, slotsFor(ModBlocks.STORAGE_STABILIZER_TIER0_DARK));
        assertEquals(256, slotsFor(ModBlocks.STORAGE_STABILIZER_TIER1_DARK));
        assertEquals(512, slotsFor(ModBlocks.STORAGE_STABILIZER_TIER2_DARK));
        assertEquals(1024, slotsFor(ModBlocks.STORAGE_STABILIZER_TIER3_DARK));
        assertEquals(2048, slotsFor(ModBlocks.STORAGE_STABILIZER_TIER4_DARK));
        assertEquals(0, slotsFor(ModBlocks.STORAGE_STABILIZER_TIER5_DARK));
    }

    @Test
    void nbtRoundTripPersistsCapacityAndDoesNotTruncateDeclaredSlots() {
        TileEntityStorageController controller = new TileEntityStorageController();
        NBTTagCompound input = inventoryNbt(256, 200, 3);

        controller.readInventoryFromNBT(input);

        assertEquals(256, controller.getStorageSlotCount());
        assertEquals(3, controller.getStackInSlot(200).getCount());

        NBTTagCompound output = controller.writeInventoryToNBT(new NBTTagCompound());
        assertEquals(256, output.getInteger(STORAGE_SLOT_COUNT));
        assertTrue(output.hasKey("Items", 9));
    }

    @Test
    void legacyNbtDefaultsTo54SlotsButStillExpandsForItsHighestItemSlot() {
        TileEntityStorageController controller = new TileEntityStorageController();

        controller.readInventoryFromNBT(inventoryNbtWithoutCapacity(53, 2));
        assertEquals(LEGACY_SLOT_COUNT, controller.getStorageSlotCount());
        assertEquals(2, controller.getStackInSlot(53).getCount());

        controller.readInventoryFromNBT(inventoryNbtWithoutCapacity(80, 4));
        assertTrue(controller.getStorageSlotCount() >= 81);
        assertEquals(4, controller.getStackInSlot(80).getCount());
    }

    @Test
    void resizingPreservesItemsAcrossShrinkAndExpansion() throws Exception {
        TileEntityStorageController controller = new TileEntityStorageController();
        setStorageSlotCount(controller, 256);
        controller.setInventorySlotContents(200, new ItemStack(Items.DIAMOND, 7));

        setStorageSlotCount(controller, BASE_SLOT_COUNT);
        assertEquals(BASE_SLOT_COUNT, controller.getStorageSlotCount());

        setStorageSlotCount(controller, 256);
        assertEquals(7, controller.getStackInSlot(200).getCount());
    }

    @Test
    void dynamicContainerUsesRuntimeStorageSizeForTransferBounds() throws Exception {
        String source = readSource("src", "main", "java", "com", "occultism", "occultism", "container",
                "ContainerStorageController.java");

        assertTrue(source.contains("storageController.getStorageSlotCount()"),
                "container slot and transfer boundaries must follow the controller's runtime size");
        assertFalse(source.contains("private static final int STORAGE_SIZE = TileEntityStorageController.SLOT_COUNT"),
                "a static 54-slot boundary would truncate expanded storage");
    }

    @Test
    void controllerHandlerSupportsConfiguredSuperStacks() {
        TileEntityStorageController controller = new TileEntityStorageController();
        IItemHandler handler = controller.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

        assertEquals(1024, ModConfig.STORAGE.controllerStackSize);
        assertTrue(ModConfig.STORAGE.overrideItemStackSizes);
        ItemStack remainder = handler.insertItem(0, new ItemStack(Items.DIAMOND, 300), false);

        assertTrue(remainder.isEmpty());
        assertEquals(300, handler.getStackInSlot(0).getCount());
        assertEquals(1024, handler.getSlotLimit(0));
        assertEquals(300, handler.extractItem(0, 300, true).getCount());
    }

    @Test
    void handlerCanRespectNormalItemStackSizesWhenOverrideDisabled() {
        boolean previousOverride = ModConfig.STORAGE.overrideItemStackSizes;
        int previousLimit = ModConfig.STORAGE.controllerStackSize;
        try {
            ModConfig.STORAGE.overrideItemStackSizes = false;
            ModConfig.STORAGE.controllerStackSize = 1024;
            TileEntityStorageController controller = new TileEntityStorageController();
            IItemHandler handler = controller.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

            ItemStack remainder = handler.insertItem(0, new ItemStack(Items.ENDER_PEARL, 64), false);
            assertEquals(16, handler.getStackInSlot(0).getCount());
            assertEquals(48, remainder.getCount());
        } finally {
            ModConfig.STORAGE.overrideItemStackSizes = previousOverride;
            ModConfig.STORAGE.controllerStackSize = previousLimit;
        }
    }

    @Test
    void realSizeNbtPreservesCountsBeyondSignedByteAndReadsLegacyCount() {
        TileEntityStorageController controller = new TileEntityStorageController();
        controller.setInventorySlotContents(0, new ItemStack(Items.DIAMOND, 1024));

        NBTTagCompound encoded = controller.writeInventoryToNBT(new NBTTagCompound());
        NBTTagCompound item = encoded.getTagList("Items", 10).getCompoundTagAt(0);
        assertEquals(1024, item.getInteger(TileEntityStorageController.TAG_REAL_SIZE));

        TileEntityStorageController decoded = new TileEntityStorageController();
        decoded.readInventoryFromNBT(encoded);
        assertEquals(1024, decoded.getStackInSlot(0).getCount());

        NBTTagCompound legacy = new NBTTagCompound();
        NBTTagList items = new NBTTagList();
        NBTTagCompound legacyItem = new ItemStack(Items.DIAMOND, 7).writeToNBT(new NBTTagCompound());
        legacyItem.setByte("Slot", (byte) 0);
        items.appendTag(legacyItem);
        legacy.setTag("Items", items);
        TileEntityStorageController legacyDecoded = new TileEntityStorageController();
        legacyDecoded.readInventoryFromNBT(legacy);
        assertEquals(7, legacyDecoded.getStackInSlot(0).getCount());
    }

    @Test
    void handlerRejectsControllersAndStorageRemotes() {
        TileEntityStorageController controller = new TileEntityStorageController();
        IItemHandler handler = controller.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

        ItemStack controllerStack = new ItemStack(ModBlocks.STORAGE_CONTROLLER);
        ItemStack remoteStack = new ItemStack(ModItems.STORAGE_REMOTE);
        assertFalse(handler.isItemValid(0, controllerStack));
        assertFalse(handler.isItemValid(0, remoteStack));
        assertTrue(handler.insertItem(0, remoteStack, true).getCount() == remoteStack.getCount());
    }

    @Test
    void virtualSnapshotAggregatesFiltersAndSortsLikeUpstream() {
        TileEntityStorageController controller = new TileEntityStorageController();
        controller.setStorageSlotCount(4);
        controller.setInventorySlotContents(0, new ItemStack(Items.DIAMOND, 3));
        controller.setInventorySlotContents(1, new ItemStack(Item.getItemFromBlock(Blocks.STONE), 30));
        controller.setInventorySlotContents(2, new ItemStack(Items.DIAMOND, 7));

        assertEquals(2, controller.getSortedStacks().size());
        assertEquals(30, controller.getSortedStacks().get(0).getCount(),
                "amount=DOWN must expose the most numerous item first");
        assertEquals(10, controller.getSortedStacks().get(1).getCount(),
                "same item stacks must be merged in the virtual list");
        assertEquals(3, controller.getStackInSlot(0).getCount(),
                "building a virtual snapshot must not reorder physical storage slots");
        assertEquals(30, controller.getStackInSlot(1).getCount());
        assertEquals(7, controller.getStackInSlot(2).getCount());
        assertEquals(1, controller.getSortedStacks("diamond", StorageSortType.NAME,
                StorageSortDirection.UP).size());
        assertEquals(30, controller.getSortedStacks("minecraft:stone", StorageSortType.NAME,
                StorageSortDirection.UP).get(0).getCount());
    }

    @Test
    void messageSnapshotIsCachedAndInvalidatedByContentsAndCapacity() {
        TileEntityStorageController controller = new TileEntityStorageController();
        MessageUpdateStacks initial = controller.getMessageUpdateStacks();
        assertSame(initial, controller.getMessageUpdateStacks(),
                "unchanged controllers should reuse the compressed snapshot");
        assertTrue(initial.getStacks().isEmpty());

        controller.setInventorySlotContents(0, new ItemStack(Items.DIAMOND, 3));
        MessageUpdateStacks afterContents = controller.getMessageUpdateStacks();
        assertNotSame(initial, afterContents, "content changes must invalidate the message cache");
        assertEquals(3, afterContents.getStacks().get(0).getCount());

        controller.setStorageSlotCount(controller.getStorageSlotCount() + 1);
        MessageUpdateStacks afterCapacity = controller.getMessageUpdateStacks();
        assertNotSame(afterContents, afterCapacity, "capacity changes must invalidate the message cache");
        assertEquals(controller.getStorageSlotCount(), afterCapacity.getMaxSlots());

        // Message accessors are defensive: mutating a returned list cannot
        // poison the controller-owned payload reused by later sends.
        afterCapacity.getStacks().clear();
        assertEquals(3, controller.getMessageUpdateStacks().getStacks().get(0).getCount());
    }

    @Test
    void virtualEntriesRetainPhysicalMappingsAndExtractAcrossSlots() {
        TileEntityStorageController controller = new TileEntityStorageController();
        controller.setStorageSlotCount(4);
        controller.setInventorySlotContents(0, new ItemStack(Items.DIAMOND, 3));
        controller.setInventorySlotContents(1, new ItemStack(Item.getItemFromBlock(Blocks.STONE), 30));
        controller.setInventorySlotContents(2, new ItemStack(Items.DIAMOND, 7));
        controller.setSortType(StorageSortType.NAME);
        controller.setSortDirection(StorageSortDirection.UP);

        List<StorageVirtualEntry> entries = controller.getVirtualStorageEntries();
        assertEquals(2, entries.size());
        StorageVirtualEntry diamond = entries.get(0);
        assertEquals(10, diamond.getCount());
        assertEquals(Arrays.asList(Integer.valueOf(0), Integer.valueOf(2)), diamond.getPhysicalSlots());

        ItemStack simulated = controller.extractVirtualStack(0, 5, true);
        assertEquals(5, simulated.getCount());
        assertEquals(3, controller.getStackInSlot(0).getCount(), "simulation must not mutate physical slots");

        ItemStack extracted = controller.extractVirtualStack(0, 5, false);
        assertEquals(5, extracted.getCount());
        assertTrue(controller.getStackInSlot(0).isEmpty());
        assertEquals(5, controller.getStackInSlot(2).getCount());
    }

    @Test
    void virtualEntriesExcludeBlacklistedStacksAndContainerLeavesEmptyCellsVisible() {
        TileEntityStorageController controller = new TileEntityStorageController();
        controller.setStorageSlotCount(4);
        controller.setInventorySlotContents(0, new ItemStack(ModBlocks.STORAGE_CONTROLLER));
        controller.setInventorySlotContents(1, new ItemStack(ModItems.STORAGE_REMOTE));
        assertTrue(controller.getVirtualStorageEntries().isEmpty());
        assertEquals(1, controller.insertIntoStorage(new ItemStack(ModItems.STORAGE_REMOTE), true).getCount());

        ContainerStorageController container = new ContainerStorageController(new InventoryPlayer(null), controller);
        assertTrue(container.isStorageSlotVisible(0));
        assertTrue(container.getSlot(0).getStack().isEmpty(), "empty virtual cells remain available to the GUI");
        assertTrue(container.isStorageSlotVisible(1));
    }

    @Test
    void sortSettingsAndQueryRoundTripThroughControllerNbt() {
        TileEntityStorageController controller = new TileEntityStorageController();
        controller.setSortType(StorageSortType.MOD);
        controller.setSortDirection(StorageSortDirection.UP);
        controller.setSearchQuery("  Diamond  ");

        NBTTagCompound encoded = controller.writeInventoryToNBT(new NBTTagCompound());
        assertEquals(StorageSortType.MOD.getValue(), encoded.getInteger(TileEntityStorageController.TAG_SORT_TYPE));
        assertEquals(StorageSortDirection.UP.getValue(),
                encoded.getInteger(TileEntityStorageController.TAG_SORT_DIRECTION));
        assertEquals("diamond", encoded.getString(TileEntityStorageController.TAG_SEARCH_QUERY));

        TileEntityStorageController decoded = new TileEntityStorageController();
        decoded.readInventoryFromNBT(encoded);
        assertEquals(StorageSortType.MOD, decoded.getSortType());
        assertEquals(StorageSortDirection.UP, decoded.getSortDirection());
        assertEquals("diamond", decoded.getSearchQuery());
    }

    @Test
    void craftingMatrixAndOrderStackRoundTripThroughControllerNbt() {
        TileEntityStorageController controller = new TileEntityStorageController();
        List<ItemStack> matrix = Arrays.asList(
                new ItemStack(Items.DIAMOND, 300), ItemStack.EMPTY, new ItemStack(Items.EMERALD, 2),
                ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY,
                ItemStack.EMPTY);
        controller.setCraftingMatrix(matrix);
        controller.setOrderStack(new ItemStack(Items.ENDER_PEARL, 7));

        NBTTagCompound encoded = controller.writeInventoryToNBT(new NBTTagCompound());
        assertTrue(encoded.hasKey(TileEntityStorageController.TAG_CRAFTING_MATRIX, 9));
        assertTrue(encoded.hasKey(TileEntityStorageController.TAG_ORDER_STACK, 10));
        assertEquals(300, encoded.getTagList(TileEntityStorageController.TAG_CRAFTING_MATRIX, 10)
                .getCompoundTagAt(0).getInteger(TileEntityStorageController.TAG_REAL_SIZE));

        TileEntityStorageController decoded = new TileEntityStorageController();
        decoded.readInventoryFromNBT(encoded);
        assertEquals(300, decoded.getCraftingMatrixSlot(0).getCount());
        assertEquals(Items.EMERALD, decoded.getCraftingMatrixSlot(2).getItem());
        assertEquals(7, decoded.getOrderStack().getCount());
        assertEquals(Items.ENDER_PEARL, decoded.getOrderStack().getItem());
    }

    @Test
    void malformedCraftingMatrixEntriesAreBoundedAndDoNotDefaultToSlotZero() {
        NBTTagCompound encoded = new NBTTagCompound();
        NBTTagList matrix = new NBTTagList();
        NBTTagCompound missingSlot = new ItemStack(Items.DIAMOND, 1).writeToNBT(new NBTTagCompound());
        matrix.appendTag(missingSlot);
        for (int i = 0; i < 12; i++) {
            NBTTagCompound invalid = new ItemStack(Items.EMERALD, 1).writeToNBT(new NBTTagCompound());
            invalid.setByte(TileEntityStorageController.TAG_MATRIX_SLOT, (byte) 20);
            matrix.appendTag(invalid);
        }
        encoded.setTag(TileEntityStorageController.TAG_CRAFTING_MATRIX, matrix);

        TileEntityStorageController decoded = new TileEntityStorageController();
        decoded.readInventoryFromNBT(encoded);
        assertTrue(decoded.getCraftingMatrixSlot(0).isEmpty());
        for (int slot = 1; slot < TileEntityStorageController.CRAFTING_MATRIX_SIZE; slot++) {
            assertTrue(decoded.getCraftingMatrixSlot(slot).isEmpty());
        }
    }

    @Test
    void storageContainerRestoresTerminalOwnedMatrixWithoutAddingItToPhysicalSlots() {
        TileEntityStorageController controller = new TileEntityStorageController();
        controller.setCraftingMatrix(Arrays.asList(new ItemStack(Items.DIAMOND, 1), ItemStack.EMPTY,
                ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY,
                ItemStack.EMPTY));

        ContainerStorageController container = new ContainerStorageController(new InventoryPlayer(null), controller);
        assertEquals(Items.DIAMOND, container.getCraftMatrix().getStackInSlot(0).getItem());
        assertEquals(1, container.getCraftMatrix().getStackInSlot(0).getCount());
        assertTrue(controller.getStackInSlot(0).isEmpty(),
                "crafting matrix must remain outside the physical storage handler");
    }

    @Test
    void portableControllerKeepsMatrixEvenWhenPhysicalStorageIsEmpty() {
        TileEntityStorageController controller = new TileEntityStorageController();
        controller.setCraftingMatrix(Arrays.asList(new ItemStack(Items.DIAMOND, 1), ItemStack.EMPTY,
                ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY,
                ItemStack.EMPTY));

        // Bootstrap does not fire Forge's ItemBlock registry event; use a
        // guaranteed non-empty carrier so this test isolates NBT persistence.
        ItemStack portable = new ItemStack(Items.STICK);
        TileEntityStorageController.writeInventoryToStack(portable, controller);
        assertTrue(portable.hasTagCompound());
        TileEntityStorageController restored = new TileEntityStorageController();
        restored.readInventoryFromStack(portable);
        assertEquals(Items.DIAMOND, restored.getCraftingMatrixSlot(0).getItem());
    }

    @Test
    void physicalSortDoesNotTouchHiddenSlots() {
        TileEntityStorageController controller = new TileEntityStorageController();
        controller.setStorageSlotCount(256);
        controller.setInventorySlotContents(200, new ItemStack(Items.EMERALD, 11));
        controller.setInventorySlotContents(0, new ItemStack(Items.DIAMOND, 2));
        controller.setInventorySlotContents(1, new ItemStack(Item.getItemFromBlock(Blocks.STONE), 20));
        controller.setStorageSlotCount(4);
        controller.sortStorageSlots();

        controller.setStorageSlotCount(256);
        assertEquals(11, controller.getStackInSlot(200).getCount(),
                "sorting a visible terminal page must preserve hidden expanded-storage slots");
        assertEquals(20, controller.getStackInSlot(0).getCount());
        assertEquals(2, controller.getStackInSlot(1).getCount());

        controller.setStorageSlotCount(4);
        controller.setSortDirection(StorageSortDirection.UP);
        controller.sortStorageSlots();
        assertEquals(2, controller.getStackInSlot(0).getCount(),
                "UP must reverse the configured amount ordering");
        assertEquals(20, controller.getStackInSlot(1).getCount());
    }

    private static int slotsFor(Object block) throws Exception {
        Method method = TileEntityStorageController.class.getDeclaredMethod("getSlotsForStabilizerBlock",
                net.minecraft.block.Block.class);
        method.setAccessible(true);
        try {
            return ((Integer) method.invoke(null, block)).intValue();
        } catch (InvocationTargetException e) {
            throw new AssertionError("stabilizer capacity lookup failed", e.getCause());
        }
    }

    private static void setStorageSlotCount(TileEntityStorageController controller, int target) throws Exception {
        List<String> candidates = Arrays.asList("setStorageSlotCount", "setMaxStorageSlots", "resizeStorage",
                "resizeStacks", "setSlotCount");
        for (String candidate : candidates) {
            try {
                Method method = TileEntityStorageController.class.getDeclaredMethod(candidate, int.class);
                method.setAccessible(true);
                method.invoke(controller, Integer.valueOf(target));
                return;
            } catch (NoSuchMethodException ignored) {
                // Try the next compatible name; this keeps the test useful while the API is finalized.
            }
        }
        throw new AssertionError("StorageController must expose an int capacity-resize helper");
    }

    private static NBTTagCompound inventoryNbt(int slotCount, int slot, int count) {
        NBTTagCompound nbt = inventoryNbtWithoutCapacity(slot, count);
        nbt.setInteger(STORAGE_SLOT_COUNT, slotCount);
        return nbt;
    }

    private static NBTTagCompound inventoryNbtWithoutCapacity(int slot, int count) {
        NBTTagCompound nbt = new NBTTagCompound();
        NBTTagList items = new NBTTagList();
        NBTTagCompound stack = new ItemStack(Items.DIAMOND, count).writeToNBT(new NBTTagCompound());
        stack.setByte("Slot", (byte) slot);
        items.appendTag(stack);
        nbt.setTag("Items", items);
        return nbt;
    }

    private static String readSource(String... path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path[0], Arrays.copyOfRange(path, 1, path.length))),
                StandardCharsets.UTF_8);
    }
}
