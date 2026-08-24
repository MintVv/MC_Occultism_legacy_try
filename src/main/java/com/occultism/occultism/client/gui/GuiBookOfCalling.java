package com.occultism.occultism.client.gui;

import java.io.IOException;

import com.occultism.occultism.item.BookOfCallingMode;
import com.occultism.occultism.item.ItemBookOfCallingFoliotJob;
import com.occultism.occultism.item.WorkAreaSize;
import com.occultism.occultism.network.MessageSetBookOfCallingMode;
import com.occultism.occultism.network.MessageSetBookOfCallingWorkAreaSize;
import com.occultism.occultism.network.ModNetworking;
import com.occultism.occultism.container.ContainerBookOfCalling;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Configuration screen for a Book of Calling.
 *
 * <p>The layout and interaction model follow the Forge 1.15 upstream screen:
 * mode and work-area controls are centered, changes are sent immediately, and
 * the only explicit control for leaving the screen is the small X button.</p>
 */
@SideOnly(Side.CLIENT)
public class GuiBookOfCalling extends GuiContainer {

    private static final int GUI_HEIGHT = 166;
    private static final int BUTTON_WIDTH = 150;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_MODE = 0;
    private static final int BUTTON_WORK_AREA = 1;
    private static final int BUTTON_EXIT = 2;
    private static final int COLOR_TEXT = 0xFFFFFFFF;

    private final int slot;
    private BookOfCallingMode[] modes = new BookOfCallingMode[0];
    private BookOfCallingMode selectedMode = BookOfCallingMode.SET_BASE;
    private WorkAreaSize workAreaSize = WorkAreaSize.SMALL;
    private ItemBookOfCallingFoliotJob activeBook;

    public GuiBookOfCalling(int slot) {
        super(new ContainerBookOfCalling(slot));
        this.slot = slot;
        this.xSize = 176;
        this.ySize = GUI_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();
        ItemBookOfCallingFoliotJob book = this.getBook();
        if (book == null) {
            this.mc.player.closeScreen();
            return;
        }

        // Keep client-side selections across a resize/re-init until the server
        // sends the inventory update for the packet that was just emitted.
        if (this.activeBook != book) {
            this.activeBook = book;
            this.modes = book.getAvailableModes();
            this.selectedMode = book.getCurrentMode(this.getStack());
            this.workAreaSize = book.getCurrentWorkAreaSize(this.getStack());
        } else {
            this.modes = book.getAvailableModes();
            if (!this.containsMode(this.selectedMode)) {
                this.selectedMode = this.modes.length == 0 ? BookOfCallingMode.SET_BASE : this.modes[0];
            }
        }
        this.rebuildButtons();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        int center = this.width / 2;
        int top = (this.height - GUI_HEIGHT) / 2;
        this.drawRightAlignedString(I18n.format("gui.occultism.book_of_calling.mode"), center - 80, top + 66);
        if (this.selectedMode.hasSize()) {
            this.drawRightAlignedString(I18n.format("gui.occultism.book_of_calling.work_area"), center - 80,
                    top + 91);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == BUTTON_MODE) {
            this.selectNextMode();
            ModNetworking.CHANNEL.sendToServer(new MessageSetBookOfCallingMode(this.slot, this.selectedMode));
            this.rebuildButtons();
            return;
        }
        if (button.id == BUTTON_WORK_AREA && this.selectedMode.hasSize()) {
            this.workAreaSize = WorkAreaSize.byOrdinal((this.workAreaSize.ordinal() + 1)
                    % WorkAreaSize.values().length);
            ModNetworking.CHANNEL.sendToServer(new MessageSetBookOfCallingWorkAreaSize(this.slot, this.workAreaSize));
            this.rebuildButtons();
            return;
        }
        if (button.id == BUTTON_EXIT) {
            this.mc.player.closeScreen();
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        // The upstream screen has no framed container background.
    }

    private void rebuildButtons() {
        int center = this.width / 2;
        int top = (this.height - GUI_HEIGHT) / 2;
        boolean showSize = this.selectedMode.hasSize();

        this.buttonList.clear();
        this.buttonList.add(new GuiButton(BUTTON_MODE, center - BUTTON_WIDTH / 2, top + 60, BUTTON_WIDTH,
                BUTTON_HEIGHT, I18n.format(this.selectedMode.getTranslationKey())));
        if (showSize) {
            this.buttonList.add(new GuiButton(BUTTON_WORK_AREA, center - BUTTON_WIDTH / 2, top + 85, BUTTON_WIDTH,
                    BUTTON_HEIGHT, I18n.format(this.workAreaSize.getTranslationKey())));
        }
        this.buttonList.add(new GuiButton(BUTTON_EXIT, center - BUTTON_HEIGHT / 2,
                top + (showSize ? 110 : 85), BUTTON_HEIGHT, BUTTON_HEIGHT, "X"));
    }

    private void selectNextMode() {
        if (this.modes.length == 0) {
            return;
        }
        int index = 0;
        for (int i = 0; i < this.modes.length; i++) {
            if (this.modes[i] == this.selectedMode) {
                index = i;
                break;
            }
        }
        this.selectedMode = this.modes[(index + 1) % this.modes.length];
    }

    private boolean containsMode(BookOfCallingMode mode) {
        for (BookOfCallingMode available : this.modes) {
            if (available == mode) {
                return true;
            }
        }
        return false;
    }

    private ItemStack getStack() {
        if (this.mc.player == null || this.slot < 0 || this.slot >= this.mc.player.inventory.getSizeInventory()) {
            return ItemStack.EMPTY;
        }
        return this.mc.player.inventory.getStackInSlot(this.slot);
    }

    private ItemBookOfCallingFoliotJob getBook() {
        ItemStack stack = this.getStack();
        return stack.getItem() instanceof ItemBookOfCallingFoliotJob
                ? (ItemBookOfCallingFoliotJob) stack.getItem() : null;
    }

    private void drawRightAlignedString(String text, int right, int y) {
        this.fontRenderer.drawString(text, right - this.fontRenderer.getStringWidth(text), y, COLOR_TEXT);
    }
}
