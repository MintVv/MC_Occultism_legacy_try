package com.occultism.occultism.container;

import com.occultism.occultism.entity.EntityFoliotJob;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.EntityHandsInvWrapper;

/** Server-authoritative inventory view for a summoned work spirit. */
public class ContainerSpirit extends Container {

    protected static final int PLAYER_INVENTORY_START = 0;
    protected static final int PLAYER_HOTBAR_START = 27;
    protected static final int PLAYER_SLOT_END = 36;
    protected static final int SPIRIT_SLOT = 36;
    protected static final int BASE_SLOT_END = 37;

    private final EntityFoliotJob spirit;

    public ContainerSpirit(InventoryPlayer playerInventory, EntityFoliotJob spirit) {
        this.spirit = spirit;
        this.setupPlayerInventorySlots(playerInventory);
        this.setupPlayerHotbar(playerInventory);
        this.setupEntityInventory();
    }

    protected void setupPlayerInventorySlots(InventoryPlayer playerInventory) {
        int playerInventoryTop = 84;
        int playerInventoryLeft = 8;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlotToContainer(new Slot(playerInventory, column + row * 9 + 9,
                        playerInventoryLeft + column * 18, playerInventoryTop + row * 18));
            }
        }
    }

    protected void setupPlayerHotbar(InventoryPlayer playerInventory) {
        int hotbarTop = 142;
        int hotbarLeft = 8;
        for (int column = 0; column < 9; column++) {
            this.addSlotToContainer(new Slot(playerInventory, column, hotbarLeft + column * 18, hotbarTop));
        }
    }

    protected void setupEntityInventory() {
        this.addSlotToContainer(new SlotItemHandler(new EntityHandsInvWrapper(spirit), 0, 152, 54));
    }

    public EntityFoliotJob getSpirit() {
        return this.spirit;
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return playerIn != null && this.spirit != null && this.spirit.isEntityAlive()
                && this.spirit.world == playerIn.world
                && playerIn.world.getEntityByID(this.spirit.getEntityId()) == this.spirit
                && this.spirit.getDistanceSq(playerIn) < 64.0D;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        if (index < 0 || index >= BASE_SLOT_END) {
            return ItemStack.EMPTY;
        }
        Slot slot = this.inventorySlots.get(index);
        if (slot == null || !slot.getHasStack()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getStack();
        ItemStack original = stack.copy();
        if (index == SPIRIT_SLOT) {
            if (!this.mergeItemStack(stack, PLAYER_INVENTORY_START, PLAYER_SLOT_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.mergeItemStack(stack, SPIRIT_SLOT, BASE_SLOT_END, false)) {
            if (index >= PLAYER_INVENTORY_START && index < PLAYER_HOTBAR_START) {
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
}
