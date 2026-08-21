package com.occultism.occultism.item;

import com.occultism.occultism.tileentity.TileEntityStorageController;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBlockStorageController extends ItemBlock {

    public ItemBlockStorageController(Block block) {
        super(block);
        this.setMaxStackSize(1);
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side,
            float hitX, float hitY, float hitZ, IBlockState newState) {
        boolean placed = super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState);
        if (!placed) {
            return false;
        }
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityStorageController) {
            ((TileEntityStorageController) tile).readInventoryFromStack(stack);
            world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
        }
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        int storedItemCount = TileEntityStorageController.getStoredItemCount(stack);
        if (storedItemCount > 0) {
            tooltip.add(I18n.format("item.occultism.storage_controller.tooltip.stored_items", storedItemCount));
        }
    }
}
