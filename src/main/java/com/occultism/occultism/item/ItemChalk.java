package com.occultism.occultism.item;

import com.occultism.occultism.block.BlockChalkGlyph;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemChalk extends Item {

    private final BlockChalkGlyph glyphBlock;

    public ItemChalk(BlockChalkGlyph glyphBlock) {
        this.glyphBlock = glyphBlock;
        this.glyphBlock.setChalkItem(this);
        this.setMaxStackSize(1);
        this.setMaxDamage(256);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
            EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        IBlockState clickedState = worldIn.getBlockState(pos);
        boolean replacingGlyph = clickedState.getBlock() instanceof BlockChalkGlyph;
        boolean replacingBlock = replacingGlyph || clickedState.getBlock().isReplaceable(worldIn, pos);

        if (!replacingBlock && facing != EnumFacing.UP) {
            return EnumActionResult.FAIL;
        }

        BlockPos placePos = replacingBlock ? pos : pos.up();
        if (!player.canPlayerEdit(placePos, facing, stack) || !this.glyphBlock.canPlaceBlockAt(worldIn, placePos)) {
            return EnumActionResult.FAIL;
        }

        IBlockState targetState = worldIn.getBlockState(placePos);
        boolean sameGlyph = targetState.getBlock() == this.glyphBlock;

        if (!worldIn.isRemote) {
            int sign = sameGlyph ? this.glyphBlock.getNextSign(targetState) : this.glyphBlock.getRandomSign(worldIn.rand);
            IBlockState placedState = this.glyphBlock.getDefaultState().withProperty(BlockChalkGlyph.SIGN, sign);
            worldIn.setBlockState(placePos, placedState, 3);

            SoundType soundType = this.glyphBlock.getSoundType(placedState, worldIn, placePos, player);
            worldIn.playSound(null, placePos, soundType.getPlaceSound(), SoundCategory.BLOCKS,
                    (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);

            if (!player.capabilities.isCreativeMode && !sameGlyph) {
                stack.damageItem(1, player);
            }
        }

        return EnumActionResult.SUCCESS;
    }
}
