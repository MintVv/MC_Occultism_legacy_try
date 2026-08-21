package com.occultism.occultism.ritual;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.occultism.occultism.block.BlockChalkGlyph;
import com.occultism.occultism.registry.ModBlocks;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class PentacleDetector {

    private static final int SEARCH_RADIUS = 10;

    private static final List<PentacleDefinition> DEFINITIONS = Collections.unmodifiableList(Arrays.asList(
            new PentacleDefinition("summon_foliot", "Aviar's Circle",
                    "_________",
                    "___WWW___",
                    "__W_W_W__",
                    "_W_1W1_W_",
                    "_WWW0WWW_",
                    "_W_1W1_W_",
                    "__W_W_W__",
                    "___WWW___",
                    "_________"),
            new PentacleDefinition("possess_foliot", "Hedyrin's Lure",
                    "_________",
                    "____W____",
                    "___W1W___",
                    "__W_Y_W__",
                    "_W1Y0Y1W_",
                    "__W_Y_W__",
                    "___W1W___",
                    "____W____",
                    "_________"),
            new PentacleDefinition("craft_foliot", "Eziveus' Spectral Compulsion",
                    "_________",
                    "__WXXXW__",
                    "_WW1_1WW_",
                    "_X1W_W1X_",
                    "_X__0__X_",
                    "_X1W_W1X_",
                    "_WW1_1WW_",
                    "__WXXXW__",
                    "_________"),
            new PentacleDefinition("summon_djinni", "Ophyx' Calling",
                    "_____________",
                    "____LLLLL____",
                    "___L_L1L_L___",
                    "__L2_WWW_2L__",
                    "_L__W_S_W__L_",
                    "_LLW_1S1_WLL_",
                    "_L1WSS0SSW1L_",
                    "_LLW_1S1_WLL_",
                    "_L__W_S_W__L_",
                    "__L2_WWW_2L__",
                    "___L_L1L_L___",
                    "____LLLLL____",
                    "_____________"),
            new PentacleDefinition("possess_djinni", "Ihagan's Enthrallment",
                    "_____________",
                    "______L______",
                    "_____L2L_____",
                    "____Y_W_Y____",
                    "___Y1S1S1Y___",
                    "__L_S_Y_S_L__",
                    "_L2W1Y0Y1W2L_",
                    "__L_S_Y_S_L__",
                    "___Y1S1S1Y___",
                    "____Y_W_Y____",
                    "_____L2L_____",
                    "______L______",
                    "_____________"),
            new PentacleDefinition("craft_djinni", "Strigeor's Higher Binding",
                    "_____________",
                    "__XLXLXLXLX__",
                    "_XL_2___2_LX_",
                    "_L__WXXXW__L_",
                    "_X2WS1_1SW2X_",
                    "_L_X1S_S1X_L_",
                    "_X_X__0__X_X_",
                    "_L_X1S_S1X_L_",
                    "_X2WS1_1SW2X_",
                    "_L__WXXXW__L_",
                    "_XL_2___2_LX_",
                    "__XLXLXLXLX__",
                    "_____________"),
            new PentacleDefinition("summon_unbound_afrit", "Kandar's Opened Conjure",
                    "_________________",
                    "______OOOOO______",
                    "____OO__2__OO____",
                    "___O__LLLLL__O___",
                    "__O__L_L1L_L__O__",
                    "__O_L2_WWW_2L_O__",
                    "_O_L__W_G_W__L_O_",
                    "_O_LLW_1G1_WLL_O_",
                    "_O2L1WGG0GGW1L2O_",
                    "_O_LLW_1G1_WLL_O_",
                    "_O_L__W_G_W__L_O_",
                    "__O_L2_WWW_2L_O__",
                    "__O__L_L1L_L__O__",
                    "___O__LLLLL__O___",
                    "____OO__2__OO____",
                    "______OOOOO______",
                    "_________________"),
            new PentacleDefinition("summon_afrit", "Abras' Conjure",
                    "_________________",
                    "______OOOOO______",
                    "____OOR_2_ROO____",
                    "___OR_LLLLL_RO___",
                    "__OR3L_L1L_L3RO__",
                    "__O_L2_WWW_2L_O__",
                    "_ORL__W_G_W__LRO_",
                    "_O_LLW_1G1_WLL_O_",
                    "_O2L1WGG0GGW1L2O_",
                    "_O_LLW_1G1_WLL_O_",
                    "_ORL__W_G_W__LRO_",
                    "__O_L2_WWW_2L_O__",
                    "__OR3L_L1L_L3RO__",
                    "___OR_LLLLL_RO___",
                    "____OOR_2_ROO____",
                    "______OOOOO______",
                    "_________________"),
            new PentacleDefinition("possess_unbound_afrit", "Odus' Open Convocation",
                    "_________________",
                    "________Y________",
                    "_______Y3Y_______",
                    "________L________",
                    "_____O_L2L_O_____",
                    "____O2Y_W_Y2O____",
                    "_____Y1G1G1Y_____",
                    "__Y_L_G_Y_G_L_Y__",
                    "_Y3L2W1Y0Y1W2L3Y_",
                    "__Y_L_G_Y_G_L_Y__",
                    "_____Y1G1G1Y_____",
                    "____O2Y_W_Y2O____",
                    "_____O_L2L_O_____",
                    "________L________",
                    "_______Y3Y_______",
                    "________Y________",
                    "_________________"),
            new PentacleDefinition("possess_afrit", "Posuc's Convocation",
                    "_________________",
                    "________Y________",
                    "_______Y3Y_______",
                    "______R_L_R______",
                    "_____O_L2L_O_____",
                    "____O2Y_W_Y2O____",
                    "___R_Y1G1G1Y_R___",
                    "__Y_L_G_Y_G_L_Y__",
                    "_Y3L2W1Y0Y1W2L3Y_",
                    "__Y_L_G_Y_G_L_Y__",
                    "___R_Y1G1G1Y_R___",
                    "____O2Y_W_Y2O____",
                    "_____O_L2L_O_____",
                    "______R_L_R______",
                    "_______Y3Y_______",
                    "________Y________",
                    "_________________"),
            new PentacleDefinition("craft_afrit", "Sevira's Permanent Confinement",
                    "_________________",
                    "__RXOXOXRXOXOXR__",
                    "_RX____3_3____XR_",
                    "_X__XLXLXLXLX__X_",
                    "_O_XL_2___2_LX_O_",
                    "_X_L__WXXXW__L_X_",
                    "_O_X2WG1_1GW2X_O_",
                    "_X3L_X1G_G1X_L3X_",
                    "_R_X_X__0__X_X_R_",
                    "_X3L_X1G_G1X_L3X_",
                    "_O_X2WG1_1GW2X_O_",
                    "_X_L__WXXXW__L_X_",
                    "_O_XL_2___2_LX_O_",
                    "_X__XLXLXLXLX__X_",
                    "_RX____3_3____XR_",
                    "__RXOXOXRXOXOXR__",
                    "_________________"),
            new PentacleDefinition("summon_unbound_marid", "Tibira's Attraction",
                    "_________________",
                    "______OOOOO______",
                    "__4_OOR_2_ROO_4__",
                    "___OR_LLLLL_RO___",
                    "__OR3L_L1L_L3RO__",
                    "__O_L2_WWW_2L_O__",
                    "_ORL__W_K_W__LRO_",
                    "_O_LLW_1K1_WLL_O_",
                    "_O2L1WKK0KKW1L2O_",
                    "_O_LLW_1K1_WLL_O_",
                    "_ORL__W_K_W__LRO_",
                    "__O_L2_WWW_2L_O__",
                    "__OR3L_L1L_L3RO__",
                    "___OR_LLLLL_RO___",
                    "__4_OOR_2_ROO_4__",
                    "______OOOOO______",
                    "_________________"),
            new PentacleDefinition("summon_marid", "Fatma's Incentivized Attraction",
                    "_____________________",
                    "_______UUUUUUU_______",
                    "_____UU__U3U__UU_____",
                    "____U___OOOOO___U____",
                    "___U4_OOR_2_ROO_4U___",
                    "__U__OR_LLLLL_RO__U__",
                    "__U_OR3L_L1L_L3RO_U__",
                    "_U__O_L2_WWW_2L_O__U_",
                    "_U_ORL__W_K_W__LRO_U_",
                    "_UUO_LLW_1K1_WLL_OUU_",
                    "_U3O2L1WKK0KKW1L2O3U_",
                    "_UUO_LLW_1K1_WLL_OUU_",
                    "_U_ORL__W_K_W__LRO_U_",
                    "_U__O_L2_WWW_2L_O__U_",
                    "__U_OR3L_L1L_L3RO_U__",
                    "__U__OR_LLLLL_RO__U__",
                    "___U4_OOR_2_ROO_4U___",
                    "____U___OOOOO___U____",
                    "_____UU__U3U__UU_____",
                    "_______UUUUUUU_______",
                    "_____________________"),
            new PentacleDefinition("possess_marid", "Xeovrenth Adjure",
                    "_____________________",
                    "__________U__________",
                    "_________Y4Y_________",
                    "________U_Y_U________",
                    "_______Y_Y3Y_Y_______",
                    "______U_R_L_R_U______",
                    "_____U3O_L2L_O3U_____",
                    "____Y_O2Y_W_Y2O_Y____",
                    "___U_R_Y1K1K1Y_R_U___",
                    "__Y_Y_L_K_Y_K_L_Y_Y__",
                    "_U4Y3L2W1Y0Y1W2L3Y4U_",
                    "__Y_Y_L_K_Y_K_L_Y_Y__",
                    "___U_R_Y1K1K1Y_R_U___",
                    "____Y_O2Y_W_Y2O_Y____",
                    "_____U3O_L2L_O3U_____",
                    "______U_R_L_R_U______",
                    "_______Y_Y3Y_Y_______",
                    "________U_Y_U________",
                    "_________Y4Y_________",
                    "__________U__________",
                    "_____________________"),
            new PentacleDefinition("craft_marid", "Uphyxes Inverted Tower",
                    "_____________________",
                    "__XXUUUXUUUUUXUUUXX__",
                    "_XX_______4_______XX_",
                    "_X__RXOXOXRXOXOXR__X_",
                    "_U_RX____3_3____XR_U_",
                    "_U_X__XLXLXLXLX__X_U_",
                    "_U_O_XL_2___2_LX_O_U_",
                    "_X_X_L__WXXXW__L_X_X_",
                    "_U_O_X2WK1_1KW2X_O_U_",
                    "_U_X3L_X1K_K1X_L3X_U_",
                    "_U4R_X_X__0__X_X_R4U_",
                    "_U_X3L_X1K_K1X_L3X_U_",
                    "_U_O_X2WK1_1KW2X_O_U_",
                    "_X_X_L__WXXXW__L_X_X_",
                    "_U_O_XL_2___2_LX_O_U_",
                    "_U_X__XLXLXLXLX__X_U_",
                    "_U_RX____3_3____XR_U_",
                    "_X__RXOXOXRXOXOXR__X_",
                    "_XX_______4_______XX_",
                    "__XXUUUXUUUUUXUUUXX__",
                    "_____________________"),
            new PentacleDefinition("resurrect_spirit", "Susje's Simple Circle",
                    "___________",
                    "_____W_____",
                    "__W_____W__",
                    "___________",
                    "____WWW____",
                    "_W__W0W__W_",
                    "____WWW____",
                    "___________",
                    "__W_____W__",
                    "_____W_____",
                    "___________"),
            new PentacleDefinition("summon_wild_afrit", "Abras' Open Conjure",
                    "___________",
                    "____PPP____",
                    "___WCWCW___",
                    "__W_WNW_W__",
                    "_PCWP_PWCP_",
                    "_PWZ_0_ZWP_",
                    "_PCWP_PWCP_",
                    "__W_WNW_W__",
                    "___WCWCW___",
                    "____PPP____",
                    "___________"),
            new PentacleDefinition("summon_wild_greater_spirit", "Osorin's Unbound Calling",
                    "___________",
                    "____XXX____",
                    "___W_W_W___",
                    "__W_W2W_W__",
                    "_X_WX_XW_X_",
                    "_XW2_0_2WX_",
                    "_X_WX_XW_X_",
                    "__W_W2W_W__",
                    "___W_W_W___",
                    "____XXX____",
                    "___________"),
            new PentacleDefinition("contact_wild_spirit", "Osorin's Unbound Calling",
                    "_____________",
                    "______P______",
                    "__A_AE_EA_A__",
                    "____PE_EP____",
                    "__APA_P_APA__",
                    "__EE_____EE__",
                    "_P__P_0_P__P_",
                    "__EE_____EE__",
                    "__APA_P_APA__",
                    "____PE_EP____",
                    "__A_AE_EA_A__",
                    "______P______",
                    "_____________"),
            new PentacleDefinition("contact_eldritch_spirit", "Ronaza's Contact",
                    "_____________________",
                    "__________C__________",
                    "_________C_C_________",
                    "_________CMC_________",
                    "____BB__CMMMC__BB____",
                    "____B_BBCMPMCBB_B____",
                    "_____BABAE_EABAB_____",
                    "_____BB_PE_EP_BB_____",
                    "____CCAPA_P_APACC____",
                    "__CCMMEE_____EEMMCC__",
                    "_C_MMP__P_0_P__PMM_C_",
                    "__CCMMEE_____EEMMCC__",
                    "____CCAPA_P_APACC____",
                    "_____BB_PE_EP_BB_____",
                    "_____BABAE_EABAB_____",
                    "____B_BBCMPMCBB_B____",
                    "____BB__CMMMC__BB____",
                    "_________CMC_________",
                    "_________C_C_________",
                    "__________C__________",
                    "_____________________")));

    private PentacleDetector() {
    }

    public static List<PentacleDefinition> getDefinitions() {
        return DEFINITIONS;
    }

    public static PentacleDefinition getDefinitionById(String id) {
        for (PentacleDefinition definition : DEFINITIONS) {
            if (definition.getId().equals(id)) {
                return definition;
            }
        }
        return null;
    }

    public static List<PentacleMatch> findMatchesAt(World world, BlockPos center) {
        List<PentacleMatch> matches = new ArrayList<>();
        for (PentacleDefinition definition : DEFINITIONS) {
            int requiredGlyphs = countRequiredGlyphs(definition);
            if (requiredGlyphs > 0 && matches(world, center, definition)) {
                matches.add(new PentacleMatch(definition, center, requiredGlyphs));
            }
        }
        matches.sort(Comparator.comparingInt(PentacleMatch::getRequiredGlyphs).reversed());
        return matches;
    }

    public static List<PentacleSymbol> getRequiredSymbols(PentacleDefinition definition, BlockPos center) {
        List<PentacleSymbol> symbols = new ArrayList<>();
        String[] pattern = definition.getPattern();
        for (int z = 0; z < definition.getHeight(); z++) {
            String row = pattern[z];
            for (int x = 0; x < definition.getWidth(); x++) {
                char symbol = row.charAt(x);
                if (!isRequiredSymbol(symbol)) {
                    continue;
                }
                BlockPos pos = center.add(x - definition.getCenterX(), 0, z - definition.getCenterZ());
                symbols.add(new PentacleSymbol(symbol, pos));
            }
        }
        return symbols;
    }

    public static boolean matchesRequiredSymbol(World world, BlockPos pos, char symbol, IBlockState state) {
        return isRequiredSymbol(symbol) && symbolMatches(world, pos, symbol, state);
    }

    public static List<PentacleMatch> findMatches(World world, BlockPos near) {
        List<PentacleMatch> matches = new ArrayList<>();
        for (PentacleDefinition definition : DEFINITIONS) {
            for (int x = -SEARCH_RADIUS; x <= SEARCH_RADIUS; x++) {
                for (int z = -SEARCH_RADIUS; z <= SEARCH_RADIUS; z++) {
                    BlockPos center = near.add(x, 0, z);
                    int requiredGlyphs = countRequiredGlyphs(definition);
                    if (requiredGlyphs > 0 && matches(world, center, definition)) {
                        matches.add(new PentacleMatch(definition, center, requiredGlyphs));
                    }
                }
            }
        }
        matches.sort(Comparator.comparingInt(PentacleMatch::getRequiredGlyphs).reversed());
        return matches;
    }

    private static boolean matches(World world, BlockPos center, PentacleDefinition definition) {
        String[] pattern = definition.getPattern();
        for (int z = 0; z < definition.getHeight(); z++) {
            String row = pattern[z];
            for (int x = 0; x < definition.getWidth(); x++) {
                char symbol = row.charAt(x);
                if (!isRequiredSymbol(symbol)) {
                    continue;
                }
                BlockPos checkPos = center.add(x - definition.getCenterX(), 0, z - definition.getCenterZ());
                IBlockState state = world.getBlockState(checkPos);
                if (!symbolMatches(world, checkPos, symbol, state, definition)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static int countRequiredGlyphs(PentacleDefinition definition) {
        int count = 0;
        for (String row : definition.getPattern()) {
            for (int i = 0; i < row.length(); i++) {
                if (isRequiredSymbol(row.charAt(i))) {
                    count++;
                }
            }
        }
        return count;
    }

    private static boolean isRequiredSymbol(char symbol) {
        return isGlyphSymbol(symbol) || isParaphernaliaSymbol(symbol);
    }

    private static boolean isGlyphSymbol(char symbol) {
        return "WSGKBROYLECAUXMP".indexOf(symbol) >= 0;
    }

    private static boolean isParaphernaliaSymbol(char symbol) {
        return "1234NZ".indexOf(symbol) >= 0;
    }

    private static boolean symbolMatches(World world, BlockPos pos, char symbol, IBlockState state) {
        return symbolMatches(world, pos, symbol, state, null);
    }

    private static boolean symbolMatches(World world, BlockPos pos, char symbol, IBlockState state,
            PentacleDefinition definition) {
        // The upstream wild-Afrit pattern uses C for candles, while the older
        // 1.12 detector uses C for cyan chalk in contact_eldritch_spirit.
        if (definition != null && "summon_wild_afrit".equals(definition.getId())) {
            if (symbol == 'C') {
                return isCandleBlock(state.getBlock());
            }
            // Upstream P means purple chalk; P is pink in several legacy 1.12
            // patterns, whose purple glyph is represented by X instead.
            if (symbol == 'P') {
                return glyphMatches('X', state.getBlock());
            }
        }
        if (isGlyphSymbol(symbol)) {
            return glyphMatches(symbol, state.getBlock());
        }
        return paraphernaliaMatches(world, pos, symbol, state);
    }

    private static boolean paraphernaliaMatches(World world, BlockPos pos, char symbol, IBlockState state) {
        switch (symbol) {
            case '1':
                return isCandleBlock(state.getBlock());
            case '2':
                return isSkullBlock(world, pos, 0);
            case '3':
                return state.getBlock() == ModBlocks.SPIRIT_ATTUNED_CRYSTAL;
            case '4':
                return isSkullBlock(world, pos, 1);
            case 'N':
                return isSkullBlock(world, pos, 1);
            case 'Z':
                return isSkullBlock(world, pos, 0);
            default:
                return false;
        }
    }

    private static boolean isCandleBlock(Block block) {
        if (block == ModBlocks.LARGE_CANDLE) {
            return true;
        }
        ResourceLocation registryName = block.getRegistryName();
        return registryName != null && registryName.getPath().contains("candle");
    }

    private static boolean isSkullBlock(World world, BlockPos pos, int skullType) {
        if (world.getBlockState(pos).getBlock() != Blocks.SKULL) {
            return false;
        }
        TileEntity tileEntity = world.getTileEntity(pos);
        return tileEntity instanceof TileEntitySkull
                ? ((TileEntitySkull) tileEntity).getSkullType() == skullType
                : skullType == 0;
    }

    private static boolean glyphMatches(char symbol, Block block) {
        if (block == ModBlocks.CHALK_GLYPH_RAINBOW || block == ModBlocks.CHALK_GLYPH_VOID) {
            return true;
        }
        switch (symbol) {
            case 'W':
                return block == ModBlocks.CHALK_GLYPH_WHITE || block == ModBlocks.CHALK_GLYPH_LIGHT_GRAY
                        || block == ModBlocks.CHALK_GLYPH_GRAY || block == ModBlocks.CHALK_GLYPH_BLACK
                        || block == ModBlocks.CHALK_GLYPH_VOID;
            case 'S':
                return block == ModBlocks.CHALK_GLYPH_LIGHT_GRAY || block == ModBlocks.CHALK_GLYPH_GRAY
                        || block == ModBlocks.CHALK_GLYPH_BLACK || block == ModBlocks.CHALK_GLYPH_VOID;
            case 'G':
                return block == ModBlocks.CHALK_GLYPH_GRAY || block == ModBlocks.CHALK_GLYPH_BLACK
                        || block == ModBlocks.CHALK_GLYPH_VOID;
            case 'K':
                return block == ModBlocks.CHALK_GLYPH_BLACK || block == ModBlocks.CHALK_GLYPH_VOID;
            case 'B':
                return block == ModBlocks.CHALK_GLYPH_BROWN;
            case 'R':
                return block == ModBlocks.CHALK_GLYPH_RED;
            case 'O':
                return block == ModBlocks.CHALK_GLYPH_ORANGE;
            case 'Y':
                return block == ModBlocks.CHALK_GLYPH_YELLOW;
            case 'L':
                return block == ModBlocks.CHALK_GLYPH_LIME;
            case 'E':
                return block == ModBlocks.CHALK_GLYPH_GREEN;
            case 'C':
                return block == ModBlocks.CHALK_GLYPH_CYAN;
            case 'A':
                return block == ModBlocks.CHALK_GLYPH_LIGHT_BLUE;
            case 'U':
                return block == ModBlocks.CHALK_GLYPH_BLUE;
            case 'X':
                return block == ModBlocks.CHALK_GLYPH_PURPLE;
            case 'M':
                return block == ModBlocks.CHALK_GLYPH_MAGENTA;
            case 'P':
                return block == ModBlocks.CHALK_GLYPH_PINK;
            default:
                return block instanceof BlockChalkGlyph;
        }
    }

    public static final class PentacleSymbol {

        private final char symbol;
        private final BlockPos pos;

        private PentacleSymbol(char symbol, BlockPos pos) {
            this.symbol = symbol;
            this.pos = pos;
        }

        public char getSymbol() {
            return this.symbol;
        }

        public BlockPos getPos() {
            return this.pos;
        }

        public boolean isGlyph() {
            return isGlyphSymbol(this.symbol);
        }
    }
}
