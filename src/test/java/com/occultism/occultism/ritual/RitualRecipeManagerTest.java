package com.occultism.occultism.ritual;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import com.occultism.occultism.Occultism;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RitualRecipeManagerTest {
    private Field recipes;
    private Object previousRecipes;

    @BeforeAll
    static void bootstrap() {
        Bootstrap.register();
    }

    @BeforeEach
    void saveRegistry() throws Exception {
        recipes = RitualRecipeManager.class.getDeclaredField("definitions");
        recipes.setAccessible(true);
        previousRecipes = recipes.get(null);
        RitualRecipeManager.load(json("[]"));
    }

    @AfterEach
    void restoreRegistry() throws Exception {
        recipes.set(null, previousRecipes);
    }

    @Test
    void emptyArrayAndWrappedEmptyRegistryStayLoadedAcrossEveryQuery() {
        assertEmptyQueriesDoNotReload();
        RitualRecipeManager.load(json("{\"rituals\":[]}"));
        assertEmptyQueriesDoNotReload();
    }

    @Test
    void removingLastRecipeByIdDoesNotBringItBack() {
        RitualRecipeManager.addDefinition(definition("last"));
        assertEquals(1, RitualRecipeManager.removeById("occultism:last"));
        assertEquals(0, RitualRecipeManager.removeById("last"));
        assertEmptyQueriesDoNotReload();
    }

    @Test
    void removingLastPentacleByPredicateKeepsAnEmptyRegistry() {
        RitualRecipeManager.addDefinition(definition("first"));
        RitualRecipeManager.addDefinition(definition("second"));
        assertEquals(2, RitualRecipeManager.removeIf(recipe -> "summon_foliot".equals(recipe.getPentacleId())));
        assertEquals(0, RitualRecipeManager.removeIf(recipe -> true));
        assertEmptyQueriesDoNotReload();
    }

    @Test
    void clearIsIdempotentAndAddingAfterClearDoesNotReloadBundledRecipes() {
        RitualRecipeManager.addDefinition(definition("first"));
        RitualRecipeManager.addDefinition(definition("second"));
        assertEquals(2, RitualRecipeManager.removeAll());
        assertEquals(0, RitualRecipeManager.removeAll());
        assertEmptyQueriesDoNotReload();
        RitualDefinition replacement = definition("only_script_recipe");
        RitualRecipeManager.addDefinition(replacement);
        assertEquals(Collections.singletonList(replacement), RitualRecipeManager.getDefinitions());
    }

    @Test
    void failedLoadIsTerminalUntilAnExplicitReloadAndNeverPublishesAPartialTable() {
        RitualRecipeManager.addDefinition(definition("old"));
        RitualRecipeManager.load(json("[" + recipeJson("valid") + ",{}]"));
        assertEmptyQueriesDoNotReload();
        RitualRecipeManager.load(json("[" + recipeJson("recovered") + "]"));
        assertEquals(1, RitualRecipeManager.getDefinitions().size());
        assertNotNull(RitualRecipeManager.getById("recovered"));
        assertNull(RitualRecipeManager.getById("old"));
        assertNull(RitualRecipeManager.getById("valid"));
    }

    @Test
    void missingOrMalformedResourceDoesNotRetryOnQueries() {
        RitualRecipeManager.load((InputStream) null);
        assertEmptyQueriesDoNotReload();
        RitualRecipeManager.load(json("{broken"));
        assertEmptyQueriesDoNotReload();
    }

    @Test
    void failedReadClosesItsResourceAndLeavesAnInitializedEmptyRegistry() {
        boolean[] closed = {false};
        RitualRecipeManager.load(new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("Test ritual resource read failure");
            }

            @Override
            public void close() {
                closed[0] = true;
            }
        });
        assertTrue(closed[0]);
        assertEmptyQueriesDoNotReload();
    }

    @Test
    void explicitReloadReplacesScriptsEvenWhenThePreviousTableWasEmpty() {
        RitualRecipeManager.load(json("[" + recipeJson("default_test") + "]"));
        assertNotNull(RitualRecipeManager.getById("default_test"));
        RitualRecipeManager.removeAll();
        RitualRecipeManager.addDefinition(definition("script"));
        RitualRecipeManager.load(json("{\"rituals\":[" + recipeJson("replacement_test") + "]}"));
        assertEquals(1, RitualRecipeManager.getDefinitions().size());
        assertNotNull(RitualRecipeManager.getById("replacement_test"));
        assertNull(RitualRecipeManager.getById("script"));
        assertNull(RitualRecipeManager.getById("default_test"));
    }

    @Test
    void scriptMutationsPublishImmutableSnapshotsAndReplaceIdsWithoutDuplicates() {
        RitualDefinition first = definition("same_id");
        RitualRecipeManager.addDefinition(first);
        List<RitualDefinition> before = RitualRecipeManager.getDefinitions();
        RitualDefinition replacement = definition("same_id");
        RitualRecipeManager.addDefinition(replacement);
        assertEquals(Collections.singletonList(first), before);
        assertEquals(Collections.singletonList(replacement), RitualRecipeManager.getDefinitions());
        assertThrows(UnsupportedOperationException.class, () -> before.clear());
        List<RitualDefinition> beforeClear = RitualRecipeManager.getDefinitions();
        RitualRecipeManager.removeAll();
        assertEquals(Collections.singletonList(replacement), beforeClear);
        assertEmptyQueriesDoNotReload();
    }

    private void assertEmptyQueriesDoNotReload() {
        List<RitualDefinition> snapshot = RitualRecipeManager.getDefinitions();
        assertTrue(snapshot.isEmpty());
        Logger logger = (Logger) Occultism.LOGGER;
        int[] loadMessages = {0};
        AbstractAppender appender = new AbstractAppender("ritual-registry-no-reload", null,
                PatternLayout.createDefaultLayout(), true) {
            @Override
            public void append(LogEvent event) {
                if (event.getMessage().getFormattedMessage().contains("ritual")) {
                    loadMessages[0]++;
                }
            }
        };
        appender.start();
        logger.addAppender(appender);
        try {
            assertNull(RitualRecipeManager.getById("craft_chalk_white"));
            assertNull(RitualRecipeManager.getFirstForPentacle("summon_foliot"));
            assertTrue(RitualRecipeManager.getForPentacle("summon_foliot").isEmpty());
            assertTrue(RitualRecipeManager.getForPentacleAndActivation("summon_foliot",
                    new ItemStack(Items.BOOK)).isEmpty());
            assertNull(RitualRecipeManager.findMatching(Collections.singletonList(new PentacleMatch(
                    PentacleDetector.getDefinitionById("summon_foliot"), BlockPos.ORIGIN, 1)),
                    new ItemStack(Items.BOOK)));
            assertTrue(RitualRegistry.getDefinitions().isEmpty());
            assertSame(snapshot, RitualRecipeManager.getDefinitions(), "reads must not publish a newly loaded table");
            assertEquals(0, loadMessages[0], "even a failed reload must not repeat or log again on reads");
        } finally {
            logger.removeAppender(appender);
            appender.stop();
        }
    }

    private static RitualDefinition definition(String id) {
        return new RitualDefinition(id, "Registry test", "summon_foliot",
                new RitualIngredient(Items.BOOK, 1), new RitualResult(new ItemStack(Items.DIAMOND)), 30);
    }

    private static String recipeJson(String id) {
        return "{\"id\":\"" + id + "\",\"pentacle_id\":\"summon_foliot\","
                + "\"activation_item\":\"minecraft:book\",\"result\":{\"item\":\"minecraft:diamond\"}}";
    }

    private static InputStream json(String text) {
        return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
    }
}
