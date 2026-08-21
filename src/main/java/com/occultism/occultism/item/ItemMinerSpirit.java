package com.occultism.occultism.item;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.occultism.occultism.util.ItemNBTUtil;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemMinerSpirit extends Item {

    private final int maxMiningTime;
    private final int rollsPerOperation;
    private final int outputMultiplier;

    public ItemMinerSpirit(int maxMiningTime, int rollsPerOperation, int outputMultiplier, int durability) {
        this.maxMiningTime = maxMiningTime;
        this.rollsPerOperation = rollsPerOperation;
        this.outputMultiplier = outputMultiplier;
        this.setMaxStackSize(1);
        this.setMaxDamage(durability);
    }

    public int getMaxMiningTime() {
        return this.maxMiningTime;
    }

    public int getRollsPerOperation() {
        return this.rollsPerOperation;
    }

    public int getOutputMultiplier() {
        return this.outputMultiplier;
    }

    @Override
    public int getItemEnchantability() {
        return 15;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return this.isSupportedMinerEnchantment(enchantment) || super.canApplyAtEnchantingTable(stack, enchantment);
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        for (Map.Entry<Enchantment, Integer> entry : EnchantmentHelper.getEnchantments(book).entrySet()) {
            if (this.isSupportedMinerEnchantment(entry.getKey())) {
                return true;
            }
        }
        return false;
    }

    private boolean isSupportedMinerEnchantment(Enchantment enchantment) {
        return enchantment == Enchantments.EFFICIENCY || enchantment == Enchantments.FORTUNE
                || enchantment == Enchantments.SILK_TOUCH;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        String name = ItemNBTUtil.getBoundSpiritName(stack);
        if (!name.isEmpty()) {
            tooltip.add(I18n.format("item.occultism.bound_spirit.tooltip", ItemNBTUtil.formatSpiritName(name)));
        }
    }
}
