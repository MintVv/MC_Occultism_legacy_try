package com.occultism.occultism.item;

import java.util.List;

import javax.annotation.Nullable;

import com.occultism.occultism.util.ItemNBTUtil;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemDimensionalMatrix extends Item {

    public ItemDimensionalMatrix() {
        this.setMaxStackSize(1);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        String spiritName = ItemNBTUtil.getBoundSpiritName(stack);
        if (spiritName.isEmpty()) {
            spiritName = I18n.format("item.occultism.bound_spirit.unknown");
        } else {
            spiritName = ItemNBTUtil.formatSpiritName(spiritName);
        }
        tooltip.add(I18n.format(this.getTranslationKey() + ".tooltip", spiritName));
    }
}
