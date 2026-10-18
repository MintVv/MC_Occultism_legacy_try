package com.occultism.occultism.block;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
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
 * Large candle used by pentacles, lightable like its upstream counterpart.
 *
 * <p>The guide already told players that the large candle "can be lit" and that a torch changes
 * its state, but the block was a bare pentacle marker with no state at all. This adds the missing
 * behaviour: a {@code lit} property, lighting with any torch, extinguishing with an empty hand,
 * a light level and flame particles while lit.</p>
 *
 * <p>It still extends {@link BlockPentacleRequirement} so pentacle detection keeps matching the
 * block regardless of its lit state.</p>
 */
public class BlockLargeCandle extends BlockPentacleRequirement {

    public static final PropertyBool LIT = PropertyBool.create("lit");

    /** White keeps the base candle unchanged; coloured variants tint the same model. */
    private final int color;

    public BlockLargeCandle() {
        this(0xFFFFFF);
    }

    public BlockLargeCandle(int color) {
        super(Material.CIRCUITS, SoundType.CLOTH,
                new AxisAlignedBB(0.25D, 0.0D, 0.25D, 0.75D, 0.625D, 0.75D), BlockRenderLayer.CUTOUT);
        this.color = color;
        this.setDefaultState(this.blockState.getBaseState().withProperty(LIT, Boolean.FALSE));
    }

    /** Tint applied by the block and item colour handlers; the model already carries tintindex 0. */
    public int getColor() {
        return this.color;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] {LIT});
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(LIT, (meta & 1) == 1);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(LIT) ? 1 : 0;
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state.getValue(LIT) ? 14 : 0;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
            EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack held = player.getHeldItem(hand);
        boolean lit = state.getValue(LIT);
        if (!lit && isTorch(held)) {
            if (!world.isRemote) {
                world.setBlockState(pos, state.withProperty(LIT, Boolean.TRUE), 3);
                world.playSound(null, pos, SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.BLOCKS, 0.5F, 1.2F);
            }
            return true;
        }
        if (lit && held.isEmpty()) {
            if (!world.isRemote) {
                world.setBlockState(pos, state.withProperty(LIT, Boolean.FALSE), 3);
            }
            return true;
        }
        return false;
    }

    /** Upstream accepts any torch; 1.12 has the plain and the redstone one, plus our spirit torch. */
    private static boolean isTorch(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        Item item = stack.getItem();
        return item == Item.getItemFromBlock(Blocks.TORCH)
                || item == Item.getItemFromBlock(Blocks.REDSTONE_TORCH)
                || item == Item.getItemFromBlock(com.occultism.occultism.registry.ModBlocks.SPIRIT_TORCH);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random random) {
        if (!state.getValue(LIT)) {
            return;
        }
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.68D;
        double z = pos.getZ() + 0.5D;
        world.spawnParticle(EnumParticleTypes.FLAME, x, y, z, 0.0D, 0.0D, 0.0D);
        if (random.nextFloat() < 0.35F) {
            world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x, y, z, 0.0D, 0.01D, 0.0D);
        }
    }
}
