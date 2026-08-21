package com.occultism.occultism.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;

public class EnchantmentFractureSoul extends Enchantment {

    public EnchantmentFractureSoul() {
        super(Rarity.VERY_RARE, EnumEnchantmentType.WEAPON, new EntityEquipmentSlot[] {EntityEquipmentSlot.MAINHAND});
        this.setName("occultism.fracture_soul");
    }

    @Override
    public int getMinEnchantability(int enchantmentLevel) {
        return 24 + (enchantmentLevel - 1) * 4;
    }

    @Override
    public int getMaxEnchantability(int enchantmentLevel) {
        return 27 + (enchantmentLevel - 1) * 3;
    }

    @Override
    public int getMaxLevel() {
        return 9;
    }

    @Override
    public boolean canApply(ItemStack stack) {
        return stack.getItem() instanceof ItemSword || stack.getItem() instanceof ItemAxe
                || super.canApply(stack);
    }
}
