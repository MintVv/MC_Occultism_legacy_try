package com.occultism.occultism.world;

import java.util.Random;

import com.occultism.occultism.registry.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLog;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

public class WorldGenOtherworldTree extends WorldGenerator {

    private final boolean natural;

    public WorldGenOtherworldTree(boolean notify) {
        this(notify, false);
    }

    public WorldGenOtherworldTree(boolean notify, boolean natural) {
        super(notify);
        this.natural = natural;
    }

    @Override
    public boolean generate(World world, Random random, BlockPos pos) {
        int height = 4 + random.nextInt(3);
        if (pos.getY() < 1 || pos.getY() + height + 1 > world.getHeight()) {
            return false;
        }
        if (!this.hasRoom(world, pos, height)) {
            return false;
        }
        if (!this.prepareSoil(world, pos.down())) {
            return false;
        }

        this.placeLeaves(world, pos, height);
        IBlockState log = (this.natural ? ModBlocks.OTHERWORLD_LOG_NATURAL : ModBlocks.OTHERWORLD_LOG).getDefaultState()
                .withProperty(BlockLog.LOG_AXIS, BlockLog.EnumAxis.Y);
        for (int y = 0; y < height; y++) {
            BlockPos logPos = pos.up(y);
            if (this.canReplace(world, logPos)) {
                this.setBlockAndNotifyAdequately(world, logPos, log);
            }
        }
        return true;
    }

    private boolean hasRoom(World world, BlockPos pos, int height) {
        for (int y = 0; y <= height + 1; y++) {
            int radius = 1;
            if (y == 0) {
                radius = 0;
            } else if (y >= height - 1) {
                radius = 2;
            }

            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos checkPos = pos.add(x, y, z);
                    if (checkPos.getY() < 0 || checkPos.getY() >= world.getHeight() || !this.canReplace(world, checkPos)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean prepareSoil(World world, BlockPos soilPos) {
        IBlockState soilState = world.getBlockState(soilPos);
        Block soil = soilState.getBlock();
        if (!soil.canSustainPlant(soilState, world, soilPos, net.minecraft.util.EnumFacing.UP,
                (net.minecraftforge.common.IPlantable) ModBlocks.OTHERWORLD_SAPLING)) {
            return false;
        }
        if (soil == Blocks.GRASS || soil == Blocks.DIRT) {
            this.setBlockAndNotifyAdequately(world, soilPos, Blocks.DIRT.getDefaultState());
        }
        return true;
    }

    private void placeLeaves(World world, BlockPos pos, int height) {
        BlockPos top = pos.up(height);
        IBlockState leaves = (this.natural ? ModBlocks.OTHERWORLD_LEAVES_NATURAL : ModBlocks.OTHERWORLD_LEAVES)
                .getDefaultState();
        for (int y = -3; y <= 0; y++) {
            int radius = y == 0 ? 1 : 2;
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (Math.abs(x) == radius && Math.abs(z) == radius && y != -1) {
                        continue;
                    }
                    BlockPos leafPos = top.add(x, y, z);
                    if (this.canReplace(world, leafPos)) {
                        this.setBlockAndNotifyAdequately(world, leafPos, leaves);
                    }
                }
            }
        }
    }

    private boolean canReplace(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        Material material = state.getMaterial();
        return block.isAir(state, world, pos)
                || block.isLeaves(state, world, pos)
                || block.isReplaceable(world, pos)
                || material == Material.PLANTS
                || material == Material.VINE;
    }
}
