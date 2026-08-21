package com.occultism.occultism.item;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemIesniumPickaxe extends ItemPickaxe {

    public ItemIesniumPickaxe(ToolMaterial material) {
        super(material);
        this.setMaxStackSize(1);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        String text = I18n.format(this.getTranslationKey() + ".auto_tooltip");
        if (text.equals(this.getTranslationKey() + ".auto_tooltip")) {
            return;
        }
        for (String line : text.split("\\\\n")) {
            if (!line.isEmpty()) {
                tooltip.add(line);
            }
        }
    }
}
