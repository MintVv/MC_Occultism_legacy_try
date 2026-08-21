package com.occultism.occultism.client.gui;

import com.occultism.occultism.container.ContainerDimensionalMineshaft;
import com.occultism.occultism.tileentity.TileEntityDimensionalMineshaft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiDimensionalMineshaft extends GuiContainer {

    private static final int PANEL = 0xFF1F2024;
    private static final int PANEL_EDGE = 0xFF555866;
    private static final int SLOT = 0xFF2D3038;
    private static final int SLOT_EDGE = 0xFF777B8A;
    private static final int PROGRESS_BG = 0xFF343743;
    private static final int PROGRESS = 0xFF8E78D8;

    private final InventoryPlayer playerInventory;
    private final TileEntityDimensionalMineshaft mineshaft;
    private final ContainerDimensionalMineshaft container;

    public GuiDimensionalMineshaft(InventoryPlayer playerInventory, TileEntityDimensionalMineshaft mineshaft) {
        super(new ContainerDimensionalMineshaft(playerInventory, mineshaft));
        this.playerInventory = playerInventory;
        this.mineshaft = mineshaft;
        this.container = (ContainerDimensionalMineshaft) this.inventorySlots;
        this.xSize = 176;
        this.ySize = 178;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String title = this.mineshaft.getDisplayName().getUnformattedText();
        this.fontRenderer.drawString(title, 8, 6, 0xE6E6E6);
        this.fontRenderer.drawString(this.playerInventory.getDisplayName().getUnformattedText(), 8, 84, 0xD0D0D0);
        this.fontRenderer.drawString(I18n.format("gui.occultism.dimensional_mineshaft.miner"), 18, 20, 0xD0D0D0);
        this.fontRenderer.drawString(I18n.format("gui.occultism.dimensional_mineshaft.outputs"), 98, 6, 0xD0D0D0);

        int max = this.container.getMaxMiningTime();
        if (max > 0) {
            String progress = I18n.format("gui.occultism.dimensional_mineshaft.progress",
                    max - this.container.getMiningTime(), max);
            this.fontRenderer.drawString(progress, 58, 62, 0xD0D0D0);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        int x = this.guiLeft;
        int y = this.guiTop;
        drawRect(x, y, x + this.xSize, y + this.ySize, PANEL);
        drawRect(x, y, x + this.xSize, y + 1, PANEL_EDGE);
        drawRect(x, y + this.ySize - 1, x + this.xSize, y + this.ySize, PANEL_EDGE);
        drawRect(x, y, x + 1, y + this.ySize, PANEL_EDGE);
        drawRect(x + this.xSize - 1, y, x + this.xSize, y + this.ySize, PANEL_EDGE);

        this.drawSlotBackground(x + 26, y + 34);
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                this.drawSlotBackground(x + 98 + column * 18, y + 18 + row * 18);
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.drawSlotBackground(x + 8 + column * 18, y + 96 + row * 18);
            }
        }
        for (int column = 0; column < 9; column++) {
            this.drawSlotBackground(x + 8 + column * 18, y + 154);
        }

        drawRect(x + 56, y + 47, x + 91, y + 52, PROGRESS_BG);
        int progress = this.container.getProgressScaled(35);
        if (progress > 0) {
            drawRect(x + 56, y + 47, x + 56 + progress, y + 52, PROGRESS);
        }
        this.drawArrow(x + 56, y + 42);
    }

    private void drawSlotBackground(int x, int y) {
        drawRect(x - 1, y - 1, x + 17, y + 17, SLOT_EDGE);
        drawRect(x, y, x + 16, y + 16, SLOT);
    }

    private void drawArrow(int x, int y) {
        drawRect(x, y + 1, x + 31, y + 4, 0xFF777B8A);
        drawRect(x + 27, y - 3, x + 31, y + 8, 0xFF777B8A);
        drawRect(x + 31, y - 1, x + 35, y + 6, 0xFF777B8A);
    }
}
