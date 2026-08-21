package com.occultism.occultism.tileentity;

import com.occultism.occultism.item.ItemMinerSpirit;
import com.occultism.occultism.mineshaft.DimensionalMineshaftRecipe;
import com.occultism.occultism.registry.ModBlocks;
import com.occultism.occultism.registry.ModItems;
import com.occultism.occultism.util.InventoryAccess;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

public class TileEntityDimensionalMineshaft extends TileEntity implements ITickable, ISidedInventory {

    public static final int MINER_SLOT = 0;
    public static final int FIRST_OUTPUT_SLOT = 1;
    public static final int SLOT_COUNT = 10;
    public static final int DEFAULT_MAX_MINING_TIME = 400;
    private static final int EFFICIENCY_TIME_REDUCTION_PER_LEVEL = 30;
    private static final int[] INPUT_SLOTS = {MINER_SLOT};
    private static final int[] OUTPUT_SLOTS = {1, 2, 3, 4, 5, 6, 7, 8, 9};

    private NonNullList<ItemStack> stacks = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private int miningTime;
    private int maxMiningTime;
    private Item currentMinerItem;
    private final IItemHandlerModifiable itemHandlerTop = new SidedInvWrapper(this, EnumFacing.UP);
    private final IItemHandlerModifiable itemHandlerBottom = new SidedInvWrapper(this, EnumFacing.DOWN);
    private final IItemHandlerModifiable itemHandlerSide = new SidedInvWrapper(this, EnumFacing.WEST);
    private final IItemHandler itemHandlerAnySide = new CombinedInvWrapper(this.itemHandlerTop, this.itemHandlerBottom);

    @Override
    public void update() {
        if (this.world == null || this.world.isRemote) {
            return;
        }

        ItemStack miner = this.getStackInSlot(MINER_SLOT);
        if (!this.isValidMiner(miner)) {
            this.resetMining();
            return;
        }

        if (this.world.isBlockPowered(this.pos)) {
            this.miningTime = 0;
            this.markDirty();
            return;
        }

        if (miner.getItem() != this.currentMinerItem || this.miningTime <= 0) {
            this.currentMinerItem = miner.getItem();
            this.maxMiningTime = this.getMaxMiningTime(miner);
            this.miningTime = this.maxMiningTime;
            this.markDirty();
            return;
        }

        this.miningTime--;
        if (this.miningTime <= 0) {
            this.mine(miner);
            ItemStack currentMiner = this.getStackInSlot(MINER_SLOT);
            this.currentMinerItem = currentMiner.isEmpty() ? null : currentMiner.getItem();
            this.maxMiningTime = currentMiner.isEmpty() ? 0 : this.getMaxMiningTime(currentMiner);
            this.miningTime = currentMiner.isEmpty() ? 0 : this.maxMiningTime;
        }

        if (this.world.getTotalWorldTime() % 20L == 0L) {
            this.markDirty();
        }
    }

    public ItemStack takeMiner() {
        ItemStack stack = this.getStackInSlot(MINER_SLOT);
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        this.setInventorySlotContents(MINER_SLOT, ItemStack.EMPTY);
        this.resetMining();
        return stack;
    }

