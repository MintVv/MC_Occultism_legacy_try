package com.occultism.occultism.item;

import java.util.List;


import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Pentacle diviner - right-click a sacrificial bowl to see the pentacle that belongs there.
 *
 * <p>This item has no upstream counterpart: newer versions put the preview behind a button in the
 * recipe viewer, which 1.12's JEI cannot host because its recipe categories expose no click hook.
 * Filling that gap with a handheld tool is the closest 1.12 equivalent to Blood Magic's ritual
 * diviner, and it is deliberately shipped as a local convenience rather than as ported content.</p>
 *
 * <p>Normal use projects the pentacle already drawn around the bowl. Sneaking projects a pentacle
 * from the list as a template, so a player can see what to build before spending chalk.</p>
 */
public class ItemPentacleDiviner extends Item {

    public ItemPentacleDiviner() {
        this.setMaxStackSize(1);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (world.isRemote) {
            com.occultism.occultism.client.PentacleDivinerClient.use(world, player, stack);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
        tooltip.add(net.minecraft.client.resources.I18n.format("item.occultism.pentacle_diviner.tooltip"));
        tooltip.add(net.minecraft.client.resources.I18n.format("item.occultism.pentacle_diviner.tooltip_cycle"));
    }
}
