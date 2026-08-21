package com.occultism.occultism.block;

import com.occultism.occultism.Occultism;
import com.occultism.occultism.gui.ModGuiHandler;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockSpiritGrindstone extends BlockModelOnly {

    public BlockSpiritGrindstone() {
        super(Material.ROCK);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
            EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            playerIn.openGui(Occultism.INSTANCE, ModGuiHandler.SPIRIT_GRINDSTONE, worldIn,
                    pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }
}
