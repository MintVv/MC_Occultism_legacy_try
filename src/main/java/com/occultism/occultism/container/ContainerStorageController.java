package com.occultism.occultism.container;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.occultism.occultism.client.gui.ClientStorageCache;
import com.occultism.occultism.item.ItemStorageRemote;
import com.occultism.occultism.tileentity.TileEntityStableWormhole;
import com.occultism.occultism.tileentity.StorageVirtualEntry;
import com.occultism.occultism.tileentity.TileEntityStorageController;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.player.EntityPlayerMP;

public class ContainerStorageController extends Container {

    public static final int VISIBLE_STORAGE_ROWS = TileEntityStorageController.ROWS;
    private static final int VISIBLE_STORAGE_SLOTS = VISIBLE_STORAGE_ROWS * TileEntityStorageController.COLUMNS;
    private static final int STORAGE_LEFT = 8;
    private static final int STORAGE_TOP = 18;
    private static final int PLAYER_INVENTORY_LEFT = 8;
    private static final int PLAYER_INVENTORY_TOP = 140;
    private static final int HOTBAR_TOP = 198;
    private static final int CRAFTING_OUTPUT_X = 292;
    private static final int CRAFTING_OUTPUT_Y = 44;
    private static final int CRAFTING_GRID_X = 226;
    private static final int CRAFTING_GRID_Y = 26;

    private final TileEntityStorageController storageController;
    private final InventoryPlayer playerInventory;
    private final boolean remoteAccess;
    private final int storageSize;
    private int storageRows;
    private int storageScrollRow;
    private List<ItemStack> clientStorageSnapshot = new ArrayList<>();
    private final ClientStorageCache clientStorageCache = new ClientStorageCache();
    private boolean clientStorageSnapshotSet;
    private int clientUsedSlots;
    private int clientMaxSlots;
    private final int playerInventoryStart;
    private final int playerHotbarStart;
    private final int playerSlotEnd;
    private final InventoryCrafting craftMatrix;
    private final InventoryCraftResult craftResult;
    private IRecipe currentRecipe;
    private boolean recipeLocked;

    public ContainerStorageController(InventoryPlayer playerInventory, TileEntityStorageController storageController) {
        this(playerInventory, storageController, false);
    }

    public ContainerStorageController(InventoryPlayer playerInventory, TileEntityStorageController storageController,
            boolean remoteAccess) {
        this.storageController = storageController;
        this.playerInventory = playerInventory;
        this.remoteAccess = remoteAccess;
        this.storageSize = storageController.getStorageSlotCount();
        this.craftMatrix = new InventoryCrafting(this, 3, 3);
        this.craftResult = new InventoryCraftResult();
        this.currentRecipe = null;
        this.recipeLocked = false;
        this.storageRows = 1;
        this.storageScrollRow = 0;
        this.clientStorageSnapshotSet = false;
        this.clientUsedSlots = 0;
        this.clientMaxSlots = this.storageSize;
        this.playerInventoryStart = this.storageSize + 10;
        this.playerHotbarStart = this.playerInventoryStart + 27;
        this.playerSlotEnd = this.playerHotbarStart + 9;

        for (int slot = 0; slot < this.storageSize; slot++) {
            int row = slot / TileEntityStorageController.COLUMNS;
            int column = slot % TileEntityStorageController.COLUMNS;
            this.addSlotToContainer(new VirtualStorageSlot(storageController, slot,
                    STORAGE_LEFT + column * 18, this.storageSlotY(row)));
        }

        this.addSlotToContainer(new StorageCraftingResultSlot(playerInventory.player, this.craftMatrix,
                this.craftResult, 0, CRAFTING_OUTPUT_X, CRAFTING_OUTPUT_Y));
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                this.addSlotToContainer(new Slot(this.craftMatrix, column + row * 3,
                        CRAFTING_GRID_X + column * 18, CRAFTING_GRID_Y + row * 18) {
                    @Override
                    public int getSlotStackLimit() {
                        return ContainerStorageController.this.storageController.getStorageStackLimit();
                    }
                });
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlotToContainer(new Slot(playerInventory, column + row * 9 + 9,
                        PLAYER_INVENTORY_LEFT + column * 18, PLAYER_INVENTORY_TOP + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            this.addSlotToContainer(new Slot(playerInventory, column, PLAYER_INVENTORY_LEFT + column * 18,
                    HOTBAR_TOP));
        }

        // Restore the terminal-owned matrix after all slots are registered.  The
        // matrix is persisted by the controller, but never exposed through its
        // physical ITEM_HANDLER capability.
        this.recipeLocked = true;
        try {
            List<ItemStack> persistedMatrix = storageController.getCraftingMatrix();
            for (int slot = 0; slot < this.craftMatrix.getSizeInventory() && slot < persistedMatrix.size(); slot++) {
                ItemStack stack = persistedMatrix.get(slot);
                if (stack != null && !stack.isEmpty()) {
                    this.craftMatrix.setInventorySlotContents(slot, stack.copy());
                }
            }
        } finally {
            this.recipeLocked = false;
        }
        this.refreshStorageView();
        this.updateCraftingResult();
    }

    public TileEntityStorageController getStorageController() {
        return this.storageController;
    }

    /**
     * Mirrors the upstream terminal heartbeat: external machine inserts and
     * extracts must eventually invalidate the client virtual-stack view while
     * a direct or remote GUI remains open.  The loaded/permission checks also
     * stop a stale remote container from sending snapshots after its target
     * chunk has unloaded.
     */
    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (!(this.playerInventory.player instanceof EntityPlayerMP)
                || !this.isStorageControllerLoaded()
                || this.storageController.getWorld().isRemote
                || this.storageController.getWorld().getTotalWorldTime() % 40L != 0L) {
            return;
        }
        EntityPlayerMP player = (EntityPlayerMP) this.playerInventory.player;
        if (this.canInteractWith(player)) {
            com.occultism.occultism.network.ModNetworking.syncStorageStacks(player, this.storageController);
        }
    }

