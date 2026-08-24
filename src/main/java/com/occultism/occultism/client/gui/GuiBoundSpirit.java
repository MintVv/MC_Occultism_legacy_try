package com.occultism.occultism.client.gui;

import com.occultism.occultism.Tags;
import com.occultism.occultism.container.ContainerBoundSpirit;
import com.occultism.occultism.entity.EntityBoundSpirit;
import com.occultism.occultism.util.ItemNBTUtil;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Compact inventory screen for the upstream-style non-job spirit inventory. */
@SideOnly(Side.CLIENT)
public class GuiBoundSpirit extends GuiContainer {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Tags.MOD_ID, "textures/gui/inventory_spirit.png");
    private final EntityBoundSpirit spirit;

    public GuiBoundSpirit(InventoryPlayer playerInventory, EntityBoundSpirit spirit) {
        super(new ContainerBoundSpirit(playerInventory, spirit));
        this.spirit = spirit;
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
        String tier = I18n.format("spirit_tier.occultism." + this.spirit.getTier().getId().getPath());
        this.fontRenderer.drawString(TextFormatting.ITALIC + I18n.format("gui.occultism.bound_spirit.tier", tier)
                + TextFormatting.RESET, 65, 31, 0x404040);
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
