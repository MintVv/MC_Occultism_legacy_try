package com.occultism.occultism.container;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The mineshaft screen and slot origins must match upstream.
 *
 * <p>Upstream blits {@code otherworld_miner.png} at the default 176x166 and puts the miner input at
 * (26, 35), the output grid at y 17 and the player inventory at y 84. This port used to draw its own
 * rectangle panel at 176x178 with slots a few pixels off, so these checks pin the upstream values.
 * The screen numbers cannot be read without a client, so they are checked at the source level.
 */
class MineshaftLayoutContractTest {

    private static final Path SOURCE_ROOT = Paths.get("src/main/java/com/occultism/occultism");

    @Test
    void theScreenUsesTheUpstreamTextureAndSize() throws IOException {
        String screen = new String(Files.readAllBytes(
                SOURCE_ROOT.resolve("client/gui/GuiDimensionalMineshaft.java")), StandardCharsets.UTF_8);
        assertTrue(screen.contains("textures/gui/otherworld_miner.png"),
                "the screen must draw upstream's miner texture");
        assertTrue(screen.contains("xSize = 176"), "upstream keeps the default 176 width");
        assertTrue(screen.contains("ySize = 166"), "upstream keeps the default 166 height");
        assertTrue(screen.contains("drawModalRectWithCustomSizedTexture"),
                "the screen must blit the texture instead of drawing rectangles");
    }

    @Test
    void theContainerUsesTheUpstreamSlotOrigins() throws IOException {
        String container = new String(Files.readAllBytes(
                SOURCE_ROOT.resolve("container/ContainerDimensionalMineshaft.java")), StandardCharsets.UTF_8);
        assertTrue(container.contains("MINER_SLOT, 26, 35"), "upstream puts the miner input at 26,35");
        assertTrue(container.contains("98 + column * 18, 17 + row * 18"),
                "upstream starts the output grid at y 17");
        assertTrue(container.contains("8 + column * 18, 84 + row * 18"),
                "upstream starts the player inventory at y 84");
    }
}
