package com.occultism.occultism.item;

import com.occultism.occultism.block.BlockChalkGlyph;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemBrush extends Item {

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
            EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        IBlockState state = worldIn.getBlockState(pos);

        if (!(state.getBlock() instanceof BlockChalkGlyph)) {
            return EnumActionResult.PASS;
        }

        if (!player.canPlayerEdit(pos, facing, stack)) {
            return EnumActionResult.FAIL;
        }

        if (!worldIn.isRemote) {
            worldIn.setBlockToAir(pos);
            worldIn.playSound(null, pos, SoundEvents.BLOCK_CLOTH_BREAK, SoundCategory.PLAYERS,
                    0.5F, 1.0F + worldIn.rand.nextFloat() * 0.5F);
        }

        return EnumActionResult.SUCCESS;
    }
}
