package com.occultism.occultism.registry;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.occultism.occultism.Tags;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Silently retires registry names that older development saves still reference but that this port
 * deliberately never registered.
 *
 * <p>Without this handler Forge treats those save entries as unidentified mappings: it prints a
 * missing registry entry warning, prompts the player, and forces a full world backup on every load.
 *
 * <p>The Nautilus armor family is the only such case. It depends on the modern mount equipment
 * system, which has no supported 1.12 gameplay path, so as recorded in
 * {@code src/main/resources/assets/occultism/lang/en_us.lang:1498}
 * ({@code guide.occultism.dictionary.animal_armor.body}) "those armor rituals and items are
 * intentionally not migrated". There is therefore no semantically equivalent item to
 * {@code remap()} onto: remapping would silently swap a different item into player inventories and
 * containers. {@code ignore()} is the honest disposition - the stale entry is abandoned and the
 * reference disappears, exactly matching the "not migrated" decision. {@code rituals.json} contains
 * no Nautilus armor ritual either, so nothing in current data can recreate these ids.
 *
 * <p>The list below was derived by diffing every {@code occultism:} registry name stored in old
 * development saves ({@code run/saves/New World-20260711-182812.zip} and
 * {@code New World-20260622-024350.zip}, 482 names each) against the registry names actually
 * present in {@code src/main/java}. Both saves yielded the same eight orphans: four armor items and
 * their four ritual dummy items.
 *
 * <p>Anything not on this list is intentionally left at {@link RegistryEvent.MissingMappings.Action#DEFAULT}
 * so genuinely unexpected missing entries stay loud and visible.
 */
@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class ModMissingMappings {

    /**
     * Registry paths (namespace is always {@code occultism}) of the intentionally unmigrated
     * Nautilus armor items and their ritual dummies. Note the dummy paths contain a slash.
     */
    private static final Set<String> RETIRED_ITEM_PATHS = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(
                    "copper_nautilus_armor",
                    "diamond_nautilus_armor",
                    "golden_nautilus_armor",
                    "iron_nautilus_armor",
                    "ritual_dummy/misc_copper_nautilus_armor",
                    "ritual_dummy/misc_diamond_nautilus_armor",
                    "ritual_dummy/misc_golden_nautilus_armor",
                    "ritual_dummy/misc_iron_nautilus_armor")));

    private ModMissingMappings() {
    }

    /**
     * Returns the immutable set of retired item registry paths, exposed for contract tests.
     */
    public static Set<String> getRetiredItemPaths() {
        return RETIRED_ITEM_PATHS;
    }

    /**
     * @return true when the given missing registry name is a known, intentionally unmigrated entry
     *         that should be dropped instead of reported.
     */
    public static boolean isRetiredItem(ResourceLocation key) {
        return key != null
                && Tags.MOD_ID.equals(key.getNamespace())
                && RETIRED_ITEM_PATHS.contains(key.getPath());
    }

    @SubscribeEvent
    public static void onMissingItemMappings(RegistryEvent.MissingMappings<Item> event) {
        for (RegistryEvent.MissingMappings.Mapping<Item> mapping : event.getAllMappings()) {
            if (isRetiredItem(mapping.key)) {
                // Abandon the stale save reference. Never remap: see the class javadoc.
                mapping.ignore();
            }
        }
    }
}
