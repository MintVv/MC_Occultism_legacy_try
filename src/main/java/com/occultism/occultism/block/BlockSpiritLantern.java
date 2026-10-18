package com.occultism.occultism.block;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Spirit lantern, ported from upstream's {@code LanternBlock} registration.
 *
 * <p>Upstream uses the vanilla lantern block; 1.12 has no such block, so this is a small custom
 * block with the same behaviour: a {@code hanging} state, light level 10, a metal sound and the
 * same strength. The geometry replaces the 1.13+ {@code template_lantern} parent, which 1.12 does
 * not ship.</p>
 */
public class BlockSpiritLantern extends Block {

    public static final PropertyBool HANGING = PropertyBool.create("hanging");

    private static final AxisAlignedBB STANDING = new AxisAlignedBB(5 / 16D, 0.0D, 5 / 16D, 11 / 16D, 7 / 16D, 11 / 16D);
    private static final AxisAlignedBB HANGING_BOX = new AxisAlignedBB(5 / 16D, 1 / 16D, 5 / 16D, 11 / 16D, 8 / 16D, 11 / 16D);

    public BlockSpiritLantern() {
        super(Material.IRON);
        this.setSoundType(SoundType.METAL);
        this.setHardness(3.5F);
        this.setResistance(3.5F);
        this.setLightLevel(10 / 15F);
        this.setLightOpacity(0);
        this.setDefaultState(this.blockState.getBaseState().withProperty(HANGING, Boolean.FALSE));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] {HANGING});
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(HANGING, (meta & 1) == 1);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(HANGING) ? 1 : 0;
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY,
            float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        // Placing against the underside of a block hangs the lantern, as upstream does.
        boolean hanging = facing == EnumFacing.DOWN || (facing != EnumFacing.UP && !canStandOn(world, pos));
        return this.getDefaultState().withProperty(HANGING, hanging && canHangFrom(world, pos));
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return canStandOn(world, pos) || canHangFrom(world, pos);
    }

    private static boolean canStandOn(World world, BlockPos pos) {
        BlockPos below = pos.down();
        return world.getBlockState(below).isSideSolid(world, below, EnumFacing.UP);
    }

    private static boolean canHangFrom(World world, BlockPos pos) {
        BlockPos above = pos.up();
        return world.getBlockState(above).isSideSolid(world, above, EnumFacing.DOWN);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        boolean hanging = state.getValue(HANGING);
        boolean supported = hanging ? canHangFrom(world, pos) : canStandOn(world, pos);
        if (!supported) {
            this.dropBlockAsItem(world, pos, state, 0);
            world.setBlockToAir(pos);
        }
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return state.getValue(HANGING) ? HANGING_BOX : STANDING;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return null;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random random) {
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + (state.getValue(HANGING) ? 0.35D : 0.25D);
        double z = pos.getZ() + 0.5D;
        world.spawnParticle(EnumParticleTypes.FLAME, x, y, z, 0.0D, 0.0D, 0.0D);
    }

    /** Right-clicking with an empty hand plays the lantern sound, using the 1.12 metal sound, since 1.12 has no lantern sound. */
    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
            EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!player.getHeldItem(hand).isEmpty()) {
            return false;
        }
        if (world.isRemote) {
            world.playSound(player, pos, SoundEvents.BLOCK_METAL_PLACE, SoundCategory.BLOCKS, 0.4F, 1.6F);
        }
        return true;
    }
}
