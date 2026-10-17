package com.occultism.occultism.compat.patchouli;

import com.occultism.occultism.registry.ModBlocks;
import com.occultism.occultism.ritual.PentacleDefinition;
import com.occultism.occultism.ritual.PentacleDetector;
import com.occultism.occultism.ritual.RitualDefinition;
import com.occultism.occultism.ritual.RitualRegistry;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import vazkii.patchouli.client.book.BookEntry;
import vazkii.patchouli.client.book.BookPage;
import vazkii.patchouli.client.book.gui.GuiBook;
import vazkii.patchouli.client.book.gui.GuiBookEntry;

/** The upstream ritual diagram, translated into Patchouli's 116 by 180 page. */
@SideOnly(Side.CLIENT)
public class PageRitual extends BookPage {
    private String ritual;
    private transient RitualDefinition definition;
    private static final int[][] BOWLS = {
            {30, 40}, {60, 70}, {30, 100}, {0, 70},
            {45, 40}, {60, 50}, {15, 100}, {0, 90},
            {15, 40}, {60, 90}, {45, 100}, {0, 50}
    };

    @Override
    public void build(BookEntry entry, int pageNum) {
        super.build(entry, pageNum);
        this.definition = RitualRegistry.getById(this.ritual);
        if (this.definition != null) {
            entry.addRelevantStack(this.definition.getResult().createStack(), pageNum);
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        if (this.definition == null) {
            this.fontRenderer.drawSplitString(I18n.format("gui.occultism.dictionary.reference.unresolved",
                    this.ritual), 0, 0, GuiBook.PAGE_WIDTH, this.book.textColor);
            return;
        }
        String pentacleId = this.definition.getPentacleId().replace("occultism:", "");
        PentacleDefinition pentacle = PentacleDetector.getDefinitionById(pentacleId);
        String key = "multiblock.occultism." + pentacleId;
        String name = I18n.hasKey(key) ? I18n.format(key)
                : pentacle == null ? pentacleId : pentacle.getDisplayName();
        this.label(name, 18, 0, 98, this.book.linkColor);
        this.item(this.definition.getRitualDummy().createStack(), 0, 0, mouseX, mouseY);
        if (this.definition.requiresSacrifice()) {
            this.label(I18n.format("gui.occultism.dictionary.reference.sacrifice",
                    I18n.format(this.definition.getSacrifice().getDisplayName())), 0, 22, 116, this.book.textColor);
        }
        for (int i = 0; i < this.definition.getAdditionalIngredients().size() && i < BOWLS.length; i++) {
            int x = BOWLS[i][0] + 8;
            int y = BOWLS[i][1] + 12;
            this.item(new ItemStack(ModBlocks.SACRIFICIAL_BOWL), x, y, mouseX, mouseY);
            this.item(this.definition.getAdditionalIngredients().get(i).createDisplayStack(),
                    x, y - 5, mouseX, mouseY);
        }
        this.item(new ItemStack(ModBlocks.GOLDEN_SACRIFICIAL_BOWL), 38, 82, mouseX, mouseY);
        this.item(this.definition.getActivationIngredient().createDisplayStack(), 38, 77, mouseX, mouseY);
        this.item(new ItemStack(ModBlocks.GOLDEN_SACRIFICIAL_BOWL), 93, 122, mouseX, mouseY);
        this.item(this.definition.getResult().createStack(), 93, 117, mouseX, mouseY);
        if (this.definition.requiresItemUse()) {
            this.item(this.definition.getItemToUse().createDisplayStack(), 93, 43, mouseX, mouseY);
        }
        if (this.definition.summonsEntity()) {
            this.label(I18n.format("gui.occultism.dictionary.reference.entity",
                    this.definition.getEntityToSummon().getDisplayId()), 0, 146, 116, this.book.textColor);
        }
        if (this.definition.hasSpiritJobType()) {
            this.label(I18n.format("job.occultism."
                    + this.definition.getSpiritJobType().getId().getPath()), 0, 158, 116, this.book.textColor);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (button != 0 || this.definition == null || mouseX < 18 || mouseX > 116 || mouseY < 0 || mouseY > 14) {
            return;
        }
        net.minecraft.util.ResourceLocation id = new net.minecraft.util.ResourceLocation("occultism",
                "pentacles/" + this.definition.getPentacleId().replace("occultism:", ""));
        BookEntry target = this.book.contents.entries.get(id);
        if (target != null && !target.isLocked()) {
            this.parent.displayLexiconGui(new GuiBookEntry(this.book, target, 0), true);
        }
    }

    private void item(ItemStack stack, int x, int y, int mouseX, int mouseY) {
        if (stack.isEmpty()) {
            return;
        }
        RenderHelper.enableGUIStandardItemLighting();
        this.mc.getRenderItem().renderItemAndEffectIntoGUI(stack, x, y);
        this.mc.getRenderItem().renderItemOverlayIntoGUI(this.fontRenderer, stack, x, y, null);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
            this.parent.setTooltipStack(stack);
        }
    }

    private void label(String value, int x, int y, int width, int color) {
        float scale = Math.min(1.0F, (float) width / Math.max(1, this.fontRenderer.getStringWidth(value)));
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        GlStateManager.scale(scale, scale, 1);
        this.fontRenderer.drawString(value, 0, 0, color);
        GlStateManager.popMatrix();
    }
}
