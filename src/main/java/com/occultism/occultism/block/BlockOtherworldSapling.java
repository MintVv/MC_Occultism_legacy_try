package com.occultism.occultism.block;

import java.util.Random;

import com.occultism.occultism.registry.ModCreativeTabs;
import com.occultism.occultism.world.WorldGenOtherworldTree;
import net.minecraft.block.BlockBush;
import net.minecraft.block.IGrowable;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockOtherworldSapling extends BlockBush implements IGrowable {

    private static final WorldGenOtherworldTree TREE_GENERATOR = new WorldGenOtherworldTree(true);

    public BlockOtherworldSapling() {
        super();
        this.setSoundType(SoundType.PLANT);
        this.setCreativeTab(ModCreativeTabs.OCCULTISM);
        this.setTickRandomly(true);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
        if (!world.isRemote && world.getLightFromNeighbors(pos.up()) >= 9 && random.nextInt(7) == 0) {
            this.generateTree(world, pos, state, random);
        }
    }

    @Override
    public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean isClient) {
        return true;
    }

    @Override
    public boolean canUseBonemeal(World world, Random random, BlockPos pos, IBlockState state) {
        return random.nextFloat() < 0.45D;
    }

    @Override
    public void grow(World world, Random random, BlockPos pos, IBlockState state) {
        this.generateTree(world, pos, state, random);
    }

    private void generateTree(World world, BlockPos pos, IBlockState state, Random random) {
        world.setBlockState(pos, Blocks.AIR.getDefaultState(), 4);
        if (!TREE_GENERATOR.generate(world, random, pos)) {
            world.setBlockState(pos, state, 4);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }
}
