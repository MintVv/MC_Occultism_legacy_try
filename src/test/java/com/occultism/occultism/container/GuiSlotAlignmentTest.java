package com.occultism.occultism.container;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks the container slot originals against the slot grid actually drawn in the GUI textures.
 *
 * <p>The previous two rounds moved the satchel and mineshaft layouts onto the upstream textures. A
 * source-level check can only confirm the numbers were copied; it cannot notice that a number is
 * wrong. This test reads the PNGs, finds the 16x16 slot cells by their vanilla interior colour, and
 * compares the detected column and row origins with the constants the containers use. When the two
 * disagree the slots are drawn off the artwork, which is exactly the bug it caught on the mineshaft
 * hotbar (154 against 142 in both the texture and upstream).</p>
 */
class GuiSlotAlignmentTest {

    private static final Path ASSETS = Paths.get("src/main/resources/assets/occultism");
    /** Vanilla slot interiors: normal slots, and the darker filter slots the transporter draws. */
    private static final int[] SLOT_COLOURS = {0xFF8B8B8B, 0xFF474747};

    @Test
    void satchelSlotsSitOnTheTextureGrid() throws IOException {
        Grid grid = Grid.of(ASSETS.resolve("textures/gui/inventory_satchel.png"));

        // 13 wide by 9 tall storage grid starting at the upstream origin.
        assertEquals(13, grid.columnCount(), "the satchel texture must show 13 slot columns");
        assertEquals(8, grid.columnStarts().first().intValue(), "the storage grid starts at x 8");
        assertTrue(grid.rowStarts().contains(8), "the storage grid starts at y 8");
        assertTrue(grid.rowStarts().contains(8 + 8 * 18), "the storage grid is nine rows tall");

        // Player inventory and hotbar, both from upstream.
        assertTrue(grid.rowStarts().contains(174), "the player inventory starts at y 174");
        assertTrue(grid.rowStarts().contains(232), "the hotbar sits at y 232");
    }

    @Test
    void mineshaftSlotsSitOnTheTextureGrid() throws IOException {
        Grid grid = Grid.of(ASSETS.resolve("textures/gui/otherworld_miner.png"));

        assertEquals(9, grid.columnCount(), "the miner texture must show 9 slot columns");
        assertTrue(grid.rowStarts().contains(17), "the output grid starts at y 17");
        assertTrue(grid.rowStarts().contains(84), "the player inventory starts at y 84");
        assertTrue(grid.rowStarts().contains(142), "the hotbar sits at y 142");
    }

    @Test
    void theContainersUseStartsTheTexturesAgreeWith() throws IOException {
        String satchel = read("container/ContainerSatchel.java");
        assertTrue(satchel.contains("PLAYER_INVENTORY_TOP = 174"), "satchel player inventory must match the texture");
        assertTrue(satchel.contains("HOTBAR_TOP = 232"), "satchel hotbar must match the texture");

        String miner = read("container/ContainerDimensionalMineshaft.java");
        assertTrue(miner.contains("8 + column * 18, 142"), "mineshaft hotbar must match the texture");
        assertTrue(miner.contains("8 + column * 18, 84 + row * 18"), "mineshaft inventory must match the texture");
    }

    @Test
    void spiritScreenSlotsSitOnTheTextureGrid() throws IOException {
        Grid grid = Grid.of(ASSETS.resolve("textures/gui/inventory_spirit.png"));
        // ContainerSpirit: player inventory at y 84, hotbar at y 142, spirit slot at (152, 54).
        assertTrue(grid.rowStarts().contains(84), "the spirit screen inventory starts at y 84");
        assertTrue(grid.rowStarts().contains(142), "the spirit screen hotbar sits at y 142");
        assertTrue(grid.rowStarts().contains(54), "the spirit slot sits at y 54");
        assertEquals(9, grid.columnCount(), "the spirit screen shows 9 slot columns");
    }

    @Test
    void transporterScreenSlotsSitOnTheTextureGrid() throws IOException {
        Grid grid = Grid.of(ASSETS.resolve("textures/gui/inventory_spirit_transporter_tagfilter.png"));
        // ContainerSpiritTransporter: filter rows at 84 and 102, player inventory at 138, hotbar at 196.
        assertTrue(grid.rowStarts().contains(84), "the first filter row sits at y 84");
        assertTrue(grid.rowStarts().contains(102), "the second filter row sits at y 102");
        assertTrue(grid.rowStarts().contains(138), "the transporter inventory starts at y 138");
        assertTrue(grid.rowStarts().contains(196), "the transporter hotbar sits at y 196");
    }

    @Test
    void theSpiritContainersUseStartsTheTexturesAgreeWith() throws IOException {
        String spirit = read("container/ContainerSpirit.java");
        assertTrue(spirit.contains("playerInventoryTop = 84"), "the spirit inventory must start at y 84");
        assertTrue(spirit.contains("hotbarTop = 142"), "the spirit hotbar must sit at y 142");
        assertTrue(spirit.contains("152, 54"), "the spirit slot must sit at 152,54");

        String transporter = read("container/ContainerSpiritTransporter.java");
        assertTrue(transporter.contains("playerInventoryTop = 138"), "the transporter inventory starts at 138");
        assertTrue(transporter.contains("hotbarTop = 196"), "the transporter hotbar sits at 196");
        assertTrue(transporter.contains("top = 84;"), "the filter rows start at y 84");
    }

    private static String read(String relative) throws IOException {
        return new String(Files.readAllBytes(Paths.get("src/main/java/com/occultism/occultism").resolve(relative)),
                StandardCharsets.UTF_8);
    }

    /** The distinct slot origins of a GUI texture, discovered from the artwork itself. */
    private static final class Grid {

        private final TreeSet<Integer> columns = new TreeSet<>();
        private final TreeSet<Integer> rows = new TreeSet<>();

        static Grid of(Path texture) throws IOException {
            BufferedImage image = ImageIO.read(texture.toFile());
            if (image == null) {
                throw new IOException("cannot read " + texture);
            }
            Grid grid = new Grid();
            List<int[]> corners = new ArrayList<>();
            for (int y = 0; y + 16 <= image.getHeight(); y++) {
                for (int x = 0; x + 16 <= image.getWidth(); x++) {
                    if (!isSlotCell(image, x, y)) {
                        continue;
                    }
                    boolean insideKnownCell = false;
                    for (int[] corner : corners) {
                        if (x - corner[0] >= 0 && x - corner[0] < 16 && y - corner[1] >= 0 && y - corner[1] < 16) {
                            insideKnownCell = true;
                            break;
                        }
                    }
                    if (!insideKnownCell) {
                        corners.add(new int[] {x, y});
                    }
                }
            }
            for (int[] corner : corners) {
                grid.columns.add(corner[0]);
                grid.rows.add(corner[1]);
            }
            return grid;
        }

        private static boolean isSlotCell(BufferedImage image, int x, int y) {
            for (int colour : SLOT_COLOURS) {
                if (isUniform(image, x, y, colour)) {
                    return true;
                }
            }
            return false;
        }

        private static boolean isUniform(BufferedImage image, int x, int y, int colour) {
            for (int dy = 0; dy < 16; dy++) {
                for (int dx = 0; dx < 16; dx++) {
                    if ((image.getRGB(x + dx, y + dy) & 0xFFFFFFFF) != colour) {
                        return false;
                    }
                }
            }
            return true;
        }

        int columnCount() {
            return this.columns.size();
        }

        TreeSet<Integer> columnStarts() {
            return this.columns;
        }

        TreeSet<Integer> rowStarts() {
            return this.rows;
        }
    }
}
