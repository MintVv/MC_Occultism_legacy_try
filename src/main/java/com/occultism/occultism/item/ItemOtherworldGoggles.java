package com.occultism.occultism.item;

import java.util.List;

import javax.annotation.Nullable;

import com.occultism.occultism.Tags;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemOtherworldGoggles extends ItemArmor {

    private static final String ARMOR_TEXTURE =
            Tags.MOD_ID + ":textures/models/armor/otherworld_goggles_layer_1.png";

    public ItemOtherworldGoggles(ItemArmor.ArmorMaterial material) {
        super(material, 0, EntityEquipmentSlot.HEAD);
        this.setMaxStackSize(1);
    }

    public static boolean isGoggles(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ItemOtherworldGoggles;
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        return ARMOR_TEXTURE;
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
