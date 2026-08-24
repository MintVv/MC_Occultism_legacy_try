package com.occultism.occultism.world;

import java.util.Random;

import com.occultism.occultism.registry.ModBlocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockOre;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

/**
 * Generates a bounded underground source of natural otherworld wood.
 *
 * <p>Unlike the later multi-chunk feature, this 1.12 generator is deliberately
 * limited to the chunk that requested it.</p>
 */
public class WorldGenOtherworldGrove extends WorldGenerator {

    public static final int MIN_GENERATION_Y = 25;
    public static final int MAX_GENERATION_Y = 60;
    public static final int GENERATION_CHANCE = 400;

    private static final int HORIZONTAL_RADIUS = 7;
    private static final int VERTICAL_RADIUS = 11;
    private static final int INNER_HORIZONTAL_RADIUS = 5;
    private static final int INNER_VERTICAL_RADIUS = 9;
    private static final int FLOOR_OFFSET = 7;
    private static final int TREE_HEIGHT_MARGIN = 7;

    private static final WorldGenOtherworldTree NATURAL_TREE = new WorldGenOtherworldTree(true, true);

    public WorldGenOtherworldGrove(boolean notify) {
        super(notify);
    }

    @Override
    public boolean generate(World world, Random random, BlockPos pos) {
        if (world.provider.getDimension() != 0 || pos.getY() < MIN_GENERATION_Y
                || pos.getY() > MAX_GENERATION_Y || world.canSeeSky(pos)) {
            return false;
        }
        if (!this.isAreaLoaded(world, pos) || !this.hasSafeTerrain(world, pos)) {
            return false;
        }

        IBlockState[][][] original = this.snapshot(world, pos);
        this.carveChamber(world, pos);
        this.placeFloor(world, pos);

        BlockPos treePos = pos.down(FLOOR_OFFSET - 1);
        if (!NATURAL_TREE.generate(world, random, treePos)) {
            this.restore(world, pos, original);
            return false;
        }

        if (random.nextFloat() < 0.1F) {
            this.placeFlower(world, pos, random);
        }
        if (random.nextFloat() < 0.1F) {
            this.placeCeilingLight(world, pos);
        }
        return true;
    }

    private boolean isAreaLoaded(World world, BlockPos center) {
        return world.isAreaLoaded(center.add(-HORIZONTAL_RADIUS, -VERTICAL_RADIUS, -HORIZONTAL_RADIUS),
                center.add(HORIZONTAL_RADIUS, VERTICAL_RADIUS + TREE_HEIGHT_MARGIN, HORIZONTAL_RADIUS));
    }

