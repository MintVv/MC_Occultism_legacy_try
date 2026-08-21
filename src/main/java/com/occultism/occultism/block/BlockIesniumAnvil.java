package com.occultism.occultism.block;

import com.occultism.occultism.Occultism;
import com.occultism.occultism.gui.ModGuiHandler;

import net.minecraft.block.BlockAnvil;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockIesniumAnvil extends BlockAnvil {

    public BlockIesniumAnvil() {
        super();
        this.setSoundType(SoundType.ANVIL);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
            EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            playerIn.openGui(Occultism.INSTANCE, ModGuiHandler.IESNIUM_ANVIL, worldIn, pos.getX(), pos.getY(),
                    pos.getZ());
        }
        return true;
    }

    @Override
    public int damageDropped(IBlockState state) {
        return 0;
    }

    @Override
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
        items.add(new ItemStack(this));
    }
}
