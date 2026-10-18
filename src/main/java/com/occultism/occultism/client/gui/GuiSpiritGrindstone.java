package com.occultism.occultism.client.gui;

import com.occultism.occultism.container.ContainerSpiritGrindstone;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class GuiSpiritGrindstone extends GuiContainer {

    private static final int PANEL = 0xFF1F2024;
    private static final int PANEL_EDGE = 0xFF555866;
    private static final int SLOT = 0xFF2D3038;
    private static final int SLOT_EDGE = 0xFF777B8A;

    private final InventoryPlayer playerInventory;

    public GuiSpiritGrindstone(InventoryPlayer playerInventory, World world, BlockPos pos) {
        super(new ContainerSpiritGrindstone(playerInventory, world, pos));
        this.playerInventory = playerInventory;
        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRenderer.drawString(I18n.format("gui.occultism.spirit_grindstone.container"), 8, 6, 0xE6E6E6);
        this.fontRenderer.drawString(this.playerInventory.getDisplayName().getUnformattedText(), 8, 72, 0xD0D0D0);
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

        this.drawSlotBackground(x + 49, y + 19);
        this.drawSlotBackground(x + 49, y + 40);
        this.drawSlotBackground(x + 129, y + 34);
        this.drawArrow(x + 80, y + 38);

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.drawSlotBackground(x + 8 + column * 18, y + 84 + row * 18);
            }
        }

        for (int column = 0; column < 9; column++) {
            this.drawSlotBackground(x + 8 + column * 18, y + 142);
        }
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