    private boolean hasSafeTerrain(World world, BlockPos center) {
        for (int x = -HORIZONTAL_RADIUS; x <= HORIZONTAL_RADIUS; x++) {
            for (int y = -VERTICAL_RADIUS; y <= VERTICAL_RADIUS; y++) {
                for (int z = -HORIZONTAL_RADIUS; z <= HORIZONTAL_RADIUS; z++) {
                    if (!this.isInsideEllipsoid(x, y, z, HORIZONTAL_RADIUS, VERTICAL_RADIUS)) {
                        continue;
                    }
                    BlockPos checkPos = center.add(x, y, z);
                    if (checkPos.getY() <= 0 || checkPos.getY() >= world.getHeight()
                            || world.canSeeSky(checkPos) || !this.isSafeTerrainBlock(world, checkPos)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean isSafeTerrainBlock(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        Material material = state.getMaterial();
        if (block == Blocks.BEDROCK || material.isLiquid() || world.getTileEntity(pos) != null) {
            return false;
        }
        if (block.isAir(state, world, pos) || block.isReplaceable(world, pos)) {
            return true;
        }
        // Granite, diorite, and andesite are metadata variants of Blocks.STONE in 1.12.
        return block == Blocks.STONE || block == Blocks.DIRT || block == Blocks.GRASS
                || block == Blocks.GRAVEL || block == Blocks.REDSTONE_ORE || block == Blocks.LIT_REDSTONE_ORE
                || block instanceof BlockOre;
    }

    private IBlockState[][][] snapshot(World world, BlockPos center) {
        int size = HORIZONTAL_RADIUS * 2 + 1;
        int height = VERTICAL_RADIUS * 2 + 1;
        IBlockState[][][] states = new IBlockState[size][height][size];
        for (int x = -HORIZONTAL_RADIUS; x <= HORIZONTAL_RADIUS; x++) {
            for (int y = -VERTICAL_RADIUS; y <= VERTICAL_RADIUS; y++) {
                for (int z = -HORIZONTAL_RADIUS; z <= HORIZONTAL_RADIUS; z++) {
                    if (!this.isInsideEllipsoid(x, y, z, HORIZONTAL_RADIUS, VERTICAL_RADIUS)) {
                        continue;
                    }
                    states[x + HORIZONTAL_RADIUS][y + VERTICAL_RADIUS][z + HORIZONTAL_RADIUS] =
                            world.getBlockState(center.add(x, y, z));
                }
            }
        }
        return states;
    }

    private void restore(World world, BlockPos center, IBlockState[][][] states) {
        for (int x = -HORIZONTAL_RADIUS; x <= HORIZONTAL_RADIUS; x++) {
            for (int y = -VERTICAL_RADIUS; y <= VERTICAL_RADIUS; y++) {
                for (int z = -HORIZONTAL_RADIUS; z <= HORIZONTAL_RADIUS; z++) {
                    if (!this.isInsideEllipsoid(x, y, z, HORIZONTAL_RADIUS, VERTICAL_RADIUS)) {
                        continue;
                    }
                    this.setBlockAndNotifyAdequately(world, center.add(x, y, z),
                            states[x + HORIZONTAL_RADIUS][y + VERTICAL_RADIUS][z + HORIZONTAL_RADIUS]);
                }
            }
        }
    }

    private void carveChamber(World world, BlockPos center) {
        for (int x = -HORIZONTAL_RADIUS; x <= HORIZONTAL_RADIUS; x++) {
            for (int y = -VERTICAL_RADIUS; y <= VERTICAL_RADIUS; y++) {
                for (int z = -HORIZONTAL_RADIUS; z <= HORIZONTAL_RADIUS; z++) {
                    if (this.isInsideEllipsoid(x, y, z, INNER_HORIZONTAL_RADIUS, INNER_VERTICAL_RADIUS)) {
                        this.setBlockAndNotifyAdequately(world, center.add(x, y, z), Blocks.AIR.getDefaultState());
                    } else if (this.isInsideEllipsoid(x, y, z, HORIZONTAL_RADIUS, VERTICAL_RADIUS)) {
                        this.setBlockAndNotifyAdequately(world, center.add(x, y, z),
                                ModBlocks.OTHERSTONE_NATURAL.getDefaultState());
                    }
                }
            }
        }
    }

    private void placeFloor(World world, BlockPos center) {
        BlockPos floor = center.down(FLOOR_OFFSET);
        for (int x = -INNER_HORIZONTAL_RADIUS + 1; x <= INNER_HORIZONTAL_RADIUS - 1; x++) {
            for (int z = -INNER_HORIZONTAL_RADIUS + 1; z <= INNER_HORIZONTAL_RADIUS - 1; z++) {
                if (this.isInsideEllipsoid(x, -FLOOR_OFFSET, z, INNER_HORIZONTAL_RADIUS, INNER_VERTICAL_RADIUS)) {
                    this.setBlockAndNotifyAdequately(world, floor.add(x, 0, z), Blocks.GRASS.getDefaultState());
                }
            }
        }
    }

    private void placeFlower(World world, BlockPos center, Random random) {
        int x = random.nextInt(INNER_HORIZONTAL_RADIUS * 2 - 1) - INNER_HORIZONTAL_RADIUS + 1;
        int z = random.nextInt(INNER_HORIZONTAL_RADIUS * 2 - 1) - INNER_HORIZONTAL_RADIUS + 1;
        BlockPos flowerPos = center.add(x, -FLOOR_OFFSET + 1, z);
        if (world.isAirBlock(flowerPos) && ModBlocks.OTHERFLOWER_NATURAL.canPlaceBlockAt(world, flowerPos)) {
            this.setBlockAndNotifyAdequately(world, flowerPos, ModBlocks.OTHERFLOWER_NATURAL.getDefaultState());
        }
    }

    private void placeCeilingLight(World world, BlockPos center) {
        BlockPos lightPos = center.up(INNER_VERTICAL_RADIUS + 1);
        if (world.getBlockState(lightPos).getBlock() == ModBlocks.OTHERSTONE_NATURAL) {
            this.setBlockAndNotifyAdequately(world, lightPos, Blocks.GLOWSTONE.getDefaultState());
        }
    }

    private boolean isInsideEllipsoid(int x, int y, int z, int horizontalRadius, int verticalRadius) {
        double horizontal = (double) (x * x + z * z) / (horizontalRadius * horizontalRadius);
        double vertical = (double) (y * y) / (verticalRadius * verticalRadius);
        return horizontal + vertical <= 1.0D;
    }
}
