package com.occultism.occultism.tileentity;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

import com.occultism.occultism.api.common.blockentity.IStorageController;
import com.occultism.occultism.api.common.blockentity.IStorageControllerProxy;
import com.occultism.occultism.api.common.data.GlobalBlockPos;
import com.occultism.occultism.entity.EntityFoliotJob;
import com.occultism.occultism.network.MessageUpdateStacks;
import com.occultism.occultism.registry.ModBlocks;
import com.occultism.occultism.registry.ModItems;
import com.occultism.occultism.config.ModConfig;
import com.occultism.occultism.block.BlockStorageController;
import com.occultism.occultism.block.BlockStorageStabilizer;
import com.occultism.occultism.spirit.DepositOrder;
import com.occultism.occultism.spirit.MachineReference;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

public class TileEntityStorageController extends TileEntity
        implements IInventory, ITickable, IStorageController, IStorageControllerProxy {

    public static final int ROWS = 6;
    public static final int COLUMNS = 9;
    /** The capacity of a newly placed controller in the upstream storage model. */
    public static final int BASE_SLOT_COUNT = 128;
    /** Capacity used when loading a pre-dynamic 1.12 controller NBT payload. */
    public static final int LEGACY_SLOT_COUNT = ROWS * COLUMNS;
    /** Kept as a source-compatible alias for integrations that used SLOT_COUNT. */
    public static final int SLOT_COUNT = BASE_SLOT_COUNT;
    public static final int MAX_STABILIZER_DISTANCE = 5;
    public static final String TAG_STORAGE_SLOT_COUNT = "StorageSlotCount";
    public static final String TAG_BLOCK_ENTITY = "BlockEntityTag";
    public static final String TAG_ITEMS = "Items";
    /** Integer count companion for ItemStack's legacy byte Count field. */
    public static final String TAG_REAL_SIZE = "RealSize";
    /** Upstream-compatible persisted terminal crafting matrix and order slot. */
    public static final String TAG_CRAFTING_MATRIX = "matrix";
    public static final String TAG_MATRIX_SLOT = "slot";
    public static final String TAG_ORDER_STACK = "orderStack";
    public static final int CRAFTING_MATRIX_SIZE = 9;
    /** Upstream-compatible persistent storage terminal settings. */
    public static final String TAG_SORT_TYPE = "sortType";
    public static final String TAG_SORT_DIRECTION = "sortDirection";
    public static final String TAG_SEARCH_QUERY = "searchQuery";
    /** Upstream storage-controller linked-machine registry (runtime + NBT snapshot). */
    public static final String TAG_LINKED_MACHINES = "linkedMachines";
    public static final String TAG_LINKED_MACHINE = "machine";
    /** Prevent a malformed save or packet from turning this into an unbounded map. */
    public static final int MAX_LINKED_MACHINES = 128;

    /*
     * The backing list is intentionally never shrunk. A removed stabilizer must not
     * destroy items in now-hidden slots; those stacks become available again when the
     * controller is expanded later.
     */
    private NonNullList<ItemStack> stacks = NonNullList.withSize(configuredBaseSlotCount(), ItemStack.EMPTY);
    private int storageSlotCount = configuredBaseSlotCount();
    private StorageSortType sortType = StorageSortType.AMOUNT;
    private StorageSortDirection sortDirection = StorageSortDirection.DOWN;
    private String searchQuery = "";
    /**
     * The matrix is terminal state, not part of the physical storage handler.
     * It is copied into an open container and persisted so a reopened terminal
     * retains the selected recipe just like the upstream controller.
     */
    private NonNullList<ItemStack> craftingMatrix = NonNullList.withSize(CRAFTING_MATRIX_SIZE, ItemStack.EMPTY);
    /** Reserved upstream order slot.  The 1.12 port exposes persistence/API only. */
    private ItemStack orderStack = ItemStack.EMPTY;
    /**
     * Machine-manager references currently registered with this controller. The
     * map key includes dimension and insert position, matching upstream's
     * GlobalBlockPos key while remaining usable with the 1.12 data model.
     */
    private final Map<String, MachineReference> linkedMachines = new LinkedHashMap<>();
    /** Runtime-only route from a linked machine endpoint to its manager spirit. */
    private final Map<String, UUID> depositOrderSpirits = new LinkedHashMap<>();
    /** InvWrapper caps inserts at 64 through IInventory; the controller uses a custom handler. */
    private final IItemHandlerModifiable itemHandler = new StorageControllerItemHandler(this);
    /**
     * Bounded server-to-client virtual storage snapshot.  The message owns its
     * compressed payload, so it can be reused until physical contents or the
     * active capacity changes.  Keeping this at the controller (rather than in
     * a container) also makes direct, remote and proxy-backed sends agree on the
     * same snapshot boundary.
     */
    @Nullable
    private transient MessageUpdateStacks cachedMessageUpdateStacks;

    @Override
    public void update() {
        if (this.world != null && !this.world.isRemote && this.world.getTotalWorldTime() % 20L == 0L) {
            this.updateStabilizers();
            this.pruneLinkedMachines();
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger(TAG_STORAGE_SLOT_COUNT, this.storageSlotCount);
        this.writeStorageSettings(compound);
        this.writeCraftingState(compound);
        this.writeLinkedMachines(compound);
        this.saveItems(compound, true);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        int requiredSlots = getRequiredItemListSize(compound);
        this.storageSlotCount = Math.max(this.readStorageSlotCount(compound, LEGACY_SLOT_COUNT), requiredSlots);
        this.readStorageSettings(compound);
        this.readCraftingState(compound);
        this.readLinkedMachines(compound);
        this.stacks = NonNullList.withSize(this.storageSlotCount, ItemStack.EMPTY);
        this.loadItems(compound);
        this.invalidateMessageUpdateStacks();
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return this.writeToNBT(new NBTTagCompound());
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.pos, 0, this.getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        this.readFromNBT(packet.getNbtCompound());
    }

    public NBTTagCompound writeInventoryToNBT(NBTTagCompound compound) {
        compound.setInteger(TAG_STORAGE_SLOT_COUNT, this.storageSlotCount);
        this.writeStorageSettings(compound);
        this.writeCraftingState(compound);
        this.saveItems(compound, true);
        return compound;
    }

    public void readInventoryFromNBT(NBTTagCompound compound) {
        int requiredSlots = getRequiredItemListSize(compound);
        this.storageSlotCount = Math.max(this.readStorageSlotCount(compound, LEGACY_SLOT_COUNT), requiredSlots);
        this.readStorageSettings(compound);
        this.readCraftingState(compound);
        this.stacks = NonNullList.withSize(this.storageSlotCount, ItemStack.EMPTY);
        this.loadItems(compound);
        this.invalidateMessageUpdateStacks();
        this.markDirty();
    }

    public void readInventoryFromStack(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null && tag.hasKey(TAG_BLOCK_ENTITY, 10)) {
            this.readInventoryFromNBT(tag.getCompoundTag(TAG_BLOCK_ENTITY));
        }
    }

    public static ItemStack writeInventoryToStack(ItemStack stack, TileEntityStorageController storageController) {
        if (storageController.isEmpty() && !storageController.hasPersistedCraftingState()) {
            return stack;
        }
        NBTTagCompound stackTag = stack.getTagCompound();
        if (stackTag == null) {
            stackTag = new NBTTagCompound();
            stack.setTagCompound(stackTag);
        }
        stackTag.setTag(TAG_BLOCK_ENTITY, storageController.writeInventoryToNBT(new NBTTagCompound()));
        return stack;
    }

    public static int getStoredItemCount(ItemStack stack) {
        NBTTagCompound stackTag = stack.getTagCompound();
        if (stackTag == null || !stackTag.hasKey(TAG_BLOCK_ENTITY, 10)) {
            return 0;
        }
        NBTTagCompound blockEntityTag = stackTag.getCompoundTag(TAG_BLOCK_ENTITY);
        int slotCount = readStorageSlotCount(blockEntityTag, LEGACY_SLOT_COUNT);
        NonNullList<ItemStack> contents = NonNullList.withSize(Math.max(slotCount, getRequiredItemListSize(blockEntityTag)),
                ItemStack.EMPTY);
        loadItems(blockEntityTag, contents);
        int count = 0;
        for (ItemStack stored : contents) {
            if (!stored.isEmpty()) {
                count += stored.getCount();
            }
        }
        return count;
    }

    /** True when a portable controller must retain terminal-only state as well as physical items. */
    public boolean hasPersistedCraftingState() {
        if (this.orderStack != null && !this.orderStack.isEmpty()) {
            return true;
        }
        for (ItemStack stack : this.craftingMatrix) {
            if (stack != null && !stack.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /** Returns a defensive copy of the persisted 3x3 terminal crafting matrix. */
    public List<ItemStack> getCraftingMatrix() {
        List<ItemStack> result = new ArrayList<>(CRAFTING_MATRIX_SIZE);
        for (ItemStack stack : this.craftingMatrix) {
            result.add(stack == null ? ItemStack.EMPTY : stack.copy());
        }
        return result;
    }

    /** Returns one persisted matrix slot, or an empty stack for an invalid slot. */
    public ItemStack getCraftingMatrixSlot(int slot) {
        return slot >= 0 && slot < CRAFTING_MATRIX_SIZE ? this.craftingMatrix.get(slot).copy() : ItemStack.EMPTY;
    }

    /**
     * Replaces the persisted matrix with defensive copies.  Matrix stacks are
     * deliberately excluded from the ITEM_HANDLER capability; they belong to
     * the terminal UI and are only counted when a container consumes them.
     */
    public void setCraftingMatrix(List<ItemStack> matrix) {
        NonNullList<ItemStack> replacement = NonNullList.withSize(CRAFTING_MATRIX_SIZE, ItemStack.EMPTY);
        if (matrix != null) {
            int limit = Math.min(CRAFTING_MATRIX_SIZE, matrix.size());
            for (int slot = 0; slot < limit; slot++) {
                ItemStack stack = matrix.get(slot);
                replacement.set(slot, stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
            }
        }
        this.craftingMatrix = replacement;
        this.markDirty();
    }

    /** Updates one persisted matrix slot without exposing the backing stack. */
    public void setCraftingMatrixSlot(int slot, @Nullable ItemStack stack) {
        if (slot < 0 || slot >= CRAFTING_MATRIX_SIZE) {
            return;
        }
        this.craftingMatrix.set(slot, stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
        this.markDirty();
    }

    /** Clears the persisted matrix after an open container safely returns it. */
    public void clearCraftingMatrix() {
        this.craftingMatrix = NonNullList.withSize(CRAFTING_MATRIX_SIZE, ItemStack.EMPTY);
        this.markDirty();
    }

    /** Returns a defensive copy of the reserved upstream order slot. */
    public ItemStack getOrderStack() {
        return this.orderStack.copy();
    }

    /** Stores the reserved upstream order slot; no order routing is implied. */
    public void setOrderStack(@Nullable ItemStack stack) {
        this.orderStack = stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
        this.markDirty();
    }

    /** Clears the reserved upstream order slot. */
    public void clearOrderStack() {
        this.orderStack = ItemStack.EMPTY;
        this.markDirty();
    }

    /**
     * Returns defensive copies of the machine references registered with this
     * controller. A list is used in the 1.12 API because the upstream map key is
     * GlobalBlockPos, which has no direct equivalent in this branch.
     */
    public List<MachineReference> getLinkedMachines() {
        List<MachineReference> result = new ArrayList<>(this.linkedMachines.size());
        for (MachineReference reference : this.linkedMachines.values()) {
            result.add(copyMachineReference(reference));
        }
        return result;
    }

    /** Returns whether a machine with the same insert endpoint is registered. */
    public boolean isMachineLinked(@Nullable MachineReference reference) {
        String key = linkedMachineKey(reference);
        return key != null && this.linkedMachines.containsKey(key);
    }

    /**
     * Registers a strict machine reference. Invalid references and duplicate
     * insert endpoints are ignored; replacing a duplicate refreshes its faces,
     * output endpoint and custom name.
     */
    public boolean linkMachine(@Nullable MachineReference reference) {
        if (reference == null || !reference.isStrictlyConfigured()) {
            return false;
        }
        String key = linkedMachineKey(reference);
        if (key == null) {
            return false;
        }
        if (!this.linkedMachines.containsKey(key) && this.linkedMachines.size() >= MAX_LINKED_MACHINES) {
            return false;
        }
        MachineReference copy = copyMachineReference(reference);
        MachineReference old = this.linkedMachines.put(key, copy);
        if (old == null || !sameMachineReference(old, copy)) {
            this.markDirty();
            this.syncLinkedMachineUpdate();
            return true;
        }
        return false;
    }

    /** Removes a registered machine by its insert endpoint. */
    public boolean unlinkMachine(@Nullable MachineReference reference) {
        String key = linkedMachineKey(reference);
        if (key == null || this.linkedMachines.remove(key) == null) {
            return false;
        }
        this.depositOrderSpirits.remove(key);
        this.markDirty();
        this.syncLinkedMachineUpdate();
        return true;
    }

    /** Registers the spirit that should receive orders for a linked machine. */
    public boolean registerMachineManager(@Nullable MachineReference reference, @Nullable UUID spiritId) {
        if (reference == null || spiritId == null || !reference.isStrictlyConfigured()
                || (this.world != null && (this.world.isRemote || !reference.isValidFor(this.world)))) {
            return false;
        }
        String key = linkedMachineKey(reference);
        if (key == null) {
            return false;
        }
        this.linkMachine(reference);
        this.depositOrderSpirits.put(key, spiritId);
        return true;
    }

    /** Removes the spirit route while retaining no stale linked-machine entry. */
    public boolean unregisterMachineManager(@Nullable MachineReference reference, @Nullable UUID spiritId) {
        String key = linkedMachineKey(reference);
        if (key == null) {
            return false;
        }
        UUID registered = this.depositOrderSpirits.get(key);
        if (spiritId != null && registered != null && !spiritId.equals(registered)) {
            return false;
        }
        this.depositOrderSpirits.remove(key);
        return this.linkedMachines.remove(key) != null;
    }

    /**
     * Delivers an order to the registered manager spirit on this server. The
     * method never loads another dimension or chunk and removes dead routes.
     */
    public boolean addDepositOrder(@Nullable MachineReference reference, @Nullable ItemStack template, int amount) {
        String key = linkedMachineKey(reference);
        if (key == null || template == null || template.isEmpty() || amount <= 0
                || this.world == null || this.world.isRemote || !(this.world instanceof WorldServer)) {
            return false;
        }
        // Match the upstream controller contract: an order is only dispatched
        // when at least one matching stack is currently available.  The spirit
        // may still receive a partial order when the requested amount exceeds
        // the available amount; this preserves the upstream refill behaviour.
        ItemStack available = this.getItemStack(template, amount, true);
        if (available.isEmpty()) {
            return false;
        }
        UUID spiritId = this.depositOrderSpirits.get(key);
        if (spiritId == null) {
            return false;
        }
        Entity entity = ((WorldServer) this.world).getEntityFromUuid(spiritId);
        if (!(entity instanceof EntityFoliotJob)) {
            this.unregisterMachineManager(reference, spiritId);
            return false;
        }
        EntityFoliotJob spirit = (EntityFoliotJob) entity;
        if (this.pos == null || spirit.getStorageControllerPos() == null || !this.pos.equals(spirit.getStorageControllerPos())
                || spirit.getStorageControllerDimension() != this.world.provider.getDimension()
                || !spirit.isMachineReferenceFor(reference)) {
            this.unregisterMachineManager(reference, spiritId);
            return false;
        }
        spirit.addDepositOrder(new DepositOrder(template, amount));
        return true;
    }

    /**
     * 1.12 equivalent of the upstream IStorageController#getItemStack.  It
     * extracts up to {@code requestedSize} matching stacks while preserving the
     * first stack's item/tag identity; simulation never mutates the controller.
     */
    public ItemStack getItemStack(@Nullable ItemStack template, int requestedSize, boolean simulate) {
        if (template == null || template.isEmpty() || requestedSize <= 0) {
            return ItemStack.EMPTY;
        }
        IItemHandler handler = this.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        ItemStack firstMatched = ItemStack.EMPTY;
        int remaining = requestedSize;
        for (int slot = 0; slot < handler.getSlots() && remaining > 0; slot++) {
            ItemStack candidate = handler.extractItem(slot, remaining, true);
            if (candidate.isEmpty() || this.isBlacklisted(candidate)) {
                continue;
            }
            if (firstMatched.isEmpty()) {
                if (!ItemHandlerHelper.canItemStacksStack(template, candidate)) {
                    continue;
                }
                firstMatched = candidate.copy();
                firstMatched.setCount(0);
            } else if (!ItemHandlerHelper.canItemStacksStack(firstMatched, candidate)) {
                continue;
            }
            int toExtract = Math.min(candidate.getCount(), remaining);
            ItemStack extracted = handler.extractItem(slot, toExtract, simulate);
            if (extracted.isEmpty()) {
                continue;
            }
            if (firstMatched.getCount() == 0) {
                firstMatched = extracted.copy();
                firstMatched.setCount(extracted.getCount());
            } else {
                firstMatched.grow(extracted.getCount());
            }
            remaining -= extracted.getCount();
        }
        return firstMatched.isEmpty() ? ItemStack.EMPTY : firstMatched;
    }

    public int getDepositOrderRouteCount() {
        return this.depositOrderSpirits.size();
    }

    /** Removes all stale entries and returns how many were removed. */
    public int pruneLinkedMachines() {
        int removed = 0;
        java.util.Iterator<Map.Entry<String, MachineReference>> iterator = this.linkedMachines.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, MachineReference> entry = iterator.next();
            if (entry.getValue() == null || !entry.getValue().isStrictlyConfigured()
                    || linkedMachineKey(entry.getValue()) == null
                    || (this.world != null && !this.world.isRemote && !entry.getValue().isValidFor(this.world))) {
                iterator.remove();
                this.depositOrderSpirits.remove(entry.getKey());
                removed++;
            }
        }
        if (removed > 0) {
            this.markDirty();
            this.syncLinkedMachineUpdate();
        }
        return removed;
    }

    /** Compatibility alias matching the upstream controller method name. */
    public void setLinkedMachines(List<MachineReference> machines) {
        this.linkedMachines.clear();
        this.depositOrderSpirits.clear();
        if (machines != null) {
            int limit = Math.min(MAX_LINKED_MACHINES, machines.size());
            for (int index = 0; index < limit; index++) {
                MachineReference machine = machines.get(index);
                String key = linkedMachineKey(machine);
                if (key != null && machine.isStrictlyConfigured()) {
                    this.linkedMachines.put(key, copyMachineReference(machine));
                }
            }
        }
        this.markDirty();
        this.syncLinkedMachineUpdate();
    }

    public int getLinkedMachineCount() {
        return this.linkedMachines.size();
    }

    private static String linkedMachineKey(@Nullable MachineReference reference) {
        if (reference == null || reference.getInsertPosition() == null || !reference.hasInsertEndpoint()) {
            return null;
        }
        BlockPos position = reference.getInsertPosition();
        return reference.getInsertDimension() + ":" + position.getX() + ":" + position.getY() + ":"
                + position.getZ();
    }

    private static MachineReference copyMachineReference(MachineReference reference) {
        return MachineReference.from(reference.writeToNBT(new NBTTagCompound()));
    }

    private static boolean sameMachineReference(MachineReference left, MachineReference right) {
        return sameEndpoint(left.getInsertPosition(), right.getInsertPosition())
                && left.getInsertDimension() == right.getInsertDimension()
                && left.getInsertFacing() == right.getInsertFacing()
                && sameEndpoint(left.getExtractPosition(), right.getExtractPosition())
                && left.getExtractDimension() == right.getExtractDimension()
                && left.getExtractFacing() == right.getExtractFacing()
                && sameString(left.getInsertRegistryName(), right.getInsertRegistryName())
                && sameString(left.getExtractRegistryName(), right.getExtractRegistryName())
                && sameString(left.getCustomName(), right.getCustomName());
    }

    private static boolean sameEndpoint(@Nullable BlockPos left, @Nullable BlockPos right) {
        return left == null ? right == null : left.equals(right);
    }

    private static boolean sameString(@Nullable String left, @Nullable String right) {
        return left == null ? right == null : left.equals(right);
    }

    /* Package-visible for persistence contract tests; writeToNBT remains the runtime entry point. */
    void writeLinkedMachines(NBTTagCompound compound) {
        NBTTagList list = new NBTTagList();
        int count = 0;
        for (MachineReference reference : this.linkedMachines.values()) {
            if (count >= MAX_LINKED_MACHINES || reference == null || !reference.isStrictlyConfigured()) {
                break;
            }
            NBTTagCompound entry = new NBTTagCompound();
            entry.setTag(TAG_LINKED_MACHINE, reference.writeToNBT(new NBTTagCompound()));
            list.appendTag(entry);
            count++;
        }
        compound.setTag(TAG_LINKED_MACHINES, list);
    }

    /* Package-visible for persistence contract tests; readFromNBT remains the runtime entry point. */
    void readLinkedMachines(NBTTagCompound compound) {
        this.linkedMachines.clear();
        this.depositOrderSpirits.clear();
        if (compound == null || !compound.hasKey(TAG_LINKED_MACHINES, 9)) {
            return;
        }
        NBTTagList list = compound.getTagList(TAG_LINKED_MACHINES, 10);
        for (int index = 0; index < list.tagCount() && index < MAX_LINKED_MACHINES; index++) {
            NBTTagCompound entry = list.getCompoundTagAt(index);
            if (!entry.hasKey(TAG_LINKED_MACHINE, 10)) {
                continue;
            }
            try {
                MachineReference reference = MachineReference.from(entry.getCompoundTag(TAG_LINKED_MACHINE));
                String key = linkedMachineKey(reference);
                if (key != null && reference.isStrictlyConfigured()) {
                    this.linkedMachines.put(key, reference);
                }
            } catch (RuntimeException ignored) {
                // Ignore malformed entries while preserving valid registrations.
            }
        }
    }

    private void syncLinkedMachineUpdate() {
        if (this.world != null && !this.world.isRemote && this.pos != null) {
            IBlockState state = this.world.getBlockState(this.pos);
            this.world.notifyBlockUpdate(this.pos, state, state, 3);
        }
    }

    @Override
    public String getName() {
        return "container.occultism.storage_controller";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentTranslation(this.getName());
    }

    @Override
    public int getSizeInventory() {
        return this.storageSlotCount;
    }

    /** Returns the currently available (visible to handlers and containers) slots. */
    public int getStorageSlotCount() {
        return this.storageSlotCount;
    }

    /** Alias matching the terminology used by the upstream storage controller. */
    public int getMaxStorageSlots() {
        return this.storageSlotCount;
    }

    /**
     * Updates active capacity without truncating the backing list. Hidden stacks are
     * still serialized and become accessible when capacity grows again.
     */
    public void setStorageSlotCount(int slotCount) {
        int normalized = Math.max(0, slotCount);
        if (normalized > this.stacks.size()) {
            NonNullList<ItemStack> expanded = NonNullList.withSize(normalized, ItemStack.EMPTY);
            for (int slot = 0; slot < this.stacks.size(); slot++) {
                expanded.set(slot, this.stacks.get(slot));
            }
            this.stacks = expanded;
        }
        if (this.storageSlotCount != normalized) {
            this.storageSlotCount = normalized;
            this.markDirty();
        }
    }

    /** Compatibility alias for callers using the upstream name. */
    public void setMaxStorageSlots(int slotCount) {
        this.setStorageSlotCount(slotCount);
    }

    /** Returns the key used to order the terminal's virtual item list. */
    public StorageSortType getSortType() {
        return this.sortType;
    }

    public void setSortType(@Nullable StorageSortType sortType) {
        StorageSortType normalized = sortType == null ? StorageSortType.AMOUNT : sortType;
        if (this.sortType != normalized) {
            this.sortType = normalized;
            this.markDirty();
        }
    }

    /** Returns the direction used to order the terminal's virtual item list. */
    public StorageSortDirection getSortDirection() {
        return this.sortDirection;
    }

    public void setSortDirection(@Nullable StorageSortDirection sortDirection) {
        StorageSortDirection normalized = sortDirection == null ? StorageSortDirection.DOWN : sortDirection;
        if (this.sortDirection != normalized) {
            this.sortDirection = normalized;
            this.markDirty();
        }
    }

    /**
     * Returns the current case-insensitive terminal query. The query is persisted
     * with the controller so a reopened terminal starts in the same view.
     */
    public String getSearchQuery() {
        return this.searchQuery;
    }

    public void setSearchQuery(@Nullable String query) {
        String normalized = normalizeSearchQuery(query);
        if (!this.searchQuery.equals(normalized)) {
            this.searchQuery = normalized;
            this.markDirty();
        }
    }

    /** Returns whether a physical stack should remain visible for the current query. */
    public boolean matchesSearchQuery(@Nullable ItemStack stack) {
        // Empty slots remain interactive so the terminal can accept inserts;
        // only populated slots are filtered by the query.
        return stack == null || stack.isEmpty() || matchesSearch(stack, this.searchQuery);
    }

    /**
     * Persists a terminal setting change and sends the updated tile state to
     * clients watching this controller.  The regular setters intentionally only
     * mark the tile dirty so local inventory operations do not emit a packet for
     * every stack mutation.
     */
    public void markSettingsDirtyAndSync() {
        this.markDirty();
        if (this.world != null && !this.world.isRemote && this.pos != null) {
            IBlockState state = this.world.getBlockState(this.pos);
            this.world.notifyBlockUpdate(this.pos, state, state, 3);
        }
    }

    /**
     * Builds an aggregated, filtered and sorted snapshot without mutating the
     * physical slots. This is the 1.12 equivalent of the upstream controller's
     * virtual storage list and is safe to use for client rendering.
     */
    public List<ItemStack> getSortedStacks() {
        return this.getSortedStacks(this.searchQuery, this.sortType, this.sortDirection);
    }

    public List<ItemStack> getSortedStacks(@Nullable String query, @Nullable StorageSortType type,
            @Nullable StorageSortDirection direction) {
        List<ItemStack> result = new ArrayList<>();
        for (StorageVirtualEntry entry : this.getVirtualStorageEntries(query, type, direction)) {
            result.add(entry.getStack());
        }
        return result;
    }

    /** Returns the current query/sort view with physical handler mappings. */
    public List<StorageVirtualEntry> getVirtualStorageEntries() {
        return this.getVirtualStorageEntries(this.searchQuery, this.sortType, this.sortDirection);
    }

    /**
     * Aggregates compatible physical stacks, filters the aggregate view, and
     * sorts only the returned entries. Physical storage order is unchanged.
     */
    public List<StorageVirtualEntry> getVirtualStorageEntries(@Nullable String query,
            @Nullable StorageSortType type, @Nullable StorageSortDirection direction) {
        String normalizedQuery = normalizeSearchQuery(query);
        StorageSortType normalizedType = type == null ? StorageSortType.AMOUNT : type;
        StorageSortDirection normalizedDirection = direction == null ? StorageSortDirection.DOWN : direction;
        List<StorageVirtualEntry> result = new ArrayList<>();
        for (int slot = 0; slot < this.storageSlotCount; slot++) {
            ItemStack stored = this.stacks.get(slot);
            if (stored.isEmpty() || this.isBlacklisted(stored) || !matchesSearch(stored, normalizedQuery)) {
                continue;
            }
            StorageVirtualEntry merged = null;
            for (StorageVirtualEntry entry : result) {
                if (ItemHandlerHelper.canItemStacksStack(entry.peekStack(), stored)) {
                    merged = entry;
                    break;
                }
            }
            if (merged == null) {
                result.add(new StorageVirtualEntry(stored, slot));
            } else {
                merged.merge(stored, slot);
            }
        }
        Comparator<ItemStack> comparator = createComparator(normalizedType, normalizedDirection);
        result.sort((left, right) -> comparator.compare(left.peekStack(), right.peekStack()));
        return result;
    }

    /** Returns one current virtual entry, or {@code null} for an invalid index. */
    @Nullable
    public StorageVirtualEntry getVirtualStorageEntry(int virtualIndex) {
        List<StorageVirtualEntry> entries = this.getVirtualStorageEntries();
        return virtualIndex >= 0 && virtualIndex < entries.size() ? entries.get(virtualIndex) : null;
    }

    public int getVirtualStorageEntryCount() {
        return this.getVirtualStorageEntries().size();
    }

    /**
     * Extracts from an aggregate entry by walking its physical slots through the
     * real handler.  A stale entry is harmless: changed or incompatible slots
     * are skipped and the remaining matching slots are considered.
     */
    public ItemStack extractVirtualStack(int virtualIndex, int amount, boolean simulate) {
        if (amount <= 0) {
            return ItemStack.EMPTY;
        }
        StorageVirtualEntry entry = this.getVirtualStorageEntry(virtualIndex);
        if (entry == null) {
            return ItemStack.EMPTY;
        }
        return this.extractVirtualStack(entry, amount, simulate);
    }

    /** Extracts from a previously captured entry using its retained mapping. */
    public ItemStack extractVirtualStack(@Nullable StorageVirtualEntry entry, int amount, boolean simulate) {
        if (entry == null || amount <= 0) {
            return ItemStack.EMPTY;
        }
        IItemHandler handler = this.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        ItemStack result = ItemStack.EMPTY;
        int remaining = amount;
        for (Integer physicalSlot : entry.getPhysicalSlots()) {
            if (remaining <= 0) {
                break;
            }
            int slot = physicalSlot.intValue();
            if (slot < 0 || slot >= handler.getSlots()) {
                continue;
            }
            ItemStack current = handler.getStackInSlot(slot);
            if (current.isEmpty() || !ItemHandlerHelper.canItemStacksStack(entry.peekStack(), current)) {
                continue;
            }
            ItemStack extracted = handler.extractItem(slot, remaining, simulate);
            if (extracted.isEmpty()) {
                continue;
            }
            if (result.isEmpty()) {
                result = extracted.copy();
            } else {
                result.grow(extracted.getCount());
            }
            remaining -= extracted.getCount();
        }
        return result;
    }

    /** Inserts through every real handler slot and returns the remainder. */
    public ItemStack insertIntoStorage(@Nullable ItemStack stack, boolean simulate) {
        if (stack == null || stack.isEmpty() || this.isBlacklisted(stack)) {
            return stack == null ? ItemStack.EMPTY : stack.copy();
        }
        IItemHandler handler = this.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        ItemStack remaining = stack.copy();
        for (int slot = 0; slot < handler.getSlots() && !remaining.isEmpty(); slot++) {
            remaining = handler.insertItem(slot, remaining, simulate);
        }
        return remaining;
    }

    // ---------------------------------------------------------------------
    // 1.12 storage-controller API
    // ---------------------------------------------------------------------

    /** Returns defensive copies of the aggregated storage stacks. */
    @Override
    public List<ItemStack> getStacks() {
        List<ItemStack> result = new ArrayList<>();
        for (StorageVirtualEntry entry : this.getVirtualStorageEntries("", StorageSortType.AMOUNT,
                StorageSortDirection.DOWN)) {
            result.add(entry.getStack());
        }
        return result;
    }

    @Override
    public int getMaxSlots() {
        return this.storageSlotCount;
    }

    /**
     * The controller is also a proxy in the upstream API.  Returning itself
     * lets integrations use one capability contract for controllers and
     * loaded-only wormhole proxies without introducing a second lookup path.
     */
    @Override
    public IStorageController getLinkedStorageController() {
        return this;
    }

    @Override
    public GlobalBlockPos getLinkedStorageControllerPosition() {
        return new GlobalBlockPos(this.pos, this.world);
    }

    /** A placed controller cannot move; links are changed on the proxy item/block. */
    @Override
    public void setLinkedStorageControllerPosition(@Nullable GlobalBlockPos blockPos) {
        // Intentionally no-op, matching the upstream block-entity contract.
    }

    @Override
    public void setMaxSlots(int slots) {
        this.setStorageSlotCount(slots);
    }

    @Override
    public int getUsedSlots() {
        int used = 0;
        for (int slot = 0; slot < this.storageSlotCount; slot++) {
            if (!this.stacks.get(slot).isEmpty()) {
                used++;
            }
        }
        return used;
    }

    @Override
    public MessageUpdateStacks getMessageUpdateStacks() {
        if (this.cachedMessageUpdateStacks == null) {
            this.cachedMessageUpdateStacks = new MessageUpdateStacks(this.getStacks(), this.getUsedSlots(),
                    this.getMaxSlots());
        }
        return this.cachedMessageUpdateStacks;
    }

    @Override
    public IItemHandler getStorageItemHandler() {
        return this.itemHandler;
    }

    /** Compatibility alias for integrations that use the upstream handler term. */
    public IItemHandler getItemHandler() {
        return this.getStorageItemHandler();
    }

    @Override
    public int insertStack(@Nullable ItemStack stack, boolean simulate) {
        return this.insertIntoStorage(stack, simulate).getCount();
    }

    @Override
    public ItemStack getItemStack(@Nullable Predicate<ItemStack> comparator, int requestedSize, boolean simulate) {
        if (comparator == null || requestedSize <= 0) {
            return ItemStack.EMPTY;
        }
        IItemHandler handler = this.getStorageItemHandler();
        ItemStack result = ItemStack.EMPTY;
        int remaining = requestedSize;
        for (int slot = 0; slot < handler.getSlots() && remaining > 0; slot++) {
            ItemStack current = handler.getStackInSlot(slot);
            if (current.isEmpty() || !comparator.test(current)) {
                continue;
            }
            if (!result.isEmpty() && !ItemHandlerHelper.canItemStacksStack(result, current)) {
                continue;
            }
            ItemStack extracted = handler.extractItem(slot, remaining, simulate);
            if (extracted.isEmpty()) {
                continue;
            }
            if (result.isEmpty()) {
                result = extracted.copy();
            } else if (ItemHandlerHelper.canItemStacksStack(result, extracted)) {
                result.grow(extracted.getCount());
            }
            remaining -= extracted.getCount();
        }
        return result;
    }

    @Override
    public ItemStack getOneOfMostCommonItem(@Nullable Predicate<ItemStack> comparator, boolean simulate) {
        if (comparator == null) {
            return ItemStack.EMPTY;
        }
        List<StorageVirtualEntry> entries = this.getVirtualStorageEntries("", StorageSortType.AMOUNT,
                StorageSortDirection.DOWN);
        for (StorageVirtualEntry entry : entries) {
            if (comparator.test(entry.peekStack())) {
                return this.extractVirtualStack(entry, 1, simulate);
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public int getAvailableAmount(@Nullable Predicate<ItemStack> comparator) {
        if (comparator == null) {
            return 0;
        }
        int amount = 0;
        for (int slot = 0; slot < this.storageSlotCount; slot++) {
            ItemStack stack = this.stacks.get(slot);
            if (!stack.isEmpty() && comparator.test(stack)) {
                amount += stack.getCount();
            }
        }
        return amount;
    }

    @Override
    public void onContentsChanged() {
        this.markDirty();
    }

    /**
     * Every mutating inventory path in this 1.12 port ultimately calls
     * {@code markDirty}.  Invalidating here prevents a stale compressed message
     * when a handler, container, machine or proxy changes a physical stack.
     */
    @Override
    public void markDirty() {
        this.invalidateMessageUpdateStacks();
        super.markDirty();
    }

    private void invalidateMessageUpdateStacks() {
        this.cachedMessageUpdateStacks = null;
    }

    /** Compatibility alias for code that calls the virtual list a snapshot. */
    public List<ItemStack> getStorageSnapshot(@Nullable String query, @Nullable StorageSortType type,
            @Nullable StorageSortDirection direction) {
        return this.getSortedStacks(query, type, direction);
    }

    /**
     * Sorts only active slots in-place. Hidden slots are deliberately untouched,
     * preserving stacks across stabilizer removal and later expansion.
     */
    public void sortStorageSlots() {
        List<ItemStack> active = new ArrayList<>(this.storageSlotCount);
        for (int slot = 0; slot < this.storageSlotCount; slot++) {
            active.add(this.stacks.get(slot));
        }
        active.sort(createPhysicalComparator(this.sortType, this.sortDirection));
        for (int slot = 0; slot < this.storageSlotCount; slot++) {
            this.stacks.set(slot, active.get(slot));
        }
        this.markDirty();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.stacks) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return index >= 0 && index < this.storageSlotCount ? this.stacks.get(index) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack stack = index >= 0 && index < this.storageSlotCount && !this.stacks.get(index).isEmpty() && count > 0
                ? this.stacks.get(index).splitStack(count)
                : ItemStack.EMPTY;
        if (!stack.isEmpty()) {
            this.markDirty();
        }
        return stack;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack stack = index >= 0 && index < this.storageSlotCount
                ? this.stacks.set(index, ItemStack.EMPTY)
                : ItemStack.EMPTY;
        if (!stack.isEmpty()) {
            this.markDirty();
        }
        return stack;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (index < 0 || index >= this.storageSlotCount) {
            return;
        }
        this.stacks.set(index, stack);
        if (!stack.isEmpty() && stack.getCount() > this.getStackLimit(stack)) {
            stack.setCount(this.getStackLimit(stack));
        }
        this.markDirty();
    }

    @Override
    public int getInventoryStackLimit() {
        return this.getStorageStackLimit();
    }

    /** Returns the configured per-slot controller stack limit. */
    public int getStorageStackLimit() {
        return Math.max(1, ModConfig.STORAGE.controllerStackSize);
    }

    /**
     * Resolves the effective limit for an item. With the upstream-compatible
     * override disabled, the item's normal stack size is still respected.
     */
    public int getStackLimit(@Nullable ItemStack stack) {
        int configured = this.getStorageStackLimit();
        if (ModConfig.STORAGE.overrideItemStackSizes || stack == null || stack.isEmpty()) {
            return configured;
        }
        return Math.min(configured, stack.getMaxStackSize());
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return this.world != null && this.world.getTileEntity(this.pos) == this
                && player.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D,
                        this.pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void openInventory(EntityPlayer player) {
    }

    @Override
    public void closeInventory(EntityPlayer player) {
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return index >= 0 && index < this.storageSlotCount && stack != null && !stack.isEmpty()
                && !this.isBlacklisted(stack);
    }

    /** Storage networks must not contain another controller or their own remote. */
    public boolean isBlacklisted(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        if (stack.getItem() instanceof ItemBlock) {
            return ((ItemBlock) stack.getItem()).getBlock() instanceof BlockStorageController;
        }
        return stack.getItem() == ModItems.STORAGE_REMOTE;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {
    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
        for (int slot = 0; slot < this.stacks.size(); slot++) {
            this.stacks.set(slot, ItemStack.EMPTY);
        }
        this.markDirty();
    }

    /** Recomputes capacity from the six unobstructed rays above this controller. */
    public void updateStabilizers() {
        if (this.world == null || this.pos == null) {
            return;
        }
        int slots = configuredBaseSlotCount();
        for (BlockPos stabilizer : this.findValidStabilizers()) {
            slots += getSlotsForStabilizerBlock(this.world.getBlockState(stabilizer).getBlock());
        }
        this.setStorageSlotCount(slots);
    }

    /**
     * Finds the first correctly oriented stabilizer in each direction from the block
     * above the controller. A non-air block before the stabilizer blocks the ray.
     */
    public List<BlockPos> findValidStabilizers() {
        if (this.world == null || this.pos == null) {
            return Collections.emptyList();
        }
        Set<BlockPos> result = new LinkedHashSet<>();
        BlockPos start = this.pos.up();
        for (EnumFacing facing : EnumFacing.values()) {
            BlockPos current = start;
            for (int distance = 0; distance <= MAX_STABILIZER_DISTANCE; distance++) {
                IBlockState state = this.world.getBlockState(current);
                Block block = state.getBlock();
                // The downward ray starts one block above the controller and
                // crosses the controller itself before reaching below it.
                if (current.equals(this.pos)) {
                    current = current.offset(facing);
                    continue;
                }
                if (block instanceof BlockStorageStabilizer) {
                    if (state.getValue(BlockStorageStabilizer.FACING) == facing.getOpposite()) {
                        result.add(current);
                    }
                    break;
                }
                if (!this.world.isAirBlock(current)) {
                    break;
                }
                current = current.offset(facing);
            }
        }
        return new ArrayList<>(result);
    }

    public static boolean isStorageStabilizer(Block block) {
        return block instanceof BlockStorageStabilizer;
    }

    public static int getSlotsForStabilizerBlock(Block block) {
        if (block == ModBlocks.STORAGE_STABILIZER_TIER1 || block == ModBlocks.STORAGE_STABILIZER_TIER1_DARK) {
            return ModConfig.STORAGE.stabilizerTier1Slots;
        }
        if (block == ModBlocks.STORAGE_STABILIZER_TIER2 || block == ModBlocks.STORAGE_STABILIZER_TIER2_DARK) {
            return ModConfig.STORAGE.stabilizerTier2Slots;
        }
        if (block == ModBlocks.STORAGE_STABILIZER_TIER3 || block == ModBlocks.STORAGE_STABILIZER_TIER3_DARK) {
            return ModConfig.STORAGE.stabilizerTier3Slots;
        }
        if (block == ModBlocks.STORAGE_STABILIZER_TIER4 || block == ModBlocks.STORAGE_STABILIZER_TIER4_DARK) {
            return ModConfig.STORAGE.stabilizerTier4Slots;
        }
        if (block == ModBlocks.STORAGE_STABILIZER_TIER5 || block == ModBlocks.STORAGE_STABILIZER_TIER5_DARK) {
            return ModConfig.STORAGE.stabilizerTier5Slots;
        }
        return 0;
    }

    private void writeStorageSettings(NBTTagCompound compound) {
        compound.setInteger(TAG_SORT_TYPE, this.sortType.getValue());
        compound.setInteger(TAG_SORT_DIRECTION, this.sortDirection.getValue());
        if (!this.searchQuery.isEmpty()) {
            compound.setString(TAG_SEARCH_QUERY, this.searchQuery);
        } else {
            compound.removeTag(TAG_SEARCH_QUERY);
        }
    }

    /** Writes the upstream matrix/order payload into the normal and portable NBT forms. */
    private void writeCraftingState(NBTTagCompound compound) {
        NBTTagList matrix = new NBTTagList();
        for (int slot = 0; slot < CRAFTING_MATRIX_SIZE; slot++) {
            ItemStack stack = this.craftingMatrix.get(slot);
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            NBTTagCompound item = stack.writeToNBT(new NBTTagCompound());
            // Upstream uses a lower-case key in its network payload.  Keep the
            // integer companion so super-stack counts survive 1.12's byte Count.
            item.setByte(TAG_MATRIX_SLOT, (byte) slot);
            item.setInteger(TAG_REAL_SIZE, stack.getCount());
            matrix.appendTag(item);
        }
        compound.setTag(TAG_CRAFTING_MATRIX, matrix);
        if (this.orderStack == null || this.orderStack.isEmpty()) {
            compound.removeTag(TAG_ORDER_STACK);
        } else {
            NBTTagCompound order = this.orderStack.writeToNBT(new NBTTagCompound());
            order.setInteger(TAG_REAL_SIZE, this.orderStack.getCount());
            compound.setTag(TAG_ORDER_STACK, order);
        }
    }

    /** Reads both upstream lower-case and tolerant legacy upper-case Slot keys. */
    private void readCraftingState(NBTTagCompound compound) {
        this.craftingMatrix = NonNullList.withSize(CRAFTING_MATRIX_SIZE, ItemStack.EMPTY);
        if (compound != null && compound.hasKey(TAG_CRAFTING_MATRIX, 9)) {
            NBTTagList matrix = compound.getTagList(TAG_CRAFTING_MATRIX, 10);
            // Bound malformed NBT; a legitimate terminal matrix has at most 9 entries.
            for (int index = 0; index < matrix.tagCount() && index < CRAFTING_MATRIX_SIZE; index++) {
                NBTTagCompound item = matrix.getCompoundTagAt(index);
                int slot;
                if (item.hasKey("Slot", 3)) {
                    slot = item.getInteger("Slot");
                } else if (item.hasKey("Slot", 1)) {
                    slot = item.getByte("Slot") & 255;
                } else if (item.hasKey(TAG_MATRIX_SLOT, 3)) {
                    slot = item.getInteger(TAG_MATRIX_SLOT);
                } else if (item.hasKey(TAG_MATRIX_SLOT, 1)) {
                    slot = item.getByte(TAG_MATRIX_SLOT) & 255;
                } else {
                    continue;
                }
                if (slot < 0 || slot >= CRAFTING_MATRIX_SIZE) {
                    continue;
                }
                try {
                    ItemStack stack = new ItemStack(item);
                    if (item.hasKey(TAG_REAL_SIZE, 3)) {
                        stack.setCount(Math.max(0, item.getInteger(TAG_REAL_SIZE)));
                    }
                    this.craftingMatrix.set(slot, stack);
                } catch (RuntimeException ignored) {
                    // Ignore malformed matrix entries while preserving the rest.
                }
            }
        }
        if (compound != null && compound.hasKey(TAG_ORDER_STACK, 10)) {
            try {
                ItemStack stack = new ItemStack(compound.getCompoundTag(TAG_ORDER_STACK));
                if (compound.getCompoundTag(TAG_ORDER_STACK).hasKey(TAG_REAL_SIZE, 3)) {
                    stack.setCount(Math.max(0, compound.getCompoundTag(TAG_ORDER_STACK)
                            .getInteger(TAG_REAL_SIZE)));
                }
                this.orderStack = stack.isEmpty() ? ItemStack.EMPTY : stack;
            } catch (RuntimeException ignored) {
                this.orderStack = ItemStack.EMPTY;
            }
        } else {
            this.orderStack = ItemStack.EMPTY;
        }
    }

    private void readStorageSettings(NBTTagCompound compound) {
        this.sortType = compound.hasKey(TAG_SORT_TYPE, 3)
                ? StorageSortType.byValue(compound.getInteger(TAG_SORT_TYPE))
                : StorageSortType.AMOUNT;
        this.sortDirection = compound.hasKey(TAG_SORT_DIRECTION, 3)
                ? StorageSortDirection.byValue(compound.getInteger(TAG_SORT_DIRECTION))
                : StorageSortDirection.DOWN;
        this.searchQuery = compound.hasKey(TAG_SEARCH_QUERY, 8)
                ? normalizeSearchQuery(compound.getString(TAG_SEARCH_QUERY))
                : "";
    }

    private static String normalizeSearchQuery(@Nullable String query) {
        return query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
    }

    private static boolean matchesSearch(ItemStack stack, String query) {
        if (query.isEmpty()) {
            return true;
        }
        String displayName = safeLower(stack.getDisplayName());
        String registryName = "";
        if (stack.getItem() != null && stack.getItem().getRegistryName() != null) {
            registryName = safeLower(stack.getItem().getRegistryName().toString());
        }
        return displayName.contains(query) || registryName.contains(query);
    }

    private static String safeLower(@Nullable String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private static void mergeIntoSnapshot(List<ItemStack> result, ItemStack stack) {
        for (ItemStack existing : result) {
            if (ItemHandlerHelper.canItemStacksStack(existing, stack)) {
                existing.grow(stack.getCount());
                return;
            }
        }
        result.add(stack.copy());
    }

    private static Comparator<ItemStack> createComparator(StorageSortType type, StorageSortDirection direction) {
        Comparator<ItemStack> comparator;
        switch (type) {
            case NAME:
                comparator = Comparator.comparing(TileEntityStorageController::itemName)
                        .thenComparingInt(ItemStack::getCount);
                break;
            case MOD:
                comparator = Comparator.comparing(TileEntityStorageController::itemMod)
                        .thenComparing(TileEntityStorageController::itemName)
                        .thenComparingInt(ItemStack::getCount);
                break;
            case AMOUNT:
            default:
                comparator = Comparator.comparingInt(ItemStack::getCount)
                        .thenComparing(TileEntityStorageController::itemName);
                break;
        }
        return direction.isDown() ? comparator.reversed() : comparator;
    }

    private static Comparator<ItemStack> createPhysicalComparator(StorageSortType type,
            StorageSortDirection direction) {
        Comparator<ItemStack> sorted = createComparator(type, direction);
        return (left, right) -> {
            if (left.isEmpty()) {
                return right.isEmpty() ? 0 : 1;
            }
            if (right.isEmpty()) {
                return -1;
            }
            return sorted.compare(left, right);
        };
    }

    private static String itemName(ItemStack stack) {
        String displayName = safeLower(stack.getDisplayName());
        String registryName = stack.getItem() == null || stack.getItem().getRegistryName() == null
                ? "" : safeLower(stack.getItem().getRegistryName().toString());
        return displayName + "|" + registryName;
    }

    private static String itemMod(ItemStack stack) {
        if (stack.getItem() == null || stack.getItem().getRegistryName() == null) {
            return "";
        }
        String registryName = stack.getItem().getRegistryName().toString();
        int separator = registryName.indexOf(':');
        return safeLower(separator < 0 ? registryName : registryName.substring(0, separator));
    }

    private static int configuredBaseSlotCount() {
        return Math.max(1, ModConfig.STORAGE.controllerBaseSlots);
    }

    private static int readStorageSlotCount(NBTTagCompound compound, int fallback) {
        int declared = compound.hasKey(TAG_STORAGE_SLOT_COUNT, 3)
                ? compound.getInteger(TAG_STORAGE_SLOT_COUNT)
                : fallback;
        return Math.max(0, declared);
    }

    private static int getRequiredItemListSize(NBTTagCompound compound) {
        if (!compound.hasKey(TAG_ITEMS, 9)) {
            return 0;
        }
        int largestSlot = -1;
        NBTTagList items = compound.getTagList(TAG_ITEMS, 10);
        for (int index = 0; index < items.tagCount(); index++) {
            NBTTagCompound item = items.getCompoundTagAt(index);
            int slot = item.hasKey("Slot", 3) ? item.getInteger("Slot") : item.getByte("Slot") & 255;
            largestSlot = Math.max(largestSlot, slot);
        }
        return largestSlot + 1;
    }

    private void saveItems(NBTTagCompound compound, boolean saveEmpty) {
        saveItems(compound, this.stacks, saveEmpty);
    }

    private static void saveItems(NBTTagCompound compound, NonNullList<ItemStack> contents, boolean saveEmpty) {
        NBTTagList items = new NBTTagList();
        for (int slot = 0; slot < contents.size(); slot++) {
            ItemStack stack = contents.get(slot);
            if (!stack.isEmpty()) {
                NBTTagCompound item = stack.writeToNBT(new NBTTagCompound());
                item.setInteger("Slot", slot);
                // ItemStack.writeToNBT stores Count as a byte in 1.12. Keep the
                // original integer so controller stacks larger than 127 survive.
                item.setInteger(TAG_REAL_SIZE, stack.getCount());
                items.appendTag(item);
            }
        }
        if (!items.isEmpty() || saveEmpty) {
            compound.setTag(TAG_ITEMS, items);
        }
    }

    private void loadItems(NBTTagCompound compound) {
        loadItems(compound, this.stacks);
    }

    private static void loadItems(NBTTagCompound compound, NonNullList<ItemStack> target) {
        if (!compound.hasKey(TAG_ITEMS, 9)) {
            return;
        }
        NBTTagList items = compound.getTagList(TAG_ITEMS, 10);
        for (int index = 0; index < items.tagCount(); index++) {
            NBTTagCompound item = items.getCompoundTagAt(index);
            int slot = item.hasKey("Slot", 3) ? item.getInteger("Slot") : item.getByte("Slot") & 255;
            if (slot >= 0 && slot < target.size()) {
                ItemStack stack = new ItemStack(item);
                if (item.hasKey(TAG_REAL_SIZE, 3)) {
                    stack.setCount(Math.max(0, item.getInteger(TAG_REAL_SIZE)));
                }
                target.set(slot, stack);
            }
        }
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override
    @Nullable
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return (T) this.itemHandler;
        }
        return super.getCapability(capability, facing);
    }

    /** IItemHandler implementation that keeps the controller's hidden slots and integer stack sizes. */
    private static final class StorageControllerItemHandler implements IItemHandlerModifiable {

        private final TileEntityStorageController controller;

        private StorageControllerItemHandler(TileEntityStorageController controller) {
            this.controller = controller;
        }

        @Override
        public int getSlots() {
            return this.controller.getStorageSlotCount();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            this.validateSlot(slot);
            return this.controller.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            this.validateSlot(slot);
            if (stack == null || stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
            if (!this.controller.isItemValidForSlot(slot, stack)) {
                return stack.copy();
            }

            ItemStack existing = this.controller.getStackInSlot(slot);
            if (!existing.isEmpty() && !ItemHandlerHelper.canItemStacksStack(existing, stack)) {
                return stack.copy();
            }

            int limit = this.controller.getStackLimit(stack);
            int current = existing.isEmpty() ? 0 : existing.getCount();
            int accepted = Math.min(stack.getCount(), Math.max(0, limit - current));
            if (accepted <= 0) {
                return stack.copy();
            }
            if (!simulate) {
                if (existing.isEmpty()) {
                    ItemStack inserted = stack.copy();
                    inserted.setCount(accepted);
                    this.controller.setInventorySlotContents(slot, inserted);
                } else {
                    existing.grow(accepted);
                    this.controller.setInventorySlotContents(slot, existing);
                }
            }
            if (accepted == stack.getCount()) {
                return ItemStack.EMPTY;
            }
            return ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - accepted);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            this.validateSlot(slot);
            if (amount <= 0) {
                return ItemStack.EMPTY;
            }
            ItemStack existing = this.controller.getStackInSlot(slot);
            if (existing.isEmpty()) {
                return ItemStack.EMPTY;
            }
            int extracted = Math.min(amount, Math.min(existing.getCount(),
                    this.controller.getStackLimit(existing)));
            ItemStack result = ItemHandlerHelper.copyStackWithSize(existing, extracted);
            if (!simulate) {
                if (extracted >= existing.getCount()) {
                    this.controller.setInventorySlotContents(slot, ItemStack.EMPTY);
                } else {
                    existing.shrink(extracted);
                    this.controller.setInventorySlotContents(slot, existing);
                }
            }
            return result;
        }

        @Override
        public int getSlotLimit(int slot) {
            return this.controller.getStorageStackLimit();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot >= 0 && slot < this.controller.getStorageSlotCount()
                    && stack != null && !stack.isEmpty()
                    && this.controller.isItemValidForSlot(slot, stack);
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            this.validateSlot(slot);
            this.controller.setInventorySlotContents(slot, stack == null ? ItemStack.EMPTY : stack);
        }

        private void validateSlot(int slot) {
            if (slot < 0 || slot >= this.controller.getStorageSlotCount()) {
                throw new RuntimeException("Slot " + slot + " not in valid range - [0, "
                        + this.controller.getStorageSlotCount() + ")");
            }
        }
    }
}
