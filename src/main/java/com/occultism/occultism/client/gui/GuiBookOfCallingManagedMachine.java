package com.occultism.occultism.client.gui;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;

import com.occultism.occultism.network.ModNetworking;
import com.occultism.occultism.spirit.MachineReference;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** 1.12 adaptation of the upstream managed-machine Book of Calling screen. */
@SideOnly(Side.CLIENT)
public class GuiBookOfCallingManagedMachine extends GuiScreen {

    private static final int GUI_HEIGHT = 166;
    private static final int BUTTON_WIDTH = 150;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_MARGIN = 5;
    private static final int BUTTON_TOP = 60;
    private static final int BUTTON_INSERT_FACING = 0;
    private static final int BUTTON_EXTRACT_FACING = 1;
    private static final int BUTTON_EXIT = 2;
    private static final int COLOR_TEXT = 0xFFFFFFFF;

    private final MachineReference baseReference;
    private final String originalCustomName;
    private EnumFacing insertFacing;
    private EnumFacing extractFacing;
    private String customName;
    private GuiTextField nameField;

    public GuiBookOfCallingManagedMachine(MachineReference reference) {
        this.baseReference = copy(reference);
        this.insertFacing = this.baseReference.getInsertFacing();
        this.extractFacing = this.baseReference.getExtractFacing();
        this.originalCustomName = this.baseReference.getCustomName();
        this.customName = this.originalCustomName;
    }

    @Override
    public void initGui() {
        super.initGui();
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();

        int center = this.width / 2;
        int top = (this.height - GUI_HEIGHT) / 2;
        this.buttonList.add(new GuiButton(BUTTON_INSERT_FACING, center - BUTTON_WIDTH / 2, top + BUTTON_TOP,
                BUTTON_WIDTH, BUTTON_HEIGHT, this.getFacingText(this.insertFacing)));
        this.buttonList.add(new GuiButton(BUTTON_EXTRACT_FACING, center - BUTTON_WIDTH / 2,
                top + BUTTON_TOP + BUTTON_HEIGHT + BUTTON_MARGIN, BUTTON_WIDTH, BUTTON_HEIGHT,
                this.getFacingText(this.extractFacing)));

        int textWidth = BUTTON_WIDTH - 4;
        this.nameField = new GuiTextField(0, this.fontRenderer, center - textWidth / 2,
                top + BUTTON_TOP + BUTTON_HEIGHT * 2 + BUTTON_MARGIN * 2, textWidth, BUTTON_HEIGHT);
        this.nameField.setMaxStringLength(30);
        this.nameField.setTextColor(COLOR_TEXT);
        this.nameField.setText(this.customName);
        this.nameField.setFocused(true);

        this.buttonList.add(new GuiButton(BUTTON_EXIT, center - BUTTON_HEIGHT / 2,
                top + BUTTON_TOP + BUTTON_HEIGHT * 3 + BUTTON_MARGIN * 3,
                BUTTON_HEIGHT, BUTTON_HEIGHT, "X"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        if (this.nameField != null) {
            this.nameField.drawTextBox();
        }

        int center = this.width / 2;
        int top = (this.height - GUI_HEIGHT) / 2;
        this.drawRightAlignedString(I18n.format("gui.occultism.book_of_calling.manage_machine.insert"),
                center - 80, top + BUTTON_TOP + 5);
        this.drawRightAlignedString(I18n.format("gui.occultism.book_of_calling.manage_machine.extract"),
                center - 80, top + BUTTON_TOP + BUTTON_HEIGHT + BUTTON_MARGIN + 5);
        this.drawRightAlignedString(I18n.format("gui.occultism.book_of_calling.manage_machine.custom_name"),
                center - 80, top + BUTTON_TOP + BUTTON_HEIGHT * 2 + BUTTON_MARGIN * 2 + 6);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == BUTTON_INSERT_FACING) {
            this.insertFacing = nextFacing(this.insertFacing);
            ModNetworking.sendManagedMachine(this.makeMachineReference());
            this.initGui();
            return;
        }
        if (button.id == BUTTON_EXTRACT_FACING) {
            this.extractFacing = nextFacing(this.extractFacing);
            ModNetworking.sendManagedMachine(this.makeMachineReference());
            this.initGui();
            return;
        }
        if (button.id == BUTTON_EXIT) {
            this.mc.displayGuiScreen(null);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (this.nameField != null && this.nameField.textboxKeyTyped(typedChar, keyCode)) {
            this.customName = this.nameField.getText();
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (this.nameField != null && this.nameField.mouseClicked(mouseX, mouseY, mouseButton)) {
            return;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void updateScreen() {
        if (this.nameField != null) {
            this.nameField.updateCursorCounter();
        }
        super.updateScreen();
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
        if (this.nameField != null) {
            this.nameField.setFocused(false);
        }
        if (!StringUtils.isBlank(this.customName) && !this.customName.equals(this.originalCustomName)) {
            ModNetworking.sendManagedMachine(this.makeMachineReference());
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private MachineReference makeMachineReference() {
        MachineReference reference = copy(this.baseReference);
        reference.setInsertPosition(reference.getInsertPosition(), reference.getInsertDimension(), this.insertFacing,
                reference.getInsertRegistryName());
        reference.setExtractPosition(reference.getExtractPosition(), reference.getExtractDimension(),
                this.extractFacing, reference.getExtractRegistryName());
        reference.setCustomName(this.customName);
        return reference;
    }

    private String getFacingText(EnumFacing facing) {
        return I18n.format("enum.occultism.facing." + facing.getName());
    }

    private void drawRightAlignedString(String text, int right, int y) {
        this.fontRenderer.drawString(text, right - this.fontRenderer.getStringWidth(text), y, COLOR_TEXT);
    }

    private static EnumFacing nextFacing(EnumFacing facing) {
        EnumFacing[] values = EnumFacing.values();
        return values[(facing.ordinal() + 1) % values.length];
    }

    private static MachineReference copy(MachineReference reference) {
        return reference == null ? new MachineReference()
                : MachineReference.from(reference.writeToNBT(new NBTTagCompound()));
    }
}
