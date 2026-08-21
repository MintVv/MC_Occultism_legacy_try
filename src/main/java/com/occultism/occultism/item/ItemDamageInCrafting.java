package com.occultism.occultism.item;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemDamageInCrafting extends Item {

    public ItemDamageInCrafting(int durability) {
        this.setMaxDamage(durability);
        this.setMaxStackSize(1);
    }

    @Override
    public boolean hasContainerItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getContainerItem(ItemStack itemStack) {
        ItemStack copy = itemStack.copy();
        copy.setCount(1);
        int nextDamage = copy.getItemDamage() + 1;
        if (nextDamage >= copy.getMaxDamage()) {
            return ItemStack.EMPTY;
        }
        copy.setItemDamage(nextDamage);
        return copy;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        String key = this.getTranslationKey() + ".auto_tooltip";
        String text = I18n.format(key);
        if (text.equals(key)) {
            return;
        }
        for (String line : text.split("\\\\n|\\n")) {
            if (!line.isEmpty()) {
                tooltip.add(line);
            }
        }
    }
}
