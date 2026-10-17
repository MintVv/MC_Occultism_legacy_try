package com.occultism.occultism.ritual;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import com.occultism.occultism.registry.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Every registered pentacle must be buildable out of real blocks and then accepted by the
 * detector that is supposed to recognise it.
 *
 * <p>This is the closest thing to an in-world check the test suite can reach: it exercises the
 * pattern data, the symbol table and the matcher together. When they disagree, the pentacle can
 * be drawn exactly as the book shows it and still never start a ritual, with no error to explain
 * why.</p>
 */
class PentacleBuildabilityTest {

    @BeforeAll
    static void bootstrap() {
        Bootstrap.register();
    }

    @Test
    void everyRegisteredPentacleCanBeBuiltAndDetected() {
        assertFalse(PentacleDetector.getDefinitions().isEmpty(), "No pentacles are registered");

        List<String> failures = new ArrayList<>();
        for (PentacleDefinition definition : PentacleDetector.getDefinitions()) {
            String problem = check(definition);
            if (problem != null) {
                failures.add(problem);
            }
        }
        assertTrue(failures.isEmpty(), "Pentacles that cannot be built and detected: " + failures);
    }

    /** Returns null when the pentacle round-trips, otherwise a description of what broke. */
    private static String check(PentacleDefinition definition) {
        RitualTestWorld world = new RitualTestWorld(false);
        BlockPos center = BlockPos.ORIGIN;
        // The centre carries symbol '0', which the matcher skips; place the bowl anyway so the
        // fixture matches what a player builds.
        world.installBowl(center, true);

        for (PentacleDetector.PentacleSymbol symbol : PentacleDetector.getRequiredSymbols(definition, center)) {
            if (!placeSymbol(world, definition, symbol)) {
                return definition.getId() + " symbol '" + symbol.getSymbol() + "' at " + symbol.getPos()
                        + " has no block that satisfies the matcher";
            }
        }

        if (!PentacleDetector.matches(world, center, definition)) {
            return definition.getId() + " is not detected even though every symbol was placed from its own pattern";
        }
        return null;
    }

    /**
     * Picks the first block the matcher accepts for this symbol.
     *
     * <p>{@code matchesPreviewSymbol} decides the block type; skull symbols additionally need the
     * right tile-entity type, which only the world-aware check can confirm.</p>
     */
    private static boolean placeSymbol(RitualTestWorld world, PentacleDefinition definition,
            PentacleDetector.PentacleSymbol symbol) {
        for (IBlockState state : candidates()) {
            if (!PentacleDetector.matchesPreviewSymbol(definition, symbol.getSymbol(), state)) {
                continue;
            }
            world.states.put(symbol.getPos(), state);
            if (state.getBlock() != Blocks.SKULL) {
                return PentacleDetector.matchesRequiredSymbol(world, symbol.getPos(), symbol.getSymbol(), state,
                        definition);
            }
            for (int type = 0; type <= 1; type++) {
                TileEntitySkull skull = new TileEntitySkull();
                skull.setWorld(world);
                skull.setPos(symbol.getPos());
                skull.setType(type);
                world.tiles.put(symbol.getPos(), skull);
                if (PentacleDetector.matchesRequiredSymbol(world, symbol.getPos(), symbol.getSymbol(), state,
                        definition)) {
                    return true;
                }
            }
            // No skull type worked; leave the last one in place and let the caller report it.
            return false;
        }
        return false;
    }

    /**
     * The preview multiblock must ask the definition-aware overload. It places blocks using the
     * pentacle's own symbol meanings, so the definition-blind overload rejects those cells and
     * leaves the preview unplaceable - which is exactly what happened for summon_wild_afrit.
     */
    @Test
    void thePatchouliPreviewChecksSymbolsWithTheDefinitionInHand() throws Exception {
        String source = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(
                "src/main/java/com/occultism/occultism/compat/patchouli/PentacleMultiblock.java")),
                java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(source.contains("matchesRequiredSymbol(world, target, symbol, world.getBlockState(target),")
                        || source.matches("(?s)matchesRequiredSymbol\\([^)]*this\\.definition\\s*\\)"),
                "PentacleMultiblock.test must call the definition-aware matchesRequiredSymbol overload");
    }

    private static List<IBlockState> candidates() {
        List<IBlockState> states = new ArrayList<>();
        for (Block glyph : ModBlocks.CHALK_GLYPHS) {
            states.add(glyph.getDefaultState());
        }
        states.add(ModBlocks.LARGE_CANDLE.getDefaultState());
        states.add(ModBlocks.SPIRIT_ATTUNED_CRYSTAL.getDefaultState());
        states.add(Blocks.SKULL.getDefaultState());
        return states;
    }
}