    public boolean isRemoteAccess() {
        return this.remoteAccess;
    }

    public int getStorageSize() {
        return this.storageSize;
    }

    public int getStorageRows() {
        return this.storageRows;
    }

    public int getStorageScrollRow() {
        return this.storageScrollRow;
    }

    public int getMaxStorageScrollRow() {
        return Math.max(0, this.storageRows - VISIBLE_STORAGE_ROWS);
    }

    public int getClientUsedSlots() {
        return this.clientUsedSlots;
    }

    public int getClientMaxSlots() {
        return this.clientMaxSlots;
    }

    public InventoryCrafting getCraftMatrix() {
        return this.craftMatrix;
    }

    public InventoryCraftResult getCraftResultInventory() {
        return this.craftResult;
    }

    public IRecipe getCurrentRecipe() {
        return this.currentRecipe;
    }

    /**
     * Returns whether a server-side crafting transaction currently owns the
     * matrix.  The lock is intentionally transient container state: upstream
     * does not persist it on the controller, and a reopened terminal must be
     * usable even after a server interruption.
     */
    public boolean isRecipeLocked() {
        return this.recipeLocked;
    }

    /**
     * Applies an upstream-compatible recipe-transfer payload. Each {@code sN}
     * list contains one or more alternative item stacks for matrix slot N.
     * The server takes one matching item from the player's inventory first and
     * then from the physical controller, never trusting client-provided counts.
     */
    public boolean setRecipeFromNBT(NBTTagCompound payload, EntityPlayer player) {
        if (this.recipeLocked || payload == null || player == null || !this.canInteractWith(player)
                || this.storageController.getWorld() == null || this.storageController.getWorld().isRemote) {
            return false;
        }
        boolean ownedRecipeLock = !this.recipeLocked;
        if (ownedRecipeLock) {
            this.recipeLocked = true;
        }
        try {
            for (int i = 0; i < this.craftMatrix.getSizeInventory(); i++) {
                ItemStack existing = this.craftMatrix.getStackInSlot(i);
                if (!existing.isEmpty()) {
                    this.returnStackSafely(player, existing.copy());
                    this.craftMatrix.setInventorySlotContents(i, ItemStack.EMPTY);
                }
            }
            for (int i = 0; i < 9; i++) {
                if (!payload.hasKey("s" + i, 9)) {
                    continue;
                }
                NBTTagList alternatives = payload.getTagList("s" + i, 10);
                int limit = Math.min(16, alternatives.tagCount());
                for (int alternative = 0; alternative < limit; alternative++) {
                    ItemStack template;
                    try {
                        template = new ItemStack(alternatives.getCompoundTagAt(alternative));
                    } catch (RuntimeException ignored) {
                        continue;
                    }
                    if (template.isEmpty()) {
                        continue;
                    }
                    template.setCount(1);
                    ItemStack found = this.takeMatchingFromPlayer(template);
                    if (found.isEmpty()) {
                        found = this.extractMatchingFromStorage(template, 1);
                    }
                    if (!found.isEmpty()) {
                        found.setCount(1);
                        this.craftMatrix.setInventorySlotContents(i, found);
                        break;
                    }
                }
            }
        } finally {
            if (ownedRecipeLock) {
                this.recipeLocked = false;
            }
        }
        this.syncCraftingMatrixToController();
        this.updateCraftingResult();
        if (player instanceof EntityPlayerMP) {
            com.occultism.occultism.network.ModNetworking.syncStorageStacks((EntityPlayerMP) player,
                    this.storageController);
        }
        return true;
    }

