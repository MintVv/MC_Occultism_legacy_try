package com.occultism.occultism.client.gui;

import java.io.IOException;

import com.occultism.occultism.container.ContainerStorageController;
import com.occultism.occultism.network.ModNetworking;
import com.occultism.occultism.tileentity.TileEntityStorageController;
import com.occultism.occultism.tileentity.TileEntityStableWormhole;
import com.occultism.occultism.tileentity.StorageSortDirection;
import com.occultism.occultism.tileentity.StorageSortType;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class GuiStorageController extends GuiContainer {

    private static final int BUTTON_SORT_TYPE = 100;
    private static final int BUTTON_SORT_DIRECTION = 101;
    private static final int BUTTON_CLEAR_SEARCH = 102;

    private static final int PANEL = 0xFF1F2024;
    private static final int PANEL_EDGE = 0xFF555866;
    private static final int SLOT = 0xFF2D3038;
    private static final int SLOT_EDGE = 0xFF777B8A;

    private final InventoryPlayer playerInventory;
    private final TileEntityStorageController storageController;
    private GuiTextField searchField;

    public GuiStorageController(InventoryPlayer playerInventory, TileEntityStorageController storageController) {
        this(playerInventory, storageController, false);
    }

    public GuiStorageController(InventoryPlayer playerInventory, TileEntityStorageController storageController,
            boolean remoteAccess) {
        super(new ContainerStorageController(playerInventory, storageController, remoteAccess));
        this.playerInventory = playerInventory;
        this.storageController = storageController;
        // The right-hand terminal panel contains the 3x3 crafting matrix and
        // result slot; the storage viewport and player inventory remain at the
        // vanilla 176px coordinates on the left.
        this.xSize = 362;
        this.ySize = 222;
    }

    public GuiStorageController(InventoryPlayer playerInventory, TileEntityStorageController storageController,
            int remoteSlot) {
        super(new ContainerStorageController(playerInventory, storageController, remoteSlot));
        this.playerInventory = playerInventory;
        this.storageController = storageController;
        this.xSize = 362;
        this.ySize = 222;
    }

    public GuiStorageController(InventoryPlayer playerInventory, TileEntityStorageController storageController,
            TileEntityStableWormhole stableWormhole) {
        super(new ContainerStorageController(playerInventory, storageController, stableWormhole));
        this.playerInventory = playerInventory;
        this.storageController = storageController;
        this.xSize = 362;
        this.ySize = 222;
    }

    @Override
    public void initGui() {
        super.initGui();
        Keyboard.enableRepeatEvents(true);
        int left = this.guiLeft;
        int top = this.guiTop;
        this.searchField = new GuiTextField(0, this.fontRenderer, left + 8, top + 4, 64, 12);
        this.searchField.setMaxStringLength(128);
        this.searchField.setTextColor(0xFFFFFF);
        this.searchField.setDisabledTextColour(0x777777);
        this.searchField.setEnableBackgroundDrawing(false);
        this.searchField.setText(this.storageController.getSearchQuery());
        this.searchField.setCursorPositionEnd();

        this.buttonList.add(new GuiButton(BUTTON_SORT_TYPE, left + 74, top + 2, 45, 16,
                this.getSortTypeLabel()));
        this.buttonList.add(new GuiButton(BUTTON_SORT_DIRECTION, left + 121, top + 2, 29, 16,
                this.getSortDirectionLabel()));
        this.buttonList.add(new GuiButton(BUTTON_CLEAR_SEARCH, left + 152, top + 2, 16, 16, "X"));
        ModNetworking.requestStorageStacks();
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        if (this.searchField != null && this.searchField.getText().isEmpty()) {
            this.fontRenderer.drawString(I18n.format("gui.occultism.storage_controller.search"), 10, 6, 0x888888);
        }
        this.fontRenderer.drawString(this.playerInventory.getDisplayName().getUnformattedText(), 8, 128, 0xD0D0D0);
        this.fontRenderer.drawString(I18n.format("gui.occultism.storage_controller.crafting"), 206, 8, 0xD0D0D0);
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

        drawRect(x + 202, y + 18, x + 350, y + 106, PANEL_EDGE);
        drawRect(x + 203, y + 19, x + 349, y + 105, PANEL);
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                this.drawSlotBackground(x + 226 + column * 18, y + 26 + row * 18);
            }
        }
        this.drawSlotBackground(x + 292, y + 44);

        for (int row = 0; row < TileEntityStorageController.ROWS; row++) {
            for (int column = 0; column < TileEntityStorageController.COLUMNS; column++) {
                this.drawSlotBackground(x + 8 + column * 18, y + 18 + row * 18);
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.drawSlotBackground(x + 8 + column * 18, y + 140 + row * 18);
            }
        }

        for (int column = 0; column < 9; column++) {
            this.drawSlotBackground(x + 8 + column * 18, y + 198);
        }

        this.drawStorageScrollbar(x, y);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (this.searchField != null && this.searchField.textboxKeyTyped(typedChar, keyCode)) {
            this.sendStorageSettings();
            return;
        }
        ContainerStorageController container = this.getStorageContainer();
        if (keyCode == Keyboard.KEY_UP) {
            container.scrollStorage(-1);
            return;
        }
        if (keyCode == Keyboard.KEY_DOWN) {
            container.scrollStorage(1);
            return;
        }
        if (keyCode == Keyboard.KEY_PRIOR) {
            container.scrollStorage(-ContainerStorageController.VISIBLE_STORAGE_ROWS);
            return;
        }
        if (keyCode == Keyboard.KEY_NEXT) {
            container.scrollStorage(ContainerStorageController.VISIBLE_STORAGE_ROWS);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel == 0) {
            return;
        }
        this.getStorageContainer().scrollStorage(wheel < 0 ? 1 : -1);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (this.searchField != null) {
            this.searchField.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == BUTTON_SORT_TYPE) {
            this.storageController.setSortType(this.storageController.getSortType().next());
            button.displayString = this.getSortTypeLabel();
            this.sendStorageSettings();
        } else if (button.id == BUTTON_SORT_DIRECTION) {
            this.storageController.setSortDirection(this.storageController.getSortDirection().next());
            button.displayString = this.getSortDirectionLabel();
            this.sendStorageSettings();
        } else if (button.id == BUTTON_CLEAR_SEARCH) {
            if (this.searchField != null) {
                this.searchField.setText("");
            }
            this.sendStorageSettings();
        } else {
            super.actionPerformed(button);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (this.searchField != null) {
            this.searchField.drawTextBox();
        }
    }

    private void sendStorageSettings() {
        String query = this.searchField == null ? "" : this.searchField.getText();
        this.storageController.setSearchQuery(query);
        this.getStorageContainer().refreshStorageView();
        ModNetworking.sendStorageSettings(query, this.storageController.getSortType(),
                this.storageController.getSortDirection());
    }

    private String getSortTypeLabel() {
        StorageSortType type = this.storageController.getSortType();
        return I18n.format("gui.occultism.storage_controller.sort." + type.name().toLowerCase());
    }

    private String getSortDirectionLabel() {
        StorageSortDirection direction = this.storageController.getSortDirection();
        return I18n.format("gui.occultism.storage_controller.direction." + direction.name().toLowerCase());
    }

    private ContainerStorageController getStorageContainer() {
        return (ContainerStorageController) this.inventorySlots;
    }

    /** Called by MessageUpdateStacks on the client main thread. */
    public void applyStorageSnapshot(java.util.List<net.minecraft.item.ItemStack> stacks, int usedSlots,
            int maxSlots) {
        this.getStorageContainer().applyClientStorageSnapshot(stacks, usedSlots, maxSlots);
    }

    public ClientStorageCache getClientStorageCache() {
        return this.getStorageContainer().getClientStorageCache();
    }

    /** Entry point for recipe-transfer integrations (JEI/recipe book adapters). */
    public void setRecipeFromTemplate(NBTTagCompound template) {
        ModNetworking.sendStorageRecipe(template);
    }

    public void setRecipeById(net.minecraft.util.ResourceLocation recipeId) {
        ModNetworking.sendStorageRecipeById(recipeId);
    }

    private void drawStorageScrollbar(int x, int y) {
        ContainerStorageController container = this.getStorageContainer();
        int maxScroll = container.getMaxStorageScrollRow();
        int trackTop = y + 18;
        int trackBottom = y + 126;
        drawRect(x + 171, trackTop, x + 174, trackBottom, PANEL_EDGE);
        if (maxScroll == 0) {
            drawRect(x + 170, trackTop, x + 175, trackBottom, SLOT_EDGE);
            return;
        }
        int thumbHeight = Math.max(12, (trackBottom - trackTop) * ContainerStorageController.VISIBLE_STORAGE_ROWS
                / container.getStorageRows());
        int thumbTravel = trackBottom - trackTop - thumbHeight;
        int thumbTop = trackTop + thumbTravel * container.getStorageScrollRow() / maxScroll;
        drawRect(x + 170, thumbTop, x + 175, thumbTop + thumbHeight, SLOT_EDGE);
    }

    private void drawSlotBackground(int x, int y) {
        drawRect(x - 1, y - 1, x + 17, y + 17, SLOT_EDGE);
        drawRect(x, y, x + 16, y + 16, SLOT);
    }
}
