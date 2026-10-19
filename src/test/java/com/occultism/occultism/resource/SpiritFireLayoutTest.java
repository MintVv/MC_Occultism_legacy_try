package com.occultism.occultism.resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The spirit fire page must keep its icons in their own boxes.
 *
 * <p>It used to overlap itself: with the input at x 30 and the flame at x 82, the first arrow - drawn
 * from the input plus 26 and 32 pixels long - ran from 56 to 88 and disappeared under the flame, and
 * the second arrow ran from 108 to 140 across the output slot at 133. The page is drawn by two
 * classes, so the numbers now live in the category and the wrapper reads them; this test pins the
 * arithmetic rather than the source strings, so the layout cannot silently overlap again.</p>
 */
class SpiritFireLayoutTest {

    private static final Path JEI = Paths.get("src/main/java/com/occultism/occultism/compat/jei");
    private static final int SLOT = 16;

    @Test
    void theThreeColumnsDoNotOverlap() throws IOException {
        int input = constant("SpiritFireRecipeCategory.java", "INPUT_X");
        int fire = constant("SpiritFireRecipeCategory.java", "FIRE_X");
        int output = constant("SpiritFireRecipeCategory.java", "OUTPUT_X");
        int length = constant("SpiritFireRecipeCategory.java", "ARROW_LENGTH");
        int before = constant("SpiritFireRecipeCategory.java", "ARROW_BEFORE_X");
        int after = constant("SpiritFireRecipeCategory.java", "ARROW_AFTER_X");

        assertTrue(before >= input + SLOT, "the first arrow must start after the input slot");
        assertTrue(before + length <= fire, "the first arrow must end before the flame");
        assertTrue(after >= fire + SLOT, "the second arrow must start after the flame");
        assertTrue(after + length <= output, "the second arrow must end before the output slot");
    }

    @Test
    void theWrapperReadsTheCategoryConstants() throws IOException {
        String wrapper = new String(Files.readAllBytes(JEI.resolve("SpiritFireRecipeWrapper.java")),
                StandardCharsets.UTF_8);
        assertTrue(wrapper.contains("SpiritFireRecipeCategory.FIRE_X"),
                "the wrapper must not duplicate the layout numbers");
        assertTrue(wrapper.contains("SpiritFireRecipeCategory.ARROW_BEFORE_X"));
        assertTrue(!wrapper.contains("private static final int FIRE_X ="),
                "a second copy of the layout is how the icons drifted apart");
    }

    private static int constant(String file, String name) throws IOException {
        String source = new String(Files.readAllBytes(JEI.resolve(file)), StandardCharsets.UTF_8);
        for (String line : source.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("static final int " + name + " =")) {
                String value = trimmed.substring(trimmed.indexOf('=') + 1).replace(";", "").trim();
                try {
                    return Integer.parseInt(value);
                } catch (NumberFormatException exception) {
                    // Expression form such as "INPUT_X + 20": resolve the single operand.
                    String[] parts = value.split("\\+");
                    return constant(file, parts[0].trim()) + Integer.parseInt(parts[1].trim());
                }
            }
        }
        throw new AssertionError("constant not found: " + name + " in " + file);
    }

    @Test
    void theRowsFitInsideTheBackground() throws IOException {
        int first = constant("SpiritFireRecipeCategory.java", "FIRST_ROW_Y");
        int second = constant("SpiritFireRecipeCategory.java", "SECOND_ROW_Y");
        String category = new String(Files.readAllBytes(JEI.resolve("SpiritFireRecipeCategory.java")),
                StandardCharsets.UTF_8);
        assertTrue(category.contains("createBlankDrawable(168, 150)"), "background size changed");
        assertEquals(true, second + SLOT <= 150, "the second row must fit inside the background");
        assertEquals(true, first + SLOT <= second, "the rows must not overlap");
    }
}
