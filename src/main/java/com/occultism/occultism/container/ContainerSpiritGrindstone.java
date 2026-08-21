package com.occultism.occultism.container;

import com.google.common.collect.Maps;
import com.occultism.occultism.registry.ModBlocks;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ContainerSpiritGrindstone extends Container {

    private static final int INPUT_0 = 0;
    private static final int INPUT_1 = 1;
    private static final int RESULT = 2;
    private static final int PLAYER_INVENTORY_START = 3;
    private static final int PLAYER_HOTBAR_START = PLAYER_INVENTORY_START + 27;
    private static final int PLAYER_SLOT_END = PLAYER_HOTBAR_START + 9;
    private static final float REPAIR_DURABILITY_MULTIPLIER = 1.2F;

    private final IInventory inputSlots = new InventoryBasic("Repair", true, 2) {
        @Override
        public void markDirty() {
            super.markDirty();
            ContainerSpiritGrindstone.this.onCraftMatrixChanged(this);
        }
    };
    private final IInventory outputSlot = new InventoryCraftResult();
    private final World world;
    private final BlockPos pos;

    public ContainerSpiritGrindstone(InventoryPlayer playerInventory, World world, BlockPos pos) {
        this.world = world;
        this.pos = pos;

        this.addSlotToContainer(new InputSlot(this.inputSlots, INPUT_0, 49, 19));
        this.addSlotToContainer(new InputSlot(this.inputSlots, INPUT_1, 49, 40));
        this.addSlotToContainer(new ResultSlot(playerInventory.player, this.inputSlots, this.outputSlot, RESULT, 129, 34));

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlotToContainer(new Slot(playerInventory, column + row * 9 + 9,
                        8 + column * 18, 84 + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            this.addSlotToContainer(new Slot(playerInventory, column, 8 + column * 18, 142));
        }

        this.updateResult();
    }

    @Override
    public void onCraftMatrixChanged(IInventory inventoryIn) {
        super.onCraftMatrixChanged(inventoryIn);
        if (inventoryIn == this.inputSlots) {
            this.updateResult();
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return this.world.getBlockState(this.pos).getBlock() == ModBlocks.SPIRIT_GRINDSTONE
                && playerIn.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D,
                this.pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void onContainerClosed(EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);
        if (!this.world.isRemote) {
            this.clearContainer(playerIn, this.world, this.inputSlots);
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack original = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot == null || !slot.getHasStack()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getStack();
        original = stack.copy();

        if (index == RESULT) {
            if (!this.mergeItemStack(stack, PLAYER_INVENTORY_START, PLAYER_SLOT_END, true)) {
                return ItemStack.EMPTY;
            }
            slot.onSlotChange(stack, original);
        } else if (index == INPUT_0 || index == INPUT_1) {
            if (!this.mergeItemStack(stack, PLAYER_INVENTORY_START, PLAYER_SLOT_END, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (canProcess(stack)) {
                if (!this.mergeItemStack(stack, INPUT_0, RESULT, false)) {
                    if (index >= PLAYER_INVENTORY_START && index < PLAYER_HOTBAR_START) {
                        if (!this.mergeItemStack(stack, PLAYER_HOTBAR_START, PLAYER_SLOT_END, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (index >= PLAYER_HOTBAR_START && index < PLAYER_SLOT_END
                            && !this.mergeItemStack(stack, PLAYER_INVENTORY_START, PLAYER_HOTBAR_START, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (index >= PLAYER_INVENTORY_START && index < PLAYER_HOTBAR_START) {
                if (!this.mergeItemStack(stack, PLAYER_HOTBAR_START, PLAYER_SLOT_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= PLAYER_HOTBAR_START && index < PLAYER_SLOT_END
                    && !this.mergeItemStack(stack, PLAYER_INVENTORY_START, PLAYER_HOTBAR_START, false)) {
                return ItemStack.EMPTY;
            }
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
        return original;
    }

    private void updateResult() {
        this.outputSlot.setInventorySlotContents(0,
                this.computeResult(this.inputSlots.getStackInSlot(INPUT_0), this.inputSlots.getStackInSlot(INPUT_1)));
        this.detectAndSendChanges();
    }

    private ItemStack computeResult(ItemStack first, ItemStack second) {
        if (first.isEmpty() && second.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (first.getCount() > 1 || second.getCount() > 1) {
            return ItemStack.EMPTY;
        }
        if (first.isEmpty() || second.isEmpty()) {
            ItemStack source = first.isEmpty() ? second : first;
            return hasAnyEnchantment(source) ? this.removeOnlyCursesFrom(source.copy(), true) : ItemStack.EMPTY;
        }
        return this.mergeItems(first, second);
    }

    private ItemStack mergeItems(ItemStack first, ItemStack second) {
        if (first.getItem() != second.getItem() || !first.isItemStackDamageable()) {
            return ItemStack.EMPTY;
        }

        ItemStack result = first.copy();
        result.setCount(1);
        int maxDamage = result.getMaxDamage();
        int remaining = first.getMaxDamage() - first.getItemDamage()
                + second.getMaxDamage() - second.getItemDamage()
                + (int) (first.getMaxDamage() * (REPAIR_DURABILITY_MULTIPLIER - 1.0F));
        result.setItemDamage(Math.max(0, maxDamage - remaining));
        return this.removeOnlyCursesFrom(result, false);
    }

    private ItemStack removeOnlyCursesFrom(ItemStack stack, boolean requireRemovedCurse) {
        Map<Enchantment, Integer> enchantments = Maps.newLinkedHashMap(EnchantmentHelper.getEnchantments(stack));
        boolean changed = false;
        Iterator<Map.Entry<Enchantment, Integer>> iterator = enchantments.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Enchantment, Integer> entry = iterator.next();
            if (entry.getKey() == null || entry.getKey().isCurse()) {
                iterator.remove();
                changed = true;
            }
        }

        if (requireRemovedCurse && !changed) {
            return ItemStack.EMPTY;
        }

        if (stack.getItem() == Items.ENCHANTED_BOOK) {
            if (enchantments.isEmpty()) {
                return new ItemStack(Items.BOOK);
            }
            ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                ItemEnchantedBook.addEnchantment(book, new EnchantmentData(entry.getKey(), entry.getValue()));
            }
            return book;
        }

        EnchantmentHelper.setEnchantments(enchantments, stack);
        if (enchantments.isEmpty() && stack.hasTagCompound()) {
            NBTTagCompound tag = stack.getTagCompound();
            tag.removeTag("ench");
            if (tag.isEmpty()) {
                stack.setTagCompound(null);
            }
        }
        return stack;
    }

    private static boolean canProcess(ItemStack stack) {
        return !stack.isEmpty() && (stack.isItemStackDamageable() || hasAnyEnchantment(stack));
    }

    private static boolean hasAnyEnchantment(ItemStack stack) {
        return !EnchantmentHelper.getEnchantments(stack).isEmpty();
    }

    private static class InputSlot extends Slot {

        InputSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
            super(inventoryIn, index, xPosition, yPosition);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return canProcess(stack);
        }

        @Override
        public int getSlotStackLimit() {
            return 1;
        }
    }

    private class ResultSlot extends Slot {

        private final EntityPlayer player;
        private final IInventory inputs;

        ResultSlot(EntityPlayer player, IInventory inputs, IInventory inventoryIn, int index, int xPosition,
                int yPosition) {
            super(inventoryIn, 0, xPosition, yPosition);
            this.player = player;
            this.inputs = inputs;
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return false;
        }

        @Override
        public ItemStack onTake(EntityPlayer thePlayer, ItemStack stack) {
            this.inputs.setInventorySlotContents(INPUT_0, ItemStack.EMPTY);
            this.inputs.setInventorySlotContents(INPUT_1, ItemStack.EMPTY);
            if (!ContainerSpiritGrindstone.this.world.isRemote) {
                ContainerSpiritGrindstone.this.world.playEvent(1030, ContainerSpiritGrindstone.this.pos, 0);
            }
            return super.onTake(this.player, stack);
        }
    }
}
