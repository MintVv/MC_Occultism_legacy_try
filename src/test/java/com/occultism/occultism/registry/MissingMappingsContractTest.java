package com.occultism.occultism.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.occultism.occultism.Tags;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.junit.jupiter.api.Test;

/**
 * Pins the disposition of the eight Nautilus armor registry names that only exist in old
 * development saves. The armor family is intentionally not migrated (en_us.lang:1498), so these
 * entries must be ignored, never remapped onto a substitute item.
 */
class MissingMappingsContractTest {

    private static final Set<String> EXPECTED_PATHS = new HashSet<>(Arrays.asList(
            "copper_nautilus_armor",
            "diamond_nautilus_armor",
            "golden_nautilus_armor",
            "iron_nautilus_armor",
            "ritual_dummy/misc_copper_nautilus_armor",
            "ritual_dummy/misc_diamond_nautilus_armor",
            "ritual_dummy/misc_golden_nautilus_armor",
            "ritual_dummy/misc_iron_nautilus_armor"));

    @Test
    void retiredListHoldsExactlyTheEightOrphanedNautilusArmorNames() {
        assertEquals(EXPECTED_PATHS, ModMissingMappings.getRetiredItemPaths());
        assertEquals(8, ModMissingMappings.getRetiredItemPaths().size());
        for (String path : EXPECTED_PATHS) {
            assertTrue(ModMissingMappings.isRetiredItem(new ResourceLocation(Tags.MOD_ID, path)),
                    "occultism:" + path + " must be recognised as an intentionally retired item");
        }
    }

    @Test
    void ritualDummyNamesKeepTheirSlashSeparatedRegistryForm() {
        for (String path : ModMissingMappings.getRetiredItemPaths()) {
            assertTrue(path.endsWith("_nautilus_armor"), "unexpected retired path: " + path);
            if (path.startsWith("ritual_dummy")) {
                assertTrue(path.startsWith("ritual_dummy/misc_"),
                        "ritual dummy names must keep the slashed 'ritual_dummy/misc_' form: " + path);
            }
            assertFalse(path.contains(":"), "retired paths must not include the namespace: " + path);
        }
        assertEquals(4, ModMissingMappings.getRetiredItemPaths().stream()
                .filter(path -> path.startsWith("ritual_dummy/misc_")).count());
    }

    @Test
    void namesOutsideTheListAndOutsideTheModNamespaceAreLeftAlone() {
        assertFalse(ModMissingMappings.isRetiredItem(null));
        // Still-registered items and unrelated missing entries must keep Forge's default handling.
        assertFalse(ModMissingMappings.isRetiredItem(new ResourceLocation(Tags.MOD_ID, "nautilus_shell")));
        assertFalse(ModMissingMappings.isRetiredItem(
                new ResourceLocation(Tags.MOD_ID, "ritual_dummy/misc_copper_horse_armor")));
        assertFalse(ModMissingMappings.isRetiredItem(new ResourceLocation(Tags.MOD_ID, "spirit_attuned_gem")));
        assertFalse(ModMissingMappings.isRetiredItem(new ResourceLocation(Tags.MOD_ID, "totally_unknown_item")));
        // Same path under a foreign namespace belongs to another mod, not to us.
        assertFalse(ModMissingMappings.isRetiredItem(new ResourceLocation("minecraft", "iron_nautilus_armor")));
        assertFalse(ModMissingMappings.isRetiredItem(new ResourceLocation("othermod", "copper_nautilus_armor")));
    }

    @Test
    void retiredPathSetIsImmutable() {
        assertThrows(UnsupportedOperationException.class,
                () -> ModMissingMappings.getRetiredItemPaths().add("something_else"));
    }

    @Test
    void handlerSubscribesToItemMissingMappingsUnderTheModId() throws Exception {
        Mod.EventBusSubscriber subscriber = ModMissingMappings.class.getAnnotation(Mod.EventBusSubscriber.class);
        assertNotNull(subscriber, "ModMissingMappings must be an @Mod.EventBusSubscriber");
        assertEquals(Tags.MOD_ID, subscriber.modid());
        assertEquals("occultism", Tags.MOD_ID);

        Method handler = ModMissingMappings.class
                .getDeclaredMethod("onMissingItemMappings", RegistryEvent.MissingMappings.class);
        assertNotNull(handler.getAnnotation(SubscribeEvent.class),
                "the missing mapping handler must carry @SubscribeEvent");
        ParameterizedType parameter = (ParameterizedType) handler.getGenericParameterTypes()[0];
        assertEquals(RegistryEvent.MissingMappings.class, parameter.getRawType());
        assertEquals(Item.class, parameter.getActualTypeArguments()[0],
                "all eight orphans are ITEM registry entries, so the generic filter must be Item");
    }

    @Test
    void retiredEntriesAreDroppedAndRenamesAreRemapped() throws Exception {
        // Two different dispositions, both deliberate:
        //  - retired content (the Nautilus armour family) is abandoned, because remapping it onto
        //    something else would silently swap a different item into player inventories;
        //  - renamed content keeps the id the player already owns, remapped from an explicit table
        //    so nothing is substituted by guesswork.
        String source = readSource("src", "main", "java", "com", "occultism", "occultism", "registry",
                "ModMissingMappings.java");
        assertTrue(source.contains("mapping.ignore()"), "retired entries must be dropped with ignore()");
        assertTrue(source.contains("isRetiredItem(mapping.key)"), "retired entries are matched by key");
        assertTrue(source.contains("RENAMED.get("), "renames must come from the explicit table");
        assertTrue(source.contains("mapping.remap("), "renames must remap so the player keeps the item");
        assertFalse(source.contains("mapping.warn()"));
        assertFalse(source.contains("mapping.fail()"));
        assertTrue(source.contains("event.getAllMappings()"));
        assertTrue(source.contains("en_us.lang"),
                "the ignore rationale must cite the intentional non-migration note");
    }

    @Test
    void theRenameTableMatchesUpstream121Ids() {
        assertEquals("chalk_gold",
                com.occultism.occultism.registry.ModMissingMappings.renamedPath("chalk_yellow"));
        assertEquals("chalk_glyph_gold",
                com.occultism.occultism.registry.ModMissingMappings.renamedPath("chalk_glyph_yellow"));
        assertEquals("otherstone_pedestal_silver",
                com.occultism.occultism.registry.ModMissingMappings.renamedPath("otherrock_pedestal"));
        assertNull(com.occultism.occultism.registry.ModMissingMappings.renamedPath("chalk_yellow_impure"),
                "the impure chalk keeps the upstream id and must not be renamed");
    }

    @Test
    void noNautilusArmorItemOrRitualRemainsInCurrentData() throws Exception {
        String items = readSource("src", "main", "java", "com", "occultism", "occultism", "registry",
                "ModItems.java");
        assertFalse(items.contains("nautilus_armor"),
                "nautilus armor items are intentionally not registered");
        assertTrue(items.contains("NAUTILUS_SHELL = item(\"nautilus_shell\")"),
                "the unrelated nautilus shell item must stay registered");

        String rituals = readSource("src", "main", "resources", "assets", "occultism", "rituals", "rituals.json");
        assertFalse(rituals.contains("nautilus_armor"),
                "no ritual may produce an ignored nautilus armor id");
    }

    private static String readSource(String... path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path[0], Arrays.copyOfRange(path, 1, path.length))),
                StandardCharsets.UTF_8);
    }
}
