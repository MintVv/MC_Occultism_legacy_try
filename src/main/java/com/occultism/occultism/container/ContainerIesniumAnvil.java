package com.occultism.occultism.container;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.occultism.occultism.registry.ModBlocks;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerRepair;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ContainerIesniumAnvil extends ContainerRepair {

    private static final int MAXIMUM_RAW_COST = 100;

    private final IInventory outputSlot;
    private final IInventory inputSlots;
    private final World world;
    private final BlockPos pos;
    private final EntityPlayer player;
    private String repairedItemName;

    public ContainerIesniumAnvil(InventoryPlayer playerInventory, final World worldIn, final BlockPos blockPosIn,
            EntityPlayer player) {
        super(playerInventory, worldIn, blockPosIn, player);
        this.inventorySlots.clear();
        this.inventoryItemStacks.clear();

        this.outputSlot = new InventoryCraftResult();
        this.inputSlots = new InventoryBasic("Iesnium Repair", true, 2) {
            @Override
            public void markDirty() {
                super.markDirty();
                ContainerIesniumAnvil.this.onCraftMatrixChanged(this);
            }
        };
        this.world = worldIn;
        this.pos = blockPosIn;
        this.player = player;

        this.addSlotToContainer(new Slot(this.inputSlots, 0, 27, 47));
        this.addSlotToContainer(new Slot(this.inputSlots, 1, 76, 47));
        this.addSlotToContainer(new Slot(this.outputSlot, 2, 134, 47) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return false;
            }

            @Override
            public boolean canTakeStack(EntityPlayer playerIn) {
                return (playerIn.capabilities.isCreativeMode || playerIn.experienceLevel >= ContainerIesniumAnvil.this.maximumCost)
                        && ContainerIesniumAnvil.this.maximumCost > 0 && this.getHasStack();
            }

            @Override
            public ItemStack onTake(EntityPlayer thePlayer, ItemStack stack) {
                if (!thePlayer.capabilities.isCreativeMode) {
                    thePlayer.addExperienceLevel(-ContainerIesniumAnvil.this.maximumCost);
                }

                net.minecraftforge.common.ForgeHooks.onAnvilRepair(thePlayer, stack,
                        ContainerIesniumAnvil.this.inputSlots.getStackInSlot(0),
                        ContainerIesniumAnvil.this.inputSlots.getStackInSlot(1));

                ContainerIesniumAnvil.this.inputSlots.setInventorySlotContents(0, ItemStack.EMPTY);

                if (ContainerIesniumAnvil.this.materialCost > 0) {
                    ItemStack material = ContainerIesniumAnvil.this.inputSlots.getStackInSlot(1);
                    if (!material.isEmpty() && material.getCount() > ContainerIesniumAnvil.this.materialCost) {
                        material.shrink(ContainerIesniumAnvil.this.materialCost);
                        ContainerIesniumAnvil.this.inputSlots.setInventorySlotContents(1, material);
                    } else {
                        ContainerIesniumAnvil.this.inputSlots.setInventorySlotContents(1, ItemStack.EMPTY);
                    }
                } else {
                    ContainerIesniumAnvil.this.inputSlots.setInventorySlotContents(1, ItemStack.EMPTY);
                }

                ContainerIesniumAnvil.this.maximumCost = 0;
                if (!ContainerIesniumAnvil.this.world.isRemote) {
                    ContainerIesniumAnvil.this.world.playEvent(1030, ContainerIesniumAnvil.this.pos, 0);
                }
                return stack;
            }
        });

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlotToContainer(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int k = 0; k < 9; ++k) {
            this.addSlotToContainer(new Slot(playerInventory, k, 8 + k * 18, 142));
        }
    }

    @Override
    public void onCraftMatrixChanged(IInventory inventoryIn) {
        super.onCraftMatrixChanged(inventoryIn);
        if (inventoryIn == this.inputSlots) {
            this.updateRepairOutput();
        }
    }

    @Override
    public void updateRepairOutput() {
        ItemStack leftInput = this.inputSlots.getStackInSlot(0);
        this.maximumCost = 1;
        int workCost = 0;
        int priorWorkCost = 0;
        int renameCost = 0;

        if (leftInput.isEmpty()) {
            this.outputSlot.setInventorySlotContents(0, ItemStack.EMPTY);
            this.maximumCost = 0;
            return;
        }

        ItemStack result = leftInput.copy();
        ItemStack rightInput = this.inputSlots.getStackInSlot(1);
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(result);
        priorWorkCost += leftInput.getRepairCost() + (rightInput.isEmpty() ? 0 : rightInput.getRepairCost() / 2);
        this.materialCost = 0;
        boolean rightIsBook = false;

        if (!rightInput.isEmpty()) {
            if (!net.minecraftforge.common.ForgeHooks.onAnvilChange(this, leftInput, rightInput, this.outputSlot,
                    this.repairedItemName, priorWorkCost)) {
                return;
            }
            rightIsBook = rightInput.getItem() == Items.ENCHANTED_BOOK
                    && !ItemEnchantedBook.getEnchantments(rightInput).isEmpty();

            if (result.isItemStackDamageable() && result.getItem().getIsRepairable(leftInput, rightInput)) {
                int damageRepaired = Math.min(result.getItemDamage(), result.getMaxDamage() / 3);
                if (damageRepaired <= 0) {
                    this.outputSlot.setInventorySlotContents(0, ItemStack.EMPTY);
                    this.maximumCost = 0;
                    return;
                }

                int materialCount;
                for (materialCount = 0; damageRepaired > 0 && materialCount < rightInput.getCount(); ++materialCount) {
                    result.setItemDamage(result.getItemDamage() - damageRepaired);
                    ++workCost;
                    damageRepaired = Math.min(result.getItemDamage(), result.getMaxDamage() / 3);
                }
                this.materialCost = materialCount;
            } else {
                if (!rightIsBook && (result.getItem() != rightInput.getItem() || !result.isItemStackDamageable())) {
                    this.outputSlot.setInventorySlotContents(0, ItemStack.EMPTY);
                    this.maximumCost = 0;
                    return;
                }

                if (result.isItemStackDamageable() && !rightIsBook) {
                    int leftDurability = leftInput.getMaxDamage() - leftInput.getItemDamage();
                    int rightDurability = rightInput.getMaxDamage() - rightInput.getItemDamage();
                    int resultDurability = leftDurability + rightDurability + result.getMaxDamage() * 13 / 100;
                    int resultDamage = result.getMaxDamage() - resultDurability;
                    if (resultDamage < 0) {
                        resultDamage = 0;
                    }
                    if (resultDamage < result.getItemDamage()) {
                        result.setItemDamage(resultDamage);
                        workCost += 2;
                    }
                }

                Map<Enchantment, Integer> rightEnchantments = EnchantmentHelper.getEnchantments(rightInput);
                boolean anyCompatible = false;
                boolean anyIncompatible = false;

                for (Enchantment enchantment : rightEnchantments.keySet()) {
                    if (enchantment == null) {
                        continue;
                    }
                    int leftLevel = enchantments.containsKey(enchantment) ? enchantments.get(enchantment) : 0;
                    int rightLevel = rightEnchantments.get(enchantment);
                    int resultLevel = leftLevel == rightLevel ? rightLevel + 1 : Math.max(rightLevel, leftLevel);
                    boolean compatible = enchantment.canApply(leftInput);

                    if (this.player.capabilities.isCreativeMode || leftInput.getItem() == Items.ENCHANTED_BOOK) {
                        compatible = true;
                    }

                    for (Enchantment existing : enchantments.keySet()) {
                        if (existing != enchantment && !enchantment.isCompatibleWith(existing)) {
                            compatible = false;
                            ++workCost;
                        }
                    }

                    if (!compatible) {
                        anyIncompatible = true;
                    } else {
                        anyCompatible = true;
                        int maximumLevel = enchantment.getMaxLevel() + 1;
                        if (resultLevel > maximumLevel) {
                            resultLevel = Math.max(leftLevel, rightLevel);
                        }
                        enchantments.put(enchantment, resultLevel);

                        int enchantmentCost = this.getEnchantmentCost(enchantment);
                        if (rightIsBook) {
                            enchantmentCost = Math.max(1, enchantmentCost / 2);
                        }
                        workCost += enchantmentCost * resultLevel;

                        if (leftInput.getCount() > 1) {
                            this.outputSlot.setInventorySlotContents(0, ItemStack.EMPTY);
                            this.maximumCost = 0;
                            return;
                        }
                    }
                }

                if (anyIncompatible && !anyCompatible) {
                    this.outputSlot.setInventorySlotContents(0, ItemStack.EMPTY);
                    this.maximumCost = 0;
                    return;
                }
            }
        }

        if (StringUtils.isBlank(this.repairedItemName)) {
            if (leftInput.hasDisplayName()) {
                renameCost = 1;
                workCost += renameCost;
                result.clearCustomName();
            }
        } else if (!this.repairedItemName.equals(leftInput.getDisplayName())) {
            renameCost = 1;
            workCost += renameCost;
            result.setStackDisplayName(this.repairedItemName);
        }

        if (rightIsBook && !result.getItem().isBookEnchantable(result, rightInput)) {
            result = ItemStack.EMPTY;
        }

        if (renameCost == workCost && renameCost > 0) {
            priorWorkCost = 0;
        }

        int rawCost = priorWorkCost + workCost;
        this.maximumCost = this.discountCost(rawCost);

        if (workCost <= 0) {
            result = ItemStack.EMPTY;
        }

        if (rawCost >= MAXIMUM_RAW_COST && !this.player.capabilities.isCreativeMode) {
            result = ItemStack.EMPTY;
        }

        if (!result.isEmpty()) {
            int newRepairCost = result.getRepairCost();
            if (!rightInput.isEmpty() && newRepairCost < rightInput.getRepairCost()) {
                newRepairCost = rightInput.getRepairCost();
            }

            if (renameCost != workCost || renameCost == 0) {
                newRepairCost = this.increaseRepairCost(newRepairCost);
            }

            result.setRepairCost(newRepairCost);
            EnchantmentHelper.setEnchantments(enchantments, result);
        }

        this.outputSlot.setInventorySlotContents(0, result);
        this.detectAndSendChanges();
    }

    private int getEnchantmentCost(Enchantment enchantment) {
        switch (enchantment.getRarity()) {
            case COMMON:
                return 1;
            case UNCOMMON:
                return 2;
            case RARE:
                return 4;
            case VERY_RARE:
                return 8;
            default:
                return 1;
        }
    }

    private int discountCost(int rawCost) {
        if (rawCost <= 0) {
            return 0;
        }
        return Math.max(1, (rawCost + 1) / 2);
    }

    private int increaseRepairCost(int oldRepairCost) {
        return oldRepairCost == Integer.MAX_VALUE ? Integer.MAX_VALUE : oldRepairCost + 1;
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        listener.sendWindowProperty(this, 0, this.maximumCost);
    }

    @Override
    public void updateProgressBar(int id, int data) {
        if (id == 0) {
            this.maximumCost = data;
        }
    }

    @Override
    public void onContainerClosed(EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);
        if (!this.world.isRemote) {
            this.clearContainer(playerIn, this.world, this.inputSlots);
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return this.world.getBlockState(this.pos).getBlock() == ModBlocks.IESNIUM_ANVIL
                && playerIn.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D,
                        this.pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack copied = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            copied = stack.copy();

            if (index == 2) {
                if (!this.mergeItemStack(stack, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onSlotChange(stack, copied);
            } else if (index != 0 && index != 1) {
                if (index >= 3 && index < 39 && !this.mergeItemStack(stack, 0, 2, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(stack, 3, 39, false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }

            if (stack.getCount() == copied.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, stack);
        }

        return copied;
    }

    @Override
    public void updateItemName(String newName) {
        this.repairedItemName = newName;

        if (this.getSlot(2).getHasStack()) {
            ItemStack result = this.getSlot(2).getStack();
            if (StringUtils.isBlank(newName)) {
                result.clearCustomName();
            } else {
                result.setStackDisplayName(this.repairedItemName);
            }
        }

        this.updateRepairOutput();
    }
}
