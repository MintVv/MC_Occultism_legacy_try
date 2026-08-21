package com.occultism.occultism.resource;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

/** Contract coverage for the 1.12 storage terminal's client-facing controls. */
class StorageControllerGuiPortContractTest {

    @Test
    void guiExposesSearchSortDirectionAndClearControls() throws Exception {
        String source = readSource("src", "main", "java", "com", "occultism", "occultism", "client", "gui",
                "GuiStorageController.java");
        String container = readSource("src", "main", "java", "com", "occultism", "occultism", "container",
                "ContainerStorageController.java");
        assertTrue(source.contains("GuiTextField"));
        assertTrue(source.contains("BUTTON_SORT_TYPE"));
        assertTrue(source.contains("BUTTON_SORT_DIRECTION"));
        assertTrue(source.contains("BUTTON_CLEAR_SEARCH"));
        assertTrue(source.contains("ModNetworking.sendStorageSettings"));
        assertTrue(source.contains("refreshStorageView"));
        assertTrue(container.contains("getVirtualStorageEntries"));
    }

    @Test
    void guiUsesBothLanguageSetsForEveryStorageControlLabel() throws Exception {
        String english = readSource("src", "main", "resources", "assets", "occultism", "lang", "en_us.lang");
        String chinese = readSource("src", "main", "resources", "assets", "occultism", "lang", "zh_cn.lang");
        String[] keys = {
                "gui.occultism.storage_controller.search=",
                "gui.occultism.storage_controller.sort.amount=",
                "gui.occultism.storage_controller.sort.name=",
                "gui.occultism.storage_controller.sort.mod=",
                "gui.occultism.storage_controller.direction.down=",
                "gui.occultism.storage_controller.direction.up="
        };
        for (String key : keys) {
            assertTrue(english.contains(key), "missing English key: " + key);
            assertTrue(chinese.contains(key), "missing Chinese key: " + key);
        }
    }

    @Test
    void serverFilteringKeepsEmptySlotsAvailableForInsertion() throws Exception {
        String controller = readSource("src", "main", "java", "com", "occultism", "occultism", "tileentity",
                "TileEntityStorageController.java");
        String container = readSource("src", "main", "java", "com", "occultism", "occultism", "container",
                "ContainerStorageController.java");
        assertTrue(controller.contains("stack == null || stack.isEmpty() || matchesSearch"));
        assertTrue(container.contains("isStorageSlotVisible"));
        assertTrue(container.contains("refreshStorageView"));
        assertTrue(container.contains("VirtualStorageSlot"));
        assertTrue(container.contains("extractVirtualStack"));
        assertTrue(container.contains("insertIntoStorage"));
    }

    private static String readSource(String... path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path[0], java.util.Arrays.copyOfRange(path, 1, path.length))),
                StandardCharsets.UTF_8);
    }
}
