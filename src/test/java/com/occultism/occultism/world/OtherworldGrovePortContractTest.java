package com.occultism.occultism.world;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

class OtherworldGrovePortContractTest {

    @Test
    void generatorIsRestrictedToTheOverworldAndUndergroundCoordinates() throws Exception {
        String source = readSource("src", "main", "java", "com", "occultism", "occultism", "world",
                "WorldGenOtherworldGrove.java");
        assertTrue(source.contains("world.provider.getDimension() != 0"));
        assertTrue(source.contains("world.canSeeSky(pos)"));
        assertTrue(source.contains("MIN_GENERATION_Y"));
        assertTrue(source.contains("MAX_GENERATION_Y"));
    }

    @Test
    void groveUsesNaturalOtherworldBlocksAndNaturalTreeGenerator() throws Exception {
        String source = readSource("src", "main", "java", "com", "occultism", "occultism", "world",
                "WorldGenOtherworldGrove.java");
        assertTrue(source.contains("ModBlocks.OTHERSTONE_NATURAL"));
        assertTrue(source.contains("ModBlocks.OTHERFLOWER_NATURAL"));
        assertTrue(source.contains("new WorldGenOtherworldTree(true, true)"));
        assertTrue(source.contains("Blocks.GRASS"));
        assertTrue(source.contains("FLOOR_OFFSET"));
    }

    @Test
    void worldGeneratorKeepsNetherOreGenerationSeparateFromTheGrove() throws Exception {
        String source = readSource("src", "main", "java", "com", "occultism", "occultism", "world",
                "ModWorldGenerator.java");
        assertTrue(source.contains("dimension == 0"));
        assertTrue(source.contains("OTHERWORLD_GROVE.generate(world, random, grovePos)"));
        assertTrue(source.contains("dimension == -1"));
        assertTrue(source.contains("ModBlocks.IESNIUM_ORE_NATURAL"));
        assertFalse(source.indexOf("ModBlocks.IESNIUM_ORE_NATURAL") < source.indexOf("dimension == 0"),
                "Nether iesnium generation must remain in the Nether branch");
    }

    @Test
    void generatorProtectsLiquidsBedrockAndTileEntitiesBeforeCarving() throws Exception {
        String source = readSource("src", "main", "java", "com", "occultism", "occultism", "world",
                "WorldGenOtherworldGrove.java");
        assertTrue(source.contains("material.isLiquid()"));
        assertTrue(source.contains("block == Blocks.BEDROCK"));
        assertTrue(source.contains("world.getTileEntity(pos) != null"));
        assertTrue(source.contains("hasSafeTerrain(world, pos)"));
        assertTrue(source.contains("if (!this.isInsideEllipsoid"));
        assertTrue(source.contains("continue;"), "Offsets outside the ellipsoid must be skipped, not reject the grove");
        assertTrue(source.contains("block instanceof BlockOre"),
                "Natural ore veins must not make almost every candidate chamber fail preflight");
        assertTrue(source.contains("states[x + HORIZONTAL_RADIUS][y + VERTICAL_RADIUS][z + HORIZONTAL_RADIUS]"));
    }

    @Test
    void groveTriggerStaysInsideTheGeneratingChunk() throws Exception {
        String source = readSource("src", "main", "java", "com", "occultism", "occultism", "world",
                "ModWorldGenerator.java");
        assertTrue(source.contains("chunkX * 16 + 7 + random.nextInt(2)"));
        assertTrue(source.contains("chunkZ * 16 + 7 + random.nextInt(2)"));
    }

    private static String readSource(String... path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path[0], Arrays.copyOfRange(path, 1, path.length))),
                StandardCharsets.UTF_8);
    }
}
