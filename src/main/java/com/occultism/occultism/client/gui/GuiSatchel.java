package com.occultism.occultism.client.gui;

import com.occultism.occultism.container.ContainerSatchel;
import com.occultism.occultism.container.SatchelInventory;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;

public class GuiSatchel extends GuiContainer {

    private static final int PANEL = 0xFF1F2024;
    private static final int PANEL_EDGE = 0xFF555866;
    private static final int SLOT = 0xFF2D3038;
    private static final int SLOT_EDGE = 0xFF777B8A;

    private final InventoryPlayer playerInventory;
    private final SatchelInventory satchelInventory;

    public GuiSatchel(InventoryPlayer playerInventory, SatchelInventory satchelInventory, int selectedSlot) {
        super(new ContainerSatchel(playerInventory, satchelInventory, selectedSlot));
        this.playerInventory = playerInventory;
        this.satchelInventory = satchelInventory;
        this.xSize = 322;
        this.ySize = 250;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRenderer.drawString(this.satchelInventory.getDisplayName().getUnformattedText(), 8, 6, 0xE6E6E6);
        this.fontRenderer.drawString(this.playerInventory.getDisplayName().getUnformattedText(), 77, 154, 0xD0D0D0);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        // JEI warns (and the screen looks flat) when the standard dark backdrop is missing.
        this.drawDefaultBackground();
        int x = this.guiLeft;
        int y = this.guiTop;
        drawRect(x, y, x + this.xSize, y + this.ySize, PANEL);
        drawRect(x, y, x + this.xSize, y + 1, PANEL_EDGE);
        drawRect(x, y + this.ySize - 1, x + this.xSize, y + this.ySize, PANEL_EDGE);
        drawRect(x, y, x + 1, y + this.ySize, PANEL_EDGE);
        drawRect(x + this.xSize - 1, y, x + this.xSize, y + this.ySize, PANEL_EDGE);

        for (int row = 0; row < 7; row++) {
            for (int column = 0; column < 17; column++) {
                this.drawSlotBackground(x + 8 + column * 18, y + 23 + row * 18);
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.drawSlotBackground(x + 77 + column * 18, y + 166 + row * 18);
            }
        }

        for (int column = 0; column < 9; column++) {
            this.drawSlotBackground(x + 77 + column * 18, y + 224);
        }
    }

    private void drawSlotBackground(int x, int y) {
        drawRect(x - 1, y - 1, x + 17, y + 17, SLOT_EDGE);
        drawRect(x, y, x + 16, y + 16, SLOT);
    }
}
