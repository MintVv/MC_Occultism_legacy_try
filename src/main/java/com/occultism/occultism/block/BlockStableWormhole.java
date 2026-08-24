package com.occultism.occultism.block;

import javax.annotation.Nullable;

import com.occultism.occultism.Occultism;
import com.occultism.occultism.gui.ModGuiHandler;
import com.occultism.occultism.tileentity.TileEntityStableWormhole;
import com.occultism.occultism.tileentity.TileEntityStorageController;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

public class BlockStableWormhole extends BlockModelOnly {

    public static final PropertyBool LINKED = PropertyBool.create("linked");

    public BlockStableWormhole() {
        super(Material.ROCK);
        this.setDefaultState(this.blockState.getBaseState().withProperty(LINKED, Boolean.FALSE));
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityStableWormhole();
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return NULL_AABB;
    }

    @Override
    public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return true;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
            EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileEntity tile = worldIn.getTileEntity(pos);
        if (!(tile instanceof TileEntityStableWormhole)) {
            return false;
        }

        if (!worldIn.isRemote) {
            TileEntityStableWormhole wormhole = (TileEntityStableWormhole) tile;
            if (!wormhole.hasLinkedController()) {
                playerIn.sendStatusMessage(new TextComponentTranslation(this.getTranslationKey()
                        + ".message.unlinked"), true);
                return true;
            }
            World targetWorld = DimensionManager.getWorld(wormhole.getLinkedDimension());
            BlockPos controllerPos = wormhole.getLinkedControllerPos();
            if (targetWorld == null || !targetWorld.isBlockLoaded(controllerPos)) {
                playerIn.sendStatusMessage(new TextComponentTranslation(this.getTranslationKey()
                        + ".message.not_loaded"), true);
                return true;
            }

            TileEntity controller = targetWorld.getTileEntity(controllerPos);
            if (!(controller instanceof TileEntityStorageController) || controller.isInvalid()) {
                wormhole.clearLinkedController();
                worldIn.setBlockState(pos, state.withProperty(LINKED, Boolean.FALSE), 3);
                playerIn.sendStatusMessage(new TextComponentTranslation(this.getTranslationKey()
                        + ".message.missing"), true);
                return true;
            }

            // Keep the wormhole as the access source.  The GUI handler resolves
            // its linked controller from this block, so it cannot be confused
            // with a Remote ItemStack slot.
            playerIn.openGui(Occultism.INSTANCE, ModGuiHandler.STORAGE_WORMHOLE, worldIn,
                    pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state,
            int fortune) {
        ItemStack drop = new ItemStack(this);
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityStableWormhole) {
            TileEntityStableWormhole.writeLinkToStack(drop, (TileEntityStableWormhole) tile);
        }
        drops.add(drop);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(LINKED, (meta & 1) == 1);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(LINKED) ? 1 : 0;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, LINKED);
    }
}
