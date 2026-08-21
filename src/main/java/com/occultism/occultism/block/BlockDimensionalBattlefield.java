package com.occultism.occultism.block;

import com.occultism.occultism.Occultism;
import com.occultism.occultism.gui.ModGuiHandler;
import com.occultism.occultism.tileentity.TileEntityDimensionalBattlefield;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockDimensionalBattlefield extends BlockModelOnly {

    public BlockDimensionalBattlefield() {
        super(Material.ROCK);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityDimensionalBattlefield();
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
            EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!(worldIn.getTileEntity(pos) instanceof TileEntityDimensionalBattlefield)) {
            return false;
        }
        if (!worldIn.isRemote) {
            playerIn.openGui(Occultism.INSTANCE, ModGuiHandler.DIMENSIONAL_BATTLEFIELD, worldIn,
                    pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    @Override
    public boolean hasComparatorInputOverride(IBlockState state) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(IBlockState blockState, World worldIn, BlockPos pos) {
        TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof TileEntityDimensionalBattlefield) {
            return ((TileEntityDimensionalBattlefield) tile).getRedstoneSignal();
        }
        return 0;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof TileEntityDimensionalBattlefield) {
            InventoryHelper.dropInventoryItems(worldIn, pos, (TileEntityDimensionalBattlefield) tile);
        }
        super.breakBlock(worldIn, pos, state);
    }
}
