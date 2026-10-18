package com.occultism.occultism.client.gui;

import com.occultism.occultism.container.ContainerDimensionalBattlefield;
import com.occultism.occultism.tileentity.TileEntityDimensionalBattlefield;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiDimensionalBattlefield extends GuiContainer {

    private static final int PANEL = 0xFF1F2024;
    private static final int PANEL_EDGE = 0xFF555866;
    private static final int SLOT = 0xFF2D3038;
    private static final int SLOT_EDGE = 0xFF777B8A;
    private static final int PROGRESS_BG = 0xFF343743;
    private static final int PROGRESS = 0xFFC05D5D;

    private final InventoryPlayer playerInventory;
    private final TileEntityDimensionalBattlefield battlefield;
    private final ContainerDimensionalBattlefield container;

    public GuiDimensionalBattlefield(InventoryPlayer playerInventory, TileEntityDimensionalBattlefield battlefield) {
        super(new ContainerDimensionalBattlefield(playerInventory, battlefield));
        this.playerInventory = playerInventory;
        this.battlefield = battlefield;
        this.container = (ContainerDimensionalBattlefield) this.inventorySlots;
        this.xSize = 176;
        this.ySize = 212;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String title = this.battlefield.getDisplayName().getUnformattedText();
        this.fontRenderer.drawString(title, 8, 6, 0xE6E6E6);
        this.fontRenderer.drawString(this.playerInventory.getDisplayName().getUnformattedText(), 8, 118, 0xD0D0D0);
        this.fontRenderer.drawString(I18n.format("gui.occultism.dimensional_battlefield.soul"), 19, 23, 0xD0D0D0);
        this.fontRenderer.drawString(I18n.format("gui.occultism.dimensional_battlefield.outputs"), 76, 6, 0xD0D0D0);

        int max = this.container.getMaxBattleTime();
        if (max > 0) {
            String progress = I18n.format("gui.occultism.dimensional_battlefield.progress",
                    max - this.container.getBattleTime(), max);
            this.fontRenderer.drawString(progress, 8, 91, 0xD0D0D0);
        }
        int fuelCost = this.container.getFuelCost();
        if (fuelCost > 0) {
            String fuel = I18n.format("gui.occultism.dimensional_battlefield.fuel_cost", fuelCost);
            this.fontRenderer.drawString(fuel, 8, 102, 0xD0D0D0);
        }
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

        this.drawSlotBackground(x + 26, y + 35);
        this.drawSlotBackground(x + 17, y + 61);
        this.drawSlotBackground(x + 35, y + 61);
        for (int row = 0; row < 5; row++) {
            for (int column = 0; column < 5; column++) {
                this.drawSlotBackground(x + 76 + column * 18, y + 17 + row * 18);
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.drawSlotBackground(x + 8 + column * 18, y + 130 + row * 18);
            }
        }
        for (int column = 0; column < 9; column++) {
            this.drawSlotBackground(x + 8 + column * 18, y + 188);
        }

        drawRect(x + 8, y + 84, x + 62, y + 89, PROGRESS_BG);
        int progress = this.container.getProgressScaled(54);
        if (progress > 0) {
            drawRect(x + 8, y + 84, x + 8 + progress, y + 89, PROGRESS);
        }
    }

    private void drawSlotBackground(int x, int y) {
        drawRect(x - 1, y - 1, x + 17, y + 17, SLOT_EDGE);
        drawRect(x, y, x + 16, y + 16, SLOT);
    }
}
