package com.occultism.occultism.client.gui;

import org.apache.commons.lang3.text.WordUtils;

import com.occultism.occultism.Tags;
import com.occultism.occultism.container.ContainerSpirit;
import com.occultism.occultism.entity.EntityFoliotJob;
import com.occultism.occultism.util.ItemNBTUtil;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.client.renderer.GlStateManager;

public class GuiSpirit extends GuiContainer {

    protected static final ResourceLocation TEXTURE =
            new ResourceLocation(Tags.MOD_ID, "textures/gui/inventory_spirit.png");
    protected static final String TRANSLATION_KEY_BASE = "gui." + Tags.MOD_ID + ".spirit";

    protected final EntityFoliotJob spirit;

    public GuiSpirit(net.minecraft.entity.player.InventoryPlayer playerInventory, EntityFoliotJob spirit) {
        this(new ContainerSpirit(playerInventory, spirit));
    }

    /** Allows specialized spirit screens to reuse the upstream spirit layout. */
    protected GuiSpirit(ContainerSpirit container) {
        super(container);
        this.spirit = container.getSpirit();
        this.xSize = 175;
        this.ySize = 165;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String name = ItemNBTUtil.formatSpiritName(this.spirit.getDisplayName().getUnformattedText());
        this.fontRenderer.drawString(name, 65, 17, 0x404040);

        int maxAge = this.spirit.getSpiritMaxAge();
        if (maxAge >= 0) {
            int agePercent = maxAge > 0
                    ? (int) Math.floor(this.spirit.getSpiritAge() / (float) maxAge * 100.0F) : 0;
            this.fontRenderer.drawString(I18n.format(TRANSLATION_KEY_BASE + ".age", agePercent),
                    65, 31, 0x404040);
        }

        String jobId = this.spirit.getJobType().getId().toString().replace(':', '.');
        String jobText = I18n.format(TRANSLATION_KEY_BASE + ".job", I18n.format("job." + jobId));
        String[] lines = WordUtils.wrap(jobText, 15, "\n", true).split("[\\r\\n]+", 2);
        for (int index = 0; index < lines.length; index++) {
            this.fontRenderer.drawString(TextFormatting.ITALIC + lines[index] + TextFormatting.RESET,
                    65, 50 + index * this.fontRenderer.FONT_HEIGHT, 0x404040);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(TEXTURE);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        GuiInventory.drawEntityOnScreen(this.guiLeft + 35, this.guiTop + 65, 30,
                this.guiLeft + 51 - mouseX, this.guiTop + 25 - mouseY, this.spirit);
    }
}
