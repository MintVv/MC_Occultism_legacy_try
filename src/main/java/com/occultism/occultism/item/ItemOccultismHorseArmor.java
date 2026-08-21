package com.occultism.occultism.item;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.HorseArmorType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemOccultismHorseArmor extends Item {

    private final HorseArmorType armorType;
    private final String armorTexture;

    public ItemOccultismHorseArmor(HorseArmorType armorType, String armorTexture) {
        this.armorType = armorType;
        this.armorTexture = armorTexture;
        this.setMaxStackSize(1);
    }

    @Override
    public HorseArmorType getHorseArmorType(ItemStack stack) {
        return this.armorType == null ? HorseArmorType.NONE : this.armorType;
    }

    @Override
    public String getHorseArmorTexture(EntityLiving wearer, ItemStack stack) {
        return this.armorTexture;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        String key = this.getTranslationKey() + ".auto_tooltip";
        if (!I18n.hasKey(key)) {
            return;
        }
        for (String line : I18n.format(key).split("\\\\n|\\r?\\n")) {
            if (!line.isEmpty()) {
                tooltip.add(line);
            }
        }
    }
}
