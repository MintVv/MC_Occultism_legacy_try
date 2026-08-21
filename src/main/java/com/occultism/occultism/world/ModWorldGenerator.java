package com.occultism.occultism.world;

import com.google.common.base.Predicate;
import com.occultism.occultism.registry.ModBlocks;
import java.util.Random;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraftforge.fml.common.IWorldGenerator;

public final class ModWorldGenerator implements IWorldGenerator {

    private static final Predicate<IBlockState> NETHERRACK = new Predicate<IBlockState>() {
        @Override
        public boolean apply(IBlockState input) {
            return input != null && input.getBlock() == Blocks.NETHERRACK;
        }
    };

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator,
            IChunkProvider chunkProvider) {
        int dimension = world.provider.getDimension();

        if (dimension == 0) {
            generateOre(ModBlocks.COPPER_ORE.getDefaultState(), world, random, chunkX, chunkZ, 32, 96, 8, 12);
            generateOre(ModBlocks.SILVER_ORE.getDefaultState(), world, random, chunkX, chunkZ, 8, 48, 6, 8);
        } else if (dimension == -1) {
            generateOre(ModBlocks.IESNIUM_ORE_NATURAL.getDefaultState(), world, random, chunkX, chunkZ, 8, 64, 4, 4,
                    NETHERRACK);
        }
    }

    private static void generateOre(IBlockState state, World world, Random random, int chunkX, int chunkZ,
            int minY, int maxY, int veinSize, int attempts) {
        for (int i = 0; i < attempts; i++) {
            BlockPos pos = randomPos(random, chunkX, chunkZ, minY, maxY);
            new WorldGenMinable(state, veinSize).generate(world, random, pos);
        }
    }

    private static void generateOre(IBlockState state, World world, Random random, int chunkX, int chunkZ,
            int minY, int maxY, int veinSize, int attempts, Predicate<IBlockState> predicate) {
        for (int i = 0; i < attempts; i++) {
            BlockPos pos = randomPos(random, chunkX, chunkZ, minY, maxY);
            new WorldGenMinable(state, veinSize, predicate).generate(world, random, pos);
        }
    }

    private static BlockPos randomPos(Random random, int chunkX, int chunkZ, int minY, int maxY) {
        int x = chunkX * 16 + random.nextInt(16);
        int y = minY + random.nextInt(maxY - minY + 1);
        int z = chunkZ * 16 + random.nextInt(16);
        return new BlockPos(x, y, z);
    }
}
