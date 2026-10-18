package com.occultism.occultism.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;

public class SatchelInventory extends InventoryBasic {

    public static final int SIZE = 13 * 9; // upstream SatchelContainer.SATCHEL_SIZE
    private static final String TAG_ITEMS = "Items";

    private final ItemStack satchelStack;
    private final NonNullList<ItemStack> contents;

    public SatchelInventory(ItemStack satchelStack) {
        super(satchelStack.getDisplayName(), true, SIZE);
        this.satchelStack = satchelStack;
        this.contents = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        this.readFromStack();
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return index >= 0 && index < this.contents.size() ? this.contents.get(index) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack stack = ItemStackHelper.getAndSplit(this.contents, index, count);
        if (!stack.isEmpty()) {
            this.markDirty();
        }
        return stack;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack stack = ItemStackHelper.getAndRemove(this.contents, index);
        if (!stack.isEmpty()) {
            this.markDirty();
        }
        return stack;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        this.contents.set(index, stack);
        if (!stack.isEmpty() && stack.getCount() > this.getInventoryStackLimit()) {
            stack.setCount(this.getInventoryStackLimit());
        }
        this.markDirty();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.contents) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void markDirty() {
        this.writeToStack();
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return !this.satchelStack.isEmpty();
    }

    @Override
    public void clear() {
        for (int slot = 0; slot < this.contents.size(); slot++) {
            this.contents.set(slot, ItemStack.EMPTY);
        }
        this.markDirty();
    }

    public ItemStack insert(ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack remaining = stack.copy();
        for (int slot = 0; slot < this.contents.size() && !remaining.isEmpty(); slot++) {
            remaining = this.insertIntoOccupiedSlot(slot, remaining);
        }
        for (int slot = 0; slot < this.contents.size() && !remaining.isEmpty(); slot++) {
            remaining = this.insertIntoEmptySlot(slot, remaining);
        }
        return remaining.isEmpty() ? ItemStack.EMPTY : remaining;
    }

    public void writeToStack() {
        NBTTagCompound tag = this.satchelStack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            this.satchelStack.setTagCompound(tag);
        }

        ItemStackHelper.saveAllItems(tag, this.contents, true);
    }

    private void readFromStack() {
        NBTTagCompound tag = this.satchelStack.getTagCompound();
        if (tag != null && tag.hasKey(TAG_ITEMS, 9)) {
            ItemStackHelper.loadAllItems(tag, this.contents);
        }
    }

    private ItemStack insertIntoOccupiedSlot(int slot, ItemStack stack) {
        ItemStack existing = this.contents.get(slot);
        if (existing.isEmpty() || !canMergeStacks(existing, stack)) {
            return stack;
        }

        int limit = Math.min(existing.getMaxStackSize(), this.getInventoryStackLimit());
        int insertCount = Math.min(stack.getCount(), limit - existing.getCount());
        if (insertCount <= 0) {
            return stack;
        }

        ItemStack remaining = stack.copy();
        existing.grow(insertCount);
        remaining.shrink(insertCount);
        this.markDirty();
        return remaining;
    }

    private ItemStack insertIntoEmptySlot(int slot, ItemStack stack) {
        if (!this.contents.get(slot).isEmpty()) {
            return stack;
        }

        int insertCount = Math.min(stack.getCount(),
                Math.min(stack.getMaxStackSize(), this.getInventoryStackLimit()));
        if (insertCount <= 0) {
            return stack;
        }

        ItemStack inserted = stack.copy();
        inserted.setCount(insertCount);
        this.contents.set(slot, inserted);
        ItemStack remaining = stack.copy();
        remaining.shrink(insertCount);
        this.markDirty();
        return remaining;
    }

    private static boolean canMergeStacks(ItemStack existing, ItemStack incoming) {
        return ItemStack.areItemsEqual(existing, incoming) && ItemStack.areItemStackTagsEqual(existing, incoming);
    }
}
