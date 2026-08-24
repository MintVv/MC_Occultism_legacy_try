package com.occultism.occultism.container;

import com.occultism.occultism.entity.EntityBoundSpirit;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

/** Server-authoritative inventory view for a non-job bound spirit. */
public class ContainerBoundSpirit extends Container {

    private static final int PLAYER_INVENTORY_START = 0;
    private static final int PLAYER_HOTBAR_START = 27;
    private static final int PLAYER_SLOT_END = 36;
    private static final int SPIRIT_SLOT = 36;
    private static final int BASE_SLOT_END = 37;

    private final EntityBoundSpirit spirit;

    public ContainerBoundSpirit(InventoryPlayer playerInventory, EntityBoundSpirit spirit) {
        this.spirit = spirit;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlotToContainer(new Slot(playerInventory, column + row * 9 + 9,
                        8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            this.addSlotToContainer(new Slot(playerInventory, column, 8 + column * 18, 142));
        }
        this.addSlotToContainer(new SlotItemHandler(spirit.getSpiritInventory(), 0, 152, 54));
    }

    public EntityBoundSpirit getSpirit() {
        return this.spirit;
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return playerIn != null && this.spirit != null && this.spirit.isEntityAlive()
                && this.spirit.world == playerIn.world
                && playerIn.world.getEntityByID(this.spirit.getEntityId()) == this.spirit
                && this.spirit.canPlayerAccess(playerIn)
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