    public ItemStack takeFirstOutput() {
        for (int slot = FIRST_OUTPUT_SLOT; slot < SLOT_COUNT; slot++) {
            ItemStack stack = this.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                this.setInventorySlotContents(slot, ItemStack.EMPTY);
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    public boolean hasOutputs() {
        for (int slot = FIRST_OUTPUT_SLOT; slot < SLOT_COUNT; slot++) {
            if (!this.getStackInSlot(slot).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void mine(ItemStack miner) {
        int rolls = this.getRollsPerOperation(miner);
        int multiplier = this.getOutputMultiplier(miner);
        List<ItemStack> outputs = new ArrayList<>();

        for (int i = 0; i < rolls; i++) {
            ItemStack result = this.rollOutput(miner);
            if (result.isEmpty()) {
                continue;
            }
            result.setCount(result.getCount() * multiplier);
            this.applyMinerOperationLimit(result);
            this.mergeBatched(outputs, result);
        }

        ItemStack minerToSave = this.getMinerToSaveAfterOperation(miner);
        List<ItemStack> requiredOutputs = new ArrayList<>(outputs);
        if (!minerToSave.isEmpty()) {
            requiredOutputs.add(minerToSave);
        }

        if (!this.canOutputAll(requiredOutputs)) {
            return;
        }

        for (ItemStack output : outputs) {
            this.outputStack(output);
        }
        this.damageMiner(miner, minerToSave);
        this.markDirty();
    }

    private void applyMinerOperationLimit(ItemStack result) {
        if (result.getItem() == ModItems.MINING_DIM_CORE && result.getCount() > 1) {
            result.setCount(1);
        }
    }

    private ItemStack getMinerToSaveAfterOperation(ItemStack miner) {
        if (miner.isEmpty() || !miner.isItemStackDamageable()) {
            return ItemStack.EMPTY;
        }
        int maxDamage = miner.getMaxDamage();
        int nextDamage = miner.getItemDamage() + 1;
        if (maxDamage <= 0 || nextDamage >= maxDamage) {
            return ItemStack.EMPTY;
        }
        if (nextDamage < maxDamage - 1) {
            return ItemStack.EMPTY;
        }
        ItemStack savedMiner = miner.copy();
        savedMiner.setCount(1);
        savedMiner.setItemDamage(nextDamage);
        return savedMiner;
    }

    private void damageMiner(ItemStack miner, ItemStack minerToSave) {
        if (miner.isEmpty() || !miner.isItemStackDamageable()) {
            return;
        }
        if (!minerToSave.isEmpty()) {
            this.setInventorySlotContents(MINER_SLOT, ItemStack.EMPTY);
            this.outputStack(minerToSave);
            return;
        }
        miner.setItemDamage(miner.getItemDamage() + 1);
        if (miner.getItemDamage() >= miner.getMaxDamage()) {
            this.setInventorySlotContents(MINER_SLOT, ItemStack.EMPTY);
        } else {
            this.setInventorySlotContents(MINER_SLOT, miner);
        }
    }

    private void outputStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        ItemStack remaining = this.tryOutputToExtractor(stack);
        if (!remaining.isEmpty()) {
            remaining = this.insertIntoOutputSlots(remaining);
        }
        if (!remaining.isEmpty()) {
            net.minecraft.inventory.InventoryHelper.spawnItemStack(this.world, this.pos.getX() + 0.5D,
                    this.pos.getY() + 1.0D, this.pos.getZ() + 0.5D, remaining);
        }
    }

    private ItemStack tryOutputToExtractor(ItemStack stack) {
        return this.tryOutputToExtractor(stack, false);
    }

    private ItemStack tryOutputToExtractor(ItemStack stack, boolean simulate) {
        TileEntity tile = this.getExtractorOutputTarget();
        if (tile == null) {
            return stack;
        }
        ItemStack remaining = this.insertIntoInventory(tile, stack, EnumFacing.UP, simulate);
        if (!simulate && remaining.getCount() < stack.getCount()) {
            tile.markDirty();
        }
        return remaining;
    }

    @Nullable
    private TileEntity getExtractorOutputTarget() {
        if (this.world.getBlockState(this.pos.down()).getBlock() != ModBlocks.DIMENSIONAL_EXTRACTOR) {
            return null;
        }
        TileEntity tile = this.world.getTileEntity(this.pos.down(2));
        return InventoryAccess.isInventory(tile, EnumFacing.UP) ? tile : null;
    }

    private ItemStack insertIntoInventory(TileEntity tile, ItemStack stack, EnumFacing side, boolean simulate) {
        if (tile instanceof IInventory) {
            return simulate ? this.simulateInsertIntoInventory((IInventory) tile, stack, side)
                    : InventoryAccess.insert(tile, stack, side);
        }
        if (!tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side)) {
            return simulate ? stack.copy() : InventoryAccess.insert(tile, stack, side);
        }
        IItemHandler handler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
        if (handler == null) {
            return stack.copy();
        }
        ItemStack remaining = stack.copy();
        for (int slot = 0; slot < handler.getSlots() && !remaining.isEmpty(); slot++) {
            remaining = handler.insertItem(slot, remaining, simulate);
        }
        return remaining;
    }

    private ItemStack simulateInsertIntoInventory(IInventory inventory, ItemStack stack, EnumFacing side) {
        return this.insertIntoInventorySimulation(inventory, this.createInventorySimulation(inventory), stack, side);
    }

    private NonNullList<ItemStack> createInventorySimulation(IInventory inventory) {
        NonNullList<ItemStack> simulated = NonNullList.withSize(inventory.getSizeInventory(), ItemStack.EMPTY);
        for (int slot = 0; slot < simulated.size(); slot++) {
            ItemStack existing = inventory.getStackInSlot(slot);
            simulated.set(slot, existing.isEmpty() ? ItemStack.EMPTY : existing.copy());
        }
        return simulated;
    }

    private ItemStack insertIntoInventorySimulation(IInventory inventory, NonNullList<ItemStack> simulated,
            ItemStack stack, EnumFacing side) {
        ItemStack remaining = stack.copy();
        if (inventory instanceof ISidedInventory) {
            ISidedInventory sided = (ISidedInventory) inventory;
            for (int slot : sided.getSlotsForFace(side)) {
                remaining = this.insertIntoInventorySlot(inventory, simulated, slot, remaining, side);
                if (remaining.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
            return remaining;
        }
        for (int slot = 0; slot < simulated.size() && !remaining.isEmpty(); slot++) {
            remaining = this.insertIntoInventorySlot(inventory, simulated, slot, remaining, null);
        }
        return remaining;
    }

    private ItemStack insertIntoInventorySlot(IInventory inventory, NonNullList<ItemStack> target, int slot,
            ItemStack stack, @Nullable EnumFacing side) {
        if (stack.isEmpty() || !inventory.isItemValidForSlot(slot, stack)) {
            return stack;
        }
        if (inventory instanceof ISidedInventory && side != null
                && !((ISidedInventory) inventory).canInsertItem(slot, stack, side)) {
            return stack;
        }
        ItemStack remaining = stack.copy();
        ItemStack existing = target.get(slot);
        if (existing.isEmpty()) {
            int insertCount = Math.min(remaining.getCount(),
                    Math.min(remaining.getMaxStackSize(), inventory.getInventoryStackLimit()));
            ItemStack inserted = remaining.copy();
            inserted.setCount(insertCount);
            target.set(slot, inserted);
            remaining.shrink(insertCount);
            return remaining;
        }
        if (!this.canMergeStacks(existing, remaining)) {
            return remaining;
        }
        int maxStackSize = Math.min(existing.getMaxStackSize(), inventory.getInventoryStackLimit());
        int insertCount = Math.min(remaining.getCount(), maxStackSize - existing.getCount());
        if (insertCount <= 0) {
            return remaining;
        }
        existing.grow(insertCount);
        remaining.shrink(insertCount);
        return remaining;
    }

    private ItemStack insertIntoOutputSlots(ItemStack stack) {
        ItemStack remaining = stack.copy();
        for (int slot = FIRST_OUTPUT_SLOT; slot < SLOT_COUNT && !remaining.isEmpty(); slot++) {
            ItemStack existing = this.getStackInSlot(slot);
            if (existing.isEmpty()) {
                int insertCount = Math.min(remaining.getCount(), remaining.getMaxStackSize());
                ItemStack inserted = remaining.copy();
                inserted.setCount(insertCount);
                this.setInventorySlotContents(slot, inserted);
                remaining.shrink(insertCount);
                continue;
            }
            if (!this.canMergeStacks(existing, remaining)) {
                continue;
            }
            int insertCount = Math.min(remaining.getCount(), existing.getMaxStackSize() - existing.getCount());
            if (insertCount <= 0) {
                continue;
            }
            existing.grow(insertCount);
            this.setInventorySlotContents(slot, existing);
            remaining.shrink(insertCount);
        }
        return remaining;
    }

    private boolean canOutputAll(List<ItemStack> outputs) {
        NonNullList<ItemStack> simulated = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
        for (int slot = FIRST_OUTPUT_SLOT; slot < SLOT_COUNT; slot++) {
            ItemStack existing = this.getStackInSlot(slot);
            simulated.set(slot, existing.isEmpty() ? ItemStack.EMPTY : existing.copy());
        }
        TileEntity extractorTarget = this.getExtractorOutputTarget();
        NonNullList<ItemStack> simulatedExtractorInventory = extractorTarget instanceof IInventory
                ? this.createInventorySimulation((IInventory) extractorTarget)
                : null;
        IItemHandler extractorHandler = this.getExtractorHandler(extractorTarget);
        NonNullList<ItemStack> simulatedExtractorHandler = extractorHandler == null
                ? null
                : this.createItemHandlerSimulation(extractorHandler);
        for (ItemStack output : outputs) {
            ItemStack remaining = this.insertIntoExtractorSimulation(output, extractorTarget,
                    simulatedExtractorInventory, extractorHandler, simulatedExtractorHandler);
            remaining = this.insertIntoOutputSlots(remaining, simulated);
            if (!remaining.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Nullable
    private IItemHandler getExtractorHandler(@Nullable TileEntity extractorTarget) {
        if (extractorTarget == null || extractorTarget instanceof IInventory
                || !extractorTarget.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP)) {
            return null;
        }
        return extractorTarget.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
    }

    private ItemStack insertIntoExtractorSimulation(ItemStack stack, @Nullable TileEntity extractorTarget,
            @Nullable NonNullList<ItemStack> simulatedInventory, @Nullable IItemHandler handler,
            @Nullable NonNullList<ItemStack> simulatedHandler) {
        if (extractorTarget == null) {
            return stack.copy();
        }
        if (extractorTarget instanceof IInventory && simulatedInventory != null) {
            return this.insertIntoInventorySimulation((IInventory) extractorTarget, simulatedInventory, stack,
                    EnumFacing.UP);
        }
        if (handler != null && simulatedHandler != null) {
            return this.insertIntoItemHandlerSimulation(handler, simulatedHandler, stack);
        }
        return stack.copy();
    }

    private NonNullList<ItemStack> createItemHandlerSimulation(IItemHandler handler) {
        NonNullList<ItemStack> simulated = NonNullList.withSize(handler.getSlots(), ItemStack.EMPTY);
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack existing = handler.getStackInSlot(slot);
            simulated.set(slot, existing.isEmpty() ? ItemStack.EMPTY : existing.copy());
        }
        return simulated;
    }

    private ItemStack insertIntoItemHandlerSimulation(IItemHandler handler, NonNullList<ItemStack> target,
            ItemStack stack) {
        ItemStack remaining = stack.copy();
        for (int slot = 0; slot < handler.getSlots() && !remaining.isEmpty(); slot++) {
            ItemStack existing = target.get(slot);
            if (existing.isEmpty()) {
                remaining = this.insertIntoEmptyHandlerSlot(handler, target, slot, remaining);
                continue;
            }
            if (!this.canMergeStacks(existing, remaining)) {
                continue;
            }
            int maxStackSize = Math.min(existing.getMaxStackSize(), handler.getSlotLimit(slot));
            int insertCount = Math.min(remaining.getCount(), maxStackSize - existing.getCount());
            if (insertCount <= 0) {
                continue;
            }
            ItemStack candidate = remaining.copy();
            candidate.setCount(insertCount);
            ItemStack rejected = handler.insertItem(slot, candidate.copy(), true);
            int accepted = candidate.getCount() - rejected.getCount();
            if (accepted <= 0) {
                continue;
            }
            existing.grow(accepted);
            remaining.shrink(accepted);
        }
        return remaining;
    }

    private ItemStack insertIntoEmptyHandlerSlot(IItemHandler handler, NonNullList<ItemStack> target, int slot,
            ItemStack stack) {
        int insertCount = Math.min(stack.getCount(), Math.min(stack.getMaxStackSize(), handler.getSlotLimit(slot)));
        if (insertCount <= 0) {
            return stack;
        }
        ItemStack candidate = stack.copy();
        candidate.setCount(insertCount);
        ItemStack rejected = handler.insertItem(slot, candidate.copy(), true);
        int accepted = candidate.getCount() - rejected.getCount();
        if (accepted <= 0) {
            return stack;
        }
        ItemStack inserted = stack.copy();
        inserted.setCount(accepted);
        target.set(slot, inserted);
        ItemStack remaining = stack.copy();
        remaining.shrink(accepted);
        return remaining;
    }

    private ItemStack insertIntoOutputSlots(ItemStack stack, NonNullList<ItemStack> target) {
        ItemStack remaining = stack.copy();
        for (int slot = FIRST_OUTPUT_SLOT; slot < SLOT_COUNT && !remaining.isEmpty(); slot++) {
            ItemStack existing = target.get(slot);
            if (existing.isEmpty()) {
                int insertCount = Math.min(remaining.getCount(), remaining.getMaxStackSize());
                ItemStack inserted = remaining.copy();
                inserted.setCount(insertCount);
                target.set(slot, inserted);
                remaining.shrink(insertCount);
                continue;
            }
            if (!this.canMergeStacks(existing, remaining)) {
                continue;
            }
            int insertCount = Math.min(remaining.getCount(), existing.getMaxStackSize() - existing.getCount());
            if (insertCount <= 0) {
                continue;
            }
            existing.grow(insertCount);
            remaining.shrink(insertCount);
        }
        return remaining;
    }

    private void mergeBatched(List<ItemStack> outputs, ItemStack incoming) {
        for (ItemStack existing : outputs) {
            if (this.canMergeStacks(existing, incoming)) {
                existing.grow(incoming.getCount());
                return;
            }
        }
        outputs.add(incoming);
    }

    private boolean canMergeStacks(ItemStack existing, ItemStack incoming) {
        return ItemStack.areItemsEqual(existing, incoming) && ItemStack.areItemStackTagsEqual(existing, incoming);
    }

    private ItemStack rollOutput(ItemStack miner) {
        DimensionalMineshaftRecipe recipe = DimensionalMineshaftRecipe.find(miner);
        if (recipe == null) {
            return ItemStack.EMPTY;
        }
        return recipe.rollOutput(this.world.rand);
    }

    private boolean isValidMiner(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ItemMinerSpirit;
    }

    private int getMaxMiningTime(ItemStack miner) {
        if (!this.isValidMiner(miner)) {
            return DEFAULT_MAX_MINING_TIME;
        }
        int miningTime = ((ItemMinerSpirit) miner.getItem()).getMaxMiningTime();
        int efficiencyLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, miner);
        return Math.max(1, miningTime - efficiencyLevel * EFFICIENCY_TIME_REDUCTION_PER_LEVEL);
    }

    private int getRollsPerOperation(ItemStack miner) {
        if (!this.isValidMiner(miner)) {
            return 1;
        }
        int fortuneLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, miner);
        return ((ItemMinerSpirit) miner.getItem()).getRollsPerOperation() + fortuneLevel;
    }

    private int getOutputMultiplier(ItemStack miner) {
        if (!this.isValidMiner(miner)) {
            return 1;
        }
        int silkTouchLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, miner);
        return ((ItemMinerSpirit) miner.getItem()).getOutputMultiplier() * (silkTouchLevel + 1);
    }

    private void resetMining() {
        this.miningTime = 0;
        this.maxMiningTime = 0;
        this.currentMinerItem = null;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        ItemStackHelper.saveAllItems(compound, this.stacks);
        compound.setInteger("MiningTime", this.miningTime);
        compound.setInteger("MaxMiningTime", this.maxMiningTime);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.stacks = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(compound, this.stacks);
        this.miningTime = compound.getInteger("MiningTime");
        this.maxMiningTime = compound.getInteger("MaxMiningTime");
        ItemStack miner = this.getStackInSlot(MINER_SLOT);
        this.currentMinerItem = miner.isEmpty() ? null : miner.getItem();
    }

    @Override
    public String getName() {
        return "container.occultism.dimensional_mineshaft";
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
        return SLOT_COUNT;
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
        return index >= 0 && index < this.stacks.size() ? this.stacks.get(index) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack stack = ItemStackHelper.getAndSplit(this.stacks, index, count);
        if (!stack.isEmpty()) {
            if (index == MINER_SLOT) {
                this.resetMining();
            }
            this.markDirty();
        }
        return stack;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack stack = ItemStackHelper.getAndRemove(this.stacks, index);
        if (!stack.isEmpty()) {
            if (index == MINER_SLOT) {
                this.resetMining();
            }
            this.markDirty();
        }
        return stack;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        this.stacks.set(index, stack);
        if (!stack.isEmpty() && stack.getCount() > this.getInventoryStackLimit()) {
            stack.setCount(this.getInventoryStackLimit());
        }
        if (index == MINER_SLOT && (stack.isEmpty() || stack.getItem() != this.currentMinerItem)) {
            this.resetMining();
        }
        this.markDirty();
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return this.world.getTileEntity(this.pos) == this
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
        return index == MINER_SLOT && this.isValidMiner(stack);
    }

    @Override
    public int getField(int id) {
        if (id == 0) {
            return this.miningTime;
        }
        if (id == 1) {
            return this.maxMiningTime;
        }
        return 0;
    }

    @Override
    public void setField(int id, int value) {
        if (id == 0) {
            this.miningTime = value;
        } else if (id == 1) {
            this.maxMiningTime = value;
        }
    }

    @Override
    public int getFieldCount() {
        return 2;
    }

    @Override
    public void clear() {
        this.stacks = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
        this.resetMining();
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        return side == EnumFacing.UP ? INPUT_SLOTS : OUTPUT_SLOTS;
    }

    @Override
    public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
        return index == MINER_SLOT && direction == EnumFacing.UP && this.isItemValidForSlot(index, itemStackIn)
                && this.getStackInSlot(MINER_SLOT).isEmpty();
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        return index >= FIRST_OUTPUT_SLOT && index < SLOT_COUNT;
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
            if (facing == null) {
                return (T) this.itemHandlerAnySide;
            }
            if (facing == EnumFacing.UP) {
                return (T) this.itemHandlerTop;
            }
            if (facing == EnumFacing.DOWN) {
                return (T) this.itemHandlerBottom;
            }
            return (T) this.itemHandlerSide;
        }
        return super.getCapability(capability, facing);
    }
}
