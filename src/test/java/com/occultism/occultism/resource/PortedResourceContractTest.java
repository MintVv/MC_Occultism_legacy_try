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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

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
    private static final Set<String> SLAB_ITEMS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "occultism:otherplanks_slab",
            "occultism:otherstone_slab",
            "occultism:otherrock_slab",
            "occultism:polished_otherstone_slab",
            "occultism:polished_otherrock_slab",
            "occultism:otherstone_bricks_slab",
            "occultism:otherrock_bricks_slab")));

    @Test
    void everySlabItemReferenceDeclaresItsMetadata() throws IOException {
        Path recipeRoot = ASSET_ROOT.resolve("recipes");
        try (Stream<Path> files = Files.list(recipeRoot)) {
            files.filter(path -> path.getFileName().toString().endsWith(".json"))
                    .forEach(path -> assertSlabReferencesHaveData(readJsonUnchecked(path), path));
        }
    }

    @Test
    void otherplanksFenceGateCoversEveryBlockState() throws IOException {
        JsonObject root = readJson(ASSET_ROOT.resolve("blockstates").resolve("otherplanks_fence_gate.json"));
        JsonObject variants = root.getAsJsonObject("variants");
        assertEquals(32, variants.size(), "Fence gate must cover facing/in_wall/open/powered combinations");

        Set<String> actual = new HashSet<>();
        for (Map.Entry<String, JsonElement> entry : variants.entrySet()) {
            actual.add(normalizeVariant(entry.getKey()));
        }
        for (String facing : Arrays.asList("north", "south", "east", "west")) {
            for (boolean inWall : Arrays.asList(false, true)) {
                for (boolean open : Arrays.asList(false, true)) {
                    for (boolean powered : Arrays.asList(false, true)) {
                        String expected = "facing=" + facing + ",in_wall=" + inWall + ",open=" + open
                                + ",powered=" + powered;
                        assertTrue(actual.contains(normalizeVariant(expected)), "Missing fence gate variant " + expected);
                    }
                }
            }
        }
    }

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
    void doorWoodUsesTheRegisteredItemDoor() throws IOException {
        String blocks = readUtf8(REGISTRY_ROOT.resolve("ModBlocks.java"));
        String oreDictionary = readUtf8(REGISTRY_ROOT.resolve("ModOreDictionary.java"));

        assertTrue(blocks.contains("public static final ItemDoor OTHERPLANKS_DOOR_ITEM"),
                "The otherplanks door item must retain its ItemDoor type");
        assertTrue(blocks.contains("event.getRegistry().register(OTHERPLANKS_DOOR_ITEM);"),
                "The retained ItemDoor instance must be the registered item");
        assertTrue(oreDictionary.contains(
                "OreDictionary.registerOre(\"doorWood\", ModBlocks.OTHERPLANKS_DOOR_ITEM);"),
                "doorWood must register the actual ItemDoor rather than the BlockDoor");
        assertFalse(oreDictionary.contains(
                "OreDictionary.registerOre(\"doorWood\", ModBlocks.OTHERPLANKS_DOOR);"),
                "Registering a BlockDoor produces a missing-item stack in 1.12");
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

    private static void assertSlabReferencesHaveData(JsonElement element, Path recipe) {
        if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) {
                assertSlabReferencesHaveData(child, recipe);
            }
            return;
        }
        if (!element.isJsonObject()) {
            return;
        }

        JsonObject object = element.getAsJsonObject();
        if (object.has("item") && object.get("item").isJsonPrimitive()
                && SLAB_ITEMS.contains(object.get("item").getAsString())) {
            assertTrue(object.has("data"), "Slab item reference is missing data in " + recipe);
        }
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            assertSlabReferencesHaveData(entry.getValue(), recipe);
        }
    }

    private static String normalizeVariant(String variant) {
        List<String> properties = new ArrayList<>(Arrays.asList(variant.split(",")));
        Collections.sort(properties);
        return String.join(",", properties);
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
