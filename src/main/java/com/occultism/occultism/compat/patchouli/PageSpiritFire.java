package com.occultism.occultism.compat.patchouli;

import com.occultism.occultism.registry.ModBlocks;
import com.occultism.occultism.ritual.SpiritFireRecipeRegistry;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import vazkii.patchouli.client.book.BookEntry;
import vazkii.patchouli.client.book.BookPage;

@SideOnly(Side.CLIENT)
public class PageSpiritFire extends BookPage {
    private String output;
    private transient SpiritFireRecipeRegistry.Recipe recipe;

    @Override
    public void build(BookEntry entry, int pageNum) {
        super.build(entry, pageNum);
        for (SpiritFireRecipeRegistry.Recipe candidate : SpiritFireRecipeRegistry.getRecipes()) {
            if (this.output.equals(String.valueOf(candidate.getOutput().getItem().getRegistryName()))) {
                this.recipe = candidate;
                entry.addRelevantStack(candidate.getOutput(), pageNum);
                break;
            }
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.parent.drawCenteredStringNoShadow(
                I18n.format("gui.occultism.dictionary.reference.spirit_fire"), 58, 0, this.book.headerColor);
        if (this.recipe == null || this.recipe.getInputs().isEmpty()) {
            this.fontRenderer.drawSplitString(I18n.format("gui.occultism.dictionary.reference.unresolved",
                    this.output), 0, 25, 116, this.book.textColor);
            return;
        }
        ItemStack input = this.recipe.getInputs().get(
                (this.parent.ticksInBook / 20) % this.recipe.getInputs().size());
        ItemStack[] stacks = {input, new ItemStack(ModBlocks.SPIRIT_FIRE), this.recipe.getOutput()};
        for (int i = 0; i < stacks.length; i++) {
            int x = 7 + i * 43;
            RenderHelper.enableGUIStandardItemLighting();
            this.mc.getRenderItem().renderItemAndEffectIntoGUI(stacks[i], x, 44);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableLighting();
            if (i < 2) {
                this.fontRenderer.drawString(">", x + 24, 48, this.book.textColor);
            }
            if (mouseX >= x && mouseX < x + 16 && mouseY >= 44 && mouseY < 60) {
                this.parent.setTooltipStack(stacks[i]);
            }
        }
        this.fontRenderer.drawSplitString(this.recipe.getOutput().getDisplayName(),
                0, 80, 116, this.book.textColor);
    }
}
