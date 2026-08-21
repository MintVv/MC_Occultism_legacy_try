package com.occultism.occultism.item;

import java.util.List;

import javax.annotation.Nullable;

import com.occultism.occultism.Occultism;
import com.occultism.occultism.gui.ModGuiHandler;
import com.occultism.occultism.util.ItemNBTUtil;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemEnderSatchel extends Item {

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        if (!worldIn.isRemote) {
            int slot = handIn == EnumHand.MAIN_HAND ? playerIn.inventory.currentItem : 40;
            playerIn.openGui(Occultism.INSTANCE, ModGuiHandler.ENDER_SATCHEL, worldIn, slot, 0, 0);
            worldIn.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ, SoundEvents.BLOCK_ENDERCHEST_OPEN,
                    SoundCategory.PLAYERS, 1.0F, 1.0F);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
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
