package com.occultism.occultism.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

class PortedResourceContractTest {

    private static final Path ASSET_ROOT = Paths.get("src", "main", "resources", "assets", "occultism");
    private static final Path REGISTRY_ROOT = Paths.get("src", "main", "java", "com", "occultism", "occultism",
            "registry");
    @Test
    void dictionaryDetailsUseResolvableLocalSyntax() throws IOException {
        JsonObject root = readJson(ASSET_ROOT.resolve("dictionary").resolve("modonomicon_index.json"));
        List<String> strings = new ArrayList<>();
        collectStrings(root, strings);
        for (String value : strings) {
            assertFalse(value.startsWith("Item: {") || value.startsWith("Item: OrderedDict("),
                    "Dictionary item detail contains a serialized object: " + value);
            assertFalse("Recipe: occultism:crafting/bound_book_of_binding".equals(value),
                    "Dictionary references the missing generic bound-book recipe");
            assertFalse("Multiblock: occultism:storage_stabilizer_demo".equals(value),
                    "Dictionary references a multiblock with no local definition");
        }
    }

    @Test
    void upstreamBuildingBlocksArePorted() throws IOException {
        // These blocks were removed once as "decorative". Upstream 1.21.1 registers the whole
        // family (OccultismBlocks.java), and removing it stranded 69 registry ids in existing
        // saves, so the decision is now to port them. This pins that.
        String blocks = readUtf8(REGISTRY_ROOT.resolve("ModBlocks.java"));
        String[] buildingIds = {
                "otherstone_stairs", "otherrock_stairs", "polished_otherstone_stairs",
                "polished_otherrock_stairs", "otherstone_bricks_stairs", "otherrock_bricks_stairs",
                "otherstone_slab", "otherrock_slab", "polished_otherstone_slab", "polished_otherrock_slab",
                "otherstone_bricks_slab", "otherrock_bricks_slab", "otherstone_wall", "otherrock_wall",
                "polished_otherstone_wall", "polished_otherrock_wall", "otherstone_bricks_wall",
                "otherrock_bricks_wall", "otherstone_button", "otherrock_button",
                "otherstone_pressure_plate", "otherrock_pressure_plate", "otherplanks_slab",
                "otherplanks_stairs", "otherplanks_button", "otherplanks_pressure_plate",
                "otherplanks_fence", "otherplanks_fence_gate", "otherplanks_trapdoor",
                "otherplanks_door", "chiseled_otherstone_bricks", "chiseled_otherrock_bricks",
                "cracked_otherstone_bricks", "cracked_otherrock_bricks"
        };
        for (String id : buildingIds) {
            assertTrue(blocks.contains("\"" + id + "\""), "Building block missing from the registry: " + id);
            assertTrue(Files.exists(ASSET_ROOT.resolve("blockstates").resolve(id + ".json")),
                    "Missing blockstate for restored block: " + id);
            boolean smelted = id.endsWith("cracked_otherstone_bricks") || id.endsWith("cracked_otherrock_bricks");
            if (!smelted) {
                assertTrue(Files.exists(ASSET_ROOT.resolve("recipes").resolve(id + ".json")),
                        "Missing recipe for restored block: " + id);
            }
        }
    }

    @Test
    void repairIconHasMatchingFourFrameAnimationMetadata() throws IOException {
        Path texture = ASSET_ROOT.resolve("textures").resolve("item").resolve("repair_icon.png");
        BufferedImage image = ImageIO.read(texture.toFile());
        assertNotNull(image, "repair_icon.png must be a readable PNG");
        assertEquals(16, image.getWidth(), "repair_icon.png must retain its upstream frame width");
        assertEquals(image.getWidth() * 4, image.getHeight(), "repair_icon.png must contain four square frames");

        JsonObject metadata = readJson(texture.resolveSibling("repair_icon.png.mcmeta"));
        JsonObject animation = metadata.getAsJsonObject("animation");
        assertNotNull(animation, "repair_icon.png.mcmeta must define an animation section");
        assertFalse(animation.get("interpolate").getAsBoolean(), "The upstream repair animation is not interpolated");
        JsonArray frames = animation.getAsJsonArray("frames");
        assertEquals(4, frames.size(), "repair_icon.png.mcmeta must describe every PNG frame");
        for (int index = 0; index < frames.size(); index++) {
            JsonObject frame = frames.get(index).getAsJsonObject();
            assertEquals(index, frame.get("index").getAsInt(), "Unexpected repair icon frame order");
            assertEquals(5, frame.get("time").getAsInt(), "Every repair icon frame must last five ticks");
        }
    }

