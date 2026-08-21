package com.occultism.occultism.item;

import java.util.List;

import javax.annotation.Nullable;

import com.occultism.occultism.util.ItemNBTUtil;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemInfusedPickaxe extends ItemPickaxe {

    public ItemInfusedPickaxe(ToolMaterial material) {
        super(material);
        this.setMaxStackSize(1);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        addTranslatedTooltip(tooltip, this.getTranslationKey() + ".auto_tooltip");

        String spiritName = ItemNBTUtil.getBoundSpiritName(stack);
        if (spiritName.isEmpty()) {
            spiritName = I18n.format("item.occultism.bound_spirit.unknown");
        } else {
            spiritName = ItemNBTUtil.formatSpiritName(spiritName);
        }
        tooltip.add(I18n.format(this.getTranslationKey() + ".tooltip", spiritName));
    }

    private static void addTranslatedTooltip(List<String> tooltip, String key) {
        String text = I18n.format(key);
        if (key.equals(text)) {
            return;
        }
        for (String line : text.split("\\\\n")) {
            if (!line.isEmpty()) {
                tooltip.add(line);
            }
        }
    }
}
