package com.occultism.occultism.block;

import javax.annotation.Nullable;

import com.occultism.occultism.tileentity.TileEntityStorageController;

import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/** Directional storage stabilizer that points toward its controller. */
public class BlockStorageStabilizer extends BlockModelOnly {

    public static final PropertyDirection FACING = PropertyDirection.create("facing");

    private static final AxisAlignedBB EAST_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.3D, 1.0D, 1.0D);
    private static final AxisAlignedBB WEST_AABB = new AxisAlignedBB(0.7D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
    private static final AxisAlignedBB NORTH_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.7D, 1.0D, 1.0D, 1.0D);
    private static final AxisAlignedBB SOUTH_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.3D);
    private static final AxisAlignedBB UP_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.3D, 1.0D);
    private static final AxisAlignedBB DOWN_AABB = new AxisAlignedBB(0.0D, 0.7D, 0.0D, 1.0D, 1.0D, 1.0D);

    public BlockStorageStabilizer() {
        super(net.minecraft.block.material.Material.IRON);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.UP));
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        switch (state.getValue(FACING)) {
            case EAST:
                return EAST_AABB;
            case WEST:
                return WEST_AABB;
            case NORTH:
                return NORTH_AABB;
            case SOUTH:
                return SOUTH_AABB;
            case DOWN:
                return DOWN_AABB;
            case UP:
            default:
                return UP_AABB;
        }
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return NULL_AABB;
    }

    @Override
    public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return true;
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY,
            float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, facing == null ? EnumFacing.UP : facing);
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        this.notifyStorageControllers(worldIn, pos, state);
        super.onBlockAdded(worldIn, pos, state);
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, net.minecraft.block.Block blockIn,
            BlockPos fromPos) {
        this.notifyStorageControllers(worldIn, pos, state);
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        this.notifyStorageControllers(worldIn, pos, state);
        super.breakBlock(worldIn, pos, state);
    }

    /** Notifies a controller on the ray this stabilizer is pointing along. */
    public void notifyStorageControllers(World worldIn, BlockPos pos, IBlockState state) {
        if (worldIn == null || pos == null || state == null || !state.getPropertyKeys().contains(FACING)) {
            return;
        }
        EnumFacing facing = state.getValue(FACING);
        BlockPos start = facing.getAxis().isHorizontal() ? pos.down() : pos;
        for (int distance = 0; distance <= TileEntityStorageController.MAX_STABILIZER_DISTANCE; distance++) {
            BlockPos candidate = start.offset(facing, distance);
            net.minecraft.tileentity.TileEntity tile = worldIn.getTileEntity(candidate);
            if (tile instanceof TileEntityStorageController) {
                // Keep tracing after a hit: more than one controller can share
                // the same line, and each must recalculate its own capacity.
                ((TileEntityStorageController) tile).updateStabilizers();
            }
        }
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        // Metadata 0 was the only state used by the old non-directional block.
        // Keep it as UP, then assign the remaining five values explicitly so all
        // six directions have a lossless 1.12 representation.
        switch (meta & 7) {
            case 1:
                return this.getDefaultState().withProperty(FACING, EnumFacing.DOWN);
            case 2:
                return this.getDefaultState().withProperty(FACING, EnumFacing.NORTH);
            case 3:
                return this.getDefaultState().withProperty(FACING, EnumFacing.SOUTH);
            case 4:
                return this.getDefaultState().withProperty(FACING, EnumFacing.WEST);
            case 5:
                return this.getDefaultState().withProperty(FACING, EnumFacing.EAST);
            case 0:
            default:
                return this.getDefaultState().withProperty(FACING, EnumFacing.UP);
        }
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        switch (state.getValue(FACING)) {
            case DOWN:
                return 1;
            case NORTH:
                return 2;
            case SOUTH:
                return 3;
            case WEST:
                return 4;
            case EAST:
                return 5;
            case UP:
            default:
                return 0;
        }
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }
}
