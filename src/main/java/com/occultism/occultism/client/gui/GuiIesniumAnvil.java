package com.occultism.occultism.client.gui;

import java.io.IOException;

import com.occultism.occultism.container.ContainerIesniumAnvil;

import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;

public class GuiIesniumAnvil extends GuiContainer implements IContainerListener {

    private static final ResourceLocation ANVIL_RESOURCE = new ResourceLocation("textures/gui/container/anvil.png");
    private static final int EXPENSIVE_COST = 100;

    private final ContainerIesniumAnvil anvil;
    private final InventoryPlayer playerInventory;
    private GuiTextField nameField;

    public GuiIesniumAnvil(InventoryPlayer inventory, World world, BlockPos pos) {
        super(new ContainerIesniumAnvil(inventory, world, pos, Minecraft.getMinecraft().player));
        this.playerInventory = inventory;
        this.anvil = (ContainerIesniumAnvil) this.inventorySlots;
    }

    @Override
    public void initGui() {
        super.initGui();
        Keyboard.enableRepeatEvents(true);
        int left = (this.width - this.xSize) / 2;
        int top = (this.height - this.ySize) / 2;
        this.nameField = new GuiTextField(0, this.fontRenderer, left + 62, top + 24, 103, 12);
        this.nameField.setTextColor(-1);
        this.nameField.setDisabledTextColour(-1);
        this.nameField.setEnableBackgroundDrawing(false);
        this.nameField.setMaxStringLength(35);
        this.inventorySlots.removeListener(this);
        this.inventorySlots.addListener(this);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
        this.inventorySlots.removeListener(this);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        GlStateManager.disableLighting();
        GlStateManager.disableBlend();
        this.fontRenderer.drawString(I18n.format("tile.occultism.iesnium_anvil.name"), 60, 6, 4210752);

        if (this.anvil.maximumCost > 0) {
            int color = 8453920;
            boolean shouldDraw = true;
            String text = I18n.format("container.repair.cost", this.anvil.maximumCost);

            if (this.anvil.maximumCost >= EXPENSIVE_COST && !this.mc.player.capabilities.isCreativeMode) {
                text = I18n.format("container.repair.expensive");
                color = 16736352;
            } else if (!this.anvil.getSlot(2).getHasStack()) {
                shouldDraw = false;
            } else if (!this.anvil.getSlot(2).canTakeStack(this.playerInventory.player)) {
                color = 16736352;
            }

            if (shouldDraw) {
                int shadow = -16777216 | (color & 16579836) >> 2 | color & -16777216;
                int x = this.xSize - 8 - this.fontRenderer.getStringWidth(text);
                if (this.fontRenderer.getUnicodeFlag()) {
                    drawRect(x - 3, 65, this.xSize - 7, 77, -16777216);
                    drawRect(x - 2, 66, this.xSize - 8, 76, -12895429);
                } else {
                    this.fontRenderer.drawString(text, x, 68, shadow);
                    this.fontRenderer.drawString(text, x + 1, 67, shadow);
                    this.fontRenderer.drawString(text, x + 1, 68, shadow);
                }
                this.fontRenderer.drawString(text, x, 67, color);
            }
        }

        GlStateManager.enableLighting();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (this.nameField.textboxKeyTyped(typedChar, keyCode)) {
            this.renameItem();
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    private void renameItem() {
        String name = this.nameField.getText();
        Slot slot = this.anvil.getSlot(0);
        if (slot != null && slot.getHasStack() && !slot.getStack().hasDisplayName()
                && name.equals(slot.getStack().getDisplayName())) {
            name = "";
        }

        this.anvil.updateItemName(name);
        this.mc.player.connection.sendPacket(new CPacketCustomPayload("MC|ItemName",
                (new PacketBuffer(Unpooled.buffer())).writeString(name)));
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.nameField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
        GlStateManager.disableLighting();
        GlStateManager.disableBlend();
        this.nameField.drawTextBox();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(ANVIL_RESOURCE);
        int left = (this.width - this.xSize) / 2;
        int top = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(left, top, 0, 0, this.xSize, this.ySize);
        this.drawTexturedModalRect(left + 59, top + 20, 0, this.ySize + (this.anvil.getSlot(0).getHasStack() ? 0 : 16),
                110, 16);

        if ((this.anvil.getSlot(0).getHasStack() || this.anvil.getSlot(1).getHasStack())
                && !this.anvil.getSlot(2).getHasStack()) {
            this.drawTexturedModalRect(left + 99, top + 45, this.xSize, 0, 28, 21);
        }
    }

    @Override
    public void sendAllContents(Container containerToSend, NonNullList<ItemStack> itemsList) {
        this.sendSlotContents(containerToSend, 0, containerToSend.getSlot(0).getStack());
    }

    @Override
    public void sendSlotContents(Container containerToSend, int slotInd, ItemStack stack) {
        if (slotInd == 0) {
            this.nameField.setText(stack.isEmpty() ? "" : stack.getDisplayName());
            this.nameField.setEnabled(!stack.isEmpty());
            if (!stack.isEmpty()) {
                this.renameItem();
            }
        }
    }

    @Override
    public void sendWindowProperty(Container containerIn, int varToUpdate, int newValue) {
    }

    @Override
    public void sendAllWindowProperties(Container containerIn, IInventory inventory) {
    }
}
