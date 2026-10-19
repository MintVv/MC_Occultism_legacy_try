package com.occultism.occultism.compat.jei;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.occultism.occultism.ritual.PentacleDefinition;
import com.occultism.occultism.ritual.PentacleDetector;
import com.occultism.occultism.ritual.RitualDefinition;
import com.occultism.occultism.ritual.RitualIngredient;
import com.occultism.occultism.ritual.RitualRegistry;
import com.occultism.occultism.spirit.SpiritJobType;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class RitualRecipeWrapper implements IRecipeWrapper {

    private static final int PATTERN_X = 4;
    private static final int PATTERN_Y = 19;
    private static final int PATTERN_SIZE = 16;
    private static final int INFO_CENTER_X = 94;
    private final RitualDefinition ritual;
    private final PentacleDefinition pentacle;
    private final List<ItemStack> inputs;
    private final List<ItemStack> bowlInputs;
    private final ItemStack itemToUse;
    private final ItemStack output;

    public RitualRecipeWrapper(RitualDefinition ritual) {
        this.ritual = ritual;
        this.pentacle = PentacleDetector.getDefinitionById(ritual.getPentacleId());
        this.inputs = this.createInputs(ritual);
        this.bowlInputs = this.createBowlInputs(ritual);
        this.itemToUse = ritual.requiresItemUse() ? ritual.getItemToUse().createDisplayStack() : ItemStack.EMPTY;
        // Show the real product so the ritual is findable by what it makes. Upstream does the
        // same; before this the page always displayed the ritual dummy, so searching JEI for
        // e.g. afrit essence found nothing even though the ritual produces it.
        ItemStack result = ritual.getResult().createStack();
        this.output = result.isEmpty() ? ritual.getRitualDummy().createStack() : result;
    }

    public static List<RitualRecipeWrapper> createAll() {
        List<RitualRecipeWrapper> wrappers = new ArrayList<>();
        for (RitualDefinition ritual : RitualRegistry.getDefinitions()) {
            wrappers.add(new RitualRecipeWrapper(ritual));
        }
        return wrappers;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputs(ItemStack.class, this.inputs);
        ingredients.setOutput(ItemStack.class, this.output);
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        this.drawCentered(minecraft, this.trim(minecraft, this.getRitualTitle(), 118), INFO_CENTER_X, 2,
                0x202020);
        this.drawCentered(minecraft, this.getSpiritLine(), INFO_CENTER_X, 14, 0x202020);
        this.drawCentered(minecraft, this.getJobLine(), INFO_CENTER_X, 26, 0x202020);
        if (this.hasItemToUse()) {
            this.drawCentered(minecraft, I18n.format("jei.occultism.item_to_use"), INFO_CENTER_X,
                    RitualRecipeCategory.ITEM_TO_USE_LABEL_Y, 0x202020);
        }

        RitualRecipeCategory.drawGoldenSacrificialBowl(minecraft, RitualRecipeCategory.RITUAL_CENTER_X,
                RitualRecipeCategory.RITUAL_CENTER_Y);
        for (int i = 0; i < this.bowlInputs.size(); i++) {
            int[] pos = RitualRecipeCategory.getBowlPosition(i);
            RitualRecipeCategory.drawSacrificialBowl(minecraft, pos[0], pos[1]);
        }
        RitualRecipeCategory.drawGoldenSacrificialBowl(minecraft, RitualRecipeCategory.OUTPUT_BOWL_X,
                RitualRecipeCategory.OUTPUT_BOWL_Y);
        RitualRecipeCategory.drawArrow(minecraft);
        this.drawPreviewIcon(minecraft, PATTERN_X, PATTERN_Y);
    }

    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        if (mouseX >= PATTERN_X && mouseX < PATTERN_X + PATTERN_SIZE && mouseY >= PATTERN_Y
                && mouseY < PATTERN_Y + PATTERN_SIZE && this.pentacle != null) {
            List<String> tooltip = new ArrayList<>();
            // Upstream shows the pentacle name only; the registry id is not player-facing.
            tooltip.add(I18n.format("jei.occultism.ritual.pentacle", this.pentacle.getDisplayName()));
            return tooltip;
        }
        return Collections.emptyList();
    }

    public List<ItemStack> getInputs() {
        return this.inputs;
    }

    public List<ItemStack> getBowlInputs() {
        return this.bowlInputs;
    }

    public ItemStack getItemToUse() {
        return this.itemToUse;
    }

    public boolean hasItemToUse() {
        return !this.itemToUse.isEmpty();
    }

    private List<ItemStack> createInputs(RitualDefinition ritual) {
        List<ItemStack> stacks = new ArrayList<>();
        stacks.add(ritual.getActivationIngredient().createDisplayStack());
        if (ritual.requiresItemUse()) {
            stacks.add(ritual.getItemToUse().createDisplayStack());
        }
        stacks.addAll(this.createBowlInputs(ritual));
        return stacks;
    }

    private List<ItemStack> createBowlInputs(RitualDefinition ritual) {
        List<ItemStack> stacks = new ArrayList<>();
        for (RitualIngredient ingredient : ritual.getAdditionalIngredients()) {
            stacks.add(ingredient.createDisplayStack());
        }
        return stacks;
    }

    private void drawPreviewIcon(Minecraft minecraft, int x, int y) {
        this.drawPentacle(minecraft, x, y, PATTERN_SIZE);
    }

    private void drawCentered(Minecraft minecraft, String text, int centerX, int y, int color) {
        minecraft.fontRenderer.drawString(text, centerX - minecraft.fontRenderer.getStringWidth(text) / 2, y, color);
    }

    @SuppressWarnings("unused")
    private void drawPentacle(Minecraft minecraft, int x, int y, int size) {
        Gui.drawRect(x, y, x + size, y + size, 0x22000000);
        if (this.pentacle == null) {
            minecraft.fontRenderer.drawString("?", x + size / 2 - 2, y + size / 2 - 4, 0x777777);
            return;
        }

        int width = this.pentacle.getWidth();
        int height = this.pentacle.getHeight();
        int cell = Math.max(2, Math.min(6, Math.min(size / width, size / height)));
        int startX = x + (size - width * cell) / 2;
        int startY = y + (size - height * cell) / 2;
        String[] pattern = this.pentacle.getPattern();
        for (int row = 0; row < height; row++) {
            for (int column = 0; column < width; column++) {
                int color = this.getSymbolColor(pattern[row].charAt(column));
                if (color == 0) {
                    continue;
                }
                int left = startX + column * cell;
                int top = startY + row * cell;
                Gui.drawRect(left, top, left + cell - 1, top + cell - 1, 0xFF000000 | color);
            }
        }
    }

    private int getSymbolColor(char symbol) {
        switch (symbol) {
            case '0':
                return 0xE7B735;
            case '1':
                return 0xF1DFA8;
            case '2':
                return 0xD8D8D8;
            case '3':
                return 0x5FE0FF;
            case '4':
                return 0x343434;
            case 'W':
                return 0xFFFFFF;
            case 'S':
                return 0xBEBEBE;
            case 'G':
                return 0x777777;
            case 'K':
                return 0x1C1C1C;
            case 'B':
                return 0x835432;
            case 'R':
                return 0xB02E26;
            case 'O':
                return 0xF9801D;
            case 'Y':
                return 0xFED83D;
            case 'L':
                return 0x80C71F;
            case 'E':
                return 0x5E7C16;
            case 'C':
                return 0x169C9C;
            case 'A':
                return 0x3AB3DA;
            case 'U':
                return 0x3C44AA;
            case 'X':
                return 0x8932B8;
            case 'M':
                return 0xC74EBD;
            case 'P':
                return 0xF38BAA;
            default:
                return 0;
        }
    }

    private String trim(Minecraft minecraft, String text, int width) {
        return minecraft.fontRenderer.trimStringToWidth(text, width);
    }

    private String getSpiritLine() {
        if (this.ritual.summonsEntity() && !this.ritual.hasSpiritJobType()) {
            return this.getEntitySummonLine();
        }

        Item item = this.ritual.getActivationIngredient().createDisplayStack().getItem();
        ResourceLocation id = item.getRegistryName();
        String name = id == null ? "" : id.getPath();
        if (name.contains("foliot")) {
            return I18n.format("jei.occultism.ritual.summon_foliot");
        }
        if (name.contains("djinni")) {
            return I18n.format("jei.occultism.ritual.summon_djinni");
        }
        if (name.contains("afrit")) {
            return I18n.format("jei.occultism.ritual.summon_afrit");
        }
        if (name.contains("marid")) {
            return I18n.format("jei.occultism.ritual.summon_marid");
        }
        return I18n.format("jei.occultism.ritual.generic");
    }

    private String getEntitySummonLine() {
        String entityId = this.ritual.getEntityToSummon().getDisplayId();
        int summonNumber = this.ritual.getEntityToSummon().getSummonNumber();
        if (summonNumber > 1) {
            return I18n.format("jei.occultism.ritual.summon_entities", summonNumber, entityId);
        }
        return I18n.format("jei.occultism.ritual.summon_entity", entityId);
    }

    /**
     * The localized ritual name when the definition carries a translation key, otherwise the raw English name.
     */
    private String getRitualTitle() {
        return this.ritual.hasDisplayNameKey()
                ? I18n.format(this.ritual.getDisplayNameKey())
                : this.ritual.getDisplayName();
    }

    private String getJobLine() {
        if (this.ritual.hasSpiritJobType()) {
            return this.getSpiritJobLine(this.ritual.getSpiritJobType());
        }

        // Deliberately the raw English display_name: this heuristic matches English keywords, not the localized name.
        String name = this.ritual.getDisplayName().toLowerCase(Locale.ROOT);
        if (name.contains("crusher")) {
            return I18n.format("jei.occultism.ritual.job.crusher");
        }
        if (name.contains("smelter")) {
            return I18n.format("jei.occultism.ritual.job.smelter");
        }
        if (name.contains("crystallizer")) {
            return I18n.format("jei.occultism.ritual.job.crystallizer");
        }
        if (name.contains("cleaner") || name.contains("janitor")) {
            return I18n.format("jei.occultism.ritual.job.cleaner");
        }
        if (name.contains("transporter")) {
            return I18n.format("jei.occultism.ritual.job.transporter");
        }
        if (name.contains("farmer")) {
            return I18n.format("jei.occultism.ritual.job.farmer");
        }
        if (name.contains("lumberjack")) {
            return I18n.format("jei.occultism.ritual.job.lumberjack");
        }
        if (name.contains("trader")) {
            return I18n.format("jei.occultism.ritual.job.trader");
        }
        if (this.ritual.requiresSacrifice()) {
            return I18n.format("jei.occultism.ritual.sacrifice", I18n.format(this.ritual.getSacrifice().getDisplayName()));
        }
        return I18n.format("jei.occultism.ritual.duration_line",
                Math.max(1, this.ritual.getDurationTicks() / 20));
    }

    private String getSpiritJobLine(SpiritJobType jobType) {
        switch (jobType) {
            case CRUSHER:
                return I18n.format("jei.occultism.ritual.job.crusher");
            case CRYSTALLIZER:
                return I18n.format("jei.occultism.ritual.job.crystallizer");
            case SMELTER:
                return I18n.format("jei.occultism.ritual.job.smelter");
            case CLEANER:
                return I18n.format("jei.occultism.ritual.job.cleaner");
            case FARMER:
                return I18n.format("jei.occultism.ritual.job.farmer");
            case TRANSPORT_ITEMS:
                return I18n.format("jei.occultism.ritual.job.transporter");
            case LUMBERJACK:
                return I18n.format("jei.occultism.ritual.job.lumberjack");
            case TRADER_OTHERSTONE:
            case TRADER_OTHERROCK:
            case TRADER_SAPLING:
                return I18n.format("jei.occultism.ritual.job.trader");
            case CLEAR_WEATHER:
                return I18n.format("jei.occultism.ritual.job.clear_weather");
            case RAIN_WEATHER:
                return I18n.format("jei.occultism.ritual.job.rain_weather");
            case THUNDER_WEATHER:
                return I18n.format("jei.occultism.ritual.job.thunder_weather");
            case DAY_TIME:
                return I18n.format("jei.occultism.ritual.job.day_time");
            case NIGHT_TIME:
                return I18n.format("jei.occultism.ritual.job.night_time");
            default:
                return I18n.format("jei.occultism.ritual.duration_line",
                        Math.max(1, this.ritual.getDurationTicks() / 20));
        }
    }
}
