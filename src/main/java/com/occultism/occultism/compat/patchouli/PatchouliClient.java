package com.occultism.occultism.compat.patchouli;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.occultism.occultism.registry.ModBlocks;
import com.occultism.occultism.ritual.PentacleDefinition;
import com.occultism.occultism.ritual.PentacleDetector;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSkull;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import vazkii.patchouli.api.PatchouliAPI;
import vazkii.patchouli.client.book.ClientBookRegistry;

@SideOnly(Side.CLIENT)
public final class PatchouliClient {
    private PatchouliClient() {
    }

    public static void register() {
        ClientBookRegistry.INSTANCE.pageTypes.put("occultism:ritual", PageRitual.class);
        ClientBookRegistry.INSTANCE.pageTypes.put("occultism:spirit_fire", PageSpiritFire.class);
        ClientBookRegistry.INSTANCE.pageTypes.put("occultism:spirit_trade", PageSpiritTrade.class);
        for (PentacleDefinition definition : PentacleDetector.getDefinitions()) {
            registerPentacle(definition);
        }
    }

    private static void registerPentacle(PentacleDefinition definition) {
        Set<Character> symbols = new LinkedHashSet<>();
        for (String row : definition.getPattern()) {
            for (char symbol : row.toCharArray()) {
                symbols.add(symbol);
            }
        }
        List<Object> mappings = new ArrayList<>();
        for (char symbol : symbols) {
            mappings.add(symbol);
            IBlockState display = displayState(definition, symbol);
            if (symbol == '_' || symbol == ' ') {
                mappings.add(PatchouliAPI.instance.anyMatcher());
            } else {
                mappings.add(PatchouliAPI.instance.predicateMatcher(display,
                        state -> symbol == '0' ? state.getBlock() == ModBlocks.GOLDEN_SACRIFICIAL_BOWL
                                : PentacleDetector.matchesPreviewSymbol(definition, symbol, state)));
            }
        }
        // Patchouli's first horizontal index is X; detector rows are Z.
        String[] columns = new String[definition.getWidth()];
        for (int x = 0; x < columns.length; x++) {
            StringBuilder column = new StringBuilder();
            for (String row : definition.getPattern()) {
                column.append(row.charAt(x));
            }
            columns[x] = column.toString();
        }
        PatchouliAPI.instance.registerMultiblock(new ResourceLocation("occultism", definition.getId()),
                new PentacleMultiblock(definition, new String[][] {columns}, mappings.toArray())
                        .setSymmetrical(true));
    }

    private static IBlockState displayState(PentacleDefinition definition, char symbol) {
        if ("summon_wild_afrit".equals(definition.getId())) {
            if (symbol == 'C') {
                return ModBlocks.LARGE_CANDLE.getDefaultState();
            }
            if (symbol == 'P') {
                return ModBlocks.CHALK_GLYPH_PURPLE.getDefaultState();
            }
        }
        String glyphs = "WSGKBROYLECAUXMP";
        Block[] blocks = {ModBlocks.CHALK_GLYPH_WHITE, ModBlocks.CHALK_GLYPH_LIGHT_GRAY,
                ModBlocks.CHALK_GLYPH_GRAY, ModBlocks.CHALK_GLYPH_BLACK, ModBlocks.CHALK_GLYPH_BROWN,
                ModBlocks.CHALK_GLYPH_RED, ModBlocks.CHALK_GLYPH_ORANGE, ModBlocks.CHALK_GLYPH_YELLOW,
                ModBlocks.CHALK_GLYPH_LIME, ModBlocks.CHALK_GLYPH_GREEN, ModBlocks.CHALK_GLYPH_CYAN,
                ModBlocks.CHALK_GLYPH_LIGHT_BLUE, ModBlocks.CHALK_GLYPH_BLUE,
                ModBlocks.CHALK_GLYPH_PURPLE, ModBlocks.CHALK_GLYPH_MAGENTA, ModBlocks.CHALK_GLYPH_PINK};
        int index = glyphs.indexOf(symbol);
        if (index >= 0) {
            return blocks[index].getDefaultState();
        }
        switch (symbol) {
            case '0': return ModBlocks.GOLDEN_SACRIFICIAL_BOWL.getDefaultState();
            case '1': return ModBlocks.LARGE_CANDLE.getDefaultState();
            case '3': return ModBlocks.SPIRIT_ATTUNED_CRYSTAL.getDefaultState();
            case '2':
            case '4':
            case 'N':
            case 'Z': return Blocks.SKULL.getDefaultState().withProperty(BlockSkull.FACING, EnumFacing.UP);
            default: return Blocks.AIR.getDefaultState();
        }
    }
}
