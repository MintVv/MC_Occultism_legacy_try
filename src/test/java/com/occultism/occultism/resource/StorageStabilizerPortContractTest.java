package com.occultism.occultism.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.occultism.occultism.block.BlockStorageStabilizer;
import com.occultism.occultism.registry.ModBlocks;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Bootstrap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

/** Contract coverage for the directional 1.12 storage stabilizer port. */
class StorageStabilizerPortContractTest {

    private static final Path ASSET_ROOT = Paths.get("src", "main", "resources", "assets", "occultism");
    private static final Path JAVA_ROOT = Paths.get("src", "main", "java", "com", "occultism", "occultism");
    private static final List<String> STABILIZER_NAMES = Arrays.asList(
            "storage_stabilizer_tier0", "storage_stabilizer_tier1", "storage_stabilizer_tier2",
            "storage_stabilizer_tier3", "storage_stabilizer_tier4", "storage_stabilizer_tier5",
            "storage_stabilizer_tier0_dark", "storage_stabilizer_tier1_dark", "storage_stabilizer_tier2_dark",
            "storage_stabilizer_tier3_dark", "storage_stabilizer_tier4_dark", "storage_stabilizer_tier5_dark");

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void allRegisteredStabilizersUseTheDirectionalBlockImplementation() {
        Block[] blocks = {
                ModBlocks.STORAGE_STABILIZER_TIER0,
                ModBlocks.STORAGE_STABILIZER_TIER1,
                ModBlocks.STORAGE_STABILIZER_TIER2,
                ModBlocks.STORAGE_STABILIZER_TIER3,
                ModBlocks.STORAGE_STABILIZER_TIER4,
                ModBlocks.STORAGE_STABILIZER_TIER5,
                ModBlocks.STORAGE_STABILIZER_TIER0_DARK,
                ModBlocks.STORAGE_STABILIZER_TIER1_DARK,
                ModBlocks.STORAGE_STABILIZER_TIER2_DARK,
                ModBlocks.STORAGE_STABILIZER_TIER3_DARK,
                ModBlocks.STORAGE_STABILIZER_TIER4_DARK,
                ModBlocks.STORAGE_STABILIZER_TIER5_DARK
        };

        assertEquals(STABILIZER_NAMES.size(), blocks.length);
        for (Block block : blocks) {
            assertTrue(block instanceof BlockStorageStabilizer,
                    "registered stabilizer must retain directional state and lifecycle behavior");
            assertTrue(block.getDefaultState().getPropertyKeys().contains(BlockStorageStabilizer.FACING));
        }
    }

    @Test
    void metadataKeepsLegacyZeroAsUpAndRoundTripsAllSixDirections() {
        BlockStorageStabilizer block = new BlockStorageStabilizer();

        assertEquals(EnumFacing.UP, block.getStateFromMeta(0).getValue(BlockStorageStabilizer.FACING),
                "old non-directional metadata must load as the default upward state");

        EnumFacing[] directions = {
                EnumFacing.UP, EnumFacing.DOWN, EnumFacing.NORTH,
                EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST
        };
        int[] expectedMetadata = { 0, 1, 2, 3, 4, 5 };
        for (int index = 0; index < directions.length; index++) {
            IBlockState state = block.getDefaultState().withProperty(BlockStorageStabilizer.FACING, directions[index]);
            assertEquals(expectedMetadata[index], block.getMetaFromState(state));
            assertEquals(directions[index], block.getStateFromMeta(block.getMetaFromState(state))
                    .getValue(BlockStorageStabilizer.FACING));
        }

        assertEquals(EnumFacing.UP, block.getStateFromMeta(6).getValue(BlockStorageStabilizer.FACING));
        assertEquals(EnumFacing.UP, block.getStateFromMeta(7).getValue(BlockStorageStabilizer.FACING));
    }

