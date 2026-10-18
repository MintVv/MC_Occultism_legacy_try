package com.occultism.occultism.client.gui;

import com.occultism.occultism.container.ContainerSatchel;
import com.occultism.occultism.container.SatchelInventory;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

/**
 * Satchel screen, laid out like upstream's {@code SatchelScreen} and drawn with its texture.
 *
 * <p>This used to be a hand-drawn rectangle panel sized 322x250 around a 17x7 grid, which is a
 * different inventory from upstream's 13x9 and did not look like a container at all. The size,
 * the texture and the slot origins now all come from upstream.</p>
 */
public class GuiSatchel extends GuiContainer {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("occultism", "textures/gui/inventory_satchel.png");

    private final InventoryPlayer playerInventory;
    private final SatchelInventory satchelInventory;

    public GuiSatchel(InventoryPlayer playerInventory, SatchelInventory satchelInventory, int selectedSlot) {
        super(new ContainerSatchel(playerInventory, satchelInventory, selectedSlot));
        this.playerInventory = playerInventory;
        this.satchelInventory = satchelInventory;
        // Upstream SatchelScreen: imageWidth 247, imageHeight 256.
        this.xSize = 247;
        this.ySize = 256;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRenderer.drawString(this.satchelInventory.getDisplayName().getUnformattedText(), 8, 6, 0xE6E6E6);
        this.fontRenderer.drawString(this.playerInventory.getDisplayName().getUnformattedText(), 44, 162, 0xD0D0D0);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        this.drawDefaultBackground();
        this.mc.getTextureManager().bindTexture(TEXTURE);
        drawModalRectWithCustomSizedTexture(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize,
                this.xSize, this.ySize);
    }
}
