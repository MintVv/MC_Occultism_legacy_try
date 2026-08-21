package com.occultism.occultism.client.gui;

import com.occultism.occultism.container.ContainerEnderSatchel;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;

public class GuiEnderSatchel extends GuiContainer {

    private static final int PANEL = 0xFF1F2024;
    private static final int PANEL_EDGE = 0xFF555866;
    private static final int SLOT = 0xFF2D3038;
    private static final int SLOT_EDGE = 0xFF777B8A;

    private final InventoryPlayer playerInventory;
    private final IInventory enderInventory;

    public GuiEnderSatchel(InventoryPlayer playerInventory, IInventory enderInventory, int selectedSlot) {
        super(new ContainerEnderSatchel(playerInventory, enderInventory, playerInventory.player, selectedSlot));
        this.playerInventory = playerInventory;
        this.enderInventory = enderInventory;
        this.xSize = 176;
        this.ySize = 186;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRenderer.drawString(this.enderInventory.getDisplayName().getUnformattedText(), 8, 6, 0xE6E6E6);
        this.fontRenderer.drawString(this.playerInventory.getDisplayName().getUnformattedText(), 8, 91, 0xD0D0D0);
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

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.drawSlotBackground(x + 8 + column * 18, y + 18 + row * 18);
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.drawSlotBackground(x + 8 + column * 18, y + 103 + row * 18);
            }
        }

        for (int column = 0; column < 9; column++) {
            this.drawSlotBackground(x + 8 + column * 18, y + 161);
        }
    }

    private void drawSlotBackground(int x, int y) {
        drawRect(x - 1, y - 1, x + 17, y + 17, SLOT_EDGE);
        drawRect(x, y, x + 16, y + 16, SLOT);
    }
}
