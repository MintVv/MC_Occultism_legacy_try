package com.occultism.occultism.compat.patchouli;

import java.util.List;

import com.occultism.occultism.spirit.SpiritTradeRecipeRegistry;
import com.occultism.occultism.spirit.SpiritTradeIngredient;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import vazkii.patchouli.client.book.BookEntry;
import vazkii.patchouli.client.book.BookPage;

@SideOnly(Side.CLIENT)
public class PageSpiritTrade extends BookPage {
    private String recipe;
    private static final ResourceLocation CRAFTING_TEXTURE =
            new ResourceLocation("occultism", "textures/gui/book/crafting_textures.png");

    @Override
    public void build(BookEntry entry, int pageNum) {
        super.build(entry, pageNum);
        SpiritTradeRecipeRegistry.Recipe trade = SpiritTradeRecipeRegistry.find(this.recipe);
        if (trade != null) {
            entry.addRelevantStack(trade.getOutput(), pageNum);
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.parent.drawCenteredStringNoShadow(I18n.format("gui.occultism.dictionary.reference.spirit_trade"),
                58, 0, this.book.headerColor);
        SpiritTradeRecipeRegistry.Recipe trade = SpiritTradeRecipeRegistry.find(this.recipe);
        if (trade == null) {
            this.fontRenderer.drawSplitString(I18n.format("gui.occultism.dictionary.reference.unresolved",
                    this.recipe), 0, 25, 116, this.book.textColor);
            return;
        }
        // BookSpiritTradeRecipePageRenderer: first box, throw arrow, output arrow and box.
        this.mc.getTextureManager().bindTexture(CRAFTING_TEXTURE);
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableBlend();
        List<SpiritTradeIngredient> costs = trade.getIngredients();
        int spacing = costs.size() == 1 ? 36 : 18;
        int rows = (costs.size() + 2) / 3;
        int outputY = 40 + (rows - 1) * 9;
        for (int index = 0; index < costs.size(); index++) {
            int size = costs.size() == 1 ? 24 : 18;
            Gui.drawScaledCustomSizeModalRect(8 + index % 3 * spacing, 40 + index / 3 * spacing,
                    11, 71, 24, 24, size, size, 128, 256);
        }
        int arrowX = costs.size() == 1 ? 44 : 64;
        Gui.drawModalRectWithCustomSizedTexture(arrowX, outputY + 7, 0, 246, 18, 10, 128, 256);
        if (costs.size() == 1) {
            Gui.drawModalRectWithCustomSizedTexture(69, outputY, 72, 71, 36, 24, 128, 256);
        } else {
            Gui.drawModalRectWithCustomSizedTexture(81, outputY, 11, 71, 24, 24, 128, 256);
        }
        for (int index = 0; index < costs.size(); index++) {
            List<ItemStack> inputs = costs.get(index).getDisplayInputs();
            int padding = costs.size() == 1 ? 4 : 1;
            int x = 8 + index % 3 * spacing + padding;
            int y = 40 + index / 3 * spacing + padding;
            if (inputs.isEmpty()) {
                this.fontRenderer.drawString("?", x + 4, y + 4, this.book.textColor);
            } else {
                this.parent.renderItemStack(x, y, mouseX, mouseY,
                        inputs.get((this.parent.ticksInBook / 20) % inputs.size()));
            }
        }
        this.parent.renderItemStack(costs.size() == 1 ? 84 : 85, outputY + 4, mouseX, mouseY, trade.getOutput());
        this.fontRenderer.drawSplitString(I18n.format("gui.occultism.dictionary.reference.trade_timing",
                trade.getConversionSteps() * 20, trade.getMaxTradesPerRound()), 0, costs.size() == 1 ? 80 : 106, 116,
                this.book.textColor);
    }
}
