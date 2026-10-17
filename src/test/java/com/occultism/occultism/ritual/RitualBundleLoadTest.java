package com.occultism.occultism.ritual;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.occultism.occultism.registry.ModBlocks;
import com.occultism.occultism.registry.ModItems;
import net.minecraft.block.Block;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Loads the shipped ritual bundle through the production loader.
 *
 * <p>The registry tests feed {@link RitualRecipeManager} synthetic JSON, so the real data file was
 * never parsed by the code that reads it at server start. A schema slip, a renamed item or an
 * unparsable ingredient would therefore only ever show up as a failed ritual table on a running
 * server. This parses the actual resource with every mod item available.</p>
 */
class RitualBundleLoadTest {

    private static final String RITUAL_RESOURCE = "/assets/occultism/rituals/rituals.json";

    @BeforeAll
    static void bootstrap() throws Exception {
        Bootstrap.register();
        withRegistryUnlocked(() -> {
            try {
                registerModItems();
                registerModBlockItems();
            } catch (Exception exception) {
                throw new IllegalStateException(exception);
            }
        });
    }

    @Test
    void theShippedBundleLoadsCompletelyThroughTheLoader() throws Exception {
        JsonElement bundle = new JsonParser().parse(
                new InputStreamReader(openBundle(), StandardCharsets.UTF_8));
        RitualRecipeManager.load(openBundle());

        Set<String> expected = new HashSet<>();
        for (JsonElement element : bundle.getAsJsonArray()) {
            expected.add(element.getAsJsonObject().get("id").getAsString());
        }
        assertFalse(expected.isEmpty(), "The shipped ritual file must not be empty");

        Set<String> loaded = new HashSet<>();
        for (RitualDefinition definition : RitualRecipeManager.getDefinitions()) {
            loaded.add(definition.getId());
        }

        Set<String> missing = new HashSet<>(expected);
        missing.removeAll(loaded);
        assertTrue(missing.isEmpty(), "Rituals the loader dropped: " + missing);
        assertEquals(expected.size(), loaded.size(), "Loader produced a different number of rituals");

        // Every definition must be usable: the result stack has to resolve and the pentacle has to
        // exist, otherwise the ritual starts and then fails at completion.
        for (RitualDefinition definition : RitualRecipeManager.getDefinitions()) {
            assertFalse(definition.getResult().createStack().isEmpty(),
                    definition.getId() + " has an unresolvable result");
            assertTrue(PentacleDetector.getDefinitionById(definition.getPentacleId()) != null,
                    definition.getId() + " references unknown pentacle " + definition.getPentacleId());
        }
    }

    private static InputStream openBundle() {
        InputStream stream = RitualBundleLoadTest.class.getResourceAsStream(RITUAL_RESOURCE);
        assertTrue(stream != null, "Missing ritual resource " + RITUAL_RESOURCE);
        return stream;
    }


    /** Forge locks the registry after pre-init; unlock it just for this fixture. */
    private static void withRegistryUnlocked(Runnable action) throws Exception {
        Object registry = Item.REGISTRY;
        Field locked = null;
        for (Class<?> type = registry.getClass(); type != null && locked == null; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                if (field.getType() == boolean.class && field.getName().toLowerCase().contains("lock")) {
                    field.setAccessible(true);
                    locked = field;
                    break;
                }
            }
        }
        if (locked == null) {
            throw new IllegalStateException("No lock flag found on " + registry.getClass());
        }
        boolean previous = locked.getBoolean(registry);
        locked.setBoolean(registry, false);
        try {
            action.run();
        } finally {
            locked.setBoolean(registry, previous);
        }
    }

    private static void registerModItems() throws Exception {
        for (Field field : ModItems.class.getFields()) {
            if (!Modifier.isStatic(field.getModifiers()) || !Item.class.isAssignableFrom(field.getType())) {
                continue;
            }
            Item item = (Item) field.get(null);
            if (item == null || item.getRegistryName() == null) {
                continue;
            }
            if (!Item.REGISTRY.containsKey(item.getRegistryName())) {
                Item.REGISTRY.putObject(item.getRegistryName(), item);
            }
        }
        assertTrue(Item.getByNameOrId("occultism:book_of_binding_bound_foliot") != null,
                "Item registration helper did not work");
    }

    private static void registerModBlockItems() throws Exception {
        for (Field field : ModBlocks.class.getFields()) {
            if (!Modifier.isStatic(field.getModifiers()) || !Block.class.isAssignableFrom(field.getType())) {
                continue;
            }
            Block block = (Block) field.get(null);
            if (block == null || block.getRegistryName() == null) {
                continue;
            }
            if (!ForgeRegistries.BLOCKS.containsKey(block.getRegistryName())) {
                ForgeRegistries.BLOCKS.register(block);
            }
            if (!Item.REGISTRY.containsKey(block.getRegistryName())) {
                Item.REGISTRY.putObject(block.getRegistryName(),
                        new ItemBlock(block).setRegistryName(block.getRegistryName()));
            }
        }
    }
}
