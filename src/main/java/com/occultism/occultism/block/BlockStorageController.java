package com.occultism.occultism.block;

import com.occultism.occultism.Occultism;
import com.occultism.occultism.gui.ModGuiHandler;
import com.occultism.occultism.item.ItemBlockStableWormhole;
import com.occultism.occultism.item.ItemStorageRemote;
import com.occultism.occultism.tileentity.TileEntityStorageController;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockStorageController extends BlockModelOnly {

    public BlockStorageController() {
        super(Material.ROCK);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityStorageController();
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        super.onBlockAdded(worldIn, pos, state);
        this.refreshStorageCapacity(worldIn, pos);
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, net.minecraft.block.Block blockIn,
            BlockPos fromPos) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
        this.refreshStorageCapacity(worldIn, pos);
    }

    private void refreshStorageCapacity(World worldIn, BlockPos pos) {
        if (!worldIn.isRemote && worldIn.getTileEntity(pos) instanceof TileEntityStorageController) {
            ((TileEntityStorageController) worldIn.getTileEntity(pos)).updateStabilizers();
        }
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
            EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!(worldIn.getTileEntity(pos) instanceof TileEntityStorageController)) {
            return false;
        }
        if (playerIn.isSneaking() && (playerIn.getHeldItem(hand).getItem() instanceof ItemStorageRemote
                || playerIn.getHeldItem(hand).getItem() instanceof ItemBlockStableWormhole)) {
            return false;
        }
        if (!worldIn.isRemote) {
            playerIn.openGui(Occultism.INSTANCE, ModGuiHandler.STORAGE_CONTROLLER, worldIn,
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
        return tile instanceof TileEntityStorageController
                ? net.minecraft.inventory.Container.calcRedstoneFromInventory((TileEntityStorageController) tile)
                : 0;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state,
            int fortune) {
        ItemStack drop = new ItemStack(this);
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityStorageController) {
            TileEntityStorageController.writeInventoryToStack(drop, (TileEntityStorageController) tile);
        }
        drops.add(drop);
    }
}
