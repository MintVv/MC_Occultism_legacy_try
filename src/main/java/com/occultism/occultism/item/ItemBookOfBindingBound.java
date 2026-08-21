package com.occultism.occultism.item;

import java.util.List;

import javax.annotation.Nullable;

import com.occultism.occultism.util.ItemNBTUtil;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBookOfBindingBound extends Item {

    @Override
    public void onCreated(ItemStack stack, World worldIn, EntityPlayer playerIn) {
        if (!worldIn.isRemote) {
            ItemNBTUtil.generateBoundSpiritNameIfNone(stack);
        }
        super.onCreated(stack, worldIn, playerIn);
    }

    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (!worldIn.isRemote && entityIn instanceof EntityPlayer && ItemNBTUtil.generateBoundSpiritNameIfNone(stack)) {
            ((EntityPlayer) entityIn).inventoryContainer.detectAndSendChanges();
        }
        super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        String name = ItemNBTUtil.getBoundSpiritName(stack);
        if (name.isEmpty()) {
            name = I18n.format("item.occultism.bound_spirit.unknown");
        } else {
            name = ItemNBTUtil.formatSpiritName(name);
        }
        tooltip.add(I18n.format("item.occultism.bound_spirit.tooltip", name));
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return true;
    }
}
