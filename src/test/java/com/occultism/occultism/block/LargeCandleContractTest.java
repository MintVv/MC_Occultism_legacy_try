package com.occultism.occultism.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.occultism.occultism.registry.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * The large candle must stay lightable.
 *
 * <p>The guide tells players the candle "can be lit" and that a torch changes its state, but the
 * block used to be a bare pentacle marker with no state at all. These checks keep the behaviour
 * from silently regressing back to that.</p>
 */
class LargeCandleContractTest {

    @BeforeAll
    static void bootstrap() {
        Bootstrap.register();
    }

    @Test
    void theCandleCarriesALitState() {
        assertTrue(ModBlocks.LARGE_CANDLE instanceof BlockLargeCandle,
                "large_candle must use BlockLargeCandle, not the bare pentacle marker");

        IBlockState unlit = ModBlocks.LARGE_CANDLE.getDefaultState();
        assertFalse(unlit.getValue(BlockLargeCandle.LIT), "a fresh candle must start unlit");

        IBlockState lit = unlit.withProperty(BlockLargeCandle.LIT, Boolean.TRUE);
        assertTrue(lit.getValue(BlockLargeCandle.LIT));
    }

    @Test
    void onlyTheLitStateEmitsLight() {
        IBlockState unlit = ModBlocks.LARGE_CANDLE.getDefaultState();
        IBlockState lit = unlit.withProperty(BlockLargeCandle.LIT, Boolean.TRUE);

        assertEquals(0, ModBlocks.LARGE_CANDLE.getLightValue(unlit, null, null));
        assertTrue(ModBlocks.LARGE_CANDLE.getLightValue(lit, null, null) > 0,
                "a lit candle must emit light");
    }

    @Test
    void theLitStateSurvivesAMetadataRoundTrip() {
        IBlockState lit = ModBlocks.LARGE_CANDLE.getDefaultState()
                .withProperty(BlockLargeCandle.LIT, Boolean.TRUE);
        int meta = ModBlocks.LARGE_CANDLE.getMetaFromState(lit);
        assertTrue(ModBlocks.LARGE_CANDLE.getStateFromMeta(meta).getValue(BlockLargeCandle.LIT),
                "the lit flag must round-trip through metadata so it survives a save");
    }

    @Test
    void pentacleDetectionStillMatchesTheBlock() {
        // Detection compares the block, not the state, so a lit candle in a pentacle keeps working.
        assertEquals(ModBlocks.LARGE_CANDLE,
                ModBlocks.LARGE_CANDLE.getDefaultState().withProperty(BlockLargeCandle.LIT, Boolean.TRUE).getBlock());
    }
}
