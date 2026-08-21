package com.occultism.occultism.tileentity;

import javax.annotation.Nullable;

import com.occultism.occultism.api.common.blockentity.IStorageController;
import com.occultism.occultism.api.common.blockentity.IStorageControllerProxy;
import com.occultism.occultism.api.common.data.GlobalBlockPos;
import com.occultism.occultism.block.BlockStableWormhole;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class TileEntityStableWormhole extends TileEntity implements IStorageControllerProxy {

    public static final String TAG_CONTROLLER_X = "StorageControllerX";
    public static final String TAG_CONTROLLER_Y = "StorageControllerY";
    public static final String TAG_CONTROLLER_Z = "StorageControllerZ";
    public static final String TAG_CONTROLLER_DIMENSION = "StorageControllerDimension";
    /** Nested form used by the public proxy API; legacy item links keep the four old keys. */
    public static final String TAG_LINKED_STORAGE_POSITION = "linkedStorageControllerPosition";

    private BlockPos linkedControllerPos;
    private int linkedDimension;

    public boolean hasLinkedController() {
        return this.linkedControllerPos != null;
    }

    @Nullable
    public BlockPos getLinkedControllerPos() {
        return this.linkedControllerPos;
    }

    public int getLinkedDimension() {
        return this.linkedDimension;
    }

    /** Returns whether this wormhole points at the supplied storage controller. */
    public boolean isLinkedToController(@Nullable World targetWorld, @Nullable BlockPos targetPos) {
        return this.linkedControllerPos != null && targetWorld != null && targetPos != null
                && this.linkedControllerPos.equals(targetPos)
                && this.linkedDimension == targetWorld.provider.getDimension();
    }

    /** Resolves the linked controller without loading a missing dimension or chunk. */
    @Nullable
    private TileEntityStorageController getLinkedController() {
        if (this.world == null || this.linkedControllerPos == null) {
            return null;
        }
        World targetWorld = DimensionManager.getWorld(this.linkedDimension);
        if (targetWorld == null || !targetWorld.isBlockLoaded(this.linkedControllerPos)) {
            return null;
        }
        TileEntity tile = targetWorld.getTileEntity(this.linkedControllerPos);
        if (tile instanceof TileEntityStorageController) {
            TileEntityStorageController controller = (TileEntityStorageController) tile;
            if (!controller.isInvalid()) {
                return controller;
            }
        }
        // Once the target chunk is loaded, a non-controller tile proves that
        // the link is stale.  Keep the link while the dimension/chunk is
        // unavailable so unloading never destroys a valid cross-dimension
        // reference; clear stale links only on the logical server.
        if (!this.world.isRemote) {
            this.clearLinkedController();
        }
        return null;
    }

    /** Returns the loaded target using the public 1.12 storage API contract. */
    @Override
    @Nullable
    public IStorageController getLinkedStorageController() {
        return this.getLinkedController();
    }

    @Override
    @Nullable
    public GlobalBlockPos getLinkedStorageControllerPosition() {
        return this.linkedControllerPos == null ? null
                : new GlobalBlockPos(this.linkedControllerPos, this.linkedDimension);
    }

    @Override
    public void setLinkedStorageControllerPosition(@Nullable GlobalBlockPos blockPos) {
        BlockPos position = blockPos == null ? null : blockPos.getPos();
        if (position == null) {
            this.clearLinkedController();
            return;
        }
        this.linkedControllerPos = position.toImmutable();
        this.linkedDimension = blockPos.getDimension();
        this.markDirty();
    }

    /** Exposes the target controller inventory to Forge item-handler consumers. */
    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && this.getLinkedController() != null) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    @Nullable
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            TileEntityStorageController controller = this.getLinkedController();
            if (controller != null) {
                return controller.getCapability(capability, facing);
            }
        }
        return super.getCapability(capability, facing);
    }

    public void setLinkedController(World world, BlockPos pos) {
        this.linkedControllerPos = pos.toImmutable();
        this.linkedDimension = world.provider.getDimension();
        this.markDirty();
    }

    public void clearLinkedController() {
        this.linkedControllerPos = null;
        this.linkedDimension = 0;
        this.markDirty();
        if (this.world != null && !this.world.isRemote
                && this.world.getBlockState(this.pos).getBlock() instanceof BlockStableWormhole
                && this.world.getBlockState(this.pos).getValue(BlockStableWormhole.LINKED)) {
            this.world.setBlockState(this.pos,
                    this.world.getBlockState(this.pos).withProperty(BlockStableWormhole.LINKED, Boolean.FALSE), 3);
        }
    }

    public void readLinkFromStack(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        GlobalBlockPos reference = getLinkFromTag(tag);
        if (reference == null || !reference.isPresent()) {
            this.clearLinkedController();
            return;
        }
        this.linkedControllerPos = reference.getPos().toImmutable();
        this.linkedDimension = reference.getDimension();
        this.markDirty();
    }

    public static boolean hasLinkedController(ItemStack stack) {
        GlobalBlockPos reference = getLinkedControllerPosition(stack);
        return reference != null && reference.isPresent();
    }

    /** Returns the nested or legacy item link without resolving a world. */
    @Nullable
    public static GlobalBlockPos getLinkedControllerPosition(@Nullable ItemStack stack) {
        NBTTagCompound tag = stack == null ? null : stack.getTagCompound();
        return getLinkFromTag(tag);
    }

    public static void writeLinkToStack(ItemStack stack, TileEntityStableWormhole wormhole) {
        if (!wormhole.hasLinkedController()) {
            return;
        }
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
        }
        BlockPos pos = wormhole.getLinkedControllerPos();
        tag.setInteger(TAG_CONTROLLER_X, pos.getX());
        tag.setInteger(TAG_CONTROLLER_Y, pos.getY());
        tag.setInteger(TAG_CONTROLLER_Z, pos.getZ());
        tag.setInteger(TAG_CONTROLLER_DIMENSION, wormhole.getLinkedDimension());
        tag.setTag(TAG_LINKED_STORAGE_POSITION,
                new GlobalBlockPos(pos, wormhole.getLinkedDimension()).serializeNBT());
        stack.setTagCompound(tag);
    }

    public static void writeLinkToStack(ItemStack stack, World world, BlockPos pos) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
        }
        tag.setInteger(TAG_CONTROLLER_X, pos.getX());
        tag.setInteger(TAG_CONTROLLER_Y, pos.getY());
        tag.setInteger(TAG_CONTROLLER_Z, pos.getZ());
        tag.setInteger(TAG_CONTROLLER_DIMENSION, world.provider.getDimension());
        tag.setTag(TAG_LINKED_STORAGE_POSITION,
                new GlobalBlockPos(pos, world).serializeNBT());
        stack.setTagCompound(tag);
    }

    private static boolean hasLink(NBTTagCompound tag) {
        return tag != null && tag.hasKey(TAG_CONTROLLER_X, 3) && tag.hasKey(TAG_CONTROLLER_Y, 3)
                && tag.hasKey(TAG_CONTROLLER_Z, 3) && tag.hasKey(TAG_CONTROLLER_DIMENSION, 3);
    }

    /** Reads the nested modern link first, then the legacy flat link. */
    @Nullable
    private static GlobalBlockPos getLinkFromTag(@Nullable NBTTagCompound tag) {
        if (tag == null) {
            return null;
        }
        if (tag.hasKey(TAG_LINKED_STORAGE_POSITION, 10)) {
            GlobalBlockPos nested = GlobalBlockPos.from(tag.getCompoundTag(TAG_LINKED_STORAGE_POSITION));
            return nested.isPresent() ? nested : null;
        }
        if (hasLink(tag)) {
            return new GlobalBlockPos(new BlockPos(tag.getInteger(TAG_CONTROLLER_X),
                    tag.getInteger(TAG_CONTROLLER_Y), tag.getInteger(TAG_CONTROLLER_Z)),
                    tag.getInteger(TAG_CONTROLLER_DIMENSION));
        }
        return null;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        if (this.linkedControllerPos != null) {
            compound.setInteger(TAG_CONTROLLER_X, this.linkedControllerPos.getX());
            compound.setInteger(TAG_CONTROLLER_Y, this.linkedControllerPos.getY());
            compound.setInteger(TAG_CONTROLLER_Z, this.linkedControllerPos.getZ());
            compound.setInteger(TAG_CONTROLLER_DIMENSION, this.linkedDimension);
            compound.setTag(TAG_LINKED_STORAGE_POSITION,
                    new GlobalBlockPos(this.linkedControllerPos, this.linkedDimension).serializeNBT());
        } else {
            compound.removeTag(TAG_LINKED_STORAGE_POSITION);
        }
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        GlobalBlockPos reference = getLinkFromTag(compound);
        if (reference == null || !reference.isPresent()) {
            this.linkedControllerPos = null;
            this.linkedDimension = 0;
        } else {
            this.linkedControllerPos = reference.getPos().toImmutable();
            this.linkedDimension = reference.getDimension();
        }
    }
}
