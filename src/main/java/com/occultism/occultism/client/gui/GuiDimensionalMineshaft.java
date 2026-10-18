package com.occultism.occultism.client.gui;

import com.occultism.occultism.container.ContainerDimensionalMineshaft;
import com.occultism.occultism.tileentity.TileEntityDimensionalMineshaft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Dimensional mineshaft screen, laid out like upstream's {@code DimensionalMineshaftScreen}.
 *
 * <p>This used to draw a hand-made rectangle panel at 176x178. Upstream blits
 * {@code otherworld_miner.png} at its default 176x166 and draws the progress bar from the texture
 * too, so both the frame and the bar now come from that image.</p>
 */
@SideOnly(Side.CLIENT)
public class GuiDimensionalMineshaft extends GuiContainer {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("occultism", "textures/gui/otherworld_miner.png");

    private final InventoryPlayer playerInventory;
    private final TileEntityDimensionalMineshaft mineshaft;
    private final ContainerDimensionalMineshaft container;

    public GuiDimensionalMineshaft(InventoryPlayer playerInventory, TileEntityDimensionalMineshaft mineshaft) {
        super(new ContainerDimensionalMineshaft(playerInventory, mineshaft));
        this.playerInventory = playerInventory;
        this.mineshaft = mineshaft;
        this.container = (ContainerDimensionalMineshaft) this.inventorySlots;
        // Upstream relies on the AbstractContainerScreen defaults.
        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String title = this.mineshaft.getDisplayName().getUnformattedText();
        this.fontRenderer.drawString(title, 8, 6, 0xE6E6E6);
        this.fontRenderer.drawString(this.playerInventory.getDisplayName().getUnformattedText(), 8, 72, 0xD0D0D0);
        this.fontRenderer.drawString(I18n.format("gui.occultism.dimensional_mineshaft.miner"), 18, 20, 0xD0D0D0);
        this.fontRenderer.drawString(I18n.format("gui.occultism.dimensional_mineshaft.outputs"), 98, 6, 0xD0D0D0);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        this.drawDefaultBackground();
        this.mc.getTextureManager().bindTexture(TEXTURE);
        drawModalRectWithCustomSizedTexture(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize,
                this.xSize, this.ySize);

        // Upstream blits the bar from the same texture at (61, 41) with UV (176, 0) and height 4.
        int progress = this.container.getProgressScaled(18);
        if (progress > 0) {
            drawModalRectWithCustomSizedTexture(this.guiLeft + 61, this.guiTop + 41, 176, 0, progress + 1, 4,
                    256, 256);
        }
    }
}
