package com.occultism.occultism.util;

import java.util.function.Predicate;
import java.util.function.ToIntFunction;

import javax.annotation.Nullable;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public final class InventoryAccess {

    private InventoryAccess() {
    }

    public static boolean isInventory(@Nullable TileEntity tile) {
        return isInventory(tile, null);
    }

    public static boolean isInventory(@Nullable TileEntity tile, @Nullable EnumFacing facing) {
        return tile instanceof IInventory || selectItemHandler(tile, facing, handler -> true) != null;
    }

    public static boolean hasSpace(@Nullable TileEntity tile, ItemStack stack) {
        return hasSpace(tile, stack, null);
    }

    public static boolean hasSpace(@Nullable TileEntity tile, ItemStack stack, @Nullable EnumFacing facing) {
        if (tile instanceof IInventory) {
            return hasInventorySpace((IInventory) tile, stack, facing);
        }
        IItemHandler handler = selectItemHandler(tile, facing, itemHandler -> hasHandlerSpace(itemHandler, stack));
        return handler != null && hasHandlerSpace(handler, stack);
    }

    public static boolean hasAnyExtractable(@Nullable TileEntity tile) {
        return hasAnyExtractable(tile, null, stack -> true);
    }

    public static boolean hasAnyExtractable(@Nullable TileEntity tile, @Nullable EnumFacing facing) {
        return hasAnyExtractable(tile, facing, stack -> true);
    }

    public static boolean hasAnyExtractable(@Nullable TileEntity tile, Predicate<ItemStack> predicate) {
        return hasAnyExtractable(tile, null, predicate);
    }

    public static boolean hasAnyExtractable(@Nullable TileEntity tile, @Nullable EnumFacing facing,
            Predicate<ItemStack> predicate) {
        Predicate<ItemStack> filter = predicate == null ? stack -> true : predicate;
        if (tile instanceof IInventory) {
            return hasInventoryExtractable((IInventory) tile, facing, filter);
        }

        return selectItemHandler(tile, facing, handler -> hasHandlerExtractable(handler, filter)) != null;
    }

    public static ItemStack insert(@Nullable TileEntity tile, ItemStack stack) {
        return insert(tile, stack, null);
    }

    public static ItemStack insert(@Nullable TileEntity tile, ItemStack stack, @Nullable EnumFacing facing) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (tile instanceof IInventory) {
            return insertIntoInventory((IInventory) tile, stack, facing);
        }
        IItemHandler handler = selectItemHandler(tile, facing, itemHandler -> hasHandlerSpace(itemHandler, stack));
        return handler == null ? stack.copy() : insertIntoHandler(handler, stack);
    }

    public static ItemStack extractFirst(@Nullable TileEntity tile, ToIntFunction<ItemStack> maxCount,
            Predicate<ItemStack> predicate) {
        return extractFirst(tile, null, maxCount, predicate);
    }

    public static ItemStack extractFirst(@Nullable TileEntity tile, @Nullable EnumFacing facing,
            ToIntFunction<ItemStack> maxCount, Predicate<ItemStack> predicate) {
        if (tile instanceof IInventory) {
            return extractFirstFromInventory((IInventory) tile, facing, maxCount, predicate);
        }
        ItemStack extracted = extractFirstFromHandler(getDirectItemHandler(tile, facing), maxCount, predicate);
        if (!extracted.isEmpty()) {
            return extracted;
        }
        if (facing != null) {
            extracted = extractFirstFromHandler(getDirectItemHandler(tile, null), maxCount, predicate);
            if (!extracted.isEmpty()) {
                return extracted;
            }
        }
        for (EnumFacing side : EnumFacing.values()) {
            if (side == facing) {
                continue;
            }
            extracted = extractFirstFromHandler(getDirectItemHandler(tile, side), maxCount, predicate);
            if (!extracted.isEmpty()) {
                return extracted;
            }
        }
        return ItemStack.EMPTY;
    }

    @Nullable
    private static IItemHandler selectItemHandler(@Nullable TileEntity tile, @Nullable EnumFacing facing,
            Predicate<IItemHandler> predicate) {
        IItemHandler handler = getDirectItemHandler(tile, facing);
        if (handler != null && predicate.test(handler)) {
            return handler;
        }
        if (facing != null) {
            handler = getDirectItemHandler(tile, null);
            if (handler != null && predicate.test(handler)) {
                return handler;
            }
        }
        for (EnumFacing side : EnumFacing.values()) {
            if (side == facing) {
                continue;
            }
            handler = getDirectItemHandler(tile, side);
            if (handler != null && predicate.test(handler)) {
                return handler;
            }
        }
        return null;
    }

    @Nullable
    private static IItemHandler getDirectItemHandler(@Nullable TileEntity tile, @Nullable EnumFacing facing) {
        if (tile == null || !tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing)) {
            return null;
        }
        return tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing);
    }

    private static boolean hasInventorySpace(IInventory inventory, ItemStack stack, @Nullable EnumFacing facing) {
        if (stack.isEmpty()) {
            return false;
        }
        if (inventory instanceof ISidedInventory && facing != null) {
            ISidedInventory sided = (ISidedInventory) inventory;
            for (int slot : sided.getSlotsForFace(facing)) {
                if (canInsertIntoInventorySlot(inventory, slot, stack, facing)) {
                    return true;
                }
            }
            return false;
        }
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
            if (canInsertIntoInventorySlot(inventory, slot, stack, null)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasInventoryExtractable(IInventory inventory, @Nullable EnumFacing facing,
            Predicate<ItemStack> predicate) {
        if (inventory instanceof ISidedInventory && facing != null) {
            ISidedInventory sided = (ISidedInventory) inventory;
            for (int slot : sided.getSlotsForFace(facing)) {
                ItemStack existing = inventory.getStackInSlot(slot);
                if (!existing.isEmpty() && predicate.test(existing)
                        && sided.canExtractItem(slot, existing, facing)) {
                    return true;
                }
            }
            return false;
        }
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
            ItemStack existing = inventory.getStackInSlot(slot);
            if (!existing.isEmpty() && predicate.test(existing)) {
                return true;
            }
        }
        return false;
    }

    private static boolean canInsertIntoInventorySlot(IInventory inventory, int slot, ItemStack stack,
            @Nullable EnumFacing facing) {
        if (!inventory.isItemValidForSlot(slot, stack)) {
            return false;
        }
        if (inventory instanceof ISidedInventory && facing != null
                && !((ISidedInventory) inventory).canInsertItem(slot, stack, facing)) {
            return false;
        }
        ItemStack existing = inventory.getStackInSlot(slot);
        if (existing.isEmpty()) {
            return true;
        }
        return canMergeStacks(existing, stack)
                && existing.getCount() < Math.min(existing.getMaxStackSize(), inventory.getInventoryStackLimit());
    }

    private static boolean hasHandlerSpace(IItemHandler handler, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack remaining = handler.insertItem(slot, stack.copy(), true);
            if (remaining.isEmpty() || remaining.getCount() < stack.getCount()) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasHandlerExtractable(IItemHandler handler, Predicate<ItemStack> predicate) {
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack extracted = handler.extractItem(slot, 1, true);
            if (!extracted.isEmpty() && predicate.test(extracted)) {
                return true;
            }
        }
        return false;
    }

    private static ItemStack insertIntoInventory(IInventory inventory, ItemStack stack, @Nullable EnumFacing facing) {
        ItemStack remaining = stack.copy();
        if (inventory instanceof ISidedInventory && facing != null) {
            ISidedInventory sided = (ISidedInventory) inventory;
            for (int slot : sided.getSlotsForFace(facing)) {
                remaining = insertIntoInventorySlot(inventory, slot, remaining, facing);
                if (remaining.isEmpty()) {
                    break;
                }
            }
            inventory.markDirty();
            return remaining;
        }
        for (int slot = 0; slot < inventory.getSizeInventory() && !remaining.isEmpty(); slot++) {
            remaining = insertIntoInventorySlot(inventory, slot, remaining, null);
        }
        inventory.markDirty();
        return remaining;
    }

    private static ItemStack insertIntoInventorySlot(IInventory inventory, int slot, ItemStack stack,
            @Nullable EnumFacing facing) {
        if (stack.isEmpty() || !canInsertIntoInventorySlot(inventory, slot, stack, facing)) {
            return stack;
        }
        ItemStack remaining = stack.copy();
        ItemStack existing = inventory.getStackInSlot(slot);
        if (existing.isEmpty()) {
            int insertCount = Math.min(remaining.getCount(),
                    Math.min(remaining.getMaxStackSize(), inventory.getInventoryStackLimit()));
            ItemStack inserted = remaining.copy();
            inserted.setCount(insertCount);
            inventory.setInventorySlotContents(slot, inserted);
            remaining.shrink(insertCount);
            return remaining;
        }
        int maxStackSize = Math.min(existing.getMaxStackSize(), inventory.getInventoryStackLimit());
        int insertCount = Math.min(remaining.getCount(), maxStackSize - existing.getCount());
        if (insertCount <= 0) {
            return remaining;
        }
        existing.grow(insertCount);
        inventory.setInventorySlotContents(slot, existing);
        remaining.shrink(insertCount);
        return remaining;
    }

    private static ItemStack insertIntoHandler(IItemHandler handler, ItemStack stack) {
        ItemStack remaining = stack.copy();
        for (int slot = 0; slot < handler.getSlots() && !remaining.isEmpty(); slot++) {
            remaining = handler.insertItem(slot, remaining, false);
        }
        return remaining;
    }

    private static ItemStack extractFirstFromInventory(IInventory inventory, @Nullable EnumFacing facing,
            ToIntFunction<ItemStack> maxCount, Predicate<ItemStack> predicate) {
        if (inventory instanceof ISidedInventory && facing != null) {
            ISidedInventory sided = (ISidedInventory) inventory;
            for (int slot : sided.getSlotsForFace(facing)) {
                ItemStack extracted = extractFromInventorySlot(inventory, slot, facing, maxCount, predicate);
                if (!extracted.isEmpty()) {
                    return extracted;
                }
            }
            return ItemStack.EMPTY;
        }
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
            ItemStack extracted = extractFromInventorySlot(inventory, slot, null, maxCount, predicate);
            if (!extracted.isEmpty()) {
                return extracted;
            }
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack extractFromInventorySlot(IInventory inventory, int slot, @Nullable EnumFacing facing,
            ToIntFunction<ItemStack> maxCount, Predicate<ItemStack> predicate) {
        ItemStack existing = inventory.getStackInSlot(slot);
        if (existing.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack probe = existing.copy();
        probe.setCount(Math.min(existing.getCount(), Math.max(0, maxCount.applyAsInt(existing))));
        if (probe.isEmpty() || !predicate.test(probe)) {
            return ItemStack.EMPTY;
        }
        if (inventory instanceof ISidedInventory && facing != null
                && !((ISidedInventory) inventory).canExtractItem(slot, probe, facing)) {
            return ItemStack.EMPTY;
        }
        ItemStack extracted = inventory.decrStackSize(slot, probe.getCount());
        inventory.markDirty();
        return extracted;
    }

    private static ItemStack extractFirstFromHandler(IItemHandler handler, ToIntFunction<ItemStack> maxCount,
            Predicate<ItemStack> predicate) {
        if (handler == null) {
            return ItemStack.EMPTY;
        }
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack existing = handler.getStackInSlot(slot);
            if (existing.isEmpty()) {
                continue;
            }
            int count = Math.min(existing.getCount(), Math.max(0, maxCount.applyAsInt(existing)));
            if (count <= 0) {
                continue;
            }
            ItemStack probe = handler.extractItem(slot, count, true);
            if (probe.isEmpty() || !predicate.test(probe)) {
                continue;
            }
            return handler.extractItem(slot, probe.getCount(), false);
        }
        return ItemStack.EMPTY;
    }

    private static boolean canMergeStacks(ItemStack existing, ItemStack incoming) {
        return ItemStack.areItemsEqual(existing, incoming) && ItemStack.areItemStackTagsEqual(existing, incoming);
    }
}
