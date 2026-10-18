package com.occultism.occultism.container;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The satchel layout must match upstream.
 *
 * <p>Upstream's {@code SatchelContainer} declares {@code SATCHEL_SIZE = 13 * 9} and its screen is
 * 247x256, drawn from {@code inventory_satchel.png}. This port used to carry 17 columns by 7 rows
 * (119 slots) and a hand-drawn 322x250 panel. The screen numbers cannot be read without a client,
 * so they are checked at the source level - the same approach the other contract tests use.</p>
 */
class SatchelLayoutContractTest {

    private static final Path SOURCE_ROOT = Paths.get("src/main/java/com/occultism/occultism");

    @Test
    void theSatchelHoldsThirteenByNineSlots() {
        assertEquals(13 * 9, SatchelInventory.SIZE,
                "upstream SatchelContainer declares SATCHEL_SIZE = 13 * 9");
    }

    @Test
    void theContainerUsesTheUpstreamGridAndOrigins() throws IOException {
        String container = read("container/ContainerSatchel.java");
        assertTrue(container.contains("SATCHEL_ROW_LENGTH = 13"), "the satchel grid must be 13 wide");
        assertTrue(container.contains("SATCHEL_LEFT = 8"), "upstream starts the grid at x 8");
        assertTrue(container.contains("SATCHEL_TOP = 8"), "upstream starts the grid at y 8");
        assertTrue(container.contains("PLAYER_INVENTORY_LEFT = 44"), "upstream player inventory starts at x 44");
        assertTrue(container.contains("PLAYER_INVENTORY_TOP = 174"), "upstream player inventory starts at y 174");
        assertTrue(container.contains("HOTBAR_LEFT = 44"), "upstream hotbar starts at x 44");
        assertTrue(container.contains("HOTBAR_TOP = 232"), "upstream hotbar starts at y 232");
    }

    @Test
    void theScreenUsesTheUpstreamImageSizeAndTexture() throws IOException {
        String screen = read("client/gui/GuiSatchel.java");
        assertTrue(screen.contains("xSize = 247"), "upstream satchel screen is 247 wide");
        assertTrue(screen.contains("ySize = 256"), "upstream satchel screen is 256 tall");
        assertTrue(screen.contains("textures/gui/inventory_satchel.png"),
                "the screen must draw upstream's satchel texture");
        assertTrue(screen.contains("drawModalRectWithCustomSizedTexture"),
                "the screen must blit the texture instead of drawing rectangles");
    }

    private static String read(String relative) throws IOException {
        return new String(Files.readAllBytes(SOURCE_ROOT.resolve(relative)), StandardCharsets.UTF_8);
    }
}
