package com.occultism.occultism.block;

import com.occultism.occultism.Occultism;
import com.occultism.occultism.gui.ModGuiHandler;
import com.occultism.occultism.tileentity.TileEntityDimensionalMineshaft;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class BlockDimensionalMineshaft extends BlockModelOnly {

    public BlockDimensionalMineshaft() {
        super(Material.ROCK);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityDimensionalMineshaft();
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
            EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileEntity tile = worldIn.getTileEntity(pos);
        if (!(tile instanceof TileEntityDimensionalMineshaft)) {
            return false;
        }

        TileEntityDimensionalMineshaft mineshaft = (TileEntityDimensionalMineshaft) tile;
        ItemStack held = playerIn.getHeldItem(hand);

        if (!held.isEmpty() && mineshaft.isItemValidForSlot(TileEntityDimensionalMineshaft.MINER_SLOT, held)) {
            if (worldIn.isRemote) {
                return true;
            }
            if (!mineshaft.getStackInSlot(TileEntityDimensionalMineshaft.MINER_SLOT).isEmpty()) {
                playerIn.sendStatusMessage(new TextComponentTranslation("occultism.dimensional_mineshaft.occupied"),
                        true);
                return true;
            }
            ItemStack inserted = held.copy();
            inserted.setCount(1);
            mineshaft.setInventorySlotContents(TileEntityDimensionalMineshaft.MINER_SLOT, inserted);
            held.shrink(1);
            playerIn.sendStatusMessage(new TextComponentTranslation("occultism.dimensional_mineshaft.inserted"),
                    true);
            return true;
        }

        if (held.isEmpty() && playerIn.isSneaking()) {
            if (!worldIn.isRemote) {
                ItemStack taken = mineshaft.takeFirstOutput();
                if (taken.isEmpty()) {
                    taken = mineshaft.takeMiner();
                }
                if (taken.isEmpty()) {
                    playerIn.sendStatusMessage(new TextComponentTranslation("occultism.dimensional_mineshaft.empty"),
                            true);
                } else if (!playerIn.inventory.addItemStackToInventory(taken)) {
                    InventoryHelper.spawnItemStack(worldIn, playerIn.posX, playerIn.posY, playerIn.posZ, taken);
                }
            }
            return true;
        }

        if (!worldIn.isRemote) {
            playerIn.openGui(Occultism.INSTANCE, ModGuiHandler.DIMENSIONAL_MINESHAFT, worldIn,
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
        return tile instanceof TileEntityDimensionalMineshaft
                ? net.minecraft.inventory.Container.calcRedstoneFromInventory((TileEntityDimensionalMineshaft) tile)
                : 0;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof TileEntityDimensionalMineshaft) {
            InventoryHelper.dropInventoryItems(worldIn, pos, (TileEntityDimensionalMineshaft) tile);
        }
        super.breakBlock(worldIn, pos, state);
    }
}
