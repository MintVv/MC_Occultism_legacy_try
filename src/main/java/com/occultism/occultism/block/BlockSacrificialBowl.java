package com.occultism.occultism.block;

import com.occultism.occultism.tileentity.TileEntitySacrificialBowl;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockSacrificialBowl extends Block {

    private static final AxisAlignedBB BOUNDING_BOX =
            new AxisAlignedBB(0.125D, 0.0D, 0.125D, 0.875D, 0.5D, 0.875D);

    public BlockSacrificialBowl() {
        super(Material.ROCK);
        this.setSoundType(SoundType.STONE);
        this.setHardness(2.0F);
        this.setResistance(10.0F);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
            EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            TileEntitySacrificialBowl bowl = this.getOrCreateTileEntity(worldIn, pos, state);
            if (bowl != null) {
                ItemStack heldStack = playerIn.getHeldItem(hand);

                if (!bowl.isEmpty()) {
                    ItemStack removed = bowl.removeStack();
                    ItemStack displayStack = removed.copy();
                    if (!playerIn.inventory.addItemStackToInventory(removed)) {
                        playerIn.dropItem(removed, false);
                    }
                    playerIn.sendStatusMessage(new TextComponentTranslation("occultism.sacrificial_bowl.removed",
                            displayStack.getDisplayName()), false);
                    worldIn.playSound(null, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 0.4F, 1.0F);
                    return true;
                }

                if (!heldStack.isEmpty()) {
                    ItemStack inserted = heldStack.copy();
                    inserted.setCount(1);
                    bowl.setStack(inserted);
                    if (!playerIn.capabilities.isCreativeMode) {
                        heldStack.shrink(1);
                    }
                    playerIn.sendStatusMessage(new TextComponentTranslation("occultism.sacrificial_bowl.inserted",
                            inserted.getDisplayName()), false);
                    worldIn.playSound(null, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 0.25F, 0.7F);
                    return true;
                }
            }
        }
        return true;
    }

    private TileEntitySacrificialBowl getOrCreateTileEntity(World world, BlockPos pos, IBlockState state) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntitySacrificialBowl) {
            return (TileEntitySacrificialBowl) tileEntity;
        }
        TileEntitySacrificialBowl bowl = (TileEntitySacrificialBowl) this.createTileEntity(world, state);
        world.setTileEntity(pos, bowl);
        return bowl;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntitySacrificialBowl();
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof TileEntitySacrificialBowl) {
            ItemStack stack = ((TileEntitySacrificialBowl) tileEntity).getStack();
            if (!stack.isEmpty()) {
                InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), stack);
            }
        }
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return BOUNDING_BOX;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }
}