    private ItemStack takeMatchingFromPlayer(ItemStack template) {
        for (int slot = 0; slot < this.playerInventory.getSizeInventory(); slot++) {
            ItemStack current = this.playerInventory.getStackInSlot(slot);
            if (current.isEmpty() || !ItemStack.areItemsEqual(template, current)
                    || !ItemStack.areItemStackTagsEqual(template, current)) {
                continue;
            }
            ItemStack result = this.playerInventory.decrStackSize(slot, 1);
            if (!result.isEmpty()) {
                result.setCount(1);
                return result;
            }
        }
        return ItemStack.EMPTY;
    }

    /** Recomputes the recipe on the server and mirrors it on the client for a responsive GUI. */
    public void updateCraftingResult() {
        if (this.recipeLocked) {
            return;
        }
        this.currentRecipe = null;
        ItemStack result = ItemStack.EMPTY;
        if (this.storageController.getWorld() != null) {
            IRecipe recipe = CraftingManager.findMatchingRecipe(this.craftMatrix, this.storageController.getWorld());
            if (recipe != null) {
                this.currentRecipe = recipe;
                result = recipe.getCraftingResult(this.craftMatrix);
                this.craftResult.setRecipeUsed(recipe);
            }
        }
        this.craftResult.setInventorySlotContents(0, result == null ? ItemStack.EMPTY : result);
        if (this.storageController.getWorld() == null || this.storageController.getWorld().isRemote) {
            return;
        }
        this.detectAndSendChanges();
    }

    @Override
    public void onCraftMatrixChanged(net.minecraft.inventory.IInventory inventoryIn) {
        if (inventoryIn == this.craftMatrix) {
            this.syncCraftingMatrixToController();
            this.updateCraftingResult();
        }
    }

