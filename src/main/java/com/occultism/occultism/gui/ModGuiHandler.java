package com.occultism.occultism.gui;

import com.occultism.occultism.client.gui.GuiDimensionalMineshaft;
import com.occultism.occultism.client.gui.GuiDimensionalBattlefield;
import com.occultism.occultism.client.gui.GuiBookOfCalling;
import com.occultism.occultism.client.gui.GuiDictionaryOfSpirits;
import com.occultism.occultism.client.gui.GuiEnderSatchel;
import com.occultism.occultism.client.gui.GuiIesniumAnvil;
import com.occultism.occultism.client.gui.GuiSatchel;
import com.occultism.occultism.client.gui.GuiSpiritGrindstone;
import com.occultism.occultism.client.gui.GuiStorageController;
import com.occultism.occultism.container.ContainerDimensionalMineshaft;
import com.occultism.occultism.container.ContainerDimensionalBattlefield;
import com.occultism.occultism.container.ContainerBookOfCalling;
import com.occultism.occultism.container.ContainerDictionaryOfSpirits;
import com.occultism.occultism.container.ContainerEnderSatchel;
import com.occultism.occultism.container.ContainerIesniumAnvil;
import com.occultism.occultism.container.ContainerSatchel;
import com.occultism.occultism.container.ContainerSpiritGrindstone;
import com.occultism.occultism.container.ContainerStorageController;
import com.occultism.occultism.container.SatchelInventory;
import com.occultism.occultism.item.ItemSatchel;
import com.occultism.occultism.item.ItemBookOfCallingFoliotJob;
import com.occultism.occultism.registry.ModBlocks;
import com.occultism.occultism.registry.ModItems;
import com.occultism.occultism.tileentity.TileEntityDimensionalMineshaft;
import com.occultism.occultism.tileentity.TileEntityDimensionalBattlefield;
import com.occultism.occultism.tileentity.TileEntityStorageController;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ModGuiHandler implements IGuiHandler {

    public static final int DIMENSIONAL_MINESHAFT = 1;
    public static final int SATCHEL = 2;
    public static final int ENDER_SATCHEL = 3;
    public static final int SPIRIT_GRINDSTONE = 4;
    public static final int STORAGE_CONTROLLER = 5;
    public static final int STORAGE_REMOTE = 6;
    public static final int DICTIONARY_OF_SPIRITS = 7;
    public static final int IESNIUM_ANVIL = 8;
    public static final int DIMENSIONAL_BATTLEFIELD = 9;
    public static final int BOOK_OF_CALLING = 10;

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if (id == DIMENSIONAL_MINESHAFT) {
            TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
            if (tile instanceof TileEntityDimensionalMineshaft) {
                return new ContainerDimensionalMineshaft(player.inventory, (TileEntityDimensionalMineshaft) tile);
            }
        } else if (id == DIMENSIONAL_BATTLEFIELD) {
            TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
            if (tile instanceof TileEntityDimensionalBattlefield) {
                return new ContainerDimensionalBattlefield(player.inventory, (TileEntityDimensionalBattlefield) tile);
            }
        } else if (id == SATCHEL) {
            ItemStack stack = player.inventory.getStackInSlot(x);
            if (ItemSatchel.opensSatchelInventory(stack)) {
                return new ContainerSatchel(player.inventory, new SatchelInventory(stack), x);
            }
        } else if (id == ENDER_SATCHEL) {
            ItemStack stack = player.inventory.getStackInSlot(x);
            if (!stack.isEmpty() && stack.getItem() == ModItems.ENDER_SATCHEL) {
                return new ContainerEnderSatchel(player.inventory, player.getInventoryEnderChest(), player, x);
            }
        } else if (id == SPIRIT_GRINDSTONE) {
            BlockPos pos = new BlockPos(x, y, z);
            if (world.getBlockState(pos).getBlock() == ModBlocks.SPIRIT_GRINDSTONE) {
                return new ContainerSpiritGrindstone(player.inventory, world, pos);
            }
        } else if (id == STORAGE_CONTROLLER) {
            TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
            if (tile instanceof TileEntityStorageController && !tile.isInvalid()) {
                return new ContainerStorageController(player.inventory, (TileEntityStorageController) tile);
            }
        } else if (id == STORAGE_REMOTE) {
            TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
            if (tile instanceof TileEntityStorageController && !tile.isInvalid()) {
                return new ContainerStorageController(player.inventory, (TileEntityStorageController) tile, true);
            }
        } else if (id == DICTIONARY_OF_SPIRITS) {
            return new ContainerDictionaryOfSpirits();
        } else if (id == BOOK_OF_CALLING) {
            ItemStack stack = player.inventory.getStackInSlot(x);
            if (!stack.isEmpty() && stack.getItem() instanceof ItemBookOfCallingFoliotJob) {
                return new ContainerBookOfCalling();
            }
        } else if (id == IESNIUM_ANVIL) {
            BlockPos pos = new BlockPos(x, y, z);
            if (world.getBlockState(pos).getBlock() == ModBlocks.IESNIUM_ANVIL) {
                return new ContainerIesniumAnvil(player.inventory, world, pos, player);
            }
        }
        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if (id == DIMENSIONAL_MINESHAFT) {
            TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
            if (tile instanceof TileEntityDimensionalMineshaft) {
                return new GuiDimensionalMineshaft(player.inventory, (TileEntityDimensionalMineshaft) tile);
            }
        } else if (id == DIMENSIONAL_BATTLEFIELD) {
            TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
            if (tile instanceof TileEntityDimensionalBattlefield) {
                return new GuiDimensionalBattlefield(player.inventory, (TileEntityDimensionalBattlefield) tile);
            }
        } else if (id == SATCHEL) {
            ItemStack stack = player.inventory.getStackInSlot(x);
            if (ItemSatchel.opensSatchelInventory(stack)) {
                return new GuiSatchel(player.inventory, new SatchelInventory(stack), x);
            }
        } else if (id == ENDER_SATCHEL) {
            ItemStack stack = player.inventory.getStackInSlot(x);
            if (!stack.isEmpty() && stack.getItem() == ModItems.ENDER_SATCHEL) {
                return new GuiEnderSatchel(player.inventory, player.getInventoryEnderChest(), x);
            }
        } else if (id == SPIRIT_GRINDSTONE) {
            BlockPos pos = new BlockPos(x, y, z);
            if (world.getBlockState(pos).getBlock() == ModBlocks.SPIRIT_GRINDSTONE) {
                return new GuiSpiritGrindstone(player.inventory, world, pos);
            }
        } else if (id == STORAGE_CONTROLLER) {
            TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
            if (tile instanceof TileEntityStorageController && !tile.isInvalid()) {
                return new GuiStorageController(player.inventory, (TileEntityStorageController) tile);
            }
        } else if (id == STORAGE_REMOTE) {
            TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
            TileEntityStorageController storageController = tile instanceof TileEntityStorageController
                    && !tile.isInvalid()
                    ? (TileEntityStorageController) tile
                    : new TileEntityStorageController();
            return new GuiStorageController(player.inventory, storageController, true);
        } else if (id == DICTIONARY_OF_SPIRITS) {
            return new GuiDictionaryOfSpirits();
        } else if (id == BOOK_OF_CALLING) {
            ItemStack stack = player.inventory.getStackInSlot(x);
            if (!stack.isEmpty() && stack.getItem() instanceof ItemBookOfCallingFoliotJob) {
                return new GuiBookOfCalling(x);
            }
        } else if (id == IESNIUM_ANVIL) {
            BlockPos pos = new BlockPos(x, y, z);
            if (world.getBlockState(pos).getBlock() == ModBlocks.IESNIUM_ANVIL) {
                return new GuiIesniumAnvil(player.inventory, world, pos);
            }
        }
        return null;
    }
}
