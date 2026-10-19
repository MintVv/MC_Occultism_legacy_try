package com.occultism.occultism.resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.occultism.occultism.item.ItemPentacleDiviner;
import com.occultism.occultism.registry.ModItems;
import net.minecraft.init.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards three registration mistakes that looked fine in the build but broke in game.
 *
 * <p>The diviner was registered through the generic {@code item(...)} helper, which constructs a
 * plain {@code Item}; the behaviour class existed but nothing instantiated it, so right-clicking did
 * nothing. The door item copied the block's translation key, even though an {@code ItemDoor} is not
 * an {@code ItemBlock}, and the tooltip printed {@code item.tile.occultism.otherplanks_door.name}.
 * And a decorative {@code otherglass} block shipped with upstream's 15-alpha texture, which in 1.12
 * renders as an almost invisible cube, so it was removed.</p>
 */
class ItemRegistrationContractTest {

    @BeforeAll
    static void bootstrap() {
        Bootstrap.register();
    }

    @Test
    void theDivinerIsRegisteredWithItsOwnClass() {
        assertTrue(ModItems.PENTACLE_DIVINER instanceof ItemPentacleDiviner,
                "pentacle_diviner must use ItemPentacleDiviner, not a plain Item");
    }

    @Test
    void theDoorItemDoesNotBorrowTheBlockKey() throws IOException {
        String blocks = read("src/main/java/com/occultism/occultism/registry/ModBlocks.java");
        assertTrue(blocks.contains("setTranslationKey(Tags.MOD_ID + \".otherplanks_door\")"),
                "the door item needs its own item.* key; copying the block key yields item.tile.*");
        assertFalse(blocks.contains("item.setTranslationKey(block.getTranslationKey())"),
                "an ItemDoor must not inherit the block translation key");
    }

    @Test
    void theDoorItemNameExistsInBothLanguages() throws IOException {
        for (String locale : new String[] {"en_us.lang", "zh_cn.lang"}) {
            String lang = read("src/main/resources/assets/occultism/lang/" + locale);
            assertTrue(lang.contains("item.occultism.otherplanks_door.name="),
                    "missing door item name in " + locale);
        }
    }

    @Test
    void theNearInvisibleOtherglassBlockIsGone() throws IOException {
        String blocks = read("src/main/java/com/occultism/occultism/registry/ModBlocks.java");
        assertFalse(blocks.contains("\"otherglass\""),
                "the decorative otherglass block was removed: its upstream texture is 15/255 alpha");
        assertTrue(blocks.contains("\"otherglass_natural\""),
                "the natural variant stays");
        assertEquals(false, Files.exists(Paths.get(
                        "src/main/resources/assets/occultism/blockstates/otherglass.json")),
                "its blockstate was removed with the block");
    }

    private static String read(String relative) throws IOException {
        return new String(Files.readAllBytes(Paths.get(relative)), StandardCharsets.UTF_8);
    }
}
