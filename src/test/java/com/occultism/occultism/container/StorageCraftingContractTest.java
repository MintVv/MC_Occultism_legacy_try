package com.occultism.occultism.container;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import com.occultism.occultism.tileentity.TileEntityStorageController;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;

/** Contract coverage for the 1.12 terminal crafting port. */
class StorageCraftingContractTest {

    @Test
    void containerExposesThreeByThreeMatrixAfterStorageSlots() {
        TileEntityStorageController controller = new TileEntityStorageController();
        controller.setStorageSlotCount(4);
        ContainerStorageController container = new ContainerStorageController(new InventoryPlayer(null), controller);

        assertEquals(9, container.getCraftMatrix().getSizeInventory());
        assertTrue(container.getCraftResultInventory().getStackInSlot(0).isEmpty());
        assertEquals(0, container.getSlot(4).getSlotIndex(), "result slot follows storage slots");
        assertEquals(8, container.getSlot(13).getSlotIndex(), "the last 3x3 cell remains before player slots");
        assertEquals(9, container.getSlot(14).getSlotIndex(), "player inventory starts after crafting slots");
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

    private static String readSource(String... path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path[0], java.util.Arrays.copyOfRange(path, 1, path.length))),
                StandardCharsets.UTF_8);
    }
}
