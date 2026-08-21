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

public class ItemRitualDummy extends Item {

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip,
            ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        this.addTranslatedTooltip(tooltip, this.getTranslationKey() + ".auto_tooltip");
        this.addTranslatedTooltip(tooltip, this.getTranslationKey() + ".tooltip");
    }

    @SideOnly(Side.CLIENT)
    private void addTranslatedTooltip(List<String> tooltip, String key) {
        if (!I18n.hasKey(key)) {
            return;
        }

        String text = I18n.format(key);
        if (text == null || text.isEmpty()) {
            return;
        }

        for (String line : text.split("\\\\n|\\r?\\n")) {
            if (!line.isEmpty()) {
                tooltip.add(line);
            }
        }
    }
}
