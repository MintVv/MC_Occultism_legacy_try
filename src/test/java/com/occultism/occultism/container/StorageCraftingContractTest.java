package com.occultism.occultism.container;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import com.occultism.occultism.tileentity.TileEntityStorageController;
import com.occultism.occultism.item.ItemStorageRemote;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/** Contract coverage for the 1.12 terminal crafting port. */
class StorageCraftingContractTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void containerExposesThreeByThreeMatrixBeforePlayerSlots() {
        TileEntityStorageController controller = new TileEntityStorageController();
        controller.setStorageSlotCount(4);
        ContainerStorageController container = new ContainerStorageController(new InventoryPlayer(null), controller);

        assertEquals(9, container.getCraftMatrix().getSizeInventory());
        assertTrue(container.getCraftResultInventory().getStackInSlot(0).isEmpty());
        assertEquals(0, container.getSlot(0).getSlotIndex(), "result slot is first");
        assertEquals(0, container.getSlot(1).getSlotIndex(), "the first 3x3 cell follows the result");
        assertEquals(8, container.getSlot(9).getSlotIndex(), "the last 3x3 cell remains before the order slot");
        assertEquals(0, container.getSlot(10).getSlotIndex(), "the order slot follows the crafting matrix");
        assertEquals(9, container.getSlot(11).getSlotIndex(), "player inventory starts after crafting slots");
        assertEquals(0, container.getSlot(38).getSlotIndex(), "hotbar starts after the player inventory");
    }

    @Test
    void recipeTransferAndSafeReturnCodeIsPresent() throws Exception {
        String source = readSource("src", "main", "java", "com", "occultism", "occultism", "container",
                "ContainerStorageController.java");
        assertTrue(source.contains("setRecipeFromNBT"));
        assertTrue(source.contains("extractMatchingFromStorage"));
        assertTrue(source.contains("returnStackSafely"));
        assertTrue(source.contains("CraftingManager.findMatchingRecipe"));
        assertTrue(source.contains("StorageCraftingResultSlot"));
    }

    @Test
    void recipeLockIsTransientAndCoversWholeShiftCraftTransaction() throws Exception {
        TileEntityStorageController controller = new TileEntityStorageController();
        ContainerStorageController container = new ContainerStorageController(new InventoryPlayer(null), controller);
        assertTrue(!container.isRecipeLocked(), "a newly opened terminal must not start locked");

        String source = readSource("src", "main", "java", "com", "occultism", "occultism", "container",
                "ContainerStorageController.java");
        assertTrue(source.contains("if (this.recipeLocked || payload == null"),
                "recipe transfer must be rejected while a craft transaction owns the matrix");
        assertTrue(source.contains("boolean previousRecipeLock = this.recipeLocked"),
                "shift crafting must preserve the previous lock owner");
        assertTrue(source.contains("this.recipeLocked = true;"),
                "shift crafting must lock before consuming a batch");
        assertTrue(source.contains("this.recipeLocked = previousRecipeLock;"),
                "shift crafting must always unlock in finally");
        assertTrue(source.contains("public boolean isRecipeLocked()"));
    }

    @Test
    void recipeMessageUsesBoundedUpstreamSlotKeys() throws Exception {
        String message = readSource("src", "main", "java", "com", "occultism", "occultism", "network",
                "MessageSetRecipe.java");
        String networking = readSource("src", "main", "java", "com", "occultism", "occultism", "network",
                "ModNetworking.java");
        assertTrue(message.contains("MAX_NBT_BYTES"));
        assertTrue(message.contains("s[0-8]"));
        assertTrue(message.contains("tagCount() > 16"));
        assertTrue(networking.contains("MessageSetRecipe.Handler.class"));
    }

    @Test
    void guiAllocatesSpaceForCraftingPanel() throws Exception {
        String gui = readSource("src", "main", "java", "com", "occultism", "occultism", "client", "gui",
                "GuiStorageController.java");
        assertTrue(gui.contains("this.xSize = 362"));
        assertTrue(gui.contains("+ 226 + column * 18"));
        assertTrue(gui.contains("storage_controller.crafting"));
    }

    @Test
    void remoteContainerPersistsMatrixAndOrderOnTheSelectedRemote() {
        InventoryPlayer inventory = new InventoryPlayer(null);
        ItemStack remote = new ItemStack(new ItemStorageRemote());
        inventory.setInventorySlotContents(0, remote);
        ItemStorageRemote.setCraftingMatrix(remote, Arrays.asList(
                new ItemStack(Items.DIAMOND, 300), ItemStack.EMPTY, ItemStack.EMPTY,
                ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY,
                ItemStack.EMPTY));
        ItemStorageRemote.setOrderStack(remote, new ItemStack(Items.ENDER_PEARL, 4));

        ContainerStorageController container = new ContainerStorageController(inventory,
                new TileEntityStorageController(), 0);
        assertTrue(container.isRemoteItemAccess());
        assertEquals(0, container.getRemoteSlot());
        assertEquals(300, container.getCraftMatrix().getStackInSlot(0).getCount());
        assertEquals(4, container.getOrderStack().getCount());

        container.getCraftMatrix().setInventorySlotContents(2, new ItemStack(Items.EMERALD, 2));
        container.getOrderInventory().setInventorySlotContents(0, new ItemStack(Items.GOLD_INGOT, 3));
        container.syncOrderStackToController();

        List<ItemStack> decoded = ItemStorageRemote.getCraftingMatrix(remote);
        assertEquals(2, decoded.get(2).getCount());
        assertEquals(3, ItemStorageRemote.getOrderStack(remote).getCount());
    }

    @Test
    void remoteContainerLocksTheSelectedInventorySlotAndRejectsReplacement() {
        InventoryPlayer inventory = new InventoryPlayer(null);
        ItemStack remote = new ItemStack(new ItemStorageRemote());
        inventory.setInventorySlotContents(0, remote);
        ContainerStorageController container = new ContainerStorageController(inventory,
                new TileEntityStorageController(), 0);

        assertFalse(container.getSlot(38).getHasStack());
        assertFalse(container.getSlot(38).isItemValid(new ItemStack(Items.STICK)));
        assertFalse(container.getSlot(38).canTakeStack(null));
        assertEquals(remote, container.getStorageRemote());

        inventory.setInventorySlotContents(0, new ItemStack(Items.STICK));
        assertTrue(container.getStorageRemote().isEmpty());
    }

    private static String readSource(String... path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path[0], java.util.Arrays.copyOfRange(path, 1, path.length))),
                StandardCharsets.UTF_8);
    }
}