    @Test
    void storageRemoteResolvesLinkedControllerAcrossLoadedDimensions() throws IOException {
        String remote = readUtf8(Paths.get("src", "main", "java", "com", "occultism", "occultism", "item",
                "ItemStorageRemote.java"));
        String wormhole = readUtf8(Paths.get("src", "main", "java", "com", "occultism", "occultism", "block",
                "BlockStableWormhole.java"));
        String gui = readUtf8(Paths.get("src", "main", "java", "com", "occultism", "occultism", "gui",
                "ModGuiHandler.java"));

        assertTrue(remote.contains("DimensionManager.getWorld(dimension)"));
        assertTrue(remote.contains("playerIn.openGui(Occultism.INSTANCE, ModGuiHandler.STORAGE_REMOTE, worldIn"));
        assertTrue(remote.contains("findInventorySlot(playerIn, stack, handIn)"));
        assertFalse(remote.contains("wrong_dimension"));
        assertTrue(wormhole.contains("DimensionManager.getWorld(wormhole.getLinkedDimension())"));
        assertTrue(wormhole.contains("ModGuiHandler.STORAGE_WORMHOLE"));
        assertTrue(gui.contains("new ContainerStorageController(player.inventory,\n                            (TileEntityStorageController) resolved, x)"));
        assertTrue(gui.contains("new ContainerStorageController(player.inventory,\n                            (TileEntityStorageController) resolved, (TileEntityStableWormhole) tile)"));
    }

    @Test
    void remoteStorageContainerDoesNotUsePlayerDistanceForValidity() throws IOException {
        String container = readUtf8(Paths.get("src", "main", "java", "com", "occultism", "occultism", "container",
                "ContainerStorageController.java"));
        assertTrue(container.contains("if (this.remoteAccess)"));
        assertTrue(container.contains("return this.isRemoteAccessValid(playerIn);"));
        assertTrue(container.contains("return this.storageController.isUsableByPlayer(playerIn);"));
        assertTrue(container.contains("ItemStorageRemote.isLinkedToController"));
    }

    @Test
    void stableWormholeProxiesItemHandlerToLoadedTargetController() throws IOException {
        String wormhole = readUtf8(Paths.get("src", "main", "java", "com", "occultism", "occultism", "tileentity",
                "TileEntityStableWormhole.java"));
        assertTrue(wormhole.contains("DimensionManager.getWorld(this.linkedDimension)"));
        assertTrue(wormhole.contains("CapabilityItemHandler.ITEM_HANDLER_CAPABILITY"));
        assertTrue(wormhole.contains("controller.getCapability(capability, facing)"));
        assertTrue(wormhole.contains("if (tile instanceof TileEntityStorageController)"));
        assertTrue(wormhole.contains("this.clearLinkedController()"));
    }

    private static void collectStrings(JsonElement element, List<String> strings) {
        if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) {
                collectStrings(child, strings);
            }
            return;
        }
        if (element.isJsonObject()) {
            for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
                collectStrings(entry.getValue(), strings);
            }
            return;
        }
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            strings.add(element.getAsString());
        }
    }

    private static JsonObject readJsonUnchecked(Path path) {
        try {
            return readJson(path);
        } catch (IOException e) {
            throw new AssertionError("Failed to read " + path, e);
        }
    }

    private static JsonObject readJson(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return new JsonParser().parse(reader).getAsJsonObject();
        }
    }

    private static String readUtf8(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
