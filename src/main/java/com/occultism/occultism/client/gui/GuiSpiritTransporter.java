package com.occultism.occultism.client.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;

import com.occultism.occultism.Tags;
import com.occultism.occultism.container.ContainerSpiritTransporter;
import com.occultism.occultism.entity.EntityFoliotJob;
import com.occultism.occultism.network.ModNetworking;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** 1.12 adaptation of the upstream transporter and cleaner filter screen. */
@SideOnly(Side.CLIENT)
public class GuiSpiritTransporter extends GuiSpirit {

    private static final int FILTER_MODE_BUTTON = 200;
    private static final net.minecraft.util.ResourceLocation TEXTURE =
            new net.minecraft.util.ResourceLocation(Tags.MOD_ID,
                    "textures/gui/inventory_spirit_transporter_tagfilter.png");
    private static final String TRANSLATION_KEY_BASE =
            "gui." + Tags.MOD_ID + ".spirit.transporter";

    private GuiTextField tagFilterTextField;
    private FilterModeButton filterModeButton;
    private String tagFilter = "";

    public GuiSpiritTransporter(InventoryPlayer playerInventory, EntityFoliotJob spirit) {
        super(new ContainerSpiritTransporter(playerInventory, spirit));
        this.xSize = 176;
        this.ySize = 220;
    }

    @Override
    public void initGui() {
        super.initGui();
        Keyboard.enableRepeatEvents(true);

        this.tagFilter = EntityFoliotJob.normalizeTagFilter(this.spirit.getTagFilter());
        this.filterModeButton = new FilterModeButton(this.guiLeft + 151, this.guiTop + 83);
        this.buttonList.add(this.filterModeButton);

        this.tagFilterTextField = new GuiTextField(0, this.fontRenderer, this.guiLeft + 8,
                this.guiTop + 123, 124, this.fontRenderer.FONT_HEIGHT);
        this.tagFilterTextField.setMaxStringLength(EntityFoliotJob.MAX_TAG_FILTER_LENGTH);
        this.tagFilterTextField.setEnableBackgroundDrawing(false);
        this.tagFilterTextField.setTextColor(0xFFFFFF);
        this.tagFilterTextField.setDisabledTextColour(0xFFFFFF);
        this.tagFilterTextField.setText(this.tagFilter);
        this.tagFilterTextField.setFocused(true);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (this.tagFilterTextField != null) {
            if (this.tagFilterTextField.getText().isEmpty() && !this.tagFilterTextField.isFocused()) {
                String hint = "forge:ores;*logs*;item:minecraft:chest";
                String clipped = this.fontRenderer.trimStringToWidth(hint, this.tagFilterTextField.width);
                this.fontRenderer.drawString(clipped, this.tagFilterTextField.x, this.tagFilterTextField.y, 0x777777);
            }
            this.tagFilterTextField.drawTextBox();
        }
        this.drawTransporterTooltips(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(TEXTURE);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        GuiInventory.drawEntityOnScreen(this.guiLeft + 35, this.guiTop + 65, 30,
                this.guiLeft + 51 - mouseX, this.guiTop + 25 - mouseY, this.spirit);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == FILTER_MODE_BUTTON) {
            boolean blacklist = !this.spirit.isFilterBlacklist();
            this.spirit.setFilterBlacklist(blacklist);
            ModNetworking.sendSpiritFilterMode(blacklist, this.spirit.getEntityId());
            return;
        }
        super.actionPerformed(button);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (this.tagFilterTextField != null) {
            if (this.tagFilterTextField.isFocused()
                    && this.tagFilterTextField.textboxKeyTyped(typedChar, keyCode)) {
                this.updateTagFilterFromField();
                return;
            }
            if (this.tagFilterTextField.isFocused()
                    && this.mc.gameSettings.keyBindInventory.isActiveAndMatches(keyCode)) {
                return;
            }
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (this.tagFilterTextField != null && this.isPointInTagFilter(mouseX, mouseY)) {
            if (mouseButton == 1) {
                this.tagFilterTextField.setText("");
                this.updateTagFilterFromField();
                return;
            }
            if (this.tagFilterTextField.mouseClicked(mouseX, mouseY, mouseButton)) {
                return;
            }
        }
        if (this.tagFilterTextField != null) {
            this.tagFilterTextField.setFocused(false);
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void updateScreen() {
        if (this.tagFilterTextField != null) {
            this.tagFilterTextField.updateCursorCounter();
        }
        super.updateScreen();
    }

    @Override
    public void onGuiClosed() {
        this.updateTagFilterFromField();
        this.spirit.setTagFilter(this.tagFilter);
        ModNetworking.sendSpiritTagFilter(this.tagFilter, this.spirit.getEntityId());
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
        if (this.tagFilterTextField != null) {
            this.tagFilterTextField.setFocused(false);
        }
    }

    private void updateTagFilterFromField() {
        if (this.tagFilterTextField != null) {
            this.tagFilter = EntityFoliotJob.normalizeTagFilter(this.tagFilterTextField.getText());
        }
    }

    private boolean isPointInTagFilter(int mouseX, int mouseY) {
        return mouseX >= this.guiLeft + 7 && mouseX < this.guiLeft + 8 + 124
                && mouseY >= this.guiTop + 121 && mouseY < this.guiTop + 123 + this.fontRenderer.FONT_HEIGHT + 4;
    }

    private void drawTransporterTooltips(int mouseX, int mouseY) {
        if (this.filterModeButton != null && this.isPointInButton(this.filterModeButton, mouseX, mouseY)) {
            List<String> lines = new ArrayList<>();
            lines.add(I18n.format(TRANSLATION_KEY_BASE + ".filter_mode"));
            lines.add(I18n.format(TRANSLATION_KEY_BASE + ".filter_mode."
                    + (this.spirit.isFilterBlacklist() ? "blacklist" : "whitelist")));
            this.drawHoveringText(lines, mouseX, mouseY);
            return;
        }
        if (this.isPointInTagFilter(mouseX, mouseY)) {
            String tooltip = I18n.format(TRANSLATION_KEY_BASE + ".tag_filter");
            List<String> lines = new ArrayList<>();
            for (String line : tooltip.split("[\\r\\n]+")) {
                lines.add(line);
            }
            this.drawHoveringText(lines, mouseX, mouseY);
        }
    }

    private boolean isPointInButton(GuiButton button, int mouseX, int mouseY) {
        return mouseX >= button.x && mouseX < button.x + button.width
                && mouseY >= button.y && mouseY < button.y + button.height;
    }

    private class FilterModeButton extends GuiButton {

        private FilterModeButton(int x, int y) {
            super(FILTER_MODE_BUTTON, x, y, 18, 18, "");
        }

        @Override
        public void drawButton(net.minecraft.client.Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
            if (!this.visible) {
                return;
            }
            this.hovered = mouseX >= this.x && mouseY >= this.y
                    && mouseX < this.x + this.width && mouseY < this.y + this.height;
            minecraft.getTextureManager().bindTexture(TEXTURE);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            int u = 177 + (this.hovered ? 20 : 0);
            int v = GuiSpiritTransporter.this.spirit.isFilterBlacklist() ? 0 : 20;
            Gui.drawScaledCustomSizeModalRect(this.x, this.y, u, v, 20, 20,
                    this.width, this.height, 256.0F, 256.0F);
        }
    }
}
