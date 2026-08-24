package com.occultism.occultism.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

/** Source and resource contract for the upstream 1.15 spirit inventory screen. */
class SpiritGuiPortContractTest {

    @Test
    void guiMatchesUpstreamLayoutAndInformation() throws Exception {
        String gui = readSource("src", "main", "java", "com", "occultism", "occultism", "client",
                "gui", "GuiSpirit.java");
        String container = readSource("src", "main", "java", "com", "occultism", "occultism",
                "container", "ContainerSpirit.java");

        assertTrue(gui.contains("this.xSize = 175"));
        assertTrue(gui.contains("this.ySize = 165"));
        assertTrue(gui.contains("this.guiLeft + 35, this.guiTop + 65, 30"));
        assertTrue(gui.contains("TRANSLATION_KEY_BASE + \".age\""));
        assertTrue(gui.contains("TRANSLATION_KEY_BASE + \".job\""));
        assertTrue(gui.contains("ItemNBTUtil.formatSpiritName"));
        assertTrue(container.contains("new EntityHandsInvWrapper(spirit), 0, 152, 54"));
        assertTrue(container.contains("playerInventoryTop = 84"));
        assertTrue(container.contains("hotbarTop = 142"));
    }

    @Test
    void serverOwnsOpeningAndInventoryValidity() throws Exception {
        String entity = readSource("src", "main", "java", "com", "occultism", "occultism", "entity",
                "EntityFoliotJob.java");
        String handler = readSource("src", "main", "java", "com", "occultism", "occultism", "gui",
                "ModGuiHandler.java");
        String container = readSource("src", "main", "java", "com", "occultism", "occultism",
                "container", "ContainerSpirit.java");

        assertTrue(entity.contains("player.isSneaking() && player.getHeldItem(hand).isEmpty()"));
        assertTrue(entity.contains("ModGuiHandler.SPIRIT"));
        assertTrue(handler.contains("world.getEntityByID(x)"));
        assertTrue(handler.contains("new ContainerSpirit(player.inventory"));
        assertTrue(container.contains("this.spirit.world == playerIn.world"));
        assertTrue(container.contains("playerIn.world.getEntityByID(this.spirit.getEntityId()) == this.spirit"));
        assertTrue(container.contains("this.spirit.getDistanceSq(playerIn) < 64.0D"));
    }

    @Test
    void ageIsEntitySyncedAndTranslationsExist() throws Exception {
        String entity = readSource("src", "main", "java", "com", "occultism", "occultism", "entity",
                "EntityFoliotJob.java");
        String english = readSource("src", "main", "resources", "assets", "occultism", "lang",
                "en_us.lang");
        String chinese = readSource("src", "main", "resources", "assets", "occultism", "lang",
                "zh_cn.lang");

        assertTrue(entity.contains("private static final DataParameter<Integer> SPIRIT_AGE"));
        assertTrue(entity.contains("private static final DataParameter<Integer> SPIRIT_MAX_AGE"));
        assertTrue(entity.contains("this.dataManager.set(SPIRIT_AGE"));
        assertTrue(entity.contains("this.dataManager.set(SPIRIT_MAX_AGE"));
        assertTrue(english.contains("gui.occultism.spirit.age=Essence Decay: %d%%"));
        assertTrue(english.contains("gui.occultism.spirit.job=%s"));
        assertTrue(chinese.contains("gui.occultism.spirit.age=本质衰变: %d%%"));
        assertTrue(chinese.contains("gui.occultism.spirit.job=%s"));
    }

    @Test
    void upstreamTextureIsPresentAtNativeSize() throws Exception {
        Path texture = Paths.get("src", "main", "resources", "assets", "occultism", "textures", "gui",
                "inventory_spirit.png");
        BufferedImage image = ImageIO.read(texture.toFile());
        assertNotNull(image);
        assertEquals(256, image.getWidth());
        assertEquals(256, image.getHeight());
    }

    private static String readSource(String... path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path[0], java.util.Arrays.copyOfRange(path, 1, path.length))),
                StandardCharsets.UTF_8);
    }
}
