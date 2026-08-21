package com.occultism.occultism.client.gui;

import java.io.IOException;

import com.occultism.occultism.item.BookOfCallingMode;
import com.occultism.occultism.item.ItemBookOfCallingFoliotJob;
import com.occultism.occultism.item.WorkAreaSize;
import com.occultism.occultism.network.MessageSetBookOfCallingConfig;
import com.occultism.occultism.network.ModNetworking;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiBookOfCalling extends GuiScreen {

    private static final int GUI_WIDTH = 241;
    private static final int GUI_HEIGHT = 86;
    private static final int BUTTON_MODE = 0;
    private static final int BUTTON_WORK_AREA = 1;
    private static final int BUTTON_CONFIRM = 2;
    private static final int COLOR_TOP = 0xFF4A275C;
    private static final int COLOR_PANEL = 0xFF536070;
    private static final int COLOR_PANEL_DARK = 0xFF313946;
    private static final int COLOR_LINE = 0xFF1B1D23;
    private static final int COLOR_TEXT = 0xFFFFFFFF;

    private final int slot;
    private BookOfCallingMode[] modes = new BookOfCallingMode[0];
    private int selectedMode;
    private int selectedWorkArea;
    private GuiButton workAreaButton;

    public GuiBookOfCalling(int slot) {
        this.slot = slot;
    }

    @Override
    public void initGui() {
        super.initGui();
        ItemBookOfCallingFoliotJob book = this.getBook();
        if (book == null) {
            this.mc.displayGuiScreen(null);
            return;
        }

        this.modes = book.getAvailableModes();
        this.selectedMode = this.indexOf(book.getCurrentMode(this.getStack()));
        this.selectedWorkArea = book.getCurrentWorkAreaSize(this.getStack()).ordinal();

        int left = (this.width - GUI_WIDTH) / 2;
        int top = (this.height - GUI_HEIGHT) / 2;
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(BUTTON_MODE, left + 72, top + 22, 120, 18, this.getModeButtonText()));
        this.workAreaButton = new GuiButton(BUTTON_WORK_AREA, left + 72, top + 54, 120, 18,
                this.getWorkAreaButtonText());
        this.buttonList.add(this.workAreaButton);
        this.buttonList.add(new GuiButton(BUTTON_CONFIRM, left + 205, top + 54, 28, 18,
                I18n.format("gui.occultism.book_of_calling.confirm")));
        this.updateButtonState();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        int left = (this.width - GUI_WIDTH) / 2;
        int top = (this.height - GUI_HEIGHT) / 2;
        drawRect(left, top - 4, left + GUI_WIDTH, top + 12, COLOR_TOP);
        drawRect(left + 3, top + 9, left + GUI_WIDTH - 3, top + GUI_HEIGHT, COLOR_PANEL);
        drawRect(left + 4, top + 10, left + GUI_WIDTH - 4, top + GUI_HEIGHT - 1, COLOR_PANEL_DARK);
        drawRect(left + 3, top + 47, left + GUI_WIDTH - 3, top + 48, COLOR_LINE);
        drawRect(left + 202, top + 47, left + 203, top + GUI_HEIGHT - 1, COLOR_LINE);

        String title = this.getStack().isEmpty() ? I18n.format("item.occultism.book_of_calling")
                : this.getStack().getDisplayName();
        this.drawCenteredString(this.fontRenderer, title, left + GUI_WIDTH / 2, top, 0xFFE8DDF1);
        this.drawRightAlignedString(I18n.format("gui.occultism.book_of_calling.mode"), left + 66, top + 27);
        this.drawRightAlignedString(I18n.format("gui.occultism.book_of_calling.work_area"), left + 66, top + 59);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == BUTTON_MODE) {
            if (this.modes.length > 0) {
                this.selectedMode = (this.selectedMode + 1) % this.modes.length;
            }
            button.displayString = this.getModeButtonText();
            this.updateButtonState();
            return;
        }
        if (button.id == BUTTON_WORK_AREA && this.selectedMode().hasSize()) {
            this.selectedWorkArea = (this.selectedWorkArea + 1) % WorkAreaSize.values().length;
            button.displayString = this.getWorkAreaButtonText();
            return;
        }
        if (button.id == BUTTON_CONFIRM) {
            ModNetworking.CHANNEL.sendToServer(new MessageSetBookOfCallingConfig(this.slot, this.selectedMode(),
                    WorkAreaSize.byOrdinal(this.selectedWorkArea)));
            this.mc.displayGuiScreen(null);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private void updateButtonState() {
        if (this.workAreaButton != null) {
            this.workAreaButton.enabled = this.selectedMode().hasSize();
            this.workAreaButton.displayString = this.getWorkAreaButtonText();
        }
    }

    private String getModeButtonText() {
        return I18n.format(this.selectedMode().getTranslationKey());
    }

    private String getWorkAreaButtonText() {
        if (!this.selectedMode().hasSize()) {
            return I18n.format("gui.occultism.book_of_calling.work_area.not_applicable");
        }
        return I18n.format(WorkAreaSize.byOrdinal(this.selectedWorkArea).getTranslationKey());
    }

    private BookOfCallingMode selectedMode() {
        if (this.modes.length == 0) {
            return BookOfCallingMode.SET_BASE;
        }
        return this.modes[Math.max(0, Math.min(this.selectedMode, this.modes.length - 1))];
    }

    private int indexOf(BookOfCallingMode mode) {
        for (int i = 0; i < this.modes.length; i++) {
            if (this.modes[i] == mode) {
                return i;
            }
        }
        return 0;
    }

    private ItemStack getStack() {
        return this.mc.player == null ? ItemStack.EMPTY : this.mc.player.inventory.getStackInSlot(this.slot);
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