    @Test
    void directionalShapesAreNonSolidAndMatchTheUpstreamSixFaceGeometry() {
        BlockStorageStabilizer block = new BlockStorageStabilizer();
        assertTrue(block.isPassable(null, null));

        AxisAlignedBB[] expected = {
                new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.3D, 1.0D),
                new AxisAlignedBB(0.0D, 0.7D, 0.0D, 1.0D, 1.0D, 1.0D),
                new AxisAlignedBB(0.0D, 0.0D, 0.7D, 1.0D, 1.0D, 1.0D),
                new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.3D),
                new AxisAlignedBB(0.7D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D),
                new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.3D, 1.0D, 1.0D)
        };
        EnumFacing[] directions = {
                EnumFacing.UP, EnumFacing.DOWN, EnumFacing.NORTH,
                EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST
        };
        for (int index = 0; index < directions.length; index++) {
            IBlockState state = block.getDefaultState().withProperty(BlockStorageStabilizer.FACING, directions[index]);
            assertBox(expected[index], block.getBoundingBox(state, null, null));
            assertNull(block.getCollisionBoundingBox(state, null, null));
        }
    }

    @Test
    void placementUsesTheClickedFaceAsTheStabilizerDirection() {
        BlockStorageStabilizer block = new BlockStorageStabilizer();
        IBlockState state = block.getStateForPlacement(null, new net.minecraft.util.math.BlockPos(0, 0, 0),
                EnumFacing.WEST, 0.0F, 0.0F, 0.0F, 0, null);
        assertEquals(EnumFacing.WEST, state.getValue(BlockStorageStabilizer.FACING));
    }

    @Test
    void everyBlockstateContainsAllSixRotatedDirectionalVariants() throws IOException {
        for (String name : STABILIZER_NAMES) {
            JsonObject variants = readJson(name).getAsJsonObject("variants");
            assertEquals(6, variants.size(), name + " must expose six facing variants");

            assertRotation(variants, "facing=down", 180, null);
            assertRotation(variants, "facing=east", 90, 90);
            assertRotation(variants, "facing=north", 90, null);
            assertRotation(variants, "facing=south", 90, 180);
            assertRotation(variants, "facing=up", null, null);
            assertRotation(variants, "facing=west", 90, 270);
        }
    }

    @Test
    void controllerScanRequiresOppositeFacingAndStopsAtTheFirstSolidBlock() throws IOException {
        String controller = readJava("tileentity", "TileEntityStorageController.java");
        assertTrue(controller.contains("state.getValue(BlockStorageStabilizer.FACING) == facing.getOpposite()"));
        assertTrue(controller.contains("if (!this.world.isAirBlock(current))"));
        assertTrue(controller.contains("current.equals(this.pos)"),
                "the downward ray must cross the controller before searching below it");
        assertTrue(controller.contains("current = current.offset(facing)"));
        assertTrue(controller.contains("MAX_STABILIZER_DISTANCE"));
    }

    @Test
    void stabilizerLifecycleNotifiesEveryControllerOnItsFacingRay() throws IOException {
        String stabilizer = readJava("block", "BlockStorageStabilizer.java");
        assertTrue(stabilizer.contains("getStateForPlacement"));
        assertTrue(stabilizer.contains("onBlockAdded"));
        assertTrue(stabilizer.contains("neighborChanged"));
        assertTrue(stabilizer.contains("breakBlock"));
        assertTrue(stabilizer.contains("facing.getAxis().isHorizontal() ? pos.down() : pos"));
        assertTrue(stabilizer.contains("worldIn.getTileEntity(candidate)"));
        assertTrue(stabilizer.contains("updateStabilizers()"));
        assertTrue(stabilizer.contains("Keep tracing after a hit"),
                "a stabilizer must continue tracing after notifying one controller");
    }

    private static void assertBox(AxisAlignedBB expected, AxisAlignedBB actual) {
        assertNotNull(actual);
        assertEquals(expected.minX, actual.minX, 0.000001D);
        assertEquals(expected.minY, actual.minY, 0.000001D);
        assertEquals(expected.minZ, actual.minZ, 0.000001D);
        assertEquals(expected.maxX, actual.maxX, 0.000001D);
        assertEquals(expected.maxY, actual.maxY, 0.000001D);
        assertEquals(expected.maxZ, actual.maxZ, 0.000001D);
    }

    private static void assertRotation(JsonObject variants, String key, Integer x, Integer y) {
        assertTrue(variants.has(key), "missing variant " + key);
        JsonObject variant = variants.getAsJsonObject(key);
        assertNotNull(variant.get("model"));
        if (x == null) {
            assertFalse(variant.has("x"), key + " should not have an x rotation");
        } else {
            assertEquals(x.intValue(), variant.get("x").getAsInt(), key + " x rotation");
        }
        if (y == null) {
            assertFalse(variant.has("y"), key + " should not have a y rotation");
        } else {
            assertEquals(y.intValue(), variant.get("y").getAsInt(), key + " y rotation");
        }
    }

    private static JsonObject readJson(String name) throws IOException {
        Path path = ASSET_ROOT.resolve("blockstates").resolve(name + ".json");
        return new JsonParser().parse(readUtf8(path)).getAsJsonObject();
    }

    private static String readJava(String directory, String file) throws IOException {
        return readUtf8(JAVA_ROOT.resolve(directory).resolve(file));
    }

    private static String readUtf8(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
