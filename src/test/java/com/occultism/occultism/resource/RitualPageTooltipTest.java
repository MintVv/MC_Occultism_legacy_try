package com.occultism.occultism.resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The ritual recipe page must not show registry ids to the player.
 *
 * <p>The page used to render two tooltip lines over the pentacle preview: the pentacle's display name
 * and, underneath, {@code ID: <registry path>}. Upstream shows the name only - its recipe category
 * draws the translated multiblock name and nothing else - so the id line was a local addition that
 * leaked internal naming into the interface.</p>
 */
class RitualPageTooltipTest {

    private static final Path SOURCE = Paths.get("src/main/java/com/occultism/occultism");
    private static final Path LANG = Paths.get("src/main/resources/assets/occultism/lang");

    @Test
    void thePentacleTooltipShowsOnlyTheName() throws IOException {
        String wrapper = read(SOURCE.resolve("compat/jei/RitualRecipeWrapper.java"));
        assertTrue(wrapper.contains("jei.occultism.ritual.pentacle\""),
                "the page must still name the pentacle");
        assertFalse(wrapper.contains("pentacle_id"),
                "the page must not render the pentacle registry id");
    }

    @Test
    void theIdTranslationKeyIsGone() throws IOException {
        for (String locale : new String[] {"en_us.lang", "zh_cn.lang"}) {
            String lang = new String(Files.readAllBytes(LANG.resolve(locale)), StandardCharsets.UTF_8);
            assertFalse(lang.contains("jei.occultism.ritual.pentacle_id="),
                    "unused id label left in " + locale);
        }
    }

    private static String read(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