    /** Mirrors the open container matrix into terminal NBT without exposing it as storage. */
    private void syncCraftingMatrixToController() {
        if (this.storageController == null || this.recipeLocked) {
            return;
        }
        List<ItemStack> matrix = new ArrayList<>(this.craftMatrix.getSizeInventory());
        for (int slot = 0; slot < this.craftMatrix.getSizeInventory(); slot++) {
            ItemStack stack = this.craftMatrix.getStackInSlot(slot);
            matrix.add(stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
        }
        this.storageController.setCraftingMatrix(matrix);
    }

    public boolean hasClientStorageSnapshot() {
        return this.clientStorageSnapshotSet;
    }

    /** Applies a validated server snapshot to the client-side virtual slots. */
    public void applyClientStorageSnapshot(List<ItemStack> stacks, int usedSlots, int maxSlots) {
        this.clientStorageCache.update(stacks);
        this.clientStorageSnapshot = new ArrayList<>();
        for (ItemStack stack : this.clientStorageCache.getStacks()) {
            this.clientStorageSnapshot.add(stack.copy());
        }
        this.clientUsedSlots = Math.max(0, usedSlots);
        this.clientMaxSlots = Math.max(0, maxSlots);
        this.clientStorageSnapshotSet = true;
        this.refreshStorageView();
    }

    public List<ItemStack> getClientStorageSnapshot() {
        List<ItemStack> result = new ArrayList<>();
        for (ItemStack stack : this.clientStorageSnapshot) {
            result.add(stack.copy());
        }
        return result;
    }

    public ClientStorageCache getClientStorageCache() {
        return this.clientStorageCache;
    }

    /** Upstream-compatible cache injection hook for integrations such as JEI. */
    public void setClientStorageCache(ClientStorageCache cache) {
        List<ItemStack> stacks = cache == null ? Collections.emptyList() : cache.getStacks();
        this.clientStorageCache.update(stacks);
        this.clientStorageSnapshot = new ArrayList<>();
        for (ItemStack stack : stacks) {
            this.clientStorageSnapshot.add(stack.copy());
        }
        this.clientStorageSnapshotSet = true;
        this.refreshStorageView();
    }

    /**
     * Moves the storage slots inside the fixed six-row viewport. Player slots stay
     * at the vanilla coordinates, so expanded inventories do not push them out of
     * the GUI and shift-click ranges remain stable.
     */
    public void setStorageScrollRow(int row) {
        int clamped = Math.max(0, Math.min(row, this.getMaxStorageScrollRow()));
        if (this.storageScrollRow == clamped) {
            return;
        }
        this.storageScrollRow = clamped;
        this.refreshStorageView();
    }

    public void scrollStorage(int delta) {
        this.setStorageScrollRow(this.storageScrollRow + delta);
    }

    public int getPlayerInventoryTop() {
        return PLAYER_INVENTORY_TOP;
    }

    public int getHotbarTop() {
        return HOTBAR_TOP;
    }

    /**
     * Repositions physical slots after a query or sort change.  Slots that do
     * not match the controller query are moved outside the viewport; their
     * contents remain in the controller and can still be revealed by clearing
     * the query.
     */
    public void refreshStorageView() {
        int entryCount = this.clientStorageSnapshotSet ? this.clientStorageSnapshot.size()
                : this.storageController.getVirtualStorageEntries().size();
        this.storageRows = Math.max(1, (entryCount + TileEntityStorageController.COLUMNS - 1)
                / TileEntityStorageController.COLUMNS);
        this.storageScrollRow = Math.max(0, Math.min(this.storageScrollRow, this.getMaxStorageScrollRow()));
        for (int slotIndex = 0; slotIndex < this.storageSize; slotIndex++) {
            Slot slot = this.inventorySlots.get(slotIndex);
            if (!(slot instanceof VirtualStorageSlot) || slotIndex >= VISIBLE_STORAGE_SLOTS) {
                slot.xPos = STORAGE_LEFT;
                slot.yPos = -200;
                continue;
            }
            int virtualIndex = this.storageScrollRow * TileEntityStorageController.COLUMNS + slotIndex;
            int rowIndex = slotIndex / TileEntityStorageController.COLUMNS;
            int column = slotIndex % TileEntityStorageController.COLUMNS;
            VirtualStorageSlot virtualSlot = (VirtualStorageSlot) slot;
            virtualSlot.setVirtualEntryIndex(virtualIndex < entryCount ? virtualIndex : -1);
            if (this.clientStorageSnapshotSet) {
                virtualSlot.setClientSnapshot(virtualIndex < entryCount
                        ? this.clientStorageSnapshot.get(virtualIndex) : ItemStack.EMPTY);
            }
            slot.xPos = STORAGE_LEFT + column * 18;
            slot.yPos = STORAGE_TOP + rowIndex * 18;
        }
    }

    public boolean isStorageSlotVisible(int slotIndex) {
        return slotIndex >= 0 && slotIndex < Math.min(this.storageSize, VISIBLE_STORAGE_SLOTS)
                && this.inventorySlots.get(slotIndex) instanceof VirtualStorageSlot;
    }

    private int storageSlotY(int row) {
        int visibleRow = row - this.storageScrollRow;
        return visibleRow >= 0 && visibleRow < VISIBLE_STORAGE_ROWS
                ? STORAGE_TOP + visibleRow * 18
                : -200;
    }

    /**
     * Packet handlers call this before accepting a setting update.  A direct
     * controller GUI requires the normal 8-block interaction range.  Remote
     * access requires a bound remote in the player's inventory or a nearby
     * loaded stable wormhole bound to the same controller.
     */
    public boolean canUpdateStorageSettings(EntityPlayer playerIn) {
        if (playerIn == null || !this.isStorageControllerLoaded()) {
            return false;
        }
        if (!this.remoteAccess) {
            return this.storageController.isUsableByPlayer(playerIn);
        }
        return this.isRemoteAccessValid(playerIn);
    }

    private boolean isRemoteAccessValid(EntityPlayer playerIn) {
        // Remote links may outlive their target chunk.  Never allow a stale
        // container to mutate a detached TileEntity while the link remains in
        // the player's inventory or in a nearby wormhole.
        if (!this.isStorageControllerLoaded() || playerIn == null) {
            return false;
        }
        if (playerIn.inventory != null) {
            for (int slot = 0; slot < playerIn.inventory.getSizeInventory(); slot++) {
                ItemStack stack = playerIn.inventory.getStackInSlot(slot);
                if (ItemStorageRemote.isLinkedToController(stack, this.storageController.getWorld(),
                        this.storageController.getPos())) {
                    return true;
                }
            }
        }

        if (playerIn.world == null) {
            return false;
        }
        for (TileEntity tile : playerIn.world.loadedTileEntityList) {
            if (!(tile instanceof TileEntityStableWormhole)) {
                continue;
            }
            TileEntityStableWormhole wormhole = (TileEntityStableWormhole) tile;
            BlockPos wormholePos = wormhole.getPos();
            if (wormholePos != null && wormhole.isLinkedToController(this.storageController.getWorld(),
                    this.storageController.getPos())
                    && playerIn.getDistanceSq(wormholePos.getX() + 0.5D, wormholePos.getY() + 0.5D,
                            wormholePos.getZ() + 0.5D) <= 64.0D) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true only while this container still points at the exact loaded
     * controller instance used when it was opened.  The identity check avoids
     * accepting a newly-created tile at the same coordinates after a chunk
     * unload/reload cycle.
     */
    public boolean isStorageControllerLoaded() {
        if (this.storageController == null || this.storageController.isInvalid()
                || this.storageController.getWorld() == null || this.storageController.getPos() == null) {
            return false;
        }
        return this.storageController.getWorld().isBlockLoaded(this.storageController.getPos())
                && this.storageController.getWorld().getTileEntity(this.storageController.getPos())
                        == this.storageController;
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        if (!this.isStorageControllerLoaded()) {
            return false;
        }
        if (this.remoteAccess) {
            return this.isRemoteAccessValid(playerIn);
        }
        return this.storageController.isUsableByPlayer(playerIn);
    }

    @Override
    public void onContainerClosed(EntityPlayer playerIn) {
        if (this.storageController.getWorld() != null && !this.storageController.getWorld().isRemote) {
            // Matrix stacks are returned below.  Clear the terminal copy first so
            // a subsequent open cannot count or display the returned items twice.
            this.storageController.clearCraftingMatrix();
            for (int i = 0; i < this.craftMatrix.getSizeInventory(); i++) {
                ItemStack stack = this.craftMatrix.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    this.returnStackSafely(playerIn, stack.copy());
                    this.craftMatrix.setInventorySlotContents(i, ItemStack.EMPTY);
                }
            }
        }
        super.onContainerClosed(playerIn);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack original = ItemStack.EMPTY;
        if (index < 0 || index >= this.inventorySlots.size()) {
            return ItemStack.EMPTY;
        }
        Slot slot = this.inventorySlots.get(index);

        if (slot == null || !slot.getHasStack()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getStack();
        original = stack.copy();

        if (index < this.storageSize) {
            if (!(slot instanceof VirtualStorageSlot)) {
                return ItemStack.EMPTY;
            }
            int virtualIndex = ((VirtualStorageSlot) slot).getVirtualEntryIndex();
            if (virtualIndex < 0) {
                return ItemStack.EMPTY;
            }

            // Extract through the controller handler first. If the player's
            // inventory cannot accept everything, put the remainder back via
            // the same handler so no aggregate snapshot can duplicate items.
            ItemStack extracted = this.storageController.extractVirtualStack(virtualIndex, stack.getCount(), false);
            if (extracted.isEmpty()) {
                return ItemStack.EMPTY;
            }
            ItemStack remainder = extracted.copy();
            this.mergeItemStack(remainder, this.playerInventoryStart, this.playerSlotEnd, false);
            int accepted = extracted.getCount() - remainder.getCount();
            if (accepted <= 0) {
                this.storageController.insertIntoStorage(extracted, false);
                return ItemStack.EMPTY;
            }
            if (!remainder.isEmpty()) {
                this.storageController.insertIntoStorage(remainder, false);
            }
        } else if (index == this.storageSize) {
            // Shift-clicking the result crafts as many batches as fit in the
            // player's inventory. The method performs all mutations server-side.
            if (this.storageController.getWorld() == null || this.storageController.getWorld().isRemote) {
                return ItemStack.EMPTY;
            }
            this.craftShift(playerIn);
            return ItemStack.EMPTY;
        } else if (index > this.storageSize && index < this.playerInventoryStart) {
            // Crafting ingredients are returned to the player's inventory when
            // shift-clicked; they must never be routed through the aggregate slot.
            if (!this.mergeItemStack(stack, this.playerInventoryStart, this.playerSlotEnd, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.mergeIntoStorage(stack)) {
            if (index >= this.playerInventoryStart && index < this.playerHotbarStart) {
                if (!this.mergeItemStack(stack, this.playerHotbarStart, this.playerSlotEnd, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= this.playerHotbarStart && index < this.playerSlotEnd
                    && !this.mergeItemStack(stack, this.playerInventoryStart, this.playerHotbarStart, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (index < this.storageSize) {
            slot.onSlotChanged();
            slot.onTake(playerIn, original);
            if (playerIn instanceof EntityPlayerMP) {
                com.occultism.occultism.network.ModNetworking.syncStorageStacks((EntityPlayerMP) playerIn,
                        this.storageController);
            }
            return original;
        }

        if (stack.isEmpty()) {
            slot.putStack(ItemStack.EMPTY);
        } else {
            slot.onSlotChanged();
        }

        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(playerIn, stack);
        if (playerIn instanceof EntityPlayerMP) {
            com.occultism.occultism.network.ModNetworking.syncStorageStacks((EntityPlayerMP) playerIn,
                    this.storageController);
        }
        return original;
    }

    /** Called by the result slot after the vanilla click code removed its stack. */
    private ItemStack onCraftingResultTaken(EntityPlayer player, ItemStack clicked) {
        if (this.storageController.getWorld() == null || this.storageController.getWorld().isRemote) {
            return clicked;
        }
        if (player == null || !this.canInteractWith(player) || this.currentRecipe == null
                || !this.currentRecipe.matches(this.craftMatrix, this.storageController.getWorld())) {
            this.updateCraftingResult();
            return ItemStack.EMPTY;
        }
        ItemStack expected = this.currentRecipe.getCraftingResult(this.craftMatrix);
        if (expected.isEmpty() || clicked.isEmpty() || !ItemStack.areItemsEqual(expected, clicked)
                || !ItemStack.areItemStackTagsEqual(expected, clicked)) {
            this.updateCraftingResult();
            return ItemStack.EMPTY;
        }
        ItemStack result = this.craftOne(player);
        return result.isEmpty() ? ItemStack.EMPTY : result;
    }

    /** Performs one recipe operation and refills empty matrix slots from storage. */
    private ItemStack craftOne(EntityPlayer player) {
        IRecipe recipe = this.currentRecipe;
        if (recipe == null || !recipe.matches(this.craftMatrix, this.storageController.getWorld())) {
            return ItemStack.EMPTY;
        }
        ItemStack result = recipe.getCraftingResult(this.craftMatrix);
        if (result == null || result.isEmpty()) {
            return ItemStack.EMPTY;
        }
        List<ItemStack> templates = new ArrayList<>(9);
        for (int i = 0; i < 9; i++) {
            templates.add(this.craftMatrix.getStackInSlot(i).copy());
        }
        NonNullList<ItemStack> remaining = recipe.getRemainingItems(this.craftMatrix);
        boolean ownedRecipeLock = !this.recipeLocked;
        if (ownedRecipeLock) {
            this.recipeLocked = true;
        }
        try {
            for (int i = 0; i < 9; i++) {
                ItemStack current = this.craftMatrix.getStackInSlot(i);
                if (!current.isEmpty()) {
                    this.craftMatrix.decrStackSize(i, 1);
                }
                ItemStack remainder = remaining != null && i < remaining.size() ? remaining.get(i) : ItemStack.EMPTY;
                if (remainder != null && !remainder.isEmpty()) {
                    this.placeRemainder(player, i, remainder.copy());
                }
                if (this.craftMatrix.getStackInSlot(i).isEmpty() && !templates.get(i).isEmpty()) {
                    ItemStack refill = this.extractMatchingFromStorage(templates.get(i), 1);
                    if (!refill.isEmpty()) {
                        this.craftMatrix.setInventorySlotContents(i, refill);
                    }
                }
            }
        } finally {
            if (ownedRecipeLock) {
                this.recipeLocked = false;
            }
        }
        if (ownedRecipeLock) {
            this.syncCraftingMatrixToController();
            this.updateCraftingResult();
            if (player instanceof EntityPlayerMP) {
                com.occultism.occultism.network.ModNetworking.syncStorageStacks((EntityPlayerMP) player,
                        this.storageController);
            }
        }
        return result.copy();
    }

    private void placeRemainder(EntityPlayer player, int matrixSlot, ItemStack remainder) {
        ItemStack current = this.craftMatrix.getStackInSlot(matrixSlot);
        if (current.isEmpty()) {
            this.craftMatrix.setInventorySlotContents(matrixSlot, remainder);
            return;
        }
        if (ItemStack.areItemsEqual(current, remainder) && ItemStack.areItemStackTagsEqual(current, remainder)) {
            int limit = Math.min(this.storageController.getStackLimit(current), current.getMaxStackSize());
            int accepted = Math.min(remainder.getCount(), Math.max(0, limit - current.getCount()));
            if (accepted > 0) {
                current.grow(accepted);
                remainder.shrink(accepted);
            }
        }
        if (!remainder.isEmpty()) {
            this.returnStackSafely(player, remainder);
        }
    }

    private ItemStack extractMatchingFromStorage(ItemStack template, int amount) {
        if (template == null || template.isEmpty() || amount <= 0) {
            return ItemStack.EMPTY;
        }
        List<StorageVirtualEntry> entries = this.storageController.getVirtualStorageEntries("", null, null);
        for (StorageVirtualEntry entry : entries) {
            ItemStack candidate = entry.getStack();
            if (ItemStack.areItemsEqual(template, candidate) && ItemStack.areItemStackTagsEqual(template, candidate)) {
                return this.storageController.extractVirtualStack(entry, amount, false);
            }
        }
        return ItemStack.EMPTY;
    }

    private void returnStackSafely(EntityPlayer player, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        ItemStack remainder = this.storageController.insertIntoStorage(stack, false);
        if (!remainder.isEmpty() && player != null) {
            net.minecraftforge.items.ItemHandlerHelper.giveItemToPlayer(player, remainder);
        }
    }

    private void craftShift(EntityPlayer player) {
        if (player == null || this.storageController.getWorld() == null || this.storageController.getWorld().isRemote) {
            return;
        }
        List<ItemStack> crafted = new ArrayList<>();
        boolean previousRecipeLock = this.recipeLocked;
        this.recipeLocked = true;
        try {
            ItemStack initialOutput = this.craftResult.getStackInSlot(0);
            int resultUnitCount = Math.max(1, initialOutput.getCount());
            int resultLimit = Math.max(resultUnitCount, initialOutput.getMaxStackSize());
            int count = 0;
            while (count + resultUnitCount <= resultLimit && this.currentRecipe != null) {
                ItemStack output = this.craftResult.getStackInSlot(0);
                if (output.isEmpty() || output.getCount() != resultUnitCount
                        || !ItemStack.areItemsEqual(initialOutput, output)
                        || !ItemStack.areItemStackTagsEqual(initialOutput, output)) {
                    break;
                }
                ItemStack simulated = output.copy();
                if (!net.minecraftforge.items.ItemHandlerHelper.insertItemStacked(
                        new net.minecraftforge.items.wrapper.PlayerMainInvWrapper(this.playerInventory), simulated,
                        true).isEmpty()) {
                    break;
                }
                ItemStack made = this.craftOne(player);
                if (made.isEmpty()) {
                    break;
                }
                crafted.add(made);
                count += made.getCount();
            }
            ItemStack finalResult = ItemStack.EMPTY;
            for (ItemStack made : crafted) {
                if (finalResult.isEmpty()) {
                    finalResult = made.copy();
                    continue;
                }
                if (ItemStack.areItemsEqual(finalResult, made)
                        && ItemStack.areItemStackTagsEqual(finalResult, made)) {
                    finalResult.grow(made.getCount());
                } else {
                    this.returnStackSafely(player, made);
                }
            }
            if (!finalResult.isEmpty()) {
                net.minecraftforge.items.ItemHandlerHelper.giveItemToPlayer(player, finalResult);
            }
        } finally {
            this.recipeLocked = previousRecipeLock;
        }
        if (!this.recipeLocked) {
            this.syncCraftingMatrixToController();
            this.updateCraftingResult();
            if (player instanceof EntityPlayerMP) {
                com.occultism.occultism.network.ModNetworking.syncStorageStacks((EntityPlayerMP) player,
                        this.storageController);
            }
        }
    }

    /**
     * Container.mergeItemStack additionally clamps to ItemStack#getMaxStackSize
     * (64 in vanilla), so use the controller handler for the player-to-storage
     * path and preserve the configured 1024-item stack limit.
     */
    private boolean mergeIntoStorage(ItemStack stack) {
        int before = stack.getCount();
        ItemStack remaining = this.storageController.insertIntoStorage(stack, false);
        stack.setCount(remaining.getCount());
        return stack.getCount() < before;
    }

    private static final class StorageCraftingResultSlot extends SlotCrafting {

        private final ContainerStorageController container;

        private StorageCraftingResultSlot(EntityPlayer player, InventoryCrafting matrix, InventoryCraftResult result,
                int index, int xPosition, int yPosition) {
            super(player, matrix, result, index, xPosition, yPosition);
            this.container = (ContainerStorageController) matrix.eventHandler;
        }

        @Override
        public boolean canTakeStack(EntityPlayer playerIn) {
            return this.getHasStack() && this.container.canInteractWith(playerIn)
                    && this.container.getCurrentRecipe() != null;
        }

        @Override
        public ItemStack onTake(EntityPlayer playerIn, ItemStack stack) {
            // Preserve vanilla craft events and recipe unlocking while the
            // container performs its storage-aware matrix update.
            this.onCrafting(stack);
            return this.container.onCraftingResultTaken(playerIn, stack);
        }
    }

    private static final class VirtualStorageSlot extends Slot {

        private final TileEntityStorageController storageController;
        private int virtualEntryIndex = -1;
        private ItemStack clientSnapshot = ItemStack.EMPTY;
        private boolean clientSnapshotSet;

        private VirtualStorageSlot(TileEntityStorageController storageController, int index, int xPosition,
                int yPosition) {
            super(storageController, index, xPosition, yPosition);
            this.storageController = storageController;
        }

        private void setVirtualEntryIndex(int virtualEntryIndex) {
            this.virtualEntryIndex = virtualEntryIndex;
            this.clientSnapshot = ItemStack.EMPTY;
            this.clientSnapshotSet = false;
        }

        private void setClientSnapshot(ItemStack stack) {
            this.clientSnapshot = stack == null ? ItemStack.EMPTY : stack.copy();
            this.clientSnapshotSet = true;
        }

        private int getVirtualEntryIndex() {
            return this.virtualEntryIndex;
        }

        @Override
        public ItemStack getStack() {
            if (this.storageController.getWorld() != null && this.storageController.getWorld().isRemote
                    && this.clientSnapshotSet) {
                return this.clientSnapshot.copy();
            }
            StorageVirtualEntry entry = this.storageController.getVirtualStorageEntry(this.virtualEntryIndex);
            return entry == null ? ItemStack.EMPTY : entry.getStack();
        }

        @Override
        public ItemStack decrStackSize(int amount) {
            if (this.storageController.getWorld() != null && this.storageController.getWorld().isRemote) {
                if (this.clientSnapshotSet) {
                    if (this.clientSnapshot.isEmpty() || amount <= 0) {
                        return ItemStack.EMPTY;
                    }
                    int extracted = Math.min(amount, this.clientSnapshot.getCount());
                    ItemStack result = this.clientSnapshot.copy();
                    result.setCount(extracted);
                    this.clientSnapshot.shrink(extracted);
                    return result;
                }
                if (amount <= 0) {
                    return ItemStack.EMPTY;
                }
                return this.virtualEntryIndex < 0 ? ItemStack.EMPTY
                        : this.storageController.extractVirtualStack(this.virtualEntryIndex, amount, false);
            }
            return this.virtualEntryIndex < 0 ? ItemStack.EMPTY
                    : this.storageController.extractVirtualStack(this.virtualEntryIndex, amount, false);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            // Direct placement into an aggregate slot cannot safely infer a
            // physical target; shift-click insertion uses the real handler.
            return false;
        }

        @Override
        public boolean canTakeStack(EntityPlayer playerIn) {
            return this.virtualEntryIndex >= 0 && !this.getStack().isEmpty();
        }

        @Override
        public void putStack(ItemStack stack) {
            // Window packets update this client-only copy. Never write a server
            // aggregate snapshot back into physical storage.
            if (this.storageController.getWorld() != null && this.storageController.getWorld().isRemote) {
                this.clientSnapshot = stack == null ? ItemStack.EMPTY : stack.copy();
                this.clientSnapshotSet = true;
            }
        }

        @Override
        public int getSlotStackLimit() {
            return this.storageController.getStorageStackLimit();
        }

        @Override
        public int getItemStackLimit(ItemStack stack) {
            return this.storageController.getStackLimit(stack);
        }
    }
}
