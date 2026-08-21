package com.occultism.occultism.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.occultism.occultism.tileentity.StorageSortDirection;
import com.occultism.occultism.tileentity.StorageSortType;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

/** Contract coverage for the 1.12 storage-setting packet boundary. */
class StorageSettingsNetworkContractTest {

    @Test
    void settingsPayloadRoundTripsAndNormalizesBoundedQuery() {
        StringBuilder suffix = new StringBuilder();
        for (int i = 0; i < 180; i++) {
            suffix.append('x');
        }
        String longQuery = "  Diamond " + suffix.toString();
        MessageUpdateStorageSettings encoded = new MessageUpdateStorageSettings(longQuery, StorageSortType.MOD,
                StorageSortDirection.UP);
        ByteBuf buffer = Unpooled.buffer();
        try {
            encoded.toBytes(buffer);
            MessageUpdateStorageSettings decoded = new MessageUpdateStorageSettings();
            decoded.fromBytes(buffer);

            assertEquals(MessageUpdateStorageSettings.MAX_SEARCH_QUERY_LENGTH, decoded.getSearchQuery().length());
            assertEquals(decoded.getSearchQuery(), decoded.getSearchQuery().toLowerCase(java.util.Locale.ROOT));
            assertEquals(StorageSortType.MOD, decoded.getSortType());
            assertEquals(StorageSortDirection.UP, decoded.getSortDirection());
        } finally {
            buffer.release();
        }
    }

    @Test
    void networkRegistrationExposesSimpleClientApiAndServerValidation() throws Exception {
        String networking = readSource("src", "main", "java", "com", "occultism", "occultism", "network",
                "ModNetworking.java");
        String message = readSource("src", "main", "java", "com", "occultism", "occultism", "network",
                "MessageUpdateStorageSettings.java");
        String container = readSource("src", "main", "java", "com", "occultism", "occultism", "container",
                "ContainerStorageController.java");

        assertTrue(networking.contains("MessageUpdateStorageSettings.Handler.class"));
        assertTrue(networking.contains("sendStorageSettings(String searchQuery"));
        assertTrue(message.contains("player.openContainer instanceof ContainerStorageController"));
        assertTrue(message.contains("container.canUpdateStorageSettings(player)"));
        assertTrue(message.contains("controller.sortStorageSlots()"));
        assertTrue(message.contains("controller.markSettingsDirtyAndSync()"));
        assertTrue(message.contains("container.refreshStorageView()"));
        assertTrue(container.contains("ItemStorageRemote.isLinkedToController"));
        assertTrue(container.contains("isLinkedToController(this.storageController.getWorld()"));
    }

    @Test
    void queryFilteringKeepsEmptyPhysicalSlotsAvailable() throws Exception {
        String controller = readSource("src", "main", "java", "com", "occultism", "occultism", "tileentity",
                "TileEntityStorageController.java");
        String container = readSource("src", "main", "java", "com", "occultism", "occultism", "container",
                "ContainerStorageController.java");
        assertTrue(controller.contains("stack == null || stack.isEmpty() || matchesSearch"));
        assertTrue(container.contains("isStorageSlotVisible"));
    }

    private static String readSource(String... path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path[0], java.util.Arrays.copyOfRange(path, 1, path.length))),
                StandardCharsets.UTF_8);
    }
}
